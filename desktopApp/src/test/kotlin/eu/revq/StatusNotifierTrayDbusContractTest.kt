package eu.revq

import java.lang.reflect.Proxy
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.freedesktop.dbus.messages.ExportedObject

class StatusNotifierTrayDbusContractTest {
    @Test
    fun newIconCanBeConstructed() {
        assertEquals("NewIcon", StatusNotifierItem.NewIcon(STATUS_NOTIFIER_ITEM_TEST_PATH).name)
    }

    @Test
    fun newToolTipCanBeConstructed() {
        assertEquals("NewToolTip", StatusNotifierItem.NewToolTip(STATUS_NOTIFIER_ITEM_TEST_PATH).name)
    }

    @Test
    fun statusNotifierItemIntrospectsZeroArgumentSignals() {
        val introspection = ExportedObject(statusNotifierItemProxy(), false).introspectiondata

        assertTrue(ZERO_ARGUMENT_NEW_ICON.containsMatchIn(introspection), introspection)
        assertTrue(ZERO_ARGUMENT_NEW_TOOLTIP.containsMatchIn(introspection), introspection)
    }

    @Test
    fun genericPixmapPropertiesUseStatusNotifierSignatures() {
        val introspection = ExportedObject(statusNotifierItemProxy(), false).introspectiondata
        val actualSignatures = Regex(
            """<property name="(IconPixmap|OverlayIconPixmap|AttentionIconPixmap|ToolTip)" type="([^"]+)""",
        ).findAll(introspection).associate { match ->
            match.groupValues[1] to match.groupValues[2]
        }

        assertEquals(
            mapOf(
                "IconPixmap" to "a(iiay)",
                "OverlayIconPixmap" to "a(iiay)",
                "AttentionIconPixmap" to "a(iiay)",
                "ToolTip" to "(sa(iiay)ss)",
            ),
            actualSignatures,
            introspection,
        )
    }

    @Test
    fun productionItemExposesGenericIconPixmaps() {
        val item: StatusNotifierItem = createStatusNotifierItem(
            reviewCount = 0,
            badgeEnabled = false,
            activate = {},
        )

        assertPixmapPayload(item.getIconPixmap())
        assertPixmapPayload(item.getToolTip().iconPixmap)
        assertTrue(item.getOverlayIconPixmap().isEmpty())
        assertTrue(item.getAttentionIconPixmap().isEmpty())
    }

    @Test
    fun disabledBadgeMasksPositiveCountFromInitialIconPixmaps() {
        val zeroCountItem = createStatusNotifierItem(
            reviewCount = 0,
            badgeEnabled = false,
            activate = {},
        )
        val positiveCountItem = createStatusNotifierItem(
            reviewCount = 3,
            badgeEnabled = false,
            activate = {},
        )

        assertEquals("RevQ", positiveCountItem.getTitle())
        assertEquals("RevQ", positiveCountItem.getToolTip().title)
        assertEquals("RevQ · Pull request review queue", positiveCountItem.getToolTip().description)
        zeroCountItem.getIconPixmap().zip(positiveCountItem.getIconPixmap()).forEach { (expected, actual) ->
            assertEquals(expected.width, actual.width)
            assertEquals(expected.height, actual.height)
            assertContentEquals(expected.pixels, actual.pixels)
        }
    }

    private fun assertPixmapPayload(pixmaps: List<StatusNotifierPixmap>) {
        assertTrue(pixmaps.isNotEmpty())
        assertEquals(listOf(22, 32, 44), pixmaps.map(StatusNotifierPixmap::width))
        assertEquals(listOf(22, 32, 44), pixmaps.map(StatusNotifierPixmap::height))
        pixmaps.forEach { pixmap ->
            assertEquals(pixmap.width * pixmap.height * 4, pixmap.pixels.size)
        }
    }

    private fun statusNotifierItemProxy(): StatusNotifierItem = Proxy.newProxyInstance(
        StatusNotifierItem::class.java.classLoader,
        arrayOf(StatusNotifierItem::class.java),
    ) { _, method, _ ->
        when (method.name) {
            "getObjectPath" -> STATUS_NOTIFIER_ITEM_TEST_PATH
            else -> error("Unexpected invocation during introspection: ${method.name}")
        }
    } as StatusNotifierItem

    private companion object {
        const val STATUS_NOTIFIER_ITEM_TEST_PATH = "/StatusNotifierItem"
        val ZERO_ARGUMENT_NEW_ICON = Regex("<signal name=\"NewIcon\">\\s*</signal>")
        val ZERO_ARGUMENT_NEW_TOOLTIP = Regex("<signal name=\"NewToolTip\">\\s*</signal>")
    }
}
