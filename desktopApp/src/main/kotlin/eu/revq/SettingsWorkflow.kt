package eu.revq

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

data class SettingsDraft(
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
    val onboardingCompleted: Boolean = false,
    val githubIdentityLogin: String = "",
    val githubIdentityHost: String = "",
    val dismissedUpdateVersion: String = "",
    val latestKnownUpdateVersion: String = "",
    val lastUpdateCheck: String = "",
    val automaticUpdateChecksEnabled: Boolean = true,
    val lastUpdateError: String = "",
) {
    fun normalized(): SettingsDraft = copy(
        repositories = repositories.normalizedLines(),
        organizations = organizations.normalizedLines(),
        githubExecutable = githubExecutable.trim(),
        mutedRepositories = mutedRepositories.normalizedLines(),
        autoRefreshIntervalMinutes = autoRefreshIntervalMinutes.clampedNumber(default = 5, range = 1L..240L),
        sortMode = sortMode.trim().ifBlank { "Urgency" },
        staleThresholdDays = staleThresholdDays.clampedNumber(default = 2, range = 1L..30L),
        reminderTime = reminderTime.trim().ifBlank { "09:00" },
        reminderDays = reminderDays.trim().ifBlank { "Mon-Fri" },
        quietHours = quietHours.trim().ifBlank { "18:00-08:00" },
        reminderSnoozeMinutes = reminderSnoozeMinutes.clampedNumber(default = 60, range = 5L..1_440L),
    )

    fun toSettings(): RevqSettings = RevqSettings(
        repositories = repositories,
        organizations = organizations,
        githubExecutable = githubExecutable,
        githubDetectionSource = githubDetectionSource,
        mutedRepositories = mutedRepositories,
        autoRefreshEnabled = autoRefreshEnabled,
        autoRefreshIntervalMinutes = autoRefreshIntervalMinutes,
        sortMode = sortMode,
        groupByRepository = groupByRepository,
        staleThresholdDays = staleThresholdDays,
        compactRows = compactRows,
        reminderEnabled = reminderEnabled,
        reminderTime = reminderTime,
        reminderDays = reminderDays,
        quietHours = quietHours,
        remindOnlyWhenQueueNotClear = remindOnlyWhenQueueNotClear,
        reminderSnoozeMinutes = reminderSnoozeMinutes,
        onboardingCompleted = onboardingCompleted,
        githubIdentityLogin = githubIdentityLogin,
        githubIdentityHost = githubIdentityHost,
        dismissedUpdateVersion = dismissedUpdateVersion,
        latestKnownUpdateVersion = latestKnownUpdateVersion,
        lastUpdateCheck = lastUpdateCheck,
        automaticUpdateChecksEnabled = automaticUpdateChecksEnabled,
        lastUpdateError = lastUpdateError,
    )

    companion object {
        fun from(settings: RevqSettings): SettingsDraft = SettingsDraft(
            repositories = settings.repositories,
            organizations = settings.organizations,
            githubExecutable = settings.githubExecutable,
            githubDetectionSource = settings.githubDetectionSource,
            mutedRepositories = settings.mutedRepositories,
            autoRefreshEnabled = settings.autoRefreshEnabled,
            autoRefreshIntervalMinutes = settings.autoRefreshIntervalMinutes,
            sortMode = settings.sortMode,
            groupByRepository = settings.groupByRepository,
            staleThresholdDays = settings.staleThresholdDays,
            compactRows = settings.compactRows,
            reminderEnabled = settings.reminderEnabled,
            reminderTime = settings.reminderTime,
            reminderDays = settings.reminderDays,
            quietHours = settings.quietHours,
            remindOnlyWhenQueueNotClear = settings.remindOnlyWhenQueueNotClear,
            reminderSnoozeMinutes = settings.reminderSnoozeMinutes,
            onboardingCompleted = settings.onboardingCompleted,
            githubIdentityLogin = settings.githubIdentityLogin,
            githubIdentityHost = settings.githubIdentityHost,
            dismissedUpdateVersion = settings.dismissedUpdateVersion,
            latestKnownUpdateVersion = settings.latestKnownUpdateVersion,
            lastUpdateCheck = settings.lastUpdateCheck,
            automaticUpdateChecksEnabled = settings.automaticUpdateChecksEnabled,
            lastUpdateError = settings.lastUpdateError,
        )
    }
}

data class SettingsValidation(
    val errors: Map<String, String> = emptyMap(),
) {
    val isValid: Boolean
        get() = errors.isEmpty()
}

data class SettingsWorkflowSnapshot(
    val applied: RevqSettings,
    val draft: SettingsDraft,
    val validation: SettingsValidation,
    val hasChanges: Boolean,
    val section: SettingsSection = SettingsSection.General,
    val focusedRowIndex: Int = 0,
)

