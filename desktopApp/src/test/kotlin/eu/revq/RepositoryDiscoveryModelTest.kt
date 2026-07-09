package eu.revq

import kotlin.test.Test
import kotlin.test.assertEquals

class RepositoryDiscoveryModelTest {
    @Test
    fun allOrganizationScopeIncludesNewlyDiscoveredRepositories() {
        val selection = RepositoryScopeSelection(
            organizationScopes = mapOf(
                "acme" to OrganizationScope.All,
            ),
        )
        val repositories = listOf(
            DiscoveredRepository("acme/api", owner = "acme"),
            DiscoveredRepository("acme/new-service", owner = "acme"),
            DiscoveredRepository("other/ignored", owner = "other"),
        )

        assertEquals(
            setOf("acme/api", "acme/new-service"),
            selection.activeRepositories(repositories),
        )
    }

    @Test
    fun activeReviewWorkProducesVisibleRepositorySuggestions() {
        val review = pullRequest(1, PullRequestSource.ReviewRequest)
        val mine = pullRequest(2, PullRequestSource.Mine)

        assertEquals(
            listOf(
                RepositorySuggestion(
                    repository = "acme/api",
                    reasons = setOf(
                        RepositorySuggestionReason.ReviewRequested,
                        RepositorySuggestionReason.AuthoredByYou,
                    ),
                    relevantPullRequestCount = 2,
                ),
            ),
            suggestRepositories(listOf(review, mine)),
        )
    }

    private fun pullRequest(
        number: Int,
        source: PullRequestSource,
    ) = PullRequest(
        repository = RepositoryId("acme", "api"),
        number = number,
        title = "Discovery $number",
        url = "https://github.com/acme/api/pull/$number",
        updatedAt = "2026-07-09T10:00:00Z",
        source = source,
    )
}
