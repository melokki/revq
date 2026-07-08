package eu.revq.commands

import eu.revq.AppState
import eu.revq.HandledUndo
import eu.revq.PullRequest
import eu.revq.PullRequestSource
import eu.revq.RepositoryId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CommandModelTest {
    @Test
    fun undoLastReviewedIsOnlyEnabledWhenThereIsSomethingToUndo() {
        val emptyState = AppState()
        val emptyContext = CommandContext.from(emptyState)
        val command = CommandRegistry.find(CommandId.UndoLastReviewed)!!

        assertFalse(command.isEnabled(emptyContext))

        val state = AppState().apply {
            lastUndoReview = HandledUndo(reviewRequest(), "2026-07-07T10:00:00Z")
        }

        assertTrue(command.isEnabled(CommandContext.from(state)))
    }

    @Test
    fun commandAliasesIncludeNaturalKeyboardFirstIntentWords() {
        assertTrue(CommandRegistry.find(CommandId.MarkSelectedReviewed)!!.aliases.contains("ship"))
        assertTrue(CommandRegistry.find(CommandId.GoToSettings)!!.aliases.contains("prefs"))
        assertTrue(CommandRegistry.find(CommandId.GoToBlocked)!!.aliases.contains("stuck"))
        assertTrue(CommandRegistry.find(CommandId.OpenSelectedPrInGitHub)!!.aliases.contains("open pr"))
        assertTrue(CommandRegistry.find(CommandId.GoToNeedsReview)!!.aliases.contains("close settings"))
        assertTrue(CommandRegistry.find(CommandId.ClearFilter)!!.aliases.contains("clear search"))
        assertTrue(CommandRegistry.find(CommandId.ToggleGroupByRepository)!!.aliases.contains("group by repository"))
        assertTrue(CommandRegistry.find(CommandId.CycleSortMode)!!.aliases.contains("sort"))
    }

    @Test
    fun executingUndoLastReviewedRestoresThePreviousReview() {
        val pr = reviewRequest()
        val state = AppState().apply {
            handledReviewRecords = mapOf(pr.key to pr.updatedMarker)
            lastUndoReview = HandledUndo(pr, pr.updatedMarker)
        }

        val result = CommandExecutor(state).execute(CommandId.UndoLastReviewed)

        assertEquals(CommandExecutionResult.Executed, result)
        assertFalse(pr.key in state.handledReviewRecords)
        assertEquals(pr, state.selectedPullRequest)
    }

    @Test
    fun visibleWorkspaceControlsHavePaletteCommands() {
        val expected = listOf(
            CommandId.Refresh,
            CommandId.ClearFilter,
            CommandId.ToggleGroupByRepository,
            CommandId.ToggleCompactRows,
            CommandId.CycleSortMode,
            CommandId.PreviousReview,
            CommandId.NextReview,
            CommandId.EndReviewSession,
        )

        expected.forEach { id ->
            assertTrue(CommandRegistry.find(id) != null, "$id should be available from the command palette")
        }
    }

    @Test
    fun executingWorkspaceControlCommandsUpdatesState() {
        val first = reviewRequest(number = 1)
        val second = reviewRequest(number = 2)
        val state = AppState().apply {
            pullRequests = listOf(first, second)
            selectedPullRequest = second
            reviewSessionActive = true
            reviewSessionQueueKeys = listOf(first.key, second.key)
            searchQuery = "acme/mobile"
        }
        val executor = CommandExecutor(state)

        assertEquals(CommandExecutionResult.Executed, executor.execute(CommandId.ClearFilter))
        assertEquals("", state.searchQuery)

        assertEquals(CommandExecutionResult.Executed, executor.execute(CommandId.PreviousReview))
        assertEquals(first, state.selectedPullRequest)

        assertEquals(CommandExecutionResult.Executed, executor.execute(CommandId.ToggleGroupByRepository))
        assertTrue(state.groupByRepository)

        assertEquals(CommandExecutionResult.Executed, executor.execute(CommandId.ToggleCompactRows))
        assertTrue(state.compactRows)

        assertEquals(CommandExecutionResult.Executed, executor.execute(CommandId.CycleSortMode))
        assertEquals("Updated newest", state.sortMode)

        assertEquals(CommandExecutionResult.Executed, executor.execute(CommandId.EndReviewSession))
        assertFalse(state.reviewSessionActive)
    }

    @Test
    fun navigatingFromSettingsThroughPaletteReturnsToWorkspace() {
        val state = AppState().apply {
            selectView(eu.revq.View.Settings)
        }

        val result = CommandExecutor(state).execute(CommandId.GoToNeedsReview)

        assertEquals(CommandExecutionResult.Executed, result)
        assertEquals(eu.revq.View.NeedsReview, state.view)
        assertEquals(eu.revq.keyboard.FocusRegion.PullRequestList, state.keyboardFocusRegion)
    }

    private fun reviewRequest(number: Int = 42): PullRequest = PullRequest(
        repository = RepositoryId("acme", "mobile"),
        number = number,
        title = "Review keyboard flow",
        url = "https://github.com/acme/mobile/pull/$number",
        updatedAt = "2026-07-07T10:00:00Z",
        source = PullRequestSource.ReviewRequest,
    )
}
