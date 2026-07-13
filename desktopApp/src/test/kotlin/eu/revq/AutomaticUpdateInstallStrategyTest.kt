package eu.revq

import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class AutomaticUpdateInstallStrategyTest {
    @Test
    fun `Linux helper waits installs the verified deb and relaunches RevQ`() {
        val packagePath = Path.of("/tmp/revq-0.2.0-linux-x86_64.deb")
        val statusPath = Path.of("/tmp/revq-update-result")

        val plan = planAutomaticUpdateInstall(
            request(
                platform = UpdatePlatform.LinuxX64,
                path = packagePath,
                restartCommand = listOf("/opt/revq/bin/RevQ", "--restored"),
            ),
            statusPath,
        )

        assertTrue(plan.script.contains("kill -0 \"\$pid\""))
        assertTrue(plan.script.contains("/usr/bin/pkexec /usr/bin/dpkg --install \"\$package\""))
        assertTrue(plan.script.contains("nohup \"\$@\""))
        assertEquals(
            listOf("42", packagePath.toString(), statusPath.toString(), "/opt/revq/bin/RevQ", "--restored"),
            plan.arguments,
        )
    }

    @Test
    fun `macOS helper mounts the dmg replaces the current app and reopens it`() {
        val dmgPath = Path.of("/tmp/revq-0.2.0-macos-arm64.dmg")
        val statusPath = Path.of("/tmp/revq-update-result")

        val plan = planAutomaticUpdateInstall(
            request(
                platform = UpdatePlatform.MacArm64,
                path = dmgPath,
                restartCommand = listOf("/Applications/RevQ.app/Contents/MacOS/RevQ"),
            ),
            statusPath,
        )

        assertTrue(plan.script.contains("/usr/bin/hdiutil attach"))
        assertTrue(plan.script.contains("with administrator privileges"))
        assertTrue(plan.script.contains("/usr/bin/open \"\$target_app\""))
        assertEquals(
            listOf("42", dmgPath.toString(), statusPath.toString(), "/Applications/RevQ.app"),
            plan.arguments,
        )
    }

    @Test
    fun `automatic installation rejects platforms without an implemented package strategy`() {
        assertFailsWith<IllegalArgumentException> {
            planAutomaticUpdateInstall(
                request(
                    platform = UpdatePlatform.WindowsX64,
                    path = Path.of("/tmp/revq.exe"),
                    restartCommand = listOf("C:\\Program Files\\RevQ\\RevQ.exe"),
                ),
                Path.of("/tmp/revq-update-result"),
            )
        }
    }

    private fun request(
        platform: UpdatePlatform,
        path: Path,
        restartCommand: List<String>,
    ): UpdateInstallRequest = UpdateInstallRequest(
        release = ReleaseInfo(
            version = AppVersion.parse("0.2.0"),
            title = "RevQ 0.2.0",
            notes = "",
            publishedAt = null,
            assets = emptyList(),
        ),
        asset = ReleaseAsset(path.fileName.toString(), "https://example.invalid/${path.fileName}"),
        path = path,
        platform = platform,
        launchContext = UpdateLaunchContext(
            processId = 42,
            restartCommand = restartCommand,
        ),
    )
}
