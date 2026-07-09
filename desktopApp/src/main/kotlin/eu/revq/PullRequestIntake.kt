package eu.revq

interface PullRequestIntakeGateway : GitHubRefreshGateway, RepositoryCatalogGateway

class PullRequestIntake(
    gateway: PullRequestIntakeGateway,
) {
    private val tracking = RepositoryTracking(gateway)
    private val refresh = GitHubRefresh(gateway)

    suspend fun refreshScope(
        explicitRepositories: List<String>,
        organizations: List<String>,
        onProgress: (GitHubRefreshProgress) -> Unit = {},
    ): List<PullRequest> {
        val repositories = tracking.resolveRefreshTargets(
            explicitRepositories = explicitRepositories,
            organizations = organizations,
        )
        if (repositories.isEmpty()) {
            error("No repositories were found for the configured tracking scope.")
        }
        return refresh.refresh(repositories, onProgress)
    }
}
