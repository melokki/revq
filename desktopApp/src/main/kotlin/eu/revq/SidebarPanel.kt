package eu.revq

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.RateReview
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Today
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.skia.Image as SkiaImage
import java.io.File
import java.net.URL
import java.util.concurrent.TimeUnit
import eu.revq.keyboard.FocusRegion
import eu.revq.keyboard.KeyboardMode

private val SidebarSelectedBackground = Color(0xFF1D2227)
private val SidebarOliveSoft = Color(0xFF2C3323)
private val SidebarRoseSoft = Color(0xFF3B2427)
private val SidebarReadySoft = Color(0xFF21352A)
private val SidebarNeutralBadge = Color(0xFF2A2F36)
private val SidebarDanger = Color(0xFFE06A6A)

private data class GitHubProfile(
    val login: String,
    val name: String?,
    val avatarUrl: String?,
    val profileUrl: String,
)

@Composable
fun SidebarPanel(
    state: AppState,
) {
    var githubProfile by remember { mutableStateOf<GitHubProfile?>(null) }
    var profileLoading by remember { mutableStateOf(true) }
    var profileError by remember { mutableStateOf<String?>(null) }
    var repositoryScope by remember { mutableStateOf<String?>(null) }
    var searchBeforeRepositoryScope by remember { mutableStateOf("") }

    LaunchedEffect(state.ghPathText, state.ghTestResult) {
        profileLoading = true
        profileError = null

        val result = withContext(Dispatchers.IO) {
            runCatching { loadGitHubProfile(state.ghPathText) }
        }

        githubProfile = result.getOrNull()
        profileError = result.exceptionOrNull()?.message
        profileLoading = false
    }

    val repositoryOptions = state.activePullRequests()
        .map { it.repository.toString() }
        .distinct()
        .sorted()

    Column(
        modifier = Modifier
            .width(284.dp)
            .fillMaxHeight()
            .background(SidebarBg)
            .border(
                width = 1.dp,
                color = if (
                    state.keyboardMode == KeyboardMode.Normal &&
                    state.keyboardFocusRegion == FocusRegion.Sidebar
                ) {
                    Olive.copy(alpha = 0.22f)
                } else {
                    Color.Transparent
                },
            ),
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                SidebarHeader()
            }

            if (repositoryOptions.isNotEmpty()) {
                item {
                    RepositoryScopeSelector(
                        selectedRepository = repositoryScope,
                        repositories = repositoryOptions,
                        onSelect = { repo ->
                            if (repositoryScope == null && repo != null) {
                                searchBeforeRepositoryScope = state.searchQuery
                            }
                            repositoryScope = repo
                            state.searchQuery = repo ?: searchBeforeRepositoryScope
                            state.selectedPullRequest = null
                            state.statusLine = if (repo == null) {
                                "Repository scope cleared"
                            } else {
                                "Repository scope: $repo"
                            }
                        },
                        onManageRepositories = { state.selectView(View.Settings) },
                    )
                }
            }

            item {
                SidebarGroup(title = "Primary") {
                    SidebarItem(
                        state = state,
                        view = View.NeedsReview,
                        count = state.reviewQueue().size,
                        icon = Icons.Rounded.RateReview,
                        badgeKind = SidebarBadgeKind.Review,
                        alwaysShowCount = true,
                    )

                    val myPullRequests = state.activePullRequests()
                        .filter { it.source == PullRequestSource.Mine }
                    val myActionCount = myPullRequests.count {
                        attentionKind(it) == AttentionKind.Action || attentionKind(it) == AttentionKind.Blocked
                    }

                    SidebarItem(
                        state = state,
                        view = View.Mine,
                        count = myPullRequests.size,
                        secondaryCount = myActionCount,
                        icon = Icons.Rounded.Person,
                    )

                    SidebarItem(
                        state = state,
                        view = View.Pinned,
                        count = state.pinnedPullRequests().size,
                        icon = Icons.Rounded.PushPin,
                    )
                }
            }

            item {
                SidebarGroup(title = "Views") {
                    SidebarItem(
                        state = state,
                        view = View.Today,
                        count = state.todayPullRequests().size,
                        icon = Icons.Rounded.Today,
                    )
                    SidebarItem(
                        state = state,
                        view = View.Blocked,
                        count = state.activePullRequests().count {
                            attentionKind(it) == AttentionKind.Blocked
                        },
                        icon = Icons.Rounded.Block,
                        badgeKind = SidebarBadgeKind.Blocked,
                    )
                    SidebarItem(
                        state = state,
                        view = View.Ready,
                        count = state.activePullRequests().count {
                            attentionKind(it) == AttentionKind.Ready
                        },
                        icon = Icons.Rounded.CheckCircle,
                        badgeKind = SidebarBadgeKind.Ready,
                    )
                    SidebarItem(
                        state = state,
                        view = View.Handled,
                        label = "Reviewed",
                        count = state.handledPullRequests().size,
                        icon = Icons.Rounded.DoneAll,
                    )
                }
            }

            item {
                Spacer(Modifier.height(4.dp))
            }
        }

        HorizontalDivider(Modifier, DividerDefaults.Thickness, color = Border)

        GitHubAccountFooter(
            state = state,
            profile = githubProfile,
            isLoading = profileLoading,
            profileError = profileError,
        )
    }
}

