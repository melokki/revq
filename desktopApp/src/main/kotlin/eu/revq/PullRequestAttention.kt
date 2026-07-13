package eu.revq

enum class ReviewRequestKind {
    Direct,
    Team,
    Assignment,
}

fun classifyReviewRequest(
    viewerLogin: String,
    requestedReviewers: List<String>,
    assignees: List<String>,
): ReviewRequestKind = when {
    requestedReviewers.any {
        it.removePrefix("@").equals(viewerLogin, ignoreCase = true)
    } -> ReviewRequestKind.Direct
    requestedReviewers.any { it.startsWith("team/", ignoreCase = true) } ->
        ReviewRequestKind.Team
    assignees.any {
        it.removePrefix("@").equals(viewerLogin, ignoreCase = true)
    } -> ReviewRequestKind.Assignment
    else -> ReviewRequestKind.Direct
}

data class PullRequestAttentionDescription(
    val primaryReason: AttentionReason,
    val ownStatuses: List<OwnPullRequestStatus>,
    val ownStatus: OwnPullRequestStatus?,
    val kind: AttentionKind,
    val needsAction: Boolean,
    val canMerge: Boolean,
    val nextAction: String,
    val presentation: PullRequestPresentation,
    val ownStatusPresentations: List<OwnPullRequestStatusPresentation>,
)

data class PullRequestPresentation(
    val statusTitle: String,
    val statusBody: String,
    val rowStatus: String,
    val selectedContext: List<String>,
    val recommendationTitle: String = statusTitle,
    val recommendationBody: String = statusBody,
    val keyboardAction: PullRequestKeyboardAction? = null,
)

data class PullRequestKeyboardAction(
    val key: String,
    val label: String,
)

data class OwnPullRequestStatusPresentation(
    val status: OwnPullRequestStatus,
    val title: String,
    val body: String,
    val rowStatus: String,
)

object PullRequestAttention {
    fun describe(
        pullRequest: PullRequest,
        handledMarker: String? = null,
    ): PullRequestAttentionDescription {
        val ownStatuses = ownStatuses(pullRequest)
        val ownStatus = ownStatuses.firstOrNull()
        val kind = attentionKind(pullRequest, ownStatus)
        val canMerge = isReadyToMerge(pullRequest)
        val primaryReason = when {
            pullRequest.source == PullRequestSource.ReviewRequest -> when (pullRequest.reviewRequestKind) {
                ReviewRequestKind.Team -> AttentionReason.TeamReviewRequest
                ReviewRequestKind.Assignment -> AttentionReason.Assignment
                ReviewRequestKind.Direct,
                null -> AttentionReason.DirectReviewRequest
            }
            ownStatus == OwnPullRequestStatus.ChangesRequested ->
                AttentionReason.ChangesRequested
            ownStatus == OwnPullRequestStatus.ChecksFailing ->
                AttentionReason.FailingCi
            else -> AttentionReason.Other
        }
        val nextAction = nextAction(
            pullRequest = pullRequest,
            ownStatus = ownStatus,
            handled = handledMarker == pullRequest.updatedMarker,
            canMerge = canMerge,
        )
        val ownStatusPresentations = ownStatuses.map { status ->
            OwnPullRequestStatusPresentation(
                status = status,
                title = statusTitle(status),
                body = statusBody(status, pullRequest),
                rowStatus = rowStatus(pullRequest, status),
            )
        }
        val primaryStatusPresentation = ownStatusPresentations.firstOrNull()

        return PullRequestAttentionDescription(
            primaryReason = primaryReason,
            ownStatuses = ownStatuses,
            ownStatus = ownStatus,
            kind = kind,
            needsAction = kind == AttentionKind.Action || kind == AttentionKind.Blocked,
            canMerge = canMerge,
            nextAction = nextAction,
            presentation = PullRequestPresentation(
                statusTitle = primaryStatusPresentation?.title ?: primaryReason.label,
                statusBody = primaryStatusPresentation?.body ?: nextAction,
                rowStatus = primaryStatusPresentation?.rowStatus ?: primaryReason.label,
                selectedContext = selectedContext(pullRequest),
                recommendationTitle = primaryStatusPresentation?.title ?: "Review this pull request",
                recommendationBody = primaryStatusPresentation?.body
                    ?: "Open the diff, check the changes, and leave an approval, comment, or change request.",
                keyboardAction = when {
                    pullRequest.source == PullRequestSource.ReviewRequest ->
                        PullRequestKeyboardAction("m", "Mark reviewed & next")
                    kind in setOf(AttentionKind.Action, AttentionKind.Blocked) ->
                        PullRequestKeyboardAction("o", "Open PR")
                    else -> null
                },
            ),
            ownStatusPresentations = ownStatusPresentations,
        )
    }

    private fun selectedContext(pullRequest: PullRequest): List<String> = buildList {
        when {
            pullRequest.checksFailing > 0 -> add("${pullRequest.checksFailing} CI failing")
            pullRequest.checksPending > 0 -> add("${pullRequest.checksPending} CI pending")
            pullRequest.checksTotal > 0 -> add("CI passing")
        }
        pullRequest.approvingReviewers
            .distinct()
            .size
            .takeIf { it > 0 }
            ?.let { count -> add(if (count == 1) "1 approval" else "$count approvals") }
    }

