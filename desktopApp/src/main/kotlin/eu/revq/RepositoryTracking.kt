package eu.revq

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface RepositoryCatalogGateway {
    fun discoverRepositories(organizations: List<String>): List<String>
}

class RepositoryTracking(
    private val catalog: RepositoryCatalogGateway,
) {
    suspend fun resolveRefreshTargets(
        explicitRepositories: List<String>,
        organizations: List<String>,
    ): List<String> {
        val discovered = if (organizations.isEmpty()) {
            emptyList()
        } else {
            withContext(Dispatchers.IO) {
                catalog.discoverRepositories(organizations)
            }
        }

        return (explicitRepositories + discovered)
            .map(String::trim)
            .filter(String::isNotEmpty)
            .distinct()
            .sorted()
    }
}
