package eu.revq

import java.util.concurrent.CountDownLatch
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class AppStateDiscoveryTest {
    @Test
    fun discoveringConfiguredOrganizationPublishesRepositories() {
        val catalog = object : RepositoryCatalogGateway {
            override fun discoverRepositories(organizations: List<String>): List<String> {
                assertEquals(listOf("acme"), organizations)
                return listOf("acme/mobile", "acme/server")
            }
        }
        val state = AppState(repositoryCatalog = catalog).apply {
            organizationsText = "acme"
        }

        state.discoverTargets()
        waitUntil { !state.isDiscovering }

        assertFalse(state.isDiscovering)
        assertEquals(listOf("acme/mobile", "acme/server"), state.discoveredTrackingRepositories)
        assertEquals(
            "Discovered 2 repositories · 0 currently tracked · 2 available to add",
            state.statusLine,
        )
    }

    @Test
    fun stalledDiscoveryStopsWithUsefulRetryState() {
        val neverCompletes = CountDownLatch(1)
        val catalog = object : RepositoryCatalogGateway {
            override fun discoverRepositories(organizations: List<String>): List<String> {
                neverCompletes.await()
                return emptyList()
            }
        }
        val state = AppState(
            repositoryCatalog = catalog,
            discoveryTimeoutMillis = 50,
        ).apply {
            organizationsText = "acme"
        }

        state.discoverTargets()
        waitUntil { !state.isDiscovering }

        assertFalse(state.isDiscovering)
        assertEquals(emptyList(), state.discoveredTrackingRepositories)
        assertEquals(
            "Discover failed · GitHub repository discovery timed out. Check your connection and try again.",
            state.statusLine,
        )
    }

    @Test
    fun startingDiscoveryClearsStaleRefreshErrorSoProgressIsVisible() {
        val releaseDiscovery = CountDownLatch(1)
        val catalog = object : RepositoryCatalogGateway {
            override fun discoverRepositories(organizations: List<String>): List<String> {
                releaseDiscovery.await()
                return emptyList()
            }
        }
        val state = AppState(repositoryCatalog = catalog).apply {
            organizationsText = "acme"
            lastRefreshError = "Previous refresh failed"
        }

        try {
            state.discoverTargets()

            assertEquals(null, state.lastRefreshError)
            assertEquals("Discovering repositories…", state.statusLine)
        } finally {
            releaseDiscovery.countDown()
        }
    }
}

private fun waitUntil(
    timeoutMillis: Long = 2_000,
    condition: () -> Boolean,
) {
    val deadline = System.currentTimeMillis() + timeoutMillis
    while (!condition() && System.currentTimeMillis() < deadline) {
        Thread.sleep(10)
    }
}
