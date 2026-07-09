package eu.revq

object QueueContext {
    fun visible(
        pullRequests: List<PullRequest>,
        scope: QueueScopeFilter,
        searchQuery: String,
    ): List<PullRequest> {
        val query = searchQuery.trim().lowercase()
        return pullRequests
            .filter { pullRequest -> scope.includes(pullRequest) }
            .filter { pullRequest ->
                query.isEmpty() ||
                    pullRequest.title.lowercase().contains(query) ||
                    pullRequest.repository.toString().lowercase().contains(query) ||
                    pullRequest.number.toString().contains(query)
            }
    }

    fun restoreSelection(
        visiblePullRequests: List<PullRequest>,
        rememberedKey: String?,
    ): PullRequest? =
        visiblePullRequests.firstOrNull { it.key == rememberedKey }
            ?: visiblePullRequests.firstOrNull()

    private fun QueueScopeFilter.includes(pullRequest: PullRequest): Boolean = when (this) {
        QueueScopeFilter.All -> true
        is QueueScopeFilter.Organization -> pullRequest.repository.owner == owner
        is QueueScopeFilter.Repository -> pullRequest.repository.toString() == nameWithOwner
    }
}
