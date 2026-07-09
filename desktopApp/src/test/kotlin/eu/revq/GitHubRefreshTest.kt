package eu.revq

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class GitHubRefreshTest {
    @Test
    fun refreshCollectsRepositoriesAndReportsProgressThroughOneInterface() = runBlocking {
        val shared = pullRequest(42)
        val gateway = object : GitHubRefreshGateway {
            override fun prepareRefresh(): String = "bogdan"

            override fun refreshRepository(
                repository: String,
                login: String,
            ): List<PullRequest> = when (repository) {
                "acme/mobile" -> listOf(shared)
                "acme/server" -> listOf(shared, pullRequest(43))
                else -> emptyList()
            }
        }
        val progress = mutableListOf<GitHubRefreshProgress>()

        val result = GitHubRefresh(gateway).refresh(
            repositories = listOf("acme/mobile", "acme/server"),
            onProgress = progress::add,
        )

        assertEquals(listOf(42, 43), result.map { it.number })
        assertEquals(
            listOf(
                GitHubRefreshProgress(0, 2, "acme/mobile"),
                GitHubRefreshProgress(1, 2, "acme/server"),
                GitHubRefreshProgress(2, 2, null),
            ),
            progress,
        )
    }

    private fun pullRequest(number: Int): PullRequest = PullRequest(
        repository = RepositoryId("acme", "mobile"),
        number = number,
        title = "Pull request $number",
        url = "https://github.com/acme/mobile/pull/$number",
        updatedAt = "2026-07-09T10:00:00Z",
        source = PullRequestSource.ReviewRequest,
    )
}
