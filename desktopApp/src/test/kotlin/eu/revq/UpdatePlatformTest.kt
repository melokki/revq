package eu.revq

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class UpdatePlatformTest {
    @Test
    fun `asset selection is exact for supported platform and never falls back`() {
        val assets = listOf(
            ReleaseAsset("revq-0.2.0-linux-x86_64.deb", "linux"),
            ReleaseAsset("revq-0.2.0-macos-arm64.dmg", "mac"),
            ReleaseAsset("revq-0.2.0-windows-x86_64.exe", "windows"),
        )

        assertEquals(
            "revq-0.2.0-linux-x86_64.deb",
            selectUpdateAsset(AppVersion.parse("0.2.0"), UpdatePlatform.LinuxX64, assets)?.name,
        )
        assertEquals(
            "revq-0.2.0-macos-arm64.dmg",
            selectUpdateAsset(AppVersion.parse("0.2.0"), UpdatePlatform.MacArm64, assets)?.name,
        )
        assertNull(selectUpdateAsset(AppVersion.parse("0.2.0"), UpdatePlatform.Unsupported, assets))
    }
}
