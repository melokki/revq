package eu.revq.ui.commandpalette

import eu.revq.AppState
import eu.revq.PullRequest
import eu.revq.PullRequestSource
import eu.revq.View
import eu.revq.ownPullRequestPrimaryStatus
import eu.revq.ownPullRequestStatusTitle
import eu.revq.parseLines
import eu.revq.commands.CommandCategory
import eu.revq.commands.CommandContext
import eu.revq.commands.CommandId
import eu.revq.commands.CommandRegistry

object PaletteResultProvider {
    fun results(
        mode: PaletteMode,
        state: AppState,
        query: String,
    ): List<PaletteResult> {
        val raw = when (mode) {
            PaletteMode.Universal -> universalResults(state, query)
        }
        return filterPaletteResults(raw, query)
    }

    private fun universalResults(
        state: AppState,
        query: String,
    ): List<PaletteResult> {
        if (query.isBlank()) {
            val context = CommandContext.from(state)
            val contextual = contextActionResults(state)
            val contextualIds = contextual
                .mapNotNull { (it as? PaletteResult.CommandResult)?.command?.id }
                .toSet()

            val recent = state.recentCommandIds
                .filterNot { it in contextualIds }
                .mapNotNull { id ->
                    CommandRegistry.find(id)?.let { command ->
                        commandResult(command, PaletteSection.Recent, context)
                    }
                }

            val suggestedIds = listOf(
                CommandId.GoToNeedsReview,
                CommandId.GoToSettings,
                CommandId.Refresh,
                CommandId.ShowKeyboardShortcuts,
            )
            val usedIds = contextualIds + recent.map { it.command.id }
            val suggested = suggestedIds
                .filterNot { it in usedIds }
                .mapNotNull(CommandRegistry::find)
                .map { command ->
                    commandResult(command, PaletteSection.Suggested, context)
                }

            return contextual + recent + suggested
        }

        return commandResults(state) +
                pullRequestSearchResults(state) +
                repositoryResults(state) +
                shortcutReferenceResults(state)
    }

    private fun commandResults(state: AppState): List<PaletteResult> {
        val context = CommandContext.from(state)
        val contextual = contextActionResults(state)
        val contextualIds = contextual
            .mapNotNull { (it as? PaletteResult.CommandResult)?.command?.id }
            .toSet()

        val remaining = CommandRegistry.all()
            .filterNot { it.id in contextualIds }
            .map { command ->
                commandResult(
                    command = command,
                    section = when (command.category) {
                        CommandCategory.Review -> PaletteSection.Review
                        CommandCategory.Navigate -> PaletteSection.Views
                        CommandCategory.System -> PaletteSection.System
                    },
                    context = context,
                )
            }

        return contextual + remaining
    }

    private fun contextActionResults(state: AppState): List<PaletteResult> {
        val context = CommandContext.from(state)
        val selected = state.selectedPullRequest

        val ids = buildList {
            if (state.reviewQueue().isNotEmpty() && !state.reviewSessionActive) {
                add(CommandId.StartReviewSession)
            }

            if (selected != null) {
                add(CommandId.OpenSelectedPrInGitHub)
                if (selected.source == PullRequestSource.ReviewRequest) {
                    add(CommandId.MarkSelectedReviewed)
                    add(CommandId.NextReview)
                }
                add(CommandId.ToggleSelectedPrPin)
                add(CommandId.CopySelectedPrUrl)
                add(CommandId.CopySelectedPrMarkdown)
                add(CommandId.OpenSelectedRepository)
                add(CommandId.ToggleMuteSelectedRepository)
            }

        }

        return ids
            .distinct()
            .mapNotNull(CommandRegistry::find)
            .filter { it.isEnabled(context) }
            .map { command ->
                PaletteResult.CommandResult(
                    command = command,
                    section = PaletteSection.Actions,
                    enabled = true,
                )
            }
    }

    private fun pullRequestSearchResults(state: AppState): List<PaletteResult> =
        state.activePullRequests()
            .distinctBy { it.source to it.key }
            .map { pr -> pullRequestResult(pr, defaultTargetView(state, pr)) }

