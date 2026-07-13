package eu.revq

import eu.revq.keyboard.FocusRegion
import kotlin.test.Test
import kotlin.test.assertEquals

class ReviewWorkspaceTest {
    @Test
    fun browsingSidebarChangesQueueWhileKeepingSidebarFocus() {
        val workspace = ReviewWorkspace()

        listOf(View.Handled, View.Mine, View.Pinned, View.NeedsReview).forEach { expectedView ->
            workspace.apply(WorkspaceAction.BrowseSidebar(delta = 1))

            assertEquals(expectedView, workspace.snapshot.view)
            assertEquals(expectedView, workspace.snapshot.sidebarView)
            assertEquals(FocusRegion.Sidebar, workspace.snapshot.focusRegion)
        }
    }

    @Test
    fun eachQueueRestoresItsOwnSelection() {
        val reviewOne = pullRequest(1)
        val reviewTwo = pullRequest(2)
        val mine = pullRequest(3, PullRequestSource.Mine)
        val workspace = ReviewWorkspace()

        workspace.apply(WorkspaceAction.ReplacePullRequests(listOf(reviewOne, reviewTwo, mine)))
        workspace.apply(WorkspaceAction.SelectPullRequest(reviewTwo))
        workspace.apply(WorkspaceAction.SelectView(View.Mine))
        workspace.apply(WorkspaceAction.SelectView(View.NeedsReview))

        assertEquals(reviewTwo, workspace.snapshot.selectedPullRequest)
    }

    @Test
    fun queueSelectionUsesTheConfiguredPinnedAndFilterContext() {
        val first = pullRequest(1)
        val pinned = pullRequest(2)
        val workspace = ReviewWorkspace()

        workspace.apply(WorkspaceAction.ReplacePullRequests(listOf(first, pinned)))
        workspace.apply(
            WorkspaceAction.ConfigureQueue(
                workspace.snapshot.configuration.copy(
                    pinnedPullRequestKeys = setOf(pinned.key),
                    searchQuery = "Workspace 2",
                ),
            ),
        )
        workspace.apply(WorkspaceAction.SelectView(View.Pinned))

        assertEquals(listOf(pinned), workspace.snapshot.visiblePullRequests)
        assertEquals(pinned, workspace.snapshot.selectedPullRequest)
    }

    @Test
    fun keyboardIntentMovesSelectionAndClosesDetailsInsideWorkspace() {
        val first = pullRequest(1)
        val second = pullRequest(2)
        val workspace = ReviewWorkspace()

        workspace.apply(WorkspaceAction.ReplacePullRequests(listOf(first, second)))
        workspace.apply(WorkspaceAction.ToggleSelectedDetails)
        workspace.apply(WorkspaceAction.MoveSelection(delta = 1))

        assertEquals(second, workspace.snapshot.selectedPullRequest)
        assertEquals(null, workspace.snapshot.expandedPullRequestKey)
    }

    private fun pullRequest(
        number: Int,
        source: PullRequestSource = PullRequestSource.ReviewRequest,
    ): PullRequest = PullRequest(
        repository = RepositoryId("acme", "mobile"),
        number = number,
        title = "Workspace $number",
        url = "https://github.com/acme/mobile/pull/$number",
        updatedAt = "2026-07-07T10:00:00Z",
        source = source,
    )
}
