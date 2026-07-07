package eu.revq

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.KeyboardCommandKey
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.Typography
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import kotlinx.coroutines.Job
import androidx.compose.ui.window.application
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.awt.Desktop
import java.awt.MenuItem
import java.awt.PopupMenu
import java.awt.SystemTray
import java.awt.Toolkit
import java.awt.TrayIcon
import java.awt.datatransfer.StringSelection
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.DayOfWeek
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.concurrent.TimeUnit
import kotlin.io.path.exists
import kotlin.io.path.readLines
import kotlin.io.path.writeLines
import kotlin.math.max
import kotlin.system.exitProcess
import eu.revq.commands.CommandId
import eu.revq.commands.CommandExecutor
import eu.revq.keyboard.FocusRegion
import eu.revq.keyboard.KeyboardAction
import eu.revq.keyboard.KeyboardContext
import eu.revq.keyboard.KeyboardMode
import eu.revq.keyboard.KeyboardRouter
import eu.revq.keyboard.keyboardHints
import eu.revq.keyboard.nextKeyboardAction
import eu.revq.ui.commandpalette.CommandPalette
import eu.revq.ui.commandpalette.CommandPaletteState

private val AppBg = Color(0xFF101215)
internal val SidebarBg = Color(0xFF15181C)
internal val PanelBg = Color(0xFF1B1F24)
internal val PanelElevated = Color(0xFF252A31)
internal val Border = Color(0xFF363C45)
internal val TextPrimary = Color(0xFFECE8DD)
internal val TextMuted = Color(0xFF9C988E)
internal val Olive = Color(0xFFA7C46A)
private val OliveSoft = Color(0xFF2C3323)
internal val Amber = Color(0xFFE2B45C)
private val Rose = Color(0xFFE06A6A)
internal val ReadyGreen = Color(0xFF75C78C)
internal val InfoBlue = Color(0xFF7AA7E8)
private val HandledMuted = Color(0xFF6F747D)

private val RevqColorScheme = darkColorScheme(
    background = AppBg,
    surface = PanelBg,
    surfaceVariant = PanelElevated,
    primary = Olive,
    onPrimary = Color(0xFF12140F),
    secondary = Amber,
    onSecondary = Color(0xFF17130A),
    error = Rose,
    onError = Color(0xFF1A0E10),
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextMuted,
    outline = Border,
)

private val RevqTypography = Typography(
    headlineSmall = TextStyle(fontSize = 27.sp, lineHeight = 34.sp, fontWeight = FontWeight.Bold),
    titleLarge = TextStyle(fontSize = 23.sp, lineHeight = 30.sp, fontWeight = FontWeight.Bold),
    titleMedium = TextStyle(fontSize = 18.sp, lineHeight = 24.sp, fontWeight = FontWeight.SemiBold),
    titleSmall = TextStyle(fontSize = 16.sp, lineHeight = 22.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 23.sp),
    bodyMedium = TextStyle(fontSize = 15.sp, lineHeight = 22.sp),
    bodySmall = TextStyle(fontSize = 13.5f.sp, lineHeight = 19.sp),
    labelLarge = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold),
    labelMedium = TextStyle(fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.SemiBold),
    labelSmall = TextStyle(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.SemiBold),
)

@Composable
fun RevqTheme(
    uiScale: Float = 1f,
    content: @Composable () -> Unit,
) {
    // Compose Desktop already receives the operating-system display scale through
    // LocalDensity. RevQ must not apply a second manual multiplier, otherwise 4K
    // displays become inconsistent and the sidebar can overflow. The parameter is
    // kept for old saved configs, but the effective scale is always OS-driven.
    MaterialTheme(colorScheme = RevqColorScheme, typography = RevqTypography, content = content)
}

@Composable
fun revqTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    focusedLabelColor = Olive,
    unfocusedLabelColor = TextMuted,
    focusedPlaceholderColor = TextMuted,
    unfocusedPlaceholderColor = TextMuted,
    focusedBorderColor = Olive,
    unfocusedBorderColor = Border,
    cursorColor = Olive,
    focusedContainerColor = Color(0xFF15181C),
    unfocusedContainerColor = Color(0xFF15181C),
)

fun main() = application {
    val appState = remember { AppState() }
    val mainWindowState = remember { WindowState(size = DpSize(1680.dp, 1040.dp), position = WindowPosition.Aligned(Alignment.Center)) }

    LaunchedEffect(Unit) {
        appState.loadFromDisk()
        installBestEffortTray(appState)
        appState.startReminderScheduler()
        appState.startAutoRefreshScheduler()
        if (appState.repositoriesText.isNotBlank()) {
            appState.refresh()
        } else {
            appState.statusLine = "Complete setup to start your review queue"
            appState.view = View.Settings
        }
    }

    Window(
        onCloseRequest = ::exitApplication,
        state = mainWindowState,
        title = "RevQ",
    ) {
        RevqTheme(uiScale = appState.uiScale) {
            RevqApp(appState)
        }
    }

    if (appState.showReminderWindow) {
        Window(
            onCloseRequest = { appState.closeReminderWindow() },
            title = "RevQ Review Reminder",
            undecorated = true,
            alwaysOnTop = true,
            resizable = false,
            state = WindowState(
                size = DpSize(840.dp, 700.dp),
                position = WindowPosition.Aligned(Alignment.Center),
            ),
        ) {
            RevqTheme(uiScale = appState.uiScale) {
                ReminderWindow(appState)
            }
        }
    }
}

enum class View(val label: String, val description: String) {
    Today("Today", "Review requests first, then your own PRs as a separate section."),
    NeedsReview("Needs Review", "PRs waiting on you. Start here."),
    Pinned("Pinned", "Your manually pinned pull requests."),
    Mine("My Pull Requests", "Your own open PRs, kept separate from PRs waiting on your review."),
    Blocked("Blocked", "Your PRs that look stuck."),
    Ready("Ready", "PRs that may be ready to move forward."),
    Handled("Handled", "Reviews you handled locally until they change again."),
    Settings("Settings", "Configure GitHub tracking and reminders."),
}

enum class PullRequestSource { ReviewRequest, Mine }
enum class AttentionKind { Review, Action, Blocked, Ready, Quiet }

enum class OwnPullRequestStatus {
    Draft,
    ChangesRequested,
    MergeConflict,
    ChecksFailing,
    DiscussionNeedsResponse,
    ApprovedAndReady,
    WaitingForReviewer,
    NoActionNeeded,
}

data class RepositoryId(val owner: String, val name: String) {
    override fun toString(): String = "$owner/$name"
}

data class PullRequest(
    val repository: RepositoryId,
    val number: Int,
    val title: String,
    val url: String,
    val updatedAt: String?,
    val comments: Int = 0,
    val source: PullRequestSource,
    val authorLogin: String? = null,
    val isDraft: Boolean = false,
    val reviewDecision: String? = null,
    val mergeable: String? = null,
    val mergeStateStatus: String? = null,
    val reviewRequestsCount: Int = 0,
    val checksTotal: Int = 0,
    val checksFailing: Int = 0,
    val checksPending: Int = 0,
    val unresolvedDiscussionCount: Int? = null,
    val requestedReviewers: List<String> = emptyList(),
    val changeRequestReviewers: List<String> = emptyList(),
    val approvingReviewers: List<String> = emptyList(),
    val unresolvedDiscussionAuthors: List<String> = emptyList(),
) {
    val key: String get() = "${repository.owner}/${repository.name}#$number"
    val updatedMarker: String get() = updatedAt ?: "unknown"
    val discussionNeedsResponse: Boolean get() = (unresolvedDiscussionCount ?: 0) > 0
}

data class DiscussionInsight(
    val unresolvedCount: Int,
    val authors: List<String>,
)

data class WorkerResult<T>(val value: T? = null, val error: String? = null)
data class HandledUndo(val pullRequest: PullRequest, val marker: String)

class AppState {
    private val scope = MainScope()
    private val configDir: Path = Paths.get(System.getProperty("user.home"), ".config", "revq-compose")
    private val reposFile: Path = configDir.resolve("repositories.txt")
    private val orgsFile: Path = configDir.resolve("organizations.txt")
    private val ghPathFile: Path = configDir.resolve("gh-path.txt")
    private val ghDetectionSourceFile: Path = configDir.resolve("gh-detection-source.txt")
    private val handledFile: Path = configDir.resolve("handled-reviews.txt")
    private val uiScaleFile: Path = configDir.resolve("ui-scale.txt")
    private val cacheFile: Path = configDir.resolve("pull-requests-cache.tsv")
    private val pinnedFile: Path = configDir.resolve("pinned-prs.txt")
    private val mutedRepositoriesFile: Path = configDir.resolve("muted-repositories.txt")
    private val autoRefreshEnabledFile: Path = configDir.resolve("auto-refresh-enabled.txt")
    private val autoRefreshIntervalFile: Path = configDir.resolve("auto-refresh-interval-minutes.txt")
    private val sortModeFile: Path = configDir.resolve("sort-mode.txt")
    private val groupByRepositoryFile: Path = configDir.resolve("group-by-repository.txt")
    private val staleThresholdDaysFile: Path = configDir.resolve("stale-threshold-days.txt")
    private val compactRowsFile: Path = configDir.resolve("compact-rows.txt")
    private val focusReviewModeFile: Path = configDir.resolve("focus-review-mode.txt")
    private val reminderEnabledFile: Path = configDir.resolve("reminder-enabled.txt")
    private val reminderTimeFile: Path = configDir.resolve("reminder-time.txt")
    private val reminderDaysFile: Path = configDir.resolve("reminder-days.txt")
    private val reminderQuietHoursFile: Path = configDir.resolve("reminder-quiet-hours.txt")
    private val reminderOnlyWhenQueueFile: Path = configDir.resolve("reminder-only-when-queue.txt")
    private val reminderSnoozeMinutesFile: Path = configDir.resolve("reminder-snooze-minutes.txt")
    private val reminderSnoozedUntilFile: Path = configDir.resolve("reminder-snoozed-until.txt")
    private val reminderDismissedDateFile: Path = configDir.resolve("reminder-dismissed-date.txt")
    private var reminderSchedulerJob: Job? = null
    private var autoRefreshJob: Job? = null

    var view by mutableStateOf(View.NeedsReview)
    var searchQuery by mutableStateOf("")
    var repositoriesText by mutableStateOf("")
    var organizationsText by mutableStateOf("")
    var ghPathText by mutableStateOf("")
    var pullRequests by mutableStateOf(emptyList<PullRequest>())
    var selectedPullRequest by mutableStateOf<PullRequest?>(null)
    var handledReviewRecords by mutableStateOf(emptyMap<String, String>())
    var isRefreshing by mutableStateOf(false)
    var isDiscovering by mutableStateOf(false)
    var lastRefreshStartedAt by mutableStateOf<Instant?>(null)
    var lastRefreshFinishedAt by mutableStateOf<Instant?>(null)
    var lastRefreshError by mutableStateOf<String?>(null)
    var statusLine by mutableStateOf("Ready")
    var reviewSessionActive by mutableStateOf(false)
    var focusReviewMode by mutableStateOf(false)
    var showReminderWindow by mutableStateOf(false)
    var isTestingGh by mutableStateOf(false)
    var ghTestResult by mutableStateOf<String?>(null)
    var ghDetectionSource by mutableStateOf("Not detected")
    var refreshPhase by mutableStateOf("Idle")
    var refreshDone by mutableStateOf(0)
    var refreshTotal by mutableStateOf(0)
    var lastUndoReview by mutableStateOf<HandledUndo?>(null)
    var reminderEnabled by mutableStateOf(true)
    var reminderTimeText by mutableStateOf("09:00")
    var reminderDaysText by mutableStateOf("Mon-Fri")
    var quietHoursText by mutableStateOf("18:00-08:00")
    var remindOnlyWhenQueueNotClear by mutableStateOf(true)
    var reminderSnoozeMinutesText by mutableStateOf("60")
    var reminderSnoozedUntil by mutableStateOf<Instant?>(null)
    var reminderDismissedDate by mutableStateOf<String?>(null)
    var nextReminderAt by mutableStateOf<Instant?>(null)
    var reminderStatus by mutableStateOf("Reminder not scheduled yet")
    var reminderWindowIsPreview by mutableStateOf(false)
    var uiScale by mutableStateOf(1.0f)
    var densityMode by mutableStateOf("OS automatic")
    var pinnedPrKeys by mutableStateOf(emptySet<String>())
    var mutedRepositoriesText by mutableStateOf("")
    var autoRefreshEnabled by mutableStateOf(true)
    var autoRefreshIntervalMinutesText by mutableStateOf("5")
    var sortMode by mutableStateOf("Urgency")
    var groupByRepository by mutableStateOf(false)
    var staleThresholdDaysText by mutableStateOf("2")
    var compactRows by mutableStateOf(false)
    var diagnosticsText by mutableStateOf("")
    var sessionHandledCount by mutableStateOf(0)
    var reviewSessionQueueKeys by mutableStateOf<List<String>>(emptyList())
    var recentCommandIds by mutableStateOf<List<CommandId>>(emptyList())
        private set

    // Keyboard-navigation state. Palette state is kept at the UI shell level.
    var keyboardMode by mutableStateOf(KeyboardMode.Normal)
    var keyboardFocusRegion by mutableStateOf(FocusRegion.PullRequestList)
    var sidebarKeyboardView by mutableStateOf(View.NeedsReview)
    var keyboardPageStep by mutableStateOf(6)
    var settingsSectionIndex by mutableStateOf(0)
    var settingsFocusedRowIndex by mutableStateOf(0)

    fun loadFromDisk() {
        Files.createDirectories(configDir)
        repositoriesText = reposFile.safeLines().joinToString("\n")
        organizationsText = orgsFile.safeLines().joinToString("\n")
        ghPathText = ghPathFile.safeLines().firstOrNull().orEmpty()
        GhClient.configureExecutable(ghPathText)
        if (ghPathText.isBlank()) {
            GhClient.detectExecutableResult()?.let { detected ->
                ghPathText = detected.executable
                ghDetectionSource = detected.source.label
                GhClient.configureExecutable(detected.executable)
            } ?: run {
                ghDetectionSource = "Not detected"
            }
        } else {
            ghDetectionSource = ghDetectionSourceFile.safeLines().firstOrNull()?.ifBlank { null }
                ?: GhDetectionSource.Configured.label
        }
        uiScale = 1.0f
        densityMode = "OS automatic"
        Files.deleteIfExists(uiScaleFile)
        reminderEnabled = reminderEnabledFile.safeLines().firstOrNull()?.toBooleanStrictOrNull() ?: true
        reminderTimeText = reminderTimeFile.safeLines().firstOrNull()?.ifBlank { null } ?: "09:00"
        reminderDaysText = reminderDaysFile.safeLines().firstOrNull()?.ifBlank { null } ?: "Mon-Fri"
        quietHoursText = reminderQuietHoursFile.safeLines().firstOrNull()?.ifBlank { null } ?: "18:00-08:00"
        remindOnlyWhenQueueNotClear = reminderOnlyWhenQueueFile.safeLines().firstOrNull()?.toBooleanStrictOrNull() ?: true
        reminderSnoozeMinutesText = reminderSnoozeMinutesFile.safeLines().firstOrNull()?.ifBlank { null } ?: "60"
        reminderSnoozedUntil = reminderSnoozedUntilFile.safeLines().firstOrNull()?.let { runCatching { Instant.parse(it) }.getOrNull() }
        reminderDismissedDate = reminderDismissedDateFile.safeLines().firstOrNull()?.ifBlank { null }
        handledReviewRecords = handledFile.safeLines()
            .mapNotNull { line ->
                val parts = line.split("\t", limit = 2)
                if (parts.size == 2) parts[0] to parts[1] else null
            }
            .toMap()
        pullRequests = dedupePullRequests(loadCache())
        pinnedPrKeys = pinnedFile.safeLines().toSet()
        mutedRepositoriesText = mutedRepositoriesFile.safeLines().joinToString("\n")
        autoRefreshEnabled = autoRefreshEnabledFile.safeLines().firstOrNull()?.toBooleanStrictOrNull() ?: true
        autoRefreshIntervalMinutesText = autoRefreshIntervalFile.safeLines().firstOrNull()?.ifBlank { null } ?: "5"
        sortMode = sortModeFile.safeLines().firstOrNull()?.ifBlank { null } ?: "Urgency"
        groupByRepository = groupByRepositoryFile.safeLines().firstOrNull()?.toBooleanStrictOrNull() ?: false
        staleThresholdDaysText = staleThresholdDaysFile.safeLines().firstOrNull()?.ifBlank { null } ?: "2"
        compactRows = compactRowsFile.safeLines().firstOrNull()?.toBooleanStrictOrNull() ?: false
        focusReviewMode = focusReviewModeFile.safeLines().firstOrNull()?.toBooleanStrictOrNull() ?: false
    }

