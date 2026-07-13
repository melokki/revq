package eu.revq.commands

import eu.revq.AppState
import eu.revq.PullRequestSource
import eu.revq.View
import eu.revq.UpdateState

object CommandRegistry {
    private val commands: List<AppCommand> = listOf(
        AppCommand(
            id = CommandId.MarkSelectedReviewed,
            title = "Handle or merge selected PR",
            description = "Mark a review request handled, or merge an approved clean personal PR.",
            category = CommandCategory.Review,
            aliases = listOf("done", "handled", "reviewed", "merge", "merge pr"),
            shortcut = Shortcut.single(ShortcutKey.M),
            isEnabled = {
                it.selectedPullRequest?.source == PullRequestSource.ReviewRequest ||
                        it.canMergeSelectedPullRequest
            },
        ),
        AppCommand(
            id = CommandId.NextReview,
            title = "Skip for now",
            description = "Leave the current review in the queue and move to the next one.",
            category = CommandCategory.Review,
            aliases = listOf("next", "skip"),
            shortcut = Shortcut.single(ShortcutKey.S),
            isEnabled = { it.reviewQueueSize > 0 },
        ),
        AppCommand(
            id = CommandId.OpenSelectedPrInGitHub,
            title = "Open selected PR",
            description = "Open the selected pull request in GitHub.",
            category = CommandCategory.Review,
            aliases = listOf("github", "browser", "open pull request"),
            shortcut = Shortcut.single(ShortcutKey.O),
            isEnabled = { it.hasSelectedPullRequest },
        ),
        AppCommand(
            id = CommandId.CopySelectedPrUrl,
            title = "Copy selected PR URL",
            category = CommandCategory.Review,
            aliases = listOf("copy link", "url"),
            shortcut = Shortcut.single(ShortcutKey.C),
            isEnabled = { it.hasSelectedPullRequest },
        ),
        AppCommand(
            id = CommandId.CopySelectedPrMarkdown,
            title = "Copy selected PR as Markdown",
            category = CommandCategory.Review,
            aliases = listOf("markdown", "copy markdown"),
            isEnabled = { it.hasSelectedPullRequest },
        ),
        AppCommand(
            id = CommandId.CopyReviewQueueDigest,
            title = "Copy review queue digest",
            category = CommandCategory.Review,
            aliases = listOf("digest", "queue summary"),
            shortcut = Shortcut.single(ShortcutKey.D),
            isEnabled = { it.reviewQueueSize > 0 },
        ),
        AppCommand(
            id = CommandId.ToggleSelectedPrPin,
            title = "Pin/unpin selected PR",
            category = CommandCategory.Review,
            aliases = listOf("pin", "unpin", "favorite"),
            shortcut = Shortcut.single(ShortcutKey.P),
            isEnabled = { it.hasSelectedPullRequest },
        ),
        AppCommand(
            id = CommandId.OpenTopReviewPullRequests,
            title = "Open top review PRs",
            category = CommandCategory.Review,
            aliases = listOf("open queue", "batch open"),
            isEnabled = { it.reviewQueueSize > 0 },
        ),
        AppCommand(
            id = CommandId.OpenSelectedRepository,
            title = "Open selected repository",
            category = CommandCategory.Review,
            aliases = listOf("repository", "repo", "github repository"),
            isEnabled = { it.hasSelectedPullRequest },
        ),
        AppCommand(
            id = CommandId.ToggleMuteSelectedRepository,
            title = "Mute/unmute selected repository",
            category = CommandCategory.Review,
            aliases = listOf("mute repository", "unmute repository", "hide repo"),
            isEnabled = { it.hasSelectedPullRequest },
        ),

        AppCommand(
            id = CommandId.GoToNeedsReview,
            title = "Go to Needs Review",
            category = CommandCategory.Navigate,
            aliases = listOf("reviews", "queue"),
        ),
        AppCommand(
            id = CommandId.GoToPinned,
            title = "Go to Pinned",
            category = CommandCategory.Navigate,
            aliases = listOf("pins", "favorites"),
        ),
        AppCommand(
            id = CommandId.GoToMyPullRequests,
            title = "Go to My Pull Requests",
            category = CommandCategory.Navigate,
            aliases = listOf("mine", "my prs", "authored"),
        ),
        AppCommand(
            id = CommandId.GoToBlocked,
            title = "Go to Blocked",
            category = CommandCategory.Navigate,
            aliases = listOf("blocked", "failing"),
        ),
        AppCommand(
            id = CommandId.GoToReady,
            title = "Go to Ready",
            category = CommandCategory.Navigate,
            aliases = listOf("ready", "approved"),
        ),
        AppCommand(
            id = CommandId.GoToToday,
            title = "Go to Today",
            category = CommandCategory.Navigate,
            aliases = listOf("today", "attention"),
        ),
        AppCommand(
            id = CommandId.GoToReviewed,
            title = "Go to Reviewed",
            category = CommandCategory.Navigate,
            aliases = listOf("handled", "done", "reviewed"),
        ),
        AppCommand(
            id = CommandId.GoToSettings,
            title = "Go to Settings",
            category = CommandCategory.Navigate,
            aliases = listOf("preferences", "configuration", "config"),
        ),

        AppCommand(
            id = CommandId.Refresh,
            title = "Refresh",
            description = "Refresh GitHub pull request data.",
            category = CommandCategory.System,
            aliases = listOf("reload", "sync"),
            shortcut = Shortcut.single(ShortcutKey.R),
            isEnabled = { !it.isRefreshing },
            disabledReason = { "Refresh is already running." },
        ),
        AppCommand(
            id = CommandId.TestGitHubCli,
            title = "Test GitHub CLI",
            category = CommandCategory.System,
            aliases = listOf("gh", "connection", "github cli"),
            isEnabled = { !it.isTestingGh },
        ),
        AppCommand(
            id = CommandId.DiscoverRepositories,
            title = "Discover repositories",
            category = CommandCategory.System,
            aliases = listOf("discover", "repositories", "organizations"),
            isEnabled = { !it.isDiscovering },
        ),
        AppCommand(
            id = CommandId.PreviewReminder,
            title = "Preview reminder",
            category = CommandCategory.System,
            aliases = listOf("reminder", "preview notification"),
        ),
        AppCommand(
            id = CommandId.CopyDiagnostics,
            title = "Copy diagnostics",
            category = CommandCategory.System,
            aliases = listOf("debug", "diagnostics", "support"),
        ),
        AppCommand(
            id = CommandId.ShowKeyboardShortcuts,
            title = "Show keyboard shortcuts",
            category = CommandCategory.System,
            aliases = listOf("help", "keys", "shortcuts"),
        ),
        AppCommand(
            id = CommandId.InstallUpdate,
            title = "Install RevQ update",
            description = "Download, verify, install, and restart RevQ automatically.",
            category = CommandCategory.Updates,
            aliases = listOf("update", "install", "version", "upgrade"),
            isEnabled = { it.updateState is UpdateState.Available },
        ),
        AppCommand(
            id = CommandId.ViewReleaseNotes,
            title = "View release notes",
            category = CommandCategory.Updates,
            aliases = listOf("update", "version", "what's new", "changelog"),
            isEnabled = {
                (it.updateState as? UpdateState.Available)?.release?.notes?.isNotBlank() == true
            },
        ),
        AppCommand(
            id = CommandId.DismissUpdate,
            title = "Dismiss update",
            category = CommandCategory.Updates,
            aliases = listOf("update", "release", "version", "hide"),
            isEnabled = {
                (it.updateState as? UpdateState.Available)?.dismissed == false
            },
        ),
        AppCommand(
            id = CommandId.CancelUpdateDownload,
            title = "Cancel update download",
            category = CommandCategory.Updates,
            aliases = listOf("update", "download", "cancel"),
            isEnabled = { it.updateState is UpdateState.Downloading },
        ),
        AppCommand(
            id = CommandId.CheckForUpdates,
            title = "Check for updates",
            category = CommandCategory.Updates,
            aliases = listOf("update", "version", "release", "check now"),
            isEnabled = { it.updateState != UpdateState.Checking },
        ),
    )

