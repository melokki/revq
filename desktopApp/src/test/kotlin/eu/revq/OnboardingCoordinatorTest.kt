package eu.revq

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class OnboardingCoordinatorTest {
    @Test
    fun `missing GitHub CLI blocks onboarding before authentication`() = runBlocking {
        val gateway = RecordingPreflightGateway(
            dependency = GhDependencyResult.Missing,
        )
        val coordinator = OnboardingCoordinator(gateway)

        coordinator.start()

        assertEquals(GhDependencyState.Missing, coordinator.state.dependency)
        assertEquals(OnboardingStep.GitHubCliRequired, coordinator.state.step)
        assertEquals(0, gateway.authenticationChecks)
    }

    @Test
    fun `authenticated user sees active identity before scope discovery`() = runBlocking {
        val identity = GitHubIdentity("bogdan", "github.com")
        val gateway = RecordingPreflightGateway(
            dependency = GhDependencyResult.Available,
            authentication = GhAuthenticationResult.Authenticated(identity),
        )
        val coordinator = OnboardingCoordinator(gateway)

        coordinator.start()

        assertEquals(OnboardingStep.ConfirmIdentity, coordinator.state.step)
        assertEquals(GhAuthState.Authenticated(identity), coordinator.state.authentication)
    }

    @Test
    fun `check again resumes after GitHub CLI is installed`() = runBlocking {
        val gateway = RecoveringPreflightGateway()
        val coordinator = OnboardingCoordinator(gateway)
        coordinator.start()

        gateway.installed = true
        coordinator.retry()

        assertEquals(OnboardingStep.ConfirmIdentity, coordinator.state.step)
        assertEquals(2, gateway.dependencyChecks)
    }

    @Test
    fun `user can review selected scope and complete onboarding`() = runBlocking {
        val progressStore = InMemoryOnboardingProgressStore()
        val discovery = RepositoryDiscoveryResult(
            login = "bogdan",
            organizations = listOf(DiscoveredOrganization("acme")),
            repositories = listOf(
                DiscoveredRepository("acme/api", "acme"),
                DiscoveredRepository("acme/archived", "acme", archived = true),
            ),
        )
        val coordinator = OnboardingCoordinator(
            preflight = RecordingPreflightGateway(
                dependency = GhDependencyResult.Available,
                authentication = GhAuthenticationResult.Authenticated(GitHubIdentity("bogdan")),
            ),
            discovery = OnboardingDiscoveryGateway { discovery },
            progressStore = progressStore,
        )

        coordinator.start()
        coordinator.confirmIdentity()
        coordinator.selectScope(
            RepositoryScopeSelection(
                organizationScopes = mapOf("acme" to OrganizationScope.All),
            ),
        )
        coordinator.reviewScope()

        assertEquals(OnboardingStep.ReviewScope, coordinator.state.step)
        assertEquals(1, coordinator.state.summary?.activeRepositoryCount)
        assertEquals(1, coordinator.state.summary?.organizationCount)

        coordinator.complete()

        assertEquals(OnboardingStep.Complete, coordinator.state.step)
        assertEquals(true, progressStore.load().completed)
        assertEquals(setOf("acme/api"), progressStore.load().activeRepositories)
    }
}

private class RecordingPreflightGateway(
    private val dependency: GhDependencyResult,
    private val authentication: GhAuthenticationResult = GhAuthenticationResult.NotAuthenticated,
) : GitHubPreflightGateway {
    var authenticationChecks = 0

    override suspend fun checkDependency(): GhDependencyResult = dependency

    override suspend fun checkAuthentication(): GhAuthenticationResult {
        authenticationChecks += 1
        return authentication
    }
}

private class RecoveringPreflightGateway : GitHubPreflightGateway {
    var installed = false
    var dependencyChecks = 0

    override suspend fun checkDependency(): GhDependencyResult {
        dependencyChecks += 1
        return if (installed) GhDependencyResult.Available else GhDependencyResult.Missing
    }

    override suspend fun checkAuthentication(): GhAuthenticationResult =
        GhAuthenticationResult.Authenticated(GitHubIdentity("bogdan"))
}
