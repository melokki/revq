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

    private fun reviewRequest(): PullRequest = PullRequest(
        repository = RepositoryId("acme", "mobile"),
        number = 42,
        title = "Review keyboard flow",
        url = "https://github.com/acme/mobile/pull/42",
        updatedAt = "2026-07-07T10:00:00Z",
        source = PullRequestSource.ReviewRequest,
    )
}
