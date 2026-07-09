package eu.revq

import java.time.Duration
import java.time.Instant

object ReviewQueue {
    fun visible(
        pullRequests: List<PullRequest>,
        view: View,
        searchQuery: String,
        scope: QueueScopeFilter,
        handledReviewRecords: Map<String, String>,
        pinnedPullRequestKeys: Set<String>,
        mutedRepositories: Set<String>,
        sortMode: String,
        staleThresholdDays: Long,
    ): List<PullRequest> {
        val base = when (view) {
            View.Today -> todayPullRequests(
                pullRequests = pullRequests,
                handledReviewRecords = handledReviewRecords,
                mutedRepositories = mutedRepositories,
            )
            View.NeedsReview -> reviewQueue(
                pullRequests = pullRequests,
                handledReviewRecords = handledReviewRecords,
                mutedRepositories = mutedRepositories,
            )
            View.Pinned -> activePullRequests(pullRequests, mutedRepositories)
                .filter { it.key in pinnedPullRequestKeys }
            View.Mine -> activePullRequests(pullRequests, mutedRepositories)
                .filter { it.source == PullRequestSource.Mine }
            View.Blocked -> activePullRequests(pullRequests, mutedRepositories)
                .filter { PullRequestAttention.describe(it).kind == AttentionKind.Blocked }
            View.Ready -> activePullRequests(pullRequests, mutedRepositories)
                .filter { PullRequestAttention.describe(it).kind == AttentionKind.Ready }
            View.Handled -> activePullRequests(pullRequests, mutedRepositories)
                .filter { it.source == PullRequestSource.ReviewRequest }
                .filter { isHandledCurrent(it, handledReviewRecords) }
            View.Settings -> emptyList()
        }

        return QueueContext.visible(
            pullRequests = base,
            scope = scope,
            searchQuery = searchQuery,
        ).let {
            sort(
                view = view,
                pullRequests = it,
                sortMode = sortMode,
                staleThresholdDays = staleThresholdDays,
                pinnedPullRequestKeys = pinnedPullRequestKeys,
            )
        }
    }

    fun reviewQueue(
        pullRequests: List<PullRequest>,
        handledReviewRecords: Map<String, String>,
        mutedRepositories: Set<String>,
    ): List<PullRequest> = activePullRequests(pullRequests, mutedRepositories)
        .filter { it.source == PullRequestSource.ReviewRequest }
        .filterNot { isHandledCurrent(it, handledReviewRecords) }

    fun activePullRequests(
        pullRequests: List<PullRequest>,
        mutedRepositories: Set<String>,
    ): List<PullRequest> =
        pullRequests.filterNot { it.repository.toString() in mutedRepositories }

    fun isHandledCurrent(
        pullRequest: PullRequest,
        handledReviewRecords: Map<String, String>,
    ): Boolean = handledReviewRecords[pullRequest.key] == pullRequest.updatedMarker

    private fun todayPullRequests(
        pullRequests: List<PullRequest>,
        handledReviewRecords: Map<String, String>,
        mutedRepositories: Set<String>,
    ): List<PullRequest> {
        val active = activePullRequests(pullRequests, mutedRepositories)
        val needsReview = reviewQueue(pullRequests, handledReviewRecords, mutedRepositories)
        val myAction = active.filter {
            it.source == PullRequestSource.Mine &&
                PullRequestAttention.describe(it).needsAction
        }
        val ready = active.filter {
            it.source == PullRequestSource.Mine &&
                PullRequestAttention.describe(it).ownStatus == OwnPullRequestStatus.ApprovedAndReady
        }
        return dedupePullRequests(needsReview + myAction + ready)
    }

    private fun sort(
        view: View,
        pullRequests: List<PullRequest>,
        sortMode: String,
        staleThresholdDays: Long,
        pinnedPullRequestKeys: Set<String>,
    ): List<PullRequest> {
        val urgencyOrder: (PullRequest) -> Int = { pr ->
            val attention = PullRequestAttention.describe(pr)
            when {
                view == View.Pinned && pr.key in pinnedPullRequestKeys -> 0
                pr.source == PullRequestSource.ReviewRequest && isOlderThan(pr.updatedAt, staleThresholdDays) -> 10
                pr.source == PullRequestSource.ReviewRequest -> 20
                pr.source == PullRequestSource.Mine -> when (attention.ownStatus ?: OwnPullRequestStatus.NoActionNeeded) {
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
            "Updated newest" -> pullRequests.sortedByDescending { instantOrNull(it.updatedAt) ?: Instant.EPOCH }
            "Updated oldest" -> pullRequests.sortedBy { instantOrNull(it.updatedAt) ?: Instant.EPOCH }
            "Repository" -> pullRequests.sortedWith(compareBy<PullRequest> { it.repository.toString() }.thenBy { it.number })
            "Comments" -> pullRequests.sortedByDescending { it.comments }
            else -> pullRequests.sortedWith(
                compareBy<PullRequest> { urgencyOrder(it) }
                    .thenBy { instantOrNull(it.updatedAt) ?: Instant.EPOCH }
                    .thenBy { it.repository.toString() }
                    .thenBy { it.number },
            )
        }
    }

    private fun isOlderThan(value: String?, days: Long): Boolean {
        val instant = instantOrNull(value) ?: return false
        return instant.isBefore(Instant.now().minus(Duration.ofDays(days)))
    }
}
