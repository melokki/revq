package eu.revq.commands

import eu.revq.PullRequestSource

object CommandRegistry {
    private val commands: List<AppCommand> = listOf(
        AppCommand(
            id = CommandId.StartReviewSession,
            title = "Start review session",
            description = "Begin a focused pass through the review queue.",
            category = CommandCategory.Review,
            aliases = listOf("review", "session", "start reviewing"),
            isEnabled = { it.reviewQueueSize > 0 && !it.reviewSessionActive },
            disabledReason = {
                when {
                    it.reviewSessionActive -> "A review session is already active."
                    it.reviewQueueSize == 0 -> "No PRs need review right now."
                    else -> null
                }
            },
        ),
        AppCommand(
            id = CommandId.MarkSelectedReviewed,
            title = "Mark reviewed & next",
            description = "Handle the selected review locally and advance to the next review.",
            category = CommandCategory.Review,
            aliases = listOf("done", "handled", "reviewed", "ship", "complete", "finish review"),
            shortcut = Shortcut.single(ShortcutKey.M),
            isEnabled = {
                it.selectedPullRequest?.source == PullRequestSource.ReviewRequest
            },
            disabledReason = {
                if (it.selectedPullRequest == null) {
                    "Select a review request first."
                } else {
                    "The selected PR is not waiting on your review."
                }
            },
        ),
        AppCommand(
            id = CommandId.UndoLastReviewed,
            title = "Undo last reviewed",
            description = "Restore the last pull request you marked reviewed.",
            category = CommandCategory.Review,
            aliases = listOf("undo", "restore", "unreview", "bring back"),
            shortcut = Shortcut.single(ShortcutKey.U),
            isEnabled = { it.canUndoReview },
            disabledReason = { "Nothing reviewed in this session to undo." },
        ),
        AppCommand(
            id = CommandId.NextReview,
            title = "Skip for now",
            description = "Leave the current review in the queue and move to the next one.",
            category = CommandCategory.Review,
            aliases = listOf("next", "skip"),
            shortcut = Shortcut.single(ShortcutKey.S),
            isEnabled = { it.reviewQueueSize > 0 },
            disabledReason = { "No PRs need review right now." },
        ),
        AppCommand(
            id = CommandId.OpenSelectedPrInGitHub,
            title = "Open selected PR",
            description = "Open the selected pull request in GitHub.",
            category = CommandCategory.Review,
            aliases = listOf("github", "browser", "open pull request", "open pr", "view pr"),
            shortcut = Shortcut.single(ShortcutKey.O),
            isEnabled = { it.hasSelectedPullRequest },
            disabledReason = { "Select a pull request first." },
        ),
        AppCommand(
            id = CommandId.CopySelectedPrUrl,
            title = "Copy selected PR URL",
            category = CommandCategory.Review,
            aliases = listOf("copy link", "url"),
            shortcut = Shortcut.single(ShortcutKey.C),
            isEnabled = { it.hasSelectedPullRequest },
            disabledReason = { "Select a pull request first." },
        ),
        AppCommand(
            id = CommandId.CopySelectedPrMarkdown,
            title = "Copy selected PR as Markdown",
            category = CommandCategory.Review,
            aliases = listOf("markdown", "copy markdown"),
            isEnabled = { it.hasSelectedPullRequest },
            disabledReason = { "Select a pull request first." },
        ),
        AppCommand(
            id = CommandId.CopyReviewQueueDigest,
            title = "Copy review queue digest",
            category = CommandCategory.Review,
            aliases = listOf("digest", "queue summary"),
            shortcut = Shortcut.single(ShortcutKey.D),
            isEnabled = { it.reviewQueueSize > 0 },
            disabledReason = { "No PRs need review right now." },
        ),
        AppCommand(
            id = CommandId.ToggleSelectedPrPin,
            title = "Pin/unpin selected PR",
            category = CommandCategory.Review,
            aliases = listOf("pin", "unpin", "favorite"),
            shortcut = Shortcut.single(ShortcutKey.P),
            isEnabled = { it.hasSelectedPullRequest },
            disabledReason = { "Select a pull request first." },
        ),
        AppCommand(
            id = CommandId.OpenTopReviewPullRequests,
            title = "Open top review PRs",
            category = CommandCategory.Review,
            aliases = listOf("open queue", "batch open"),
            isEnabled = { it.reviewQueueSize > 0 },
            disabledReason = { "No PRs need review right now." },
        ),
        AppCommand(
            id = CommandId.OpenSelectedRepository,
            title = "Open selected repository",
            category = CommandCategory.Review,
            aliases = listOf("repository", "repo", "github repository"),
            isEnabled = { it.hasSelectedPullRequest },
            disabledReason = { "Select a pull request first." },
        ),
        AppCommand(
            id = CommandId.ToggleMuteSelectedRepository,
            title = "Mute/unmute selected repository",
            category = CommandCategory.Review,
            aliases = listOf("mute repository", "unmute repository", "hide repo"),
            isEnabled = { it.hasSelectedPullRequest },
            disabledReason = { "Select a pull request first." },
        ),

        AppCommand(
            id = CommandId.GoToNeedsReview,
            title = "Go to Needs Review",
            category = CommandCategory.Navigate,
            aliases = listOf("reviews", "queue", "review queue", "needs review"),
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
            aliases = listOf("mine", "my prs", "authored", "my pull requests"),
        ),
        AppCommand(
            id = CommandId.GoToBlocked,
            title = "Go to Blocked",
            category = CommandCategory.Navigate,
            aliases = listOf("blocked", "failing", "stuck", "conflict"),
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
            aliases = listOf("preferences", "configuration", "config", "prefs", "options"),
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
            disabledReason = { "GitHub CLI test is already running." },
        ),
        AppCommand(
            id = CommandId.DiscoverRepositories,
            title = "Discover repositories",
            category = CommandCategory.System,
            aliases = listOf("discover", "repositories", "organizations"),
            isEnabled = { !it.isDiscovering },
            disabledReason = { "Repository discovery is already running." },
        ),
        AppCommand(
            id = CommandId.ToggleFocusReviewMode,
            title = "Toggle focus review mode",
            category = CommandCategory.System,
            aliases = listOf("focus", "focus mode"),
            shortcut = Shortcut.single(ShortcutKey.F),
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
}
