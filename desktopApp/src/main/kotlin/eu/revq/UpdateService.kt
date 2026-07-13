package eu.revq

import java.time.Instant
import java.time.LocalTime
import java.time.ZonedDateTime
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.security.MessageDigest
import kotlin.io.path.deleteIfExists

data class AppVersion(
    val major: Int,
    val minor: Int,
    val patch: Int,
    val prerelease: Prerelease? = null,
) : Comparable<AppVersion> {
    data class Prerelease(
        val channel: Channel,
        val number: Int,
    )

    enum class Channel(val precedence: Int) {
        Alpha(0),
        Beta(1),
        ReleaseCandidate(2),
    }

    override fun compareTo(other: AppVersion): Int =
        compareValuesBy(this, other, AppVersion::major, AppVersion::minor, AppVersion::patch)
            .takeIf { it != 0 }
            ?: when {
                prerelease == null && other.prerelease == null -> 0
                prerelease == null -> 1
                other.prerelease == null -> -1
                else -> compareValuesBy(
                    prerelease,
                    other.prerelease,
                    { it.channel.precedence },
                    Prerelease::number,
                )
            }

    override fun toString(): String = buildString {
        append("$major.$minor.$patch")
        prerelease?.let {
            val label = when (it.channel) {
                Channel.Alpha -> "alpha"
                Channel.Beta -> "beta"
                Channel.ReleaseCandidate -> "rc"
            }
            append("-$label.${it.number}")
        }
    }

    companion object {
        private val pattern = Regex(
            "^v?(\\d+)\\.(\\d+)\\.(\\d+)(?:-(alpha|beta|rc)\\.(\\d+))?$",
        )

        fun parse(value: String): AppVersion {
            val match = pattern.matchEntire(value.trim())
                ?: throw IllegalArgumentException("Malformed application version: $value")
            val channel = when (match.groupValues[4]) {
                "alpha" -> Channel.Alpha
                "beta" -> Channel.Beta
                "rc" -> Channel.ReleaseCandidate
                else -> null
            }
            return AppVersion(
                major = match.groupValues[1].toInt(),
                minor = match.groupValues[2].toInt(),
                patch = match.groupValues[3].toInt(),
                prerelease = channel?.let { Prerelease(it, match.groupValues[5].toInt()) },
            )
        }

        fun parseOrNull(value: String): AppVersion? = runCatching { parse(value) }.getOrNull()
    }
}

data class ReleaseAsset(
    val name: String,
    val downloadUrl: String,
    val sizeBytes: Long? = null,
)

enum class UpdatePlatform(
    val assetSuffix: String?,
) {
    LinuxX64("linux-x86_64.deb"),
    MacArm64("macos-arm64.dmg"),
    MacX64("macos-x86_64.dmg"),
    WindowsX64("windows-x86_64.exe"),
    Unsupported(null),
}

fun detectUpdatePlatform(
    osName: String = System.getProperty("os.name").orEmpty(),
    architecture: String = System.getProperty("os.arch").orEmpty(),
): UpdatePlatform {
    val os = osName.lowercase()
    val arch = architecture.lowercase()
    val x64 = arch in setOf("x86_64", "amd64", "x64")
    val arm64 = arch in setOf("aarch64", "arm64")
    return when {
        ("linux" in os || "nix" in os || "nux" in os) && x64 -> UpdatePlatform.LinuxX64
        ("mac" in os || "darwin" in os) && arm64 -> UpdatePlatform.MacArm64
        ("mac" in os || "darwin" in os) && x64 -> UpdatePlatform.MacX64
        "win" in os && x64 -> UpdatePlatform.WindowsX64
        else -> UpdatePlatform.Unsupported
    }
}

fun selectUpdateAsset(
    version: AppVersion,
    platform: UpdatePlatform,
    assets: List<ReleaseAsset>,
): ReleaseAsset? {
    val suffix = platform.assetSuffix ?: return null
    val expected = "revq-$version-$suffix"
    return assets.singleOrNull { it.name == expected }
}

data class ReleaseCandidate(
    val tag: String,
    val title: String,
    val notes: String = "",
    val publishedAt: Instant? = null,
    val assets: List<ReleaseAsset> = emptyList(),
    val draft: Boolean = false,
    val prerelease: Boolean = false,
)

