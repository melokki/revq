package eu.revq.commands

import eu.revq.AppState
import eu.revq.PullRequest
import eu.revq.PullRequestSource
import eu.revq.RepositoryId
import kotlin.test.Test
import kotlin.test.assertEquals

class CommandSurfaceTest {
    @Test
    fun selectedReviewPullRequestExposesContextualActionCommands() {
        val state = AppState().apply {
            val selected = PullRequest(
                repository = RepositoryId("acme", "api"),
                number = 42,
                title = "Review me",
                url = "https://github.com/acme/api/pull/42",
                updatedAt = "2026-07-10T10:00:00Z",
                source = PullRequestSource.ReviewRequest,
            )
            selectedPullRequest = selected
            pullRequests = listOf(selected)
        }

        assertEquals(
            listOf(
                CommandId.OpenSelectedPrInGitHub,
                CommandId.MarkSelectedReviewed,
                CommandId.NextReview,
                CommandId.ToggleSelectedPrPin,
                CommandId.CopySelectedPrUrl,
                CommandId.CopySelectedPrMarkdown,
                CommandId.OpenSelectedRepository,
                CommandId.ToggleMuteSelectedRepository,
            ),
            CommandSurface.contextualCommands(state).map { it.command.id },
        )
    }
}
