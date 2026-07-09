package eu.revq

enum class AttentionReason(val label: String) {
    DirectReviewRequest("Review requested"),
    TeamReviewRequest("Team review requested"),
    Assignment("Assigned"),
    ChangesRequested("Changes requested"),
    FailingCi("CI failing"),
    Other("Needs attention"),
}

fun primaryAttentionReason(pullRequest: PullRequest): AttentionReason =
    PullRequestAttention.describe(pullRequest).primaryReason

fun selectedRowContext(pullRequest: PullRequest): List<String> = buildList {
    when {
        pullRequest.checksFailing > 0 -> add("${pullRequest.checksFailing} CI failing")
        pullRequest.checksPending > 0 -> add("${pullRequest.checksPending} CI pending")
        pullRequest.checksTotal > 0 -> add("CI passing")
    }
    pullRequest.approvingReviewers
        .distinct()
        .size
        .takeIf { it > 0 }
        ?.let { count -> add(if (count == 1) "1 approval" else "$count approvals") }
}

fun queueRowMetadata(
    pullRequest: PullRequest,
    selected: Boolean,
): String = buildList {
    add("#${pullRequest.number}")
    add(primaryAttentionReason(pullRequest).label)
    if (selected) addAll(selectedRowContext(pullRequest))
}.joinToString(" · ")

data class QueuePosition(
    val current: Int,
    val total: Int,
)

sealed interface QueueScopeFilter {
    data object All : QueueScopeFilter
    data class Organization(val owner: String) : QueueScopeFilter
    data class Repository(val nameWithOwner: String) : QueueScopeFilter
}

fun selectedQueuePosition(state: AppState): QueuePosition? {
    val selected = state.selectedPullRequest ?: return null
    val visible = state.visiblePullRequests()
    val index = visible.indexOfFirst {
        it.key == selected.key && it.source == selected.source
    }
    return index.takeIf { it >= 0 }?.let {
        QueuePosition(current = it + 1, total = visible.size)
    }
}