data class ReleaseInfo(
    val version: AppVersion,
    val title: String,
    val notes: String,
    val publishedAt: Instant?,
    val assets: List<ReleaseAsset>,
)

fun interface ReleaseSource {
    suspend fun loadReleases(): List<ReleaseCandidate>
}

interface UpdateDownloadGateway {
    suspend fun download(
        asset: ReleaseAsset,
        destination: Path,
        onProgress: (downloadedBytes: Long, totalBytes: Long?) -> Unit,
    ): Path

    suspend fun downloadText(asset: ReleaseAsset): String
    fun cancel()
}

fun interface UpdateInstallStrategy {
    fun launch(request: UpdateInstallRequest)
}

data class UpdateLaunchContext(
    val processId: Long,
    val restartCommand: List<String>,
)

data class UpdateInstallRequest(
    val release: ReleaseInfo,
    val asset: ReleaseAsset,
    val path: Path,
    val platform: UpdatePlatform,
    val launchContext: UpdateLaunchContext,
)

fun interface ApplicationLifecycle {
    fun exitForUpdate()
}

private fun currentUpdateLaunchContext(): UpdateLaunchContext {
    val process = ProcessHandle.current()
    val info = process.info()
    val command = info.command().orElse(null)
    val arguments = info.arguments().orElse(emptyArray())
    return UpdateLaunchContext(
        processId = process.pid(),
        restartCommand = listOfNotNull(command) + arguments,
    )
}

data class UpdatePreferences(
    val lastSuccessfulCheck: Instant? = null,
    val latestKnownVersion: String? = null,
    val dismissedVersion: String? = null,
    val automaticChecksEnabled: Boolean = true,
    val lastError: String? = null,
)

interface UpdatePreferencesStore {
    fun load(): UpdatePreferences
    fun save(preferences: UpdatePreferences)
}

class InMemoryUpdatePreferencesStore(
    initial: UpdatePreferences = UpdatePreferences(),
) : UpdatePreferencesStore {
    private var preferences = initial

    override fun load(): UpdatePreferences = preferences

    override fun save(preferences: UpdatePreferences) {
        this.preferences = preferences
    }
}

class SettingsUpdatePreferencesStore(
    private val settingsStore: SettingsStore,
) : UpdatePreferencesStore {
    override fun load(): UpdatePreferences {
        val settings = settingsStore.load()
        return UpdatePreferences(
            lastSuccessfulCheck = settings.lastUpdateCheck
                .takeIf(String::isNotBlank)
                ?.let { runCatching { Instant.parse(it) }.getOrNull() },
            latestKnownVersion = settings.latestKnownUpdateVersion.ifBlank { null },
            dismissedVersion = settings.dismissedUpdateVersion.ifBlank { null },
            automaticChecksEnabled = settings.automaticUpdateChecksEnabled,
            lastError = settings.lastUpdateError.ifBlank { null },
        )
    }

    override fun save(preferences: UpdatePreferences) {
        settingsStore.save(
            settingsStore.load().copy(
                lastUpdateCheck = preferences.lastSuccessfulCheck?.toString().orEmpty(),
                latestKnownUpdateVersion = preferences.latestKnownVersion.orEmpty(),
                dismissedUpdateVersion = preferences.dismissedVersion.orEmpty(),
                automaticUpdateChecksEnabled = preferences.automaticChecksEnabled,
                lastUpdateError = preferences.lastError.orEmpty(),
            ),
        )
    }
}

sealed interface UpdateState {
    data object Idle : UpdateState
    data object Checking : UpdateState
    data class Current(val latestRelease: ReleaseInfo?) : UpdateState
    data class Available(
        val release: ReleaseInfo,
        val dismissed: Boolean = false,
    ) : UpdateState
    data class Downloading(
        val release: ReleaseInfo,
        val progress: Float?,
    ) : UpdateState
    data class Verifying(val release: ReleaseInfo) : UpdateState
    data class ReadyToInstall(
        val release: ReleaseInfo,
        val asset: ReleaseAsset,
        val path: Path,
    ) : UpdateState
    data class Installing(val release: ReleaseInfo) : UpdateState
    data class Restarting(val release: ReleaseInfo) : UpdateState
    data class Failed(
        val message: String,
        val release: ReleaseInfo? = null,
        val visible: Boolean = release != null,
    ) : UpdateState
}

