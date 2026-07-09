package eu.revq.commands

import eu.revq.AppState
import eu.revq.PullRequest
import eu.revq.PullRequestSource
import eu.revq.RepositoryId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CommandModelTest {
    @Test
    fun commandAliasesIncludeNaturalKeyboardFirstIntentWords() {
        assertTrue(CommandRegistry.find(CommandId.MarkSelectedReviewed)!!.aliases.contains("handled"))
        assertTrue(CommandRegistry.find(CommandId.GoToSettings)!!.aliases.contains("preferences"))
        assertTrue(CommandRegistry.find(CommandId.GoToBlocked)!!.aliases.contains("blocked"))
        assertTrue(CommandRegistry.find(CommandId.OpenSelectedPrInGitHub)!!.aliases.contains("open pull request"))
    }

    @Test
    fun navigatingFromSettingsThroughPaletteReturnsToWorkspace() {
        val state = AppState().apply {
            selectView(eu.revq.View.Settings)
        }

        val result = CommandRegistry.execute(CommandId.GoToNeedsReview, state)

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
