package eu.revq

import androidx.compose.ui.input.key.Key
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NotificationShortcutKeyTest {
    @Test
    fun dismissShortcutAcceptsEscapeAndDOnly() {
        assertTrue(isNotificationDismissKey(Key.Escape))
        assertTrue(isNotificationDismissKey(Key.D))
        assertFalse(isNotificationDismissKey(Key.Enter))
        assertEquals("Esc / D", NotificationDismissShortcutLabel)
    }
}
