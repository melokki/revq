package eu.revq

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import eu.revq.keyboard.KeyboardMode
import java.awt.Toolkit

private val SettingsNavBg = Color(0xFF171A1E)
private val SettingsGroupBg = Color(0xFF191D22)
private val SettingsSuccessBg = Color(0xFF1F2A22)
private val SettingsWarningBg = Color(0xFF2B261D)

@Composable
fun SettingsPane(state: AppState) {
    val selectedSection = currentSettingsSection(state)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PanelBg)
            .onPreviewKeyEvent { handleSettingsKeyEvent(it, state) },
    ) {
        SettingsHeader()
        HorizontalDivider(Modifier, DividerDefaults.Thickness, color = Border)

        Row(
            modifier = Modifier
                .fillMaxSize(),
        ) {
            SettingsNavigation(
                selected = selectedSection,
                onSelect = {
                    state.settingsSectionIndex = it.ordinal
                    state.settingsFocusedRowIndex = 0
                },
            )

            HorizontalDivider(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp), thickness = DividerDefaults.Thickness, color = Border
            )

            SettingsContent(
                state = state,
                section = selectedSection,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun SettingsHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(86.dp)
            .padding(horizontal = 28.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = "Settings",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = "Configure how RevQ connects, refreshes, and reminds you.",
                color = TextMuted,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = null,
                tint = ReadyGreen,
                modifier = Modifier.size(17.dp),
            )
            Text(
                text = "Changes save automatically",
                color = TextMuted,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun SettingsNavigation(
    selected: SettingsSection,
    onSelect: (SettingsSection) -> Unit,
) {
    Column(
        modifier = Modifier
            .width(220.dp)
            .fillMaxHeight()
            .background(SettingsNavBg)
            .padding(horizontal = 12.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        SettingsSection.entries.forEach { section ->
            SettingsNavigationItem(
                section = section,
                selected = selected == section,
                onClick = { onSelect(section) },
            )
        }
    }
}

@Composable
private fun SettingsNavigationItem(
    section: SettingsSection,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) PanelElevated else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(if (selected) Olive.copy(alpha = 0.16f) else Color.Transparent),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = section.icon,
                contentDescription = null,
                tint = if (selected) Olive else TextMuted,
                modifier = Modifier.size(18.dp),
            )
        }

        Text(
            text = section.label,
            color = if (selected) TextPrimary else TextMuted,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun SettingsContent(
    state: AppState,
    section: SettingsSection,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxHeight(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 980.dp)
                .fillMaxWidth()
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp),
        ) {
            SettingsKeyboardFocusStrip(state)
            when (section) {
                SettingsSection.General -> GeneralSettings(state)
                SettingsSection.GitHub -> GitHubSettings(state)
                SettingsSection.Tracking -> TrackingSettings(state)
                SettingsSection.Review -> ReviewSettings(state)
                SettingsSection.Reminders -> ReminderSettings(state)
                SettingsSection.Data -> DataSettings(state)
            }
            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun SettingsKeyboardFocusStrip(state: AppState) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF2C3323),
        border = BorderStroke(1.dp, Olive.copy(alpha = 0.32f)),
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = currentSettingsSection(state).label,
                color = Olive,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelMedium,
            )
            Text(
                text = currentSettingsRowLabel(state),
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "Enter",
                color = TextMuted,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

private fun handleSettingsKeyEvent(
    event: KeyEvent,
    state: AppState,
): Boolean {
    if (event.type != KeyEventType.KeyDown) return false

    if (state.keyboardMode == KeyboardMode.Insert) {
        if (event.key == Key.Escape) {
            state.keyboardMode = KeyboardMode.Normal
            return true
        }
        return false
    }

    val primary = event.isCtrlPressed || event.isMetaPressed

    if (primary && event.key == Key.S) {
        state.saveConfig()
        state.statusLine = "Settings saved"
        return true
    }

    return when (event.key) {
        Key.DirectionDown, Key.J -> {
            moveSettingsRow(state, 1)
            true
        }

        Key.DirectionUp, Key.K -> {
            moveSettingsRow(state, -1)
            true
        }

        Key.DirectionRight, Key.L -> {
            moveSettingsSection(state, 1)
            true
        }

        Key.DirectionLeft, Key.H -> {
            moveSettingsSection(state, -1)
            true
        }

        Key.Enter -> {
            activateFocusedSettingsRow(state)
            true
        }

        Key.Escape -> {
            state.selectView(View.NeedsReview)
            true
        }

        else -> false
    }
}

@Composable
private fun GeneralSettings(state: AppState) {
    SettingsSectionHeader(
        title = "General",
        subtitle = "Core desktop behavior and everyday queue preferences.",
    )

    if (isSetupIncomplete(state)) {
        SetupProgressCard(state)
    } else {
        RevqStatusCard(state)
    }

    SettingsSectionLabel("REFRESH")
    SettingsGroup {
        ToggleSettingRow(
            title = "Auto refresh",
            description = "Keep the review queue current while RevQ is running.",
            checked = state.autoRefreshEnabled,
            onCheckedChange = {
                state.autoRefreshEnabled = it
                state.saveConfig()
            },
        )
        SettingsDivider()
        ChoiceSettingRow(
            title = "Refresh interval",
            description = "How often RevQ checks GitHub in the background.",
            value = "${state.autoRefreshIntervalMinutesText.ifBlank { "5" }} min",
            options = listOf("1", "5", "10", "15", "30", "60"),
            optionLabel = { "$it min" },
            onSelected = {
                state.autoRefreshIntervalMinutesText = it
                state.saveConfig()
            },
            enabled = state.autoRefreshEnabled,
        )
    }

    SettingsSectionLabel("WORKSPACE DEFAULTS")
    SettingsGroup {
        ChoiceSettingRow(
            title = "Row density",
            description = "Choose how much vertical space each pull request row uses.",
            value = if (state.compactRows) "Compact" else "Comfortable",
            options = listOf("Comfortable", "Compact"),
            onSelected = {
                state.compactRows = it == "Compact"
                state.saveConfig()
            },
        )
    }
}

@Composable
private fun GitHubSettings(state: AppState) {
    var advancedOpen by remember { mutableStateOf(false) }
    var overrideDraft by remember(state.ghPathText) { mutableStateOf(state.ghPathText) }

    SettingsSectionHeader(
        title = "GitHub",
        subtitle = "RevQ uses the GitHub CLI installed on this computer.",
    )

    GitHubConnectionCard(state)

    SettingsSectionLabel("CONNECTION")
    SettingsGroup {
        ActionSettingRow(
            title = "Test connection",
            description = "Verify GitHub CLI, authentication, and access to GitHub.com.",
            actionLabel = if (state.isTestingGh) "Testing…" else "Test",
            enabled = !state.isTestingGh,
            onClick = { state.testGithubCli() },
            showProgress = state.isTestingGh,
        )
        SettingsDivider()
        ActionSettingRow(
            title = "Run auto-detection again",
            description = platformDetectionCopy(currentDesktopPlatform()),
            actionLabel = "Detect",
            enabled = !state.isTestingGh,
            onClick = { state.autoDetectGithubCli() },
        )
    }

    SettingsSectionLabel("ADVANCED")
    SettingsGroup {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { advancedOpen = !advancedOpen }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = "Executable override",
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "Only use this when automatic detection does not find the correct gh executable.",
                    color = TextMuted,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Icon(
                imageVector = Icons.Rounded.ExpandMore,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(20.dp),
            )
        }

        if (advancedOpen) {
            HorizontalDivider(Modifier, DividerDefaults.Thickness, color = Border)
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedTextField(
                    value = overrideDraft,
                    onValueChange = { overrideDraft = it },
                    label = { Text("GitHub CLI executable") },
                    placeholder = { Text(platformGhPathExample()) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focusState ->
                            state.keyboardMode = if (focusState.isFocused) {
                                KeyboardMode.Insert
                            } else {
                                KeyboardMode.Normal
                            }
                        },
                    colors = revqTextFieldColors(),
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            state.ghPathText = overrideDraft.trim()
                            state.ghDetectionSource = if (state.ghPathText.isBlank()) "Automatic detection" else GhDetectionSource.Configured.label
                            state.saveConfig()
                            state.statusLine = if (state.ghPathText.isBlank()) {
                                "GitHub CLI override cleared"
                            } else {
                                "GitHub CLI override applied"
                            }
                        },
                    ) {
                        Text("Apply override")
                    }

                    TextButton(
                        onClick = {
                            overrideDraft = ""
                            state.ghPathText = ""
                            state.ghDetectionSource = "Automatic detection"
                            state.saveConfig()
                            state.statusLine = "GitHub CLI override cleared"
                        },
                    ) {
                        Text("Clear override", color = TextMuted)
                    }
                }
            }
        }
    }
}

