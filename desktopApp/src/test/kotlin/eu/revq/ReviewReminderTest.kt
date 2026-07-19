package eu.revq

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

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
    @Test
    fun openingQueueFromPreviewDoesNotDismissOrSnoozeTheScheduledReminder() {
        val review = PullRequest(
            repository = RepositoryId("acme", "app"),
            number = 42,
            title = "Preview review",
            url = "https://example.test/42",
            updatedAt = "2026-07-18T08:00:00Z",
            source = PullRequestSource.ReviewRequest,
            reviewRequestKind = ReviewRequestKind.Direct,
        )
        val state = AppState().apply {
            pullRequests = listOf(review)
            reminderWindowIsPreview = true
            showReminderWindow = true
            reminderDismissedDate = null
            reminderSnoozedUntil = null
        }

        state.openReviewQueueFromReminder()

        assertEquals(false, state.showReminderWindow)
        assertEquals(false, state.reminderWindowIsPreview)
        assertNull(state.reminderDismissedDate)
        assertNull(state.reminderSnoozedUntil)
        assertEquals(View.NeedsReview, state.view)
        assertEquals(review, state.selectedPullRequest)
    }

}
