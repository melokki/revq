package eu.revq

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AppStateUpdateTest {
    @Test
    fun `update discovery leaves queue selection and focus unchanged`() = runBlocking {
        val service = UpdateService(
            installedVersion = AppVersion.parse("0.1.0"),
            releaseSource = ReleaseSource { listOf(ReleaseCandidate("v0.2.0", "RevQ 0.2.0")) },
            preferences = InMemoryUpdatePreferencesStore(),
        )
        val state = AppState(updateService = service)
        val selected = samplePullRequest()
        state.pullRequests = listOf(selected)
        state.selectedPullRequest = selected

        state.checkForUpdatesNow()

        assertTrue(state.updateState is UpdateState.Available)
        assertEquals(selected, state.selectedPullRequest)
        assertEquals(eu.revq.keyboard.FocusRegion.PullRequestList, state.keyboardFocusRegion)
    }

    private fun samplePullRequest() = PullRequest(
        repository = RepositoryId("acme", "api"),
        number = 42,
        title = "Keep queue stable",
        url = "https://github.com/acme/api/pull/42",
        updatedAt = null,
        comments = 0,
        source = PullRequestSource.ReviewRequest,
    )
}
