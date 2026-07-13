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
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.RateReview
import androidx.compose.material.icons.rounded.Settings
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.skia.Image as SkiaImage
import java.net.URL
import eu.revq.keyboard.FocusRegion
import eu.revq.keyboard.KeyboardMode

private val SidebarSelectedBackground = Color(0xFF1D2227)
private val SidebarOliveSoft = Color(0xFF2C3323)
private val SidebarRoseSoft = Color(0xFF3B2427)
private val SidebarNeutralBadge = Color(0xFF2A2F36)
private val SidebarDanger = Color(0xFFE06A6A)

@Composable
fun SidebarPanel(
    state: AppState,
    onOpenRepositoryScope: () -> Unit,
) {
    var githubProfile by remember { mutableStateOf<GitHubProfile?>(null) }
    var profileLoading by remember { mutableStateOf(true) }
    var profileError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(state.ghPathText, state.ghTestResult) {
        profileLoading = true
        profileError = null

        val result = withContext(Dispatchers.IO) {
            runCatching { state.loadGitHubProfile() }
        }

        githubProfile = result.getOrNull()
        profileError = result.exceptionOrNull()?.message
        profileLoading = false
    }

    val currentScope = state.currentQueueScopeFilter()
    val scopeLabel = when (currentScope) {
        QueueScopeFilter.All -> "All repositories"
        is QueueScopeFilter.Organization -> currentScope.owner
        is QueueScopeFilter.Repository -> currentScope.nameWithOwner
    }
    val scopeIsActive = currentScope != QueueScopeFilter.All

    val reviewQueue = state.reviewQueue()
    val handledQueue = state.handledPullRequests()
    val myPullRequests = state.activePullRequests()
        .filter { it.source == PullRequestSource.Mine }
    val pinnedPullRequests = state.pinnedPullRequests()

    val scopedReviewCount = reviewQueue.count { it.matchesSidebarScope(currentScope) }
    val scopedHandledCount = handledQueue.count { it.matchesSidebarScope(currentScope) }
    val scopedMyPullRequests = myPullRequests.filter { it.matchesSidebarScope(currentScope) }
    val scopedPinnedCount = pinnedPullRequests.count { it.matchesSidebarScope(currentScope) }
    val scopedMyActionCount = scopedMyPullRequests.count {
        attentionKind(it) == AttentionKind.Action ||
                attentionKind(it) == AttentionKind.Blocked
    }

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
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                SidebarHeader()
            }

            item {
                RepositoryScopeButton(
                    scopeLabel = scopeLabel,
                    scopeIsActive = scopeIsActive,
                    onOpen = onOpenRepositoryScope,
                    onClear = state::clearQueueScopeFilter,
                )
            }

            item {
                SidebarGroup(title = "Review") {
                    SidebarItem(
                        state = state,
                        view = View.NeedsReview,
                        count = reviewQueue.size,
                        scopedCount = scopedReviewCount.takeIf { scopeIsActive },
                        icon = Icons.Rounded.RateReview,
                        badgeKind = SidebarBadgeKind.Review,
                        alwaysShowCount = true,
                        showQueueClearState = !scopeIsActive && reviewQueue.isEmpty(),
                        showUnseenDot = state.needsReviewHasUnseenChanges,
                    )

                    SidebarItem(
                        state = state,
                        view = View.Handled,
                        label = "Handled",
                        count = handledQueue.size,
                        scopedCount = scopedHandledCount.takeIf { scopeIsActive },
                        icon = Icons.Rounded.DoneAll,
                    )
                }
            }

            item {
                SidebarGroup(title = "Workspace") {
                    SidebarItem(
                        state = state,
                        view = View.Mine,
                        count = myPullRequests.size,
                        scopedCount = scopedMyPullRequests.size.takeIf { scopeIsActive },
                        showAttentionDot = scopedMyActionCount > 0,
                        icon = Icons.Rounded.Person,
                        alwaysShowCount = true,
                    )

                    SidebarItem(
                        state = state,
                        view = View.Pinned,
                        count = pinnedPullRequests.size,
                        scopedCount = scopedPinnedCount.takeIf { scopeIsActive },
                        icon = Icons.Rounded.PushPin,
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
    val headerHeight = 60.dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(headerHeight),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AppBrandMark(modifier = Modifier.size(headerHeight))

        Column(
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Text(
                text = "RevQ",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
            )
            Text(
                text = "Review companion",
                color = TextMuted,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
            )
        }
    }
}

private fun PullRequest.matchesSidebarScope(scope: QueueScopeFilter): Boolean = when (scope) {
    QueueScopeFilter.All -> true
    is QueueScopeFilter.Organization -> repository.owner == scope.owner
    is QueueScopeFilter.Repository -> repository.toString() == scope.nameWithOwner
}

@Composable
private fun RepositoryScopeButton(
    scopeLabel: String,
    scopeIsActive: Boolean,
    onOpen: () -> Unit,
    onClear: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(if (scopeIsActive) PanelBg else Color.Transparent)
            .border(1.dp, Border, RoundedCornerShape(9.dp))
            .padding(start = 2.dp, end = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(7.dp))
                .clickable(onClick = onOpen)
                .padding(horizontal = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Rounded.FolderOpen,
                contentDescription = null,
                tint = if (scopeIsActive) Olive else TextMuted,
                modifier = Modifier.size(17.dp),
            )
            Spacer(Modifier.width(9.dp))
            Text(
                text = scopeLabel,
                color = TextPrimary,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = Icons.Rounded.ExpandMore,
                contentDescription = "Choose repository scope",
                tint = TextMuted,
                modifier = Modifier.size(18.dp),
            )
        }

        if (scopeIsActive) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onClear),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Clear repository scope",
                    tint = TextMuted,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

