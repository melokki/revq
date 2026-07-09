package eu.revq

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class PullRequestIntakeTest {
    @Test
    fun refreshScopeResolvesOrganizationsAndRefreshesTheCombinedRepositorySet() = runBlocking {
        val gateway = object : PullRequestIntakeGateway {
            val refreshedRepositories = mutableListOf<String>()

            override fun discoverRepositories(organizations: List<String>): List<String> {
                assertEquals(listOf("acme"), organizations)
                return listOf("acme/server", "acme/mobile")
            }

            override fun prepareRefresh(): String = "bogdan"

            override fun refreshRepository(
                repository: String,
                login: String,
            ): List<PullRequest> {
                assertEquals("bogdan", login)
                refreshedRepositories += repository
                return listOf(pullRequest(repository, refreshedRepositories.size))
            }
        }

        val progress = mutableListOf<GitHubRefreshProgress>()

        val result = PullRequestIntake(gateway).refreshScope(
            explicitRepositories = listOf("personal/tools", "acme/mobile"),
            organizations = listOf("acme"),
            onProgress = progress::add,
        )

        assertEquals(
            listOf("acme/mobile", "acme/server", "personal/tools"),
            gateway.refreshedRepositories,
        )
        assertEquals(
            listOf("acme/mobile", "acme/server", "personal/tools"),
            result.map { it.repository.toString() },
        )
        assertEquals(
            listOf(
                GitHubRefreshProgress(0, 3, "acme/mobile"),
                GitHubRefreshProgress(1, 3, "acme/server"),
                GitHubRefreshProgress(2, 3, "personal/tools"),
                GitHubRefreshProgress(3, 3, null),
            ),
            progress,
        )
    }

    private fun pullRequest(
        repository: String,
        number: Int,
    ): PullRequest {
        val repo = parseRepo(repository) ?: error("bad fixture")
        return PullRequest(
            repository = repo,
            number = number,
            title = "Pull request $number",
            url = "https://github.com/$repository/pull/$number",
            updatedAt = "2026-07-09T10:00:00Z",
            source = PullRequestSource.ReviewRequest,
        )
    }
}
