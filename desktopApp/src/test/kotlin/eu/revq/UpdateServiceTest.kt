package eu.revq

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking
import java.time.Instant
import java.nio.file.Files
import kotlin.io.path.createTempDirectory

class UpdateServiceTest {
    @Test
    fun `failed helper installation is reported after RevQ restarts`() = runBlocking {
        val directory = createTempDirectory("revq-update-result-test")
        Files.writeString(directory.resolve("install-result"), "failure\n")

        val service = UpdateService(
            installedVersion = AppVersion.parse("0.1.0"),
            releaseSource = ReleaseSource { emptyList() },
            preferences = InMemoryUpdatePreferencesStore(),
            downloadDirectory = directory,
        )

        val failure = service.state as UpdateState.Failed
        assertTrue(failure.message.contains("not be installed"))
        assertEquals(false, Files.exists(directory.resolve("install-result")))

        service.checkNow()

        assertEquals(failure, service.state)
    }

    @Test
    fun `semantic versions order stable releases and supported prereleases`() {
        val stable = AppVersion.parse("v0.2.0")

        assertTrue(stable > AppVersion.parse("0.2.0-rc.2"))
        assertTrue(AppVersion.parse("0.2.0-rc.2") > AppVersion.parse("0.2.0-beta.4"))
        assertTrue(AppVersion.parse("0.2.0-beta.4") > AppVersion.parse("0.2.0-alpha.9"))
        assertTrue(stable > AppVersion.parse("0.1.9"))
        assertEquals("0.2.0", stable.toString())
    }

    @Test
    fun `update discovery offers only a newer stable well formed release`() = runBlocking {
        val service = UpdateService(
            installedVersion = AppVersion.parse("0.1.0"),
            releaseSource = ReleaseSource {
                listOf(
                    ReleaseCandidate("v0.4.0", "Draft", draft = true),
                    ReleaseCandidate("v0.3.0-beta.1", "Beta", prerelease = true),
                    ReleaseCandidate("nightly", "Malformed"),
                    ReleaseCandidate(
                        tag = "v0.2.0",
                        title = "RevQ 0.2.0",
                        notes = "Queue improvements",
                        publishedAt = Instant.parse("2026-07-01T08:00:00Z"),
                    ),
                )
            },
            preferences = InMemoryUpdatePreferencesStore(),
        )

        service.checkNow()

        val available = service.state as UpdateState.Available
        assertEquals(AppVersion.parse("0.2.0"), available.release.version)
        assertEquals("Queue improvements", available.release.notes)
    }

    @Test
    fun `dismissal hides only the matching release and a later release is visible`() = runBlocking {
        var remote = "v0.2.0"
        val preferences = InMemoryUpdatePreferencesStore()
        val service = UpdateService(
            installedVersion = AppVersion.parse("0.1.0"),
            releaseSource = ReleaseSource { listOf(ReleaseCandidate(remote, remote)) },
            preferences = preferences,
        )

        service.checkNow()
        service.dismissAvailableUpdate()
        assertTrue((service.state as UpdateState.Available).dismissed)

        remote = "v0.2.1"
        service.checkNow()

        assertEquals(false, (service.state as UpdateState.Available).dismissed)
        assertEquals("0.2.0", preferences.load().dismissedVersion)
    }

    @Test
    fun `one install action verifies the download launches the updater and exits RevQ`() = runBlocking {
        val directory = createTempDirectory("revq-update-test")
        val packageAsset = ReleaseAsset("revq-0.2.0-linux-x86_64.deb", "package")
        val checksumAsset = ReleaseAsset("SHA256SUMS", "checksums")
        var installRequest: UpdateInstallRequest? = null
        var exitRequests = 0
        val downloader = object : UpdateDownloadGateway {
            override suspend fun download(
                asset: ReleaseAsset,
                destination: java.nio.file.Path,
                onProgress: (Long, Long?) -> Unit,
            ): java.nio.file.Path {
                Files.writeString(destination, "revq package")
                onProgress(12, 12)
                return destination
            }

            override suspend fun downloadText(asset: ReleaseAsset): String =
                "fde5e1801974d4661a49a941c6f8c460d6a99d340fc2826985f51fb0b76de187  ${packageAsset.name}"

            override fun cancel() = Unit
        }
        val service = UpdateService(
            installedVersion = AppVersion.parse("0.1.0"),
            releaseSource = ReleaseSource {
                listOf(ReleaseCandidate("v0.2.0", "RevQ", assets = listOf(packageAsset, checksumAsset)))
            },
            preferences = InMemoryUpdatePreferencesStore(),
            platform = UpdatePlatform.LinuxX64,
            downloader = downloader,
            downloadDirectory = directory,
            installer = UpdateInstallStrategy { installRequest = it },
            applicationLifecycle = ApplicationLifecycle { exitRequests += 1 },
            launchContext = UpdateLaunchContext(
                processId = 42L,
                restartCommand = listOf("/opt/revq/bin/RevQ"),
            ),
        )

        service.checkNow()
        service.downloadAndInstallUpdate()

        assertEquals(packageAsset, installRequest?.asset)
        assertEquals(packageAsset.name, installRequest?.path?.fileName.toString())
        assertEquals(UpdatePlatform.LinuxX64, installRequest?.platform)
        assertEquals(42L, installRequest?.launchContext?.processId)
        assertEquals(1, exitRequests)
        assertTrue(service.state is UpdateState.Restarting)
    }
}