    fun saveConfig() {
        Files.createDirectories(configDir)
        reposFile.writeLines(parseLines(repositoriesText))
        orgsFile.writeLines(parseLines(organizationsText))
        val ghPath = ghPathText.trim()
        if (ghPath.isBlank()) {
            Files.deleteIfExists(ghPathFile)
        } else {
            ghPathFile.writeLines(listOf(ghPath))
        }
        ghDetectionSourceFile.writeLines(listOf(ghDetectionSource))
        Files.deleteIfExists(uiScaleFile)
        mutedRepositoriesFile.writeLines(parseLines(mutedRepositoriesText))
        autoRefreshEnabledFile.writeLines(listOf(autoRefreshEnabled.toString()))
        autoRefreshIntervalFile.writeLines(listOf(autoRefreshIntervalMinutesText.trim().ifBlank { "5" }))
        sortModeFile.writeLines(listOf(sortMode.ifBlank { "Urgency" }))
        groupByRepositoryFile.writeLines(listOf(groupByRepository.toString()))
        staleThresholdDaysFile.writeLines(listOf(staleThresholdDaysText.trim().ifBlank { "2" }))
        compactRowsFile.writeLines(listOf(compactRows.toString()))
        focusReviewModeFile.writeLines(listOf(focusReviewMode.toString()))
        saveReminderState()
        GhClient.configureExecutable(ghPath)
        startReminderScheduler()
        startAutoRefreshScheduler()
        statusLine = "Settings saved · using OS display scaling · ${reminderStatus}"
    }


    fun setUiScale(scale: Float, label: String) {
        uiScale = 1.0f
        densityMode = "OS automatic"
        Files.deleteIfExists(uiScaleFile)
        statusLine = "RevQ uses the operating-system display scale automatically"
    }

    fun autoDetectGithubCli() {
        val detected = GhClient.detectExecutableResult()
        if (detected == null) {
            ghDetectionSource = "Not detected"
            ghTestResult = "Could not auto-detect GitHub CLI on ${currentDesktopPlatform().displayName}. Install gh or make it available through your system PATH."
            statusLine = "GitHub CLI auto-detect failed"
            return
        }
        ghPathText = detected.executable
        ghDetectionSource = detected.source.label
        GhClient.configureExecutable(detected.executable)
        saveConfig()
        statusLine = "Detected GitHub CLI via ${detected.source.label}"
    }

    fun removeRepository(repository: String) {
        repositoriesText = parseLines(repositoriesText).filterNot { it == repository }.joinToString("\n")
        saveConfig()
        statusLine = "Removed $repository"
    }

    fun removeOrganization(organization: String) {
        organizationsText = parseLines(organizationsText).filterNot { it == organization }.joinToString("\n")
        saveConfig()
        statusLine = "Removed organization $organization"
    }

