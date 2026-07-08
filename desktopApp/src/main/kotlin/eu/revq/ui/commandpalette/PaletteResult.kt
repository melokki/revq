package eu.revq.ui.commandpalette

import eu.revq.PullRequest
import eu.revq.View
import eu.revq.commands.AppCommand
import eu.revq.commands.CommandId

enum class PaletteSection(val label: String) {
    Actions("Actions"),
    Recent("Recent"),
    Suggested("Suggested"),
    Review("Review"),
    Views("Views"),
    System("System"),
    PullRequests("Pull requests"),
    Repositories("Repositories"),
    Movement("Movement"),
    Application("Application"),
}

sealed interface PaletteResult {
    val stableKey: String
    val title: String
    val subtitle: String?
    val shortcutLabel: String?
    val section: PaletteSection
    val enabled: Boolean
    val searchableText: String

    data class CommandResult(
        val command: AppCommand,
        override val section: PaletteSection,
        override val enabled: Boolean,
        val disabledReason: String? = null,
    ) : PaletteResult {
        override val stableKey: String = "command:${command.id.name}:${section.name}"
        override val title: String = command.title
        override val subtitle: String? = if (enabled) {
            command.description
        } else {
            disabledReason
                ?.takeIf { it.isNotBlank() }
                ?: command.description
                    ?.takeIf { it.isNotBlank() }
                ?: "Unavailable right now."
        }
        override val shortcutLabel: String? = command.shortcut?.displayLabel
        override val searchableText: String = buildString {
            append(command.title)
            append(' ')
            append(command.description.orEmpty())
            append(' ')
            append(disabledReason.orEmpty())
            append(' ')
            append(command.aliases.joinToString(" "))
            append(' ')
            append(command.shortcut?.displayLabel.orEmpty())
        }
    }

    data class PullRequestResult(
        val pullRequest: PullRequest,
        val targetView: View,
        override val subtitle: String,
        override val searchableText: String,
    ) : PaletteResult {
        override val stableKey: String = "pr:${pullRequest.source}:${pullRequest.key}:$targetView"
        override val title: String = "#${pullRequest.number}  ${pullRequest.title}"
        override val shortcutLabel: String? = null
        override val section: PaletteSection = PaletteSection.PullRequests
        override val enabled: Boolean = true
    }

    data class RepositoryResult(
        val repository: String,
    ) : PaletteResult {
        override val stableKey: String = "repo:$repository"
        override val title: String = repository
        override val subtitle: String = "Filter the current view to this repository"
        override val shortcutLabel: String? = null
        override val section: PaletteSection = PaletteSection.Repositories
        override val enabled: Boolean = true
        override val searchableText: String = repository
    }

    data class ShortcutResult(
        override val stableKey: String,
        override val title: String,
        override val subtitle: String?,
        override val shortcutLabel: String,
        override val section: PaletteSection,
        override val searchableText: String,
    ) : PaletteResult {
        override val enabled: Boolean = true
    }

    data object GoToTopResult : PaletteResult {
        override val stableKey: String = "navigation:top"
        override val title: String = "Top of current list"
        override val subtitle: String = "Jump to the first item in the active region"
        override val shortcutLabel: String? = null
        override val section: PaletteSection = PaletteSection.Views
        override val enabled: Boolean = true
        override val searchableText: String = "top first item list"
    }
}

fun PaletteResult.executionPreview(): String = when (this) {
    is PaletteResult.CommandResult -> if (enabled) {
        when (command.id) {
            CommandId.ClearFilter -> "Clear the active pull request filter"
            CommandId.EndReviewSession -> "End the current review session"
            CommandId.ToggleMuteSelectedRepository -> "Hide or restore the selected repository"
            CommandId.ToggleSelectedPrPin -> "Pin or unpin the selected pull request"
            CommandId.OpenSelectedPrInGitHub -> "Open the selected pull request in GitHub"
            else -> "Run ${command.title}"
        }
    } else {
        "Unavailable: ${subtitle ?: "This command cannot run now."}"
    }

    is PaletteResult.PullRequestResult -> {
        "Open #${pullRequest.number} in ${targetView.label}"
    }

    is PaletteResult.RepositoryResult -> {
        "Filter current view to $repository"
    }

    is PaletteResult.ShortcutResult -> {
        "Reference shortcut only"
    }

    PaletteResult.GoToTopResult -> {
        "Jump to the first item in the focused region"
    }
}

fun PaletteResult.typeLabel(): String = when (this) {
    is PaletteResult.CommandResult -> "Command"
    is PaletteResult.PullRequestResult -> "PR"
    is PaletteResult.RepositoryResult -> "Repo"
    is PaletteResult.ShortcutResult -> "Shortcut"
    PaletteResult.GoToTopResult -> "Nav"
}