    private fun pullRequestResult(
        pr: PullRequest,
        targetView: View,
    ): PaletteResult.PullRequestResult {
        val status = when (pr.source) {
            PullRequestSource.ReviewRequest -> "Needs your review"
            PullRequestSource.Mine -> ownPullRequestStatusTitle(ownPullRequestPrimaryStatus(pr))
        }

        val identities = buildList {
            pr.authorLogin?.let(::add)
            addAll(pr.requestedReviewers.orEmpty())
            addAll(pr.changeRequestReviewers.orEmpty())
            addAll(pr.approvingReviewers.orEmpty())
            addAll(pr.unresolvedDiscussionAuthors.orEmpty())
        }

        val subtitle = buildString {
            append(pr.repository)
            append(" · ")
            append(status)
            pr.authorLogin?.takeIf { it.isNotBlank() }?.let {
                append(" · @")
                append(it.removePrefix("@"))
            }
        }

        val searchable = buildString {
            append(pr.title)
            append(' ')
            append(pr.number)
            append(' ')
            append(pr.repository)
            append(' ')
            append(status)
            append(' ')
            append(identities.joinToString(" "))
            append(' ')
            append(pr.reviewDecision.orEmpty())
            append(' ')
            append(pr.mergeStateStatus.orEmpty())
            if (pr.checksFailing > 0) append(" checks failing")
            if (pr.isDraft) append(" draft")
        }

        return PaletteResult.PullRequestResult(
            pullRequest = pr,
            targetView = targetView,
            subtitle = subtitle,
            searchableText = searchable,
        )
    }

    private fun defaultTargetView(
        state: AppState,
        pr: PullRequest,
    ): View = when (pr.source) {
        PullRequestSource.ReviewRequest -> {
            if (state.isHandledCurrent(pr)) View.Handled else View.NeedsReview
        }
        PullRequestSource.Mine -> View.Mine
    }

    private fun repositoryResults(state: AppState): List<PaletteResult.RepositoryResult> {
        val repositories = (
                state.activePullRequests().map { it.repository.toString() } +
                        parseLines(state.repositoriesText)
                )
            .distinct()
            .sorted()
            .filterNot(state::isRepositoryMuted)

        return repositories.map { PaletteResult.RepositoryResult(it) }
    }

    private fun shortcutReferenceResults(state: AppState): List<PaletteResult> {
        val movement = listOf(
            shortcut("movement:j-k", "Move selection", "Move within the focused region", "j / k", PaletteSection.Movement),
            shortcut("movement:h-l", "Move between panes", "Sidebar, PR list, and Review Brief", "h / l", PaletteSection.Movement),
            shortcut("movement:last", "Last item", "Jump to the end of the focused list", "G", PaletteSection.Movement),
            shortcut("movement:half", "Half-page movement", "Move through longer lists quickly", "Ctrl+u / Ctrl+d", PaletteSection.Movement),
            shortcut("movement:activate", "Activate", "Open the focused view, brief, or action", "Enter", PaletteSection.Movement),
            shortcut("movement:escape", "Back / close", "Return to the previous keyboard context", "Esc", PaletteSection.Movement),
        )

        val paletteEntries = listOf(
            shortcut("palette:open", "Command palette", "Search and run commands, views, pull requests, repositories, and shortcuts", "Space", PaletteSection.Application),
            shortcut("palette:run", "Run numbered result", "Run the corresponding enabled visible palette result", "Ctrl+1…9", PaletteSection.Application),
            shortcut("palette:clear", "Clear palette query", "Clear the current palette search text", "Ctrl+u", PaletteSection.Application),
        )

        val context = CommandContext.from(state)
        val commands = CommandRegistry.all()
            .filter { it.shortcut != null }
            .map { command ->
                PaletteResult.ShortcutResult(
                    stableKey = "shortcut:command:${command.id.name}",
                    title = command.title,
                    subtitle = command.description,
                    shortcutLabel = command.shortcut!!.displayLabel,
                    section = when (command.category) {
                        CommandCategory.Review -> PaletteSection.Review
                        CommandCategory.Navigate -> PaletteSection.Views
                        CommandCategory.System -> PaletteSection.System
                    },
                    searchableText = buildString {
                        append("keyboard shortcuts help keys ")
                        append(command.title)
                        append(' ')
                        append(command.description.orEmpty())
                        append(' ')
                        append(command.aliases.joinToString(" "))
                        append(' ')
                        append(command.shortcut.displayLabel)
                        if (!command.isEnabled(context)) append(" unavailable")
                    },
                )
            }

        return movement + paletteEntries + commands
    }

    private fun commandResult(
        command: eu.revq.commands.AppCommand,
        section: PaletteSection,
        context: CommandContext,
    ): PaletteResult.CommandResult {
        val enabled = command.isEnabled(context)
        return PaletteResult.CommandResult(
            command = command,
            section = section,
            enabled = enabled,
            disabledReason = if (enabled) null else command.disabledReason(context),
        )
    }

    private fun shortcut(
        key: String,
        title: String,
        subtitle: String,
        shortcut: String,
        section: PaletteSection,
    ) = PaletteResult.ShortcutResult(
        stableKey = key,
        title = title,
        subtitle = subtitle,
        shortcutLabel = shortcut,
        section = section,
        searchableText = "keyboard shortcuts help keys $title $subtitle $shortcut",
    )
}
