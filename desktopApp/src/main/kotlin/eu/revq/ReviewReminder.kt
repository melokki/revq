package eu.revq

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

data class ReviewReminderInput(
    val enabled: Boolean,
    val now: Instant,
    val localDate: LocalDate,
    val localTime: LocalTime,
    val dismissedDate: String?,
    val snoozedUntil: Instant?,
    val quietHours: String,
    val onlyWhenQueueNotClear: Boolean,
    val reviewQueueSize: Int,
)

sealed interface ReviewReminderDecision {
    data object Show : ReviewReminderDecision
    data class Suppress(val reason: String) : ReviewReminderDecision
}

object ReviewReminder {
    fun dueDecision(input: ReviewReminderInput): ReviewReminderDecision {
        if (!input.enabled) return ReviewReminderDecision.Suppress("disabled")
        if (input.dismissedDate == input.localDate.toString()) {
            return ReviewReminderDecision.Suppress("dismissed today")
        }
        val snooze = input.snoozedUntil
        if (snooze != null && snooze.isAfter(input.now)) {
            return ReviewReminderDecision.Suppress("snoozed")
        }
        if (isInQuietHours(input.quietHours, input.localTime)) {
            return ReviewReminderDecision.Suppress("quiet hours")
        }
        if (input.onlyWhenQueueNotClear && input.reviewQueueSize == 0) {
            return ReviewReminderDecision.Suppress("review queue clear")
        }
        return ReviewReminderDecision.Show
    }

    fun isInQuietHours(
        quietHours: String,
        now: LocalTime,
    ): Boolean {
        val raw = quietHours.trim()
        if (raw.isBlank() || raw.equals("Off", ignoreCase = true) || raw.equals("Disabled", ignoreCase = true)) return false
        val parts = raw.split("-", limit = 2).map { it.trim() }
        if (parts.size != 2) return false
        val start = parseReminderTime(parts[0]) ?: return false
        val end = parseReminderTime(parts[1]) ?: return false
        return if (start <= end) {
            now >= start && now < end
        } else {
            now >= start || now < end
        }
    }
}
