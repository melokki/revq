package eu.revq

data class DiscoveredOrganization(
    val login: String,
)

data class DiscoveredRepository(
    val nameWithOwner: String,
    val owner: String,
    val archived: Boolean = false,
    val private: Boolean = false,
)

sealed interface OrganizationScope {
    data object Disabled : OrganizationScope
    data object All : OrganizationScope
    data class Selected(val repositories: Set<String>) : OrganizationScope
}

data class RepositoryScopeSelection(
    val organizationScopes: Map<String, OrganizationScope> = emptyMap(),
    val individualRepositories: Set<String> = emptySet(),
) {
    fun activeRepositories(
        discoveredRepositories: List<DiscoveredRepository>,
    ): Set<String> = discoveredRepositories
        .asSequence()
        .filterNot { it.archived }
        .filter { repository ->
            repository.nameWithOwner in individualRepositories ||
                    when (val scope = organizationScopes[repository.owner]) {
                        OrganizationScope.All -> true
                        is OrganizationScope.Selected ->
                            repository.nameWithOwner in scope.repositories
                        OrganizationScope.Disabled,
                        null -> false
                    }
        }
        .map { it.nameWithOwner }
        .toSet()
}

data class RepositoryDiscoveryResult(
    val login: String,
    val organizations: List<DiscoveredOrganization>,
    val repositories: List<DiscoveredRepository>,
)

class GitHubRepositoryDiscovery(
    private val runCommand: (List<String>) -> String,
) : RepositoryCatalogGateway {
    override fun discoverRepositories(organizations: List<String>): List<String> =
        organizations.flatMap { organization ->
            runCommand(
                listOf(
                    "repo", "list", organization,
                    "--limit", "1000",
                    "--json", "nameWithOwner",
                    "--template", "{{range .}}{{.nameWithOwner}}{{\"\\n\"}}{{end}}",
                ),
            )
                .lineSequence()
                .map(String::trim)
                .filter { "/" in it }
                .toList()
        }
            .distinct()
            .sorted()
}

enum class RepositorySuggestionReason {
    ReviewRequested,
    AuthoredByYou,
}

data class RepositorySuggestion(
    val repository: String,
    val reasons: Set<RepositorySuggestionReason>,
    val relevantPullRequestCount: Int,
)

fun suggestRepositories(
    pullRequests: List<PullRequest>,
): List<RepositorySuggestion> = pullRequests
    .groupBy { it.repository.toString() }
    .map { (repository, items) ->
        RepositorySuggestion(
            repository = repository,
            reasons = items.mapTo(linkedSetOf()) {
                when (it.source) {
                    PullRequestSource.ReviewRequest ->
                        RepositorySuggestionReason.ReviewRequested
                    PullRequestSource.Mine ->
                        RepositorySuggestionReason.AuthoredByYou
                }
            },
            relevantPullRequestCount = items.size,
        )
    }
    .sortedWith(
        compareByDescending<RepositorySuggestion> { it.relevantPullRequestCount }
            .thenBy { it.repository },
    )