    fun testGithubCli() {
        if (isTestingGh) return
        GhClient.configureExecutable(ghPathText)
        isTestingGh = true
        ghTestResult = null
        statusLine = "Testing GitHub CLI…"
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching { GhClient.testConnection() }
                    .fold(
                        onSuccess = { WorkerResult(value = it) },
                        onFailure = { WorkerResult(error = it.message ?: it.toString()) },
                    )
            }
            isTestingGh = false
            ghTestResult = result.value ?: result.error
            statusLine = result.value ?: "GitHub CLI test failed"
        }
    }

    fun saveHandled() {
        Files.createDirectories(configDir)
        handledFile.writeLines(handledReviewRecords.entries.map { "${it.key}\t${it.value}" })
    }

    fun refresh(showReminderAfterRefresh: Boolean = false) {
        if (isRefreshing) return

        val repos = parseLines(repositoriesText)
        if (repos.isEmpty()) {
            val orgs = parseLines(organizationsText)
            if (orgs.isNotEmpty()) {
                discoverRepositoriesThenRefresh(orgs, showReminderAfterRefresh)
                return
            }

            view = View.Settings
            lastRefreshError = "Add at least one repository in Settings, or add an organization and use Discover repositories."
            statusLine = "No repositories configured"
            return
        }

        refreshRepositories(repos, showReminderAfterRefresh)
    }

    private fun refreshRepositories(repos: List<String>, showReminderAfterRefresh: Boolean = false) {
        GhClient.configureExecutable(ghPathText)
        isRefreshing = true
        isDiscovering = false
        lastRefreshStartedAt = Instant.now()
        lastRefreshError = null
        lastUndoReview = null
        refreshTotal = repos.size
        refreshDone = 0
        refreshPhase = "Checking GitHub CLI…"
        statusLine = "Checking GitHub CLI…"

        scope.launch {
            val startedAt = System.currentTimeMillis()
            val collected = mutableListOf<PullRequest>()
            try {
                val login = withContext(Dispatchers.IO) { GhClient.prepareRefresh() }
                repos.forEachIndexed { index, repo ->
                    refreshDone = index
                    refreshPhase = "Fetching $repo…"
                    statusLine = "Refreshing ${index + 1} / ${repos.size} repositories…"
                    val repoPullRequests = withContext(Dispatchers.IO) { GhClient.refreshRepository(repo, login) }
                    collected += repoPullRequests
                    refreshDone = index + 1
                }
                finishRefresh(startedAt, WorkerResult(value = dedupePullRequests(collected)), showReminderAfterRefresh = showReminderAfterRefresh)
            } catch (error: Throwable) {
                finishRefresh(startedAt, WorkerResult<List<PullRequest>>(error = error.message ?: error.toString()), showReminderAfterRefresh = showReminderAfterRefresh)
            }
        }
    }

    private fun discoverRepositoriesThenRefresh(orgs: List<String>, showReminderAfterRefresh: Boolean = false) {
        GhClient.configureExecutable(ghPathText)
        isRefreshing = true
        isDiscovering = true
        lastRefreshStartedAt = Instant.now()
        lastRefreshError = null
        refreshTotal = 0
        refreshDone = 0
        refreshPhase = "Discovering repositories…"
        statusLine = "Discovering repositories before refresh…"

        scope.launch {
            val startedAt = System.currentTimeMillis()
            try {
                val discoveredRepositories = withContext(Dispatchers.IO) { GhClient.discoverRepositories(orgs) }
                if (discoveredRepositories.isEmpty()) error("No repositories were discovered from the configured organizations.")
                repositoriesText = discoveredRepositories.joinToString("\n")
                saveConfig()

                val login = withContext(Dispatchers.IO) { GhClient.prepareRefresh() }
                val collected = mutableListOf<PullRequest>()
                isDiscovering = false
                refreshTotal = discoveredRepositories.size
                discoveredRepositories.forEachIndexed { index, repo ->
                    refreshDone = index
                    refreshPhase = "Fetching $repo…"
                    statusLine = "Refreshing ${index + 1} / ${discoveredRepositories.size} repositories…"
                    val repoPullRequests = withContext(Dispatchers.IO) { GhClient.refreshRepository(repo, login) }
                    collected += repoPullRequests
                    refreshDone = index + 1
                }

                finishRefresh(
                    startedAt,
                    WorkerResult(value = dedupePullRequests(collected)),
                    successPrefix = "Discovered ${discoveredRepositories.size} repositories · refreshed just now",
                    showReminderAfterRefresh = showReminderAfterRefresh,
                )
            } catch (error: Throwable) {
                finishRefresh(startedAt, WorkerResult<List<PullRequest>>(error = error.message ?: error.toString()), showReminderAfterRefresh = showReminderAfterRefresh)
            }
        }
    }

    private suspend fun finishRefresh(
        startedAt: Long,
        result: WorkerResult<List<PullRequest>>,
        successPrefix: String = "Refreshed just now",
        showReminderAfterRefresh: Boolean = false,
    ) {
        val minVisibleMs = 650L
        val elapsed = System.currentTimeMillis() - startedAt
        if (elapsed < minVisibleMs) delay(minVisibleMs - elapsed)

        isRefreshing = false
        isDiscovering = false
        lastRefreshFinishedAt = Instant.now()
        refreshPhase = "Idle"
        if (result.error != null) {
            lastRefreshError = result.error
            statusLine = "GitHub refresh failed · ${result.error}"
        } else {
            pullRequests = dedupePullRequests(result.value.orEmpty())
            saveCache()
            selectedPullRequest = selectedPullRequest?.let { selected ->
                pullRequests.firstOrNull { candidate ->
                    candidate.key == selected.key && candidate.source == selected.source
                }
            }
            val reviews = reviewQueue().size
            statusLine = "$successPrefix · $reviews reviews waiting"
            if (showReminderAfterRefresh) {
                if (shouldShowReminderWindowNow()) {
                    showScheduledReminderWindow()
                    statusLine = "Reminder shown · $reviews reviews waiting"
                } else {
                    statusLine = if (reviews == 0) "Reminder checked · review queue clear" else "Reminder checked · reminder suppressed by settings"
                }
            }
        }
    }

    fun discoverTargets() {
        if (isDiscovering) return
        GhClient.configureExecutable(ghPathText)
        val orgs = parseLines(organizationsText)
        if (orgs.isEmpty()) {
            statusLine = "Add one or more organizations before discovering repositories."
            return
        }
        isDiscovering = true
        ghTestResult = null
        statusLine = "Discovering repositories…"
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching { GhClient.discoverRepositories(orgs) }
                    .fold(
                        onSuccess = { WorkerResult(value = it) },
                        onFailure = { WorkerResult(error = it.message ?: it.toString()) },
                    )
            }
            isDiscovering = false
            if (result.error != null) {
                statusLine = "Discover failed · ${result.error}"
            } else {
                val merged = (parseLines(repositoriesText) + result.value.orEmpty()).distinct().sorted()
                repositoriesText = merged.joinToString("\n")
                saveConfig()
                statusLine = "Discovered ${result.value.orEmpty().size} repositories"
            }
        }
    }

    fun visiblePullRequests(): List<PullRequest> {
        val query = searchQuery.trim().lowercase()
        val effectiveView = if (focusReviewMode && view != View.Settings && view != View.Handled && view != View.Pinned) View.NeedsReview else view
        val base = when (effectiveView) {
            View.Today -> todayPullRequests()
            View.NeedsReview -> reviewQueue()
            View.Pinned -> pinnedPullRequests()
            View.Mine -> activePullRequests().filter { it.source == PullRequestSource.Mine }
            View.Blocked -> activePullRequests().filter { attentionKind(it) == AttentionKind.Blocked }
            View.Ready -> activePullRequests().filter { attentionKind(it) == AttentionKind.Ready }
            View.Handled -> handledPullRequests()
            View.Settings -> emptyList()
        }
        return base
            .filter { query.isEmpty() || it.title.lowercase().contains(query) || it.repository.toString().lowercase().contains(query) || it.number.toString().contains(query) }
            .let { sortPullRequests(effectiveView, it) }
    }

    fun activePullRequests(): List<PullRequest> = pullRequests.filterNot { isRepositoryMuted(it.repository.toString()) }

    fun pinnedPullRequests(): List<PullRequest> = activePullRequests()
        .filter { pinnedPrKeys.contains(it.key) }
        .let { sortPullRequests(View.Pinned, it) }

    fun handledPullRequests(): List<PullRequest> = activePullRequests()
        .filter { it.source == PullRequestSource.ReviewRequest }
        .filter { isHandledCurrent(it) }
        .let { sortPullRequests(View.Handled, it) }

    fun reviewQueue(): List<PullRequest> = activePullRequests()
        .filter { it.source == PullRequestSource.ReviewRequest }
        .filterNot { isHandledCurrent(it) }
        .let { sortPullRequests(View.NeedsReview, it) }

    fun todayPullRequests(): List<PullRequest> {
        val needsReview = reviewQueue()
        val myAction = activePullRequests().filter {
            it.source == PullRequestSource.Mine && ownPullRequestNeedsAction(it)
        }
        val ready = activePullRequests().filter {
            it.source == PullRequestSource.Mine &&
                    ownPullRequestPrimaryStatus(it) == OwnPullRequestStatus.ApprovedAndReady
        }
        return sortPullRequests(View.Today, dedupePullRequests(needsReview + myAction + ready))
    }

    fun startReviewing() {
        val queue = reviewQueue()
        view = View.NeedsReview
        reviewSessionActive = true
        reviewSessionQueueKeys = queue.map { it.key }
        sessionHandledCount = 0
        selectedPullRequest = queue.firstOrNull()
        statusLine = if (selectedPullRequest == null) "Review queue clear" else "Review session started"
    }

    fun endReviewSession() {
        reviewSessionActive = false
        reviewSessionQueueKeys = emptyList()
        sessionHandledCount = 0
        statusLine = "Review session ended"
    }

    fun nextReview() {
        val reviews = reviewQueue()
        val current = selectedPullRequest

        selectedPullRequest = when {
            reviews.isEmpty() -> null
            current == null -> reviews.firstOrNull()
            else -> {
                val index = reviews.indexOfFirst { it.key == current.key }
                when {
                    index < 0 -> reviews.firstOrNull()
                    index < reviews.lastIndex -> reviews[index + 1]
                    reviews.size > 1 -> reviews.first()
                    else -> current
                }
            }
        }

        if (selectedPullRequest == null) {
            statusLine = "Review session complete"
        }
    }

    fun previousReview() {
        val reviews = reviewQueue()
        val current = selectedPullRequest ?: return
        val index = reviews.indexOfFirst { it.key == current.key }
        selectedPullRequest = reviews.getOrNull(max(0, index - 1))
    }

    fun markReviewed(pr: PullRequest? = selectedPullRequest) {
        pr ?: return

        val queueBefore = reviewQueue()
        val currentIndex = queueBefore.indexOfFirst {
            it.key == pr.key && it.source == pr.source
        }
        val preferredNextKeys = if (currentIndex >= 0) {
            queueBefore
                .drop(currentIndex + 1)
                .plus(queueBefore.take(currentIndex))
                .map { it.key }
        } else {
            emptyList()
        }

        handledReviewRecords = handledReviewRecords + (pr.key to pr.updatedMarker)
        lastUndoReview = HandledUndo(pr, pr.updatedMarker)
        saveHandled()

        if (reviewSessionActive) {
            sessionHandledCount += 1
        }

        statusLine = "Marked reviewed · ${pr.repository} #${pr.number}"

        val remaining = reviewQueue()
        selectedPullRequest = preferredNextKeys.firstNotNullOfOrNull { key ->
            remaining.firstOrNull { it.key == key }
        } ?: remaining.firstOrNull()

        if (remaining.isEmpty()) {
            statusLine = "Review queue clear"
        }
    }

    fun undoMarkReviewed() {
        val undo = lastUndoReview ?: return
        handledReviewRecords = handledReviewRecords - undo.pullRequest.key
        lastUndoReview = null
        selectedPullRequest = undo.pullRequest
        saveHandled()
        statusLine = "Restored ${undo.pullRequest.repository} #${undo.pullRequest.number}"
    }

    fun openSelectedInGitHub() {
        selectedPullRequest?.let { openUrl(it.url) }
    }

    fun copySelectedUrl() {
        val pr = selectedPullRequest ?: return
        copyToClipboard(pr.url)
        statusLine = "Copied ${pr.repository} #${pr.number} URL"
    }

    fun openSelectedRepository() {
        val pr = selectedPullRequest ?: return
        openUrl("https://github.com/${pr.repository}")
    }

    fun toggleFocusMode() {
        focusReviewMode = !focusReviewMode
        selectedPullRequest = null
        if (focusReviewMode && view != View.Settings) view = View.NeedsReview
        statusLine = if (focusReviewMode) "Focus review mode enabled" else "Focus review mode disabled"
    }

    fun isHandledCurrent(pr: PullRequest): Boolean = handledReviewRecords[pr.key] == pr.updatedMarker

    fun selectView(next: View) {
        view = next
        selectedPullRequest = null
        reviewSessionActive = false
        reviewSessionQueueKeys = emptyList()
        sessionHandledCount = 0
        keyboardMode = KeyboardMode.Normal
        if (next in SidebarKeyboardViews) {
            sidebarKeyboardView = next
        }
        if (next != View.Settings) {
            keyboardFocusRegion = FocusRegion.PullRequestList
        }
    }

    fun isPinned(pr: PullRequest): Boolean = pinnedPrKeys.contains(pr.key)

    fun togglePin(pr: PullRequest? = selectedPullRequest) {
        pr ?: return
        pinnedPrKeys = if (pinnedPrKeys.contains(pr.key)) pinnedPrKeys - pr.key else pinnedPrKeys + pr.key
        savePinned()
        statusLine = if (pinnedPrKeys.contains(pr.key)) "Pinned ${pr.repository} #${pr.number}" else "Unpinned ${pr.repository} #${pr.number}"
    }

    fun savePinned() {
        Files.createDirectories(configDir)
        pinnedFile.writeLines(pinnedPrKeys.sorted())
    }

    fun isRepositoryMuted(repository: String): Boolean = parseLines(mutedRepositoriesText).contains(repository)

    fun toggleMuteSelectedRepository() {
        val repo = selectedPullRequest?.repository?.toString() ?: return
        mutedRepositoriesText = if (isRepositoryMuted(repo)) {
            parseLines(mutedRepositoriesText).filterNot { it == repo }.joinToString("\n")
        } else {
            (parseLines(mutedRepositoriesText) + repo).distinct().sorted().joinToString("\n")
        }
        saveConfig()
        selectedPullRequest = null
        statusLine = if (isRepositoryMuted(repo)) "Muted $repo" else "Unmuted $repo"
    }

    fun validateTrackingText() {
        val invalidRepos = parseLines(repositoriesText).filter { parseRepo(it) == null }
        val invalidOrgs = parseLines(organizationsText).filter { it.contains("/") || it.contains(" ") }
        statusLine = when {
            invalidRepos.isEmpty() && invalidOrgs.isEmpty() -> "Tracking looks valid · ${parseLines(repositoriesText).size} repositories · ${parseLines(organizationsText).size} organizations"
            else -> "Tracking issues · invalid repos: ${invalidRepos.joinToString().ifBlank { "none" }} · invalid orgs: ${invalidOrgs.joinToString().ifBlank { "none" }}"
        }
    }

    fun copySelectedMarkdown() {
        val pr = selectedPullRequest ?: return
        copyToClipboard("[${pr.repository} #${pr.number}: ${pr.title}](${pr.url})")
        statusLine = "Copied PR as Markdown"
    }

    fun copyReviewDigest() {
        val lines = reviewQueue().take(20).mapIndexed { index, pr ->
            "${index + 1}. ${pr.repository} #${pr.number} — ${pr.title} (${pr.url})"
        }
        copyToClipboard(if (lines.isEmpty()) "RevQ: review queue clear" else "RevQ review queue\n" + lines.joinToString("\n"))
        statusLine = "Copied review queue digest"
    }

    fun openTopReviewPullRequests() {
        val items = reviewQueue().take(5)
        items.forEach { openUrl(it.url) }
        statusLine = if (items.isEmpty()) "Review queue clear" else "Opened ${items.size} review PRs"
    }

    fun clearCache() {
        pullRequests = emptyList()
        selectedPullRequest = null
        Files.deleteIfExists(cacheFile)
        statusLine = "Local PR cache cleared"
    }

    fun clearHandledReviews() {
        handledReviewRecords = emptyMap()
        lastUndoReview = null
        saveHandled()
        statusLine = "Handled review state cleared"
    }

    fun copyDiagnostics() {
        val text = buildString {
            appendLine("RevQ diagnostics")
            appendLine("gh path: ${ghPathText.ifBlank { "auto-detect" }}")
            appendLine("repositories: ${parseLines(repositoriesText).size}")
            appendLine("organizations: ${parseLines(organizationsText).size}")
            appendLine("muted repositories: ${parseLines(mutedRepositoriesText).size}")
            appendLine("cached PRs: ${pullRequests.size}")
            appendLine("visible PRs: ${visiblePullRequests().size}")
            appendLine("review queue: ${reviewQueue().size}")
            appendLine("auto refresh: $autoRefreshEnabled every ${autoRefreshIntervalMinutesText.ifBlank { "5" }} minutes")
            appendLine("reminders: $reminderEnabled at ${reminderTimeText.ifBlank { "09:00" }}; ${reminderStatus}")
            appendLine("last error: ${lastRefreshError ?: "none"}")
        }
        diagnosticsText = text
        copyToClipboard(text)
        statusLine = "Copied diagnostics"
    }

    fun recordCommandExecution(commandId: CommandId) {
        recentCommandIds = (listOf(commandId) + recentCommandIds.filterNot { it == commandId }).take(6)
    }

    private fun saveCache() {
        Files.createDirectories(configDir)
        cacheFile.writeLines(pullRequests.map(::serializePullRequest))
    }

    private fun loadCache(): List<PullRequest> = cacheFile.safeLines().mapNotNull(::deserializePullRequest)

    fun startAutoRefreshScheduler() {
        autoRefreshJob?.cancel()
        autoRefreshJob = scope.launch {
            while (true) {
                val minutes = autoRefreshIntervalMinutesText.toLongOrNull()?.coerceIn(1L, 240L) ?: 5L
                delay(minutes * 60_000L)
                if (autoRefreshEnabled && !isRefreshing && parseLines(repositoriesText).isNotEmpty()) {
                    statusLine = "Auto-refreshing review queue…"
                    refresh()
                }
            }
        }
    }

    fun sortPullRequests(view: View, prs: List<PullRequest>): List<PullRequest> {
        val staleDays = staleThresholdDaysText.toLongOrNull()?.coerceIn(1L, 30L) ?: 2L
        val urgencyOrder: (PullRequest) -> Int = { pr ->
            when {
                pinnedPrKeys.contains(pr.key) -> 0
                pr.source == PullRequestSource.ReviewRequest && isOlderThan(pr.updatedAt, staleDays) -> 10
                pr.source == PullRequestSource.ReviewRequest -> 20
                pr.source == PullRequestSource.Mine -> when (ownPullRequestPrimaryStatus(pr)) {
                    OwnPullRequestStatus.ChangesRequested -> 30
                    OwnPullRequestStatus.MergeConflict -> 31
                    OwnPullRequestStatus.ChecksFailing -> 32
                    OwnPullRequestStatus.DiscussionNeedsResponse -> 33
                    OwnPullRequestStatus.ApprovedAndReady -> 40
                    OwnPullRequestStatus.WaitingForReviewer -> 50
                    OwnPullRequestStatus.Draft -> 60
                    OwnPullRequestStatus.NoActionNeeded -> 70
                }
                else -> 80
            }
        }
        return when (sortMode) {
            "Updated newest" -> prs.sortedByDescending { instantOrNull(it.updatedAt) ?: Instant.EPOCH }
            "Updated oldest" -> prs.sortedBy { instantOrNull(it.updatedAt) ?: Instant.EPOCH }
            "Repository" -> prs.sortedWith(compareBy<PullRequest> { it.repository.toString() }.thenBy { it.number })
            "Comments" -> prs.sortedByDescending { it.comments }
            else -> prs.sortedWith(compareBy<PullRequest> { urgencyOrder(it) }.thenBy { instantOrNull(it.updatedAt) ?: Instant.EPOCH })
        }
    }

    fun previewReminderWindow() {
        reminderWindowIsPreview = true
        showReminderWindow = true
    }

    fun showScheduledReminderWindow() {
        reminderWindowIsPreview = false
        showReminderWindow = true
    }

    fun closeReminderWindow() {
        if (reminderWindowIsPreview) {
            reminderWindowIsPreview = false
            showReminderWindow = false
        } else {
            dismissReminderToday()
        }
    }

    fun startReviewingFromReminder() {
        reminderWindowIsPreview = false
        showReminderWindow = false
        reminderSnoozedUntil = null
        reminderDismissedDate = LocalDate.now().toString()
        saveReminderState()
        startReminderScheduler()
        startReviewing()
        statusLine = "Review reminder handled · review session started"
    }

    fun snoozeReminder() {
        val minutes = reminderSnoozeMinutesText.toLongOrNull()?.coerceIn(5L, 24L * 60L) ?: 60L
        reminderWindowIsPreview = false
        showReminderWindow = false
        reminderSnoozedUntil = Instant.now().plus(Duration.ofMinutes(minutes))
        reminderDismissedDate = null
        saveReminderState()
        startReminderScheduler()
        statusLine = "Reminder snoozed until ${formatReminderInstant(reminderSnoozedUntil!!)}"
    }

    fun dismissReminderToday() {
        reminderWindowIsPreview = false
        showReminderWindow = false
        reminderSnoozedUntil = null
        reminderDismissedDate = LocalDate.now().toString()
        saveReminderState()
        startReminderScheduler()
        statusLine = "Reminder dismissed for today"
    }

    fun saveReminderState() {
        Files.createDirectories(configDir)
        reminderEnabledFile.writeLines(listOf(reminderEnabled.toString()))
        reminderTimeFile.writeLines(listOf(reminderTimeText.trim().ifBlank { "09:00" }))
        reminderDaysFile.writeLines(listOf(reminderDaysText.trim().ifBlank { "Mon-Fri" }))
        reminderQuietHoursFile.writeLines(listOf(quietHoursText.trim().ifBlank { "18:00-08:00" }))
        reminderOnlyWhenQueueFile.writeLines(listOf(remindOnlyWhenQueueNotClear.toString()))
        reminderSnoozeMinutesFile.writeLines(listOf(reminderSnoozeMinutesText.trim().ifBlank { "60" }))
        reminderSnoozedUntil?.let { reminderSnoozedUntilFile.writeLines(listOf(it.toString())) } ?: Files.deleteIfExists(reminderSnoozedUntilFile)
        reminderDismissedDate?.let { reminderDismissedDateFile.writeLines(listOf(it)) } ?: Files.deleteIfExists(reminderDismissedDateFile)
    }

    fun startReminderScheduler() {
        reminderSchedulerJob?.cancel()
        reminderSchedulerJob = scope.launch {
            // Recompute frequently enough that settings/snooze changes feel responsive, but never show
            // the window on app startup just because today's configured time is already in the past.
            while (true) {
                if (!reminderEnabled) {
                    nextReminderAt = null
                    reminderStatus = "Reminders disabled"
                    delay(30_000)
                    continue
                }

                val scheduledAt = nextReminderInstant(Instant.now())
                nextReminderAt = scheduledAt
                reminderStatus = "Next reminder ${formatReminderInstant(scheduledAt)}"

                val now = Instant.now()
                val waitMs = Duration.between(now, scheduledAt).toMillis().coerceIn(1_000L, 60_000L)
                delay(waitMs)

                val dueAt = nextReminderAt
                if (dueAt != null && !Instant.now().isBefore(dueAt) && !showReminderWindow && !isRefreshing) {
                    runScheduledReminderCheck()
                }
            }
        }
    }

    private fun runScheduledReminderCheck() {
        if (!reminderEnabled) return
        if (isTodayDismissed()) {
            statusLine = "Reminder already dismissed today"
            return
        }

        val snooze = reminderSnoozedUntil
        if (snooze != null && !Instant.now().isBefore(snooze)) {
            reminderSnoozedUntil = null
            saveReminderState()
        }

        val repos = parseLines(repositoriesText)
        val orgs = parseLines(organizationsText)
        if (repos.isEmpty() && orgs.isEmpty()) {
            statusLine = "Reminder skipped · no repositories configured"
            return
        }

        statusLine = "Reminder time reached · refreshing review queue…"
        refresh(showReminderAfterRefresh = true)
    }

    private fun shouldShowReminderWindowNow(): Boolean {
        if (!reminderEnabled) return false
        if (isTodayDismissed()) return false
        if (isInReminderQuietHours()) return false
        if (remindOnlyWhenQueueNotClear && reviewQueue().isEmpty()) return false
        return true
    }

    private fun isTodayDismissed(): Boolean = reminderDismissedDate == LocalDate.now().toString()

    private fun isInReminderQuietHours(now: LocalTime = LocalTime.now()): Boolean {
        val raw = quietHoursText.trim()
        if (raw.isBlank() || raw.equals("Off", ignoreCase = true) || raw.equals("Disabled", ignoreCase = true)) return false
        val parts = raw.split("-", limit = 2).map { it.trim() }
        if (parts.size != 2) return false
        val start = parseReminderTime(parts[0]) ?: return false
        val end = parseReminderTime(parts[1]) ?: return false
        return if (start <= end) {
            now >= start && now < end
        } else {
            now >= start || now < end
        }
    }

    private fun nextReminderInstant(now: Instant): Instant {
        val snooze = reminderSnoozedUntil
        if (snooze != null && snooze.isAfter(now)) return snooze

        val zone = ZoneId.systemDefault()
        val nowLocal = LocalDateTime.ofInstant(now, zone)
        val time = parseReminderTime(reminderTimeText) ?: LocalTime.of(9, 0)
        val today = nowLocal.toLocalDate()

        for (offset in 0..14) {
            val date = today.plusDays(offset.toLong())
            if (!isReminderDay(date)) continue
            if (reminderDismissedDate == date.toString()) continue
            val candidate = date.atTime(time)
            if (candidate.isAfter(nowLocal)) return candidate.atZone(zone).toInstant()
        }

        return today.plusDays(1).atTime(time).atZone(zone).toInstant()
    }

    private fun isReminderDay(date: LocalDate): Boolean {
        val raw = reminderDaysText.lowercase()
        if (raw.isBlank() || raw.contains("every") || raw.contains("daily")) return true
        if (raw.contains("mon-fri") || raw.contains("weekday") || raw.contains("workday")) {
            return date.dayOfWeek !in setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
        }
        val token = when (date.dayOfWeek) {
            DayOfWeek.MONDAY -> "mon"
            DayOfWeek.TUESDAY -> "tue"
            DayOfWeek.WEDNESDAY -> "wed"
            DayOfWeek.THURSDAY -> "thu"
            DayOfWeek.FRIDAY -> "fri"
            DayOfWeek.SATURDAY -> "sat"
            DayOfWeek.SUNDAY -> "sun"
        }
        return raw.contains(token)
    }
}

enum class DesktopPlatform(val displayName: String) {
    Linux("Linux"),
    MacOS("macOS"),
    Windows("Windows"),
    Unknown("this platform"),
}

enum class GhDetectionSource(val label: String) {
    Configured("Configured override"),
    Environment("REVQ_GH_PATH"),
    Path("System PATH"),
    SystemLocator("System executable lookup"),
    LoginShell("Login shell"),
    Homebrew("Homebrew"),
    Linuxbrew("Linuxbrew"),
    Snap("Snap"),
    Chocolatey("Chocolatey"),
    Scoop("Scoop"),
    KnownLocation("Known platform location"),
}

data class GhDetectionResult(
    val executable: String,
    val source: GhDetectionSource,
)

fun currentDesktopPlatform(): DesktopPlatform {
    val osName = System.getProperty("os.name").orEmpty().lowercase()
    return when {
        osName.contains("mac") || osName.contains("darwin") -> DesktopPlatform.MacOS
        osName.contains("win") -> DesktopPlatform.Windows
        osName.contains("linux") || osName.contains("nix") || osName.contains("nux") -> DesktopPlatform.Linux
        else -> DesktopPlatform.Unknown
    }
}

object GhClient {
    @Volatile
    private var configuredExecutable: String? = null

    fun configureExecutable(path: String) {
        configuredExecutable = path.trim().ifBlank { null }
    }

    fun detectExecutable(): String? = detectExecutableResult()?.executable

    fun detectExecutableResult(): GhDetectionResult? = detectGhExecutableResult()

    fun discoverRepositories(orgs: List<String>): List<String> {
        ensureAuthenticated()
        return orgs.flatMap { org ->
            runGh(
                "repo", "list", org,
                "--limit", "100",
                "--json", "nameWithOwner",
                "--template", "{{range .}}{{.nameWithOwner}}{{\"\\n\"}}{{end}}",
            ).lines().map { it.trim() }.filter { "/" in it }
        }.distinct().sorted()
    }

    fun refresh(repositories: List<String>): List<PullRequest> {
        val login = prepareRefresh()
        return dedupePullRequests(repositories.flatMap { repo -> refreshRepository(repo, login) })
    }

    fun prepareRefresh(): String {
        ensureAuthenticated()
        return currentLogin()
    }

    fun refreshRepository(repo: String, login: String): List<PullRequest> {
        val reviewRequests = listPrs(
            repo = repo,
            source = PullRequestSource.ReviewRequest,
            search = "is:pr is:open review-requested:$login",
            author = null,
        ).filterNot { it.authorLogin?.equals(login, ignoreCase = true) == true }

        val mine = listPrs(
            repo = repo,
            source = PullRequestSource.Mine,
            search = null,
            author = login,
        )

        val discussionInsights = runCatching {
            loadDiscussionInsights(
                repo = repo,
                authorLogin = login,
            )
        }.getOrNull()

        val enrichedMine = mine.map { pr ->
            val insight = discussionInsights?.get(pr.number)
            pr.copy(
                unresolvedDiscussionCount = insight?.unresolvedCount,
                unresolvedDiscussionAuthors = insight?.authors.orEmpty(),
            )
        }

        return dedupePullRequests(reviewRequests + enrichedMine)
    }

