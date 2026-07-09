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
}
