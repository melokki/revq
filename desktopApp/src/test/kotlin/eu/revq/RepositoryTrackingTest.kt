package eu.revq

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class RepositoryTrackingTest {
    @Test
    fun organizationScopesResolveCurrentRepositoriesAtRefreshTime() = runBlocking {
        val catalog = object : RepositoryCatalogGateway {
            override fun discoverRepositories(organizations: List<String>): List<String> {
                assertEquals(listOf("acme"), organizations)
                return listOf("acme/server", "acme/mobile")
            }
        }

        val targets = RepositoryTracking(catalog).resolveRefreshTargets(
            explicitRepositories = listOf("personal/tools", "acme/mobile"),
            organizations = listOf("acme"),
        )

        assertEquals(
            listOf("acme/mobile", "acme/server", "personal/tools"),
            targets,
        )
    }

    @Test
    fun applyingDiscoveredRepositorySelectionClearsOrganizationRefreshScope() {
        val state = AppState().apply {
            organizationsText = "acme"
            pendingTrackedRepositories = setOf("acme/api")
        }

        state.applyTrackingRepositorySelection()

        assertEquals("acme/api", state.repositoriesText)
        assertEquals("", state.organizationsText)
    }
}