    fun testConnection(): String {
        val executable = resolveGhExecutable()
        ensureAuthenticated()
        val login = currentLogin()
        val version = runGh("--version").lineSequence().firstOrNull().orEmpty()
        return "✓ gh works · authenticated as $login · $version · $executable"
    }

    private fun ensureAuthenticated() {
        // Gives a better error than letting the first `gh pr list` fail later.
        runGh("auth", "status", "-h", "github.com")
    }

    private fun currentLogin(): String = runGh("api", "user", "--jq", ".login")
        .trim()
        .ifBlank { error("GitHub CLI did not return the current user login. Run `gh auth status` in a terminal.") }

    private fun listPrs(
        repo: String,
        source: PullRequestSource,
        search: String?,
        author: String?,
    ): List<PullRequest> {
        val repoId = parseRepo(repo) ?: error("Invalid repository '$repo'. Expected OWNER/REPO.")
        val args = mutableListOf(
            "pr", "list",
            "-R", repo,
            "--state", "open",
            "--limit", "100",
        )
        if (search != null) {
            args += listOf("--search", search)
        }
        if (author != null) {
            args += listOf("--author", author)
        }

        val jqExpression = """
            def reviewerLabel:
                if type != "object" then empty
                elif (.login? // "") != "" then "@" + .login
                elif (.slug? // "") != "" then "team/" + .slug
                elif (.name? // "") != "" then .name
                else empty
                end;
            .[] |
            (.author.login // "") as ${'$'}owner |
            (.comments // []) as ${'$'}comments |
            (.statusCheckRollup // []) as ${'$'}checks |
            (.reviewRequests // []) as ${'$'}reviewRequests |
            (.latestReviews // []) as ${'$'}latestReviews |
            ([${'$'}checks[]? | select(
                (.conclusion == "FAILURE") or
                (.conclusion == "CANCELLED") or
                (.conclusion == "TIMED_OUT") or
                (.conclusion == "ACTION_REQUIRED") or
                (.conclusion == "STARTUP_FAILURE") or
                (.conclusion == "STALE") or
                (.state == "FAILURE") or
                (.state == "ERROR")
            )] | length) as ${'$'}checksFailing |
            ([${'$'}checks[]? | select(
                (.conclusion == "SUCCESS") or
                (.conclusion == "NEUTRAL") or
                (.conclusion == "SKIPPED") or
                (.state == "SUCCESS")
            )] | length) as ${'$'}checksPassing |
            (${'$'}checks | length) as ${'$'}checksTotal |
            (${'$'}checksTotal - ${'$'}checksFailing - ${'$'}checksPassing) as ${'$'}checksPending |
            ([${'$'}reviewRequests[]? | reviewerLabel] | unique | join(",")) as ${'$'}requestedReviewers |
            ([${'$'}latestReviews[]? | select(.state == "CHANGES_REQUESTED") | (.author.login // empty) | "@" + .] | unique | join(",")) as ${'$'}changeRequestReviewers |
            ([${'$'}latestReviews[]? | select(.state == "APPROVED") | (.author.login // empty) | "@" + .] | unique | join(",")) as ${'$'}approvingReviewers |
            [
                .number,
                .title,
                .url,
                .updatedAt,
                (${'$'}comments | length),
                ${'$'}owner,
                .isDraft,
                (.reviewDecision // ""),
                (.mergeable // ""),
                (.mergeStateStatus // ""),
                (${'$'}reviewRequests | length),
                ${'$'}checksTotal,
                ${'$'}checksFailing,
                ${'$'}checksPending,
                ${'$'}requestedReviewers,
                ${'$'}changeRequestReviewers,
                ${'$'}approvingReviewers
            ] | @tsv
        """.trimIndent().replace("\n", " ")

        args += listOf(
            "--json",
            "number,title,url,updatedAt,comments,author,isDraft,reviewDecision,mergeable,mergeStateStatus,reviewRequests,latestReviews,statusCheckRollup",
            "--jq",
            jqExpression,
        )

        val output = runGh(*args.toTypedArray())
        return output.lines().mapNotNull { line ->
            val parts = line.split("\t", limit = 17)
            if (parts.size < 6) return@mapNotNull null

            PullRequest(
                repository = repoId,
                number = parts[0].toIntOrNull() ?: return@mapNotNull null,
                title = parts[1].unescapeTsv(),
                url = parts[2].unescapeTsv(),
                updatedAt = parts[3].unescapeTsv().ifBlank { null },
                comments = parts.getOrNull(4)?.toIntOrNull() ?: 0,
                source = source,
                authorLogin = parts.getOrNull(5)?.unescapeTsv()?.ifBlank { null },
                isDraft = parts.getOrNull(6)?.toBooleanStrictOrNull() ?: false,
                reviewDecision = parts.getOrNull(7)?.unescapeTsv()?.ifBlank { null },
                mergeable = parts.getOrNull(8)?.unescapeTsv()?.ifBlank { null },
                mergeStateStatus = parts.getOrNull(9)?.unescapeTsv()?.ifBlank { null },
                reviewRequestsCount = parts.getOrNull(10)?.toIntOrNull() ?: 0,
                checksTotal = parts.getOrNull(11)?.toIntOrNull() ?: 0,
                checksFailing = parts.getOrNull(12)?.toIntOrNull() ?: 0,
                checksPending = parts.getOrNull(13)?.toIntOrNull() ?: 0,
                requestedReviewers = deserializeIdentityList(parts.getOrNull(14)),
                changeRequestReviewers = deserializeIdentityList(parts.getOrNull(15)),
                approvingReviewers = deserializeIdentityList(parts.getOrNull(16)),
            )
        }
    }

    /**
     * GitHub's regular PR comment list does not tell us whether review threads
     * are resolved. Fetch the actual review-thread state through GraphQL and
     * only classify a discussion as open when at least one thread is unresolved.
     */
    private fun loadDiscussionInsights(
        repo: String,
        authorLogin: String,
    ): Map<Int, DiscussionInsight> {
        val searchQuery = "repo:$repo is:pr is:open author:$authorLogin"
        val graphQlQuery = """
            query(${ '$' }searchQuery: String!) {
                search(query: ${ '$' }searchQuery, type: ISSUE, first: 100) {
                    nodes {
                        ... on PullRequest {
                            number
                            reviewThreads(first: 100) {
                                nodes {
                                    isResolved
                                    comments(first: 1) {
                                        nodes {
                                            author {
                                                login
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        """.trimIndent().replace("\n", " ")

        val jqExpression = """
            .data.search.nodes[]? |
            ([.reviewThreads.nodes[]? | select(.isResolved == false)]) as ${'$'}openThreads |
            [
                .number,
                (${'$'}openThreads | length),
                ([${'$'}openThreads[]? | (.comments.nodes[0].author.login // empty) | "@" + .] | unique | join(","))
            ] |
            @tsv
        """.trimIndent().replace("\n", " ")

        val output = runGh(
            "api",
            "graphql",
            "-f", "query=$graphQlQuery",
            "-F", "searchQuery=$searchQuery",
            "--jq", jqExpression,
        )

        return output
            .lineSequence()
            .mapNotNull { line ->
                val parts = line.split("\t", limit = 3)
                val number = parts.getOrNull(0)?.toIntOrNull() ?: return@mapNotNull null
                val unresolvedCount = parts.getOrNull(1)?.toIntOrNull() ?: 0
                val authors = deserializeIdentityList(parts.getOrNull(2))
                number to DiscussionInsight(
                    unresolvedCount = unresolvedCount,
                    authors = authors,
                )
            }
            .toMap()
    }

    private fun runGh(vararg args: String): String {
        val command = listOf(resolveGhExecutable()) + args
        val process = try {
            ProcessBuilder(command)
                .redirectErrorStream(true)
                .start()
        } catch (error: IOException) {
            throw IllegalStateException(
                "GitHub CLI executable could not be started. Tried command: ${command.joinToString(" ")}\nOpen Settings and set the full path to the GitHub CLI executable.",
                error,
            )
        }

        val output = process.inputStream.bufferedReader().readText().trim()
        val finished = process.waitFor(70, TimeUnit.SECONDS)
        if (!finished) {
            process.destroyForcibly()
            error("GitHub CLI timed out while running: ${command.joinToString(" ")}")
        }
        if (process.exitValue() != 0) {
            val message = output.ifBlank { "exit code ${process.exitValue()}" }
            error("gh command failed: ${command.joinToString(" ")}\n$message")
        }
        return output
    }

    private fun resolveGhExecutable(): String {
        val configured = configuredExecutable?.trim().orEmpty()
        if (configured.isNotBlank()) {
            if (canRunGh(configured)) return configured
            throw IllegalStateException(
                "Configured GitHub CLI executable does not work: $configured\n" +
                        "Open Settings and update the executable override, or clear it to auto-detect."
            )
        }

        val envOverride = System.getenv("REVQ_GH_PATH")?.trim().orEmpty()
        if (envOverride.isNotBlank()) {
            if (canRunGh(envOverride)) return envOverride
            throw IllegalStateException(
                "REVQ_GH_PATH is set but does not work: $envOverride\n" +
                        "Update REVQ_GH_PATH or clear it and let RevQ auto-detect GitHub CLI."
            )
        }

        detectGhExecutableResult()?.let { return it.executable }

        val platform = currentDesktopPlatform()
        throw IllegalStateException(
            "GitHub CLI executable was not found on ${platform.displayName}.\n\n" +
                    platformDetectionSummary(platform) + "\n\n" +
                    "Fix options:\n" +
                    "1. Install GitHub CLI.\n" +
                    "2. Make gh available through your system PATH.\n" +
                    "3. Set REVQ_GH_PATH to the full executable path.\n" +
                    "4. Use the executable override in Settings only when auto-detection fails."
        )
    }

    private fun detectGhExecutableResult(): GhDetectionResult? {
        val environmentOverride = System.getenv("REVQ_GH_PATH")?.trim().orEmpty()
        if (environmentOverride.isNotBlank() && canRunGh(environmentOverride)) {
            return GhDetectionResult(environmentOverride, GhDetectionSource.Environment)
        }

        val platform = currentDesktopPlatform()
        val pathCandidates = pathCandidates(platform)
            .firstOrNull { canRunGh(it) }
            ?.let { return GhDetectionResult(it, GhDetectionSource.Path) }

        platformLocatorResult(platform)?.let { return it }
        packageManagerResult(platform)?.let { return it }
        shellResult(platform)?.let { return it }
        knownLocationResult(platform)?.let { return it }

        return null
    }

    private fun pathCandidates(platform: DesktopPlatform): List<String> {
        val suffixes = when (platform) {
            DesktopPlatform.Windows -> listOf("gh.exe", "gh.cmd", "gh.bat", "gh")
            else -> listOf("gh")
        }
        return System.getenv("PATH")
            .orEmpty()
            .split(File.pathSeparator)
            .filter { it.isNotBlank() }
            .flatMap { dir -> suffixes.map { suffix -> File(dir, suffix).absolutePath } }
            .distinct()
    }

    private fun platformLocatorResult(platform: DesktopPlatform): GhDetectionResult? {
        val command = when (platform) {
            DesktopPlatform.Windows -> listOf("where.exe", "gh")
            DesktopPlatform.Linux, DesktopPlatform.MacOS -> listOf("which", "gh")
            DesktopPlatform.Unknown -> return null
        }
        return runCommandForLines(command)
            .firstOrNull { canRunGh(it) }
            ?.let { GhDetectionResult(it, GhDetectionSource.SystemLocator) }
    }

    private fun packageManagerResult(platform: DesktopPlatform): GhDetectionResult? {
        return when (platform) {
            DesktopPlatform.MacOS -> brewResolveGhExecutable(
                candidates = listOf(
                    "/opt/homebrew/bin/brew",
                    "/usr/local/bin/brew",
                    "brew",
                ),
                source = GhDetectionSource.Homebrew,
            )
            DesktopPlatform.Linux -> {
                brewResolveGhExecutable(
                    candidates = listOf(
                        "/home/linuxbrew/.linuxbrew/bin/brew",
                        "brew",
                    ),
                    source = GhDetectionSource.Linuxbrew,
                ) ?: linuxSnapResult()
            }
            DesktopPlatform.Windows -> windowsPackageManagerResult()
            DesktopPlatform.Unknown -> null
        }
    }

    private fun shellResult(platform: DesktopPlatform): GhDetectionResult? {
        if (platform != DesktopPlatform.Linux && platform != DesktopPlatform.MacOS) return null
        val shells = when (platform) {
            DesktopPlatform.MacOS -> listOf("/bin/zsh", "/usr/bin/zsh", "/bin/bash", "/usr/bin/bash")
            else -> listOf("/bin/bash", "/usr/bin/bash", "/bin/zsh", "/usr/bin/zsh", "/bin/sh")
        }
        val modes = listOf(listOf("-lc"), listOf("-l", "-c"))
        return shells.firstNotNullOfOrNull { shell ->
            modes.firstNotNullOfOrNull { mode ->
                runCommandForSingleLine(listOf(shell) + mode + "command -v gh")
                    ?.takeIf(::canRunGh)
                    ?.let { GhDetectionResult(it, GhDetectionSource.LoginShell) }
            }
        }
    }

    private fun knownLocationResult(platform: DesktopPlatform): GhDetectionResult? {
        val home = System.getProperty("user.home")
        val candidates = when (platform) {
            DesktopPlatform.Linux -> listOf(
                "/usr/local/bin/gh",
                "/usr/bin/gh",
                "/home/linuxbrew/.linuxbrew/bin/gh",
                "/home/linuxbrew/.linuxbrew/opt/gh/bin/gh",
                "/snap/bin/gh",
                "/var/lib/snapd/snap/bin/gh",
                "$home/.local/bin/gh",
                "$home/bin/gh",
            )
            DesktopPlatform.MacOS -> listOf(
                "/opt/homebrew/bin/gh",
                "/opt/homebrew/opt/gh/bin/gh",
                "/usr/local/bin/gh",
                "/usr/local/opt/gh/bin/gh",
                "$home/.local/bin/gh",
                "$home/bin/gh",
            )
            DesktopPlatform.Windows -> windowsKnownLocations()
            DesktopPlatform.Unknown -> listOf("gh", "gh.exe")
        }
        return candidates.firstOrNull(::canRunGh)?.let { GhDetectionResult(it, GhDetectionSource.KnownLocation) }
    }

    private fun windowsKnownLocations(): List<String> {
        val userProfile = System.getenv("USERPROFILE").orEmpty().ifBlank { System.getProperty("user.home") }
        val localAppData = System.getenv("LOCALAPPDATA").orEmpty()
        val programFiles = System.getenv("ProgramFiles").orEmpty()
        val programFilesX86 = System.getenv("ProgramFiles(x86)").orEmpty()
        val chocolateyInstall = System.getenv("ChocolateyInstall").orEmpty()

        return buildList {
            if (programFiles.isNotBlank()) add("$programFiles\\GitHub CLI\\gh.exe")
            if (programFilesX86.isNotBlank()) add("$programFilesX86\\GitHub CLI\\gh.exe")
            if (localAppData.isNotBlank()) add("$localAppData\\Programs\\GitHub CLI\\gh.exe")
            if (chocolateyInstall.isNotBlank()) add("$chocolateyInstall\\bin\\gh.exe")
            if (userProfile.isNotBlank()) {
                add("$userProfile\\scoop\\shims\\gh.exe")
                add("$userProfile\\scoop\\apps\\gh\\current\\bin\\gh.exe")
            }
        }
    }

    private fun windowsPackageManagerResult(): GhDetectionResult? {
        val chocolatey = System.getenv("ChocolateyInstall")
            ?.takeIf { it.isNotBlank() }
            ?.let { "$it\\bin\\gh.exe" }
            ?.takeIf(::canRunGh)
        if (chocolatey != null) return GhDetectionResult(chocolatey, GhDetectionSource.Chocolatey)

        val userProfile = System.getenv("USERPROFILE").orEmpty().ifBlank { System.getProperty("user.home") }
        val scoopCandidates = listOf(
            "$userProfile\\scoop\\shims\\gh.exe",
            "$userProfile\\scoop\\apps\\gh\\current\\bin\\gh.exe",
        )
        val scoop = scoopCandidates.firstOrNull(::canRunGh)
        if (scoop != null) return GhDetectionResult(scoop, GhDetectionSource.Scoop)

        return null
    }

