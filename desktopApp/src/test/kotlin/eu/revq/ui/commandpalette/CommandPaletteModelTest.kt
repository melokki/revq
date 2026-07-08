package eu.revq.ui.commandpalette

import eu.revq.AppState
import eu.revq.RepositoryId
import eu.revq.PullRequest
import eu.revq.PullRequestSource
import eu.revq.commands.CommandId
import eu.revq.commands.CommandRegistry
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CommandPaletteModelTest {
    @Test
    fun opensUniversalPaletteByDefault() {
        val paletteState = CommandPaletteState()

        paletteState.query = "settings"
        paletteState.selectedIndex = 4
        paletteState.open()

        assertTrue(paletteState.isOpen)
        assertEquals(PaletteMode.Universal, paletteState.mode)
        assertEquals("", paletteState.query)
        assertEquals(0, paletteState.selectedIndex)
    }

    @Test
    fun emptyUniversalPaletteShowsActionsRecentsAndSuggestions() {
        val state = AppState().apply {
            pullRequests = listOf(reviewRequest())
            selectedPullRequest = pullRequests.single()
            recordCommandExecution(CommandId.Refresh)
        }

        val results = PaletteResultProvider.results(PaletteMode.Universal, state, "")

        assertTrue(results.anyCommand(CommandId.OpenSelectedPrInGitHub, PaletteSection.Actions))
        assertTrue(results.anyCommand(CommandId.Refresh, PaletteSection.Recent))
        assertTrue(results.anyCommand(CommandId.GoToSettings, PaletteSection.Suggested))
    }

    @Test
    fun typedUniversalPaletteSearchesPullRequestsRepositoriesAndShortcuts() {
        val state = AppState().apply {
            repositoriesText = "acme/mobile"
            pullRequests = listOf(reviewRequest(title = "Fix pagination keyboard focus"))
        }

        val pullRequestResults = PaletteResultProvider.results(PaletteMode.Universal, state, "pagination")
        val repositoryResults = PaletteResultProvider.results(PaletteMode.Universal, state, "acme/mobile")
        val shortcutResults = PaletteResultProvider.results(PaletteMode.Universal, state, "keyboard shortcuts")

        assertTrue(pullRequestResults.any { it is PaletteResult.PullRequestResult })
        assertTrue(repositoryResults.any { it is PaletteResult.RepositoryResult })
        assertTrue(shortcutResults.any { it is PaletteResult.ShortcutResult })
    }

    @Test
    fun blankUniversalPaletteShowsRecentPaletteTargets() {
        val review = reviewRequest(title = "Recent keyboard target")
        val state = AppState().apply {
            pullRequests = listOf(review)
            repositoriesText = "acme/mobile"
            recordPaletteTarget("repo:acme/mobile")
            recordPaletteTarget("pr:${review.key}")
        }

        val results = PaletteResultProvider.results(PaletteMode.Universal, state, "")

        assertTrue(results.filterIsInstance<PaletteResult.PullRequestResult>().any { it.pullRequest.key == review.key })
        assertTrue(results.filterIsInstance<PaletteResult.RepositoryResult>().any { it.repository == "acme/mobile" })
    }

    @Test
    fun shortcutReferenceDoesNotAdvertiseRemovedPaletteOpeners() {
        val shortcuts = PaletteResultProvider.results(PaletteMode.Universal, AppState(), "keyboard shortcuts")
            .filterIsInstance<PaletteResult.ShortcutResult>()

        val stableKeys = shortcuts.map { it.stableKey }.toSet()

        assertFalse("palette:search" in stableKeys)
        assertFalse("palette:navigate" in stableKeys)
        assertFalse("palette:help" in stableKeys)
        assertFalse(shortcuts.any { it.shortcutLabel == "Ctrl+K" || it.shortcutLabel == ":" })
    }

    @Test
    fun quickRunLabelsSkipDisabledVisibleResults() {
        val disabled = PaletteResult.CommandResult(
            command = CommandRegistry.find(CommandId.StartReviewSession)!!,
            section = PaletteSection.Actions,
            enabled = false,
            disabledReason = null,
        )
        val firstEnabled = PaletteResult.CommandResult(
            command = CommandRegistry.find(CommandId.GoToNeedsReview)!!,
            section = PaletteSection.Suggested,
            enabled = true,
            disabledReason = null,
        )
        val secondEnabled = PaletteResult.CommandResult(
            command = CommandRegistry.find(CommandId.GoToSettings)!!,
            section = PaletteSection.Suggested,
            enabled = true,
            disabledReason = null,
        )

        val labels = quickRunShortcutLabels(listOf(disabled, firstEnabled, secondEnabled))

        assertFalse(disabled.stableKey in labels)
        assertEquals("Ctrl+1", labels[firstEnabled.stableKey])
        assertEquals("Ctrl+2", labels[secondEnabled.stableKey])
    }

    @Test
    fun disabledCommandResultsShowWhyTheyCannotRun() {
        val results = PaletteResultProvider.results(PaletteMode.Universal, AppState(), "start review")
        val startReview = results
            .filterIsInstance<PaletteResult.CommandResult>()
            .first { it.command.id == CommandId.StartReviewSession }

        assertFalse(startReview.enabled)
        assertEquals("No PRs need review right now.", startReview.subtitle)
    }

    @Test
    fun selectedResultPreviewDescribesWhatEnterWillDo() {
        val pullRequest = reviewRequest()
        val prResult = PaletteResult.PullRequestResult(
            pullRequest = pullRequest,
            targetView = eu.revq.View.NeedsReview,
            subtitle = "acme/mobile · Needs your review",
            searchableText = "keyboard palette",
        )
        val repositoryResult = PaletteResult.RepositoryResult("acme/mobile")
        val disabledCommand = PaletteResult.CommandResult(
            command = CommandRegistry.find(CommandId.GoToNeedsReview)!!,
            section = PaletteSection.Actions,
            enabled = false,
            disabledReason = null,
        )

        assertEquals("Open #42 in Needs Review", prResult.executionPreview())
        assertEquals("Filter current view to acme/mobile", repositoryResult.executionPreview())
        assertEquals("Unavailable: Unavailable right now.", disabledCommand.executionPreview())
    }

    @Test
    fun commandPreviewExplainsSensitiveActions() {
        val clearFilter = PaletteResult.CommandResult(
            command = CommandRegistry.find(CommandId.ClearFilter)!!,
            section = PaletteSection.Actions,
            enabled = true,
        )
        val endSession = PaletteResult.CommandResult(
            command = CommandRegistry.find(CommandId.EndReviewSession)!!,
            section = PaletteSection.Actions,
            enabled = true,
        )

        assertEquals("Clear the active pull request filter", clearFilter.executionPreview())
        assertEquals("End the current review session", endSession.executionPreview())
    }

    @Test
    fun paletteResultsExposeDomainTypeLabels() {
        val pullRequest = PaletteResult.PullRequestResult(
            pullRequest = reviewRequest(),
            targetView = eu.revq.View.NeedsReview,
            subtitle = "acme/mobile · Needs your review",
            searchableText = "keyboard palette",
        )
        val repository = PaletteResult.RepositoryResult("acme/mobile")
        val command = PaletteResult.CommandResult(
            command = CommandRegistry.find(CommandId.Refresh)!!,
            section = PaletteSection.System,
            enabled = true,
        )

        assertEquals("PR", pullRequest.typeLabel())
        assertEquals("Repo", repository.typeLabel())
        assertEquals("Command", command.typeLabel())
    }

    private fun List<PaletteResult>.anyCommand(
        commandId: CommandId,
        section: PaletteSection,
    ): Boolean = any { result ->
        val commandResult = result as? PaletteResult.CommandResult ?: return@any false
        commandResult.command.id == commandId && commandResult.section == section
    }

    private fun reviewRequest(
        title: String = "Review keyboard palette",
    ): PullRequest = PullRequest(
        repository = RepositoryId("acme", "mobile"),
        number = 42,
        title = title,
        url = "https://github.com/acme/mobile/pull/42",
        updatedAt = "2026-07-07T10:00:00Z",
        source = PullRequestSource.ReviewRequest,
        authorLogin = "dev",
    )
}
