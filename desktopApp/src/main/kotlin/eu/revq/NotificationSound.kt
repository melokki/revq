package eu.revq

import java.awt.FileDialog
import java.awt.Frame
import java.awt.GraphicsEnvironment
import java.io.BufferedInputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.Collections
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip
import javax.sound.sampled.LineEvent

internal const val DefaultNotificationSoundResource = "sounds/notification-default.wav"

enum class NotificationSoundMode(
    val label: String,
    val persistedValue: String,
) {
    Off("Off", "off"),
    Default("Default RevQ sound", "default"),
    Custom("Custom WAV file", "custom");

    companion object {
        fun fromPersisted(value: String): NotificationSoundMode =
            entries.firstOrNull { it.persistedValue == value.trim().lowercase() } ?: Default
    }
}

data class NotificationSoundConfiguration(
    val mode: NotificationSoundMode,
    val customWavPath: String,
)

sealed interface NotificationSoundSource {
    data object Disabled : NotificationSoundSource

    data class BundledDefault(
        val resourcePath: String = DefaultNotificationSoundResource,
    ) : NotificationSoundSource

    data class CustomWav(
        val path: Path,
    ) : NotificationSoundSource

    data class FallbackToDefault(
        val reason: String,
        val resourcePath: String = DefaultNotificationSoundResource,
    ) : NotificationSoundSource

    data class Unavailable(
        val reason: String,
    ) : NotificationSoundSource
}

data class NotificationSoundFeedback(
    val message: String,
    val isWarning: Boolean = false,
)

sealed interface NotificationSoundPlayback {
    data object Disabled : NotificationSoundPlayback
    data object PlayedDefault : NotificationSoundPlayback
    data class PlayedCustom(val path: Path) : NotificationSoundPlayback
    data class FellBackToDefault(val reason: String) : NotificationSoundPlayback
    data class Failed(val reason: String) : NotificationSoundPlayback
}

object NotificationSoundResolver {
    fun resolve(
        configuration: NotificationSoundConfiguration,
        defaultSoundAvailable: Boolean,
    ): NotificationSoundSource = when (configuration.mode) {
        NotificationSoundMode.Off -> NotificationSoundSource.Disabled
        NotificationSoundMode.Default -> if (defaultSoundAvailable) {
            NotificationSoundSource.BundledDefault()
        } else {
            NotificationSoundSource.Unavailable(
                "The bundled default WAV is missing from $DefaultNotificationSoundResource.",
            )
        }

        NotificationSoundMode.Custom -> {
            val customPath = configuration.customWavPath
                .trim()
                .takeIf(String::isNotBlank)
                ?.let { runCatching { Paths.get(it) }.getOrNull() }
            val customIssue = customPathIssue(customPath)

            when {
                customIssue == null && customPath != null -> NotificationSoundSource.CustomWav(customPath)
                defaultSoundAvailable -> NotificationSoundSource.FallbackToDefault(
                    reason = customIssue ?: "No custom WAV file is selected.",
                )
                else -> NotificationSoundSource.Unavailable(
                    reason = buildString {
                        append(customIssue ?: "No custom WAV file is selected.")
                        append(" The bundled fallback is also unavailable at ")
                        append(DefaultNotificationSoundResource)
                        append('.')
                    },
                )
            }
        }
    }

    private fun customPathIssue(path: Path?): String? = when {
        path == null -> "No custom WAV file is selected."
        !path.fileName.toString().endsWith(".wav", ignoreCase = true) -> "The selected file is not a WAV file."
        !Files.exists(path) -> "The selected custom WAV no longer exists."
        !Files.isRegularFile(path) -> "The selected custom WAV is not a regular file."
        !Files.isReadable(path) -> "The selected custom WAV cannot be read."
        else -> null
    }
}

interface NotificationSoundGateway {
    fun resolve(configuration: NotificationSoundConfiguration): NotificationSoundSource
    fun play(configuration: NotificationSoundConfiguration): NotificationSoundPlayback
}