    private fun linuxSnapResult(): GhDetectionResult? {
        return listOf("/snap/bin/gh", "/var/lib/snapd/snap/bin/gh")
            .firstOrNull(::canRunGh)
            ?.let { GhDetectionResult(it, GhDetectionSource.Snap) }
    }

    private fun brewResolveGhExecutable(
        candidates: List<String>,
        source: GhDetectionSource,
    ): GhDetectionResult? {
        return candidates.firstNotNullOfOrNull { brew ->
            val prefix = runCommandForSingleLine(listOf(brew, "--prefix", "gh"))
                ?: return@firstNotNullOfOrNull null
            listOf("$prefix/bin/gh", "$prefix/libexec/bin/gh")
                .firstOrNull(::canRunGh)
                ?.let { GhDetectionResult(it, source) }
        }
    }

    private fun canRunGh(candidate: String): Boolean = runCatching {
        val process = ProcessBuilder(candidate, "--version")
            .redirectErrorStream(true)
            .start()
        val finished = process.waitFor(5, TimeUnit.SECONDS)
        if (!finished) process.destroyForcibly()
        finished && process.exitValue() == 0
    }.getOrDefault(false)

    private fun runCommandForLines(command: List<String>): List<String> = runCatching {
        val process = ProcessBuilder(command)
            .redirectErrorStream(true)
            .start()
        val output = process.inputStream.bufferedReader().readText()
        val ok = process.waitFor(5, TimeUnit.SECONDS) && process.exitValue() == 0
        if (!ok) emptyList() else output.lineSequence().map { it.trim() }.filter { it.isNotBlank() }.toList()
    }.getOrDefault(emptyList())

    private fun runCommandForSingleLine(command: List<String>): String? = runCommandForLines(command).firstOrNull()

    private fun platformDetectionSummary(platform: DesktopPlatform): String = when (platform) {
        DesktopPlatform.Linux -> "RevQ checked system PATH, executable lookup, login shells, Linuxbrew, Snap, and common user/system locations."
        DesktopPlatform.MacOS -> "RevQ checked system PATH, executable lookup, login shells, Homebrew, and common user/system locations."
        DesktopPlatform.Windows -> "RevQ checked system PATH, Windows executable lookup, Chocolatey, Scoop, and common per-user/system locations."
        DesktopPlatform.Unknown -> "RevQ checked the system PATH and common executable names."
    }

}


@Composable
fun RevqApp(state: AppState) {
    val keyboardRouter = remember { KeyboardRouter() }
    val commandExecutor = remember(state) { CommandExecutor(state) }
    val paletteState = remember { CommandPaletteState() }
    val focusManager = LocalFocusManager.current

    fun executeKeyboardAction(action: KeyboardAction): Boolean {
        when (action) {
            KeyboardAction.Unhandled -> return false
            KeyboardAction.MoveNext -> {
                if (state.view == View.Settings) {
                    moveSettingsRow(state, 1)
                } else {
                    moveWithinFocusedRegion(state, 1)
                }
            }
            KeyboardAction.MovePrevious -> {
                if (state.view == View.Settings) {
                    moveSettingsRow(state, -1)
                } else {
                    moveWithinFocusedRegion(state, -1)
                }
            }
            KeyboardAction.FocusLeft -> {
                if (state.view == View.Settings) {
                    moveSettingsSection(state, -1)
                } else {
                    moveKeyboardFocus(state, -1)
                }
            }
            KeyboardAction.FocusRight -> {
                if (state.view == View.Settings) {
                    moveSettingsSection(state, 1)
                } else {
                    moveKeyboardFocus(state, 1)
                }
            }
            KeyboardAction.FirstItem -> moveToRegionBoundary(state, first = true)
            KeyboardAction.LastItem -> moveToRegionBoundary(state, first = false)
            KeyboardAction.HalfPageDown -> moveByHalfPage(state, 1)
            KeyboardAction.HalfPageUp -> moveByHalfPage(state, -1)
            KeyboardAction.Activate -> {
                if (state.view == View.Settings) {
                    activateFocusedSettingsRow(state)
                } else {
                    activateFocusedRegion(state)
                }
            }
            KeyboardAction.Escape -> {
                if (state.view == View.Settings) {
                    state.selectView(View.NeedsReview)
                } else {
                    escapeKeyboardContext(state)
                }
            }

            KeyboardAction.ExitInsertMode -> {
                state.keyboardMode = KeyboardMode.Normal
                focusManager.clearFocus(force = true)
            }

            KeyboardAction.SaveSettings -> {
                state.saveConfig()
                state.statusLine = "Settings saved"
            }

            KeyboardAction.OpenPalette -> paletteState.open()
            KeyboardAction.CloseCommandPalette -> paletteState.close()

            is KeyboardAction.ExecuteCommand -> {
                commandExecutor.execute(action.commandId)
            }
        }
        return true
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .onPreviewKeyEvent { event ->
                val action = keyboardRouter.route(
                    event = event,
                    context = KeyboardContext(
                        mode = state.keyboardMode,
                        focusRegion = state.keyboardFocusRegion,
                        view = state.view,
                        commandPaletteOpen = paletteState.isOpen,
                    ),
                )
                executeKeyboardAction(action)
            },
        color = AppBg,
    ) {
        Box(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize()) {
                Row(Modifier.weight(1f).fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .keyboardRegionFrame(state.keyboardFocusRegion == FocusRegion.Sidebar),
                    ) {
                        SidebarPanel(state = state)
                    }
                    Divider(
                        Modifier.fillMaxHeight().width(1.dp),
                        color = if (state.keyboardFocusRegion == FocusRegion.Sidebar) {
                            Olive.copy(alpha = 0.55f)
                        } else {
                            Border
                        },
                    )
                    if (state.view == View.Settings) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .keyboardRegionFrame(true),
                        ) {
                            SettingsPane(state)
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .keyboardRegionFrame(state.keyboardFocusRegion == FocusRegion.PullRequestList),
                        ) {
                            MainPane(state, Modifier.fillMaxSize())
                        }
                        AnimatedVisibility(visible = state.selectedPullRequest != null) {
                            Row {
                                Divider(
                                    Modifier.fillMaxHeight().width(1.dp),
                                    color = if (state.keyboardFocusRegion == FocusRegion.ReviewBrief) {
                                        Olive.copy(alpha = 0.55f)
                                    } else {
                                        Border
                                    },
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .keyboardRegionFrame(state.keyboardFocusRegion == FocusRegion.ReviewBrief),
                                ) {
                                    ReviewBriefPanel(state)
                                }
                            }
                        }
                    }
                }
                BottomStatusBar(state, paletteState)
            }

            if (paletteState.isOpen) {
                CommandPalette(
                    state = state,
                    paletteState = paletteState,
                    onGoToTop = { moveToRegionBoundary(state, first = true) },
                )
            }
        }
    }

    LaunchedEffect(state.searchQuery, state.view) {
        val selected = state.selectedPullRequest
        if (selected != null && state.visiblePullRequests().none { it.key == selected.key && it.source == selected.source }) {
            state.selectedPullRequest = null
        }
    }

    LaunchedEffect(state.selectedPullRequest) {
        if (
            state.selectedPullRequest == null &&
            state.keyboardFocusRegion == FocusRegion.ReviewBrief
        ) {
            state.keyboardFocusRegion = FocusRegion.PullRequestList
        }
    }
}

private fun Modifier.keyboardRegionFrame(active: Boolean): Modifier =
    border(
        width = 1.dp,
        color = if (active) Olive.copy(alpha = 0.45f) else Color.Transparent,
    )

@Composable
fun MainPane(state: AppState, modifier: Modifier = Modifier) {
    Column(
        modifier
            .fillMaxHeight()
            .background(PanelBg)
            .border(
                width = 1.dp,
                color = if (state.keyboardFocusRegion == FocusRegion.PullRequestList) {
                    Olive.copy(alpha = 0.22f)
                } else {
                    Color.Transparent
                },
            ),
    ) {
        MainToolbar(state)

        if (state.isRefreshing) {
            LinearProgressIndicator(
                progress = if (state.refreshTotal > 0) {
                    state.refreshDone.toFloat() / state.refreshTotal.toFloat()
                } else {
                    0.12f
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp),
                color = Olive,
                trackColor = PanelElevated,
            )
        }

        WorkspaceControls(state)
        Divider(color = Border)

        if (state.lastRefreshError != null && !state.isRefreshing) {
            RefreshErrorBanner(state)
            Divider(color = Border)
        }

        // Keep the cached/current queue usable during background refreshes.
        // Skeletons are only useful before RevQ has anything meaningful to show.
        if (state.isRefreshing && state.pullRequests.isEmpty()) {
            SkeletonList()
        } else {
            PullRequestList(state)
        }
    }
}

@Composable
fun MainToolbar(state: AppState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp)
            .padding(horizontal = 22.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = if (state.reviewSessionActive) "Review session" else state.view.label,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge,
                )

                if (state.focusReviewMode && !state.reviewSessionActive) {
                    Pill("Focus", OliveSoft, Olive)
                }
            }

            Text(
                text = if (state.reviewSessionActive) {
                    sessionProgressLine(state)
                } else {
                    mainToolbarSubtitle(state)
                },
                color = TextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        if (
            state.view == View.NeedsReview &&
            !state.reviewSessionActive &&
            state.reviewQueue().isNotEmpty()
        ) {
            Button(
                onClick = { state.startReviewing() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Olive,
                    contentColor = Color(0xFF151812),
                ),
            ) {
                Icon(
                    imageVector = Icons.Rounded.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(7.dp))
                Text("Start review session")
            }
        }

        if (state.reviewSessionActive) {
            TextButton(
                onClick = { state.previousReview() },
                enabled = canGoPrevious(state),
            ) {
                Text("Previous")
            }

            TextButton(onClick = { state.endReviewSession() }) {
                Text("End session", color = TextMuted)
            }
        }

        RefreshAction(state)
    }
}

@Composable
private fun RefreshAction(state: AppState) {
    Surface(
        modifier = Modifier
            .size(42.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(enabled = !state.isRefreshing) {
                state.refresh()
            },
        color = PanelElevated,
        border = BorderStroke(1.dp, Border),
        shape = RoundedCornerShape(10.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (state.isRefreshing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(17.dp),
                    color = Olive,
                    strokeWidth = 2.dp,
                )
            } else {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = "Refresh",
                    tint = TextMuted,
                    modifier = Modifier.size(19.dp),
                )
            }
        }
    }
}

@Composable
fun WorkspaceControls(state: AppState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = { state.searchQuery = it },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = null,
                    tint = TextMuted,
                )
            },
            trailingIcon = {
                if (state.searchQuery.isNotBlank()) {
                    IconButton(onClick = { state.searchQuery = "" }) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Clear search",
                            tint = TextMuted,
                        )
                    }
                }
            },
            placeholder = {
                Text(
                    text = "Search pull requests, repositories, or numbers…",
                    color = TextMuted,
                )
            },
            singleLine = true,
            modifier = Modifier
                .weight(1f)
                .onFocusChanged { focusState ->
                    state.keyboardMode = if (focusState.isFocused) {
                        KeyboardMode.Insert
                    } else {
                        KeyboardMode.Normal
                    }
                },
            colors = revqTextFieldColors(),
        )

        WorkspaceMenuControl(
            label = "Sort",
            value = state.sortMode,
            width = 158.dp,
            options = listOf(
                "Urgency",
                "Updated newest",
                "Updated oldest",
                "Repository",
                "Comments",
            ),
            onSelect = {
                state.sortMode = it
                state.saveConfig()
            },
        )

        WorkspaceMenuControl(
            label = "Group",
            value = when {
                state.view == View.Today -> "Sections"
                state.groupByRepository -> "Repository"
                else -> "None"
            },
            width = 138.dp,
            options = listOf("None", "Repository"),
            enabled = state.view != View.Today,
            onSelect = {
                state.groupByRepository = it == "Repository"
                state.saveConfig()
            },
        )

        WorkspaceMenuControl(
            label = "Rows",
            value = if (state.compactRows) "Compact" else "Comfortable",
            width = 145.dp,
            options = listOf("Comfortable", "Compact"),
            onSelect = {
                state.compactRows = it == "Compact"
                state.saveConfig()
            },
        )
    }
}

@Composable
private fun WorkspaceMenuControl(
    label: String,
    value: String,
    width: Dp,
    options: List<String>,
    enabled: Boolean = true,
    onSelect: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Surface(
            modifier = Modifier
                .width(width)
                .height(52.dp)
                .clip(RoundedCornerShape(10.dp))
                .clickable(enabled = enabled) {
                    expanded = true
                },
            color = Color(0xFF171A1E),
            border = BorderStroke(1.dp, Border),
            shape = RoundedCornerShape(10.dp),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = label.uppercase(),
                        color = TextMuted,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = value,
                        color = if (enabled) TextPrimary else TextMuted,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Icon(
                    imageVector = Icons.Rounded.KeyboardArrowDown,
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
                .width(width)
                .background(PanelBg),
        ) {
            Text(
                text = label.uppercase(),
                color = TextMuted,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            )

            options.forEach { option ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            expanded = false
                            onSelect(option)
                        }
                        .background(
                            if (option == value) OliveSoft else Color.Transparent,
                        )
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(
                                if (option == value) Olive else Color.Transparent,
                            ),
                    )
                    Spacer(Modifier.width(9.dp))
                    Text(
                        text = option,
                        color = if (option == value) Olive else TextPrimary,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@Composable
fun RefreshErrorBanner(state: AppState) {
    val message = state.lastRefreshError.orEmpty()
    var expanded by remember(message) { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF291E21)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            Icon(
                imageVector = Icons.Rounded.ErrorOutline,
                contentDescription = null,
                tint = Rose,
                modifier = Modifier.size(18.dp),
            )

            Text(
                text = "Refresh failed",
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
            )

            Text(
                text = compactRefreshError(message),
                color = TextMuted,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )

            TextButton(onClick = { expanded = !expanded }) {
                Text(
                    text = if (expanded) "Hide details" else "Details",
                    color = Amber,
                )
            }

            TextButton(
                onClick = { state.refresh() },
                enabled = !state.isRefreshing,
            ) {
                Text("Retry", color = Amber)
            }
        }

        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 45.dp, end = 18.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = message.take(1200),
                    color = TextMuted,
                    style = MaterialTheme.typography.bodySmall,
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { state.selectView(View.Settings) }) {
                        Text("Open Settings", color = Amber)
                    }
                    TextButton(onClick = { state.testGithubCli() }) {
                        Text("Test GitHub CLI", color = Amber)
                    }
                    TextButton(onClick = { state.copyDiagnostics() }) {
                        Text("Copy diagnostics", color = Amber)
                    }
                }
            }
        }
    }
}