@Composable
private fun SidebarGroup(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
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
    Neutral,
}

@Composable
private fun SidebarItem(
    state: AppState,
    view: View,
    count: Int?,
    icon: ImageVector,
    label: String = view.label,
    scopedCount: Int? = null,
    showAttentionDot: Boolean = false,
    showUnseenDot: Boolean = false,
    badgeKind: SidebarBadgeKind = SidebarBadgeKind.Neutral,
    alwaysShowCount: Boolean = false,
    showQueueClearState: Boolean = false,
) {
    val selected = state.view == view
    val keyboardCursor =
        state.keyboardMode == KeyboardMode.Normal &&
                state.keyboardFocusRegion == FocusRegion.Sidebar &&
                state.sidebarKeyboardView == view
    val itemShape = RoundedCornerShape(10.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(1f)
            .clip(itemShape)
            .background(
                when {
                    selected -> SidebarSelectedBackground
                    keyboardCursor -> PanelElevated
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
            tint = when {
                selected -> Olive
                keyboardCursor -> TextPrimary
                else -> TextMuted
            },
            modifier = Modifier.size(19.dp),
        )

        Spacer(Modifier.width(9.dp))

        Text(
            text = label,
            color = if (selected || keyboardCursor) TextPrimary else TextMuted,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        if (showUnseenDot) {
            SidebarSignalDot(color = Olive, contentDescription = "New review queue activity")
            Spacer(Modifier.width(7.dp))
        }

        if (showAttentionDot) {
            SidebarSignalDot(color = Amber, contentDescription = "Pull requests need attention")
            Spacer(Modifier.width(7.dp))
        }

        when {
            showQueueClearState -> SidebarQueueClearMark()
            scopedCount != null && count != null -> SidebarScopeRatio(
                scopedCount = scopedCount,
                totalCount = count,
            )
            else -> {
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
    }
}

@Composable
private fun SidebarSignalDot(
    color: Color,
    contentDescription: String,
) {
    Box(
        modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(color)
            .semantics { this.contentDescription = contentDescription },
    )
}

@Composable
private fun SidebarScopeRatio(
    scopedCount: Int,
    totalCount: Int,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Text(
            text = scopedCount.toString(),
            color = if (scopedCount > 0) TextPrimary else TextMuted,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "/",
            color = TextMuted.copy(alpha = 0.62f),
            style = MaterialTheme.typography.labelSmall,
        )
        Text(
            text = totalCount.toString(),
            color = TextMuted,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun SidebarQueueClearMark() {
    Box(
        modifier = Modifier
            .size(22.dp)
            .clip(CircleShape)
            .background(ReadyGreen.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Rounded.Check,
            contentDescription = "Review queue clear",
            tint = ReadyGreen,
            modifier = Modifier.size(15.dp),
        )
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
        SidebarBadgeKind.Neutral -> SidebarNeutralBadge
    }

    val foreground = when (kind) {
        SidebarBadgeKind.Review -> Olive
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

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
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

                if (profileError != null || isLoading || profile == null) {
                    Text(
                        text = when {
                            profileError != null -> "Needs attention"
                            isLoading -> "Checking connection…"
                            else -> "Not connected"
                        },
                        color = if (connectionNeedsAttention) SidebarDanger else TextMuted,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
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
            .width(284.dp)
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

            AccountMenuAction(
                label = "Settings",
                icon = Icons.Rounded.Settings,
                onClick = {
                    onDismissRequest()
                    state.selectView(View.Settings)
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

            if (profileError != null) {
                AccountMenuAttentionCard(message = profileError)
                AccountMenuAction(
                    label = if (state.isTestingGh) "Testing connection…" else "Test GitHub connection",
                    icon = Icons.Rounded.CheckCircle,
                    enabled = !state.isTestingGh,
                    onClick = {
                        onDismissRequest()
                        state.testGithubCli()
                    },
                )
            }

            AccountMenuAction(
                label = "About RevQ",
                icon = Icons.Rounded.Info,
                onClick = {
                    onDismissRequest()
                    state.showAboutDialog = true
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

private fun loadAvatarBitmap(url: String): ImageBitmap {
    val bytes = URL(url).openStream().use { it.readBytes() }
    return SkiaImage.makeFromEncoded(bytes).toComposeImageBitmap()
}

private fun avatarFallback(login: String): String = login
    .trim()
    .take(2)
    .uppercase()
    .ifBlank { "GH" }
