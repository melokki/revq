package eu.revq.commands

import eu.revq.AppState
import eu.revq.PullRequest
import eu.revq.View
import eu.revq.keyboard.FocusRegion
import eu.revq.UpdateState

data class CommandContext(
    val view: View,
    val focusRegion: FocusRegion,
    val selectedPullRequest: PullRequest?,
    val reviewQueueSize: Int,
    val canMergeSelectedPullRequest: Boolean,
    val isRefreshing: Boolean,
    val isDiscovering: Boolean,
    val isTestingGh: Boolean,
    val updateState: UpdateState,
) {
    val hasSelectedPullRequest: Boolean
        get() = selectedPullRequest != null

    companion object {
        fun from(state: AppState): CommandContext = CommandContext(
            view = state.view,
            focusRegion = state.keyboardFocusRegion,
            selectedPullRequest = state.selectedPullRequest,
            reviewQueueSize = state.reviewQueue().size,
            canMergeSelectedPullRequest = state.canMergePullRequest(state.selectedPullRequest),
            isRefreshing = state.isRefreshing,
            isDiscovering = state.isDiscovering,
            isTestingGh = state.isTestingGh,
            updateState = state.updateState,
        )
    }
}