@Composable
private fun GitHubConnectionCard(state: AppState) {
    val result = state.ghTestResult
    val connected = result?.startsWith("✓") == true
    val failed = result != null && !connected
    val statusColor = when {
        connected -> ReadyGreen
        failed -> MaterialTheme.colorScheme.error
        state.ghPathText.isNotBlank() -> Amber
        else -> TextMuted
    }
    val statusTitle = when {
        connected -> "Connected"
        failed -> "Needs attention"
        state.ghPathText.isNotBlank() -> "GitHub CLI detected"
        else -> "GitHub CLI not detected"
    }
    val username = result
        ?.substringAfter("authenticated as ", "")
        ?.substringBefore(" ·")
        ?.takeIf { it.isNotBlank() }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = when {
            connected -> SettingsSuccessBg
            failed -> MaterialTheme.colorScheme.error.copy(alpha = 0.08f)
            else -> SettingsWarningBg
        },
        border = BorderStroke(1.dp, Border),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(statusColor.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = if (connected) Icons.Rounded.CheckCircle else Icons.Rounded.Code,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(20.dp),
                    )
                }

                Column(Modifier.weight(1f)) {
                    Text(
                        text = statusTitle,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = username?.let { "@$it" } ?: "GitHub CLI on ${currentDesktopPlatform().displayName}",
                        color = TextMuted,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            SettingsMetaLine("Executable", state.ghPathText.ifBlank { "Automatic detection pending" })
            SettingsMetaLine("Detected via", state.ghDetectionSource)

            if (failed) {
                Text(
                    text = result.lineSequence().firstOrNull().orEmpty().take(240),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun TrackingSettings(state: AppState) {
    var repositoryDraft by remember { mutableStateOf("") }
    var organizationDraft by remember { mutableStateOf("") }
    var mutedDraft by remember { mutableStateOf("") }

    SettingsSectionHeader(
        title = "Tracking",
        subtitle = "Choose which repositories RevQ watches and which ones stay out of your queues.",
    )

    SettingsSectionLabel("TRACKED REPOSITORIES")
    SettingsGroup {
        TrackingList(
            values = parseLines(state.repositoriesText),
            emptyMessage = "No repositories configured yet.",
            onRemove = { state.removeRepository(it) },
        )
        HorizontalDivider(Modifier, DividerDefaults.Thickness, color = Border)
        InlineAddRow(
            value = repositoryDraft,
            onValueChange = { repositoryDraft = it },
            placeholder = "owner/repository",
            actionLabel = "Add repository",
            state = state,
            onAdd = {
                val value = repositoryDraft.trim()
                if (parseRepo(value) == null) {
                    state.statusLine = "Invalid repository. Use owner/repository."
                } else {
                    state.repositoriesText = (parseLines(state.repositoriesText) + value).distinct().sorted().joinToString("\n")
                    state.saveConfig()
                    state.statusLine = "Added $value"
                    repositoryDraft = ""
                }
            },
        )
    }

    SettingsSectionLabel("ORGANIZATIONS")
    SettingsGroup {
        TrackingList(
            values = parseLines(state.organizationsText),
            emptyMessage = "No organizations configured yet.",
            onRemove = { state.removeOrganization(it) },
        )
        HorizontalDivider(Modifier, DividerDefaults.Thickness, color = Border)
        InlineAddRow(
            value = organizationDraft,
            onValueChange = { organizationDraft = it },
            placeholder = "organization",
            actionLabel = "Add organization",
            state = state,
            onAdd = {
                val value = organizationDraft.trim()
                if (value.isBlank() || value.contains("/") || value.contains(" ")) {
                    state.statusLine = "Invalid organization name."
                } else {
                    state.organizationsText = (parseLines(state.organizationsText) + value).distinct().sorted().joinToString("\n")
                    state.saveConfig()
                    state.statusLine = "Added organization $value"
                    organizationDraft = ""
                }
            },
        )
        HorizontalDivider(Modifier, DividerDefaults.Thickness, color = Border)
        ActionSettingRow(
            title = "Discover repositories",
            description = "Find repositories from the organizations listed above.",
            actionLabel = if (state.isDiscovering) "Discovering…" else "Discover",
            enabled = !state.isDiscovering,
            onClick = { state.discoverTargets() },
            showProgress = state.isDiscovering,
        )
    }

    SettingsSectionLabel("MUTED REPOSITORIES")
    SettingsGroup {
        TrackingList(
            values = parseLines(state.mutedRepositoriesText),
            emptyMessage = "No muted repositories.",
            removeLabel = "Unmute",
            onRemove = { repo ->
                state.mutedRepositoriesText = parseLines(state.mutedRepositoriesText)
                    .filterNot { it == repo }
                    .joinToString("\n")
                state.saveConfig()
            },
        )
        HorizontalDivider(Modifier, DividerDefaults.Thickness, color = Border)
        InlineAddRow(
            value = mutedDraft,
            onValueChange = { mutedDraft = it },
            placeholder = "owner/repository",
            actionLabel = "Mute repository",
            state = state,
            onAdd = {
                val value = mutedDraft.trim()
                if (parseRepo(value) == null) {
                    state.statusLine = "Invalid repository. Use owner/repository."
                } else {
                    state.mutedRepositoriesText = (parseLines(state.mutedRepositoriesText) + value).distinct().sorted().joinToString("\n")
                    state.saveConfig()
                    state.statusLine = "Muted $value"
                    mutedDraft = ""
                }
            },
        )
    }
}

@Composable
private fun ReviewSettings(state: AppState) {
    SettingsSectionHeader(
        title = "Review",
        subtitle = "Tune RevQ as a focused review companion rather than a generic pull request dashboard.",
    )

    SettingsSectionLabel("REVIEW BEHAVIOR")
    SettingsGroup {
        ToggleSettingRow(
            title = "Focus review mode",
            description = "Keep review-oriented views centered on Needs Review.",
            checked = state.focusReviewMode,
            onCheckedChange = {
                state.focusReviewMode = it
                state.saveConfig()
            },
        )
        SettingsDivider()
        ChoiceSettingRow(
            title = "Default sort",
            description = "Choose how pull requests are ordered when a queue opens.",
            value = state.sortMode,
            options = listOf("Urgency", "Updated newest", "Updated oldest", "Repository", "Comments"),
            onSelected = {
                state.sortMode = it
                state.saveConfig()
            },
        )
        SettingsDivider()
        ChoiceSettingRow(
            title = "Stale after",
            description = "When a review request should receive waiting-time emphasis.",
            value = "${state.staleThresholdDaysText.ifBlank { "2" }} days",
            options = listOf("1", "2", "3", "5", "7", "14"),
            optionLabel = { "$it days" },
            onSelected = {
                state.staleThresholdDaysText = it
                state.saveConfig()
            },
        )
    }

    Surface(
        color = Color(0xFF1A1E22),
        border = BorderStroke(1.dp, Border),
        shape = RoundedCornerShape(14.dp),
    ) {
        Text(
            text = "Reviewed PRs stay out of Needs Review until GitHub reports a new update. RevQ stores that state locally.",
            color = TextMuted,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Composable
private fun ReminderSettings(state: AppState) {
    SettingsSectionHeader(
        title = "Reminders",
        subtitle = "Schedule persistent review reminders without turning RevQ into a notification stream.",
    )

    SettingsSectionLabel("SCHEDULE")
    SettingsGroup {
        ToggleSettingRow(
            title = "Scheduled reminders",
            description = "Show the persistent reminder window at the configured time.",
            checked = state.reminderEnabled,
            onCheckedChange = {
                state.reminderEnabled = it
                state.saveConfig()
            },
        )
        SettingsDivider()
        ChoiceSettingRow(
            title = "Reminder time",
            description = "Local time on this computer.",
            value = state.reminderTimeText,
            options = reminderTimeOptions(),
            onSelected = {
                state.reminderTimeText = it
                state.saveConfig()
            },
            enabled = state.reminderEnabled,
            scrollableMenu = true,
        )
        SettingsDivider()
        ReminderDaysRow(state)
        SettingsDivider()
        ChoiceSettingRow(
            title = "Snooze duration",
            description = "Default delay after pressing Snooze.",
            value = snoozeDisplay(state.reminderSnoozeMinutesText),
            options = listOf("15", "30", "60", "120", "240"),
            optionLabel = { snoozeDisplay(it) },
            onSelected = {
                state.reminderSnoozeMinutesText = it
                state.saveConfig()
            },
            enabled = state.reminderEnabled,
        )
    }

    SettingsSectionLabel("DELIVERY RULES")
    SettingsGroup {
        ToggleSettingRow(
            title = "Only when reviews are waiting",
            description = "Suppress the reminder window when the review queue is clear.",
            checked = state.remindOnlyWhenQueueNotClear,
            onCheckedChange = {
                state.remindOnlyWhenQueueNotClear = it
                state.saveConfig()
            },
            enabled = state.reminderEnabled,
        )
        SettingsDivider()
        ChoiceSettingRow(
            title = "Quiet hours",
            description = "Suppress scheduled reminder windows during this local-time range.",
            value = quietHoursDisplay(state.quietHoursText),
            options = listOf("Off", "18:00-08:00", "20:00-08:00", "22:00-07:00"),
            optionLabel = { quietHoursDisplay(it) },
            onSelected = {
                state.quietHoursText = it
                state.saveConfig()
            },
            enabled = state.reminderEnabled,
        )
    }

    SettingsSectionLabel("STATUS")
    SettingsGroup {
        StaticSettingRow(
            title = "Scheduler",
            value = state.reminderStatus,
        )
        state.reminderSnoozedUntil?.let {
            SettingsDivider()
            StaticSettingRow(
                title = "Snoozed until",
                value = formatReminderInstant(it),
                valueColor = Amber,
            )
        }
        state.reminderDismissedDate?.let {
            SettingsDivider()
            StaticSettingRow(
                title = "Dismissed for",
                value = it,
            )
        }
        SettingsDivider()
        ActionSettingRow(
            title = "Preview reminder window",
            description = "Open the reminder UI without changing today's reminder state.",
            actionLabel = "Preview",
            onClick = { state.previewReminderWindow() },
        )
    }
}

@Composable
private fun ReminderDaysRow(state: AppState) {
    val allDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val selectedDays = reminderDaySet(state.reminderDaysText)

    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = "Reminder days",
            color = if (state.reminderEnabled) TextPrimary else TextMuted,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = "Choose the days on which RevQ may schedule the reminder.",
            color = TextMuted,
            style = MaterialTheme.typography.bodySmall,
        )

        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            allDays.forEach { day ->
                val selected = day in selectedDays
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .clickable(enabled = state.reminderEnabled) {
                            val next = if (selected) selectedDays - day else selectedDays + day
                            if (next.isNotEmpty()) {
                                state.reminderDaysText = allDays.filter { it in next }.joinToString(",")
                                state.saveConfig()
                            }
                        },
                    color = if (selected) Olive.copy(alpha = 0.18f) else PanelElevated,
                    border = BorderStroke(1.dp, if (selected) Olive.copy(alpha = 0.55f) else Border),
                    shape = RoundedCornerShape(999.dp),
                ) {
                    Text(
                        text = day,
                        color = if (selected) Olive else TextMuted,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun DataSettings(state: AppState) {
    var displayDiagnosticsOpen by remember { mutableStateOf(false) }

    SettingsSectionHeader(
        title = "Data & diagnostics",
        subtitle = "Inspect local RevQ state and troubleshoot GitHub, caching, or display issues.",
    )

    SettingsSectionLabel("LOCAL DATA")
    SettingsGroup {
        StaticSettingRow("Cached pull requests", state.pullRequests.size.toString())
        SettingsDivider()
        StaticSettingRow("Reviewed records", state.handledReviewRecords.size.toString())
        SettingsDivider()
        StaticSettingRow("Pinned pull requests", state.pinnedPrKeys.size.toString())
        SettingsDivider()
        StaticSettingRow("Muted repositories", parseLines(state.mutedRepositoriesText).size.toString())
        SettingsDivider()
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SecondaryButton("Clear cache") { state.clearCache() }
            SecondaryButton("Clear reviewed state") { state.clearHandledReviews() }
        }
    }

    SettingsSectionLabel("GITHUB DIAGNOSTICS")
    SettingsGroup {
        StaticSettingRow("Platform", currentDesktopPlatform().displayName)
        SettingsDivider()
        StaticSettingRow("Executable", state.ghPathText.ifBlank { "Automatic detection pending" })
        SettingsDivider()
        StaticSettingRow("Detection source", state.ghDetectionSource)
        SettingsDivider()
        StaticSettingRow("Last refresh", state.lastRefreshFinishedAt?.let { relativeInstant(it) } ?: "Never")
        SettingsDivider()
        StaticSettingRow(
            "Last error",
            state.lastRefreshError ?: "None",
            valueColor = if (state.lastRefreshError == null) ReadyGreen else MaterialTheme.colorScheme.error,
        )
        SettingsDivider()
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SecondaryButton("Test connection", enabled = !state.isTestingGh) { state.testGithubCli() }
            SecondaryButton("Copy diagnostics") { state.copyDiagnostics() }
            SecondaryButton("Validate tracking") { state.validateTrackingText() }
        }
    }

    SettingsSectionLabel("DISPLAY DIAGNOSTICS")
    SettingsGroup {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { displayDiagnosticsOpen = !displayDiagnosticsOpen }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text("OS display scaling", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Text(
                    "RevQ follows Compose Desktop and operating-system display scaling automatically.",
                    color = TextMuted,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Text(
                text = if (displayDiagnosticsOpen) "Hide" else "Show",
                color = Olive,
                style = MaterialTheme.typography.labelMedium,
            )
        }

        if (displayDiagnosticsOpen) {
            val density = LocalDensity.current
            val toolkitScale = runCatching { Toolkit.getDefaultToolkit().screenResolution / 96f }.getOrDefault(1f)
            HorizontalDivider(Modifier, DividerDefaults.Thickness, color = Border)
            StaticSettingRow("Compose density", "${"%.2f".format(density.density)}x")
            SettingsDivider()
            StaticSettingRow("Font scale", "${"%.2f".format(density.fontScale)}x")
            SettingsDivider()
            StaticSettingRow("Toolkit DPI estimate", "${"%.2f".format(toolkitScale)}x")
        }
    }

    if (state.diagnosticsText.isNotBlank()) {
        Surface(
            color = Color(0xFF14181C),
            border = BorderStroke(1.dp, Border),
            shape = RoundedCornerShape(14.dp),
        ) {
            Text(
                text = state.diagnosticsText.take(1200),
                color = TextMuted,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}

@Composable
private fun SetupProgressCard(state: AppState) {
    val repositories = parseLines(state.repositoriesText)
    val organizations = parseLines(state.organizationsText)
    val ghReady = state.ghPathText.isNotBlank()
    val trackingReady = repositories.isNotEmpty() || organizations.isNotEmpty()

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF1B211D),
        border = BorderStroke(1.dp, Border),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = "Finish setting up RevQ",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "RevQ needs GitHub CLI and at least one repository or organization before the review queue can refresh.",
                color = TextMuted,
                style = MaterialTheme.typography.bodySmall,
            )
            SetupProgressRow(ghReady, "GitHub CLI", if (ghReady) "Detected via ${state.ghDetectionSource}" else "Not detected yet")
            SetupProgressRow(trackingReady, "Tracking", when {
                repositories.isNotEmpty() -> "${repositories.size} repositories configured"
                organizations.isNotEmpty() -> "${organizations.size} organizations configured"
                else -> "Add a repository or organization"
            })
        }
    }
}

@Composable
private fun SetupProgressRow(
    done: Boolean,
    title: String,
    detail: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(
            imageVector = if (done) Icons.Rounded.CheckCircle else Icons.Rounded.Search,
            contentDescription = null,
            tint = if (done) ReadyGreen else Amber,
            modifier = Modifier.size(18.dp),
        )
        Column {
            Text(title, color = TextPrimary, fontWeight = FontWeight.SemiBold)
            Text(detail, color = TextMuted, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun RevqStatusCard(state: AppState) {
    val repoCount = parseLines(state.repositoriesText).size

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF191D22),
        border = BorderStroke(1.dp, Border),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "RevQ status",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
            )
            SettingsMetaLine("GitHub CLI", state.ghDetectionSource)
            SettingsMetaLine("Tracking", "$repoCount repositories")
            SettingsMetaLine(
                "Auto refresh",
                if (state.autoRefreshEnabled) "Every ${state.autoRefreshIntervalMinutesText.ifBlank { "5" }} minutes" else "Off",
            )
            SettingsMetaLine(
                "Reminder",
                if (state.reminderEnabled) state.reminderStatus else "Disabled",
            )
        }
    }
}

@Composable
private fun SettingsSectionHeader(
    title: String,
    subtitle: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(
            text = title,
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = subtitle,
            color = TextMuted,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun SettingsSectionLabel(text: String) {
    Text(
        text = text,
        color = TextMuted,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 2.dp, bottom = 2.dp),
    )
}

@Composable
private fun SettingsGroup(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = SettingsGroupBg,
        border = BorderStroke(1.dp, Border),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(content = content)
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        thickness = DividerDefaults.Thickness,
        color = Border
    )
}

@Composable
private fun ToggleSettingRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                color = if (enabled) TextPrimary else TextMuted,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = description,
                color = TextMuted,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
        )
    }
}

@Composable
private fun <T> ChoiceSettingRow(
    title: String,
    description: String,
    value: String,
    options: List<T>,
    optionLabel: (T) -> String = { it.toString() },
    onSelected: (T) -> Unit,
    enabled: Boolean = true,
    scrollableMenu: Boolean = false,
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = enabled) { expanded = true }
                .padding(horizontal = 16.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = if (enabled) TextPrimary else TextMuted,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = description,
                    color = TextMuted,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = value,
                    color = if (enabled) Olive else TextMuted,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                )
                Icon(
                    imageVector = Icons.Rounded.ExpandMore,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(18.dp),
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .width(280.dp)
                .background(PanelBg),
        ) {
            val menuModifier = if (scrollableMenu) {
                Modifier.heightIn(max = 360.dp).verticalScroll(rememberScrollState())
            } else {
                Modifier
            }

            Column(modifier = menuModifier) {
                options.forEach { option ->
                    val label = optionLabel(option)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                expanded = false
                                onSelected(option)
                            }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = label,
                            color = TextPrimary,
                            modifier = Modifier.weight(1f),
                        )
                        if (label == value) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                tint = Olive,
                                modifier = Modifier.size(17.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionSettingRow(
    title: String,
    description: String,
    actionLabel: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    showProgress: Boolean = false,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, color = TextPrimary, fontWeight = FontWeight.SemiBold)
            Text(description, color = TextMuted, style = MaterialTheme.typography.bodySmall)
        }
        Button(
            onClick = onClick,
            enabled = enabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = PanelElevated,
                contentColor = TextPrimary,
            ),
            border = BorderStroke(1.dp, Border),
        ) {
            if (showProgress) {
                CircularProgressIndicator(
                    modifier = Modifier.size(15.dp),
                    color = Olive,
                    strokeWidth = 2.dp,
                )
                Spacer(Modifier.width(7.dp))
            }
            Text(actionLabel)
        }
    }
}

@Composable
private fun StaticSettingRow(
    title: String,
    value: String,
    valueColor: Color = TextMuted,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Text(
            text = title,
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            color = valueColor,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.widthIn(max = 520.dp),
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun SettingsMetaLine(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = label,
            color = TextMuted,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.width(110.dp),
        )
        Text(
            text = value,
            color = TextPrimary,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun TrackingList(
    values: List<String>,
    emptyMessage: String,
    onRemove: (String) -> Unit,
    removeLabel: String = "Remove",
) {
    if (values.isEmpty()) {
        Text(
            text = emptyMessage,
            color = TextMuted,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
        )
        return
    }

    values.forEachIndexed { index, value ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(PanelElevated),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Folder,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(16.dp),
                )
            }
            Spacer(Modifier.width(10.dp))
            Text(
                text = value,
                color = TextPrimary,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            TextButton(onClick = { onRemove(value) }) {
                Text(removeLabel, color = TextMuted)
            }
        }
        if (index != values.lastIndex) SettingsDivider()
    }
}

@Composable
private fun InlineAddRow(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    actionLabel: String,
    onAdd: () -> Unit,
    state: AppState? = null,
) {
    Row(
        modifier = Modifier.padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder) },
            singleLine = true,
            modifier = Modifier
                .weight(1f)
                .onFocusChanged { focusState ->
                    if (state != null) {
                        state.keyboardMode = if (focusState.isFocused) {
                            KeyboardMode.Insert
                        } else {
                            KeyboardMode.Normal
                        }
                    }
                },
            colors = revqTextFieldColors(),
        )
        Button(
            onClick = onAdd,
            enabled = value.isNotBlank(),
        ) {
            Text(actionLabel)
        }
    }
}

