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
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.foundation.focusable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.KeyboardCommandKey
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.Typography
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
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
import java.awt.EventQueue
import java.awt.MenuItem
import java.awt.PopupMenu
import java.awt.SystemTray
import java.awt.Toolkit
import java.awt.TrayIcon
import java.awt.datatransfer.StringSelection
import java.awt.image.BufferedImage
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
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
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.concurrent.TimeUnit
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeoutException
import javax.imageio.ImageIO
import kotlin.io.path.exists
import kotlin.io.path.readLines
import kotlin.io.path.writeLines
import kotlin.math.max
import kotlin.system.exitProcess
import eu.revq.commands.CommandId
import eu.revq.commands.CommandRegistry
import eu.revq.keyboard.FocusRegion
import eu.revq.keyboard.KeyboardAction
import eu.revq.keyboard.KeyboardContext
import eu.revq.keyboard.KeyboardMode
import eu.revq.keyboard.KeyboardRouter
import eu.revq.ui.commandpalette.CommandPalette
import eu.revq.ui.commandpalette.CommandPaletteState
import eu.revq.ui.commandpalette.PaletteMode

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
    val nativeDensity = LocalDensity.current
    val detectedEnvironmentScale = remember {
        desktopEnvironmentScale() ?: currentCosmicDisplayScale()
    }
    val effectiveDensity = effectiveDesktopDensity(
        composeDensity = nativeDensity.density,
        graphicsScale = currentGraphicsScale(),
        toolkitDpi = runCatching { Toolkit.getDefaultToolkit().screenResolution }.getOrNull(),
        environmentScale = detectedEnvironmentScale,
    )
    CompositionLocalProvider(
        LocalDensity provides Density(effectiveDensity, nativeDensity.fontScale),
    ) {
        MaterialTheme(colorScheme = RevqColorScheme, typography = RevqTypography, content = content)
    }
}

fun effectiveDesktopDensity(
    composeDensity: Float,
    graphicsScale: Float? = null,
    toolkitDpi: Int? = null,
    environmentScale: Float? = null,
): Float {
    if (composeDensity > 1.05f) return composeDensity
    return listOfNotNull(
        composeDensity,
        graphicsScale,
        toolkitDpi?.div(96f),
        environmentScale,
    ).maxOrNull()?.coerceIn(1f, 4f) ?: composeDensity
}

private fun currentGraphicsScale(): Float? = runCatching {
    java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment()
        .defaultScreenDevice
        .defaultConfiguration
        .defaultTransform
        .scaleX
        .toFloat()
}.getOrNull()

private fun desktopEnvironmentScale(): Float? = sequenceOf(
    "GDK_SCALE",
    "QT_SCALE_FACTOR",
)
    .mapNotNull { System.getenv(it)?.toFloatOrNull() }
    .firstOrNull { it > 1f }

fun parseCosmicDisplayScale(output: String): Float? {
    val plainText = output.replace(Regex("\u001B\\[[;\\d]*m"), "")
    return Regex("""Scale:\s*([0-9]+(?:\.[0-9]+)?)%""")
        .findAll(plainText)
        .mapNotNull { match -> match.groupValues[1].toFloatOrNull()?.div(100f) }
        .filter { it >= 1f }
        .maxOrNull()
}

private fun currentCosmicDisplayScale(): Float? {
    val desktop = System.getenv("XDG_CURRENT_DESKTOP").orEmpty()
    val session = System.getenv("XDG_SESSION_TYPE").orEmpty()
    if (!desktop.contains("COSMIC", ignoreCase = true) || !session.equals("wayland", ignoreCase = true)) {
        return null
    }

    return runCatching {
        val process = ProcessBuilder("cosmic-randr", "list")
            .redirectErrorStream(true)
            .start()
        val output = process.inputStream.bufferedReader().readText()
        val finished = process.waitFor(2, TimeUnit.SECONDS)
        if (!finished) {
            process.destroyForcibly()
            null
        } else {
            parseCosmicDisplayScale(output)
        }
    }.getOrNull()
}

fun recommendedJavaUiScale(
    desktop: String?,
    sessionType: String?,
    cosmicScale: Float?,
): String? = PlatformPresence.recommendedJavaUiScale(desktop, sessionType, cosmicScale)