@Composable
fun PullRequestList(state: AppState) {
    val prs = state.visiblePullRequests()

    if (state.reviewSessionActive && state.reviewQueue().isEmpty()) {
        SessionCompleteState(state)
        return
    }

    if (prs.isEmpty()) {
        EmptyState(state)
        return
    }

    val listState = rememberLazyListState()
    val selected = state.selectedPullRequest
    val visibleLazyItemCount = listState.layoutInfo.visibleItemsInfo.size

    LaunchedEffect(visibleLazyItemCount) {
        if (visibleLazyItemCount > 0) {
            state.keyboardPageStep = max(1, visibleLazyItemCount / 2)
        }
    }

    val todaySections = if (state.view == View.Today) {
        listOf(
            "NEEDS YOUR REVIEW" to prs.filter {
                it.source == PullRequestSource.ReviewRequest
            },
            "MY PULL REQUESTS" to prs.filter {
                it.source == PullRequestSource.Mine &&
                        attentionKind(it) != AttentionKind.Ready
            },
            "READY TO MOVE" to prs.filter {
                attentionKind(it) == AttentionKind.Ready
            },
        )
    } else {
        emptyList()
    }

    val grouped = if (
        state.view != View.Today &&
        state.groupByRepository
    ) {
        prs.groupBy { it.repository.toString() }
    } else {
        emptyMap()
    }

    val selectedLazyIndex = selected?.let {
        lazyIndexForSelectedPr(
            state = state,
            selected = it,
            prs = prs,
            todaySections = todaySections,
            grouped = grouped,
        )
    } ?: -1

    LaunchedEffect(
        selected?.key,
        selected?.source,
        selectedLazyIndex,
        state.view,
        state.groupByRepository,
    ) {
        if (selectedLazyIndex >= 0) {
            val visibleIndices = listState.layoutInfo.visibleItemsInfo
                .map { it.index }

            if (selectedLazyIndex !in visibleIndices) {
                listState.animateScrollToItem(selectedLazyIndex)
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
    ) {
        when {
            state.view == View.Today -> {
                todaySections.forEach { (title, sectionPrs) ->
                    section(
                        title = title,
                        prs = sectionPrs,
                        state = state,
                    )
                }
            }

            state.groupByRepository -> {
                grouped.forEach { (repository, repositoryPrs) ->
                    section(
                        title = repository,
                        prs = repositoryPrs,
                        state = state,
                        uppercaseTitle = false,
                    )
                }
            }

            state.view == View.NeedsReview -> {
                section(
                    title = if (state.reviewSessionActive) {
                        "SESSION QUEUE"
                    } else {
                        "UP NEXT"
                    },
                    prs = prs,
                    state = state,
                    markFirst = !state.reviewSessionActive,
                )
            }

            else -> {
                items(
                    items = prs,
                    key = { it.key + it.source.name },
                ) { pr ->
                    PullRequestRow(
                        state = state,
                        pr = pr,
                        startHere = false,
                    )
                }
            }
        }
    }
}

private fun lazyIndexForSelectedPr(
    state: AppState,
    selected: PullRequest,
    prs: List<PullRequest>,
    todaySections: List<Pair<String, List<PullRequest>>>,
    grouped: Map<String, List<PullRequest>>,
): Int {
    fun matches(pr: PullRequest): Boolean {
        return pr.key == selected.key && pr.source == selected.source
    }

    if (state.view == View.Today) {
        var cursor = 0
        todaySections.forEach { (_, sectionPrs) ->
            if (sectionPrs.isEmpty()) return@forEach
            val index = sectionPrs.indexOfFirst(::matches)
            if (index >= 0) return cursor + 1 + index
            cursor += 1 + sectionPrs.size
        }
        return -1
    }

    if (state.groupByRepository) {
        var cursor = 0
        grouped.values.forEach { repositoryPrs ->
            val index = repositoryPrs.indexOfFirst(::matches)
            if (index >= 0) return cursor + 1 + index
            cursor += 1 + repositoryPrs.size
        }
        return -1
    }

    val index = prs.indexOfFirst(::matches)
    if (index < 0) return -1

    return if (state.view == View.NeedsReview) index + 1 else index
}

private fun LazyListScope.section(
    title: String,
    prs: List<PullRequest>,
    state: AppState,
    markFirst: Boolean = false,
    uppercaseTitle: Boolean = true,
) {
    if (prs.isEmpty()) return

    item(key = "section:$title") {
        SectionHeader(
            title = if (uppercaseTitle) title.uppercase() else title,
            count = prs.size,
        )
    }

    items(
        items = prs,
        key = { it.key + it.source.name },
    ) { pr ->
        PullRequestRow(
            state = state,
            pr = pr,
            startHere = markFirst && prs.firstOrNull()?.key == pr.key,
        )
    }
}

@Composable
private fun SectionHeader(
    title: String,
    count: Int,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF181C20))
            .padding(horizontal = 22.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            color = TextMuted,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
        )

        Text(
            text = count.toString(),
            color = TextMuted,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

@Composable
fun PullRequestRow(
    state: AppState,
    pr: PullRequest,
    startHere: Boolean,
) {
    val selected = state.selectedPullRequest?.key == pr.key &&
            state.selectedPullRequest?.source == pr.source

    val kind = attentionKind(pr)
    val presentation = rowPresentation(state, pr, startHere)
    val rowPadding = if (state.compactRows) 10.dp else 14.dp
    val avatarSize = if (state.compactRows) 34.dp else 40.dp
    val railHeight = if (state.compactRows) 46.dp else 56.dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (selected) Color(0xFF23282E) else PanelBg,
            )
            .clickable {
                state.keyboardMode = KeyboardMode.Normal
                state.keyboardFocusRegion = FocusRegion.PullRequestList
                state.selectedPullRequest = pr
            }
            .padding(horizontal = 22.dp, vertical = rowPadding),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(railHeight)
                .clip(RoundedCornerShape(999.dp))
                .background(
                    when {
                        selected -> colorForKind(kind)
                        startHere -> Olive
                        else -> Color.Transparent
                    },
                ),
        )

        Spacer(Modifier.width(11.dp))

        Box(
            modifier = Modifier
                .size(avatarSize)
                .clip(RoundedCornerShape(11.dp))
                .background(Color(0xFF2B3036))
                .border(
                    width = 1.dp,
                    color = Border,
                    shape = RoundedCornerShape(11.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = repoMonogram(pr),
                color = if (selected) TextPrimary else TextMuted,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(
                if (state.compactRows) 3.dp else 5.dp,
            ),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = pr.title,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )

                if (state.isPinned(pr)) {
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Rounded.Star,
                        contentDescription = "Pinned",
                        tint = InfoBlue,
                        modifier = Modifier.size(16.dp),
                    )
                }

                Spacer(Modifier.width(10.dp))

                Text(
                    text = staleOrRelativeLabel(pr.updatedAt),
                    color = if (isStale(pr.updatedAt)) Amber else TextMuted,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            Text(
                text = rowMetadata(pr),
                color = TextMuted,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(presentation.color),
                )

                Text(
                    text = presentation.text,
                    color = if (presentation.strong) {
                        TextPrimary
                    } else {
                        TextMuted
                    },
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }

    Divider(
        modifier = Modifier.padding(start = 76.dp),
        color = Border,
    )
}

private data class RowPresentation(
    val text: String,
    val color: Color,
    val strong: Boolean = false,
)

private fun rowPresentation(
    state: AppState,
    pr: PullRequest,
    startHere: Boolean,
): RowPresentation {
    if (pr.source == PullRequestSource.Mine) {
        val primary = ownPullRequestPrimaryStatus(pr)
        val signals = ownPullRequestSignals(pr)
        val secondaryCount = (signals.size - 1).coerceAtLeast(0)
        val suffix = if (secondaryCount > 0) " · +$secondaryCount more" else ""

        return RowPresentation(
            text = ownPullRequestRowStatus(pr, primary) + suffix,
            color = colorForOwnPullRequestStatus(primary),
            strong = primary in setOf(
                OwnPullRequestStatus.ChangesRequested,
                OwnPullRequestStatus.MergeConflict,
                OwnPullRequestStatus.ChecksFailing,
                OwnPullRequestStatus.DiscussionNeedsResponse,
                OwnPullRequestStatus.ApprovedAndReady,
            ),
        )
    }

    return when (state.view) {
        View.NeedsReview -> when {
            isStale(pr.updatedAt) -> RowPresentation(
                text = "Waiting ${staleOrRelativeLabel(pr.updatedAt)}",
                color = Amber,
                strong = true,
            )

            startHere -> RowPresentation(
                text = "Up next",
                color = Olive,
                strong = true,
            )

            else -> RowPresentation(
                text = "Review requested",
                color = Olive,
            )
        }

        View.Today -> RowPresentation("Needs review", Olive, true)

        View.Handled -> {
            val changed = whatChanged(state, pr)
            if (changed.startsWith("Updated")) {
                RowPresentation(
                    text = "Changed since reviewed",
                    color = Amber,
                    strong = true,
                )
            } else {
                RowPresentation(
                    text = "Reviewed locally",
                    color = HandledMuted,
                )
            }
        }

        else -> RowPresentation(
            text = "Review requested",
            color = Olive,
            strong = state.view == View.Pinned,
        )
    }
}

private fun rowMetadata(pr: PullRequest): String {
    return buildList {
        add("${pr.repository} #${pr.number}")

        pr.authorLogin
            ?.takeIf { it.isNotBlank() }
            ?.let { add("@$it") }

        if (pr.comments > 0) {
            add(
                if (pr.comments == 1) {
                    "1 comment"
                } else {
                    "${pr.comments} comments"
                },
            )
        }
    }.joinToString(" · ")
}

private fun mainToolbarSubtitle(state: AppState): String {
    return when (state.view) {
        View.NeedsReview -> {
            val queue = state.reviewQueue()
            val oldest = queue
                .minByOrNull {
                    instantOrNull(it.updatedAt) ?: Instant.now()
                }
                ?.let {
                    staleOrRelativeLabel(it.updatedAt)
                }

            buildString {
                append("${queue.size} waiting")
                if (oldest != null) {
                    append(" · oldest $oldest")
                }
            }
        }

        View.Today -> reviewDebtLine(state)

        View.Pinned -> {
            "${state.pinnedPullRequests().size} pinned pull requests"
        }

        View.Mine -> {
            val mine = state.activePullRequests()
                .filter {
                    it.source == PullRequestSource.Mine
                }
            val attention = mine.count(::ownPullRequestNeedsAction)
            val ready = mine.count {
                ownPullRequestPrimaryStatus(it) == OwnPullRequestStatus.ApprovedAndReady
            }
            buildString {
                append("${mine.size} open")
                if (attention > 0) append(" · $attention need attention")
                if (ready > 0) append(" · $ready ready")
            }
        }

        View.Blocked -> {
            "${state.visiblePullRequests().size} possible blockers"
        }

        View.Ready -> {
            "${state.visiblePullRequests().size} look ready to move"
        }

        View.Handled -> {
            "${state.handledPullRequests().size} locally reviewed"
        }

        View.Settings -> state.view.description
    }
}

private fun compactRefreshError(message: String): String {
    return message
        .lineSequence()
        .firstOrNull()
        ?.trim()
        .orEmpty()
        .ifBlank {
            "GitHub refresh could not complete."
        }
}

@Composable
fun BottomStatusBar(
    state: AppState,
    paletteState: CommandPaletteState,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(Color(0xFF0E1012))
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (state.lastUndoReview != null) {
            Text(
                text = "Marked reviewed.",
                color = TextMuted,
                style = MaterialTheme.typography.bodySmall,
            )

            TextButton(onClick = { state.undoMarkReviewed() }) {
                Text("Undo", color = Olive)
            }
        } else {
            val status = when {
                state.isRefreshing -> refreshProgressLine(state)
                state.lastRefreshError != null -> "Refresh failed"
                state.lastRefreshFinishedAt != null -> {
                    "Ready · ${state.reviewQueue().size} reviews waiting"
                }
                else -> state.statusLine
            }

            Text(
                text = status,
                color = if (state.lastRefreshError != null) Rose else TextMuted,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(Modifier.width(12.dp))
        NextKeyboardActionHint(state)
        Spacer(Modifier.width(10.dp))
        KeyboardContextHints(state, paletteState)
        Spacer(Modifier.width(10.dp))

        TextButton(onClick = { paletteState.open() }) {
            Icon(
                imageVector = Icons.Rounded.KeyboardCommandKey,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(6.dp))
            Text("Space More", color = TextMuted)
        }
    }
}

@Composable
private fun NextKeyboardActionHint(state: AppState) {
    val action = nextKeyboardAction(state) ?: return

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(OliveSoft)
            .border(1.dp, Olive.copy(alpha = 0.35f), RoundedCornerShape(999.dp))
            .padding(horizontal = 9.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = action.key,
            color = Olive,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = action.label,
            color = TextPrimary,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun KeyboardContextHints(
    state: AppState,
    paletteState: CommandPaletteState,
) {
    val modeLabel = when {
        paletteState.isOpen -> "PALETTE"
        state.keyboardMode == KeyboardMode.Insert -> "INSERT"
        else -> "NORMAL"
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = modeLabel,
            color = Olive,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
        )

        keyboardHints(state, paletteOpen = paletteState.isOpen).forEach { hint ->
            ShortcutHint(hint.key, hint.label)
        }
    }
}

@Composable
private fun ShortcutHint(
    key: String,
    label: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = key,
            color = TextPrimary,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .clip(RoundedCornerShape(5.dp))
                .background(PanelElevated)
                .border(1.dp, Border, RoundedCornerShape(5.dp))
                .padding(horizontal = 5.dp, vertical = 2.dp),
        )
        Text(
            text = label,
            color = TextMuted,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

@Composable
fun SkeletonList() {
    LazyColumn(Modifier.fillMaxSize().padding(top = 8.dp)) {
        items(8) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 15.dp), verticalAlignment = Alignment.Top) {
                SkeletonBox(34.dp, 34.dp, RoundedCornerShape(9.dp))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SkeletonBox(320.dp, 14.dp, RoundedCornerShape(7.dp))
                    SkeletonBox(240.dp, 11.dp, RoundedCornerShape(6.dp))
                    SkeletonBox(170.dp, 10.dp, RoundedCornerShape(5.dp))
                }
                SkeletonBox(54.dp, 10.dp, RoundedCornerShape(5.dp))
            }
            Divider(color = Border)
        }
    }
}

@Composable
fun SkeletonBox(width: Dp, height: Dp, shape: RoundedCornerShape) {
    Box(
        Modifier
            .width(width)
            .height(height)
            .clip(shape)
            .background(Brush.horizontalGradient(listOf(Color(0xFF272B30), Color(0xFF343A41), Color(0xFF272B30))))
    )
}

@Composable
fun SessionCompleteState(state: AppState) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.widthIn(max = 520.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = null,
                tint = ReadyGreen,
                modifier = Modifier.size(38.dp),
            )

            Text(
                text = "Review session complete",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge,
            )

            Text(
                text = "Nothing from this review queue is waiting on you now.",
                color = TextMuted,
            )

            if (state.sessionHandledCount > 0) {
                Text(
                    text = "${state.sessionHandledCount} reviewed in this session",
                    color = Olive,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(
                    onClick = {
                        state.selectView(View.Mine)
                    },
                ) {
                    Text("View My Pull Requests")
                }

                TextButton(onClick = { state.endReviewSession() }) {
                    Text("Done", color = TextMuted)
                }
            }
        }
    }
}

@Composable
fun EmptyState(state: AppState) {
    val spec = emptyStateSpec(state.view)

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.widthIn(max = 560.dp).padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            EmptyStateIllustration(spec)

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    spec.title,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.headlineSmall,
                )
                Text(
                    spec.subtitle,
                    color = TextMuted,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(
                    onClick = { spec.primaryAction(state) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Olive,
                        contentColor = Color(0xFF151812),
                    ),
                ) {
                    Text(spec.primaryLabel)
                }

                spec.secondaryLabel?.let { label ->
                    TextButton(
                        onClick = { spec.secondaryAction?.invoke(state) },
                        enabled = spec.secondaryAction != null,
                    ) {
                        Text(label, color = TextMuted)
                    }
                }
            }
        }
    }
}

internal data class EmptyStateSpec(
    val eyebrow: String,
    val title: String,
    val subtitle: String,
    val accent: Color,
    val icon: ImageVector,
    val heroLabel: String,
    val detailLabel: String,
    val primaryLabel: String,
    val primaryAction: (AppState) -> Unit,
    val secondaryLabel: String? = null,
    val secondaryAction: ((AppState) -> Unit)? = null,
)

internal fun emptyStateSpec(view: View): EmptyStateSpec {
    return when (view) {
        View.NeedsReview -> EmptyStateSpec(
            eyebrow = "QUEUE CLEAR",
            title = "You're caught up.",
            subtitle = "Nothing is waiting for your review right now. New review requests will appear here automatically.",
            accent = ReadyGreen,
            icon = Icons.Rounded.CheckCircle,
            heroLabel = "Inbox zero",
            detailLabel = "No pending reviews",
            primaryLabel = "View My Pull Requests",
            primaryAction = { it.selectView(View.Mine) },
            secondaryLabel = "Refresh",
            secondaryAction = { it.refresh() },
        )
        View.Today -> EmptyStateSpec(
            eyebrow = "ALL CLEAR",
            title = "Nothing urgent right now.",
            subtitle = "There are no review requests or follow-ups that need attention in your Today view.",
            accent = Olive,
            icon = Icons.Rounded.CheckCircle,
            heroLabel = "Today is quiet",
            detailLabel = "No immediate follow-ups",
            primaryLabel = "Open Needs Review",
            primaryAction = { it.selectView(View.NeedsReview) },
            secondaryLabel = "Refresh",
            secondaryAction = { it.refresh() },
        )
        View.Pinned -> EmptyStateSpec(
            eyebrow = "NO PINNED ITEMS",
            title = "Nothing pinned yet.",
            subtitle = "Pin important pull requests from the Review Brief to keep them close while you work.",
            accent = InfoBlue,
            icon = Icons.Rounded.KeyboardCommandKey,
            heroLabel = "Pin important PRs",
            detailLabel = "Use the star in the brief panel",
            primaryLabel = "Browse Needs Review",
            primaryAction = { it.selectView(View.NeedsReview) },
        )
        View.Blocked -> EmptyStateSpec(
            eyebrow = "NO BLOCKERS",
            title = "No possible blockers found.",
            subtitle = "RevQ did not find pull requests that currently look blocked or in need of escalation.",
            accent = Amber,
            icon = Icons.Rounded.ErrorOutline,
            heroLabel = "Healthy queue",
            detailLabel = "Nothing looks blocked",
            primaryLabel = "Browse Today",
            primaryAction = { it.selectView(View.Today) },
            secondaryLabel = "Refresh",
            secondaryAction = { it.refresh() },
        )
        View.Handled -> EmptyStateSpec(
            eyebrow = "REVIEWED IS EMPTY",
            title = "No reviewed PRs yet.",
            subtitle = "PRs you mark reviewed appear here until GitHub reports new activity on them.",
            accent = InfoBlue,
            icon = Icons.Rounded.CheckCircle,
            heroLabel = "Your reviewed pile",
            detailLabel = "Will grow as you clear reviews",
            primaryLabel = "Browse Needs Review",
            primaryAction = { it.selectView(View.NeedsReview) },
        )
        View.Mine -> EmptyStateSpec(
            eyebrow = "NO OPEN PRS",
            title = "No open pull requests.",
            subtitle = "RevQ did not find any open pull requests authored by you in the repositories you track.",
            accent = Olive,
            icon = Icons.Rounded.Search,
            heroLabel = "Nothing authored by you",
            detailLabel = "Try refreshing or adjusting tracked repositories",
            primaryLabel = "Refresh",
            primaryAction = { it.refresh() },
            secondaryLabel = "Open Settings",
            secondaryAction = { it.selectView(View.Settings) },
        )
        else -> EmptyStateSpec(
            eyebrow = "NOTHING HERE",
            title = "Nothing here yet.",
            subtitle = "Refresh or adjust the repositories RevQ is tracking.",
            accent = Olive,
            icon = Icons.Rounded.Refresh,
            heroLabel = "Waiting for data",
            detailLabel = "Refresh or review settings",
            primaryLabel = "Refresh",
            primaryAction = { it.refresh() },
        )
    }
}