@Composable
private fun SecondaryButton(
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = PanelElevated,
            contentColor = TextPrimary,
        ),
        border = BorderStroke(1.dp, Border),
    ) {
        Text(text)
    }
}

private fun isSetupIncomplete(state: AppState): Boolean {
    val trackingMissing = parseLines(state.repositoriesText).isEmpty() && parseLines(state.organizationsText).isEmpty()
    return state.ghPathText.isBlank() || trackingMissing
}

private fun platformDetectionCopy(platform: DesktopPlatform): String = when (platform) {
    DesktopPlatform.Linux -> "Checks PATH, executable lookup, login shells, Linuxbrew, Snap, and common Linux locations."
    DesktopPlatform.MacOS -> "Checks PATH, executable lookup, login shells, Homebrew, and common macOS locations."
    DesktopPlatform.Windows -> "Checks PATH, where.exe, Chocolatey, Scoop, and common Windows install locations."
    DesktopPlatform.Unknown -> "Checks the system PATH and common executable names."
}

private fun platformGhPathExample(): String = when (currentDesktopPlatform()) {
    DesktopPlatform.Linux -> "/usr/bin/gh"
    DesktopPlatform.MacOS -> "/opt/homebrew/bin/gh"
    DesktopPlatform.Windows -> "C:\\Program Files\\GitHub CLI\\gh.exe"
    DesktopPlatform.Unknown -> "Full path to gh executable"
}