@Composable
private fun SidebarHeader() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "RevQ",
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            text = "Reviews first · review companion",
            color = TextMuted,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun RepositoryScopeSelector(
    selectedRepository: String?,
    repositories: List<String>,
    onSelect: (String?) -> Unit,
    onManageRepositories: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(PanelBg)
                .border(1.dp, Border, RoundedCornerShape(10.dp))
                .clickable { expanded = true }
                .padding(horizontal = 11.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(PanelElevated),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.FolderOpen,
                    contentDescription = null,
                    tint = if (selectedRepository == null) TextMuted else Olive,
                    modifier = Modifier.size(17.dp),
                )
            }

            Spacer(Modifier.width(10.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text = "Repository scope",
                    color = TextMuted.copy(alpha = 0.82f),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = selectedRepository ?: "All repositories",
                    color = TextPrimary,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Icon(
                imageVector = if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                contentDescription = if (expanded) "Close repository scope menu" else "Open repository scope menu",
                tint = TextMuted,
                modifier = Modifier.size(18.dp),
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .width(318.dp)
                .background(PanelBg, RoundedCornerShape(14.dp))
                .border(1.dp, Border, RoundedCornerShape(14.dp)),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PanelBg)
                    .padding(vertical = 8.dp),
            ) {
                RepositoryScopeMenuHeader(selectedRepository)
                AccountMenuDivider()
                AccountMenuSectionLabel("Scope")

                RepositoryScopeMenuItem(
                    label = "All repositories",
                    selected = selectedRepository == null,
                    onClick = {
                        expanded = false
                        onSelect(null)
                    },
                )

                if (repositories.isNotEmpty()) {
                    AccountMenuDivider()
                    AccountMenuSectionLabel("Repositories")

                    // DropdownMenu uses intrinsic measurement internally. LazyColumn is backed by
                    // SubcomposeLayout and cannot answer intrinsic measurement queries, which causes
                    // the runtime crash. A regular scrollable Column avoids SubcomposeLayout here.
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 320.dp)
                            .verticalScroll(rememberScrollState()),
                    ) {
                        repositories.forEach { repository ->
                            RepositoryScopeMenuItem(
                                label = repository,
                                selected = repository == selectedRepository,
                                onClick = {
                                    expanded = false
                                    onSelect(repository)
                                },
                            )
                        }
                    }
                }

                AccountMenuDivider()

                AccountMenuAction(
                    label = "Manage repositories",
                    icon = Icons.Rounded.Settings,
                    onClick = {
                        expanded = false
                        onManageRepositories()
                    },
                )
            }
        }
    }
}

@Composable
private fun RepositoryScopeMenuHeader(selectedRepository: String?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(PanelElevated),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.FolderOpen,
                contentDescription = null,
                tint = if (selectedRepository == null) TextMuted else Olive,
                modifier = Modifier.size(20.dp),
            )
        }

        Spacer(Modifier.width(11.dp))

        Column(Modifier.weight(1f)) {
            Text(
                text = "Repository scope",
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = selectedRepository ?: "All repositories",
                color = TextMuted,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun RepositoryScopeMenuItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(9.dp))
            .background(if (selected) SidebarSelectedBackground else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (selected) SidebarOliveSoft else PanelElevated),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (selected) Icons.Rounded.Check else Icons.Rounded.FolderOpen,
                contentDescription = null,
                tint = if (selected) Olive else TextMuted,
                modifier = Modifier.size(17.dp),
            )
        }

        Spacer(Modifier.width(10.dp))

        Text(
            text = label,
            color = if (selected) TextPrimary else TextMuted,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun SidebarGroup(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title.uppercase(),
            color = TextMuted,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
        )
        content()
    }
}

private enum class SidebarBadgeKind {
    Review,
    Blocked,
    Ready,
    Neutral,
}

