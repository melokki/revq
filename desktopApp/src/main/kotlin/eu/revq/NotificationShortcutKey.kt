package eu.revq

import androidx.compose.ui.input.key.Key

internal const val NotificationDismissShortcutLabel = "Esc / D"
internal const val NotificationSettingsShortcutLabel = "N"

internal fun isNotificationDismissKey(key: Key): Boolean =
    key == Key.Escape || key == Key.D

internal fun isNotificationSettingsKey(key: Key): Boolean = key == Key.N