class UpdateService(
    val installedVersion: AppVersion,
    private val releaseSource: ReleaseSource,
    private val preferences: UpdatePreferencesStore,
    private val now: () -> Instant = Instant::now,
    private val platform: UpdatePlatform = detectUpdatePlatform(),
    private val downloader: UpdateDownloadGateway? = null,
    private val downloadDirectory: Path = Paths.get(System.getProperty("java.io.tmpdir"), "revq-updates"),
    private val installer: UpdateInstallStrategy = AutomaticUpdateInstallStrategy(
        downloadDirectory.resolve("install-result"),
    ),
    private val applicationLifecycle: ApplicationLifecycle = ApplicationLifecycle {},
    private val launchContext: UpdateLaunchContext = currentUpdateLaunchContext(),
) {
    private var stateListener: ((UpdateState) -> Unit)? = null
    @Volatile
    private var downloadInProgress = false
    @Volatile
    private var cancellationRequested = false

    var state: UpdateState = previousInstallationState()
        private set(value) {
            field = value
            stateListener?.invoke(value)
        }

    fun observeState(listener: (UpdateState) -> Unit) {
        stateListener = listener
        listener(state)
    }

    suspend fun checkNow() {
        val pendingInstallationFailure = state as? UpdateState.Failed
        if (pendingInstallationFailure?.visible == true && pendingInstallationFailure.release == null) return
        state = UpdateState.Checking
        try {
            val release = releaseSource.loadReleases()
                .asSequence()
                .filterNot { it.draft || it.prerelease }
                .mapNotNull { candidate ->
                    AppVersion.parseOrNull(candidate.tag)?.let { version ->
                        ReleaseInfo(
                            version = version,
                            title = candidate.title,
                            notes = candidate.notes,
                            publishedAt = candidate.publishedAt,
                            assets = candidate.assets,
                        )
                    }
                }
                .filter { it.version.prerelease == null }
                .maxByOrNull { it.version }
            val saved = preferences.load()
            preferences.save(
                saved.copy(
                    lastSuccessfulCheck = now(),
                    latestKnownVersion = release?.version?.toString(),
                    lastError = null,
                ),
            )
            state = if (release != null && release.version > installedVersion) {
                UpdateState.Available(
                    release = release,
                    dismissed = saved.dismissedVersion == release.version.toString(),
                )
            } else {
                UpdateState.Current(release)
            }
        } catch (_: Throwable) {
            val message = "Update check failed."
            preferences.save(preferences.load().copy(lastError = message))
            state = UpdateState.Failed(message)
        }
    }

    fun dismissAvailableUpdate() {
        val available = state as? UpdateState.Available ?: return
        preferences.save(
            preferences.load().copy(dismissedVersion = available.release.version.toString()),
        )
        state = available.copy(dismissed = true)
    }

    private suspend fun downloadUpdate() {
        if (downloadInProgress) return
        val available = state as? UpdateState.Available ?: return
        downloadInProgress = true
        cancellationRequested = false
        val release = available.release
        val asset = selectUpdateAsset(release.version, platform, release.assets)
        if (asset == null) {
            state = UpdateState.Failed(
                message = "No compatible installer is available for this system.",
                release = release,
            )
            downloadInProgress = false
            return
        }
        val checksumAsset = release.assets.singleOrNull { it.name == "SHA256SUMS" }
        if (checksumAsset == null) {
            state = UpdateState.Failed(
                message = "This release does not include checksum information.",
                release = release,
            )
            downloadInProgress = false
            return
        }
        val gateway = downloader
        if (gateway == null) {
            state = UpdateState.Failed("Update download is unavailable.", release)
            downloadInProgress = false
            return
        }

        Files.createDirectories(downloadDirectory)
        val destination = downloadDirectory.resolve(asset.name)
        try {
            state = UpdateState.Downloading(release, progress = 0f)
            val downloaded = gateway.download(asset, destination) { received, total ->
                state = UpdateState.Downloading(
                    release,
                    progress = total?.takeIf { it > 0 }?.let { received.toFloat() / it.toFloat() },
                )
            }
            state = UpdateState.Verifying(release)
            val checksumText = gateway.downloadText(checksumAsset)
            val expected = expectedChecksum(checksumText, asset.name)
                ?: error("Checksum entry for ${asset.name} is missing.")
            val actual = sha256(downloaded)
            if (!actual.equals(expected, ignoreCase = true)) {
                downloaded.deleteIfExists()
                error("Downloaded update did not pass checksum verification.")
            }
            state = UpdateState.ReadyToInstall(release, asset, downloaded)
        } catch (error: Throwable) {
            destination.deleteIfExists()
            state = if (cancellationRequested) {
                UpdateState.Available(release)
            } else {
                UpdateState.Failed(
                    message = error.message?.takeIf { it.isNotBlank() } ?: "Update download failed.",
                    release = release,
                )
            }
        } finally {
            downloadInProgress = false
            cancellationRequested = false
        }
    }

    suspend fun downloadAndInstallUpdate() {
        downloadUpdate()
        installVerifiedUpdate()
    }

    fun cancelDownload() {
        val downloading = state as? UpdateState.Downloading ?: return
        cancellationRequested = true
        downloader?.cancel()
        state = UpdateState.Available(downloading.release)
    }

    suspend fun retryDownload() {
        val failed = state as? UpdateState.Failed ?: return
        val release = failed.release ?: return
        state = UpdateState.Available(release)
        downloadAndInstallUpdate()
    }

    fun dismissFailure() {
        val failed = state as? UpdateState.Failed ?: return
        val release = failed.release
        if (release == null) {
            state = UpdateState.Idle
            return
        }
        preferences.save(preferences.load().copy(dismissedVersion = release.version.toString()))
        state = UpdateState.Available(release, dismissed = true)
    }

    private fun installVerifiedUpdate() {
        val ready = state as? UpdateState.ReadyToInstall ?: return
        state = UpdateState.Installing(ready.release)
        try {
            installer.launch(
                UpdateInstallRequest(
                    release = ready.release,
                    asset = ready.asset,
                    path = ready.path,
                    platform = platform,
                    launchContext = launchContext,
                ),
            )
            state = UpdateState.Restarting(ready.release)
            applicationLifecycle.exitForUpdate()
        } catch (error: Throwable) {
            state = UpdateState.Failed(
                message = error.message?.takeIf { it.isNotBlank() } ?: "The update could not be installed.",
                release = ready.release,
            )
        }
    }

    fun preferences(): UpdatePreferences = preferences.load()

    private fun previousInstallationState(): UpdateState {
        val resultPath = downloadDirectory.resolve("install-result")
        if (!Files.isRegularFile(resultPath)) return UpdateState.Idle
        val result = runCatching { Files.readString(resultPath).trim() }.getOrNull()
        resultPath.deleteIfExists()
        return if (result == "failure") {
            UpdateState.Failed(
                message = "The update could not be installed. RevQ restarted without applying it.",
                visible = true,
            )
        } else {
            UpdateState.Idle
        }
    }
}

fun nextDailyUpdateCheck(
    now: ZonedDateTime,
    target: LocalTime = LocalTime.of(9, 0),
): ZonedDateTime {
    val today = now.toLocalDate().atTime(target).atZone(now.zone)
    return if (now.isBefore(today)) today else today.plusDays(1)
}

private fun expectedChecksum(checksums: String, assetName: String): String? = checksums
    .lineSequence()
    .map { it.trim() }
    .mapNotNull { line ->
        val parts = line.split(Regex("\\s+"), limit = 2)
        if (parts.size != 2) null else parts[0] to parts[1].removePrefix("*")
    }
    .firstOrNull { (_, name) -> name == assetName }
    ?.first
    ?.takeIf { it.matches(Regex("[a-fA-F0-9]{64}")) }

private fun sha256(path: Path): String {
    val digest = MessageDigest.getInstance("SHA-256")
    Files.newInputStream(path).use { input ->
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        while (true) {
            val count = input.read(buffer)
            if (count < 0) break
            digest.update(buffer, 0, count)
        }
    }
    return digest.digest().joinToString("") { "%02x".format(it) }
}