@Composable
private fun SidebarItem(
    state: AppState,
    view: View,
    count: Int?,
    icon: ImageVector,
    label: String = view.label,
    secondaryCount: Int = 0,
    badgeKind: SidebarBadgeKind = SidebarBadgeKind.Neutral,
    alwaysShowCount: Boolean = false,
) {
    val selected = state.view == view
    val keyboardCursor =
        state.keyboardMode == KeyboardMode.Normal &&
                state.keyboardFocusRegion == FocusRegion.Sidebar &&
                state.sidebarKeyboardView == view
    val quietForFocus = state.focusReviewMode && view != View.NeedsReview
    val itemShape = RoundedCornerShape(10.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (quietForFocus) 0.48f else 1f)
            .clip(itemShape)
            .background(
                when {
                    keyboardCursor -> PanelElevated
                    selected -> SidebarSelectedBackground
                    else -> Color.Transparent
                },
            )
            .border(
                width = 1.dp,
                color = if (keyboardCursor) Olive.copy(alpha = 0.48f) else Color.Transparent,
                shape = itemShape,
            )
            .clickable {
                state.sidebarKeyboardView = view
                state.selectView(view)
            }
            .padding(vertical = 8.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(24.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(if (selected) Olive else Color.Transparent),
        )
        Spacer(Modifier.width(8.dp))

        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) Olive else TextMuted,
            modifier = Modifier.size(19.dp),
        )

        Spacer(Modifier.width(9.dp))

        Text(
            text = label,
            color = if (selected) TextPrimary else TextMuted,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        if (secondaryCount > 0) {
            Text(
                text = "•$secondaryCount",
                color = Amber,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.width(5.dp))
        }

        val shouldShowCount = count != null && (alwaysShowCount || count > 0)
        if (shouldShowCount) {
            SidebarCountBadge(
                count = count,
                kind = badgeKind,
                emphasized = selected,
            )
        }
    }
}