class JvmNotificationSoundGateway(
    private val resourceLoader: ClassLoader = JvmNotificationSoundGateway::class.java.classLoader,
    private val defaultResourcePath: String = DefaultNotificationSoundResource,
) : NotificationSoundGateway {
    private val activeClips = Collections.synchronizedSet(mutableSetOf<Clip>())

    override fun resolve(configuration: NotificationSoundConfiguration): NotificationSoundSource =
        NotificationSoundResolver.resolve(
            configuration = configuration,
            defaultSoundAvailable = resourceLoader.getResource(defaultResourcePath) != null,
        )

    override fun play(configuration: NotificationSoundConfiguration): NotificationSoundPlayback =
        when (val source = resolve(configuration)) {
            NotificationSoundSource.Disabled -> NotificationSoundPlayback.Disabled
            is NotificationSoundSource.Unavailable -> NotificationSoundPlayback.Failed(source.reason)
            is NotificationSoundSource.BundledDefault -> runCatching {
                playDefault(source.resourcePath)
                NotificationSoundPlayback.PlayedDefault
            }.getOrElse { NotificationSoundPlayback.Failed(soundFailureMessage(it)) }

            is NotificationSoundSource.FallbackToDefault -> runCatching {
                playDefault(source.resourcePath)
                NotificationSoundPlayback.FellBackToDefault(source.reason)
            }.getOrElse {
                NotificationSoundPlayback.Failed(
                    "${source.reason} The bundled fallback could not be played: ${soundFailureMessage(it)}",
                )
            }

            is NotificationSoundSource.CustomWav -> runCatching {
                playFile(source.path)
                NotificationSoundPlayback.PlayedCustom(source.path)
            }.getOrElse { customFailure ->
                val defaultAvailable = resourceLoader.getResource(defaultResourcePath) != null
                if (defaultAvailable) {
                    runCatching {
                        playDefault(defaultResourcePath)
                        NotificationSoundPlayback.FellBackToDefault(
                            "The custom WAV could not be played: ${soundFailureMessage(customFailure)}",
                        )
                    }.getOrElse { defaultFailure ->
                        NotificationSoundPlayback.Failed(
                            "The custom WAV failed and the bundled fallback could not be played: " +
                                    soundFailureMessage(defaultFailure),
                        )
                    }
                } else {
                    NotificationSoundPlayback.Failed(
                        "The custom WAV could not be played and no bundled fallback is available: " +
                                soundFailureMessage(customFailure),
                    )
                }
            }
        }

    private fun playDefault(resourcePath: String) {
        val resource = resourceLoader.getResource(resourcePath)
            ?: error("Bundled WAV resource '$resourcePath' was not found")
        BufferedInputStream(resource.openStream()).use { input ->
            AudioSystem.getAudioInputStream(input).use(::playStream)
        }
    }

    private fun playFile(path: Path) {
        AudioSystem.getAudioInputStream(path.toFile()).use(::playStream)
    }

    private fun playStream(stream: AudioInputStream) {
        val clip = AudioSystem.getClip()
        try {
            clip.open(stream)
            activeClips += clip
            clip.addLineListener { event ->
                if (event.type == LineEvent.Type.STOP || event.type == LineEvent.Type.CLOSE) {
                    activeClips -= clip
                    if (clip.isOpen) clip.close()
                }
            }
            clip.start()
        } catch (error: Throwable) {
            activeClips -= clip
            if (clip.isOpen) clip.close()
            throw error
        }
    }
}

internal fun chooseCustomNotificationWav(): Path? {
    if (GraphicsEnvironment.isHeadless()) return null

    val dialog = FileDialog(null as Frame?, "Choose notification WAV", FileDialog.LOAD).apply {
        file = "*.wav"
        filenameFilter = java.io.FilenameFilter { _, name ->
            name.endsWith(".wav", ignoreCase = true)
        }
    }
    dialog.isVisible = true

    val directory = dialog.directory ?: return null
    val file = dialog.file ?: return null
    return Paths.get(directory, file).toAbsolutePath().normalize()
}

internal fun NotificationSoundSource.settingsDescription(): String = when (this) {
    NotificationSoundSource.Disabled -> "Notification windows will stay silent."
    is NotificationSoundSource.BundledDefault -> "Using the bundled RevQ WAV."
    is NotificationSoundSource.CustomWav -> "Using ${path.fileName}."
    is NotificationSoundSource.FallbackToDefault -> "$reason Falling back to the bundled RevQ sound."
    is NotificationSoundSource.Unavailable -> reason
}

internal val NotificationSoundSource.isWarning: Boolean
    get() = this is NotificationSoundSource.FallbackToDefault || this is NotificationSoundSource.Unavailable

private fun soundFailureMessage(error: Throwable): String =
    error.message?.takeIf(String::isNotBlank) ?: error::class.simpleName.orEmpty().ifBlank { "Unknown audio error" }
