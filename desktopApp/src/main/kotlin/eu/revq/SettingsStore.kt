package eu.revq

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.exists
import kotlin.io.path.readLines
import kotlin.io.path.writeLines

data class RevqSettings(
    val repositories: List<String> = emptyList(),
    val organizations: List<String> = emptyList(),
    val githubExecutable: String = "",
    val githubDetectionSource: String = "Not detected",
    val mutedRepositories: List<String> = emptyList(),
    val autoRefreshEnabled: Boolean = true,
    val autoRefreshIntervalMinutes: String = "5",
    val showReviewCountInTray: Boolean = true,
    val notifyOnNewReviewAssignments: Boolean = true,
    val notificationSoundMode: NotificationSoundMode = NotificationSoundMode.Default,
    val customNotificationSoundPath: String = "",
    val sortMode: String = "Urgency",
    val groupByRepository: Boolean = false,
    val staleThresholdDays: String = "2",
    val compactRows: Boolean = false,
    val reminderEnabled: Boolean = true,
    val reminderTime: String = "09:00",
    val reminderDays: String = "Mon-Fri",
    val quietHours: String = "18:00-08:00",
    val remindOnlyWhenQueueNotClear: Boolean = true,
    val reminderSnoozeMinutes: String = "60",
    val onboardingCompleted: Boolean = false,
    val githubIdentityLogin: String = "",
    val githubIdentityHost: String = "",
    val dismissedUpdateVersion: String = "",
    val latestKnownUpdateVersion: String = "",
    val lastUpdateCheck: String = "",
    val automaticUpdateChecksEnabled: Boolean = true,
    val lastUpdateError: String = "",
)

interface SettingsStore {
    fun load(): RevqSettings
    fun save(settings: RevqSettings)
}

class InMemorySettingsStore(
    initial: RevqSettings = RevqSettings(),
) : SettingsStore {
    private var settings = initial

    override fun load(): RevqSettings = settings

    override fun save(settings: RevqSettings) {
        this.settings = settings
    }
}

