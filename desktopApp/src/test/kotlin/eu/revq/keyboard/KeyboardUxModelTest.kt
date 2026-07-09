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
    fun emptyQueueHasNoNextKeyboardAction() {
        assertEquals(null, nextKeyboardAction(AppState()))
    }

    @Test
    fun reviewRequestSelectionShowsRelevantDirectActions() {
        val review = pullRequest(PullRequestSource.ReviewRequest)
        val state = AppState().apply {
            pullRequests = listOf(review)
            selectedPullRequest = review
            keyboardFocusRegion = FocusRegion.PullRequestList
        }

        val hints = keyboardHints(state, paletteOpen = false)

        assertTrue(KeyboardHint("Enter", "Details") in hints)
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

    @Test
    fun activeKeyboardRegionLabelNamesTheCurrentKeyboardTarget() {
        val state = AppState()

        assertEquals("Pull requests", activeKeyboardRegionLabel(state, paletteOpen = false))
        assertEquals("Palette", activeKeyboardRegionLabel(state, paletteOpen = true))

        state.keyboardMode = KeyboardMode.Insert
        assertEquals("Typing", activeKeyboardRegionLabel(state, paletteOpen = false))

        state.selectView(eu.revq.View.Settings)
        state.keyboardMode = KeyboardMode.Normal
        assertEquals("Settings", activeKeyboardRegionLabel(state, paletteOpen = false))
    }

    @Test
    fun compactKeyboardHintsKeepBottomBarFocused() {
        val review = pullRequest(PullRequestSource.ReviewRequest)
        val state = AppState().apply {
            pullRequests = listOf(review)
            selectedPullRequest = review
            keyboardFocusRegion = FocusRegion.PullRequestList
        }

        assertEquals(
            listOf(
                KeyboardHint("j/k", "Move"),
                KeyboardHint("Enter", "Details"),
            ),
            compactKeyboardHints(state, paletteOpen = false),
        )
    }

    @Test
    fun expandedReviewShowsOnlyRelevantInlineActions() {
        val review = pullRequest(PullRequestSource.ReviewRequest)
        val state = AppState().apply {
            pullRequests = listOf(review)
            selectedPullRequest = review
            expandedPullRequestKey = review.key
        }

        assertEquals(
            listOf(
                KeyboardHint("Esc", "Close"),
                KeyboardHint("o", "Open PR"),
                KeyboardHint("p", "Pin"),
                KeyboardHint("m", "Mark handled"),
                KeyboardHint("Space", "Commands"),
            ),
            keyboardHints(state, paletteOpen = false),
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
