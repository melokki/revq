package eu.revq

import kotlin.test.Test
import kotlin.test.assertEquals

class SettingsWorkflowTest {
    @Test
    fun draftChangesAreValidatedAndPersistedOnlyWhenApplied() {
        val store = InMemorySettingsStore()
        val applied = mutableListOf<Pair<RevqSettings, RevqSettings>>()
        val workflow = SettingsWorkflow(
            store = store,
            runtime = SettingsRuntimeEffects { previous, current ->
                applied += previous to current
            },
        )
        val draft = SettingsDraft.from(workflow.snapshot.applied).copy(
            autoRefreshIntervalMinutes = "999",
            staleThresholdDays = "0",
            reminderTime = "10:30",
        )

        workflow.apply(SettingsAction.ReplaceDraft(draft))

        assertEquals(RevqSettings(), store.load())
        assertEquals(emptyList(), applied)

        workflow.apply(SettingsAction.Apply)

        val expected = RevqSettings(
            autoRefreshIntervalMinutes = "240",
            staleThresholdDays = "1",
            reminderTime = "10:30",
        )
        assertEquals(expected, workflow.snapshot.applied)
        assertEquals(expected, store.load())
        assertEquals(listOf(RevqSettings() to expected), applied)
    }

    @Test
    fun navigationIsClampedByTheSelectedSettingsSection() {
        val workflow = SettingsWorkflow(InMemorySettingsStore())

        workflow.apply(SettingsAction.SelectSection(SettingsSection.Data))
        workflow.apply(SettingsAction.MoveRow(100))
        workflow.apply(SettingsAction.MoveSection(-1))

        assertEquals(SettingsSection.Reminders, workflow.snapshot.section)
        assertEquals(settingsRowLabels(SettingsSection.Reminders).lastIndex, workflow.snapshot.focusedRowIndex)
    }
}
