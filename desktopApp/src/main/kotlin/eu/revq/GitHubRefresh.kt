package eu.revq

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface GitHubRefreshGateway {
    fun prepareRefresh(): String

    fun refreshRepository(
        repository: String,
        login: String,
    ): List<PullRequest>
}

data class GitHubRefreshProgress(
    val completed: Int,
    val total: Int,
    val repository: String?,
)

class GitHubRefresh(
    private val gateway: GitHubRefreshGateway,
) {
    suspend fun refresh(
        repositories: List<String>,
        onProgress: (GitHubRefreshProgress) -> Unit = {},
    ): List<PullRequest> {
        val login = withContext(Dispatchers.IO) {
            gateway.prepareRefresh()
        }
        val collected = mutableListOf<PullRequest>()

        repositories.forEachIndexed { index, repository ->
            onProgress(GitHubRefreshProgress(index, repositories.size, repository))
            collected += withContext(Dispatchers.IO) {
                gateway.refreshRepository(repository, login)
            }
        }

        onProgress(GitHubRefreshProgress(repositories.size, repositories.size, null))
        return dedupePullRequests(collected)
    }
}
