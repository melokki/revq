package eu.revq

import androidx.compose.ui.input.key.Key

internal const val NotificationDismissShortcutLabel = "Esc / D"

internal fun isNotificationDismissKey(key: Key): Boolean =
    key == Key.Escape || key == Key.D
