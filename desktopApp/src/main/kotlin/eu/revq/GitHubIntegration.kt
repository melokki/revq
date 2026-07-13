package eu.revq

interface GitHubIntegrationAdapter : PullRequestIntakeGateway, RepositoryCatalogGateway, GitHubPreflightGateway {
    fun configureExecutable(path: String)
    fun detectExecutableResult(): GhDetectionResult?
    fun activeIdentity(): GitHubIdentity?
    fun discoverScope(): RepositoryDiscoveryResult
    fun mergePullRequest(pullRequest: PullRequest): String
    fun testConnection(): String
    fun loadProfile(): GitHubProfile
}

data class GitHubProfile(
    val login: String,
    val name: String?,
    val avatarUrl: String?,
    val profileUrl: String,
)

class GitHubIntegration(
    private val adapter: GitHubIntegrationAdapter,
) : PullRequestIntakeGateway, RepositoryCatalogGateway, GitHubPreflightGateway {
    fun configureExecutable(path: String) = adapter.configureExecutable(path)

    fun detectExecutableResult(): GhDetectionResult? = adapter.detectExecutableResult()

    fun detectExecutable(): String? = detectExecutableResult()?.executable

    fun activeIdentity(): GitHubIdentity? = adapter.activeIdentity()

    fun discoverScope(): RepositoryDiscoveryResult = adapter.discoverScope()

    fun mergePullRequest(pullRequest: PullRequest): String = adapter.mergePullRequest(pullRequest)

    fun testConnection(): String = adapter.testConnection()

    fun loadProfile(): GitHubProfile = adapter.loadProfile()

    suspend fun refresh(
        repositories: List<String>,
        onProgress: (GitHubRefreshProgress) -> Unit = {},
    ): List<PullRequest> = PullRequestIntake(this).refreshSelectedRepositories(repositories, onProgress)

    override fun prepareRefresh(): String = adapter.prepareRefresh()

    override fun refreshRepository(repository: String, login: String): List<PullRequest> =
        adapter.refreshRepository(repository, login)

    override fun discoverRepositories(organizations: List<String>): List<String> =
        adapter.discoverRepositories(organizations)

    override suspend fun checkDependency(): GhDependencyResult = adapter.checkDependency()

    override suspend fun checkAuthentication(): GhAuthenticationResult = adapter.checkAuthentication()
}

sealed interface GitHubIntegrationRequest {
    data class ConfigureExecutable(val path: String) : GitHubIntegrationRequest
    data object Authenticate : GitHubIntegrationRequest
    data class RefreshRepository(val repository: String, val login: String) : GitHubIntegrationRequest
    data class DiscoverRepositories(val organizations: List<String>) : GitHubIntegrationRequest
    data object DiscoverScope : GitHubIntegrationRequest
    data class Merge(val pullRequest: PullRequest) : GitHubIntegrationRequest
    data object TestConnection : GitHubIntegrationRequest
    data object LoadProfile : GitHubIntegrationRequest
}

class DeterministicGitHubIntegrationAdapter(
    private val identity: GitHubIdentity = GitHubIdentity("test-user"),
    private val detectionResult: GhDetectionResult = GhDetectionResult("gh", GhDetectionSource.Path),
    private val pullRequestsByRepository: Map<String, List<PullRequest>> = emptyMap(),
    private val repositories: List<String> = emptyList(),
    private val discoverRepositories: ((List<String>) -> List<String>)? = null,
    private val discovery: RepositoryDiscoveryResult = RepositoryDiscoveryResult(
        login = identity.login,
        organizations = emptyList(),
        repositories = emptyList(),
    ),
) : GitHubIntegrationAdapter {
    private var configuredExecutable: String = ""
    val requests = mutableListOf<GitHubIntegrationRequest>()

    override fun configureExecutable(path: String) {
        configuredExecutable = path.trim()
        requests += GitHubIntegrationRequest.ConfigureExecutable(configuredExecutable)
    }

    override fun detectExecutableResult(): GhDetectionResult = detectionResult

    override fun activeIdentity(): GitHubIdentity = identity

    override suspend fun checkDependency(): GhDependencyResult = GhDependencyResult.Available

    override suspend fun checkAuthentication(): GhAuthenticationResult =
        GhAuthenticationResult.Authenticated(identity)

    override fun prepareRefresh(): String {
        requests += GitHubIntegrationRequest.Authenticate
        return identity.login
    }

    override fun refreshRepository(repository: String, login: String): List<PullRequest> {
        requests += GitHubIntegrationRequest.RefreshRepository(repository, login)
        return pullRequestsByRepository[repository].orEmpty()
    }

    override fun discoverRepositories(organizations: List<String>): List<String> {
        requests += GitHubIntegrationRequest.DiscoverRepositories(organizations)
        return discoverRepositories?.invoke(organizations) ?: repositories
    }

    override fun discoverScope(): RepositoryDiscoveryResult {
        requests += GitHubIntegrationRequest.DiscoverScope
        return discovery
    }

    override fun mergePullRequest(pullRequest: PullRequest): String {
        requests += GitHubIntegrationRequest.Merge(pullRequest)
        return "Merged ${pullRequest.repository} #${pullRequest.number}"
    }

    override fun testConnection(): String {
        requests += GitHubIntegrationRequest.TestConnection
        return "✓ gh works · authenticated as ${identity.login} · ${configuredExecutable.ifBlank { detectionResult.executable }}"
    }

    override fun loadProfile(): GitHubProfile {
        requests += GitHubIntegrationRequest.LoadProfile
        return GitHubProfile(
            login = identity.login,
            name = identity.login,
            avatarUrl = null,
            profileUrl = "https://github.com/${identity.login}",
        )
    }
}
