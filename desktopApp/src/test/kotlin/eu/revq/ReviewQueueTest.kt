package eu.revq

import kotlin.test.Test
import kotlin.test.assertEquals

class ReviewQueueTest {
    @Test
    fun todayQueueCombinesReviewRequestsActionNeededPullRequestsAndReadyPullRequests() {
        val needsReview = pullRequest(1, source = PullRequestSource.ReviewRequest, title = "Needs review")
        val handledReview = pullRequest(2, source = PullRequestSource.ReviewRequest, title = "Handled")
        val blockedMine = pullRequest(
            3,
            source = PullRequestSource.Mine,
            title = "Fix checks",
            checksTotal = 1,
            checksFailing = 1,
        )
        val readyMine = pullRequest(
            4,
            source = PullRequestSource.Mine,
            title = "Ready",
            reviewDecision = "APPROVED",
            mergeable = "MERGEABLE",
            checksTotal = 1,
        )
        val quietMine = pullRequest(5, source = PullRequestSource.Mine, title = "Quiet")

        val visible = ReviewQueue.visible(
            pullRequests = listOf(quietMine, readyMine, blockedMine, handledReview, needsReview),
            view = View.Today,
            searchQuery = "",
            scope = QueueScopeFilter.All,
            handledReviewRecords = mapOf(handledReview.key to handledReview.updatedMarker),
            pinnedPullRequestKeys = emptySet(),
            mutedRepositories = emptySet(),
            sortMode = "Urgency",
            staleThresholdDays = 2,
        )

        assertEquals(
            listOf(needsReview.key, blockedMine.key, readyMine.key),
            visible.map(PullRequest::key),
        )
    }

    private fun pullRequest(
        number: Int,
        source: PullRequestSource,
        title: String,
        checksTotal: Int = 0,
        checksFailing: Int = 0,
        reviewDecision: String? = null,
        mergeable: String? = null,
    ) = PullRequest(
        repository = RepositoryId("acme", "api"),
        number = number,
        title = title,
        url = "https://github.com/acme/api/pull/$number",
        updatedAt = "2026-07-0${number}T10:00:00Z",
        source = source,
        checksTotal = checksTotal,
        checksFailing = checksFailing,
        reviewDecision = reviewDecision,
        mergeable = mergeable,
    )
}
