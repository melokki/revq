package eu.revq

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.DataObject
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.ui.graphics.vector.ImageVector

enum class SettingsSection(
    val label: String,
    val icon: ImageVector,
) {
    General("General", Icons.Rounded.Settings),
    GitHub("GitHub", Icons.Rounded.Code),
    Tracking("Tracking", Icons.Rounded.Folder),
    Review("Review", Icons.Rounded.Tune),
    Reminders("Reminders", Icons.Rounded.Notifications),
    Data("Data & diagnostics", Icons.Rounded.DataObject),
}

fun settingsRowLabels(section: SettingsSection): List<String> = when (section) {
    SettingsSection.General -> listOf(
        "Auto refresh",
        "Refresh interval",
        "Row density",
        "Default grouping",
    )

    SettingsSection.GitHub -> listOf(
        "Test connection",
        "Run auto-detection again",
        "Executable override",
    )

    SettingsSection.Tracking -> listOf(
        "Tracked repositories",
        "Organizations",
        "Discover repositories",
        "Muted repositories",
    )

    SettingsSection.Review -> listOf(
        "Focus review mode",
        "Default sort",
        "Stale after",
    )

    SettingsSection.Reminders -> listOf(
        "Scheduled reminders",
        "Reminder time",
        "Reminder days",
        "Snooze duration",
        "Only when reviews are waiting",
        "Quiet hours",
        "Preview reminder window",
    )

    SettingsSection.Data -> listOf(
        "Clear cache",
        "Clear reviewed state",
        "Test connection",
        "Copy diagnostics",
        "Validate tracking",
        "Display diagnostics",
    )
}

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
    state.settingsSectionIndex = (state.settingsSectionIndex + delta)
        .coerceIn(SettingsSection.entries.indices)
    clampSettingsRow(state)
}

fun moveSettingsRow(
    state: AppState,
    delta: Int,
) {
    val labels = settingsRowLabels(currentSettingsSection(state))
    state.settingsFocusedRowIndex = (state.settingsFocusedRowIndex + delta)
        .coerceIn(labels.indices)
}

fun activateFocusedSettingsRow(state: AppState) {
    when (currentSettingsSection(state)) {
        SettingsSection.General -> activateGeneralSettingsRow(state)
        SettingsSection.GitHub -> activateGithubSettingsRow(state)
        SettingsSection.Tracking -> activateTrackingSettingsRow(state)
        SettingsSection.Review -> activateReviewSettingsRow(state)
        SettingsSection.Reminders -> activateReminderSettingsRow(state)
        SettingsSection.Data -> activateDataSettingsRow(state)
    }
}

private fun clampSettingsRow(state: AppState) {
    val labels = settingsRowLabels(currentSettingsSection(state))
    state.settingsFocusedRowIndex = state.settingsFocusedRowIndex.coerceIn(labels.indices)
}

private fun activateGeneralSettingsRow(state: AppState) {
    when (state.settingsFocusedRowIndex) {
        0 -> {
            state.autoRefreshEnabled = !state.autoRefreshEnabled
            state.saveConfig()
        }

        1 -> cycleValue(state.autoRefreshIntervalMinutesText, listOf("1", "5", "10", "15", "30", "60")) {
            state.autoRefreshIntervalMinutesText = it
            state.saveConfig()
        }

        2 -> {
            state.compactRows = !state.compactRows
            state.saveConfig()
        }

        3 -> {
            state.groupByRepository = !state.groupByRepository
            state.saveConfig()
        }
    }
}

private fun activateGithubSettingsRow(state: AppState) {
    when (state.settingsFocusedRowIndex) {
        0 -> state.testGithubCli()
        1 -> state.autoDetectGithubCli()
        2 -> state.statusLine = "Tab to the executable field, edit, then press Apply override."
    }
}

private fun activateTrackingSettingsRow(state: AppState) {
    when (state.settingsFocusedRowIndex) {
        2 -> state.discoverTargets()
        else -> state.statusLine = "Tab to the text field or row action, then press Enter."
    }
}

private fun activateReviewSettingsRow(state: AppState) {
    when (state.settingsFocusedRowIndex) {
        0 -> {
            state.focusReviewMode = !state.focusReviewMode
            state.saveConfig()
        }

        1 -> cycleValue(state.sortMode, listOf("Urgency", "Updated newest", "Updated oldest", "Repository", "Comments")) {
            state.sortMode = it
            state.saveConfig()
        }

        2 -> cycleValue(state.staleThresholdDaysText, listOf("1", "2", "3", "5", "7", "14")) {
            state.staleThresholdDaysText = it
            state.saveConfig()
        }
    }
}

private fun activateReminderSettingsRow(state: AppState) {
    when (state.settingsFocusedRowIndex) {
        0 -> {
            state.reminderEnabled = !state.reminderEnabled
            state.saveConfig()
        }

        1 -> cycleValue(state.reminderTimeText, reminderTimeOptions()) {
            state.reminderTimeText = it
            state.saveConfig()
        }

        2 -> state.statusLine = "Use Tab to reach individual reminder day chips."
        3 -> cycleValue(state.reminderSnoozeMinutesText, listOf("15", "30", "60", "120", "240")) {
            state.reminderSnoozeMinutesText = it
            state.saveConfig()
        }

        4 -> {
            state.remindOnlyWhenQueueNotClear = !state.remindOnlyWhenQueueNotClear
            state.saveConfig()
        }

        5 -> cycleValue(state.quietHoursText, listOf("Off", "18:00-08:00", "20:00-08:00", "22:00-07:00")) {
            state.quietHoursText = it
            state.saveConfig()
        }

        6 -> state.previewReminderWindow()
    }
}

private fun activateDataSettingsRow(state: AppState) {
    when (state.settingsFocusedRowIndex) {
        0 -> state.statusLine = "Use Tab to reach Clear cache, then press Enter."
        1 -> state.statusLine = "Use Tab to reach Clear reviewed state, then press Enter."
        2 -> state.testGithubCli()
        3 -> state.copyDiagnostics()
        4 -> state.validateTrackingText()
        5 -> state.statusLine = "Use Tab to show display diagnostics."
    }
}

private fun cycleValue(
    current: String,
    values: List<String>,
    setValue: (String) -> Unit,
) {
    val index = values.indexOf(current).takeIf { it >= 0 } ?: 0
    setValue(values[(index + 1) % values.size])
}
