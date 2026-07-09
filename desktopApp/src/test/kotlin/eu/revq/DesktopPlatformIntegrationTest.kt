package eu.revq

import java.awt.image.BufferedImage
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class DesktopPlatformIntegrationTest {
    @Test
    fun trayGlyphContrastsWithDesktopAppearance() {
        assertEquals("icon-tray-light.png", trayIconResourceName(darkAppearance = true))
        assertEquals("icon-tray-dark.png", trayIconResourceName(darkAppearance = false))
    }

    @Test
    fun cosmicWaylandPrefersStatusNotifierTray() {
        assertEquals(
            TrayBackend.StatusNotifier,
            selectTrayBackend(
                desktop = "COSMIC",
                sessionType = "wayland",
                statusNotifierAvailable = true,
                awtTraySupported = false,
            ),
        )
    }

    @Test
    fun legacyDesktopUsesAwtTrayWhenAvailable() {
        assertEquals(
            TrayBackend.Awt,
            selectTrayBackend(
                desktop = "XFCE",
                sessionType = "x11",
                statusNotifierAvailable = false,
                awtTraySupported = true,
            ),
        )
    }

    @Test
    fun statusNotifierIconUsesNetworkOrderArgbPixels() {
        val image = BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).apply {
            setRGB(0, 0, 0x7F123456)
        }

        assertContentEquals(
            byteArrayOf(0x7F, 0x12, 0x34, 0x56),
            toStatusNotifierArgb(image),
        )
    }
}
