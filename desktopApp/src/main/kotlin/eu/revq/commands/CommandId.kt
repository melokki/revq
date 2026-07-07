package eu.revq.commands

enum class CommandId {
    StartReviewSession,
    MarkSelectedReviewed,
    UndoLastReviewed,
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
    ToggleFocusReviewMode,
    PreviewReminder,
    CopyDiagnostics,
    ShowKeyboardShortcuts,
}
