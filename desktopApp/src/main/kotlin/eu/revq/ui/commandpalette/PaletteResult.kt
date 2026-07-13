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
    Management("Management"),
    Movement("Movement"),
    Application("Application"),
    Updates("Updates"),
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
        val repository: String?,
        val selected: Boolean = false,
        val scopeSelection: Boolean = false,
    ) : PaletteResult {
        override val stableKey: String = "repo:${repository ?: "all"}:${if (scopeSelection) "scope" else "search"}"
        override val title: String = repository ?: "All repositories"
        override val subtitle: String = when {
            scopeSelection && selected -> "Current repository scope"
            scopeSelection -> "Switch repository scope"
            else -> "Scope all queues to this repository"
        }
        override val shortcutLabel: String? = if (selected) "Current" else null
        override val section: PaletteSection = PaletteSection.Repositories
        override val enabled: Boolean = true
        override val searchableText: String = buildString {
            append(repository ?: "all repositories clear scope")
            if (selected) append(" current selected")
        }
    }

    data class OrganizationResult(
        val organization: String,
        val selected: Boolean = false,
    ) : PaletteResult {
        override val stableKey: String = "org:$organization:scope"
        override val title: String = organization
        override val subtitle: String =
            if (selected) "Current organization scope" else "Switch organization scope"
        override val shortcutLabel: String? = if (selected) "Current" else null
        override val section: PaletteSection = PaletteSection.Repositories
        override val enabled: Boolean = true
        override val searchableText: String = "$organization organization org scope"
    }

    data object RepositoryManagementResult : PaletteResult {
        override val stableKey: String = "repository:manage"
        override val title: String = "Manage tracked repositories"
        override val subtitle: String = "Open Settings directly on the Tracking section"
        override val shortcutLabel: String? = null
        override val section: PaletteSection = PaletteSection.Management
        override val enabled: Boolean = true
        override val searchableText: String = "manage tracked repositories settings tracking"
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
            CommandId.ToggleMuteSelectedRepository -> "Hide or restore the selected repository"
            CommandId.ToggleSelectedPrPin -> "Pin or unpin the selected pull request"
            CommandId.OpenSelectedPrInGitHub -> "Open the selected pull request in GitHub"
            else -> "Run ${command.title}"
        }
    } else {
        "Unavailable: ${subtitle ?: "This command cannot run now."}"
    }

    is PaletteResult.PullRequestResult -> "Open #${pullRequest.number} in ${targetView.label}"
    is PaletteResult.RepositoryResult -> when {
        !scopeSelection -> "Scope all queues to ${repository ?: "all repositories"}"
        repository == null -> "Clear repository scope"
        selected -> "Keep repository scope on $repository"
        else -> "Switch repository scope to $repository"
    }
    is PaletteResult.OrganizationResult ->
        if (selected) "Keep organization scope on $organization" else "Switch organization scope to $organization"
    PaletteResult.RepositoryManagementResult -> "Open Settings directly on repository tracking"
    is PaletteResult.ShortcutResult -> "Reference shortcut only"
    PaletteResult.GoToTopResult -> "Jump to the first item in the focused region"
}

fun PaletteResult.typeLabel(): String = when (this) {
    is PaletteResult.CommandResult -> "Command"
    is PaletteResult.PullRequestResult -> "PR"
    is PaletteResult.RepositoryResult -> if (scopeSelection) "Scope" else "Repo"
    is PaletteResult.OrganizationResult -> "Org"
    PaletteResult.RepositoryManagementResult -> "Action"
    is PaletteResult.ShortcutResult -> "Shortcut"
    PaletteResult.GoToTopResult -> "Nav"
}
