package eu.revq.commands

enum class CommandId {
    MarkSelectedReviewed,
    NextReview,
    OpenSelectedPrInGitHub,
    CopySelectedPrUrl,
    CopySelectedPrMarkdown,
    CopyReviewQueueDigest,
    ToggleSelectedPrPin,
    OpenTopReviewPullRequests,
    OpenSelectedRepository,
    ToggleMuteSelectedRepository,

    GoToNeedsReview,
    GoToMyPullRequests,
    GoToPinned,
    GoToToday,
    GoToBlocked,
    GoToReady,
    GoToReviewed,
    GoToSettings,

    Refresh,
    TestGitHubCli,
    DiscoverRepositories,
    PreviewReminder,
    CopyDiagnostics,
    ShowKeyboardShortcuts,
}
