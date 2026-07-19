package eu.revq

import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ReviewAssignmentNotificationTest {
    @Test
    fun firstSuccessfulRefreshEstablishesBaselineWithoutOpeningAnAlert() {
        assertEquals(
            emptyList(),
            ReviewAssignmentNotifications.newlyAssignedForRefresh(
                baselineEstablished = false,
                previous = emptyList(),
                refreshed = listOf(pullRequest(1)),
            ),
        )
    }

    @Test
    fun onlyNewUnmutedReviewRequestsBecomeAssignmentAlerts() {
        val existingReview = pullRequest(1)
        val newReview = pullRequest(2)
        val mutedReview = pullRequest(3, repository = RepositoryId("acme", "muted"))
        val ownPullRequest = pullRequest(4, source = PullRequestSource.Mine)

        assertEquals(
            listOf(newReview),
            ReviewAssignmentNotifications.newlyAssigned(
                previous = listOf(existingReview),
                refreshed = listOf(existingReview, newReview, mutedReview, ownPullRequest),
                mutedRepositories = setOf("acme/muted"),
            ),
        )
    }

    @Test
    fun alertMergingDeduplicatesPullRequestsAcrossRefreshes() {
        val first = pullRequest(1)
        val second = pullRequest(2)
        val initial = ReviewAssignmentAlert(
            pullRequests = listOf(first),
            detectedAt = Instant.parse("2026-07-18T08:00:00Z"),
        )

        val merged = ReviewAssignmentNotifications.merge(
            current = initial,
            incoming = listOf(first, second),
            detectedAt = Instant.parse("2026-07-18T08:05:00Z"),
        )

        assertEquals(listOf(second, first), merged?.pullRequests)
        assertEquals(initial.detectedAt, merged?.detectedAt)
    }

    @Test
    fun trayCountUsesTheGlobalUnmutedNeedsReviewQueue() {
        val review = pullRequest(1)
        val muted = pullRequest(2, repository = RepositoryId("acme", "muted"))
        val mine = pullRequest(3, source = PullRequestSource.Mine)
        val state = AppState().apply {
            pullRequests = listOf(review, muted, mine)
            mutedRepositoriesText = "acme/muted"
        }

        assertEquals(1, state.trayReviewCount)

        state.showReviewCountInTray = false

        assertEquals(0, state.trayReviewCount)
    }

    @Test
    fun dismissingAssignmentAlertDoesNotChangeScheduledReminderState() {
        val snoozedUntil = Instant.parse("2026-07-18T14:00:00Z")
        val state = AppState().apply {
            reminderSnoozedUntil = snoozedUntil
            reminderDismissedDate = "2026-07-18"
            showReminderWindow = true
            enqueueReviewAssignmentAlert(listOf(pullRequest(1)))
        }

        state.dismissReviewAssignmentAlert()

        assertNull(state.reviewAssignmentAlert)
        assertEquals(snoozedUntil, state.reminderSnoozedUntil)
        assertEquals("2026-07-18", state.reminderDismissedDate)
        assertEquals(true, state.showReminderWindow)
    }

    @Test
    fun openingAssignmentAlertClearsScopeWithoutChangingReminderSnooze() {
        val snoozedUntil = Instant.parse("2026-07-18T14:00:00Z")
        val review = pullRequest(1)
        val state = AppState().apply {
            pullRequests = listOf(review)
            reminderSnoozedUntil = snoozedUntil
            setQueueScopeFilter(QueueScopeFilter.Repository("other/repository"))
            enqueueReviewAssignmentAlert(listOf(review))
        }

        state.openReviewQueueFromAssignmentAlert()

        assertNull(state.reviewAssignmentAlert)
        assertEquals(snoozedUntil, state.reminderSnoozedUntil)
        assertEquals(QueueScopeFilter.All, state.currentQueueScopeFilter())
        assertEquals(View.NeedsReview, state.view)
        assertEquals(review, state.selectedPullRequest)
    }


    @Test
    fun activeMainWindowUsesBannerWhileHiddenOrSettingsUsesWindow() {
        assertEquals(
            ReviewAssignmentPresentation.Banner,
            reviewAssignmentPresentation(
                hasAlert = true,
                reminderVisible = false,
                mainWindowVisible = true,
                mainWindowActive = true,
                view = View.NeedsReview,
            ),
        )
        assertEquals(
            ReviewAssignmentPresentation.Window,
            reviewAssignmentPresentation(
                hasAlert = true,
                reminderVisible = false,
                mainWindowVisible = false,
                mainWindowActive = false,
                view = View.NeedsReview,
            ),
        )
        assertEquals(
            ReviewAssignmentPresentation.Window,
            reviewAssignmentPresentation(
                hasAlert = true,
                reminderVisible = false,
                mainWindowVisible = true,
                mainWindowActive = true,
                view = View.Settings,
            ),
        )
    }

    @Test
    fun visibleReminderDefersBothAssignmentPresentations() {
        assertEquals(
            ReviewAssignmentPresentation.Hidden,
            reviewAssignmentPresentation(
                hasAlert = true,
                reminderVisible = true,
                mainWindowVisible = false,
                mainWindowActive = false,
                view = View.NeedsReview,
            ),
        )
    }

    private fun pullRequest(
        number: Int,
        repository: RepositoryId = RepositoryId("acme", "app"),
        source: PullRequestSource = PullRequestSource.ReviewRequest,
    ): PullRequest = PullRequest(
        repository = repository,
        number = number,
        title = "Pull request $number",
        url = "https://example.test/$number",
        updatedAt = "2026-07-18T0${number.coerceAtMost(9)}:00:00Z",
        source = source,
        reviewRequestKind = if (source == PullRequestSource.ReviewRequest) ReviewRequestKind.Direct else null,
    )
}