@Composable
internal fun EmptyStateIllustration(spec: EmptyStateSpec) {
    Box(
        modifier = Modifier
            .width(344.dp)
            .height(236.dp),
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(32.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            spec.accent.copy(alpha = 0.12f),
                            PanelBg,
                        ),
                    ),
                )
                .border(BorderStroke(1.dp, Border), RoundedCornerShape(32.dp)),
        )

        DecorativeOrb(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 26.dp, top = 22.dp),
            size = 72.dp,
            color = spec.accent.copy(alpha = 0.12f),
        )

        DecorativeOrb(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 26.dp, bottom = 20.dp),
            size = 54.dp,
            color = spec.accent.copy(alpha = 0.08f),
        )

        Surface(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(end = 18.dp, top = 16.dp)
                .width(188.dp),
            color = Color(0xFF1C2026),
            border = BorderStroke(1.dp, Border),
            shape = RoundedCornerShape(24.dp),
        ) {
            IllustrationNoteCard(
                title = spec.eyebrow,
                accent = TextMuted,
                icon = spec.icon,
                compact = true,
            )
        }

        Surface(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(start = 34.dp)
                .width(208.dp),
            color = PanelElevated,
            border = BorderStroke(1.dp, Border),
            shape = RoundedCornerShape(26.dp),
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(spec.accent.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = spec.icon,
                            contentDescription = null,
                            tint = spec.accent,
                            modifier = Modifier.size(22.dp),
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = spec.heroLabel,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            text = spec.detailLabel,
                            color = TextMuted,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    IllustrationChecklistRow(
                        accent = spec.accent,
                        width = 128.dp,
                        active = true,
                    )
                    IllustrationChecklistRow(
                        accent = spec.accent,
                        width = 148.dp,
                        active = false,
                    )
                    IllustrationChecklistRow(
                        accent = spec.accent,
                        width = 114.dp,
                        active = false,
                    )
                }
            }
        }

        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 26.dp, end = 28.dp)
                .width(120.dp),
            color = Color(0xFF20242A),
            border = BorderStroke(1.dp, Border),
            shape = RoundedCornerShape(18.dp),
        ) {
            IllustrationChipCard(
                title = spec.eyebrow,
                accent = spec.accent,
            )
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 32.dp, bottom = 26.dp)
                .width(144.dp),
            color = Color(0xFF1C2026),
            border = BorderStroke(1.dp, Border),
            shape = RoundedCornerShape(18.dp),
        ) {
            IllustrationChipCard(
                title = spec.detailLabel,
                accent = TextMuted,
            )
        }
    }
}

@Composable
private fun DecorativeOrb(
    modifier: Modifier,
    size: Dp,
    color: Color,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(color),
    )
}

@Composable
private fun IllustrationNoteCard(
    title: String,
    accent: Color,
    icon: ImageVector,
    compact: Boolean,
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(15.dp),
                )
            }

            Text(
                text = title,
                color = TextMuted,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        IllustrationLine(width = 86.dp)
        IllustrationLine(width = 124.dp)
        if (!compact) {
            IllustrationLine(width = 102.dp)
        }
    }
}

@Composable
private fun IllustrationChipCard(
    title: String,
    accent: Color,
) {
    Row(
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(accent),
        )
        Text(
            text = title,
            color = TextMuted,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun IllustrationChecklistRow(
    accent: Color,
    width: Dp,
    active: Boolean,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .background(if (active) accent.copy(alpha = 0.18f) else Color(0xFF242A31)),
            contentAlignment = Alignment.Center,
        ) {
            if (active) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(12.dp),
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(TextMuted.copy(alpha = 0.8f)),
                )
            }
        }

        IllustrationLine(width = width)
    }
}

@Composable
private fun IllustrationLine(width: Dp) {
    Box(
        modifier = Modifier
            .height(8.dp)
            .width(width)
            .clip(RoundedCornerShape(999.dp))
            .background(Color(0xFF2B3138)),
    )
}


@Composable
fun Pill(label: String, color: Color, textColor: Color) {
    Text(
        label,
        color = textColor,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(color).padding(horizontal = 8.dp, vertical = 3.dp),
    )
}

internal val SidebarKeyboardViews = listOf(
    View.NeedsReview,
    View.Mine,
    View.Pinned,
    View.Today,
    View.Blocked,
    View.Ready,
    View.Handled,
)

private fun moveWithinFocusedRegion(
    state: AppState,
    delta: Int,
) {
    when (state.keyboardFocusRegion) {
        FocusRegion.Sidebar -> moveSidebarSelection(state, delta)
        FocusRegion.PullRequestList -> moveSelection(state, delta)
        FocusRegion.ReviewBrief -> Unit
    }
}

private fun moveKeyboardFocus(
    state: AppState,
    direction: Int,
) {
    if (state.view == View.Settings) return

    state.keyboardMode = KeyboardMode.Normal
    state.keyboardFocusRegion = when (state.keyboardFocusRegion) {
        FocusRegion.Sidebar -> {
            if (direction > 0) FocusRegion.PullRequestList else FocusRegion.Sidebar
        }

        FocusRegion.PullRequestList -> {
            when {
                direction < 0 -> {
                    if (state.view in SidebarKeyboardViews) {
                        state.sidebarKeyboardView = state.view
                    }
                    FocusRegion.Sidebar
                }

                direction > 0 && state.selectedPullRequest != null -> FocusRegion.ReviewBrief
                else -> FocusRegion.PullRequestList
            }
        }

        FocusRegion.ReviewBrief -> {
            if (direction < 0) FocusRegion.PullRequestList else FocusRegion.ReviewBrief
        }
    }
}

private fun moveToRegionBoundary(
    state: AppState,
    first: Boolean,
) {
    when (state.keyboardFocusRegion) {
        FocusRegion.Sidebar -> {
            state.sidebarKeyboardView = if (first) {
                SidebarKeyboardViews.first()
            } else {
                SidebarKeyboardViews.last()
            }
        }

        FocusRegion.PullRequestList -> {
            val items = state.visiblePullRequests()
            if (items.isNotEmpty()) {
                state.selectedPullRequest = if (first) items.first() else items.last()
            }
        }

        FocusRegion.ReviewBrief -> Unit
    }
}

private fun moveByHalfPage(
    state: AppState,
    direction: Int,
) {
    when (state.keyboardFocusRegion) {
        FocusRegion.Sidebar -> moveSidebarSelection(state, direction * 3)
        FocusRegion.PullRequestList -> moveSelection(
            state = state,
            delta = direction * max(1, state.keyboardPageStep),
        )
        FocusRegion.ReviewBrief -> Unit
    }
}

private fun activateFocusedRegion(state: AppState) {
    when (state.keyboardFocusRegion) {
        FocusRegion.Sidebar -> state.selectView(state.sidebarKeyboardView)

        FocusRegion.PullRequestList -> {
            if (state.selectedPullRequest == null) {
                state.visiblePullRequests().firstOrNull()?.let {
                    state.selectedPullRequest = it
                }
            } else {
                state.keyboardFocusRegion = FocusRegion.ReviewBrief
            }
        }

        FocusRegion.ReviewBrief -> state.openSelectedInGitHub()
    }
}

private fun escapeKeyboardContext(state: AppState) {
    when (state.keyboardFocusRegion) {
        FocusRegion.ReviewBrief -> state.keyboardFocusRegion = FocusRegion.PullRequestList
        FocusRegion.PullRequestList -> {
            if (state.selectedPullRequest != null) {
                state.selectedPullRequest = null
            }
        }
        FocusRegion.Sidebar -> {
            if (state.view != View.Settings) {
                state.keyboardFocusRegion = FocusRegion.PullRequestList
            }
        }
    }
}

private fun moveSidebarSelection(
    state: AppState,
    delta: Int,
) {
    val currentIndex = SidebarKeyboardViews.indexOf(state.sidebarKeyboardView)
        .takeIf { it >= 0 }
        ?: SidebarKeyboardViews.indexOf(state.view).coerceAtLeast(0)
    val nextIndex = (currentIndex + delta).coerceIn(0, SidebarKeyboardViews.lastIndex)
    state.sidebarKeyboardView = SidebarKeyboardViews[nextIndex]
}

fun moveSelection(state: AppState, delta: Int) {
    val items = state.visiblePullRequests()
    if (items.isEmpty()) return
    val current = state.selectedPullRequest
    val index = current?.let {
        items.indexOfFirst { pr -> pr.key == it.key && pr.source == it.source }
    } ?: -1
    val next = when {
        index < 0 -> if (delta < 0) items.lastIndex else 0
        else -> (index + delta).coerceIn(0, items.lastIndex)
    }
    state.selectedPullRequest = items[next]
}


fun installBestEffortTray(state: AppState) {
    runCatching {
        if (!SystemTray.isSupported()) return
        val tray = SystemTray.getSystemTray()
        if (tray.trayIcons.any { it.toolTip == "RevQ" }) return
        val image = BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB)
        val g = image.createGraphics()
        g.color = java.awt.Color(167, 196, 106)
        g.fillOval(2, 2, 12, 12)
        g.dispose()
        val popup = PopupMenu()
        popup.add(MenuItem("Show RevQ").apply { addActionListener { state.selectView(View.NeedsReview) } })
        popup.add(MenuItem("Refresh").apply { addActionListener { state.refresh() } })
        popup.add(MenuItem("Start reviewing").apply { addActionListener { state.startReviewing() } })
        popup.addSeparator()
        popup.add(MenuItem("Quit").apply { addActionListener { exitProcess(0) } })
        tray.add(TrayIcon(image, "RevQ", popup).apply { isImageAutoSize = true })
        state.statusLine = "RevQ tray installed"
    }.onFailure {
        state.statusLine = "Tray unavailable on this desktop session"
    }
}


fun dedupePullRequests(prs: List<PullRequest>): List<PullRequest> {
    // If the same PR appears both as "review requested" and "mine", keep it under
    // My Pull Requests. A PR opened by the current user should not increase the
    // Needs Review count, even when GitHub search returns it in both result sets.
    return prs
        .groupBy { it.key }
        .values
        .map { duplicates ->
            duplicates.firstOrNull { it.source == PullRequestSource.Mine } ?: duplicates.first()
        }
}

fun serializePullRequest(pr: PullRequest): String = listOf(
    pr.repository.owner,
    pr.repository.name,
    pr.number.toString(),
    pr.title.escapeTsv(),
    pr.url.escapeTsv(),
    pr.updatedAt.orEmpty().escapeTsv(),
    pr.comments.toString(),
    pr.source.name,
    pr.authorLogin.orEmpty().escapeTsv(),
    pr.isDraft.toString(),
    pr.reviewDecision.orEmpty().escapeTsv(),
    pr.mergeable.orEmpty().escapeTsv(),
    pr.mergeStateStatus.orEmpty().escapeTsv(),
    pr.reviewRequestsCount.toString(),
    pr.checksTotal.toString(),
    pr.checksFailing.toString(),
    pr.checksPending.toString(),
    pr.unresolvedDiscussionCount?.toString().orEmpty(),
    serializeIdentityList(pr.requestedReviewers),
    serializeIdentityList(pr.changeRequestReviewers),
    serializeIdentityList(pr.approvingReviewers),
    serializeIdentityList(pr.unresolvedDiscussionAuthors),
).joinToString("\t")

fun deserializePullRequest(line: String): PullRequest? {
    val parts = line.split("\t")
    if (parts.size < 8) return null
    return PullRequest(
        repository = RepositoryId(parts[0], parts[1]),
        number = parts[2].toIntOrNull() ?: return null,
        title = parts[3].unescapeTsv(),
        url = parts[4].unescapeTsv(),
        updatedAt = parts[5].unescapeTsv().ifBlank { null },
        comments = parts[6].toIntOrNull() ?: 0,
        source = runCatching { PullRequestSource.valueOf(parts[7]) }.getOrNull() ?: PullRequestSource.ReviewRequest,
        authorLogin = parts.getOrNull(8)?.unescapeTsv()?.ifBlank { null },
        isDraft = parts.getOrNull(9)?.toBooleanStrictOrNull() ?: false,
        reviewDecision = parts.getOrNull(10)?.unescapeTsv()?.ifBlank { null },
        mergeable = parts.getOrNull(11)?.unescapeTsv()?.ifBlank { null },
        mergeStateStatus = parts.getOrNull(12)?.unescapeTsv()?.ifBlank { null },
        reviewRequestsCount = parts.getOrNull(13)?.toIntOrNull() ?: 0,
        checksTotal = parts.getOrNull(14)?.toIntOrNull() ?: 0,
        checksFailing = parts.getOrNull(15)?.toIntOrNull() ?: 0,
        checksPending = parts.getOrNull(16)?.toIntOrNull() ?: 0,
        unresolvedDiscussionCount = parts.getOrNull(17)?.let { raw ->
            when {
                raw.isBlank() -> null
                raw.toIntOrNull() != null -> raw.toIntOrNull()
                raw.toBooleanStrictOrNull() == true -> 1
                raw.toBooleanStrictOrNull() == false -> 0
                else -> null
            }
        },
        requestedReviewers = deserializeIdentityList(parts.getOrNull(18)),
        changeRequestReviewers = deserializeIdentityList(parts.getOrNull(19)),
        approvingReviewers = deserializeIdentityList(parts.getOrNull(20)),
        unresolvedDiscussionAuthors = deserializeIdentityList(parts.getOrNull(21)),
    )
}

fun serializeIdentityList(values: List<String>): String = values
    .map { it.trim() }
    .filter { it.isNotBlank() }
    .distinct()
    .joinToString(",")

fun deserializeIdentityList(value: String?): List<String> = value
    .orEmpty()
    .split(",")
    .map { it.trim() }
    .filter { it.isNotBlank() }
    .distinct()

fun formatIdentityList(values: List<String>, maxVisible: Int = 2): String? {
    val identities = values
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .distinct()

    if (identities.isEmpty()) return null
    if (identities.size == 1) return identities.first()
    if (identities.size == 2) return "${identities[0]} and ${identities[1]}"

    val visible = identities.take(maxVisible.coerceAtLeast(1))
    val remaining = identities.size - visible.size
    val remainingLabel = if (remaining == 1) "1 other" else "$remaining others"
    return when {
        remaining <= 0 -> visible.joinToString(", ")
        visible.size == 1 -> "${visible.first()} and $remainingLabel"
        else -> "${visible.joinToString(", ")} and $remainingLabel"
    }
}

fun String.escapeTsv(): String = replace("\\", "\\\\").replace("\t", "\\t").replace("\n", "\\n").replace("\r", "")
fun String.unescapeTsv(): String = replace("\\n", "\n").replace("\\t", "\t").replace("\\\\", "\\")

fun isOlderThan(value: String?, days: Long): Boolean {
    val instant = instantOrNull(value) ?: return false
    return instant.isBefore(Instant.now().minus(Duration.ofDays(days)))
}

