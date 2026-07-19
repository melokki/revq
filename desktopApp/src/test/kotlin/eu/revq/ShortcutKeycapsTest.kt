package eu.revq

import kotlin.test.Test
import kotlin.test.assertEquals

class ShortcutKeycapsTest {
    @Test
    fun `renders alternative keys as separate keycaps`() {
        val presentation = shortcutKeycapPresentation("Esc / d", osName = "Linux")

        assertEquals(
            listOf(listOf("Esc"), listOf("D")),
            presentation.alternatives.map { it.keys },
        )
        assertEquals("Escape or D", presentation.accessibilityLabel)
    }

    @Test
    fun `renders primary modifier as command on macOS`() {
        val presentation = shortcutKeycapPresentation("Ctrl+S", osName = "Mac OS X")

        assertEquals(listOf(listOf("⌘", "S")), presentation.alternatives.map { it.keys })
        assertEquals("Command plus S", presentation.accessibilityLabel)
    }

    @Test
    fun `keeps page up and page down as complete alternatives`() {
        val presentation = shortcutKeycapPresentation("Page Up / Down", osName = "Linux")

        assertEquals(
            listOf(listOf("Page Up"), listOf("Page Down")),
            presentation.alternatives.map { it.keys },
        )
    }

    @Test
    fun `renders modifier alternatives as complete chords`() {
        val presentation = shortcutKeycapPresentation("Ctrl+N / Ctrl+P", osName = "Linux")

        assertEquals(
            listOf(listOf("Ctrl", "N"), listOf("Ctrl", "P")),
            presentation.alternatives.map { it.keys },
        )
    }

    @Test
    fun `splits arrow movement into two keycaps`() {
        val presentation = shortcutKeycapPresentation("↑↓", osName = "Linux")

        assertEquals(listOf(listOf("↑"), listOf("↓")), presentation.alternatives.map { it.keys })
        assertEquals("Up arrow or Down arrow", presentation.accessibilityLabel)
    }
}
