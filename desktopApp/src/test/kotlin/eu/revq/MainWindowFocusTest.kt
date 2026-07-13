package eu.revq

import kotlin.test.Test
import kotlin.test.assertEquals

class MainWindowFocusTest {
    @Test
    fun `ready main window activates native window before requesting compose focus`() {
        assertEquals(
            listOf(
                MainWindowFocusAction.BringToFront,
                MainWindowFocusAction.RequestNativeFocus,
                MainWindowFocusAction.RequestComposeRootFocus,
            ),
            mainWindowFocusActions(MainWindowFocusTrigger.ContentReady),
        )
    }

    @Test
    fun `activated main window only refreshes compose focus`() {
        assertEquals(
            listOf(MainWindowFocusAction.RequestComposeRootFocus),
            mainWindowFocusActions(MainWindowFocusTrigger.WindowActivated),
        )
    }
}