fun parseReminderTime(value: String): LocalTime? {
    val trimmed = value.trim()
    return runCatching { LocalTime.parse(trimmed) }.getOrNull()
        ?: runCatching { LocalTime.parse(trimmed.padStart(5, '0')) }.getOrNull()
}

fun formatReminderInstant(instant: Instant): String {
    val local = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
    val formatter = DateTimeFormatter.ofPattern("EEE HH:mm")
    return local.format(formatter)
}

fun parseLines(text: String): List<String> = text.lines().map { it.trim() }.filter { it.isNotEmpty() && !it.startsWith("#") }.distinct()
fun Path.safeLines(): List<String> = if (exists()) readLines() else emptyList()
fun parseRepo(value: String): RepositoryId? {
    val parts = value.trim().split("/", limit = 2)
    return if (parts.size == 2 && parts[0].isNotBlank() && parts[1].isNotBlank()) RepositoryId(parts[0], parts[1]) else null
}

fun sortForView(view: View, prs: List<PullRequest>): List<PullRequest> = when (view) {
    View.NeedsReview, View.Blocked, View.Handled -> prs.sortedBy { instantOrNull(it.updatedAt) ?: Instant.EPOCH }
    else -> prs.sortedByDescending { instantOrNull(it.updatedAt) ?: Instant.EPOCH }
}

fun ownPullRequestSignals(pr: PullRequest): List<OwnPullRequestStatus> {
    if (pr.source != PullRequestSource.Mine) return emptyList()

    val signals = mutableListOf<OwnPullRequestStatus>()

    if (pr.isDraft) {
        signals += OwnPullRequestStatus.Draft
    }

    if (pr.reviewDecision.equals("CHANGES_REQUESTED", ignoreCase = true)) {
        signals += OwnPullRequestStatus.ChangesRequested
    }

    if (
        pr.mergeable.equals("CONFLICTING", ignoreCase = true) ||
        pr.mergeStateStatus.equals("DIRTY", ignoreCase = true)
    ) {
        signals += OwnPullRequestStatus.MergeConflict
    }

    if (pr.checksFailing > 0) {
        signals += OwnPullRequestStatus.ChecksFailing
    }

    if (pr.discussionNeedsResponse) {
        signals += OwnPullRequestStatus.DiscussionNeedsResponse
    }

    val mergeStateAllowsReady = pr.mergeStateStatus == null ||
            pr.mergeStateStatus.uppercase() !in setOf("BLOCKED", "BEHIND", "DIRTY", "DRAFT", "UNKNOWN")

    val approvedAndReady =
        !pr.isDraft &&
                pr.reviewDecision.equals("APPROVED", ignoreCase = true) &&
                pr.checksFailing == 0 &&
                pr.checksPending == 0 &&
                pr.mergeable.equals("MERGEABLE", ignoreCase = true) &&
                mergeStateAllowsReady

    if (approvedAndReady) {
        signals += OwnPullRequestStatus.ApprovedAndReady
    }

    val waitingForReviewer =
        !pr.isDraft &&
                !approvedAndReady &&
                (
                        pr.reviewRequestsCount > 0 ||
                                pr.reviewDecision.equals("REVIEW_REQUIRED", ignoreCase = true)
                        )

    if (waitingForReviewer) {
        signals += OwnPullRequestStatus.WaitingForReviewer
    }

    if (signals.isEmpty()) {
        signals += OwnPullRequestStatus.NoActionNeeded
    }

    return signals.distinct()
}

fun ownPullRequestPrimaryStatus(pr: PullRequest): OwnPullRequestStatus =
    ownPullRequestSignals(pr).firstOrNull() ?: OwnPullRequestStatus.NoActionNeeded

fun ownPullRequestNeedsAction(pr: PullRequest): Boolean = ownPullRequestSignals(pr).any {
    it in setOf(
        OwnPullRequestStatus.ChangesRequested,
        OwnPullRequestStatus.MergeConflict,
        OwnPullRequestStatus.ChecksFailing,
        OwnPullRequestStatus.DiscussionNeedsResponse,
    )
}

fun ownPullRequestRowStatus(
    pr: PullRequest,
    status: OwnPullRequestStatus = ownPullRequestPrimaryStatus(pr),
): String {
    return when (status) {
        OwnPullRequestStatus.ChangesRequested ->
            formatIdentityList(pr.changeRequestReviewers, maxVisible = 1)
                ?.let { "Changes requested by $it" }
                ?: ownPullRequestStatusTitle(status)

        OwnPullRequestStatus.WaitingForReviewer ->
            formatIdentityList(pr.requestedReviewers, maxVisible = 2)
                ?.let { "Waiting on $it" }
                ?: ownPullRequestStatusTitle(status)

        OwnPullRequestStatus.ApprovedAndReady ->
            formatIdentityList(pr.approvingReviewers, maxVisible = 2)
                ?.let { "Approved by $it" }
                ?: ownPullRequestStatusTitle(status)

        OwnPullRequestStatus.DiscussionNeedsResponse ->
            formatIdentityList(pr.unresolvedDiscussionAuthors, maxVisible = 1)
                ?.let { "Open discussion with $it" }
                ?: ownPullRequestStatusTitle(status)

        else -> ownPullRequestStatusTitle(status)
    }
}

fun ownPullRequestStatusTitle(status: OwnPullRequestStatus): String = when (status) {
    OwnPullRequestStatus.Draft -> "Draft"
    OwnPullRequestStatus.ChangesRequested -> "Changes requested"
    OwnPullRequestStatus.MergeConflict -> "Merge conflict"
    OwnPullRequestStatus.ChecksFailing -> "Checks failing"
    OwnPullRequestStatus.DiscussionNeedsResponse -> "Discussion needs response"
    OwnPullRequestStatus.ApprovedAndReady -> "Approved and ready"
    OwnPullRequestStatus.WaitingForReviewer -> "Waiting for reviewer"
    OwnPullRequestStatus.NoActionNeeded -> "No action needed"
}

fun ownPullRequestStatusBody(status: OwnPullRequestStatus, pr: PullRequest): String = when (status) {
    OwnPullRequestStatus.Draft ->
        "This pull request is still a draft. Keep working on it, or mark it ready for review when the change is ready."

    OwnPullRequestStatus.ChangesRequested -> {
        val reviewers = formatIdentityList(pr.changeRequestReviewers)
        if (reviewers != null) {
            "$reviewers requested changes. Review the feedback and update the pull request before it can move forward."
        } else {
            "A reviewer requested changes. Review the feedback and update the pull request before it can move forward."
        }
    }

    OwnPullRequestStatus.MergeConflict ->
        "GitHub reports that this pull request cannot merge cleanly. Update the branch and resolve the merge conflict."

    OwnPullRequestStatus.ChecksFailing ->
        "${pr.checksFailing} ${if (pr.checksFailing == 1) "check is" else "checks are"} failing. Open GitHub to inspect the failures and decide what needs to change."

    OwnPullRequestStatus.DiscussionNeedsResponse -> {
        val count = pr.unresolvedDiscussionCount ?: 0
        val authors = formatIdentityList(pr.unresolvedDiscussionAuthors)
        buildString {
            append("$count ${if (count == 1) "review discussion is" else "review discussions are"} still unresolved.")
            if (authors != null) append(" Open ${if (count == 1) "thread involves" else "threads involve"} $authors.")
            append(" Open the review threads and respond or resolve them as appropriate.")
        }
    }

    OwnPullRequestStatus.ApprovedAndReady -> {
        val reviewers = formatIdentityList(pr.approvingReviewers)
        if (reviewers != null) {
            "Approved by $reviewers. Checks are clear and no merge conflict is detected. It looks ready to move forward."
        } else {
            "The pull request is approved, checks are clear, and no merge conflict is detected. It looks ready to move forward."
        }
    }

    OwnPullRequestStatus.WaitingForReviewer -> {
        val reviewers = formatIdentityList(pr.requestedReviewers)
        if (reviewers != null) {
            "Waiting on $reviewers. No action is required from you right now; RevQ will keep tracking the pull request for new activity."
        } else {
            "No action is required from you right now. The pull request is open and waiting for review."
        }
    }

    OwnPullRequestStatus.NoActionNeeded ->
        "RevQ found no current signal that requires your attention. You can leave this pull request alone for now."
}

fun colorForOwnPullRequestStatus(status: OwnPullRequestStatus): Color = when (status) {
    OwnPullRequestStatus.Draft -> InfoBlue
    OwnPullRequestStatus.ChangesRequested -> Rose
    OwnPullRequestStatus.MergeConflict -> Rose
    OwnPullRequestStatus.ChecksFailing -> Rose
    OwnPullRequestStatus.DiscussionNeedsResponse -> Amber
    OwnPullRequestStatus.ApprovedAndReady -> ReadyGreen
    OwnPullRequestStatus.WaitingForReviewer -> InfoBlue
    OwnPullRequestStatus.NoActionNeeded -> TextMuted
}

fun attentionKind(pr: PullRequest): AttentionKind = when {
    pr.source == PullRequestSource.ReviewRequest -> AttentionKind.Review
    pr.source == PullRequestSource.Mine -> when (ownPullRequestPrimaryStatus(pr)) {
        OwnPullRequestStatus.ChangesRequested,
        OwnPullRequestStatus.MergeConflict,
        OwnPullRequestStatus.ChecksFailing -> AttentionKind.Blocked

        OwnPullRequestStatus.DiscussionNeedsResponse -> AttentionKind.Action
        OwnPullRequestStatus.ApprovedAndReady -> AttentionKind.Ready
        OwnPullRequestStatus.Draft,
        OwnPullRequestStatus.WaitingForReviewer,
        OwnPullRequestStatus.NoActionNeeded -> AttentionKind.Quiet
    }
    else -> AttentionKind.Quiet
}

fun colorForKind(kind: AttentionKind): Color = when (kind) {
    AttentionKind.Review -> Olive
    AttentionKind.Action -> Amber
    AttentionKind.Blocked -> Rose
    AttentionKind.Ready -> ReadyGreen
    AttentionKind.Quiet -> TextMuted
}

fun rowLabel(pr: PullRequest): String = when {
    pr.source == PullRequestSource.Mine -> ownPullRequestStatusTitle(ownPullRequestPrimaryStatus(pr))
    else -> "Needs your review"
}

fun attentionReason(pr: PullRequest): String = when {
    pr.source == PullRequestSource.Mine -> ownPullRequestStatusBody(ownPullRequestPrimaryStatus(pr), pr)
    else -> "Review requested from you"
}

fun recommendationTitle(pr: PullRequest): String = when {
    pr.source == PullRequestSource.Mine -> ownPullRequestStatusTitle(ownPullRequestPrimaryStatus(pr))
    else -> "Review this pull request"
}

fun recommendationBody(pr: PullRequest): String = when {
    pr.source == PullRequestSource.Mine -> ownPullRequestStatusBody(ownPullRequestPrimaryStatus(pr), pr)
    else -> "Open the diff, check the changes, and leave an approval, comment, or change request."
}

fun whyItMatters(pr: PullRequest): String = when {
    pr.source == PullRequestSource.Mine -> ownPullRequestStatusBody(ownPullRequestPrimaryStatus(pr), pr)
    else -> "Your review is explicitly requested. This pull request is waiting on your review before it can move forward."
}

fun whatChanged(state: AppState, pr: PullRequest): String {
    val handledAt = state.handledReviewRecords[pr.key]
    return when {
        handledAt == null -> "This item has not been handled in RevQ yet."
        handledAt != pr.updatedMarker -> "Updated after you last handled it."
        else -> "No new activity since you marked it reviewed."
    }
}

fun refreshProgressLine(state: AppState): String {
    return if (state.refreshTotal > 0) {
        "${state.refreshPhase} · ${state.refreshDone} / ${state.refreshTotal} repositories"
    } else {
        state.refreshPhase
    }
}

fun queueSubtitle(state: AppState): String = when (state.view) {
    View.NeedsReview -> "${state.reviewQueue().size} reviews waiting · oldest waiting first"
    View.Today -> reviewDebtLine(state)
    View.Handled -> "${state.handledPullRequests().size} handled reviews · returns when PR changes"
    else -> state.view.description
}

fun reviewDebtLine(state: AppState): String {
    val reviews = state.reviewQueue()
    val mine = state.pullRequests.count { it.source == PullRequestSource.Mine }
    val oldest = reviews.minByOrNull { instantOrNull(it.updatedAt) ?: Instant.now() }?.let { staleOrRelativeLabel(it.updatedAt) }
    return buildString {
        append("${reviews.size} reviews waiting")
        append(" · $mine my PRs")
        if (oldest != null) append(" · oldest $oldest")
    }
}

fun sessionProgressLine(state: AppState): String {
    val keys = state.reviewSessionQueueKeys
    if (keys.isEmpty()) {
        return "${state.reviewQueue().size} pull requests waiting"
    }

    val selectedKey = state.selectedPullRequest?.key
    val index = selectedKey?.let(keys::indexOf) ?: -1

    return when {
        index >= 0 -> {
            val remainingAfterCurrent = keys
                .drop(index + 1)
                .count { key ->
                    state.reviewQueue().any { it.key == key }
                }

            buildString {
                append("Review ${index + 1} of ${keys.size}")
                if (state.sessionHandledCount > 0) {
                    append(" · ${state.sessionHandledCount} reviewed")
                }
                append(" · $remainingAfterCurrent after this")
            }
        }

        state.reviewQueue().isEmpty() -> {
            "${state.sessionHandledCount} reviewed · session complete"
        }

        else -> {
            "${state.sessionHandledCount} reviewed · ${state.reviewQueue().size} still waiting"
        }
    }
}

fun canGoNext(state: AppState): Boolean {
    val reviews = state.reviewQueue()
    val selected = state.selectedPullRequest ?: return false
    val index = reviews.indexOfFirst { it.key == selected.key }

    return if (state.reviewSessionActive) {
        index >= 0 && reviews.size > 1
    } else {
        index >= 0 && index < reviews.lastIndex
    }
}

fun canGoPrevious(state: AppState): Boolean {
    val reviews = state.reviewQueue()
    val selected = state.selectedPullRequest ?: return false
    val index = reviews.indexOfFirst { it.key == selected.key }
    return index > 0
}

fun sortHint(view: View): String = when (view) {
    View.NeedsReview -> "Sorted by urgency"
    View.Pinned -> "Pinned locally"
    View.Mine -> "Workflow sort"
    View.Blocked -> "Workflow sort"
    View.Ready -> "Workflow sort"
    View.Handled -> "Handled locally"
    View.Today -> "Review requests first"
    View.Settings -> ""
}

fun repoMonogram(pr: PullRequest): String = buildString {
    append(pr.repository.owner.firstOrNull { it.isLetterOrDigit() }?.uppercaseChar() ?: 'P')
    append(pr.repository.name.firstOrNull { it.isLetterOrDigit() }?.uppercaseChar() ?: 'R')
}

fun staleOrRelativeLabel(updatedAt: String?): String {
    val instant = instantOrNull(updatedAt) ?: return "unknown"
    val days = Duration.between(instant, Instant.now()).toDays()
    return when {
        days >= 7 -> "${days}d stale"
        days >= 2 -> "${days}d old"
        else -> relativeInstant(instant)
    }
}

fun isStale(updatedAt: String?): Boolean = instantOrNull(updatedAt)?.let { Duration.between(it, Instant.now()).toDays() >= 2 } ?: false
fun relativeInstant(instant: Instant): String {
    val duration = Duration.between(instant, Instant.now())
    return when {
        duration.toMinutes() < 1 -> "just now"
        duration.toHours() < 1 -> "${duration.toMinutes()}m ago"
        duration.toDays() < 1 -> "${duration.toHours()}h ago"
        duration.toDays() == 1L -> "yesterday"
        else -> "${duration.toDays()}d ago"
    }
}

fun instantOrNull(value: String?): Instant? = try {
    value?.let { Instant.parse(it) }
} catch (_: DateTimeParseException) {
    null
}

fun queuePositionCopy(state: AppState, pr: PullRequest): String {
    val list = if (pr.source == PullRequestSource.ReviewRequest) state.reviewQueue() else state.visiblePullRequests()
    val index = list.indexOfFirst { it.key == pr.key && it.source == pr.source }
    return if (index >= 0) "Item ${index + 1} of ${list.size}" else "Selected pull request"
}

fun copyToClipboard(text: String) {
    runCatching {
        Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(text), null)
    }
}

fun openUrl(url: String) {
    runCatching {
        if (Desktop.isDesktopSupported()) Desktop.getDesktop().browse(URI(url))
    }
}
