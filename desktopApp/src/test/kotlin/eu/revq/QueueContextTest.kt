package eu.revq

import kotlin.test.Test
import kotlin.test.assertEquals

class QueueContextTest {
    @Test
    fun visibleQueueAppliesScopeAndSearchAsOneContext() {
        val api = pullRequest(1, RepositoryId("acme", "api"), "Fix flaky API test")
        val web = pullRequest(2, RepositoryId("acme", "web"), "Refresh navigation")
        val other = pullRequest(3, RepositoryId("other", "api"), "Fix API docs")

        assertEquals(
            listOf(api),
            QueueContext.visible(
                pullRequests = listOf(api, web, other),
                scope = QueueScopeFilter.Organization("acme"),
                searchQuery = "api",
            ),
        )
    }

    private fun pullRequest(
        number: Int,
        repository: RepositoryId,
        title: String,
    ): PullRequest = PullRequest(
        repository = repository,
        number = number,
        title = title,
        url = "https://github.com/$repository/pull/$number",
        updatedAt = "2026-07-09T10:00:00Z",
        source = PullRequestSource.ReviewRequest,
    )
}
