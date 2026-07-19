package eu.revq

import kotlin.test.Test
import kotlin.test.assertEquals

class SettingsKeyboardModelTest {
    @Test
    fun generalKeyboardRowsContainOnlyGeneralWorkspaceSettings() {
        assertEquals(
            listOf(
                "Auto refresh",
                "Refresh interval",
                "Row density",
                "Default grouping",
            ),
            settingsRowLabels(SettingsSection.General),
        )
    }

    @Test
    fun notificationKeyboardRowsMatchTheDedicatedNotificationSection() {
        assertEquals(
            listOf(
                "Review count in tray",
                "New review assignment notifications",
                "Notification sound",
                "Custom WAV file",
                "Test sound",
            ),
            settingsRowLabels(SettingsSection.Notifications),
        )
    }


    @Test
    fun activatingNotificationSoundRowCyclesSoundMode() {
        val state = AppState().apply {
            settingsSectionIndex = SettingsSection.Notifications.ordinal
            settingsFocusedRowIndex = settingsRowLabels(SettingsSection.Notifications).indexOf("Notification sound")
            notificationSoundMode = NotificationSoundMode.Default
        }

        activateFocusedSettingsRow(state)

        assertEquals(NotificationSoundMode.Custom, state.notificationSoundMode)
    }

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
