package eu.revq

import kotlin.test.Test
import kotlin.test.assertEquals

class ReminderQueueClearIllustrationTest {
    @Test
    fun reminderQueueClearIllustrationFitsTheAvailablePreviewBody() {
        assertEquals(128f, ReminderQueueClearIllustrationMetrics.Height.value)
        assertEquals(500f, ReminderQueueClearIllustrationMetrics.MaxWidth.value)
        assertEquals(56f, ReminderQueueClearIllustrationMetrics.StatusIconSize.value)
        assertEquals(18f, ReminderQueueClearIllustrationMetrics.QueueLineHeight.value)
    }
}
