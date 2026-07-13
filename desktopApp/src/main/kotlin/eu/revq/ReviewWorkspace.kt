package eu.revq

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import eu.revq.keyboard.FocusRegion

data class ReviewWorkspaceSnapshot(
    val view: View,
    val sidebarView: View,
    val focusRegion: FocusRegion,
    val pullRequests: List<PullRequest> = emptyList(),
    val selectedPullRequest: PullRequest? = null,
    val configuration: ReviewWorkspaceQueueConfiguration = ReviewWorkspaceQueueConfiguration(),
    val visiblePullRequests: List<PullRequest> = emptyList(),
    val expandedPullRequestKey: String? = null,
)

data class ReviewWorkspaceQueueConfiguration(
    val searchQuery: String = "",
    val scope: QueueScopeFilter = QueueScopeFilter.All,
    val handledReviewRecords: Map<String, String> = emptyMap(),
    val pinnedPullRequestKeys: Set<String> = emptySet(),
    val mutedRepositories: Set<String> = emptySet(),
    val sortMode: String = "Urgency",
    val staleThresholdDaysText: String = "2",
)

sealed interface WorkspaceAction {
    data class BrowseSidebar(val delta: Int) : WorkspaceAction
    data class SelectView(val view: View) : WorkspaceAction
    data class HighlightSidebar(val view: View) : WorkspaceAction
    data class SetFocus(val region: FocusRegion) : WorkspaceAction
    data class MoveKeyboardFocus(val direction: Int) : WorkspaceAction
    data class ReplacePullRequests(val pullRequests: List<PullRequest>) : WorkspaceAction
    data class SelectPullRequest(val pullRequest: PullRequest?) : WorkspaceAction
    data class ConfigureQueue(val configuration: ReviewWorkspaceQueueConfiguration) : WorkspaceAction
    data class MoveSelection(val delta: Int) : WorkspaceAction
    data class MoveToBoundary(val first: Boolean) : WorkspaceAction
    data object ToggleSelectedDetails : WorkspaceAction
    data object ActivateFocused : WorkspaceAction
    data object Escape : WorkspaceAction
    data class SetExpandedPullRequest(val key: String?) : WorkspaceAction
}

