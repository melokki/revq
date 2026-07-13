package eu.revq

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PullRequestAttentionTest {
    @Test
    fun attentionDescriptionClassifiesBlockedPersonalPullRequest() {
        val pullRequest = PullRequest(
            repository = RepositoryId("acme", "mobile"),
            number = 40,
            title = "Needs another pass",
            url = "https://github.com/acme/mobile/pull/40",
            updatedAt = "2026-07-09T10:00:00Z",
            source = PullRequestSource.Mine,
            reviewDecision = "CHANGES_REQUESTED",
        )

        val description = PullRequestAttention.describe(pullRequest)

        assertEquals(AttentionKind.Blocked, description.kind)
        assertEquals(true, description.needsAction)
    }

    @Test
    fun teamReviewRequestPreservesWhyPullRequestNeedsAttention() {
        val pullRequest = PullRequest(
            repository = RepositoryId("acme", "mobile"),
            number = 41,
            title = "Team-owned change",
            url = "https://github.com/acme/mobile/pull/41",
            updatedAt = "2026-07-09T10:00:00Z",
            source = PullRequestSource.ReviewRequest,
            reviewRequestKind = ReviewRequestKind.Team,
        )

        assertEquals(
            AttentionReason.TeamReviewRequest,
            PullRequestAttention.describe(pullRequest).primaryReason,
        )
    }

    @Test
    fun directReviewRequestTakesPriorityOverAssignment() {
        assertEquals(
            ReviewRequestKind.Direct,
            classifyReviewRequest(
                viewerLogin = "bogdan",
                requestedReviewers = listOf("@bogdan", "team/mobile"),
                assignees = listOf("@bogdan"),
            ),
        )
    }

    @Test
    fun approvedCleanPersonalPullRequestIsDescribedAsReadyToMerge() {
        val pullRequest = PullRequest(
            repository = RepositoryId("acme", "mobile"),
            number = 42,
            title = "Ship keyboard navigation",
            url = "https://github.com/acme/mobile/pull/42",
            updatedAt = "2026-07-09T10:00:00Z",
            source = PullRequestSource.Mine,
            reviewDecision = "APPROVED",
            mergeable = "MERGEABLE",
            mergeStateStatus = "CLEAN",
            unresolvedDiscussionCount = 0,
        )

        val description = PullRequestAttention.describe(pullRequest)

        assertEquals(OwnPullRequestStatus.ApprovedAndReady, description.ownStatus)
        assertTrue(description.canMerge)
        assertEquals(
            "Approved, clean, and comment-free. Press m to merge this pull request.",
            description.nextAction,
        )
    }

    @Test
    fun approvedPersonalPullRequestHasOneCanonicalPresentation() {
        val pullRequest = PullRequest(
            repository = RepositoryId("acme", "mobile"),
            number = 43,
            title = "Ship the workspace",
            url = "https://github.com/acme/mobile/pull/43",
            updatedAt = "2026-07-09T10:00:00Z",
            source = PullRequestSource.Mine,
            reviewDecision = "APPROVED",
            mergeable = "MERGEABLE",
            mergeStateStatus = "CLEAN",
            unresolvedDiscussionCount = 0,
        )

        assertEquals(
            PullRequestPresentation(
                statusTitle = "Approved and ready",
                statusBody = "The pull request is approved, checks are clear, and no merge conflict is detected. It looks ready to move forward.",
                rowStatus = "Approved and ready",
                selectedContext = emptyList(),
            ),
            PullRequestAttention.describe(pullRequest).presentation,
        )
    }

    @Test
    fun everyPersonalPullRequestStatusHasCanonicalCopy() {
        val pullRequest = PullRequest(
            repository = RepositoryId("acme", "mobile"),
            number = 44,
            title = "Address feedback",
            url = "https://github.com/acme/mobile/pull/44",
            updatedAt = "2026-07-09T10:00:00Z",
            source = PullRequestSource.Mine,
            reviewDecision = "CHANGES_REQUESTED",
            changeRequestReviewers = listOf("alice"),
            checksFailing = 1,
        )

        assertEquals(
            listOf(
                OwnPullRequestStatusPresentation(
                    status = OwnPullRequestStatus.ChangesRequested,
                    title = "Changes requested",
                    body = "alice requested changes. Review the feedback and update the pull request before it can move forward.",
                    rowStatus = "Changes requested by alice",
                ),
                OwnPullRequestStatusPresentation(
                    status = OwnPullRequestStatus.ChecksFailing,
                    title = "Checks failing",
                    body = "1 check is failing. Open GitHub to inspect the failures and decide what needs to change.",
                    rowStatus = "Checks failing",
                ),
            ),
            PullRequestAttention.describe(pullRequest).ownStatusPresentations,
        )
    }
}