fun reminderTimeOptions(): List<String> {
    return (6..22).flatMap { hour ->
        listOf("%02d:00".format(hour), "%02d:30".format(hour))
    }
}

private fun reminderDaySet(raw: String): Set<String> {
    val all = linkedSetOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val lower = raw.lowercase()
    if (lower.isBlank() || lower.contains("daily") || lower.contains("every")) return all
    if (lower.contains("mon-fri") || lower.contains("weekday") || lower.contains("workday")) {
        return linkedSetOf("Mon", "Tue", "Wed", "Thu", "Fri")
    }
    return all.filterTo(linkedSetOf()) { day -> lower.contains(day.lowercase()) }
        .ifEmpty { linkedSetOf("Mon", "Tue", "Wed", "Thu", "Fri") }
}

private fun snoozeDisplay(minutes: String): String {
    val value = minutes.toIntOrNull() ?: 60
    return when {
        value < 60 -> "$value min"
        value == 60 -> "1 hour"
        value % 60 == 0 -> "${value / 60} hours"
        else -> "$value min"
    }
}

private fun quietHoursDisplay(value: String): String {
    if (value.equals("Off", ignoreCase = true) || value.equals("Disabled", ignoreCase = true)) return "Off"
    val parts = value.split("-", limit = 2)
    return if (parts.size == 2) "${parts[0]} → ${parts[1]}" else value
}
