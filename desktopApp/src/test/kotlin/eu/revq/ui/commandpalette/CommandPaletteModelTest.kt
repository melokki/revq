package eu.revq.ui.commandpalette

import androidx.compose.ui.unit.dp
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
        paletteState.open(AppState())

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
    fun `palette search understands command acronyms`() {
        val results = PaletteResultProvider.results(PaletteMode.Universal, AppState(), "gtnr")

        val firstCommand = results.filterIsInstance<PaletteResult.CommandResult>().first()
        assertEquals(CommandId.GoToNeedsReview, firstCommand.command.id)
    }

    @Test
    fun `palette search tolerates a small typo`() {
        val results = PaletteResultProvider.results(PaletteMode.Universal, AppState(), "settngs")

        val firstCommand = results.filterIsInstance<PaletteResult.CommandResult>().first()
        assertEquals(CommandId.GoToSettings, firstCommand.command.id)
    }

    @Test
    fun `typed search boosts a recently used command among equally strong matches`() {
        val state = AppState().apply {
            recordCommandExecution(CommandId.GoToSettings)
        }

        val results = PaletteResultProvider.results(PaletteMode.Universal, state, "go to")

        val firstCommand = results.filterIsInstance<PaletteResult.CommandResult>().first()
        assertEquals(CommandId.GoToSettings, firstCommand.command.id)
    }

    @Test
    fun repositoryScopePaletteIncludesTrackedOrganizations() {
        val state = AppState().apply {
            organizationsText = "acme\nplatform"
            repositoriesText = "acme/mobile"
        }

        val results = PaletteResultProvider.results(
            PaletteMode.RepositoryScope,
            state,
            "acme",
        )

        assertTrue(
            results.any {
                it is PaletteResult.OrganizationResult && it.organization == "acme"
            },
        )
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
            command = CommandRegistry.find(CommandId.Refresh)!!,
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
    fun `shortcut references never receive execution or quick run actions`() {
        val shortcut = PaletteResultProvider.results(
            PaletteMode.Universal,
            AppState(),
            "keyboard shortcuts",
        ).filterIsInstance<PaletteResult.ShortcutResult>().first()
        val refresh = PaletteResult.CommandResult(
            command = CommandRegistry.find(CommandId.Refresh)!!,
            section = PaletteSection.System,
            enabled = true,
        )

        val labels = quickRunShortcutLabels(listOf(shortcut, refresh))

        assertFalse(shortcut.actionable)
        assertFalse(shortcut.stableKey in labels)
        assertEquals("Ctrl+1", labels[refresh.stableKey])
        assertFalse(CommandPaletteState().approveExecution(shortcut))
    }

    @Test
    fun `palette shortcut labels use the platform primary modifier`() {
        val mac = paletteShortcutLabels("Mac OS X")
        val linux = paletteShortcutLabels("Linux")

        assertEquals("⌘1", mac.quickRun(1))
        assertEquals("⌘N/P", mac.moveAlternative)
        assertEquals("⌘U", mac.clear)
        assertEquals("Ctrl+1", linux.quickRun(1))
        assertEquals("Ctrl+N/P", linux.moveAlternative)
        assertEquals("Ctrl+U", linux.clear)
    }

    @Test
    fun `palette session reuses an immutable catalog until it is refreshed`() {
        val first = reviewRequest(title = "Alpha pagination")
        val second = reviewRequest(title = "Beta caching").copy(number = 43)
        val state = AppState().apply { pullRequests = listOf(first) }
        val paletteState = CommandPaletteState()

        paletteState.open(state)
        paletteState.query = "alpha pagination"
        assertTrue(paletteState.results.any { it is PaletteResult.PullRequestResult && it.pullRequest.number == 42 })

        state.pullRequests = listOf(second)
        paletteState.query = "beta caching"
        assertTrue(paletteState.results.none { it is PaletteResult.PullRequestResult })

        paletteState.refreshCatalog(state)
        assertTrue(paletteState.results.any { it is PaletteResult.PullRequestResult && it.pullRequest.number == 43 })
    }

    @Test
    fun `palette selection follows its stable result key when the catalog changes`() {
        val state = AppState()
        val paletteState = CommandPaletteState()
        paletteState.open(state)
        paletteState.selectedIndex = paletteState.results.indexOfFirst {
            it is PaletteResult.CommandResult && it.command.id == CommandId.GoToSettings
        }
        val selectedKey = paletteState.selectedResult!!.stableKey

        val review = reviewRequest()
        state.pullRequests = listOf(review)
        state.selectedPullRequest = review
        paletteState.refreshCatalog(state)

        assertEquals(selectedKey, paletteState.selectedResult?.stableKey)
    }

    @Test
    fun `palette presentation identifies every literal title match`() {
        assertEquals(
            listOf(0..3, 5..12),
            paletteMatchRanges(
                text = "Open selected pull request",
                query = "selected open",
            ),
        )
    }

    @Test
    fun `palette presentation highlights the letters used by an acronym match`() {
        assertEquals(
            listOf(0..0, 3..3, 6..6, 12..12),
            paletteMatchRanges(text = "Go to Needs Review", query = "gtnr"),
        )
    }

    @Test
    fun `palette presentation highlights the word selected by fuzzy matching`() {
        assertEquals(
            listOf(6..13),
            paletteMatchRanges(text = "Go to Settings", query = "settngs"),
        )
    }

    @Test
    fun `palette result summary distinguishes suggestions matches and no matches`() {
        assertEquals("6 suggestions", paletteResultSummary(6, ""))
        assertEquals("3 matches for “review”", paletteResultSummary(3, "review"))
        assertEquals("No matches for “missing”", paletteResultSummary(0, "missing"))
    }

    @Test
    fun `palette dimensions retain screen margins and cap large displays`() {
        assertEquals(
            PaletteDimensions(width = 468.dp, maxHeight = 620.dp),
            paletteDimensions(availableWidth = 500.dp, availableHeight = 900.dp),
        )
        assertEquals(
            PaletteDimensions(width = 800.dp, maxHeight = 468.dp),
            paletteDimensions(availableWidth = 1400.dp, availableHeight = 500.dp),
        )
    }

    @Test
    fun `compact palette chrome keeps only essential footer hints and hides type pills`() {
        assertEquals(
            PaletteChromePresentation(
                footerHints = listOf(
                    PaletteFooterHintSpec("↑↓", "Move"),
                    PaletteFooterHintSpec("Enter", "Run"),
                    PaletteFooterHintSpec("Esc", "Close"),
                ),
                showTypePills = false,
            ),
            paletteChromePresentation(
                availableWidth = 468.dp,
                acceptsTextQuery = true,
                confirming = false,
                shortcutLabels = PaletteShortcutLabels(primaryModifier = "Ctrl+"),
            ),
        )
    }

    @Test
    fun `wide palette chrome exposes all shortcuts and type pills`() {
        assertEquals(
            PaletteChromePresentation(
                footerHints = listOf(
                    PaletteFooterHintSpec("↑↓", "Move"),
                    PaletteFooterHintSpec("Ctrl+N/P", "Move"),
                    PaletteFooterHintSpec("Enter", "Confirm"),
                    PaletteFooterHintSpec("Ctrl+1…9", "Run"),
                    PaletteFooterHintSpec("Ctrl+U", "Clear"),
                    PaletteFooterHintSpec("Esc", "Close"),
                ),
                showTypePills = true,
            ),
            paletteChromePresentation(
                availableWidth = 800.dp,
                acceptsTextQuery = true,
                confirming = true,
                shortcutLabels = PaletteShortcutLabels(primaryModifier = "Ctrl+"),
            ),
        )
    }

    @Test
    fun `palette row accessibility distinguishes actions from references`() {
        val refresh = PaletteResult.CommandResult(
            command = CommandRegistry.find(CommandId.Refresh)!!,
            section = PaletteSection.System,
            enabled = true,
        )
        val shortcut = PaletteResult.ShortcutResult(
            stableKey = "shortcut:move",
            title = "Move selection",
            subtitle = "Move through results",
            shortcutLabel = "j / k",
            section = PaletteSection.Movement,
            searchableText = "move selection",
        )

        assertEquals(
            PaletteRowAccessibility(
                label = "Command: Refresh",
                stateDescription = "Selected. Action available.",
                selected = true,
                button = true,
            ),
            refresh.accessibility(selected = true),
        )
        assertEquals(
            PaletteRowAccessibility(
                label = "Shortcut: Move selection",
                stateDescription = "Reference only.",
                selected = false,
                button = false,
            ),
            shortcut.accessibility(selected = false),
        )
    }

    @Test
    fun disabledCommandResultsShowWhyTheyCannotRun() {
        val state = AppState().apply { isRefreshing = true }
        val results = PaletteResultProvider.results(PaletteMode.Universal, state, "refresh")
        val refresh = results
            .filterIsInstance<PaletteResult.CommandResult>()
            .first { it.command.id == CommandId.Refresh }

        assertFalse(refresh.enabled)
        assertEquals("Refresh is already running.", refresh.subtitle)
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
        assertEquals("Scope all queues to acme/mobile", repositoryResult.executionPreview())
        assertEquals("Unavailable: Unavailable right now.", disabledCommand.executionPreview())
    }

    @Test
    fun commandPreviewExplainsSensitiveActions() {
        val togglePin = PaletteResult.CommandResult(
            command = CommandRegistry.find(CommandId.ToggleSelectedPrPin)!!,
            section = PaletteSection.Actions,
            enabled = true,
        )
        val toggleMute = PaletteResult.CommandResult(
            command = CommandRegistry.find(CommandId.ToggleMuteSelectedRepository)!!,
            section = PaletteSection.Actions,
            enabled = true,
        )

        assertEquals("Pin or unpin the selected pull request", togglePin.executionPreview())
        assertEquals("Hide or restore the selected repository", toggleMute.executionPreview())
    }

    @Test
    fun `primary pull request action names its exact outcome and confirms merge`() {
        val review = reviewRequest()
        val reviewState = AppState().apply {
            pullRequests = listOf(review)
            selectedPullRequest = review
        }
        val readyMine = review.copy(
            source = PullRequestSource.Mine,
            reviewDecision = "APPROVED",
            mergeable = "MERGEABLE",
            mergeStateStatus = "CLEAN",
            unresolvedDiscussionCount = 0,
        )
        val mineState = AppState().apply {
            pullRequests = listOf(readyMine)
            selectedPullRequest = readyMine
        }

        val reviewAction = PaletteResultProvider.results(PaletteMode.Universal, reviewState, "")
            .filterIsInstance<PaletteResult.CommandResult>()
            .first { it.command.id == CommandId.MarkSelectedReviewed }
        val mergeAction = PaletteResultProvider.results(PaletteMode.Universal, mineState, "")
            .filterIsInstance<PaletteResult.CommandResult>()
            .first { it.command.id == CommandId.MarkSelectedReviewed }

        assertEquals("Mark selected PR reviewed", reviewAction.title)
        assertEquals("Mark #42 reviewed and move to the next review", reviewAction.executionPreview())
        assertEquals("Merge selected PR", mergeAction.title)
        assertEquals("Merge acme/mobile #42", mergeAction.executionPreview())
        assertEquals("Press Enter again to merge acme/mobile #42", mergeAction.confirmationPrompt)
    }

    @Test
    fun `unready personal pull request explains why merge is unavailable`() {
        val mine = reviewRequest().copy(source = PullRequestSource.Mine)
        val state = AppState().apply {
            pullRequests = listOf(mine)
            selectedPullRequest = mine
        }

        val merge = PaletteResultProvider.results(PaletteMode.Universal, state, "merge selected")
            .filterIsInstance<PaletteResult.CommandResult>()
            .first { it.command.id == CommandId.MarkSelectedReviewed }

        assertEquals("Merge selected PR", merge.title)
        assertFalse(merge.enabled)
        assertEquals("Selected PR is not ready to merge.", merge.subtitle)
    }

    @Test
    fun `merge executes only after the same palette result is confirmed`() {
        val paletteState = CommandPaletteState()
        val merge = PaletteResult.CommandResult(
            command = CommandRegistry.find(CommandId.MarkSelectedReviewed)!!,
            section = PaletteSection.Actions,
            enabled = true,
            displayTitle = "Merge selected PR",
            confirmationPrompt = "Press Enter again to merge acme/mobile #42",
        )
        val refresh = PaletteResult.CommandResult(
            command = CommandRegistry.find(CommandId.Refresh)!!,
            section = PaletteSection.System,
            enabled = true,
        )

        assertFalse(paletteState.approveExecution(merge))
        assertEquals("Press Enter again to merge acme/mobile #42", paletteState.confirmationMessage)
        assertTrue(paletteState.approveExecution(merge))
        assertEquals(null, paletteState.confirmationMessage)
        assertTrue(paletteState.approveExecution(refresh))
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