@Composable
private fun SidebarCountBadge(
    count: Int,
    kind: SidebarBadgeKind,
    emphasized: Boolean,
) {
    val background = when (kind) {
        SidebarBadgeKind.Review -> SidebarOliveSoft
        SidebarBadgeKind.Blocked -> SidebarRoseSoft
        SidebarBadgeKind.Ready -> SidebarReadySoft
        SidebarBadgeKind.Neutral -> SidebarNeutralBadge
    }

    val foreground = when (kind) {
        SidebarBadgeKind.Review -> Olive
        SidebarBadgeKind.Blocked -> SidebarDanger
        SidebarBadgeKind.Ready -> ReadyGreen
        SidebarBadgeKind.Neutral -> if (emphasized) TextPrimary else TextMuted
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(background)
            .padding(horizontal = 7.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = count.toString(),
            color = foreground,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun GitHubAccountFooter(
    state: AppState,
    profile: GitHubProfile?,
    isLoading: Boolean,
    profileError: String?,
) {
    var menuOpen by remember { mutableStateOf(false) }
    val connectionNeedsAttention = profileError != null

    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { menuOpen = true }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
        ) {
            GitHubAvatar(
                profile = profile,
                connected = profile != null && !connectionNeedsAttention,
                loading = isLoading,
                error = connectionNeedsAttention,
                size = 40,
            )

            Spacer(Modifier.width(11.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text = when {
                        profile != null -> "@${profile.login}"
                        isLoading -> "GitHub account"
                        else -> "GitHub CLI"
                    },
                    color = TextPrimary,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Text(
                    text = when {
                        profileError != null -> "GitHub needs attention"
                        profile != null -> "GitHub connected"
                        isLoading -> "Checking connection…"
                        else -> "Not connected"
                    },
                    color = when {
                        connectionNeedsAttention -> SidebarDanger
                        profile != null -> TextMuted
                        else -> TextMuted
                    },
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Icon(
                imageVector = if (menuOpen) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                contentDescription = if (menuOpen) "Close account menu" else "Open account menu",
                tint = TextMuted,
                modifier = Modifier.size(20.dp),
            )
        }

        AccountMenu(
            expanded = menuOpen,
            onDismissRequest = { menuOpen = false },
            state = state,
            profile = profile,
            profileError = profileError,
        )
    }
}

@Composable
private fun AccountMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    state: AppState,
    profile: GitHubProfile?,
    profileError: String?,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = Modifier
            .width(318.dp)
            .background(PanelBg, RoundedCornerShape(14.dp))
            .border(1.dp, Border, RoundedCornerShape(14.dp)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(PanelBg)
                .padding(vertical = 8.dp),
        ) {
            AccountMenuHeader(
                profile = profile,
                profileError = profileError,
            )

            AccountMenuDivider()

            AccountMenuSectionLabel("RevQ")

            AccountMenuAction(
                label = "Settings",
                icon = Icons.Rounded.Settings,
                onClick = {
                    onDismissRequest()
                    state.selectView(View.Settings)
                },
            )

            AccountMenuDivider()
            AccountMenuSectionLabel("GitHub")

            AccountMenuAction(
                label = if (state.isRefreshing) "Refreshing…" else "Refresh now",
                icon = Icons.Rounded.Refresh,
                shortcut = "R",
                enabled = !state.isRefreshing,
                onClick = {
                    onDismissRequest()
                    state.refresh()
                },
            )

            if (profileError != null) {
                AccountMenuAttentionCard(
                    message = profileError,
                )
            }

            AccountMenuAction(
                label = if (state.isTestingGh) "Testing connection…" else "Test GitHub connection",
                icon = Icons.Rounded.CheckCircle,
                enabled = !state.isTestingGh,
                onClick = {
                    onDismissRequest()
                    state.testGithubCli()
                },
            )

            AccountMenuAction(
                label = "Open GitHub profile",
                icon = Icons.AutoMirrored.Rounded.OpenInNew,
                enabled = profile != null,
                onClick = {
                    onDismissRequest()
                    profile?.let { openUrl(it.profileUrl) }
                },
            )

            AccountMenuDivider()
            AccountMenuSectionLabel("Support")

            AccountMenuAction(
                label = "Copy diagnostics",
                icon = Icons.Rounded.BugReport,
                onClick = {
                    onDismissRequest()
                    state.copyDiagnostics()
                },
            )

            AccountMenuAction(
                label = "About RevQ",
                icon = Icons.Rounded.Info,
                onClick = {
                    onDismissRequest()
                    state.statusLine = "RevQ · reviews first · review companion"
                },
            )
        }
    }
}

@Composable
private fun AccountMenuHeader(
    profile: GitHubProfile?,
    profileError: String?,
) {
    val hasError = profileError != null

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        GitHubAvatar(
            profile = profile,
            connected = profile != null && !hasError,
            loading = false,
            error = hasError,
            size = 38,
        )

        Spacer(Modifier.width(11.dp))

        Column(Modifier.weight(1f)) {
            Text(
                text = profile?.name ?: profile?.login?.let { "@$it" } ?: "GitHub account",
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            if (profile?.name != null) {
                Text(
                    text = "@${profile.login}",
                    color = TextMuted,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 3.dp),
            ) {
                StatusDot(if (hasError) SidebarDanger else ReadyGreen)
                Spacer(Modifier.width(6.dp))
                Text(
                    text = when {
                        profileError != null -> "GitHub CLI needs attention"
                        profile != null -> "GitHub connected"
                        else -> "GitHub connection unavailable"
                    },
                    color = if (hasError) SidebarDanger else TextMuted,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}

@Composable
private fun AccountMenuSectionLabel(label: String) {
    Text(
        text = label.uppercase(),
        color = TextMuted.copy(alpha = 0.72f),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
    )
}

@Composable
private fun AccountMenuAction(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    shortcut: String? = null,
    enabled: Boolean = true,
) {
    val contentAlpha = if (enabled) 1f else 0.42f

    Row(
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(9.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 9.dp)
            .alpha(contentAlpha),
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
                imageVector = icon,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(17.dp),
            )
        }

        Spacer(Modifier.width(10.dp))

        Text(
            text = label,
            color = TextPrimary,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
        )

        if (shortcut != null) {
            Surface(
                color = SidebarNeutralBadge,
                shape = RoundedCornerShape(6.dp),
            ) {
                Text(
                    text = shortcut,
                    color = TextMuted,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                )
            }
        }
    }
}

@Composable
private fun AccountMenuAttentionCard(message: String) {
    Column(
        modifier = Modifier
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(SidebarRoseSoft)
            .border(1.dp, SidebarDanger.copy(alpha = 0.28f), RoundedCornerShape(10.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = "Needs attention",
            color = SidebarDanger,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = message,
            color = TextMuted,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun AccountMenuDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 6.dp),
        thickness = DividerDefaults.Thickness,
        color = Border.copy(alpha = 0.75f)
    )
}

@Composable
private fun GitHubAvatar(
    profile: GitHubProfile?,
    connected: Boolean,
    loading: Boolean,
    error: Boolean,
    size: Int,
) {
    var avatarBitmap by remember(profile?.avatarUrl) { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(profile?.avatarUrl) {
        avatarBitmap = profile?.avatarUrl?.let { url ->
            withContext(Dispatchers.IO) {
                runCatching { loadAvatarBitmap(url) }.getOrNull()
            }
        }
    }

    val avatarSize = size.dp

    Box(modifier = Modifier.size(avatarSize)) {
        Box(
            modifier = Modifier
                .size(avatarSize)
                .clip(CircleShape)
                .background(PanelElevated)
                .border(1.dp, Border, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            when {
                avatarBitmap != null -> {
                    Image(
                        bitmap = avatarBitmap!!,
                        contentDescription = profile?.login?.let { "$it GitHub avatar" },
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(avatarSize)
                            .clip(CircleShape),
                    )
                }

                profile != null -> {
                    Text(
                        text = avatarFallback(profile.login),
                        color = TextPrimary,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }

                else -> {
                    Icon(
                        imageVector = Icons.Rounded.AccountCircle,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size((size - 14).dp),
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(11.dp)
                .clip(CircleShape)
                .background(
                    when {
                        error -> SidebarDanger
                        connected -> ReadyGreen
                        loading -> Amber
                        else -> TextMuted
                    },
                )
                .border(2.dp, SidebarBg, CircleShape),
        )
    }
}

@Composable
private fun StatusDot(color: Color) {
    Box(
        modifier = Modifier
            .size(7.dp)
            .clip(CircleShape)
            .background(color),
    )
}

private fun loadGitHubProfile(configuredGhPath: String): GitHubProfile {
    val executable = resolveSidebarGhExecutable(configuredGhPath)
    val jq = "[.login, (.name // \"\"), (.avatar_url // \"\"), (.html_url // \"\")] | @tsv"
    val command = listOf(executable, "api", "user", "--jq", jq)

    val process = ProcessBuilder(command)
        .redirectErrorStream(true)
        .start()

    val output = process.inputStream.bufferedReader().readText().trim()
    val finished = process.waitFor(10, TimeUnit.SECONDS)

    if (!finished) {
        process.destroyForcibly()
        error("GitHub profile lookup timed out")
    }

    if (process.exitValue() != 0) {
        error(output.ifBlank { "GitHub profile lookup failed" })
    }

    val parts = output.split('\t', limit = 4)
    val login = parts.getOrNull(0).orEmpty().trim()
    if (login.isBlank()) error("GitHub CLI did not return an account login")

    return GitHubProfile(
        login = login,
        name = parts.getOrNull(1)?.trim()?.ifBlank { null },
        avatarUrl = parts.getOrNull(2)?.trim()?.ifBlank { null },
        profileUrl = parts.getOrNull(3)?.trim()?.ifBlank { null }
            ?: "https://github.com/$login",
    )
}

private fun resolveSidebarGhExecutable(configuredGhPath: String): String {
    val configured = configuredGhPath.trim()
    if (configured.isNotBlank() && canRunSidebarGh(configured)) return configured

    val home = System.getProperty("user.home")
    val pathCandidates = System.getenv("PATH")
        .orEmpty()
        .split(File.pathSeparator)
        .filter { it.isNotBlank() }
        .flatMap { dir -> listOf("$dir/gh", "$dir/gh.exe") }

    val candidates = (
            pathCandidates + listOf(
                "gh",
                "/opt/homebrew/bin/gh",
                "/opt/homebrew/opt/gh/bin/gh",
                "/usr/local/bin/gh",
                "/usr/local/opt/gh/bin/gh",
                "/usr/bin/gh",
                "/home/linuxbrew/.linuxbrew/bin/gh",
                "/home/linuxbrew/.linuxbrew/opt/gh/bin/gh",
                "/snap/bin/gh",
                "/var/lib/snapd/snap/bin/gh",
                "$home/.local/bin/gh",
                "$home/bin/gh",
            )
            ).distinct()

    return candidates.firstOrNull(::canRunSidebarGh) ?: configured.ifBlank { "gh" }
}

private fun canRunSidebarGh(candidate: String): Boolean = runCatching {
    val process = ProcessBuilder(candidate, "--version")
        .redirectErrorStream(true)
        .start()
    process.waitFor(5, TimeUnit.SECONDS) && process.exitValue() == 0
}.getOrDefault(false)

private fun loadAvatarBitmap(url: String): ImageBitmap {
    val bytes = URL(url).openStream().use { it.readBytes() }
    return SkiaImage.makeFromEncoded(bytes).toComposeImageBitmap()
}

private fun avatarFallback(login: String): String = login
    .trim()
    .take(2)
    .uppercase()
    .ifBlank { "GH" }

