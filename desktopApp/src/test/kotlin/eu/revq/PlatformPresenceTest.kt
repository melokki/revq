package eu.revq

import kotlin.test.Test
import kotlin.test.assertEquals

class PlatformPresenceTest {
    @Test
    fun cosmicWaylandPresencePrefersStatusNotifierAndAppliesJavaUiScale() {
        val plan = PlatformPresence.plan(
            PlatformPresenceEnvironment(
                desktop = "COSMIC",
                sessionType = "wayland",
                cosmicScale = 2f,
                statusNotifierAvailable = true,
                awtTraySupported = true,
                darkAppearance = true,
            ),
        )

        assertEquals("2.0", plan.javaUiScale)
        assertEquals(TrayBackend.StatusNotifier, plan.trayBackend)
        assertEquals("icon-tray-light.png", plan.trayIconResourceName)
    }
}
