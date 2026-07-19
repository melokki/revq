package eu.revq

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.DataObject
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.ui.graphics.vector.ImageVector

enum class SettingsSection(
    val label: String,
    val icon: ImageVector,
) {
    General("General", Icons.Rounded.Settings),
    GitHub("GitHub", Icons.Rounded.Code),
    Tracking("Tracking", Icons.Rounded.Folder),
    Review("Review", Icons.Rounded.Tune),
    Notifications("Notifications", Icons.Rounded.VolumeUp),
    Reminders("Reminders", Icons.Rounded.Notifications),
    Data("Data & diagnostics", Icons.Rounded.DataObject),
}

private data class SettingsInteractionRow(
    val label: String,
    val activate: (AppState) -> Unit,
)

private fun settingsRows(section: SettingsSection): List<SettingsInteractionRow> = when (section) {
    SettingsSection.General -> listOf(
        SettingsInteractionRow("Auto refresh") {
            it.autoRefreshEnabled = !it.autoRefreshEnabled
            it.saveConfig()
        },
        SettingsInteractionRow("Refresh interval") { state ->
            cycleValue(state.autoRefreshIntervalMinutesText, listOf("1", "5", "10", "15", "30", "60")) {
                state.autoRefreshIntervalMinutesText = it
                state.saveConfig()
            }
        },
        SettingsInteractionRow("Row density") {
            it.compactRows = !it.compactRows
            it.saveConfig()
        },
        SettingsInteractionRow("Default grouping") {
            it.groupByRepository = !it.groupByRepository
            it.saveConfig()
        },
    )

    SettingsSection.GitHub -> listOf(
        SettingsInteractionRow("Test connection", AppState::testGithubCli),
        SettingsInteractionRow("Run auto-detection again", AppState::autoDetectGithubCli),
        SettingsInteractionRow("Executable override") {
            it.statusLine = "Tab to the executable field, edit, then press Apply override."
        },
    )

    SettingsSection.Tracking -> listOf(
        SettingsInteractionRow("Tracked repositories") {
            it.statusLine = "Tab to the text field or row action, then press Enter."
        },
        SettingsInteractionRow("Organizations") {
            it.statusLine = "Tab to the text field or row action, then press Enter."
        },
        SettingsInteractionRow("Discover repositories", AppState::discoverTargets),
        SettingsInteractionRow("Muted repositories") {
            it.statusLine = "Tab to the text field or row action, then press Enter."
        },
    )

    SettingsSection.Review -> listOf(
        SettingsInteractionRow("Default sort") { state ->
            cycleValue(state.sortMode, WorkspaceSortModes) {
                state.sortMode = it
                state.saveConfig()
            }
        },
        SettingsInteractionRow("Stale after") { state ->
            cycleValue(state.staleThresholdDaysText, listOf("1", "2", "3", "5", "7", "14")) {
                state.staleThresholdDaysText = it
                state.saveConfig()
            }
        },
    )

    SettingsSection.Notifications -> listOf(
        SettingsInteractionRow("Review count in tray") {
            it.showReviewCountInTray = !it.showReviewCountInTray
            it.saveConfig()
        },
        SettingsInteractionRow("New review assignment notifications") {
            it.notifyOnNewReviewAssignments = !it.notifyOnNewReviewAssignments
            it.saveConfig()
        },
        SettingsInteractionRow("Notification sound") { state ->
            cycleValue(state.notificationSoundMode, NotificationSoundMode.entries) {
                state.notificationSoundMode = it
                state.saveConfig()
            }
        },
        SettingsInteractionRow("Custom WAV file", AppState::chooseCustomNotificationSound),
        SettingsInteractionRow("Test sound", AppState::testNotificationSound),
    )

    SettingsSection.Reminders -> listOf(
        SettingsInteractionRow("Scheduled reminders") {
            it.reminderEnabled = !it.reminderEnabled
            it.saveConfig()
        },
        SettingsInteractionRow("Reminder time") { state ->
            cycleValue(state.reminderTimeText, reminderTimeOptions()) {
                state.reminderTimeText = it
                state.saveConfig()
            }
        },
        SettingsInteractionRow("Reminder days") {
            it.statusLine = "Use Tab to reach individual reminder day chips."
        },
        SettingsInteractionRow("Snooze duration") { state ->
            cycleValue(state.reminderSnoozeMinutesText, listOf("15", "30", "60", "120", "240")) {
                state.reminderSnoozeMinutesText = it
                state.saveConfig()
            }
        },
        SettingsInteractionRow("Only when reviews are waiting") {
            it.remindOnlyWhenQueueNotClear = !it.remindOnlyWhenQueueNotClear
            it.saveConfig()
        },
        SettingsInteractionRow("Quiet hours") { state ->
            cycleValue(state.quietHoursText, listOf("Off", "18:00-08:00", "20:00-08:00", "22:00-07:00")) {
                state.quietHoursText = it
                state.saveConfig()
            }
        },
        SettingsInteractionRow("Preview reminder window", AppState::previewReminderWindow),
    )

    SettingsSection.Data -> listOf(
        SettingsInteractionRow("Clear cache") {
            it.statusLine = "Use Tab to reach Clear cache, then press Enter."
        },
        SettingsInteractionRow("Clear reviewed state") {
            it.statusLine = "Use Tab to reach Clear reviewed state, then press Enter."
        },
        SettingsInteractionRow("Restart setup", AppState::restartOnboarding),
        SettingsInteractionRow("Test connection", AppState::testGithubCli),
        SettingsInteractionRow("Copy diagnostics", AppState::copyDiagnostics),
        SettingsInteractionRow("Validate tracking", AppState::validateTrackingText),
        SettingsInteractionRow("Display diagnostics") {
            it.statusLine = "Use Tab to show display diagnostics."
        },
    )
}

fun settingsRowLabels(section: SettingsSection): List<String> =
    settingsRows(section).map(SettingsInteractionRow::label)

fun currentSettingsSection(state: AppState): SettingsSection =
    SettingsSection.entries[state.settingsSectionIndex.coerceIn(SettingsSection.entries.indices)]

fun currentSettingsRowLabel(state: AppState): String {
    val labels = settingsRowLabels(currentSettingsSection(state))
    return labels[state.settingsFocusedRowIndex.coerceIn(labels.indices)]
}

fun moveSettingsSection(
    state: AppState,
    delta: Int,
) {
    state.applySettingsAction(SettingsAction.MoveSection(delta))
}

fun moveSettingsRow(
    state: AppState,
    delta: Int,
) {
    state.applySettingsAction(SettingsAction.MoveRow(delta))
}

fun activateFocusedSettingsRow(state: AppState) {
    settingsRows(currentSettingsSection(state))
        .getOrNull(state.settingsFocusedRowIndex)
        ?.activate
        ?.invoke(state)
}

private fun <T> cycleValue(
    current: T,
    values: List<T>,
    setValue: (T) -> Unit,
) {
    val index = values.indexOf(current).takeIf { it >= 0 } ?: 0
    setValue(values[(index + 1) % values.size])
}
