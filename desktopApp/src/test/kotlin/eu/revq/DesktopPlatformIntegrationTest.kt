package eu.revq

import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DesktopPlatformIntegrationTest {
    @Test
    fun trayGlyphContrastsWithDesktopAppearance() {
        assertEquals("icon-tray-light.png", trayIconResourceName(darkAppearance = true))
        assertEquals("icon-tray-dark.png", trayIconResourceName(darkAppearance = false))
    }

    @Test
    fun packagedTrayGlyphsHaveRealTransparencyInsteadOfABakedBackground() {
        listOf("icon-tray-light.png", "icon-tray-dark.png").forEach { resourceName ->
            val resource = checkNotNull(javaClass.classLoader.getResource(resourceName))
            val image = ImageIO.read(resource)

            assertTrue(image.colorModel.hasAlpha(), "$resourceName must contain an alpha channel")
            assertEquals(0, image.getRGB(0, 0) ushr 24, "$resourceName must have transparent corners")
            assertTrue(
                (0 until image.height).any { y ->
                    (0 until image.width).any { x -> image.getRGB(x, y) ushr 24 > 0 }
                },
                "$resourceName must contain a visible glyph",
            )
        }
    }

    @Test
    fun lightTrayGlyphIsDerivedFromTheDarkGlyphsExactSilhouette() {
        val dark = loadResourceImage("icon-tray-dark.png")
        val light = loadResourceImage("icon-tray-light.png")

        assertEquals(dark.width, light.width)
        assertEquals(dark.height, light.height)

        var darkLuminance = 0L
        var lightLuminance = 0L
        var visiblePixels = 0L
        for (y in 0 until dark.height) {
            for (x in 0 until dark.width) {
                val darkPixel = dark.getRGB(x, y)
                val lightPixel = light.getRGB(x, y)
                assertEquals(
                    darkPixel ushr 24,
                    lightPixel ushr 24,
                    "Tray variants must share one alpha mask at ($x,$y)",
                )
                if (darkPixel ushr 24 > 0) {
                    darkLuminance += rgbLuminance(darkPixel)
                    lightLuminance += rgbLuminance(lightPixel)
                    visiblePixels += 1
                }
            }
        }

        assertTrue(visiblePixels > 0)
        assertTrue(darkLuminance / visiblePixels < 64, "Dark tray glyph must remain dark")
        assertTrue(lightLuminance / visiblePixels > 191, "Light tray glyph must remain light")
    }

    @Test
    fun cosmicStatusNotifierPixmapsStayVisibleAtEveryPublishedSize() {
        listOf(22, 32, 44).forEach { size ->
            val onDarkPanel = loadTrayBufferedImage(size, darkAppearance = true)
            val onLightPanel = loadTrayBufferedImage(size, darkAppearance = false)

            assertEquals(size, onDarkPanel.width)
            assertEquals(size, onDarkPanel.height)
            assertVisibleTrayPixmap(onDarkPanel, expectLightGlyph = true)
            assertVisibleTrayPixmap(onLightPanel, expectLightGlyph = false)
        }
    }

    @Test
    fun packagedAppIconRetainsAHighResolutionSource() {
        val resource = checkNotNull(javaClass.classLoader.getResource("icon-app.png"))
        val image = ImageIO.read(resource)

        assertTrue(image.width >= 1024, "icon-app.png must be at least 1024px wide")
        assertTrue(image.height >= 1024, "icon-app.png must be at least 1024px tall")
    }

    @Test
    fun packagedAppArtworkHasATransparentOuterCanvas() {
        val resourceName = "icon-app.png"
        val image = loadResourceImage(resourceName)

        assertTrue(image.colorModel.hasAlpha(), "$resourceName must contain alpha")
        listOf(
            0 to 0,
            image.lastX to 0,
            0 to image.lastY,
            image.lastX to image.lastY,
        ).forEach { (x, y) ->
            assertEquals(0, image.getRGB(x, y) ushr 24, "$resourceName corner ($x,$y)")
        }
        assertTrue(
            image.getRGB(image.width / 2, image.height / 2) ushr 24 >= 250,
            "$resourceName must preserve opaque icon content",
        )
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

    private fun loadResourceImage(resourceName: String): BufferedImage {
        val resource = checkNotNull(javaClass.classLoader.getResource(resourceName))
        return ImageIO.read(resource)
    }

    private val BufferedImage.lastX: Int get() = width - 1
    private val BufferedImage.lastY: Int get() = height - 1

    private fun rgbLuminance(argb: Int): Long {
        val red = argb ushr 16 and 0xFF
        val green = argb ushr 8 and 0xFF
        val blue = argb and 0xFF
        return ((red * 2126L) + (green * 7152L) + (blue * 722L)) / 10_000L
    }

    private fun assertVisibleTrayPixmap(
        image: BufferedImage,
        expectLightGlyph: Boolean,
    ) {
        val visible = buildList {
            for (y in 0 until image.height) {
                for (x in 0 until image.width) {
                    val pixel = image.getRGB(x, y)
                    if (pixel ushr 24 >= 128) add(pixel)
                }
            }
        }
        val coverage = visible.size.toDouble() / (image.width * image.height)
        assertTrue(coverage in 0.20..0.75, "Tray glyph coverage was $coverage")
        val averageLuminance = visible.sumOf(::rgbLuminance) / visible.size
        if (expectLightGlyph) {
            assertTrue(averageLuminance > 191, "Dark panels need a light tray glyph")
        } else {
            assertTrue(averageLuminance < 64, "Light panels need a dark tray glyph")
        }
    }
}
