package eu.revq.keyboard

import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import eu.revq.View
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(InternalComposeUiApi::class)
class KeyboardRouterTest {
    @Test
    fun homeMovesToFirstVisiblePullRequest() {
        val action = KeyboardRouter().route(
            event = keyDown(Key.MoveHome),
            context = normalQueueContext(),
        )

        assertEquals(KeyboardAction.FirstItem, action)
    }

    @Test
    fun endMovesToLastVisiblePullRequest() {
        val action = KeyboardRouter().route(
            event = keyDown(Key.MoveEnd),
            context = normalQueueContext(),
        )

        assertEquals(KeyboardAction.LastItem, action)
    }

    @Test
    fun pageDownMovesByOneViewport() {
        val action = KeyboardRouter().route(
            event = keyDown(Key.PageDown),
            context = normalQueueContext(),
        )

        assertEquals(KeyboardAction.PageDown, action)
    }

    @Test
    fun pageUpMovesByOneViewport() {
        val action = KeyboardRouter().route(
            event = keyDown(Key.PageUp),
            context = normalQueueContext(),
        )

        assertEquals(KeyboardAction.PageUp, action)
    }

    private fun normalQueueContext() = KeyboardContext(
        mode = KeyboardMode.Normal,
        focusRegion = FocusRegion.PullRequestList,
        view = View.NeedsReview,
        commandPaletteOpen = false,
    )

    private fun keyDown(key: Key): KeyEvent = KeyEvent(
        key = key,
        type = KeyEventType.KeyDown,
    )
}
