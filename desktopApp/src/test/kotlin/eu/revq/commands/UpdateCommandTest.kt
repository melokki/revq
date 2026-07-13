package eu.revq.commands

import eu.revq.AppState
import eu.revq.AppVersion
import eu.revq.InMemoryUpdatePreferencesStore
import eu.revq.ReleaseCandidate
import eu.revq.ReleaseSource
import eu.revq.UpdateService
import eu.revq.UpdateState
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UpdateCommandTest {
    @Test
    fun `update commands expose one action for download install and restart`() {
        val install = CommandRegistry.find(CommandId.InstallUpdate)!!

        assertTrue(install.description.orEmpty().contains("restart", ignoreCase = true))
        assertTrue(CommandRegistry.all().none { it.title == "Open update installer" })
    }

    @Test
    fun `dismiss update command uses the same state transition as the banner`() = runBlocking {
        val state = AppState(
            updateService = UpdateService(
                installedVersion = AppVersion.parse("0.1.0"),
                releaseSource = ReleaseSource { listOf(ReleaseCandidate("v0.2.0", "RevQ")) },
                preferences = InMemoryUpdatePreferencesStore(),
            ),
        )
        state.checkForUpdatesNow()

        val result = CommandRegistry.execute(CommandId.DismissUpdate, state)

        assertEquals(CommandExecutionResult.Executed, result)
        assertTrue((state.updateState as UpdateState.Available).dismissed)
        assertTrue(CommandRegistry.find(CommandId.DismissUpdate)!!.aliases.contains("version"))
    }
}