    private fun rowStatus(
        pullRequest: PullRequest,
        status: OwnPullRequestStatus,
    ): String = when (status) {
        OwnPullRequestStatus.ChangesRequested ->
            formatIdentityList(pullRequest.changeRequestReviewers, maxVisible = 1)
                ?.let { "Changes requested by $it" }
                ?: statusTitle(status)

        OwnPullRequestStatus.WaitingForReviewer ->
            formatIdentityList(pullRequest.requestedReviewers, maxVisible = 2)
                ?.let { "Waiting on $it" }
                ?: statusTitle(status)

        OwnPullRequestStatus.ApprovedAndReady ->
            formatIdentityList(pullRequest.approvingReviewers, maxVisible = 2)
                ?.let { "Approved by $it" }
                ?: statusTitle(status)

        OwnPullRequestStatus.DiscussionNeedsResponse ->
            formatIdentityList(pullRequest.unresolvedDiscussionAuthors, maxVisible = 1)
                ?.let { "Open discussion with $it" }
                ?: statusTitle(status)

        else -> statusTitle(status)
    }

    private fun statusTitle(status: OwnPullRequestStatus): String = when (status) {
        OwnPullRequestStatus.Draft -> "Draft"
        OwnPullRequestStatus.ChangesRequested -> "Changes requested"
        OwnPullRequestStatus.MergeConflict -> "Merge conflict"
        OwnPullRequestStatus.ChecksFailing -> "Checks failing"
        OwnPullRequestStatus.DiscussionNeedsResponse -> "Discussion needs response"
        OwnPullRequestStatus.ApprovedAndReady -> "Approved and ready"
        OwnPullRequestStatus.WaitingForReviewer -> "Waiting for reviewer"
        OwnPullRequestStatus.NoActionNeeded -> "No action needed"
    }

    private fun statusBody(
        status: OwnPullRequestStatus,
        pullRequest: PullRequest,
    ): String = when (status) {
        OwnPullRequestStatus.Draft ->
            "This pull request is still a draft. Keep working on it, or mark it ready for review when the change is ready."

        OwnPullRequestStatus.ChangesRequested -> {
            val reviewers = formatIdentityList(pullRequest.changeRequestReviewers)
            if (reviewers != null) {
                "$reviewers requested changes. Review the feedback and update the pull request before it can move forward."
            } else {
                "A reviewer requested changes. Review the feedback and update the pull request before it can move forward."
            }
        }

        OwnPullRequestStatus.MergeConflict ->
            "GitHub reports that this pull request cannot merge cleanly. Update the branch and resolve the merge conflict."

        OwnPullRequestStatus.ChecksFailing ->
            "${pullRequest.checksFailing} ${if (pullRequest.checksFailing == 1) "check is" else "checks are"} failing. Open GitHub to inspect the failures and decide what needs to change."

        OwnPullRequestStatus.DiscussionNeedsResponse -> {
            val count = pullRequest.unresolvedDiscussionCount ?: 0
            val authors = formatIdentityList(pullRequest.unresolvedDiscussionAuthors)
            buildString {
                append("$count ${if (count == 1) "review discussion is" else "review discussions are"} still unresolved.")
                if (authors != null) append(" Open ${if (count == 1) "thread involves" else "threads involve"} $authors.")
                append(" Open the review threads and respond or resolve them as appropriate.")
            }
        }

        OwnPullRequestStatus.ApprovedAndReady -> {
            val reviewers = formatIdentityList(pullRequest.approvingReviewers)
            if (reviewers != null) {
                "Approved by $reviewers. Checks are clear and no merge conflict is detected. It looks ready to move forward."
            } else {
                "The pull request is approved, checks are clear, and no merge conflict is detected. It looks ready to move forward."
            }
        }

        OwnPullRequestStatus.WaitingForReviewer -> {
            val reviewers = formatIdentityList(pullRequest.requestedReviewers)
            if (reviewers != null) {
                "Waiting on $reviewers. No action is required from you right now; RevQ will keep tracking the pull request for new activity."
            } else {
                "No action is required from you right now. The pull request is open and waiting for review."
            }
        }

        OwnPullRequestStatus.NoActionNeeded ->
            "RevQ found no current signal that requires your attention. You can leave this pull request alone for now."
    }

    private fun attentionKind(
        pullRequest: PullRequest,
        ownStatus: OwnPullRequestStatus?,
    ): AttentionKind = when {
        pullRequest.source == PullRequestSource.ReviewRequest -> AttentionKind.Review
        ownStatus in setOf(
            OwnPullRequestStatus.ChangesRequested,
            OwnPullRequestStatus.MergeConflict,
            OwnPullRequestStatus.ChecksFailing,
        ) -> AttentionKind.Blocked
        ownStatus == OwnPullRequestStatus.DiscussionNeedsResponse -> AttentionKind.Action
        ownStatus == OwnPullRequestStatus.ApprovedAndReady -> AttentionKind.Ready
        else -> AttentionKind.Quiet
    }