private fun configureJavaDesktopUiScale() {
    if (!System.getProperty("sun.java2d.uiScale").isNullOrBlank()) return

    val scale = recommendedJavaUiScale(
        desktop = System.getenv("XDG_CURRENT_DESKTOP"),
        sessionType = System.getenv("XDG_SESSION_TYPE"),
        cosmicScale = currentCosmicDisplayScale(),
    ) ?: return

    System.setProperty("sun.java2d.uiScale", scale)
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

fun main() {
    configureJavaDesktopUiScale()

    application {
        val appState = remember {
            AppState(applicationLifecycle = ApplicationLifecycle { exitApplication() })
        }
        val mainWindowState = remember { WindowState(size = DpSize(1680.dp, 1040.dp), position = WindowPosition.Aligned(Alignment.Center)) }

        LaunchedEffect(Unit) {
            appState.loadFromDisk()
            appState.startOnboarding()
            installBestEffortTray(appState)
            appState.startReminderScheduler()
            appState.startAutoRefreshScheduler()
            appState.startUpdateScheduler()
            if (appState.repositoriesText.isNotBlank()) {
                appState.refresh()
            } else {
                appState.statusLine = "No repositories configured · open Settings to add one"
            }
        }

        Window(
            onCloseRequest = {
                if (appState.trayAvailable) {
                    appState.mainWindowVisible = false
                    appState.statusLine = "RevQ is still running in the tray"
                } else {
                    exitApplication()
                }
            },
            visible = appState.mainWindowVisible,
            state = mainWindowState,
            title = "RevQ",
            icon = appBrandPainter(),
        ) {
            DisposableEffect(window) {
                val listener = object : WindowAdapter() {
                    override fun windowActivated(event: WindowEvent?) {
                        appState.mainWindowFocusRequest += 1
                    }
                }
                window.addWindowListener(listener)
                onDispose { window.removeWindowListener(listener) }
            }
            RevqTheme(uiScale = appState.uiScale) {
                when {
                    !appState.applicationLoaded -> RevqLaunchScreen()
                    appState.onboardingRequired -> OnboardingScreen(appState)
                    else -> RevqApp(appState)
                }
            }
        }

        if (appState.showReminderWindow) {
            Window(
                onCloseRequest = { appState.closeReminderWindow() },
                title = "RevQ Review Reminder",
                icon = appBrandPainter(),
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

internal val WorkspaceSortModes = listOf(
    "Urgency",
    "Updated newest",
    "Updated oldest",
    "Repository",
    "Comments",
)

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
    val reviewRequestKind: ReviewRequestKind? = null,
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
data class ActionFeedback(
    val message: String,
    val sequence: Long,
)

data class QueueViewportState(
    val firstVisibleItemIndex: Int,
    val firstVisibleItemScrollOffset: Int,
)

data class RefreshDelta(
    val newCount: Int,
    val updatedCount: Int,
    val removedCount: Int,
) {
    val totalChanges: Int get() = newCount + updatedCount + removedCount

    fun summaryText(): String = buildList {
        if (newCount > 0) add("$newCount new")
        if (updatedCount > 0) add("$updatedCount updated")
        if (removedCount > 0) add("$removedCount removed")
    }.joinToString(" · ")
}

class AppState(
    private val pullRequestIntake: PullRequestIntake = PullRequestIntake(GhClient),
    private val settingsStore: SettingsStore = FileSettingsStore(),
    private val configDirectory: Path = defaultRevqConfigDirectory(),
    private val repositoryCatalog: RepositoryCatalogGateway = GhClient,
    private val discoveryTimeoutMillis: Long = 30_000,
    updateService: UpdateService? = null,
    onboardingCoordinator: OnboardingCoordinator? = null,
    applicationLifecycle: ApplicationLifecycle = ApplicationLifecycle {},
) {
    private val scope = MainScope()
    private val configDir: Path = configDirectory
    private val handledFile: Path = configDir.resolve("handled-reviews.txt")
    private val uiScaleFile: Path = configDir.resolve("ui-scale.txt")
    private val legacyCacheFile: Path = configDir.resolve("pull-requests-cache.tsv")
    private val pinnedFile: Path = configDir.resolve("pinned-prs.txt")
    private val reminderSnoozedUntilFile: Path = configDir.resolve("reminder-snoozed-until.txt")
    private val reminderDismissedDateFile: Path = configDir.resolve("reminder-dismissed-date.txt")
    private var reminderSchedulerJob: Job? = null
    private var autoRefreshJob: Job? = null
    private var refreshSummaryJob: Job? = null
    private var updateSchedulerJob: Job? = null
    private val updater: UpdateService = updateService ?: UpdateService(
        installedVersion = AppVersion.parse(System.getProperty("revq.app.version", "0.1.0")),
        releaseSource = CodebergReleaseSource(),
        preferences = SettingsUpdatePreferencesStore(settingsStore),
        downloader = HttpUpdateDownloadGateway(),
        applicationLifecycle = ApplicationLifecycle {
            scope.launch { applicationLifecycle.exitForUpdate() }
        },
    )
    private var onboarding: OnboardingCoordinator? = onboardingCoordinator
    private var scheduledReminderPending = false
    private var sidebarNavigationHintShownThisSession = false

    var view by mutableStateOf(View.NeedsReview)
    var searchQuery by mutableStateOf("")
    var repositoriesText by mutableStateOf("")
    var organizationsText by mutableStateOf("")
    var ghPathText by mutableStateOf("")
    var pullRequests by mutableStateOf(emptyList<PullRequest>())
    var selectedPullRequest by mutableStateOf<PullRequest?>(null)
    var expandedPullRequestKey by mutableStateOf<String?>(null)
    var handledReviewRecords by mutableStateOf(emptyMap<String, String>())
    var isRefreshing by mutableStateOf(false)
    var isDiscovering by mutableStateOf(false)
    var repositoryDiscovery by mutableStateOf<RepositoryDiscoveryResult?>(null)
    var repositoryScopeSelection by mutableStateOf(RepositoryScopeSelection())
    var repositoryDiscoveryQuery by mutableStateOf("")
    var repositoryDiscoveryError by mutableStateOf<String?>(null)
    var repositoryScopeHealth by mutableStateOf<Map<String, RepositoryHealth>>(emptyMap())
        private set

    // Tracking discovery is intentionally separate from active tracking.
    // Discovery only populates choices; repositoriesText changes only after explicit apply.
    var discoveredTrackingRepositories by mutableStateOf<List<String>>(emptyList())
    var pendingTrackedRepositories by mutableStateOf<Set<String>>(emptySet())
    var lastRefreshStartedAt by mutableStateOf<Instant?>(null)
    var lastRefreshFinishedAt by mutableStateOf<Instant?>(null)
    var lastRefreshError by mutableStateOf<String?>(null)
    var statusLine by mutableStateOf("Ready")
    var showReminderWindow by mutableStateOf(false)
    var isTestingGh by mutableStateOf(false)
    var ghTestResult by mutableStateOf<String?>(null)
    var ghDetectionSource by mutableStateOf("Not detected")
    var refreshPhase by mutableStateOf("Idle")
    var refreshDone by mutableStateOf(0)
    var refreshTotal by mutableStateOf(0)
    var lastUndoReview by mutableStateOf<HandledUndo?>(null)
    var mainWindowVisible by mutableStateOf(true)
    var trayAvailable by mutableStateOf(false)
    var mainWindowFocusRequest by mutableStateOf(0)
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
    var displayDiagnostics by mutableStateOf("Display metrics not captured yet")
    var actionFeedback by mutableStateOf<ActionFeedback?>(null)
    private var actionFeedbackSequence = 0L
    var newPullRequestKeys by mutableStateOf(emptySet<String>())
    var updatedPullRequestKeys by mutableStateOf(emptySet<String>())
    var refreshDelta by mutableStateOf<RefreshDelta?>(null)
    var needsReviewHasUnseenChanges by mutableStateOf(false)
    var showAboutDialog by mutableStateOf(false)
    var sidebarNavigationHintVisible by mutableStateOf(false)
    var isMergingPullRequest by mutableStateOf(false)
    var mergingPullRequestKey by mutableStateOf<String?>(null)
    var recentCommandIds by mutableStateOf<List<CommandId>>(emptyList())
    var recentPaletteTargets by mutableStateOf<List<String>>(emptyList())
        private set
    var updateState by mutableStateOf<UpdateState>(updater.state)
        private set
    var onboardingState by mutableStateOf(OnboardingState())
        private set
    var githubIdentity by mutableStateOf<GitHubIdentity?>(null)
        private set
    var applicationLoaded by mutableStateOf(false)
        private set

    init {
        updater.observeState { value ->
            scope.launch {
                if (updater.state == value) {
                    updateState = value
                }
            }
        }
    }

    // Keyboard-navigation state. Palette state is kept at the UI shell level.
    var keyboardMode by mutableStateOf(KeyboardMode.Normal)
    var keyboardFocusRegion by mutableStateOf(FocusRegion.PullRequestList)
    var sidebarKeyboardView by mutableStateOf(View.NeedsReview)
    var keyboardPageStep by mutableStateOf(6)
    var settingsSectionIndex by mutableStateOf(0)
    var settingsFocusedRowIndex by mutableStateOf(0)
    private var queueSelectionKeys by mutableStateOf<Map<View, String>>(emptyMap())
    private var queueViewportStates by mutableStateOf<Map<View, QueueViewportState>>(emptyMap())
    var queueScopeFilters by mutableStateOf<Map<View, QueueScopeFilter>>(emptyMap())

    fun loadFromDisk() {
        Files.createDirectories(configDir)
        var settings = settingsStore.load()
        if (!settings.onboardingCompleted && settings.repositories.isNotEmpty()) {
            settings = settings.copy(onboardingCompleted = true)
            settingsStore.save(settings)
        }
        githubIdentity = settings.githubIdentityLogin
            .takeIf(String::isNotBlank)
            ?.let { GitHubIdentity(it, settings.githubIdentityHost.ifBlank { "github.com" }) }
        repositoriesText = settings.repositories.joinToString("\n")
        organizationsText = settings.organizations.joinToString("\n")
        ghPathText = settings.githubExecutable
        ghDetectionSource = settings.githubDetectionSource
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
            ghDetectionSource = settings.githubDetectionSource.ifBlank { GhDetectionSource.Configured.label }
        }
        uiScale = 1.0f
        densityMode = "OS automatic"
        Files.deleteIfExists(uiScaleFile)
        reminderEnabled = settings.reminderEnabled
        reminderTimeText = settings.reminderTime
        reminderDaysText = settings.reminderDays
        quietHoursText = settings.quietHours
        remindOnlyWhenQueueNotClear = settings.remindOnlyWhenQueueNotClear
        reminderSnoozeMinutesText = settings.reminderSnoozeMinutes
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
        mutedRepositoriesText = settings.mutedRepositories.joinToString("\n")
        autoRefreshEnabled = settings.autoRefreshEnabled
        autoRefreshIntervalMinutesText = settings.autoRefreshIntervalMinutes
        sortMode = settings.sortMode
        groupByRepository = settings.groupByRepository
        staleThresholdDaysText = settings.staleThresholdDays
        compactRows = settings.compactRows
        if (settings.onboardingCompleted) {
            onboardingState = OnboardingState(
                step = OnboardingStep.Complete,
                dependency = GhDependencyState.Available,
                identity = githubIdentity,
            )
        }
        if (onboarding == null) {
            onboarding = OnboardingCoordinator(
                preflight = ResolvingGitHubPreflightGateway(
                    resolveExecutable = {
                        GhClient.detectExecutable() ?: ghPathText.takeIf(String::isNotBlank)
                    },
                ),
                discovery = OnboardingDiscoveryGateway { GhClient.discoverScope() },
                progressStore = SettingsOnboardingProgressStore(settingsStore),
            )
        }
        replacePullRequests(pullRequests)
        applicationLoaded = true
    }

    fun startOnboarding() {
        val coordinator = onboarding ?: return
        scope.launch {
            withContext(Dispatchers.IO) { coordinator.start() }
            onboardingState = coordinator.state
            githubIdentity = coordinator.state.identity
        }
    }

    fun retryOnboarding() {
        val coordinator = onboarding ?: return
        scope.launch {
            withContext(Dispatchers.IO) { coordinator.retry() }
            onboardingState = coordinator.state
            githubIdentity = coordinator.state.identity
        }
    }

    fun confirmOnboardingIdentity() {
        val coordinator = onboarding ?: return
        scope.launch {
            withContext(Dispatchers.IO) { coordinator.confirmIdentity() }
            onboardingState = coordinator.state
            githubIdentity = coordinator.state.identity
        }
    }

    fun setOnboardingOrganizationScope(organization: String, selected: Boolean) {
        val coordinator = onboarding ?: return
        val selection = coordinator.state.selection.copy(
            organizationScopes = coordinator.state.selection.organizationScopes +
                    (organization to if (selected) OrganizationScope.All else OrganizationScope.Disabled),
        )
        coordinator.selectScope(selection)
        onboardingState = coordinator.state
    }

    fun toggleOnboardingRepository(repository: String) {
        val coordinator = onboarding ?: return
        val selected = coordinator.state.selection.individualRepositories
        coordinator.selectScope(
            coordinator.state.selection.copy(
                individualRepositories = if (repository in selected) selected - repository else selected + repository,
            ),
        )
        onboardingState = coordinator.state
    }

    fun reviewOnboardingScope() {
        val coordinator = onboarding ?: return
        coordinator.reviewScope()
        onboardingState = coordinator.state
    }

    fun backToOnboardingScope() {
        val coordinator = onboarding ?: return
        coordinator.backToScopeSelection()
        onboardingState = coordinator.state
    }

    fun completeOnboarding() {
        val coordinator = onboarding ?: return
        coordinator.complete()
        onboardingState = coordinator.state
        val settings = settingsStore.load()
        repositoriesText = settings.repositories.joinToString("\n")
        organizationsText = settings.organizations.joinToString("\n")
        githubIdentity = coordinator.state.identity
        view = View.NeedsReview
        if (repositoriesText.isNotBlank()) refresh()
    }

    fun restartOnboarding() {
        val coordinator = onboarding ?: return
        coordinator.restart()
        onboardingState = coordinator.state
    }

    val onboardingRequired: Boolean
        get() = onboardingState.step != OnboardingStep.Complete

    suspend fun checkForUpdatesNow() {
        updater.checkNow()
        updateState = updater.state
    }

    fun checkForUpdates() {
        if (updateState == UpdateState.Checking) return
        scope.launch {
            withContext(Dispatchers.IO) { checkForUpdatesNow() }
        }
    }

    fun startUpdateScheduler() {
        updateSchedulerJob?.cancel()
        if (!updater.preferences().automaticChecksEnabled) return
        updateSchedulerJob = scope.launch {
            withContext(Dispatchers.IO) { checkForUpdatesNow() }
            while (true) {
                val now = ZonedDateTime.now()
                val next = nextDailyUpdateCheck(now)
                delay(Duration.between(now, next).toMillis().coerceAtLeast(1_000L))
                withContext(Dispatchers.IO) { checkForUpdatesNow() }
            }
        }
    }

    fun dismissUpdate() {
        updater.dismissAvailableUpdate()
        updateState = updater.state
    }

    fun downloadAndInstallUpdate() {
        scope.launch {
            withContext(Dispatchers.IO) { updater.downloadAndInstallUpdate() }
            updateState = updater.state
        }
    }

    fun retryUpdateDownload() {
        scope.launch {
            withContext(Dispatchers.IO) { updater.retryDownload() }
            updateState = updater.state
        }
    }

    fun dismissUpdateFailure() {
        updater.dismissFailure()
        updateState = updater.state
    }

    fun cancelUpdateDownload() {
        updater.cancelDownload()
        updateState = updater.state
    }

    fun showUpdateReleaseNotes() {
        showAboutDialog = true
    }

    val installedVersion: AppVersion
        get() = updater.installedVersion

    val updatePreferences: UpdatePreferences
        get() = updater.preferences()

    fun saveConfig() {
        val ghPath = ghPathText.trim()
        Files.deleteIfExists(uiScaleFile)
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
        repositoriesText = parseLines(repositoriesText)
            .filterNot { it == repository }
            .joinToString("\n")
        pendingTrackedRepositories = pendingTrackedRepositories - repository
        saveConfig()
        statusLine = "Stopped tracking $repository"
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
            result.value
                ?.substringAfter("authenticated as ", "")
                ?.substringBefore(" ·")
                ?.trim()
                ?.takeIf(String::isNotBlank)
                ?.let { applyGitHubIdentity(GitHubIdentity(it)) }
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
            lastRefreshError = "No repositories are selected. Discover repositories and apply a selection first."
            statusLine = "No repositories selected"
            return
        }

        refreshRepositories(repos, showReminderAfterRefresh)
    }

    private fun refreshRepositories(
        selectedRepositories: List<String>,
        showReminderAfterRefresh: Boolean = false,
    ) {
        GhClient.configureExecutable(ghPathText)
        isRefreshing = true
        isDiscovering = false
        lastRefreshStartedAt = Instant.now()
        lastRefreshError = null
        lastUndoReview = null
        refreshTotal = selectedRepositories.size
        refreshDone = 0
        refreshPhase = "Checking GitHub CLI…"
        statusLine = refreshPhase

        scope.launch {
            val startedAt = System.currentTimeMillis()
            try {
                val refreshed = pullRequestIntake.refreshSelectedRepositories(
                    selectedRepositories = selectedRepositories,
                ) { progress ->
                    refreshTotal = progress.total
                    refreshDone = progress.completed
                    progress.repository?.let { repository ->
                        refreshPhase = "Fetching $repository…"
                        statusLine = "Refreshing ${progress.completed + 1} / ${progress.total} repositories…"
                    }
                }
                finishRefresh(
                    startedAt,
                    WorkerResult(value = refreshed),
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

            if (showReminderAfterRefresh || scheduledReminderPending) {
                deliverScheduledReminderIfAllowed(refreshFailed = true)
            }
        } else {
            val refreshed = result.value.orEmpty()
            GhClient.activeIdentity()?.let(::applyGitHubIdentity)
            val previousByKey = pullRequests.associateBy { it.key }

            newPullRequestKeys = refreshed
                .filter { it.key !in previousByKey }
                .map { it.key }
                .toSet()

            updatedPullRequestKeys = refreshed
                .filter { pr ->
                    previousByKey[pr.key]?.updatedMarker
                        ?.let { previousMarker -> previousMarker != pr.updatedMarker }
                        ?: false
                }
                .map { it.key }
                .toSet()

            val changedReviewKeys = refreshed
                .asSequence()
                .filter { it.source == PullRequestSource.ReviewRequest }
                .map { it.key }
                .filter { it in newPullRequestKeys || it in updatedPullRequestKeys }
                .toSet()
            val currentScopeForChanges = currentQueueScopeFilter()
            val hasChangedReviewHiddenByScope = changedReviewKeys.any { key ->
                val pr = refreshed.firstOrNull { it.key == key } ?: return@any false
                when (currentScopeForChanges) {
                    QueueScopeFilter.All -> false
                    is QueueScopeFilter.Organization -> pr.repository.owner != currentScopeForChanges.owner
                    is QueueScopeFilter.Repository -> pr.repository.toString() != currentScopeForChanges.nameWithOwner
                }
            }
            if (
                changedReviewKeys.isNotEmpty() &&
                (view != View.NeedsReview || hasChangedReviewHiddenByScope)
            ) {
                needsReviewHasUnseenChanges = true
            }

            val refreshedKeys = refreshed.map { it.key }.toSet()
            val removedCount = previousByKey.keys.count { it !in refreshedKeys }
            refreshDelta = RefreshDelta(
                newCount = newPullRequestKeys.size,
                updatedCount = updatedPullRequestKeys.size,
                removedCount = removedCount,
            ).takeIf { it.totalChanges > 0 }

            refreshSummaryJob?.cancel()
            if (refreshDelta != null) {
                refreshSummaryJob = scope.launch {
                    delay(6_000)
                    refreshDelta = null
                }
            }

            replacePullRequests(refreshed)
            saveCache()
            val reviews = reviewQueue().size
            statusLine = "$successPrefix · $reviews reviews waiting"
            if (showReminderAfterRefresh || scheduledReminderPending) {
                deliverScheduledReminderIfAllowed()
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
        lastRefreshError = null
        statusLine = "Discovering repositories…"

        scope.launch {
            val result = withContext(Dispatchers.IO) {
                val discovery = CompletableFuture.supplyAsync {
                    repositoryCatalog.discoverRepositories(orgs)
                }
                try {
                    WorkerResult(value = discovery.get(discoveryTimeoutMillis, TimeUnit.MILLISECONDS))
                } catch (_: TimeoutException) {
                    discovery.cancel(true)
                    WorkerResult(
                        error = "GitHub repository discovery timed out. Check your connection and try again.",
                    )
                } catch (error: ExecutionException) {
                    val cause = error.cause ?: error
                    WorkerResult(error = cause.message ?: cause.toString())
                } catch (error: Throwable) {
                    WorkerResult(error = error.message ?: error.toString())
                }
            }

            isDiscovering = false
            if (result.error != null) {
                statusLine = "Discover failed · ${result.error}"
                return@launch
            }

            val discovered = result.value.orEmpty().distinct().sorted()
            if (discovered.isEmpty()) {
                discoveredTrackingRepositories = emptyList()
                pendingTrackedRepositories = emptySet()
                statusLine = "Discover found no repositories for the configured organizations."
                return@launch
            }

            val currentlyTracked = parseLines(repositoriesText).toSet()
            repositoryScopeHealth = validateRepositoryScope(
                savedRepositories = currentlyTracked,
                discoveredRepositories = discovered.map { repository ->
                    DiscoveredRepository(
                        nameWithOwner = repository,
                        owner = repository.substringBefore('/'),
                    )
                },
            )
            discoveredTrackingRepositories = (discovered + currentlyTracked)
                .distinct()
                .sorted()
            pendingTrackedRepositories = currentlyTracked

            val newCount = discovered.count { it !in currentlyTracked }
            statusLine = buildString {
                append("Discovered ${discovered.size} repositories")
                append(" · ${currentlyTracked.size} currently tracked")
                if (newCount > 0) append(" · $newCount available to add")
            }
        }
    }

    fun togglePendingTrackedRepository(repository: String) {
        pendingTrackedRepositories =
            if (repository in pendingTrackedRepositories) {
                pendingTrackedRepositories - repository
            } else {
                pendingTrackedRepositories + repository
            }
    }

    fun selectAllDiscoveredTrackingRepositories() {
        pendingTrackedRepositories = discoveredTrackingRepositories.toSet()
    }

    fun clearPendingTrackingSelection() {
        pendingTrackedRepositories = emptySet()
    }

    fun cancelTrackingDiscovery() {
        discoveredTrackingRepositories = emptyList()
        pendingTrackedRepositories = emptySet()
        repositoryDiscoveryQuery = ""
        statusLine = "Repository selection cancelled"
    }

    fun applyTrackingRepositorySelection() {
        repositoriesText = pendingTrackedRepositories
            .sorted()
            .joinToString("\n")
        saveConfig()

        val trackedCount = pendingTrackedRepositories.size
        discoveredTrackingRepositories = emptyList()
        pendingTrackedRepositories = emptySet()
        repositoryDiscoveryQuery = ""
        statusLine = "Tracking $trackedCount ${if (trackedCount == 1) "repository" else "repositories"}"
    }

    fun discoverGitHubScope() {
        if (isDiscovering) return
        GhClient.configureExecutable(ghPathText)
        isDiscovering = true
        repositoryDiscoveryError = null
        statusLine = "Discovering organizations and repositories…"
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching { GhClient.discoverScope() }
            }
            isDiscovering = false
            result.onSuccess { discovered ->
                repositoryDiscovery = discovered
                repositoryScopeHealth = validateRepositoryScope(
                    savedRepositories = parseLines(repositoriesText).toSet(),
                    discoveredRepositories = discovered.repositories,
                )
                val existingOrganizations = parseLines(organizationsText)
                repositoryScopeSelection = RepositoryScopeSelection(
                    organizationScopes = (
                            repositoryScopeSelection.organizationScopes +
                                    existingOrganizations.associateWith { OrganizationScope.All }
                            ),
                    individualRepositories = (
                            repositoryScopeSelection.individualRepositories +
                                    parseLines(repositoriesText)
                            ),
                )
                statusLine = "Discovered ${discovered.organizations.size} organizations and ${discovered.repositories.count { !it.archived }} repositories"
            }.onFailure { error ->
                repositoryDiscoveryError = error.message ?: "Repository discovery failed"
                statusLine = "Discovery failed · ${repositoryDiscoveryError}"
            }
        }
    }

    fun setOrganizationScope(
        organization: String,
        scope: OrganizationScope,
    ) {
        repositoryScopeSelection = repositoryScopeSelection.copy(
            organizationScopes = repositoryScopeSelection.organizationScopes + (organization to scope),
        )
    }

    fun toggleDiscoveredRepository(repository: String) {
        val selected = repositoryScopeSelection.individualRepositories
        repositoryScopeSelection = repositoryScopeSelection.copy(
            individualRepositories = if (repository in selected) {
                selected - repository
            } else {
                selected + repository
            },
        )
    }

    fun applyRepositoryScopeSelection() {
        val discovered = repositoryDiscovery ?: return
        val active = repositoryScopeSelection
            .activeRepositories(discovered.repositories)
            .sorted()
        repositoriesText = active.joinToString("\n")
        organizationsText = repositoryScopeSelection.organizationScopes
            .filterValues { it == OrganizationScope.All }
            .keys
            .sorted()
            .joinToString("\n")
        saveConfig()
        statusLine = "Saved repository scope · ${active.size} repositories"
    }

    fun visiblePullRequests(): List<PullRequest> {
        return ReviewQueue.visible(
            pullRequests = pullRequests,
            view = view,
            searchQuery = searchQuery,
            scope = currentQueueScopeFilter(),
            handledReviewRecords = handledReviewRecords,
            pinnedPullRequestKeys = pinnedPrKeys,
            mutedRepositories = parseLines(mutedRepositoriesText).toSet(),
            sortMode = sortMode,
            staleThresholdDays = staleThresholdDaysText.toLongOrNull()?.coerceIn(1L, 30L) ?: 2L,
        )
    }

    fun setQueueScopeFilter(filter: QueueScopeFilter) {
        if (view == View.NeedsReview && filter == QueueScopeFilter.All) {
            needsReviewHasUnseenChanges = false
        }
        // Repository scope is global because the control lives above every queue in the sidebar.
        // Keep the map shape for compatibility with the command layer, but store one shared value.
        queueScopeFilters = View.entries.associateWith { filter }
        expandedPullRequestKey = null
        selectedPullRequest = selectedPullRequest
            ?.takeIf { selected ->
                visiblePullRequests().any {
                    it.key == selected.key && it.source == selected.source
                }
            }
            ?: visiblePullRequests().firstOrNull()
    }

    fun currentQueueScopeFilter(): QueueScopeFilter =
        queueScopeFilters[View.NeedsReview]
            ?: queueScopeFilters.values.firstOrNull()
            ?: QueueScopeFilter.All

    fun clearQueueScopeFilter() {
        setQueueScopeFilter(QueueScopeFilter.All)
        statusLine = "Repository scope cleared"
    }

    fun activePullRequests(): List<PullRequest> =
        ReviewQueue.activePullRequests(pullRequests, parseLines(mutedRepositoriesText).toSet())

    fun replacePullRequests(items: List<PullRequest>) {
        val selected = selectedPullRequest
        val previousIndex = selected?.let { previous ->
            visiblePullRequests().indexOfFirst {
                it.key == previous.key && it.source == previous.source
            }
        } ?: -1
        pullRequests = dedupePullRequests(items)
        val visible = visiblePullRequests()
        selectedPullRequest = selected?.let { previous ->
            visible.firstOrNull { it.key == previous.key && it.source == previous.source }
        } ?: visible.getOrNull(previousIndex.coerceAtMost(visible.lastIndex))
                ?: visible.firstOrNull()
        if (view != View.Settings) {
            keyboardFocusRegion = FocusRegion.PullRequestList
        }
        if (expandedPullRequestKey !in visible.map { it.key }.toSet()) {
            expandedPullRequestKey = null
        }
    }

    fun pinnedPullRequests(): List<PullRequest> = activePullRequests()
        .filter { pinnedPrKeys.contains(it.key) }
        .let { sortPullRequests(View.Pinned, it) }

    fun handledPullRequests(): List<PullRequest> = ReviewQueue.visible(
        pullRequests = pullRequests,
        view = View.Handled,
        searchQuery = "",
        scope = QueueScopeFilter.All,
        handledReviewRecords = handledReviewRecords,
        pinnedPullRequestKeys = pinnedPrKeys,
        mutedRepositories = parseLines(mutedRepositoriesText).toSet(),
        sortMode = sortMode,
        staleThresholdDays = staleThresholdDaysText.toLongOrNull()?.coerceIn(1L, 30L) ?: 2L,
    )
        .let { sortPullRequests(View.Handled, it) }

    fun reviewQueue(): List<PullRequest> = ReviewQueue.reviewQueue(
        pullRequests = pullRequests,
        handledReviewRecords = handledReviewRecords,
        mutedRepositories = parseLines(mutedRepositoriesText).toSet(),
    )
        .let { sortPullRequests(View.NeedsReview, it) }

    fun todayPullRequests(): List<PullRequest> = ReviewQueue.visible(
        pullRequests = pullRequests,
        view = View.Today,
        searchQuery = "",
        scope = QueueScopeFilter.All,
        handledReviewRecords = handledReviewRecords,
        pinnedPullRequestKeys = pinnedPrKeys,
        mutedRepositories = parseLines(mutedRepositoriesText).toSet(),
        sortMode = sortMode,
        staleThresholdDays = staleThresholdDaysText.toLongOrNull()?.coerceIn(1L, 30L) ?: 2L,
    )

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

        expandedPullRequestKey = null
        statusLine = if (selectedPullRequest == null) {
            "Review queue clear"
        } else {
            "Skipped for now · ${selectedPullRequest!!.repository} #${selectedPullRequest!!.number}"
        }
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

        statusLine = "Marked reviewed · ${pr.repository} #${pr.number}"
        publishActionFeedback("Marked #${pr.number} as handled")

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
        selectedPullRequest?.let {
            openUrl(it.url)
            publishActionFeedback("Opened #${it.number} in browser")
        }
    }

    fun copySelectedUrl() {
        val pr = selectedPullRequest ?: return
        copyToClipboard(pr.url)
        statusLine = "Copied ${pr.repository} #${pr.number} URL"
        publishActionFeedback("PR URL copied")
    }

    fun openSelectedRepository() {
        val pr = selectedPullRequest ?: return
        openUrl("https://github.com/${pr.repository}")
    }

    fun clearFilter() {
        searchQuery = ""
        setQueueScopeFilter(QueueScopeFilter.All)
        statusLine = "Filter cleared"
    }

    fun toggleGroupByRepository() {
        if (view == View.Today) {
            statusLine = "Today uses fixed sections"
            return
        }

        groupByRepository = !groupByRepository
        saveConfig()
        statusLine = if (groupByRepository) "Grouped by repository" else "Grouping off"
    }

    fun toggleCompactRows() {
        compactRows = !compactRows
        saveConfig()
        statusLine = if (compactRows) "Compact rows on" else "Comfortable rows on"
    }

    fun cycleSortMode() {
        val currentIndex = WorkspaceSortModes.indexOf(sortMode).takeIf { it >= 0 } ?: 0
        sortMode = WorkspaceSortModes[(currentIndex + 1) % WorkspaceSortModes.size]
        saveConfig()
        statusLine = "Sort: $sortMode"
    }

    fun isHandledCurrent(pr: PullRequest): Boolean = handledReviewRecords[pr.key] == pr.updatedMarker

    fun selectView(next: View) {
        selectedPullRequest?.let { selected ->
            queueSelectionKeys = queueSelectionKeys + (view to selected.key)
        }
        view = next
        if (
            next == View.NeedsReview &&
            currentQueueScopeFilter() == QueueScopeFilter.All
        ) {
            needsReviewHasUnseenChanges = false
        }
        val rememberedKey = queueSelectionKeys[next]
        selectedPullRequest = if (next == View.Settings) {
            null
        } else {
            QueueContext.restoreSelection(visiblePullRequests(), rememberedKey)
        }
        expandedPullRequestKey = null
        keyboardMode = KeyboardMode.Normal
        if (next in SidebarKeyboardViews) {
            sidebarKeyboardView = next
        }
        if (next != View.Settings) {
            keyboardFocusRegion = FocusRegion.PullRequestList
        }
    }

    fun openTrackingSettings() {
        settingsSectionIndex = 2
        settingsFocusedRowIndex = 0
        selectView(View.Settings)
    }

    fun showSidebarNavigationHintOnce() {
        if (sidebarNavigationHintShownThisSession) return
        sidebarNavigationHintShownThisSession = true
        sidebarNavigationHintVisible = true
        scope.launch {
            delay(2_800)
            sidebarNavigationHintVisible = false
        }
    }

    fun queueViewportState(view: View): QueueViewportState? = queueViewportStates[view]

    fun rememberQueueViewport(
        view: View,
        firstVisibleItemIndex: Int,
        firstVisibleItemScrollOffset: Int,
    ) {
        queueViewportStates = queueViewportStates + (
                view to QueueViewportState(
                    firstVisibleItemIndex = firstVisibleItemIndex.coerceAtLeast(0),
                    firstVisibleItemScrollOffset = firstVisibleItemScrollOffset.coerceAtLeast(0),
                )
                )
    }

    fun isPinned(pr: PullRequest): Boolean = pinnedPrKeys.contains(pr.key)

    fun togglePin(pr: PullRequest? = selectedPullRequest) {
        pr ?: return
        pinnedPrKeys = if (pinnedPrKeys.contains(pr.key)) pinnedPrKeys - pr.key else pinnedPrKeys + pr.key
        savePinned()
        val action = if (pinnedPrKeys.contains(pr.key)) "Pinned" else "Unpinned"
        statusLine = "$action ${pr.repository} #${pr.number}"
        actionFeedbackSequence += 1
        actionFeedback = ActionFeedback("$action #${pr.number}", actionFeedbackSequence)
    }

    fun togglePullRequestDetails(pr: PullRequest) {
        selectedPullRequest = pr
        newPullRequestKeys = newPullRequestKeys - pr.key
        updatedPullRequestKeys = updatedPullRequestKeys - pr.key
        expandedPullRequestKey = if (expandedPullRequestKey == pr.key) null else pr.key
    }

    fun toggleSelectedPullRequestDetails() {
        val pr = selectedPullRequest ?: return
        togglePullRequestDetails(pr)
    }

    fun isPullRequestReadyToMerge(pr: PullRequest?): Boolean {
        return pr?.let { PullRequestAttention.describe(it).canMerge } ?: false
    }

    fun canMergePullRequest(pr: PullRequest?): Boolean =
        isPullRequestReadyToMerge(pr) && !isMergingPullRequest

    fun performSelectedMAction() {
        val pr = selectedPullRequest ?: return
        when {
            pr.source == PullRequestSource.ReviewRequest -> markReviewed(pr)
            canMergePullRequest(pr) -> mergePullRequest(pr)
            else -> publishActionFeedback("Selected PR is not ready to merge")
        }
    }

    fun mergePullRequest(pr: PullRequest? = selectedPullRequest) {
        pr ?: return
        if (!canMergePullRequest(pr)) {
            publishActionFeedback("PR #${pr.number} is not ready to merge")
            return
        }

        GhClient.configureExecutable(ghPathText)
        isMergingPullRequest = true
        mergingPullRequestKey = pr.key
        statusLine = "Merging ${pr.repository} #${pr.number}…"

        scope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching { GhClient.mergePullRequest(pr) }
                    .fold(
                        onSuccess = { WorkerResult(value = it) },
                        onFailure = { WorkerResult(error = it.message ?: it.toString()) },
                    )
            }

            isMergingPullRequest = false
            mergingPullRequestKey = null

            if (result.error != null) {
                statusLine = "Merge failed · ${result.error}"
                publishActionFeedback("Merge failed for #${pr.number}")
                return@launch
            }

            publishActionFeedback("Merged #${pr.number}")
            statusLine = result.value ?: "Merged ${pr.repository} #${pr.number}"
            refresh()
        }
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
        publishActionFeedback("Markdown copied")
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
        Files.deleteIfExists(cacheFilePath())
        Files.deleteIfExists(legacyCacheFile)
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
            appendLine(displayDiagnostics)
            appendLine("last error: ${lastRefreshError ?: "none"}")
        }
        diagnosticsText = text
        copyToClipboard(text)
        statusLine = "Copied diagnostics"
    }

    fun recordCommandExecution(commandId: CommandId) {
        recentCommandIds = (listOf(commandId) + recentCommandIds.filterNot { it == commandId }).take(6)
    }

    fun publishActionFeedback(message: String) {
        actionFeedbackSequence += 1
        actionFeedback = ActionFeedback(message, actionFeedbackSequence)
    }

    fun recordPaletteTarget(target: String) {
        recentPaletteTargets = (listOf(target) + recentPaletteTargets.filterNot { it == target }).take(6)
    }

    private fun saveCache() {
        Files.createDirectories(configDir)
        cacheFilePath().writeLines(pullRequests.map(::serializePullRequest))
    }

    private fun loadCache(): List<PullRequest> {
        val scoped = cacheFilePath()
        val source = if (scoped.exists()) scoped else legacyCacheFile
        return source.safeLines().mapNotNull(::deserializePullRequest)
    }

    private fun cacheFilePath(): Path {
        val identity = githubIdentity ?: return legacyCacheFile
        val safeHost = identity.host.replace(Regex("[^A-Za-z0-9._-]"), "_")
        val safeLogin = identity.login.replace(Regex("[^A-Za-z0-9._-]"), "_")
        return configDir.resolve("pull-requests-cache-$safeHost-$safeLogin.tsv")
    }

    private fun applyGitHubIdentity(identity: GitHubIdentity) {
        val previous = githubIdentity
        if (previous != null && previous != identity) {
            pullRequests = emptyList()
            selectedPullRequest = null
            expandedPullRequestKey = null
            statusLine = "GitHub account changed · review repository scope"
        }
        githubIdentity = identity
        settingsStore.save(
            settingsStore.load().copy(
                githubIdentityLogin = identity.login,
                githubIdentityHost = identity.host,
            ),
        )
    }

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
                view == View.Pinned && pinnedPrKeys.contains(pr.key) -> 0
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
            else -> prs.sortedWith(
                compareBy<PullRequest> { urgencyOrder(it) }
                    .thenBy { instantOrNull(it.updatedAt) ?: Instant.EPOCH }
                    .thenBy { it.repository.toString() }
                    .thenBy { it.number },
            )
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
        selectView(View.NeedsReview)
        selectedPullRequest = reviewQueue().firstOrNull()
        statusLine = if (selectedPullRequest == null) {
            "Review reminder handled · review queue clear"
        } else {
            "Review reminder handled · review queue opened"
        }
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
        settingsStore.save(currentSettings())
        reminderSnoozedUntil?.let { reminderSnoozedUntilFile.writeLines(listOf(it.toString())) } ?: Files.deleteIfExists(reminderSnoozedUntilFile)
        reminderDismissedDate?.let { reminderDismissedDateFile.writeLines(listOf(it)) } ?: Files.deleteIfExists(reminderDismissedDateFile)
    }

    private fun currentSettings(): RevqSettings = settingsStore.load().copy(
        repositories = parseLines(repositoriesText),
        organizations = parseLines(organizationsText),
        githubExecutable = ghPathText.trim(),
        githubDetectionSource = ghDetectionSource,
        mutedRepositories = parseLines(mutedRepositoriesText),
        autoRefreshEnabled = autoRefreshEnabled,
        autoRefreshIntervalMinutes = autoRefreshIntervalMinutesText.trim().ifBlank { "5" },
        sortMode = sortMode.ifBlank { "Urgency" },
        groupByRepository = groupByRepository,
        staleThresholdDays = staleThresholdDaysText.trim().ifBlank { "2" },
        compactRows = compactRows,
        reminderEnabled = reminderEnabled,
        reminderTime = reminderTimeText.trim().ifBlank { "09:00" },
        reminderDays = reminderDaysText.trim().ifBlank { "Mon-Fri" },
        quietHours = quietHoursText.trim().ifBlank { "18:00-08:00" },
        remindOnlyWhenQueueNotClear = remindOnlyWhenQueueNotClear,
        reminderSnoozeMinutes = reminderSnoozeMinutesText.trim().ifBlank { "60" },
    )

    fun startReminderScheduler() {
        reminderSchedulerJob?.cancel()
        reminderSchedulerJob = scope.launch {
            // Recompute frequently enough that settings/snooze changes feel responsive, but never show
            // the window on app startup just because today's configured time is already in the past.
            while (true) {
                if (!reminderEnabled) {
                    scheduledReminderPending = false
                    nextReminderAt = null
                    reminderStatus = "Reminders disabled"
                    delay(30_000)
                    continue
                }

                if (scheduledReminderPending) {
                    if (showReminderWindow) {
                        scheduledReminderPending = false
                    } else if (isRefreshing) {
                        reminderStatus = "Reminder due · waiting for refresh"
                        statusLine = "Reminder due · waiting for refresh"
                        delay(1_000)
                        continue
                    } else {
                        runScheduledReminderCheck()
                        delay(1_000)
                        continue
                    }
                }

                val scheduledAt = nextReminderInstant(Instant.now())
                nextReminderAt = scheduledAt
                reminderStatus = "Next reminder ${formatReminderInstant(scheduledAt)}"

                val now = Instant.now()
                val waitMs = Duration.between(now, scheduledAt).toMillis().coerceIn(1_000L, 60_000L)
                delay(waitMs)

                val dueAt = nextReminderAt
                if (dueAt != null && !Instant.now().isBefore(dueAt) && !showReminderWindow) {
                    scheduledReminderPending = true
                    if (isRefreshing) {
                        reminderStatus = "Reminder due · waiting for refresh"
                        statusLine = "Reminder due · waiting for refresh"
                    } else {
                        runScheduledReminderCheck()
                    }
                }
            }
        }
    }

    private fun runScheduledReminderCheck() {
        if (!reminderEnabled) {
            scheduledReminderPending = false
            reminderStatus = "Reminders disabled"
            return
        }

        if (isTodayDismissed()) {
            scheduledReminderPending = false
            reminderStatus = "Reminder suppressed · dismissed today"
            statusLine = "Reminder suppressed · dismissed today"
            return
        }

        val snooze = reminderSnoozedUntil
        if (snooze != null && !Instant.now().isBefore(snooze)) {
            reminderSnoozedUntil = null
            saveReminderState()
        }

        val repos = parseLines(repositoriesText)
        if (repos.isEmpty()) {
            scheduledReminderPending = false
            reminderStatus = "Reminder skipped · no repositories tracked"
            statusLine = "Reminder skipped · no repositories tracked"
            return
        }

        if (isRefreshing) {
            reminderStatus = "Reminder due · waiting for refresh"
            statusLine = "Reminder due · waiting for refresh"
            return
        }

        reminderStatus = "Reminder due · refreshing review queue"
        statusLine = "Reminder time reached · refreshing review queue…"
        refresh(showReminderAfterRefresh = true)
    }

    private fun reminderSuppressionReason(): String? {
        return when (val decision = ReviewReminder.dueDecision(
            ReviewReminderInput(
                enabled = reminderEnabled,
                now = Instant.now(),
                localDate = LocalDate.now(),
                localTime = LocalTime.now(),
                dismissedDate = reminderDismissedDate,
                snoozedUntil = reminderSnoozedUntil,
                quietHours = quietHoursText,
                onlyWhenQueueNotClear = remindOnlyWhenQueueNotClear,
                reviewQueueSize = reviewQueue().size,
            ),
        )) {
            ReviewReminderDecision.Show -> null
            is ReviewReminderDecision.Suppress -> decision.reason
        }
    }

    private fun deliverScheduledReminderIfAllowed(refreshFailed: Boolean = false) {
        scheduledReminderPending = false

        val suppressionReason = reminderSuppressionReason()
        if (suppressionReason != null) {
            reminderStatus = "Reminder suppressed · $suppressionReason"
            statusLine = "Reminder suppressed · $suppressionReason"
            return
        }

        showScheduledReminderWindow()
        val reviews = reviewQueue().size
        reminderStatus = if (refreshFailed) {
            "Reminder shown · refresh failed, using cached queue"
        } else {
            "Reminder shown · $reviews reviews waiting"
        }
        statusLine = reminderStatus
    }

    private fun isTodayDismissed(): Boolean = reminderDismissedDate == LocalDate.now().toString()

    private fun isInReminderQuietHours(now: LocalTime = LocalTime.now()): Boolean {
        return ReviewReminder.isInQuietHours(quietHoursText, now)
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

object GhClient : PullRequestIntakeGateway, RepositoryCatalogGateway {
    @Volatile
    private var configuredExecutable: String? = null
    @Volatile
    private var activeLogin: String? = null

    fun activeIdentity(): GitHubIdentity? = activeLogin?.let(::GitHubIdentity)

    fun configureExecutable(path: String) {
        configuredExecutable = path.trim().ifBlank { null }
    }

    fun detectExecutable(): String? = detectExecutableResult()?.executable

    fun detectExecutableResult(): GhDetectionResult? = detectGhExecutableResult()

    override fun discoverRepositories(organizations: List<String>): List<String> {
        ensureAuthenticated()
        return GitHubRepositoryDiscovery { command ->
            runGh(*command.toTypedArray())
        }.discoverRepositories(organizations)
    }

    fun discoverScope(): RepositoryDiscoveryResult {
        ensureAuthenticated()
        val login = currentLogin()
        val organizations = runGh(
            "api", "user/orgs", "--paginate", "--jq", ".[].login",
        )
            .lineSequence()
            .map(String::trim)
            .filter(String::isNotBlank)
            .distinct()
            .sorted()
            .map(::DiscoveredOrganization)
            .toList()

        val owners = listOf(login) + organizations.map { it.login }
        val repositories = owners.flatMap { owner ->
            runGh(
                "repo", "list", owner,
                "--limit", "1000",
                "--json", "nameWithOwner,isArchived,isPrivate",
                "--template",
                "{{range .}}{{.nameWithOwner}}{{\"\\t\"}}{{.isArchived}}{{\"\\t\"}}{{.isPrivate}}{{\"\\n\"}}{{end}}",
            )
                .lineSequence()
                .mapNotNull { line ->
                    val parts = line.split("\t")
                    val nameWithOwner = parts.getOrNull(0)?.trim().orEmpty()
                    if ("/" !in nameWithOwner) return@mapNotNull null
                    DiscoveredRepository(
                        nameWithOwner = nameWithOwner,
                        owner = owner,
                        archived = parts.getOrNull(1)?.toBooleanStrictOrNull() ?: false,
                        private = parts.getOrNull(2)?.toBooleanStrictOrNull() ?: false,
                    )
                }
        }
            .distinctBy { it.nameWithOwner }
            .sortedBy { it.nameWithOwner }

        return RepositoryDiscoveryResult(
            login = login,
            organizations = organizations,
            repositories = repositories,
        )
    }

    fun refresh(repositories: List<String>): List<PullRequest> {
        val login = prepareRefresh()
        return dedupePullRequests(repositories.flatMap { repo -> refreshRepository(repo, login) })
    }

    override fun prepareRefresh(): String {
        ensureAuthenticated()
        return currentLogin()
    }

    override fun refreshRepository(repo: String, login: String): List<PullRequest> {
        val reviewRequests = listPrs(
            repo = repo,
            source = PullRequestSource.ReviewRequest,
            search = "is:pr is:open review-requested:$login",
            author = null,
            viewerLogin = login,
        ).filterNot { it.authorLogin?.equals(login, ignoreCase = true) == true }
        val assignments = listPrs(
            repo = repo,
            source = PullRequestSource.ReviewRequest,
            search = "is:pr is:open assignee:$login",
            author = null,
            viewerLogin = login,
        ).filterNot { it.authorLogin?.equals(login, ignoreCase = true) == true }

        val mine = listPrs(
            repo = repo,
            source = PullRequestSource.Mine,
            search = null,
            author = login,
            viewerLogin = login,
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

        return dedupePullRequests(reviewRequests + assignments + enrichedMine)
    }

    fun mergePullRequest(pr: PullRequest): String {
        ensureAuthenticated()
        val endpoint = "repos/${pr.repository.owner}/${pr.repository.name}/pulls/${pr.number}/merge"

        return runGh(
            "api",
            endpoint,
            "-X", "PUT",
            "--jq", ".message // if .merged then \"Pull Request successfully merged\" else \"Merge request did not complete\" end",
        ).ifBlank {
            "Merged ${pr.repository} #${pr.number}"
        }
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

    private fun currentLogin(): String {
        val login = runGh("api", "user", "--jq", ".login")
            .trim()
            .ifBlank { error("GitHub CLI did not return the current user login. Run `gh auth status` in a terminal.") }
        activeLogin = login
        return login
    }

    private fun listPrs(
        repo: String,
        source: PullRequestSource,
        search: String?,
        author: String?,
        viewerLogin: String,
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
            (.assignees // []) as ${'$'}assignees |
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
            ([${'$'}assignees[]? | reviewerLabel] | unique | join(",")) as ${'$'}assignees |
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
                ${'$'}approvingReviewers,
                ${'$'}assignees
            ] | @tsv
        """.trimIndent().replace("\n", " ")

        args += listOf(
            "--json",
            "number,title,url,updatedAt,comments,author,isDraft,reviewDecision,mergeable,mergeStateStatus,reviewRequests,latestReviews,statusCheckRollup,assignees",
            "--jq",
            jqExpression,
        )

        val output = runGh(*args.toTypedArray())
        return output.lines().mapNotNull { line ->
            val parts = line.split("\t", limit = 18)
            if (parts.size < 6) return@mapNotNull null

            val requestedReviewers = deserializeIdentityList(parts.getOrNull(14))
            val assignees = deserializeIdentityList(parts.getOrNull(17))
            PullRequest(
                repository = repoId,
                number = parts[0].toIntOrNull() ?: return@mapNotNull null,
                title = parts[1].unescapeTsv(),
                url = parts[2].unescapeTsv(),
                updatedAt = parts[3].unescapeTsv().ifBlank { null },
                comments = parts.getOrNull(4)?.toIntOrNull() ?: 0,
                source = source,
                reviewRequestKind = if (source == PullRequestSource.ReviewRequest) {
                    classifyReviewRequest(viewerLogin, requestedReviewers, assignees)
                } else {
                    null
                },
                authorLogin = parts.getOrNull(5)?.unescapeTsv()?.ifBlank { null },
                isDraft = parts.getOrNull(6)?.toBooleanStrictOrNull() ?: false,
                reviewDecision = parts.getOrNull(7)?.unescapeTsv()?.ifBlank { null },
                mergeable = parts.getOrNull(8)?.unescapeTsv()?.ifBlank { null },
                mergeStateStatus = parts.getOrNull(9)?.unescapeTsv()?.ifBlank { null },
                reviewRequestsCount = parts.getOrNull(10)?.toIntOrNull() ?: 0,
                checksTotal = parts.getOrNull(11)?.toIntOrNull() ?: 0,
                checksFailing = parts.getOrNull(12)?.toIntOrNull() ?: 0,
                checksPending = parts.getOrNull(13)?.toIntOrNull() ?: 0,
                requestedReviewers = requestedReviewers,
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
private fun RevqLaunchScreen() {
    Surface(Modifier.fillMaxSize(), color = AppBg) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            AppBrandMark(contentDescription = "RevQ", modifier = Modifier.size(72.dp))
        }
    }
}

@Composable
fun OnboardingScreen(state: AppState) {
    val onboarding = state.onboardingState
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .semantics { contentDescription = "RevQ first-run setup: ${onboarding.step.name}" },
        color = AppBg,
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Surface(
                modifier = Modifier.widthIn(max = 760.dp).fillMaxWidth().padding(24.dp),
                color = PanelBg,
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, Border),
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        AppBrandMark(contentDescription = null, modifier = Modifier.size(48.dp))
                        Column {
                            Text("Welcome to RevQ", color = TextPrimary, style = MaterialTheme.typography.titleLarge)
                            Text("Connect GitHub and choose what RevQ should monitor.", color = TextMuted)
                        }
                    }
                    Divider(color = Border)

                    when (onboarding.step) {
                        OnboardingStep.CheckingGitHubCli,
                        OnboardingStep.CheckingAuthentication -> OnboardingChecking(onboarding.step)
                        OnboardingStep.GitHubCliRequired -> {
                            OnboardingMessage(
                                title = "GitHub CLI is required",
                                body = "RevQ uses GitHub CLI to discover repositories and retrieve pull request information. It was not found on this system.",
                            )
                            OnboardingActions {
                                TextButton(onClick = { openUrl("https://cli.github.com/") }) {
                                    Text("Installation instructions", color = TextMuted)
                                }
                                Button(onClick = state::retryOnboarding) { Text("Check again") }
                            }
                        }
                        OnboardingStep.AuthenticationRequired -> {
                            OnboardingMessage(
                                title = "GitHub authentication needs attention",
                                body = onboarding.message
                                    ?: "Authenticate with GitHub CLI, then return here and check again.",
                            )
                            OnboardingActions {
                                TextButton(onClick = { openUrl("https://cli.github.com/manual/gh_auth_login") }) {
                                    Text("Authentication instructions", color = TextMuted)
                                }
                                Button(onClick = state::retryOnboarding) { Text("Check again") }
                            }
                        }
                        OnboardingStep.ConfirmIdentity -> {
                            val identity = (onboarding.authentication as? GhAuthState.Authenticated)?.identity
                                ?: onboarding.identity
                            OnboardingMessage(
                                title = "Connected as",
                                body = identity?.let { "${it.login}\n${it.host}" }
                                    ?: "GitHub CLI is authenticated.",
                            )
                            OnboardingActions {
                                Button(onClick = state::confirmOnboardingIdentity) { Text("Continue") }
                            }
                        }
                        OnboardingStep.SelectScope -> OnboardingScopeSelection(state, onboarding)
                        OnboardingStep.ReviewScope -> OnboardingScopeSummary(state, onboarding)
                        OnboardingStep.Error -> {
                            OnboardingMessage(
                                title = "Setup needs attention",
                                body = onboarding.message ?: "RevQ could not complete this setup step.",
                            )
                            OnboardingActions {
                                Button(onClick = state::retryOnboarding) { Text("Try again") }
                            }
                        }
                        OnboardingStep.Starting -> OnboardingChecking(onboarding.step)
                        OnboardingStep.Complete -> Unit
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingChecking(step: OnboardingStep) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        CircularProgressIndicator(Modifier.size(22.dp), color = Olive, strokeWidth = 2.dp)
        Text(
            if (step == OnboardingStep.CheckingAuthentication) "Checking GitHub authentication…" else "Looking for GitHub CLI…",
            color = TextPrimary,
        )
    }
}

@Composable
private fun OnboardingMessage(title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, color = TextPrimary, style = MaterialTheme.typography.titleMedium)
        Text(body, color = TextMuted, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun OnboardingActions(content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}

@Composable
private fun OnboardingScopeSelection(state: AppState, onboarding: OnboardingState) {
    val discovery = onboarding.discovery
    if (discovery == null) {
        OnboardingMessage("No repository catalog", "Try repository discovery again.")
        return
    }
    Text("Organizations & repositories", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
    Text(
        "Choose at least one scope. Archived repositories remain visible but cannot be selected.",
        color = TextMuted,
        style = MaterialTheme.typography.bodySmall,
    )
    LazyColumn(
        modifier = Modifier.fillMaxWidth().height(390.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        items(discovery.organizations, key = { "org:${it.login}" }) { organization ->
            val selected = onboarding.selection.organizationScopes[organization.login] == OrganizationScope.All
            OnboardingChoiceRow(
                title = organization.login,
                detail = if (selected) "All repositories" else "Organization",
                selected = selected,
                enabled = true,
                onClick = { state.setOnboardingOrganizationScope(organization.login, !selected) },
            )
        }
        items(discovery.repositories, key = { "repo:${it.nameWithOwner}" }) { repository ->
            val selected = repository.nameWithOwner in onboarding.selection.individualRepositories ||
                    onboarding.selection.organizationScopes[repository.owner] == OrganizationScope.All
            OnboardingChoiceRow(
                title = repository.nameWithOwner,
                detail = when {
                    repository.archived -> "Archived"
                    repository.private -> "Private repository"
                    else -> "Repository"
                },
                selected = selected,
                enabled = !repository.archived &&
                        onboarding.selection.organizationScopes[repository.owner] != OrganizationScope.All,
                onClick = { state.toggleOnboardingRepository(repository.nameWithOwner) },
            )
        }
    }
    onboarding.message?.let { Text(it, color = Amber, style = MaterialTheme.typography.bodySmall) }
    OnboardingActions {
        Button(onClick = state::reviewOnboardingScope) { Text("Continue") }
    }
}

@Composable
private fun OnboardingChoiceRow(
    title: String,
    detail: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) Olive.copy(alpha = 0.10f) else PanelElevated)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = if (selected) Icons.Rounded.CheckCircle else Icons.Rounded.Search,
            contentDescription = if (selected) "Selected" else "Not selected",
            tint = when {
                !enabled && !selected -> TextMuted.copy(alpha = 0.45f)
                selected -> Olive
                else -> TextMuted
            },
            modifier = Modifier.size(19.dp),
        )
        Column(Modifier.weight(1f)) {
            Text(title, color = if (enabled || selected) TextPrimary else TextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(detail, color = TextMuted, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun OnboardingScopeSummary(state: AppState, onboarding: OnboardingState) {
    val summary = onboarding.summary ?: return
    OnboardingMessage(
        title = "Ready to start",
        body = "${summary.organizationCount} organizations selected\n${summary.activeRepositoryCount} active repositories",
    )
    Surface(color = PanelElevated, shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Needs Review", color = TextPrimary, fontWeight = FontWeight.SemiBold)
            Text("PRs requesting or requiring your review", color = TextMuted, style = MaterialTheme.typography.bodySmall)
            Text("My PRs", color = TextPrimary, fontWeight = FontWeight.SemiBold)
            Text("Open PRs authored by you within the selected scope", color = TextMuted, style = MaterialTheme.typography.bodySmall)
        }
    }
    OnboardingActions {
        TextButton(onClick = state::backToOnboardingScope) { Text("Back", color = TextMuted) }
        Spacer(Modifier.width(8.dp))
        Button(onClick = state::completeOnboarding) { Text("Start RevQ") }
    }
}


@Composable
fun RevqApp(state: AppState) {
    val keyboardRouter = remember { KeyboardRouter() }
    val paletteState = remember { CommandPaletteState() }
    val focusManager = LocalFocusManager.current
    val density = LocalDensity.current
    val rootFocusRequester = remember { FocusRequester() }

    LaunchedEffect(density.density, density.fontScale) {
        state.displayDiagnostics = buildDisplayDiagnostics(
            composeDensity = density.density,
            fontScale = density.fontScale,
        )
    }

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
            KeyboardAction.PageDown -> moveByHalfPage(state, 1)
            KeyboardAction.PageUp -> moveByHalfPage(state, -1)
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
            KeyboardAction.CloseCommandPalette -> {
                paletteState.close()
                rootFocusRequester.requestFocus()
            }

            is KeyboardAction.ExecuteCommand -> {
                CommandRegistry.execute(action.commandId, state)
            }
        }
        return true
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .focusRequester(rootFocusRequester)
            .focusable()
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
                        SidebarPanel(
                            state = state,
                            onOpenRepositoryScope = {
                                paletteState.open(PaletteMode.RepositoryScope)
                            },
                        )
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
                    }
                }
                UpdateBanner(state)
                BottomStatusBar(state, paletteState)
            }

            if (paletteState.isOpen) {
                CommandPalette(
                    state = state,
                    paletteState = paletteState,
                    onGoToTop = { moveToRegionBoundary(state, first = true) },
                )
            }

            AnimatedVisibility(
                visible = state.sidebarNavigationHintVisible,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 300.dp, bottom = 58.dp),
            ) {
                Surface(
                    color = PanelElevated,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Border),
                ) {
                    Text(
                        text = "Sidebar · j/k move · Enter open · l return",
                        color = TextPrimary,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    )
                }
            }

            if (state.showAboutDialog) {
                AboutRevqDialog(
                    state = state,
                    onDismiss = { state.showAboutDialog = false },
                )
            }
        }
    }

    LaunchedEffect(state.mainWindowFocusRequest) {
        delay(50)
        rootFocusRequester.requestFocus()
    }

    LaunchedEffect(state.searchQuery, state.view) {
        val selected = state.selectedPullRequest
        if (selected != null && state.visiblePullRequests().none { it.key == selected.key && it.source == selected.source }) {
            state.selectedPullRequest = null
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

        // Refresh is an intentional visual reset of the queue. Keep the skeleton
        // visible for every refresh so manual and scheduled refreshes feel consistent.
        if (state.isRefreshing) {
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
            .height(76.dp)
            .background(PanelBg)
            .padding(horizontal = 22.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = state.view.label,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge,
            )

            Text(
                text = mainToolbarSubtitle(state),
                color = TextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        RefreshAction(state)
    }
}

@Composable
private fun RefreshAction(state: AppState) {
    DiscoverableIconAction(
        label = "Refresh GitHub data",
        icon = Icons.Rounded.Refresh,
        enabled = !state.isRefreshing,
        state = state,
        onClick = { state.refresh() },
        loading = state.isRefreshing,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DiscoverableIconAction(
    label: String,
    icon: ImageVector,
    state: AppState,
    enabled: Boolean = true,
    loading: Boolean = false,
    onClick: () -> Unit,
) {
    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(),
        state = androidx.compose.material3.rememberTooltipState(),
        tooltip = {
            PlainTooltip(
                containerColor = PanelElevated,
                contentColor = TextPrimary,
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        },
    ) {
        Surface(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(10.dp))
                .semantics { contentDescription = label }
                .onFocusChanged { focusState ->
                    if (focusState.isFocused) state.statusLine = "Enter $label"
                }
                .clickable(enabled = enabled) {
                    onClick()
                },
            color = PanelElevated,
            border = BorderStroke(1.dp, Border),
            shape = RoundedCornerShape(10.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(17.dp),
                        color = Olive,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (enabled) TextMuted else TextMuted.copy(alpha = 0.45f),
                        modifier = Modifier.size(19.dp),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DiscoverableMiniIconAction(
    label: String,
    icon: ImageVector,
    state: AppState,
    onClick: () -> Unit,
) {
    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(),
        state = androidx.compose.material3.rememberTooltipState(),
        tooltip = {
            PlainTooltip(
                containerColor = PanelElevated,
                contentColor = TextPrimary,
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        },
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .semantics { contentDescription = label }
                .onFocusChanged { focusState ->
                    if (focusState.isFocused) state.statusLine = "Enter $label"
                }
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = TextMuted,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Composable
fun WorkspaceControls(state: AppState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val chips = workspaceFilterChips(state)
            if (chips.isEmpty()) {
                PaletteSearchHint()
            } else {
                chips.forEach { chip ->
                    WorkspaceFilterChipView(
                        chip = chip,
                        state = state,
                    )
                }
            }
        }

        if (state.compactRows) {
            WorkspaceFilterChipView(
                chip = WorkspaceFilterChip("Rows", "Compact", clearable = false),
                state = state,
            )
        }
    }
}

@Composable
private fun PaletteSearchHint() {
    Row(
        modifier = Modifier
            .height(36.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(Color(0xFF15181C))
            .border(1.dp, Border, RoundedCornerShape(999.dp))
            .padding(horizontal = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Icon(
            imageVector = Icons.Rounded.Search,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = "Space to search or filter",
            color = TextMuted,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun WorkspaceFilterChipView(
    chip: WorkspaceFilterChip,
    state: AppState,
) {
    Row(
        modifier = Modifier
            .height(36.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(Color(0xFF15181C))
            .border(1.dp, Border, RoundedCornerShape(999.dp))
            .padding(start = 10.dp, end = if (chip.clearable) 3.dp else 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = chip.label,
            color = Olive,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = chip.value,
            color = TextPrimary,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (chip.clearable) {
            DiscoverableMiniIconAction(
                label = "Clear filter",
                icon = Icons.Rounded.Close,
                state = state,
                onClick = { state.clearFilter() },
            )
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

    if (prs.isEmpty()) {
        EmptyState(state)
        return
    }

    val currentView = state.view
    val savedViewport = state.queueViewportState(currentView)
    val listState = remember(currentView) {
        LazyListState(
            firstVisibleItemIndex = savedViewport?.firstVisibleItemIndex ?: 0,
            firstVisibleItemScrollOffset = savedViewport?.firstVisibleItemScrollOffset ?: 0,
        )
    }

    DisposableEffect(currentView, listState) {
        onDispose {
            state.rememberQueueViewport(
                view = currentView,
                firstVisibleItemIndex = listState.firstVisibleItemIndex,
                firstVisibleItemScrollOffset = listState.firstVisibleItemScrollOffset,
            )
        }
    }

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

            // Preserve spatial context. Expanding an already visible PR should not
            // suddenly jump it to the top of the viewport.
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
                    title = "REVIEW QUEUE",
                    prs = prs,
                    state = state,
                    markFirst = state.sortMode == "Urgency" && !state.groupByRepository,
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
    val expanded = state.expandedPullRequestKey == pr.key

    val presentation = rowPresentation(state, pr, startHere)
    val rowPadding = if (state.compactRows) 10.dp else 14.dp
    val avatarSize = if (state.compactRows) 34.dp else 40.dp
    val railHeight = if (state.compactRows) 46.dp else 56.dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (selected || expanded) Color(0xFF23282E) else PanelBg),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Transparent)
                .clickable {
                    state.keyboardMode = KeyboardMode.Normal
                    state.keyboardFocusRegion = FocusRegion.PullRequestList
                    state.togglePullRequestDetails(pr)
                }
                .padding(horizontal = 22.dp, vertical = rowPadding),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(railHeight)
                    .clip(RoundedCornerShape(999.dp))
                    .background(presentation.color.copy(alpha = if (selected || startHere || presentation.strong) 1f else 0.48f)),
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
                        text = pr.repository.toString(),
                        color = TextMuted,
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
                        text = if (selected) {
                            selectedQueuePosition(state)
                                ?.let { "${it.current} of ${it.total} · ${compactCiLabel(pr)}" }
                                ?: compactCiLabel(pr)
                        } else {
                            rowUpdatedPrefix(pr)
                        },
                        color = when {
                            pr.checksFailing > 0 -> Rose
                            isStale(pr.updatedAt) -> Amber
                            else -> TextMuted
                        },
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.width(if (selected) 150.dp else 132.dp),
                    )

                    Spacer(Modifier.width(6.dp))

                    Text(
                        text = if (expanded) "⌄" else "›",
                        color = if (selected || expanded) Olive else TextMuted,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }

                Text(
                    text = pr.title,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = if (selected && !state.compactRows) 2 else 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                ) {
                    queueChangeMarker(state, pr)?.let { marker ->
                        Text(
                            text = marker.label,
                            color = marker.color,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    Text(
                        text = queueRowMetadata(pr, selected),
                        color = if (presentation.strong) {
                            TextPrimary
                        } else {
                            TextMuted
                        },
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )

                }
            }
        }

        AnimatedVisibility(visible = state.expandedPullRequestKey == pr.key) {
            InlineReviewBrief(state = state, pr = pr)
        }

        Divider(
            modifier = Modifier.padding(start = if (expanded) 22.dp else 76.dp),
            color = Border,
        )
    }
}

private data class QueueChangeMarker(
    val label: String,
    val color: Color,
)

private fun queueChangeMarker(
    state: AppState,
    pr: PullRequest,
): QueueChangeMarker? = when {
    pr.key in state.newPullRequestKeys -> QueueChangeMarker("NEW", Olive)
    pr.key in state.updatedPullRequestKeys -> QueueChangeMarker("UPDATED", Amber)
    else -> null
}

private fun compactCiLabel(pr: PullRequest): String = when {
    pr.checksFailing > 0 -> "CI ✕ ${pr.checksFailing}"
    pr.checksPending > 0 -> "CI … ${pr.checksPending}"
    pr.checksTotal > 0 -> "CI ✓"
    else -> staleOrRelativeLabel(pr.updatedAt)
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
        add(rowIdentityMetadata(pr))

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

fun rowIdentityMetadata(pr: PullRequest): String = "${pr.repository} #${pr.number}"

fun rowUpdatedPrefix(pr: PullRequest): String = "Updated ${staleOrRelativeLabel(pr.updatedAt)}"

data class WorkspaceFilterChip(
    val label: String,
    val value: String,
    val clearable: Boolean,
)

fun workspaceFilterChips(state: AppState): List<WorkspaceFilterChip> = buildList {
    state.searchQuery
        .takeIf { it.isNotBlank() }
        ?.let { add(WorkspaceFilterChip("Filter", it, clearable = true)) }

    if (state.sortMode != "Urgency") {
        add(WorkspaceFilterChip("Sort", state.sortMode, clearable = false))
    }

    if (state.groupByRepository) {
        add(WorkspaceFilterChip("Group", "Repository", clearable = false))
    }

    when (val scope = state.currentQueueScopeFilter()) {
        QueueScopeFilter.All -> Unit
        is QueueScopeFilter.Organization ->
            add(WorkspaceFilterChip("Organization", scope.owner, clearable = true))
        is QueueScopeFilter.Repository ->
            add(WorkspaceFilterChip("Repository", scope.nameWithOwner, clearable = true))
    }
}

data class SetupChecklistItem(
    val label: String,
    val complete: Boolean,
)

fun setupChecklistItems(state: AppState): List<SetupChecklistItem> = listOf(
    SetupChecklistItem("GitHub CLI", state.ghPathText.isNotBlank() || state.ghDetectionSource != "Not detected"),
    SetupChecklistItem("Tracked repositories", parseLines(state.repositoriesText).isNotEmpty()),
    SetupChecklistItem("Refresh", state.lastRefreshFinishedAt != null || state.pullRequests.isNotEmpty()),
)

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

            val staleCount = queue.count { isStale(it.updatedAt) }
            val failingCount = queue.count { it.checksFailing > 0 }

            buildString {
                append("${queue.size} waiting")
                if (staleCount > 0) append(" · $staleCount stale")
                if (failingCount > 0) append(" · $failingCount CI failing")
                if (oldest != null) append(" · oldest $oldest")
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
fun UpdateBanner(state: AppState) {
    val update = state.updateState
    val visible = when (update) {
        is UpdateState.Available -> !update.dismissed
        is UpdateState.Downloading,
        is UpdateState.Verifying,
        is UpdateState.ReadyToInstall,
        is UpdateState.Installing,
        is UpdateState.Restarting -> true
        is UpdateState.Failed -> update.visible
        UpdateState.Idle,
        UpdateState.Checking,
        is UpdateState.Current -> false
    }

    AnimatedVisibility(visible = visible) {
        Surface(
            color = OliveSoft,
            border = BorderStroke(1.dp, Olive.copy(alpha = 0.35f)),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .semantics { contentDescription = "RevQ update: ${update::class.simpleName}" }
                    .padding(horizontal = 14.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = when (update) {
                            is UpdateState.Available -> "RevQ ${update.release.version} is available"
                            is UpdateState.Downloading -> "Downloading RevQ ${update.release.version}…"
                            is UpdateState.Verifying -> "Verifying RevQ ${update.release.version}…"
                            is UpdateState.ReadyToInstall -> "Preparing RevQ ${update.release.version} installation…"
                            is UpdateState.Installing -> "Preparing RevQ ${update.release.version} installation…"
                            is UpdateState.Restarting -> "Restarting RevQ…"
                            is UpdateState.Failed -> update.message
                            else -> ""
                        },
                        color = TextPrimary,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (update is UpdateState.Downloading && update.progress != null) {
                        LinearProgressIndicator(
                            progress = { update.progress.coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            color = Olive,
                            trackColor = Border,
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                when (update) {
                    is UpdateState.Available -> {
                        if (update.release.notes.isNotBlank()) {
                            TextButton(onClick = state::showUpdateReleaseNotes) {
                                Text("What's new", color = TextMuted)
                            }
                        }
                        TextButton(onClick = state::downloadAndInstallUpdate) {
                            Text("Download & install", color = Olive)
                        }
                        TextButton(onClick = state::dismissUpdate) {
                            Text("Dismiss", color = TextMuted)
                        }
                    }
                    is UpdateState.Downloading -> TextButton(onClick = state::cancelUpdateDownload) {
                        Text("Cancel", color = TextMuted)
                    }
                    is UpdateState.Failed -> {
                        if (update.release != null) {
                            TextButton(onClick = state::retryUpdateDownload) {
                                Text("Retry", color = Olive)
                            }
                        }
                        TextButton(onClick = state::dismissUpdateFailure) {
                            Text("Dismiss", color = TextMuted)
                        }
                    }
                    else -> Unit
                }
            }
        }
    }
}

@Composable
fun BottomStatusBar(
    state: AppState,
    paletteState: CommandPaletteState,
) {
    val feedback = state.actionFeedback
    LaunchedEffect(feedback?.sequence) {
        if (feedback != null) {
            delay(2_500)
            if (state.actionFeedback?.sequence == feedback.sequence) {
                state.actionFeedback = null
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(Color(0xFF0E1012))
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val leftText = when {
            feedback != null -> feedback.message
            state.lastUndoReview != null -> "Marked reviewed · Undo available"
            state.isRefreshing -> refreshProgressLine(state)
            state.lastRefreshError != null -> "Refresh failed"
            state.refreshDelta != null -> "Refreshed · ${state.refreshDelta!!.summaryText()}"
            state.selectedPullRequest != null -> {
                val pr = state.selectedPullRequest!!
                "${pr.repository} #${pr.number}"
            }
            state.lastRefreshFinishedAt != null ->
                "Ready · ${state.reviewQueue().size} reviews waiting"
            else -> state.statusLine
        }

        Text(
            text = leftText,
            color = when {
                feedback != null -> Olive
                state.lastRefreshError != null -> Rose
                else -> TextMuted
            },
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )

        if (state.lastUndoReview != null && feedback == null) {
            TextButton(onClick = { state.undoMarkReviewed() }) {
                Text("Undo", color = Olive)
            }
            Spacer(Modifier.width(8.dp))
        }

        NavigationFooterHints(state)
        Spacer(Modifier.width(10.dp))

        TextButton(onClick = { paletteState.open() }) {
            Icon(
                imageVector = Icons.Rounded.KeyboardCommandKey,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(6.dp))
            Text("Space Commands", color = TextMuted)
        }
    }
}

@Composable
private fun NavigationFooterHints(state: AppState) {
    val hints = if (state.view == View.Settings) {
        listOf(
            "j/k" to "Move",
            "h/l" to "Sections",
            "Enter" to "Activate",
            "Esc" to "Exit",
        )
    } else {
        listOf(
            "j/k" to "Move",
            "h/l" to "Panes",
            "Enter" to "Inspect",
            "Home/End" to "Ends",
            "Esc" to "Back",
        )
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        hints.forEach { (key, label) ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = key,
                    color = TextPrimary,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = label,
                    color = TextMuted,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}

@Composable
private fun AboutRevqDialog(
    state: AppState,
    onDismiss: () -> Unit,
) {
    val version = state.installedVersion.toString()
    val repositoryUrl = (
            System.getProperty("revq.repositoryUrl")
                ?: System.getenv("REVQ_REPOSITORY_URL")
            )
        ?.takeIf { it.isNotBlank() }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.width(460.dp),
            color = PanelBg,
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.dp, Border),
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    AppBrandMark(
                        contentDescription = null,
                        modifier = Modifier.size(54.dp),
                    )
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = "RevQ",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Text(
                            text = "Review companion",
                            color = TextMuted,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }

                Divider(color = Border)

                Row(Modifier.fillMaxWidth()) {
                    Text("Version", color = TextMuted, style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.weight(1f))
                    Text(version, color = TextPrimary, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                }

                Text(
                    text = "A keyboard-first companion for keeping pull request review work visible and moving.",
                    color = TextMuted,
                    style = MaterialTheme.typography.bodyMedium,
                )

                val available = state.updateState as? UpdateState.Available
                if (available != null) {
                    Text(
                        text = "RevQ ${available.release.version} is available",
                        color = Olive,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    if (available.release.notes.isNotBlank()) {
                        Text(
                            text = available.release.notes.take(600),
                            color = TextMuted,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 8,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(
                        enabled = state.updateState != UpdateState.Checking,
                        onClick = state::checkForUpdates,
                    ) {
                        Text("Check for updates", color = Olive)
                    }
                    Spacer(Modifier.width(4.dp))
                    TextButton(
                        enabled = repositoryUrl != null,
                        onClick = {
                            repositoryUrl?.let(::openUrl) ?: run {
                                state.statusLine = "Repository URL is not configured"
                            }
                        },
                    ) {
                        Text("Open project repository", color = if (repositoryUrl != null) Olive else TextMuted)
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = onDismiss) {
                        Text("Close")
                    }
                }
            }
        }
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

fun pullRequestReasonLabel(
    state: AppState,
    pr: PullRequest,
    startHere: Boolean = false,
): String {
    if (state.view == View.Pinned && state.isPinned(pr)) return "Pinned"

    return when {
        pr.source == PullRequestSource.Mine -> ownPullRequestStatusTitle(ownPullRequestPrimaryStatus(pr))
        state.view == View.Handled -> {
            val changed = whatChanged(state, pr)
            if (changed.startsWith("Updated")) "Changed" else "Reviewed"
        }
        state.view == View.Today -> "Today"
        state.view == View.Blocked -> "Blocked"
        state.view == View.Ready -> "Ready"
        startHere -> "Up next"
        else -> "Requested"
    }
}

@Composable
private fun PullRequestReasonChip(label: String) {
    Text(
        text = label,
        color = TextMuted,
        style = MaterialTheme.typography.labelSmall,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(Color(0xFF15181C))
            .border(1.dp, Border, RoundedCornerShape(999.dp))
            .padding(horizontal = 7.dp, vertical = 2.dp),
    )
}

@Composable
fun EmptyState(state: AppState) {
    val spec = emptyStateSpec(state)

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

internal fun emptyStateSpec(state: AppState): EmptyStateSpec {
    if (state.searchQuery.isNotBlank()) {
        return EmptyStateSpec(
            eyebrow = "FILTERED OUT",
            title = "No matches in this view.",
            subtitle = "The active palette filter is hiding everything in ${state.view.label}. Clear it to return to the full list.",
            accent = Amber,
            icon = Icons.Rounded.Search,
            heroLabel = "Filtered list",
            detailLabel = state.searchQuery,
            primaryLabel = "Clear filter",
            primaryAction = { it.clearFilter() },
            secondaryLabel = "Refresh",
            secondaryAction = { it.refresh() },
        )
    }

    if (state.currentQueueScopeFilter() != QueueScopeFilter.All) {
        return EmptyStateSpec(
            eyebrow = "SCOPE EMPTY",
            title = "No PRs in this scope.",
            subtitle = "The temporary organization or repository scope has no pull requests in ${state.view.label}.",
            accent = Amber,
            icon = Icons.Rounded.Search,
            heroLabel = "Scoped queue",
            detailLabel = "Clear the scope to see the full queue",
            primaryLabel = "Show all scopes",
            primaryAction = { it.clearQueueScopeFilter() },
            secondaryLabel = "Refresh",
            secondaryAction = { it.refresh() },
        )
    }

    if (parseLines(state.repositoriesText).isEmpty()) {
        return EmptyStateSpec(
            eyebrow = "SETUP NEEDED",
            title = "Choose repositories to track.",
            subtitle = "Add repositories directly, or use configured organizations to discover available repositories and select the ones RevQ should track.",
            accent = Olive,
            icon = Icons.Rounded.FolderOpen,
            heroLabel = "No tracked repositories",
            detailLabel = "Organizations are discovery sources; only selected repositories are refreshed",
            primaryLabel = "Open tracking settings",
            primaryAction = { it.openTrackingSettings() },
            secondaryLabel = "Refresh",
            secondaryAction = { it.refresh() },
        )
    }

    return emptyStateSpec(state.view)
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
    View.Handled,
    View.Mine,
    View.Pinned,
)

private fun moveWithinFocusedRegion(
    state: AppState,
    delta: Int,
) {
    when (state.keyboardFocusRegion) {
        FocusRegion.Sidebar -> moveSidebarSelection(state, delta)
        FocusRegion.PullRequestList -> moveSelection(state, delta)
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
                    state.showSidebarNavigationHintOnce()
                    FocusRegion.Sidebar
                }

                else -> FocusRegion.PullRequestList
            }
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
                state.expandedPullRequestKey = null
                state.selectedPullRequest = if (first) items.first() else items.last()
            }
        }

    }
}

private fun moveByHalfPage(
    state: AppState,
    direction: Int,
) {
    when (state.keyboardFocusRegion) {
        // Half-page movement only makes sense in the pull request list.
        // The sidebar has only a few destinations and j/k already wrap through them.
        FocusRegion.Sidebar -> Unit
        FocusRegion.PullRequestList -> moveSelection(
            state = state,
            delta = direction * max(1, state.keyboardPageStep),
        )
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
                state.toggleSelectedPullRequestDetails()
            }
        }
    }
}

private fun escapeKeyboardContext(state: AppState) {
    when (state.keyboardFocusRegion) {
        FocusRegion.PullRequestList -> {
            if (state.expandedPullRequestKey != null) {
                state.expandedPullRequestKey = null
            } else if (state.selectedPullRequest != null) {
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
    if (SidebarKeyboardViews.isEmpty()) return

    val currentIndex = SidebarKeyboardViews.indexOf(state.sidebarKeyboardView)
        .takeIf { it >= 0 }
        ?: SidebarKeyboardViews.indexOf(state.view).coerceAtLeast(0)

    val size = SidebarKeyboardViews.size
    val nextIndex = ((currentIndex + delta) % size + size) % size
    state.sidebarKeyboardView = SidebarKeyboardViews[nextIndex]
}

fun moveSelection(state: AppState, delta: Int) {
    val items = state.visiblePullRequests()
    if (items.isEmpty()) return
    state.expandedPullRequestKey = null
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

fun buildDisplayDiagnostics(
    composeDensity: Float,
    fontScale: Float,
): String {
    val toolkitDpi = runCatching { Toolkit.getDefaultToolkit().screenResolution }.getOrNull()
    val screen = runCatching { Toolkit.getDefaultToolkit().screenSize }.getOrNull()
    val transform = runCatching {
        java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment()
            .defaultScreenDevice
            .defaultConfiguration
            .defaultTransform
    }.getOrNull()
    val session = System.getenv("XDG_SESSION_TYPE")
        ?: if (System.getenv("WAYLAND_DISPLAY") != null) "wayland" else "unknown"

    return buildString {
        append("display: Compose density ${"%.2f".format(composeDensity)}x")
        append(", font scale ${"%.2f".format(fontScale)}x")
        append(", toolkit DPI ${toolkitDpi ?: "unavailable"}")
        append(", graphics ${transform?.scaleX?.let { "%.2f".format(it) } ?: "unavailable"}x")
        append(", screen ${screen?.let { "${it.width}x${it.height}" } ?: "unavailable"}")
        append(", session $session")
    }
}


fun installBestEffortTray(state: AppState) {
    runCatching {
        System.err.println("RevQ tray: checking platform support")
        val awtSupported = SystemTray.isSupported()
        val backend = selectTrayBackend(
            desktop = System.getenv("XDG_CURRENT_DESKTOP"),
            sessionType = System.getenv("XDG_SESSION_TYPE"),
            statusNotifierAvailable = statusNotifierWatcherAvailable(),
            awtTraySupported = awtSupported,
        )
        if (backend == TrayBackend.StatusNotifier && StatusNotifierTray.install(state)) {
            System.err.println("RevQ tray: StatusNotifierItem installed")
            state.trayAvailable = true
            state.statusLine = "RevQ tray installed"
            return
        }
        if (!awtSupported) {
            state.trayAvailable = false
            System.err.println("RevQ tray: unsupported by this desktop session")
            return
        }
        val tray = SystemTray.getSystemTray()
        if (tray.trayIcons.any { it.toolTip == "RevQ" }) {
            state.trayAvailable = true
            return
        }
        val image = loadRevqTrayImage()
        System.err.println("RevQ tray: icon loaded")
        val popup = PopupMenu()
        popup.add(MenuItem("Show RevQ").apply {
            addActionListener {
                EventQueue.invokeLater {
                    state.mainWindowVisible = true
                    state.selectView(View.NeedsReview)
                }
            }
        })
        popup.add(MenuItem("Refresh").apply {
            addActionListener {
                EventQueue.invokeLater {
                    state.mainWindowVisible = true
                    state.refresh()
                }
            }
        })
        popup.add(MenuItem("Open Needs Review").apply {
            addActionListener {
                EventQueue.invokeLater {
                    state.mainWindowVisible = true
                    state.selectView(View.NeedsReview)
                }
            }
        })
        popup.addSeparator()
        popup.add(MenuItem("Quit").apply { addActionListener { exitProcess(0) } })
        val trayIcon = TrayIcon(image, "RevQ", popup).apply {
            isImageAutoSize = true
            addActionListener {
                EventQueue.invokeLater {
                    state.mainWindowVisible = true
                    state.selectView(View.NeedsReview)
                }
            }
        }
        tray.add(trayIcon)
        System.err.println("RevQ tray: menu and click handlers installed")
        state.trayAvailable = true
        state.statusLine = "RevQ tray installed"
    }.onFailure {
        System.err.println("RevQ tray: initialization failed: ${it.message}")
        state.trayAvailable = false
        state.statusLine = "Tray unavailable on this desktop session"
    }
}

private fun loadRevqTrayImage(): java.awt.Image {
    val osName = System.getProperty("os.name").lowercase()
    val isMac = osName.contains("mac")
    return awtTrayImage(if (isMac) 18 else 22)
}

fun trayIconResourceName(darkAppearance: Boolean): String =
    PlatformPresence.trayIconResourceName(darkAppearance)

enum class TrayBackend {
    StatusNotifier,
    Awt,
    None,
}

fun selectTrayBackend(
    desktop: String?,
    sessionType: String?,
    statusNotifierAvailable: Boolean,
    awtTraySupported: Boolean,
): TrayBackend = PlatformPresence.selectTrayBackend(
    desktop = desktop,
    sessionType = sessionType,
    statusNotifierAvailable = statusNotifierAvailable,
    awtTraySupported = awtTraySupported,
)

private fun statusNotifierWatcherAvailable(): Boolean = runCatching {
    val process = ProcessBuilder(
        "gdbus",
        "call",
        "--session",
        "--dest", "org.freedesktop.DBus",
        "--object-path", "/org/freedesktop/DBus",
        "--method", "org.freedesktop.DBus.NameHasOwner",
        "org.kde.StatusNotifierWatcher",
    )
        .redirectErrorStream(true)
        .start()
    val output = process.inputStream.bufferedReader().readText()
    val finished = process.waitFor(2, TimeUnit.SECONDS)
    if (!finished) process.destroyForcibly()
    finished && output.contains("true")
}.getOrDefault(false)

fun desktopUsesDarkAppearance(): Boolean = runCatching {
    val process = ProcessBuilder(
        "gdbus",
        "call",
        "--session",
        "--dest", "org.freedesktop.portal.Desktop",
        "--object-path", "/org/freedesktop/portal/desktop",
        "--method", "org.freedesktop.portal.Settings.Read",
        "org.freedesktop.appearance",
        "color-scheme",
    )
        .redirectErrorStream(true)
        .start()
    val output = process.inputStream.bufferedReader().readText()
    val finished = process.waitFor(2, TimeUnit.SECONDS)
    if (!finished) process.destroyForcibly()
    finished && Regex("""uint32\s+1""").containsMatchIn(output)
}.getOrDefault(true)


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
    pr.reviewRequestKind?.name.orEmpty(),
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
        reviewRequestKind = parts.getOrNull(22)
            ?.takeIf(String::isNotBlank)
            ?.let { runCatching { ReviewRequestKind.valueOf(it) }.getOrNull() },
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

fun ownPullRequestSignals(pr: PullRequest): List<OwnPullRequestStatus> =
    PullRequestAttention.describe(pr).ownStatuses

fun ownPullRequestPrimaryStatus(pr: PullRequest): OwnPullRequestStatus =
    PullRequestAttention.describe(pr).ownStatus ?: OwnPullRequestStatus.NoActionNeeded

fun ownPullRequestNeedsAction(pr: PullRequest): Boolean =
    PullRequestAttention.describe(pr).needsAction

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

fun attentionKind(pr: PullRequest): AttentionKind =
    PullRequestAttention.describe(pr).kind

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
