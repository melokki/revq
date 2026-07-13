package eu.revq

import java.nio.file.Files
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals

class SettingsStoreTest {
    @Test
    fun testSuiteUsesAnIsolatedHomeForDefaultSettings() {
        assertEquals(
            "test-user-home",
            defaultRevqConfigDirectory().parent.parent.fileName.toString(),
        )
    }

    @Test
    fun fileStoreRoundTripsUserSettingsThroughOneInterface() {
        val directory = createTempDirectory("revq-settings-test")
        val store = FileSettingsStore(directory)
        val settings = RevqSettings(
            repositories = listOf("acme/mobile", "acme/server"),
            organizations = listOf("acme"),
            githubExecutable = "/usr/local/bin/gh",
            githubDetectionSource = "Configured path",
            mutedRepositories = listOf("acme/legacy"),
            autoRefreshEnabled = false,
            autoRefreshIntervalMinutes = "15",
            sortMode = "Updated newest",
            groupByRepository = true,
            staleThresholdDays = "5",
            compactRows = true,
            reminderEnabled = false,
            reminderTime = "10:30",
            reminderDays = "Mon,Wed,Fri",
            quietHours = "20:00-08:00",
            remindOnlyWhenQueueNotClear = false,
            reminderSnoozeMinutes = "120",
        )

        store.save(settings)

        assertEquals(settings, store.load())
        assertEquals(
            listOf("acme/mobile", "acme/server"),
            Files.readAllLines(directory.resolve("repositories.txt")),
        )
    }

    @Test
    fun applyingDiscoveredRepositoriesSurvivesRestartWithoutForgettingOrganization() {
        val directory = createTempDirectory("revq-discovery-settings-test")
        val store = FileSettingsStore(directory)
        val state = AppState(
            settingsStore = store,
            configDirectory = directory,
        ).apply {
            organizationsText = "acme"
            pendingTrackedRepositories = setOf("acme/mobile")
        }

        state.applyTrackingRepositorySelection()

        val restarted = AppState(
            settingsStore = store,
            configDirectory = directory,
        )
        restarted.loadFromDisk()
        assertEquals("acme/mobile", restarted.repositoriesText)
        assertEquals("acme", restarted.organizationsText)
    }
}
