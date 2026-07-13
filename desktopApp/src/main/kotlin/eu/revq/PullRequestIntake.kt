package eu.revq

interface PullRequestIntakeGateway : GitHubRefreshGateway

class PullRequestIntake(
    gateway: PullRequestIntakeGateway,
) {
    private val tracking = RepositoryTracking()
    private val refresh = GitHubRefresh(gateway)

    suspend fun refreshSelectedRepositories(
        selectedRepositories: List<String>,
        onProgress: (GitHubRefreshProgress) -> Unit = {},
    ): List<PullRequest> {
        val repositories = tracking.resolveRefreshTargets(
            selectedRepositories = selectedRepositories,
        )
        if (repositories.isEmpty()) {
            error("No repositories are selected for refresh.")
        }
        return refresh.refresh(repositories, onProgress)
    }
}
