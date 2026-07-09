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
