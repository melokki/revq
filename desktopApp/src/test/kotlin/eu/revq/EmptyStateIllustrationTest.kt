package eu.revq

import kotlin.test.Test
import kotlin.test.assertEquals

class EmptyStateIllustrationTest {
    @Test
    fun everyEmptyStateUsesTheSharedFixedIllustrationFrame() {
        assertEquals(344f, EmptyStateIllustrationMetrics.Width.value)
        assertEquals(236f, EmptyStateIllustrationMetrics.Height.value)
        assertEquals(94f, EmptyStateIllustrationMetrics.NoteCardHeight.value)
        assertEquals(184f, EmptyStateIllustrationMetrics.PrimaryCardHeight.value)
        assertEquals(40f, EmptyStateIllustrationMetrics.ChipHeight.value)
    }
}