sealed interface SettingsAction {
    data class ReplaceDraft(val draft: SettingsDraft) : SettingsAction
    data object Apply : SettingsAction
    data object Reload : SettingsAction
    data object Revert : SettingsAction
    data class SelectSection(val section: SettingsSection) : SettingsAction
    data class MoveSection(val delta: Int) : SettingsAction
    data class MoveRow(val delta: Int) : SettingsAction
    data class FocusRow(val index: Int) : SettingsAction
}

fun interface SettingsRuntimeEffects {
    fun apply(previous: RevqSettings, current: RevqSettings)

    companion object {
        val None = SettingsRuntimeEffects { _, _ -> }
    }
}

class SettingsWorkflow(
    private val store: SettingsStore,
    private val runtime: SettingsRuntimeEffects = SettingsRuntimeEffects.None,
) {
    private var currentSnapshot by mutableStateOf(snapshot(store.load()))

    val snapshot: SettingsWorkflowSnapshot
        get() = currentSnapshot

    fun apply(action: SettingsAction) {
        when (action) {
            is SettingsAction.ReplaceDraft -> updateDraft(action.draft)
            SettingsAction.Apply -> persistDraft()
            SettingsAction.Reload -> currentSnapshot = snapshot(store.load()).withNavigationFrom(currentSnapshot)
            SettingsAction.Revert -> currentSnapshot = snapshot(currentSnapshot.applied).withNavigationFrom(currentSnapshot)
            is SettingsAction.SelectSection -> selectSection(action.section)
            is SettingsAction.MoveSection -> moveSection(action.delta)
            is SettingsAction.MoveRow -> moveRow(action.delta)
            is SettingsAction.FocusRow -> focusRow(action.index)
        }
    }

    private fun updateDraft(draft: SettingsDraft) {
        currentSnapshot = currentSnapshot.copy(
            draft = draft,
            validation = validate(draft),
            hasChanges = draft.toSettings() != currentSnapshot.applied,
        )
    }

    private fun persistDraft() {
        val normalizedDraft = currentSnapshot.draft.normalized()
        val validation = validate(normalizedDraft)
        if (!validation.isValid) {
            currentSnapshot = currentSnapshot.copy(validation = validation)
            return
        }

        val previous = currentSnapshot.applied
        val current = normalizedDraft.toSettings()
        store.save(current)
        runtime.apply(previous, current)
        currentSnapshot = snapshot(current).withNavigationFrom(currentSnapshot)
    }

    private fun snapshot(settings: RevqSettings): SettingsWorkflowSnapshot = SettingsWorkflowSnapshot(
        applied = settings,
        draft = SettingsDraft.from(settings),
        validation = SettingsValidation(),
        hasChanges = false,
    )

    private fun validate(draft: SettingsDraft): SettingsValidation {
        val errors = buildMap {
            draft.repositories.filter { parseRepo(it) == null }.takeIf { it.isNotEmpty() }?.let {
                put("repositories", "Use owner/repository for every tracked repository")
            }
            draft.organizations.filter { it.isBlank() || '/' in it || ' ' in it }
                .takeIf { it.isNotEmpty() }
                ?.let { put("organizations", "Organization names cannot contain spaces or slashes") }
            if (!draft.reminderTime.isClockTime()) put("reminderTime", "Use a 24-hour time such as 09:00")
        }
        return SettingsValidation(errors)
    }

    private fun selectSection(section: SettingsSection) {
        currentSnapshot = currentSnapshot.copy(
            section = section,
            focusedRowIndex = currentSnapshot.focusedRowIndex.coerceIn(settingsRowLabels(section).indices),
        )
    }

    private fun moveSection(delta: Int) {
        val next = SettingsSection.entries[
            (currentSnapshot.section.ordinal + delta).coerceIn(SettingsSection.entries.indices)
        ]
        selectSection(next)
    }

    private fun moveRow(delta: Int) {
        focusRow(currentSnapshot.focusedRowIndex + delta)
    }

    private fun focusRow(index: Int) {
        currentSnapshot = currentSnapshot.copy(
            focusedRowIndex = index.coerceIn(settingsRowLabels(currentSnapshot.section).indices),
        )
    }
}

private fun SettingsWorkflowSnapshot.withNavigationFrom(
    previous: SettingsWorkflowSnapshot,
): SettingsWorkflowSnapshot = copy(
    section = previous.section,
    focusedRowIndex = previous.focusedRowIndex.coerceIn(settingsRowLabels(previous.section).indices),
)

private fun List<String>.normalizedLines(): List<String> =
    map(String::trim).filter(String::isNotEmpty).distinct()

private fun String.clampedNumber(default: Long, range: LongRange): String =
    (trim().toLongOrNull() ?: default).coerceIn(range.first, range.last).toString()

private fun String.isClockTime(): Boolean {
    val match = Regex("^(\\d{2}):(\\d{2})$").matchEntire(trim()) ?: return false
    return match.groupValues[1].toInt() in 0..23 && match.groupValues[2].toInt() in 0..59
}
