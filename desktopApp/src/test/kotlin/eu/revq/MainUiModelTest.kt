package eu.revq

import androidx.compose.ui.unit.dp
import eu.revq.keyboard.FocusRegion
import kotlin.test.Test
import kotlin.test.assertEquals

class MainUiModelTest {
    @Test
    fun compactAboutDialogUsesWrappedActionsWithinScreenMargins() {
        assertEquals(
            AboutDialogPresentation(
                width = 448.dp,
                actionLayout = AboutDialogActionLayout.Wrapped,
            ),
            aboutDialogPresentation(availableWidth = 480.dp),
        )
    }

    @Test
    fun wideAboutDialogKeepsActionsInlineAndCapsItsWidth() {
        assertEquals(
            AboutDialogPresentation(
                width = 520.dp,
                actionLayout = AboutDialogActionLayout.Inline,
            ),
            aboutDialogPresentation(availableWidth = 900.dp),
        )
    }

    @Test
    fun personalPullRequestWithRequestedChangesHasChangesRequestedAttentionReason() {
        val pullRequest = pullRequest(
            number = 42,
            source = PullRequestSource.Mine,
            reviewDecision = "CHANGES_REQUESTED",
        )

        assertEquals(
            AttentionReason.ChangesRequested,
            primaryAttentionReason(pullRequest),
        )
    }

    @Test
    fun reviewQueuePullRequestHasDirectReviewAttentionReason() {
        assertEquals(
            AttentionReason.DirectReviewRequest,
            primaryAttentionReason(pullRequest(number = 42)),
        )
    }

    @Test
    fun personalPullRequestWithFailingCiHasFailingCiAttentionReason() {
        assertEquals(
            AttentionReason.FailingCi,
            primaryAttentionReason(
                pullRequest(
                    number = 42,
                    source = PullRequestSource.Mine,
                    checksFailing = 2,
                ),
            ),
        )
    }

    @Test
    fun selectedQueueRowRevealsCiAndApprovalContext() {
        val pullRequest = pullRequest(number = 42).copy(
            checksTotal = 3,
            approvingReviewers = listOf("alice", "bob"),
        )

        assertEquals(
            listOf("CI passing", "2 approvals"),
            selectedRowContext(pullRequest),
        )
    }

    @Test
    fun selectedQueuePositionUsesTheCurrentlyVisibleQueue() {
        val first = pullRequest(1)
        val second = pullRequest(2)
        val state = AppState().apply {
            pullRequests = listOf(first, second)
            selectedPullRequest = second
        }

        assertEquals(
            QueuePosition(current = 2, total = 2),
            selectedQueuePosition(state),
        )
    }

    @Test
    fun pinningPullRequestPublishesNonBlockingActionFeedback() {
        val pullRequest = pullRequest(42)
        val state = AppState().apply {
            selectedPullRequest = pullRequest
        }

        state.togglePin()

        assertEquals("Pinned #42", state.actionFeedback?.message)
    }

    @Test
    fun permanentSidebarContainsOnlyPrimaryProductFlows() {
        assertEquals(
            listOf(
                View.NeedsReview,
                View.Handled,
                View.Mine,
                View.Pinned,
            ),
            SidebarKeyboardViews,
        )
    }

    @Test
    fun primaryQueuesRestoreTheirOwnSelectedPullRequest() {
        val reviewOne = pullRequest(1)
        val reviewTwo = pullRequest(2)
        val mine = pullRequest(3, source = PullRequestSource.Mine)
        val state = AppState().apply {
            pullRequests = listOf(reviewOne, reviewTwo, mine)
            selectedPullRequest = reviewTwo
        }

        state.selectView(View.Mine)
        state.selectView(View.NeedsReview)

        assertEquals(reviewTwo, state.selectedPullRequest)
    }

    @Test
    fun urgencyOrderingUsesStableRepositoryAndNumberTieBreakers() {
        val alpha = pullRequest(2).copy(repository = RepositoryId("acme", "alpha"))
        val beta = pullRequest(1).copy(repository = RepositoryId("acme", "beta"))
        val state = AppState()

        assertEquals(
            listOf(alpha, beta),
            state.sortPullRequests(View.NeedsReview, listOf(beta, alpha)),
        )
    }

