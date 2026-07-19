package eu.revq

import java.time.Instant

data class ReviewAssignmentAlert(
    val pullRequests: List<PullRequest>,
    val detectedAt: Instant,
) {
    val count: Int get() = pullRequests.size
}


enum class ReviewAssignmentPresentation {
    Hidden,
    Banner,
    Window,
}

fun reviewAssignmentPresentation(
    hasAlert: Boolean,
    reminderVisible: Boolean,
    mainWindowVisible: Boolean,
    mainWindowActive: Boolean,
    view: View,
): ReviewAssignmentPresentation = when {
    !hasAlert || reminderVisible -> ReviewAssignmentPresentation.Hidden
    mainWindowVisible && mainWindowActive && view != View.Settings -> ReviewAssignmentPresentation.Banner
    else -> ReviewAssignmentPresentation.Window
}

object ReviewAssignmentNotifications {
    fun newlyAssignedForRefresh(
        baselineEstablished: Boolean,
        previous: List<PullRequest>,
        refreshed: List<PullRequest>,
        mutedRepositories: Set<String> = emptySet(),
    ): List<PullRequest> = if (baselineEstablished) {
        newlyAssigned(previous, refreshed, mutedRepositories)
    } else {
        emptyList()
    }

    fun newlyAssigned(
        previous: List<PullRequest>,
        refreshed: List<PullRequest>,
        mutedRepositories: Set<String> = emptySet(),
    ): List<PullRequest> {
        val previousReviewKeys = previous
            .asSequence()
            .filter { it.source == PullRequestSource.ReviewRequest }
            .map { it.key }
            .toSet()

        return refreshed
            .asSequence()
            .filter { it.source == PullRequestSource.ReviewRequest }
            .filterNot { it.repository.toString() in mutedRepositories }
            .filter { it.key !in previousReviewKeys }
            .distinctBy { it.key }
            .sortedWith(
                compareByDescending<PullRequest> { instantOrNull(it.updatedAt) ?: Instant.EPOCH }
                    .thenBy { it.repository.toString() }
                    .thenBy { it.number },
            )
            .toList()
    }

    fun merge(
        current: ReviewAssignmentAlert?,
        incoming: List<PullRequest>,
        detectedAt: Instant,
    ): ReviewAssignmentAlert? {
        if (incoming.isEmpty()) return current

        val merged = (current?.pullRequests.orEmpty() + incoming)
            .distinctBy { it.key }
            .sortedWith(
                compareByDescending<PullRequest> { instantOrNull(it.updatedAt) ?: Instant.EPOCH }
                    .thenBy { it.repository.toString() }
                    .thenBy { it.number },
            )

        return ReviewAssignmentAlert(
            pullRequests = merged,
            detectedAt = current?.detectedAt ?: detectedAt,
        )
    }
}