    private val byId: Map<CommandId, AppCommand> = commands.associateBy { it.id }

    fun all(): List<AppCommand> = commands

    fun find(id: CommandId): AppCommand? = byId[id]

    fun inCategory(category: CommandCategory): List<AppCommand> =
        commands.filter { it.category == category }

    fun findBySingleStroke(stroke: ShortcutStroke): AppCommand? =
        commands.firstOrNull { command ->
            command.shortcut?.strokes == listOf(stroke)
        }

    fun execute(
        commandId: CommandId,
        state: AppState,
    ): CommandExecutionResult {
        val command = find(commandId) ?: return CommandExecutionResult.Missing
        if (!command.isEnabled(CommandContext.from(state))) {
            return CommandExecutionResult.Disabled
        }

        when (commandId) {
            CommandId.MarkSelectedReviewed -> state.performSelectedMAction()
            CommandId.NextReview -> state.nextReview()
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
            CommandId.TestGitHubCli -> state.testGithubCli()
            CommandId.DiscoverRepositories -> state.discoverTargets()
            CommandId.PreviewReminder -> state.previewReminderWindow()
            CommandId.CopyDiagnostics -> state.copyDiagnostics()
            CommandId.ShowKeyboardShortcuts -> state.statusLine = "Use ? to open keyboard shortcuts"
            CommandId.InstallUpdate -> state.downloadAndInstallUpdate()
            CommandId.ViewReleaseNotes -> state.showUpdateReleaseNotes()
            CommandId.DismissUpdate -> state.dismissUpdate()
            CommandId.CancelUpdateDownload -> state.cancelUpdateDownload()
            CommandId.CheckForUpdates -> state.checkForUpdates()
        }

        state.recordCommandExecution(commandId)
        return CommandExecutionResult.Executed
    }
}