    @Test
    fun temporaryOrganizationScopeFiltersOnlyTheCurrentQueue() {
        val acme = pullRequest(1).copy(repository = RepositoryId("acme", "api"))
        val other = pullRequest(2).copy(repository = RepositoryId("other", "web"))
        val state = AppState().apply {
            pullRequests = listOf(acme, other)
        }

        state.setQueueScopeFilter(QueueScopeFilter.Organization("acme"))

        assertEquals(listOf(acme), state.visiblePullRequests())
    }

    @Test
    fun emptyQueueExplainsWhenTemporaryScopeHidesResults() {
        val state = AppState().apply {
            pullRequests = listOf(pullRequest(1))
        }
        state.setQueueScopeFilter(QueueScopeFilter.Repository("other/web"))

        assertEquals("No PRs in this scope.", emptyStateSpec(state).title)
    }

    @Test
    fun emptyQueueExplainsWhenNoRepositoriesAreConfigured() {
        val state = AppState()

        assertEquals(
            "Choose repositories to track.",
            emptyStateSpec(state).title,
        )
    }

    @Test
    fun nativeComposeDensityWinsOverDesktopFallbackSignals() {
        assertEquals(
            1.5f,
            effectiveDesktopDensity(
                composeDensity = 1.5f,
                graphicsScale = 2f,
                toolkitDpi = 192,
            ),
        )
    }

    @Test
    fun desktopFallbackIsUsedWhenComposeIncorrectlyReportsOneX() {
        assertEquals(
            2f,
            effectiveDesktopDensity(
                composeDensity = 1f,
                graphicsScale = 2f,
                toolkitDpi = 192,
            ),
        )
    }

    @Test
    fun cosmicRandrScaleIsConvertedToDesktopDensity() {
        val output = """
            eDP-1 (enabled)
              Make: AU Optronics
              Scale: 200%
              Transform: normal
        """.trimIndent()

        assertEquals(2f, parseCosmicDisplayScale(output))
    }

    @Test
    fun cosmicWaylandScaleIsAppliedBeforeDesktopUiStarts() {
        assertEquals(
            "2.0",
            recommendedJavaUiScale(
                desktop = "COSMIC",
                sessionType = "wayland",
                cosmicScale = 2f,
            ),
        )
    }

    @Test
    fun startupWithoutRepositoriesKeepsNeedsReviewVisible() {
        val state = AppState()

        state.refresh()

        assertEquals(View.NeedsReview, state.view)
    }

    @Test
    fun openingNeedsReviewSelectsTheFirstQueuedPullRequest() {
        val first = pullRequest(1)
        val second = pullRequest(2)
        val state = AppState().apply {
            pullRequests = listOf(first, second)
        }

        state.selectView(View.NeedsReview)

        assertEquals(first, state.selectedPullRequest)
    }

    @Test
    fun activatingSelectedPullRequestTogglesItsInlineDetails() {
        val selected = pullRequest(1)
        val state = AppState().apply {
            pullRequests = listOf(selected)
            selectedPullRequest = selected
        }

        state.toggleSelectedPullRequestDetails()
        assertEquals(selected.key, state.expandedPullRequestKey)

        state.toggleSelectedPullRequestDetails()
        assertEquals(null, state.expandedPullRequestKey)
    }

    @Test
    fun movingSelectionClosesInlineDetails() {
        val first = pullRequest(1)
        val second = pullRequest(2)
        val state = AppState().apply {
            pullRequests = listOf(first, second)
            selectedPullRequest = first
            expandedPullRequestKey = first.key
        }

        moveSelection(state, 1)

        assertEquals(second, state.selectedPullRequest)
        assertEquals(null, state.expandedPullRequestKey)
    }

    @Test
    fun replacingQueueSelectsFirstPullRequestWhenPreviousSelectionDisappears() {
        val removed = pullRequest(1)
        val replacement = pullRequest(2)
        val state = AppState().apply {
            pullRequests = listOf(removed)
            selectedPullRequest = removed
        }

        state.replacePullRequests(listOf(replacement))

        assertEquals(replacement, state.selectedPullRequest)
    }

    @Test
    fun refreshSelectsNearestPullRequestWhenPreviousSelectionDisappears() {
        val first = pullRequest(1)
        val removed = pullRequest(2)
        val third = pullRequest(3)
        val state = AppState().apply {
            pullRequests = listOf(first, removed, third)
            selectedPullRequest = removed
        }

        state.replacePullRequests(listOf(first, third))

        assertEquals(third, state.selectedPullRequest)
    }

