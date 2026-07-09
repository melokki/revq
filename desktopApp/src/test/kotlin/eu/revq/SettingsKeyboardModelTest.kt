package eu.revq

import kotlin.test.Test
import kotlin.test.assertEquals

class SettingsKeyboardModelTest {
    @Test
    fun reviewKeyboardRowsMatchVisibleReviewSettings() {
        assertEquals(
            listOf("Default sort", "Stale after"),
            settingsRowLabels(SettingsSection.Review),
        )
    }

    @Test
    fun movingRowsStaysWithinTheCurrentSettingsSection() {
        val state = AppState().apply {
            settingsSectionIndex = SettingsSection.General.ordinal
            settingsFocusedRowIndex = 0
        }

        moveSettingsRow(state, 10)

        assertEquals(settingsRowLabels(SettingsSection.General).lastIndex, state.settingsFocusedRowIndex)

        moveSettingsRow(state, -10)

        assertEquals(0, state.settingsFocusedRowIndex)
    }

    @Test
    fun changingSettingsSectionsClampsFocusedRowToNewSection() {
        val state = AppState().apply {
            settingsSectionIndex = SettingsSection.General.ordinal
            settingsFocusedRowIndex = 3
        }

        moveSettingsSection(state, 1)

        assertEquals(SettingsSection.GitHub.ordinal, state.settingsSectionIndex)
        assertEquals(2, state.settingsFocusedRowIndex)
    }

    @Test
    fun activatingFocusedGeneralRowsMutatesSettings() {
        val state = AppState().apply {
            settingsSectionIndex = SettingsSection.General.ordinal
            settingsFocusedRowIndex = 0
            autoRefreshEnabled = true
        }

        activateFocusedSettingsRow(state)

        assertEquals(false, state.autoRefreshEnabled)
    }

    @Test
    fun activatingTrackingDiscoverRowRunsDiscoveryAction() {
        val state = AppState().apply {
            settingsSectionIndex = SettingsSection.Tracking.ordinal
            settingsFocusedRowIndex = settingsRowLabels(SettingsSection.Tracking).indexOf("Discover repositories")
        }

        activateFocusedSettingsRow(state)

        assertEquals("Add one or more organizations before discovering repositories.", state.statusLine)
    }
}
