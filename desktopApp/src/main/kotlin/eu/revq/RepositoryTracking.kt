package eu.revq

interface RepositoryCatalogGateway {
    fun discoverRepositories(organizations: List<String>): List<String>
}

class RepositoryTracking {
    fun resolveRefreshTargets(
        selectedRepositories: List<String>,
    ): List<String> {
        return selectedRepositories
            .map(String::trim)
            .filter(String::isNotEmpty)
            .distinct()
            .sorted()
    }
}
