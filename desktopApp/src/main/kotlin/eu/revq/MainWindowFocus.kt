package eu.revq

enum class MainWindowFocusTrigger {
    ContentReady,
    WindowActivated,
}

enum class MainWindowFocusAction {
    BringToFront,
    RequestNativeFocus,
    RequestComposeRootFocus,
}

fun mainWindowFocusActions(trigger: MainWindowFocusTrigger): List<MainWindowFocusAction> =
    when (trigger) {
        MainWindowFocusTrigger.ContentReady -> listOf(
            MainWindowFocusAction.BringToFront,
            MainWindowFocusAction.RequestNativeFocus,
            MainWindowFocusAction.RequestComposeRootFocus,
        )

        MainWindowFocusTrigger.WindowActivated -> listOf(
            MainWindowFocusAction.RequestComposeRootFocus,
        )
    }
