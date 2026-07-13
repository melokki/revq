package eu.revq

import kotlinx.coroutines.runBlocking
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class RepositoryTrackingTest {
    @Test
    fun organizationsRemainDiscoverySourcesAndDoNotExpandRefreshTargets() = runBlocking {
        val targets = RepositoryTracking().resolveRefreshTargets(
            selectedRepositories = listOf("personal/tools", "acme/mobile"),
        )

        assertEquals(
            listOf("acme/mobile", "personal/tools"),
            targets,
        )
    }

    @Test
    fun applyingDiscoveredRepositorySelectionKeepsOrganizationAsDiscoverySource() {
        val directory = createTempDirectory("revq-repository-tracking-test")
        val state = AppState(
            settingsStore = FileSettingsStore(directory),
            configDirectory = directory,
        ).apply {
            organizationsText = "acme"
            pendingTrackedRepositories = setOf("acme/api")
        }

        state.applyTrackingRepositorySelection()

        assertEquals("acme/api", state.repositoriesText)
        assertEquals("acme", state.organizationsText)
    }

    @Test
    fun organizationWithoutSelectedRepositoriesPromptsDiscoveryInsteadOfRefreshing() {
        val state = AppState().apply {
            organizationsText = "acme"
        }

        state.refresh()

        assertFalse(state.isRefreshing)
        assertEquals(
            "No repositories are selected. Discover repositories and apply a selection first.",
            state.lastRefreshError,
        )
    }
}
