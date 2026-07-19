package eu.revq

import kotlin.io.path.createTempDirectory
import kotlin.io.path.writeBytes
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class NotificationSoundTest {
    @Test
    fun offModeAlwaysResolvesToDisabled() {
        assertEquals(
            NotificationSoundSource.Disabled,
            NotificationSoundResolver.resolve(
                configuration = NotificationSoundConfiguration(
                    mode = NotificationSoundMode.Off,
                    customWavPath = "/missing/sound.wav",
                ),
                defaultSoundAvailable = false,
            ),
        )
    }

    @Test
    fun validCustomWavIsUsedDirectly() {
        val sound = createTempDirectory("revq-sound-test").resolve("custom.wav")
        sound.writeBytes(byteArrayOf(1, 2, 3))

        val resolved = NotificationSoundResolver.resolve(
            configuration = NotificationSoundConfiguration(
                mode = NotificationSoundMode.Custom,
                customWavPath = sound.toString(),
            ),
            defaultSoundAvailable = true,
        )

        assertEquals(NotificationSoundSource.CustomWav(sound), resolved)
    }

    @Test
    fun missingCustomWavFallsBackToBundledDefault() {
        val resolved = NotificationSoundResolver.resolve(
            configuration = NotificationSoundConfiguration(
                mode = NotificationSoundMode.Custom,
                customWavPath = "/missing/revq.wav",
            ),
            defaultSoundAvailable = true,
        )

        val fallback = assertIs<NotificationSoundSource.FallbackToDefault>(resolved)
        assertTrue(fallback.reason.contains("no longer exists"))
    }

    @Test
    fun missingCustomAndDefaultSoundsReportUnavailable() {
        val resolved = NotificationSoundResolver.resolve(
            configuration = NotificationSoundConfiguration(
                mode = NotificationSoundMode.Custom,
                customWavPath = "",
            ),
            defaultSoundAvailable = false,
        )

        val unavailable = assertIs<NotificationSoundSource.Unavailable>(resolved)
        assertTrue(unavailable.reason.contains(DefaultNotificationSoundResource))
    }

    @Test
    fun persistedModesRemainBackwardCompatible() {
        assertEquals(NotificationSoundMode.Default, NotificationSoundMode.fromPersisted(""))
        assertEquals(NotificationSoundMode.Default, NotificationSoundMode.fromPersisted("unknown"))
        assertEquals(NotificationSoundMode.Custom, NotificationSoundMode.fromPersisted("CUSTOM"))
    }
}
