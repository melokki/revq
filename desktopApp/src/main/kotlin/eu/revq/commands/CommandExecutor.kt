package eu.revq.commands

import eu.revq.AppState
import eu.revq.View

enum class CommandExecutionResult {
    Executed,
    Disabled,
    Missing,
}

class CommandExecutor(
    private val state: AppState,
) {
    fun execute(commandId: CommandId): CommandExecutionResult {
        val command = CommandRegistry.find(commandId)
            ?: return CommandExecutionResult.Missing

        val context = CommandContext.from(state)
        if (!command.isEnabled(context)) {
            return CommandExecutionResult.Disabled
        }

        when (commandId) {
            CommandId.StartReviewSession -> state.startReviewing()
            CommandId.MarkSelectedReviewed -> state.markReviewed()
            CommandId.UndoLastReviewed -> state.undoMarkReviewed()
            CommandId.PreviousReview -> state.previousReview()
            CommandId.NextReview -> state.nextReview()
            CommandId.EndReviewSession -> state.endReviewSession()
            CommandId.OpenSelectedPrInGitHub -> state.openSelectedInGitHub()
            CommandId.CopySelectedPrUrl -> state.copySelectedUrl()
            CommandId.CopySelectedPrMarkdown -> state.copySelectedMarkdown()
            CommandId.CopyReviewQueueDigest -> state.copyReviewDigest()
            CommandId.ToggleSelectedPrPin -> state.togglePin()
            CommandId.OpenTopReviewPullRequests -> state.openTopReviewPullRequests()
            CommandId.OpenSelectedRepository -> state.openSelectedRepository()
            CommandId.ToggleMuteSelectedRepository -> state.toggleMuteSelectedRepository()

            CommandId.GoToNeedsReview -> state.selectView(View.NeedsReview)
            CommandId.GoToMyPullRequests -> state.selectView(View.Mine)
            CommandId.GoToPinned -> state.selectView(View.Pinned)
            CommandId.GoToToday -> state.selectView(View.Today)
            CommandId.GoToBlocked -> state.selectView(View.Blocked)
            CommandId.GoToReady -> state.selectView(View.Ready)
            CommandId.GoToReviewed -> state.selectView(View.Handled)
            CommandId.GoToSettings -> state.selectView(View.Settings)

            CommandId.Refresh -> state.refresh()
            CommandId.ClearFilter -> state.clearFilter()
            CommandId.ToggleGroupByRepository -> state.toggleGroupByRepository()
            CommandId.ToggleCompactRows -> state.toggleCompactRows()
            CommandId.CycleSortMode -> state.cycleSortMode()
            CommandId.TestGitHubCli -> state.testGithubCli()
            CommandId.DiscoverRepositories -> state.discoverTargets()
            CommandId.ToggleFocusReviewMode -> state.toggleFocusMode()
            CommandId.PreviewReminder -> state.previewReminderWindow()
            CommandId.CopyDiagnostics -> state.copyDiagnostics()
            CommandId.ShowKeyboardShortcuts -> state.statusLine = "Open Space and search keyboard shortcuts"
        }

        state.recordCommandExecution(commandId)
        return CommandExecutionResult.Executed
    }
}
