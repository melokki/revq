package eu.revq.keyboard

import eu.revq.AppState
import eu.revq.PullRequest
import eu.revq.PullRequestSource
import eu.revq.RepositoryId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KeyboardUxModelTest {
    @Test
    fun reviewRequestSelectionShowsRelevantDirectActions() {
        val review = pullRequest(PullRequestSource.ReviewRequest)
        val state = AppState().apply {
            pullRequests = listOf(review)
            selectedPullRequest = review
            keyboardFocusRegion = FocusRegion.PullRequestList
        }

        val hints = keyboardHints(state, paletteOpen = false)

        assertTrue(KeyboardHint("o", "GitHub") in hints)
        assertTrue(KeyboardHint("m", "Reviewed") in hints)
        assertTrue(KeyboardHint("p", "Pin") in hints)
        assertTrue(KeyboardHint("c", "Copy") in hints)
        assertTrue(KeyboardHint("Space", "More") in hints)
    }

    @Test
    fun settingsShowsRowSectionAndSaveHints() {
        val state = AppState().apply {
            selectView(eu.revq.View.Settings)
        }

        val hints = keyboardHints(state, paletteOpen = false)

        assertEquals(
            listOf(
                KeyboardHint("j/k", "Rows"),
                KeyboardHint("h/l", "Sections"),
                KeyboardHint("Enter", "Edit"),
                KeyboardHint("Esc", "Back"),
                KeyboardHint("Ctrl+S", "Save"),
                KeyboardHint("Space", "More"),
            ),
            hints,
        )
    }

    @Test
    fun nextActionPrefersMarkReviewedForSelectedReviewRequest() {
        val review = pullRequest(PullRequestSource.ReviewRequest)
        val state = AppState().apply {
            pullRequests = listOf(review)
            selectedPullRequest = review
        }

        assertEquals(
            KeyboardHint("m", "Mark reviewed & next"),
            nextKeyboardAction(state),
        )
    }

    @Test
    fun nextActionSuggestsOpeningOwnPrThatNeedsAttention() {
        val mine = pullRequest(
            source = PullRequestSource.Mine,
            checksFailing = 2,
        )
        val state = AppState().apply {
            pullRequests = listOf(mine)
            selectedPullRequest = mine
        }

        assertEquals(
            KeyboardHint("o", "Open PR"),
            nextKeyboardAction(state),
        )
    }

    private fun pullRequest(
        source: PullRequestSource,
        checksFailing: Int = 0,
    ): PullRequest = PullRequest(
        repository = RepositoryId("acme", "mobile"),
        number = 42,
        title = "Keyboard first review",
        url = "https://github.com/acme/mobile/pull/42",
        updatedAt = "2026-07-07T10:00:00Z",
        source = source,
        checksFailing = checksFailing,
    )
}