class ReviewWorkspace(
    initialSnapshot: ReviewWorkspaceSnapshot = ReviewWorkspaceSnapshot(
        view = View.NeedsReview,
        sidebarView = View.NeedsReview,
        focusRegion = FocusRegion.PullRequestList,
    ),
) {
    private var currentSnapshot by mutableStateOf(withVisibleQueue(initialSnapshot))
    private var selectionKeys: Map<View, String> = emptyMap()

    val snapshot: ReviewWorkspaceSnapshot
        get() = currentSnapshot

    fun apply(action: WorkspaceAction) {
        currentSnapshot = when (action) {
            is WorkspaceAction.BrowseSidebar -> browseSidebar(action.delta)
            is WorkspaceAction.SelectView -> selectView(action.view)
            is WorkspaceAction.HighlightSidebar -> currentSnapshot.copy(sidebarView = action.view)
            is WorkspaceAction.SetFocus -> currentSnapshot.copy(focusRegion = action.region)
            is WorkspaceAction.MoveKeyboardFocus -> moveKeyboardFocus(action.direction)
            is WorkspaceAction.ReplacePullRequests -> replacePullRequests(action.pullRequests)
            is WorkspaceAction.SelectPullRequest -> currentSnapshot.copy(selectedPullRequest = action.pullRequest)
            is WorkspaceAction.ConfigureQueue -> configureQueue(action.configuration)
            is WorkspaceAction.MoveSelection -> moveSelection(action.delta)
            is WorkspaceAction.MoveToBoundary -> moveToBoundary(action.first)
            WorkspaceAction.ToggleSelectedDetails -> toggleSelectedDetails()
            WorkspaceAction.ActivateFocused -> activateFocused()
            WorkspaceAction.Escape -> escape()
            is WorkspaceAction.SetExpandedPullRequest -> currentSnapshot.copy(expandedPullRequestKey = action.key)
        }
    }

    private fun browseSidebar(delta: Int): ReviewWorkspaceSnapshot {
        if (SidebarViews.isEmpty()) return currentSnapshot

        currentSnapshot.selectedPullRequest?.let { selected ->
            selectionKeys = selectionKeys + (currentSnapshot.view to selected.key)
        }
        val currentIndex = SidebarViews.indexOf(currentSnapshot.sidebarView)
            .takeIf { it >= 0 }
            ?: SidebarViews.indexOf(currentSnapshot.view).coerceAtLeast(0)
        val nextIndex = Math.floorMod(currentIndex + delta, SidebarViews.size)
        val nextView = SidebarViews[nextIndex]
        return withVisibleQueue(currentSnapshot.copy(
            view = nextView,
            sidebarView = nextView,
            focusRegion = FocusRegion.Sidebar,
            selectedPullRequest = selectionFor(nextView),
            expandedPullRequestKey = null,
        ))
    }

    private fun selectView(view: View): ReviewWorkspaceSnapshot {
        currentSnapshot.selectedPullRequest?.let { selected ->
            selectionKeys = selectionKeys + (currentSnapshot.view to selected.key)
        }
        return withVisibleQueue(currentSnapshot.copy(
            view = view,
            sidebarView = view.takeIf { it in SidebarViews } ?: currentSnapshot.sidebarView,
            focusRegion = if (view == View.Settings) {
                currentSnapshot.focusRegion
            } else {
                FocusRegion.PullRequestList
            },
            selectedPullRequest = selectionFor(view),
            expandedPullRequestKey = null,
        ))
    }

    private fun replacePullRequests(pullRequests: List<PullRequest>): ReviewWorkspaceSnapshot {
        val items = dedupePullRequests(pullRequests)
        val previousVisible = currentSnapshot.visiblePullRequests
        val previousIndex = currentSnapshot.selectedPullRequest?.let { selected ->
            previousVisible.indexOfFirst { it.key == selected.key && it.source == selected.source }
        } ?: -1
        val visible = visiblePullRequests(currentSnapshot.view, items, currentSnapshot.configuration)
        val selected = currentSnapshot.selectedPullRequest
        return currentSnapshot.copy(
            pullRequests = items,
            visiblePullRequests = visible,
            selectedPullRequest = selected?.let { previous ->
                visible.firstOrNull { it.key == previous.key && it.source == previous.source }
            } ?: visible.getOrNull(previousIndex.coerceAtMost(visible.lastIndex))
                ?: visible.firstOrNull(),
            expandedPullRequestKey = currentSnapshot.expandedPullRequestKey
                ?.takeIf { key -> visible.any { it.key == key } },
        )
    }

    private fun configureQueue(configuration: ReviewWorkspaceQueueConfiguration): ReviewWorkspaceSnapshot {
        val visible = visiblePullRequests(currentSnapshot.view, currentSnapshot.pullRequests, configuration)
        val selected = currentSnapshot.selectedPullRequest?.let { previous ->
            visible.firstOrNull { it.key == previous.key && it.source == previous.source }
        } ?: visible.firstOrNull()
        return currentSnapshot.copy(
            configuration = configuration,
            visiblePullRequests = visible,
            selectedPullRequest = selected,
            expandedPullRequestKey = currentSnapshot.expandedPullRequestKey
                ?.takeIf { key -> visible.any { it.key == key } },
        )
    }

    private fun moveSelection(delta: Int): ReviewWorkspaceSnapshot {
        val items = currentSnapshot.visiblePullRequests
        if (items.isEmpty()) return currentSnapshot
        val index = currentSnapshot.selectedPullRequest?.let { selected ->
            items.indexOfFirst { it.key == selected.key && it.source == selected.source }
        } ?: -1
        val nextIndex = if (index < 0) {
            if (delta < 0) items.lastIndex else 0
        } else {
            (index + delta).coerceIn(0, items.lastIndex)
        }
        return currentSnapshot.copy(
            selectedPullRequest = items[nextIndex],
            expandedPullRequestKey = null,
        )
    }

    private fun moveKeyboardFocus(direction: Int): ReviewWorkspaceSnapshot = when (currentSnapshot.focusRegion) {
        FocusRegion.Sidebar -> currentSnapshot.copy(
            focusRegion = if (direction > 0) FocusRegion.PullRequestList else FocusRegion.Sidebar,
        )
        FocusRegion.PullRequestList -> currentSnapshot.copy(
            focusRegion = if (direction < 0) FocusRegion.Sidebar else FocusRegion.PullRequestList,
            sidebarView = currentSnapshot.view.takeIf { it in SidebarViews } ?: currentSnapshot.sidebarView,
        )
    }

    private fun moveToBoundary(first: Boolean): ReviewWorkspaceSnapshot = when (currentSnapshot.focusRegion) {
        FocusRegion.Sidebar -> currentSnapshot.copy(
            sidebarView = if (first) SidebarViews.first() else SidebarViews.last(),
        )
        FocusRegion.PullRequestList -> currentSnapshot.copy(
            selectedPullRequest = currentSnapshot.visiblePullRequests
                .takeIf { it.isNotEmpty() }
                ?.let { if (first) it.first() else it.last() },
            expandedPullRequestKey = null,
        )
    }

    private fun toggleSelectedDetails(): ReviewWorkspaceSnapshot {
        val selected = currentSnapshot.selectedPullRequest ?: return currentSnapshot
        return currentSnapshot.copy(
            expandedPullRequestKey = if (currentSnapshot.expandedPullRequestKey == selected.key) null else selected.key,
        )
    }

    private fun activateFocused(): ReviewWorkspaceSnapshot = when (currentSnapshot.focusRegion) {
        FocusRegion.Sidebar -> selectView(currentSnapshot.sidebarView)
        FocusRegion.PullRequestList -> if (currentSnapshot.selectedPullRequest == null) {
            currentSnapshot.copy(selectedPullRequest = currentSnapshot.visiblePullRequests.firstOrNull())
        } else {
            toggleSelectedDetails()
        }
    }

    private fun escape(): ReviewWorkspaceSnapshot = when (currentSnapshot.focusRegion) {
        FocusRegion.Sidebar -> currentSnapshot.copy(focusRegion = FocusRegion.PullRequestList)
        FocusRegion.PullRequestList -> when {
            currentSnapshot.expandedPullRequestKey != null -> currentSnapshot.copy(expandedPullRequestKey = null)
            currentSnapshot.selectedPullRequest != null -> currentSnapshot.copy(selectedPullRequest = null)
            else -> currentSnapshot
        }
    }

    private fun selectionFor(view: View): PullRequest? {
        if (view == View.Settings) return null
        return QueueContext.restoreSelection(
            visiblePullRequests = visiblePullRequests(view, currentSnapshot.pullRequests, currentSnapshot.configuration),
            rememberedKey = selectionKeys[view],
        )
    }

    private fun visiblePullRequests(
        view: View,
        pullRequests: List<PullRequest>,
        configuration: ReviewWorkspaceQueueConfiguration,
    ): List<PullRequest> = ReviewQueue.visible(
        pullRequests = pullRequests,
        view = view,
        searchQuery = configuration.searchQuery,
        scope = configuration.scope,
        handledReviewRecords = configuration.handledReviewRecords,
        pinnedPullRequestKeys = configuration.pinnedPullRequestKeys,
        mutedRepositories = configuration.mutedRepositories,
        sortMode = configuration.sortMode,
        staleThresholdDays = configuration.staleThresholdDaysText.toLongOrNull()?.coerceIn(1L, 30L) ?: 2L,
    )

    private fun withVisibleQueue(snapshot: ReviewWorkspaceSnapshot): ReviewWorkspaceSnapshot = snapshot.copy(
        visiblePullRequests = visiblePullRequests(snapshot.view, snapshot.pullRequests, snapshot.configuration),
    )

    companion object {
        val SidebarViews: List<View> = listOf(
            View.NeedsReview,
            View.Handled,
            View.Mine,
            View.Pinned,
        )
    }
}
