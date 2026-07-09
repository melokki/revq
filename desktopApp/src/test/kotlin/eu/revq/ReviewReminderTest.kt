package eu.revq

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals

class ReviewReminderTest {
    @Test
    fun dueReminderIsSuppressedDuringQuietHoursBeforeCheckingQueueSize() {
        val decision = ReviewReminder.dueDecision(
            input = ReviewReminderInput(
                enabled = true,
                now = Instant.parse("2026-07-10T18:30:00Z"),
                localDate = LocalDate.parse("2026-07-10"),
                localTime = LocalTime.parse("21:30"),
                dismissedDate = null,
                snoozedUntil = null,
                quietHours = "18:00-08:00",
                onlyWhenQueueNotClear = true,
                reviewQueueSize = 3,
            ),
        )

        assertEquals(ReviewReminderDecision.Suppress("quiet hours"), decision)
    }
}
