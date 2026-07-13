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
import eu.revq.commands.CommandSurface

object PaletteResultProvider {
    fun catalog(
        mode: PaletteMode,
        state: AppState,
    ): PaletteCatalog {
        val blankResults = rawResults(mode, state, query = "")
        val searchableResults = rawResults(mode, state, query = "catalog")
        return PaletteCatalog(blankResults, searchableResults)
    }

    fun results(
        mode: PaletteMode,
        state: AppState,
        query: String,
    ): List<PaletteResult> = catalog(mode, state).results(query)

    private fun rawResults(
        mode: PaletteMode,
        state: AppState,
        query: String,
    ): List<PaletteResult> = when (mode) {
            PaletteMode.Universal -> universalResults(state, query)
            PaletteMode.RepositoryScope -> repositoryScopeResults(state)
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
            val recentTargets = recentTargetResults(state)

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

            return contextual + recentTargets + recent + suggested
        }

        return commandResults(state) +
                pullRequestSearchResults(state) +
                repositoryResults(state) +
                shortcutReferenceResults(state)
    }

    private fun commandResults(state: AppState): List<PaletteResult> {
        val context = CommandContext.from(state)
        val recentIds = state.recentCommandIds.toSet()
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
                        CommandCategory.Updates -> PaletteSection.Updates
                    },
                    context = context,
                    relevanceBoost = if (command.id in recentIds) -10 else 0,
                )
            }

        return contextual + remaining
    }

    private fun contextActionResults(state: AppState): List<PaletteResult> {
        val context = CommandContext.from(state)
        return CommandSurface.contextualCommands(state)
            .map { entry ->
                commandResult(
                    command = entry.command,
                    section = PaletteSection.Actions,
                    context = context,
                    relevanceBoost = -20,
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
            addAll(pr.requestedReviewers)
            addAll(pr.changeRequestReviewers)
            addAll(pr.approvingReviewers)
            addAll(pr.unresolvedDiscussionAuthors)
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

    private fun repositoryResults(state: AppState): List<PaletteResult.RepositoryResult> =
        parseLines(state.repositoriesText)
            .distinct()
            .sorted()
            .filterNot(state::isRepositoryMuted)
            .map { PaletteResult.RepositoryResult(repository = it) }

    private fun repositoryScopeResults(state: AppState): List<PaletteResult> {
        val currentScope = state.currentQueueScopeFilter()
        val currentRepository = (currentScope as? eu.revq.QueueScopeFilter.Repository)?.nameWithOwner
        val currentOrganization = (currentScope as? eu.revq.QueueScopeFilter.Organization)?.owner
        val trackedRepositories = parseLines(state.repositoriesText)
            .distinct()
            .sorted()
        val trackedOrganizations = (
            parseLines(state.organizationsText) +
                trackedRepositories.mapNotNull { it.substringBefore('/').takeIf(String::isNotBlank) }
            )
            .distinct()
            .sorted()

        return buildList {
            add(
                PaletteResult.RepositoryResult(
                    repository = null,
                    selected = currentScope == eu.revq.QueueScopeFilter.All,
                    scopeSelection = true,
                ),
            )
            trackedOrganizations.forEach { organization ->
                add(
                    PaletteResult.OrganizationResult(
                        organization = organization,
                        selected = organization == currentOrganization,
                    ),
                )
            }
            trackedRepositories.forEach { repository ->
                add(
                    PaletteResult.RepositoryResult(
                        repository = repository,
                        selected = repository == currentRepository,
                        scopeSelection = true,
                    ),
                )
            }
            add(PaletteResult.RepositoryManagementResult)
        }
    }

    private fun recentTargetResults(state: AppState): List<PaletteResult> {
        val pullRequests = state.activePullRequests().associateBy { it.key }
        val repositories = parseLines(state.repositoriesText).toSet()

        return state.recentPaletteTargets.mapNotNull { target ->
            when {
                target.startsWith("pr:") -> {
                    val pr = pullRequests[target.removePrefix("pr:")] ?: return@mapNotNull null
                    pullRequestResult(pr, defaultTargetView(state, pr))
                }
                target.startsWith("repo:") -> {
                    val repo = target.removePrefix("repo:")
                    if (repo in repositories) PaletteResult.RepositoryResult(repository = repo) else null
                }
                else -> null
            }
        }.map { result ->
            when (result) {
                is PaletteResult.PullRequestResult -> result.copy(subtitle = "Recent · ${result.subtitle}")
                is PaletteResult.RepositoryResult -> result
                else -> result
            }
        }
    }

    private fun shortcutReferenceResults(state: AppState): List<PaletteResult> {
        val movement = listOf(
            shortcut("movement:j-k", "Move selection", "Move within the focused region", "j / k", PaletteSection.Movement),
            shortcut("movement:h-l", "Move between regions", "Sidebar and pull request list", "h / l", PaletteSection.Movement),
            shortcut("movement:first-last", "First or last item", "Jump to a queue boundary", "Home / End", PaletteSection.Movement),
            shortcut("movement:page", "Viewport movement", "Move through longer lists by one viewport", "Page Up / Down", PaletteSection.Movement),
            shortcut("movement:activate", "Toggle details", "Expand or close inline details for the selected pull request", "Enter", PaletteSection.Movement),
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
                        CommandCategory.Updates -> PaletteSection.Updates
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
        relevanceBoost: Int = 0,
    ): PaletteResult.CommandResult {
        val enabled = command.isEnabled(context)
        val selected = context.selectedPullRequest
        val primaryAction = if (command.id == CommandId.MarkSelectedReviewed && selected != null) {
            if (selected.source == PullRequestSource.ReviewRequest) {
                PrimaryActionPresentation(
                    title = "Mark selected PR reviewed",
                    preview = "Mark #${selected.number} reviewed and move to the next review",
                )
            } else {
                PrimaryActionPresentation(
                    title = "Merge selected PR",
                    preview = "Merge ${selected.repository} #${selected.number}",
                    confirmation = "Press Enter again to merge ${selected.repository} #${selected.number}",
                )
            }
        } else {
            null
        }
        return PaletteResult.CommandResult(
            command = command,
            section = section,
            enabled = enabled,
            disabledReason = if (enabled) null else command.disabledReason(context),
            displayTitle = primaryAction?.title ?: command.title,
            executionDescription = primaryAction?.preview,
            confirmationPrompt = primaryAction?.confirmation,
            relevanceBoost = relevanceBoost,
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

class PaletteCatalog(
    blankResults: List<PaletteResult>,
    searchableResults: List<PaletteResult>,
) {
    private val blankResults = blankResults.toList()
    private val searchableResults = searchableResults.toList()

    fun results(query: String): List<PaletteResult> = if (query.isBlank()) {
        blankResults
    } else {
        filterPaletteResults(searchableResults, query)
    }
}

private data class PrimaryActionPresentation(
    val title: String,
    val preview: String,
    val confirmation: String? = null,
)
