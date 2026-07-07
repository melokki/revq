package eu.revq

import kotlin.test.Test
import kotlin.test.assertEquals

class SettingsKeyboardModelTest {
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
}
