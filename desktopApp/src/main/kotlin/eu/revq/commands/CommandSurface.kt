package eu.revq.commands

import eu.revq.AppState
import eu.revq.PullRequestSource

data class CommandSurfaceCommand(
    val command: AppCommand,
    val enabled: Boolean,
    val disabledReason: String? = null,
)

object CommandSurface {
    fun contextualCommands(state: AppState): List<CommandSurfaceCommand> {
        val context = CommandContext.from(state)
        val selected = state.selectedPullRequest
        val ids = buildList {
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
            when (state.updateState) {
                is eu.revq.UpdateState.Available -> {
                    add(CommandId.InstallUpdate)
                    add(CommandId.ViewReleaseNotes)
                    add(CommandId.DismissUpdate)
                }
                is eu.revq.UpdateState.Downloading -> add(CommandId.CancelUpdateDownload)
                else -> Unit
            }
        }

        return ids
            .distinct()
            .mapNotNull(CommandRegistry::find)
            .filter { it.isEnabled(context) }
            .map { command ->
                CommandSurfaceCommand(
                    command = command,
                    enabled = true,
                )
            }
    }
}
