package eu.revq

data class PlatformPresenceEnvironment(
    val desktop: String?,
    val sessionType: String?,
    val cosmicScale: Float?,
    val statusNotifierAvailable: Boolean,
    val awtTraySupported: Boolean,
    val darkAppearance: Boolean,
)

data class PlatformPresencePlan(
    val javaUiScale: String?,
    val trayBackend: TrayBackend,
    val trayIconResourceName: String,
)

object PlatformPresence {
    fun plan(environment: PlatformPresenceEnvironment): PlatformPresencePlan =
        PlatformPresencePlan(
            javaUiScale = recommendedJavaUiScale(
                desktop = environment.desktop,
                sessionType = environment.sessionType,
                cosmicScale = environment.cosmicScale,
            ),
            trayBackend = selectTrayBackend(
                desktop = environment.desktop,
                sessionType = environment.sessionType,
                statusNotifierAvailable = environment.statusNotifierAvailable,
                awtTraySupported = environment.awtTraySupported,
            ),
            trayIconResourceName = trayIconResourceName(environment.darkAppearance),
        )

    fun recommendedJavaUiScale(
        desktop: String?,
        sessionType: String?,
        cosmicScale: Float?,
    ): String? {
        if (!desktop.orEmpty().contains("COSMIC", ignoreCase = true)) return null
        if (!sessionType.orEmpty().equals("wayland", ignoreCase = true)) return null

        val scale = cosmicScale
            ?.takeIf { it > 1.05f }
            ?.coerceIn(1f, 4f)
            ?: return null

        return scale.toString()
    }

    fun selectTrayBackend(
        desktop: String?,
        sessionType: String?,
        statusNotifierAvailable: Boolean,
        awtTraySupported: Boolean,
    ): TrayBackend = when {
        desktop.orEmpty().contains("COSMIC", ignoreCase = true) &&
            sessionType.equals("wayland", ignoreCase = true) &&
            statusNotifierAvailable -> TrayBackend.StatusNotifier
        awtTraySupported -> TrayBackend.Awt
        else -> TrayBackend.None
    }

    fun trayIconResourceName(darkAppearance: Boolean): String =
        if (darkAppearance) "icon-tray-light.png" else "icon-tray-dark.png"
}
