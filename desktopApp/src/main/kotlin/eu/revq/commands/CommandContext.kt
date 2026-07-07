package eu.revq.commands

import eu.revq.AppState
import eu.revq.PullRequest
import eu.revq.View
import eu.revq.keyboard.FocusRegion

data class CommandContext(
    val view: View,
    val focusRegion: FocusRegion,
    val selectedPullRequest: PullRequest?,
    val reviewQueueSize: Int,
    val reviewSessionActive: Boolean,
    val isRefreshing: Boolean,
    val isDiscovering: Boolean,
    val isTestingGh: Boolean,
    val canUndoReview: Boolean,
) {
    val hasSelectedPullRequest: Boolean
        get() = selectedPullRequest != null

    companion object {
        fun from(state: AppState): CommandContext = CommandContext(
            view = state.view,
            focusRegion = state.keyboardFocusRegion,
            selectedPullRequest = state.selectedPullRequest,
            reviewQueueSize = state.reviewQueue().size,
            reviewSessionActive = state.reviewSessionActive,
            isRefreshing = state.isRefreshing,
            isDiscovering = state.isDiscovering,
            isTestingGh = state.isTestingGh,
            canUndoReview = state.lastUndoReview != null,
        )
    }
}