    private fun ownStatuses(pullRequest: PullRequest): List<OwnPullRequestStatus> {
        if (pullRequest.source != PullRequestSource.Mine) return emptyList()

        return buildList {
            if (pullRequest.isDraft) {
                add(OwnPullRequestStatus.Draft)
            }
            if (pullRequest.reviewDecision.equals("CHANGES_REQUESTED", ignoreCase = true)) {
                add(OwnPullRequestStatus.ChangesRequested)
            }
            if (
                pullRequest.mergeable.equals("CONFLICTING", ignoreCase = true) ||
                pullRequest.mergeStateStatus.equals("DIRTY", ignoreCase = true)
            ) {
                add(OwnPullRequestStatus.MergeConflict)
            }
            if (pullRequest.checksFailing > 0) {
                add(OwnPullRequestStatus.ChecksFailing)
            }
            if (pullRequest.discussionNeedsResponse) {
                add(OwnPullRequestStatus.DiscussionNeedsResponse)
            }

            val mergeStateAllowsReady = pullRequest.mergeStateStatus == null ||
                    pullRequest.mergeStateStatus.uppercase() !in
                    setOf("BLOCKED", "BEHIND", "DIRTY", "DRAFT", "UNKNOWN")
            val approvedAndReady =
                !pullRequest.isDraft &&
                        pullRequest.reviewDecision.equals("APPROVED", ignoreCase = true) &&
                        pullRequest.checksFailing == 0 &&
                        pullRequest.checksPending == 0 &&
                        pullRequest.mergeable.equals("MERGEABLE", ignoreCase = true) &&
                        mergeStateAllowsReady

            if (approvedAndReady) {
                add(OwnPullRequestStatus.ApprovedAndReady)
            }
            if (
                !pullRequest.isDraft &&
                !approvedAndReady &&
                (
                    pullRequest.reviewRequestsCount > 0 ||
                    pullRequest.reviewDecision.equals("REVIEW_REQUIRED", ignoreCase = true)
                )
            ) {
                add(OwnPullRequestStatus.WaitingForReviewer)
            }
            if (isEmpty()) {
                add(OwnPullRequestStatus.NoActionNeeded)
            }
        }.distinct()
    }

    private fun isReadyToMerge(pullRequest: PullRequest): Boolean {
        val mergeState = pullRequest.mergeStateStatus?.uppercase()
        val mergeStateAllowsReady =
            mergeState == null ||
                    mergeState !in setOf("BLOCKED", "BEHIND", "DIRTY", "DRAFT", "UNKNOWN")

        return pullRequest.source == PullRequestSource.Mine &&
                !pullRequest.isDraft &&
                pullRequest.reviewDecision.equals("APPROVED", ignoreCase = true) &&
                pullRequest.comments == 0 &&
                pullRequest.unresolvedDiscussionCount == 0 &&
                pullRequest.checksFailing == 0 &&
                pullRequest.checksPending == 0 &&
                pullRequest.mergeable.equals("MERGEABLE", ignoreCase = true) &&
                mergeStateAllowsReady
    }

    private fun nextAction(
        pullRequest: PullRequest,
        ownStatus: OwnPullRequestStatus?,
        handled: Boolean,
        canMerge: Boolean,
    ): String {
        if (pullRequest.source == PullRequestSource.ReviewRequest) {
            return when {
                handled -> "No action required unless new activity appears."
                pullRequest.isDraft -> "Inspect only if useful; this pull request is still a draft."
                pullRequest.checksFailing > 0 -> "Review the diff with the failing checks in context."
                pullRequest.mergeable.equals("CONFLICTING", ignoreCase = true) ||
                        pullRequest.mergeStateStatus.equals("DIRTY", ignoreCase = true) ->
                    "Review the diff, but expect the author to resolve the merge conflict."
                (pullRequest.unresolvedDiscussionCount ?: 0) > 0 ->
                    "Review the diff and pay attention to the unresolved discussion context."
                else -> "Review the diff and leave a decision."
            }
        }

        return when (ownStatus) {
            OwnPullRequestStatus.ChangesRequested ->
                "Address the requested changes and push an update."
            OwnPullRequestStatus.MergeConflict ->
                "Resolve the merge conflict before moving the pull request forward."
            OwnPullRequestStatus.ChecksFailing ->
                "Fix the failing checks before requesting another review."
            OwnPullRequestStatus.DiscussionNeedsResponse ->
                "Respond to or resolve the open review discussions."
            OwnPullRequestStatus.ApprovedAndReady ->
                if (canMerge) {
                    "Approved, clean, and comment-free. Press m to merge this pull request."
                } else {
                    "This pull request looks ready to move forward."
                }
            OwnPullRequestStatus.Draft ->
                "Continue working, or mark it ready when the change is ready."
            OwnPullRequestStatus.WaitingForReviewer,
            OwnPullRequestStatus.NoActionNeeded,
            null -> "No action required from you right now."
        }
    }
}
