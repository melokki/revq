package eu.revq

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class GitHubIntegrationTest {
    @Test
    fun deterministicAdapterExercisesTheSameRefreshSeamAsTheProcessAdapter() = runBlocking {
        val first = pullRequest(1, "acme/mobile")
        val second = pullRequest(2, "acme/server")
        val adapter = DeterministicGitHubIntegrationAdapter(
            identity = GitHubIdentity("bogdan"),
            pullRequestsByRepository = mapOf(
                "acme/mobile" to listOf(first),
                "acme/server" to listOf(first, second),
            ),
        )
        val integration = GitHubIntegration(adapter)

        integration.configureExecutable("/usr/bin/gh")
        val pullRequests = integration.refresh(listOf("acme/mobile", "acme/server"))

        assertEquals(listOf(first, second), pullRequests)
        assertEquals(
            listOf(
                GitHubIntegrationRequest.ConfigureExecutable("/usr/bin/gh"),
                GitHubIntegrationRequest.Authenticate,
                GitHubIntegrationRequest.RefreshRepository("acme/mobile", "bogdan"),
                GitHubIntegrationRequest.RefreshRepository("acme/server", "bogdan"),
            ),
            adapter.requests,
        )
    }

    private fun pullRequest(number: Int, repository: String): PullRequest = PullRequest(
        repository = parseRepo(repository)!!,
        number = number,
        title = "Integration $number",
        url = "https://github.com/$repository/pull/$number",
        updatedAt = "2026-07-07T10:00:00Z",
        source = PullRequestSource.ReviewRequest,
    )
}