class FileSettingsStore(
    private val directory: Path = defaultRevqConfigDirectory(),
) : SettingsStore {
    override fun load(): RevqSettings = RevqSettings(
        repositories = lines("repositories.txt"),
        organizations = lines("organizations.txt"),
        githubExecutable = value("gh-path.txt", ""),
        githubDetectionSource = value("gh-detection-source.txt", "Not detected"),
        mutedRepositories = lines("muted-repositories.txt"),
        autoRefreshEnabled = boolean("auto-refresh-enabled.txt", true),
        autoRefreshIntervalMinutes = value("auto-refresh-interval-minutes.txt", "5"),
        showReviewCountInTray = boolean("show-review-count-in-tray.txt", true),
        notifyOnNewReviewAssignments = boolean("notify-on-new-review-assignments.txt", true),
        notificationSoundMode = NotificationSoundMode.fromPersisted(value("notification-sound-mode.txt", "default")),
        customNotificationSoundPath = value("custom-notification-sound-path.txt", ""),
        sortMode = value("sort-mode.txt", "Urgency"),
        groupByRepository = boolean("group-by-repository.txt", false),
        staleThresholdDays = value("stale-threshold-days.txt", "2"),
        compactRows = boolean("compact-rows.txt", false),
        reminderEnabled = boolean("reminder-enabled.txt", true),
        reminderTime = value("reminder-time.txt", "09:00"),
        reminderDays = value("reminder-days.txt", "Mon-Fri"),
        quietHours = value("reminder-quiet-hours.txt", "18:00-08:00"),
        remindOnlyWhenQueueNotClear = boolean("reminder-only-when-queue.txt", true),
        reminderSnoozeMinutes = value("reminder-snooze-minutes.txt", "60"),
        onboardingCompleted = boolean("onboarding-completed.txt", false),
        githubIdentityLogin = value("github-identity-login.txt", ""),
        githubIdentityHost = value("github-identity-host.txt", ""),
        dismissedUpdateVersion = value("dismissed-update-version.txt", ""),
        latestKnownUpdateVersion = value("latest-known-update-version.txt", ""),
        lastUpdateCheck = value("last-update-check.txt", ""),
        automaticUpdateChecksEnabled = boolean("automatic-update-checks-enabled.txt", true),
        lastUpdateError = value("last-update-error.txt", ""),
    )

    override fun save(settings: RevqSettings) {
        Files.createDirectories(directory)
        write("repositories.txt", settings.repositories)
        write("organizations.txt", settings.organizations)
        writeOptional("gh-path.txt", settings.githubExecutable)
        write("gh-detection-source.txt", listOf(settings.githubDetectionSource))
        write("muted-repositories.txt", settings.mutedRepositories)
        write("auto-refresh-enabled.txt", listOf(settings.autoRefreshEnabled.toString()))
        write("auto-refresh-interval-minutes.txt", listOf(settings.autoRefreshIntervalMinutes))
        write("show-review-count-in-tray.txt", listOf(settings.showReviewCountInTray.toString()))
        write("notify-on-new-review-assignments.txt", listOf(settings.notifyOnNewReviewAssignments.toString()))
        write("notification-sound-mode.txt", listOf(settings.notificationSoundMode.persistedValue))
        writeOptional("custom-notification-sound-path.txt", settings.customNotificationSoundPath)
        write("sort-mode.txt", listOf(settings.sortMode))
        write("group-by-repository.txt", listOf(settings.groupByRepository.toString()))
        write("stale-threshold-days.txt", listOf(settings.staleThresholdDays))
        write("compact-rows.txt", listOf(settings.compactRows.toString()))
        write("reminder-enabled.txt", listOf(settings.reminderEnabled.toString()))
        write("reminder-time.txt", listOf(settings.reminderTime))
        write("reminder-days.txt", listOf(settings.reminderDays))
        write("reminder-quiet-hours.txt", listOf(settings.quietHours))
        write("reminder-only-when-queue.txt", listOf(settings.remindOnlyWhenQueueNotClear.toString()))
        write("reminder-snooze-minutes.txt", listOf(settings.reminderSnoozeMinutes))
        write("onboarding-completed.txt", listOf(settings.onboardingCompleted.toString()))
        writeOptional("github-identity-login.txt", settings.githubIdentityLogin)
        writeOptional("github-identity-host.txt", settings.githubIdentityHost)
        writeOptional("dismissed-update-version.txt", settings.dismissedUpdateVersion)
        writeOptional("latest-known-update-version.txt", settings.latestKnownUpdateVersion)
        writeOptional("last-update-check.txt", settings.lastUpdateCheck)
        write("automatic-update-checks-enabled.txt", listOf(settings.automaticUpdateChecksEnabled.toString()))
        writeOptional("last-update-error.txt", settings.lastUpdateError)
    }

    private fun lines(name: String): List<String> =
        directory.resolve(name)
            .takeIf(Path::exists)
            ?.readLines()
            ?.map(String::trim)
            ?.filter(String::isNotEmpty)
            .orEmpty()

    private fun value(
        name: String,
        default: String,
    ): String = lines(name).firstOrNull()?.ifBlank { null } ?: default

    private fun boolean(
        name: String,
        default: Boolean,
    ): Boolean = value(name, default.toString()).toBooleanStrictOrNull() ?: default

    private fun write(
        name: String,
        values: List<String>,
    ) {
        directory.resolve(name).writeLines(values)
    }

    private fun writeOptional(
        name: String,
        value: String,
    ) {
        val path = directory.resolve(name)
        if (value.isBlank()) {
            Files.deleteIfExists(path)
        } else {
            path.writeLines(listOf(value))
        }
    }
}

fun defaultRevqConfigDirectory(): Path =
    Paths.get(System.getProperty("user.home"), ".config", "revq-compose")
