package eu.revq

import kotlin.test.Test
import kotlin.test.assertEquals

class DesktopFocusLifecycleTest {
    @Test
    fun visibleReadyWindowOwnsNativeAndComposeFocusSequence() {
        val adapter = DeterministicDesktopFocusAdapter()
        val lifecycle = DesktopFocusLifecycle(adapter)

        lifecycle.attach()
        lifecycle.apply(DesktopFocusEvent.VisibilityChanged(visible = false))
        lifecycle.apply(DesktopFocusEvent.ContentReady)
        assertEquals(emptyList(), adapter.operations)

        lifecycle.apply(DesktopFocusEvent.VisibilityChanged(visible = true))
        lifecycle.apply(DesktopFocusEvent.ContentReady)
        adapter.activateWindow()
        lifecycle.apply(DesktopFocusEvent.HotReload)

        assertEquals(
            listOf(
                DesktopFocusOperation.BringToFront,
                DesktopFocusOperation.RequestNativeFocus,
                DesktopFocusOperation.RequestComposeRootFocus,
                DesktopFocusOperation.RequestComposeRootFocus,
                DesktopFocusOperation.RequestComposeRootFocus,
                DesktopFocusOperation.RequestComposeRootFocus,
                DesktopFocusOperation.BringToFront,
                DesktopFocusOperation.RequestNativeFocus,
                DesktopFocusOperation.RequestComposeRootFocus,
                DesktopFocusOperation.RequestComposeRootFocus,
            ),
            adapter.operations,
        )
        assertEquals(6, lifecycle.snapshot.composeFocusRequests)
        assertEquals(
            listOf(75L, 50L, 175L, 50L, 175L, 75L, 50L, 175L),
            adapter.scheduledDelays,
        )

        lifecycle.close()
        assertEquals(false, adapter.listenerInstalled)
    }

    @Test
    fun hidingOrClosingCancelsScheduledFocusWork() {
        val adapter = DeterministicDesktopFocusAdapter(executeScheduledImmediately = false)
        val lifecycle = DesktopFocusLifecycle(adapter)

        lifecycle.attach()
        lifecycle.apply(DesktopFocusEvent.ContentReady)
        lifecycle.apply(DesktopFocusEvent.VisibilityChanged(visible = false))
        adapter.runScheduledWork()

        assertEquals(emptyList(), adapter.operations)

        lifecycle.apply(DesktopFocusEvent.VisibilityChanged(visible = true))
        lifecycle.close()
        adapter.runScheduledWork()

        assertEquals(emptyList(), adapter.operations)
    }
}
