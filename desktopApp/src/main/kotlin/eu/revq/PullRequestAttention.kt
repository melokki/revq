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

        return PullRequestAttentionDescription(
            primaryReason = when {
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
            },
            ownStatuses = ownStatuses,
            ownStatus = ownStatus,
            kind = kind,
            needsAction = kind == AttentionKind.Action || kind == AttentionKind.Blocked,
            canMerge = canMerge,
            nextAction = nextAction(
                pullRequest = pullRequest,
                ownStatus = ownStatus,
                handled = handledMarker == pullRequest.updatedMarker,
                canMerge = canMerge,
            ),
        )
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
