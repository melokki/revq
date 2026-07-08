package eu.revq

import eu.revq.keyboard.FocusRegion
import kotlin.test.Test
import kotlin.test.assertEquals

class MainUiModelTest {
    @Test
    fun reviewSessionProgressChipShowsCurrentPosition() {
        val first = pullRequest(1)
        val second = pullRequest(2)
        val state = AppState().apply {
            pullRequests = listOf(first, second)
            reviewSessionActive = true
            reviewSessionQueueKeys = listOf(first.key, second.key)
            selectedPullRequest = second
        }

        assertEquals("2/2", reviewSessionProgressPillLabel(state))
    }

    @Test
    fun pullRequestReasonLabelExplainsWhyTheRowAppears() {
        val review = pullRequest(42, source = PullRequestSource.ReviewRequest)
        val mine = pullRequest(
            number = 43,
            source = PullRequestSource.Mine,
            checksFailing = 1,
        )
        val state = AppState().apply {
            pullRequests = listOf(review, mine)
            selectView(View.NeedsReview)
        }

        assertEquals("Up next", pullRequestReasonLabel(state, review, startHere = true))

        state.selectView(View.Mine)
        assertEquals("Checks failing", pullRequestReasonLabel(state, mine))

        state.selectView(View.Pinned)
        state.pinnedPrKeys = setOf(review.key)
        assertEquals("Pinned", pullRequestReasonLabel(state, review))
    }

    @Test
    fun movingPrSelectionDoesNotMoveTheSidebarKeyboardSelector() {
        val first = pullRequest(1)
        val second = pullRequest(2)
        val state = AppState().apply {
            pullRequests = listOf(first, second)
            selectedPullRequest = first
            keyboardFocusRegion = FocusRegion.PullRequestList
            sidebarKeyboardView = View.Mine
        }

        moveSelection(state, 1)

        assertEquals(second, state.selectedPullRequest)
        assertEquals(FocusRegion.PullRequestList, state.keyboardFocusRegion)
        assertEquals(View.Mine, state.sidebarKeyboardView)
    }

    @Test
    fun activeFilterChipsExposePaletteAppliedFilters() {
        val state = AppState().apply {
            searchQuery = "acme/mobile"
            focusReviewMode = true
            groupByRepository = true
        }

        assertEquals(
            listOf(
                WorkspaceFilterChip("Filter", "acme/mobile", clearable = true),
                WorkspaceFilterChip("Focus", "Needs Review", clearable = false),
            ),
            workspaceFilterChips(state),
        )
    }

    @Test
    fun emptyStateClearsFiltersBeforeSendingUserElsewhere() {
        val state = AppState().apply {
            searchQuery = "acme/mobile"
        }

        val spec = emptyStateSpec(state)

        assertEquals("Clear filter", spec.primaryLabel)
        spec.primaryAction(state)
        assertEquals("", state.searchQuery)
    }

    @Test
    fun rowMetadataSeparatesStableIdentityFromUpdatedTime() {
        val pr = pullRequest(42, source = PullRequestSource.ReviewRequest)

        assertEquals("acme/mobile #42", rowIdentityMetadata(pr))
        assertEquals("Updated", rowUpdatedPrefix(pr).take(7))
    }

    @Test
    fun selectedPullRequestActionStripShowsKeyboardFirstActions() {
        val review = pullRequest(42, source = PullRequestSource.ReviewRequest)
        val state = AppState().apply {
            pullRequests = listOf(review)
            selectedPullRequest = review
        }

        assertEquals(
            listOf(
                SelectedPullRequestActionHint("o", "Open"),
                SelectedPullRequestActionHint("p", "Pin"),
                SelectedPullRequestActionHint("m", "Reviewed"),
                SelectedPullRequestActionHint("c", "Copy"),
            ),
            selectedPullRequestActionHints(state),
        )
    }

    @Test
    fun setupChecklistExplainsWhatIsMissing() {
        val state = AppState()

        assertEquals(
            listOf(
                SetupChecklistItem("GitHub CLI", false),
                SetupChecklistItem("Tracked repos or orgs", false),
                SetupChecklistItem("Refresh", false),
            ),
            setupChecklistItems(state),
        )
    }

    private fun pullRequest(
        number: Int,
        source: PullRequestSource = PullRequestSource.ReviewRequest,
        checksFailing: Int = 0,
    ): PullRequest = PullRequest(
        repository = RepositoryId("acme", "mobile"),
        number = number,
        title = "Keyboard polish $number",
        url = "https://github.com/acme/mobile/pull/$number",
        updatedAt = "2026-07-07T10:00:00Z",
        source = source,
        checksFailing = checksFailing,
    )
}