    @Test
    fun pullRequestReasonLabelExplainsWhyTheRowAppears() {
        val review = pullRequest(42, source = PullRequestSource.ReviewRequest)
        val mine = pullRequest(
            number = 43,
            source = PullRequestSource.Mine,
            checksFailing = 1,
        )
        val state = AppState().apply {
            pullRequests = listOf(review, mine)
            selectView(View.NeedsReview)
        }

        assertEquals("Up next", pullRequestReasonLabel(state, review, startHere = true))

        state.selectView(View.Mine)
        assertEquals("Checks failing", pullRequestReasonLabel(state, mine))

        state.selectView(View.Pinned)
        state.pinnedPrKeys = setOf(review.key)
        assertEquals("Pinned", pullRequestReasonLabel(state, review))
    }

    @Test
    fun movingPrSelectionDoesNotMoveTheSidebarKeyboardSelector() {
        val first = pullRequest(1)
        val second = pullRequest(2)
        val state = AppState().apply {
            pullRequests = listOf(first, second)
            selectedPullRequest = first
            keyboardFocusRegion = FocusRegion.PullRequestList
            sidebarKeyboardView = View.Mine
        }

        moveSelection(state, 1)

        assertEquals(second, state.selectedPullRequest)
        assertEquals(FocusRegion.PullRequestList, state.keyboardFocusRegion)
        assertEquals(View.Mine, state.sidebarKeyboardView)
    }

    @Test
    fun browsingSidebarImmediatelyDisplaysEachQueueWithoutLeavingSidebarFocus() {
        val state = AppState().apply {
            selectView(View.NeedsReview)
            keyboardFocusRegion = FocusRegion.Sidebar
        }

        listOf(View.Handled, View.Mine, View.Pinned, View.NeedsReview).forEach { expectedView ->
            browseSidebar(state, delta = 1)

            assertEquals(expectedView, state.view)
            assertEquals(expectedView, state.sidebarKeyboardView)
            assertEquals(FocusRegion.Sidebar, state.keyboardFocusRegion)
        }
    }

    @Test
    fun activeFilterChipsExposePaletteAppliedFilters() {
        val state = AppState().apply {
            searchQuery = "acme/mobile"
            groupByRepository = true
        }

        assertEquals(
            listOf(
                WorkspaceFilterChip("Filter", "acme/mobile", clearable = true),
                WorkspaceFilterChip("Group", "Repository", clearable = false),
            ),
            workspaceFilterChips(state),
        )
    }

    @Test
    fun emptyStateClearsFiltersBeforeSendingUserElsewhere() {
        val state = AppState().apply {
            searchQuery = "acme/mobile"
        }

        val spec = emptyStateSpec(state)

        assertEquals("Clear filter", spec.primaryLabel)
        spec.primaryAction(state)
        assertEquals("", state.searchQuery)
    }

    @Test
    fun rowMetadataSeparatesStableIdentityFromUpdatedTime() {
        val pr = pullRequest(42, source = PullRequestSource.ReviewRequest)

        assertEquals("acme/mobile #42", rowIdentityMetadata(pr))
        assertEquals("Updated", rowUpdatedPrefix(pr).take(7))
    }

    @Test
    fun setupChecklistExplainsWhatIsMissing() {
        val state = AppState()

        assertEquals(
            listOf(
                SetupChecklistItem("GitHub CLI", false),
                SetupChecklistItem("Tracked repositories", false),
                SetupChecklistItem("Refresh", false),
            ),
            setupChecklistItems(state),
        )
    }

    private fun pullRequest(
        number: Int,
        source: PullRequestSource = PullRequestSource.ReviewRequest,
        checksFailing: Int = 0,
        reviewDecision: String? = null,
    ): PullRequest = PullRequest(
        repository = RepositoryId("acme", "mobile"),
        number = number,
        title = "Keyboard polish $number",
        url = "https://github.com/acme/mobile/pull/$number",
        updatedAt = "2026-07-07T10:00:00Z",
        source = source,
        checksFailing = checksFailing,
        reviewDecision = reviewDecision,
    )
}
