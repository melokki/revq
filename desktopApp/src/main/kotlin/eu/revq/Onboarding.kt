package eu.revq

import java.io.FileNotFoundException
import java.io.IOException
import java.util.concurrent.TimeUnit

sealed interface GhDependencyState {
    data object Checking : GhDependencyState
    data object Missing : GhDependencyState
    data object Available : GhDependencyState
    data class Error(val message: String) : GhDependencyState
}

sealed interface GhAuthState {
    data object Checking : GhAuthState
    data object NotAuthenticated : GhAuthState
    data class Authenticated(val identity: GitHubIdentity) : GhAuthState
    data class Invalid(val message: String) : GhAuthState
    data class Error(val message: String) : GhAuthState
}

data class GitHubIdentity(
    val login: String,
    val host: String = "github.com",
)

sealed interface GhDependencyResult {
    data object Available : GhDependencyResult
    data object Missing : GhDependencyResult
    data class Error(val message: String) : GhDependencyResult
}

sealed interface GhAuthenticationResult {
    data class Authenticated(val identity: GitHubIdentity) : GhAuthenticationResult
    data object NotAuthenticated : GhAuthenticationResult
    data class Invalid(val message: String) : GhAuthenticationResult
    data class Error(val message: String) : GhAuthenticationResult
}

interface GitHubPreflightGateway {
    suspend fun checkDependency(): GhDependencyResult
    suspend fun checkAuthentication(): GhAuthenticationResult
}

fun interface OnboardingDiscoveryGateway {
    suspend fun discover(): RepositoryDiscoveryResult
}

data class OnboardingProgress(
    val completed: Boolean = false,
    val identity: GitHubIdentity? = null,
    val selection: RepositoryScopeSelection = RepositoryScopeSelection(),
    val activeRepositories: Set<String> = emptySet(),
)

interface OnboardingProgressStore {
    fun load(): OnboardingProgress
    fun save(progress: OnboardingProgress)
    fun clear()
}

class InMemoryOnboardingProgressStore(
    initial: OnboardingProgress = OnboardingProgress(),
) : OnboardingProgressStore {
    private var progress = initial

    override fun load(): OnboardingProgress = progress

    override fun save(progress: OnboardingProgress) {
        this.progress = progress
    }

    override fun clear() {
        progress = OnboardingProgress()
    }
}

class SettingsOnboardingProgressStore(
    private val settingsStore: SettingsStore,
) : OnboardingProgressStore {
    override fun load(): OnboardingProgress {
        val settings = settingsStore.load()
        val identity = settings.githubIdentityLogin
            .takeIf(String::isNotBlank)
            ?.let { GitHubIdentity(it, settings.githubIdentityHost.ifBlank { "github.com" }) }
        return OnboardingProgress(
            completed = settings.onboardingCompleted,
            identity = identity,
            selection = RepositoryScopeSelection(
                organizationScopes = settings.organizations.associateWith { OrganizationScope.All },
                individualRepositories = settings.repositories.toSet(),
            ),
            activeRepositories = settings.repositories.toSet(),
        )
    }

    override fun save(progress: OnboardingProgress) {
        val settings = settingsStore.load()
        val allOrganizations = progress.selection.organizationScopes
            .filterValues { it == OrganizationScope.All }
            .keys
            .sorted()
        settingsStore.save(
            settings.copy(
                onboardingCompleted = progress.completed,
                githubIdentityLogin = progress.identity?.login.orEmpty(),
                githubIdentityHost = progress.identity?.host.orEmpty(),
                repositories = if (progress.completed) {
                    progress.activeRepositories.sorted()
                } else {
                    settings.repositories
                },
                organizations = if (allOrganizations.isNotEmpty()) allOrganizations else settings.organizations,
            ),
        )
    }

    override fun clear() {
        val settings = settingsStore.load()
        settingsStore.save(
            settings.copy(
                onboardingCompleted = false,
                githubIdentityLogin = "",
                githubIdentityHost = "",
            ),
        )
    }
}

data class CommandResult(
    val exitCode: Int,
    val output: String,
)

fun interface CommandExecutor {
    suspend fun run(command: List<String>): CommandResult
}

class ProcessGitHubPreflightGateway(
    private val executable: String = "gh",
    private val execute: CommandExecutor = CommandExecutor(::executeCommand),
) : GitHubPreflightGateway {
    override suspend fun checkDependency(): GhDependencyResult = try {
        val result = execute.run(listOf(executable, "--version"))
        if (result.exitCode == 0) GhDependencyResult.Available
        else GhDependencyResult.Error("GitHub CLI was found but could not be run.")
    } catch (_: FileNotFoundException) {
        GhDependencyResult.Missing
    } catch (error: IOException) {
        if (error.message.orEmpty().contains("No such file", ignoreCase = true) ||
            error.message.orEmpty().contains("cannot find", ignoreCase = true)
        ) {
            GhDependencyResult.Missing
        } else {
            GhDependencyResult.Error("GitHub CLI could not be started.")
        }
    } catch (_: Throwable) {
        GhDependencyResult.Error("GitHub CLI could not be checked.")
    }

    override suspend fun checkAuthentication(): GhAuthenticationResult = try {
        val result = execute.run(
            listOf(executable, "auth", "status", "--active", "--json", "hosts"),
        )
        if (result.exitCode != 0) {
            GhAuthenticationResult.NotAuthenticated
        } else {
            parseActiveIdentity(result.output)
                ?.let(GhAuthenticationResult::Authenticated)
                ?: GhAuthenticationResult.Invalid("GitHub CLI has no active authenticated account.")
        }
    } catch (_: Throwable) {
        GhAuthenticationResult.Error("GitHub authentication could not be checked.")
    }
}

class ResolvingGitHubPreflightGateway(
    private val resolveExecutable: () -> String?,
    private val execute: CommandExecutor = CommandExecutor(::executeCommand),
) : GitHubPreflightGateway {
    private var resolvedExecutable: String? = null

    override suspend fun checkDependency(): GhDependencyResult {
        val executable = resolveExecutable()
        if (executable.isNullOrBlank()) {
            resolvedExecutable = null
            return GhDependencyResult.Missing
        }
        val result = ProcessGitHubPreflightGateway(executable, execute).checkDependency()
        resolvedExecutable = executable.takeIf { result == GhDependencyResult.Available }
        return result
    }

    override suspend fun checkAuthentication(): GhAuthenticationResult {
        val executable = resolvedExecutable ?: resolveExecutable()
            ?: return GhAuthenticationResult.Error("GitHub CLI is not available.")
        return ProcessGitHubPreflightGateway(executable, execute).checkAuthentication()
    }
}

private fun parseActiveIdentity(output: String): GitHubIdentity? {
    val host = Regex("\"hosts\"\\s*:\\s*\\{\\s*\"([^\"]+)\"")
        .find(output)?.groupValues?.getOrNull(1) ?: return null
    val activeAccount = Regex(
        "\\{[^{}]*\"active\"\\s*:\\s*true[^{}]*\"login\"\\s*:\\s*\"([^\"]+)\"[^{}]*}",
    ).find(output) ?: Regex(
        "\\{[^{}]*\"login\"\\s*:\\s*\"([^\"]+)\"[^{}]*\"active\"\\s*:\\s*true[^{}]*}",
    ).find(output)
    val login = activeAccount?.groupValues?.getOrNull(1) ?: return null
    return GitHubIdentity(login, host)
}

private fun executeCommand(command: List<String>): CommandResult {
    val process = ProcessBuilder(command)
        .redirectErrorStream(true)
        .start()
    val finished = process.waitFor(15, TimeUnit.SECONDS)
    if (!finished) {
        process.destroyForcibly()
        return CommandResult(124, "")
    }
    val output = process.inputStream.bufferedReader().use { it.readText() }
    return CommandResult(process.exitValue(), output)
}

enum class OnboardingStep {
    CheckingGitHubCli,
    GitHubCliRequired,
    CheckingAuthentication,
    AuthenticationRequired,
    ConfirmIdentity,
    SelectScope,
    ReviewScope,
    Starting,
    Complete,
    Error,
}

data class OnboardingState(
    val step: OnboardingStep = OnboardingStep.CheckingGitHubCli,
    val dependency: GhDependencyState = GhDependencyState.Checking,
    val authentication: GhAuthState? = null,
    val message: String? = null,
    val identity: GitHubIdentity? = null,
    val discovery: RepositoryDiscoveryResult? = null,
    val selection: RepositoryScopeSelection = RepositoryScopeSelection(),
    val summary: OnboardingScopeSummary? = null,
)

data class OnboardingScopeSummary(
    val organizationCount: Int,
    val activeRepositoryCount: Int,
    val activeRepositories: Set<String>,
)

class OnboardingCoordinator(
    private val preflight: GitHubPreflightGateway,
    private val discovery: OnboardingDiscoveryGateway = OnboardingDiscoveryGateway {
        RepositoryDiscoveryResult("", emptyList(), emptyList())
    },
    private val progressStore: OnboardingProgressStore = InMemoryOnboardingProgressStore(),
) {
    var state: OnboardingState = OnboardingState()
        private set

    suspend fun start() {
        val progress = progressStore.load()
        if (progress.completed) {
            state = OnboardingState(
                step = OnboardingStep.Complete,
                dependency = GhDependencyState.Available,
                identity = progress.identity,
                selection = progress.selection,
            )
            return
        }
        state = OnboardingState()
        when (val result = preflight.checkDependency()) {
            GhDependencyResult.Available -> {
                state = state.copy(
                    step = OnboardingStep.CheckingAuthentication,
                    dependency = GhDependencyState.Available,
                    authentication = GhAuthState.Checking,
                )
                checkAuthentication()
            }
            GhDependencyResult.Missing -> {
                state = state.copy(
                    step = OnboardingStep.GitHubCliRequired,
                    dependency = GhDependencyState.Missing,
                )
            }
            is GhDependencyResult.Error -> {
                state = state.copy(
                    step = OnboardingStep.Error,
                    dependency = GhDependencyState.Error(result.message),
                    message = result.message,
                )
            }
        }
    }

    suspend fun retry() = start()

    suspend fun confirmIdentity() {
        val identity = (state.authentication as? GhAuthState.Authenticated)?.identity ?: return
        try {
            val result = discovery.discover()
            val saved = progressStore.load()
            val selection = if (
                saved.selection.organizationScopes.isEmpty() &&
                saved.selection.individualRepositories.isEmpty()
            ) {
                saved.selection.copy(
                    individualRepositories = result.repositories
                        .asSequence()
                        .filterNot { it.archived }
                        .filter { it.owner.equals(identity.login, ignoreCase = true) }
                        .map { it.nameWithOwner }
                        .toSet(),
                )
            } else {
                saved.selection
            }
            progressStore.save(saved.copy(identity = identity, selection = selection))
            state = state.copy(
                step = OnboardingStep.SelectScope,
                identity = identity,
                discovery = result,
                selection = selection,
                message = null,
            )
        } catch (_: Throwable) {
            state = state.copy(
                step = OnboardingStep.Error,
                identity = identity,
                message = "Repository discovery failed. Check your connection and try again.",
            )
        }
    }

    fun selectScope(selection: RepositoryScopeSelection) {
        val progress = progressStore.load().copy(selection = selection)
        progressStore.save(progress)
        state = state.copy(selection = selection, summary = null)
    }

    fun reviewScope() {
        val discovered = state.discovery ?: return
        val active = state.selection.activeRepositories(discovered.repositories)
        if (active.isEmpty()) {
            state = state.copy(message = "Select at least one active repository to continue.")
            return
        }
        val selectedOrganizations = state.selection.organizationScopes.count { (_, scope) ->
            scope != OrganizationScope.Disabled
        }
        state = state.copy(
            step = OnboardingStep.ReviewScope,
            summary = OnboardingScopeSummary(
                organizationCount = selectedOrganizations,
                activeRepositoryCount = active.size,
                activeRepositories = active,
            ),
            message = null,
        )
    }

    fun backToScopeSelection() {
        state = state.copy(step = OnboardingStep.SelectScope, summary = null)
    }

    fun complete() {
        val summary = state.summary ?: return
        val progress = progressStore.load().copy(
            completed = true,
            identity = state.identity,
            selection = state.selection,
            activeRepositories = summary.activeRepositories,
        )
        progressStore.save(progress)
        state = state.copy(step = OnboardingStep.Complete)
    }

    fun restart() {
        progressStore.clear()
        state = OnboardingState()
    }

    private suspend fun checkAuthentication() {
        when (val result = preflight.checkAuthentication()) {
            is GhAuthenticationResult.Authenticated -> state = state.copy(
                step = OnboardingStep.ConfirmIdentity,
                authentication = GhAuthState.Authenticated(result.identity),
            )
            GhAuthenticationResult.NotAuthenticated -> state = state.copy(
                step = OnboardingStep.AuthenticationRequired,
                authentication = GhAuthState.NotAuthenticated,
            )
            is GhAuthenticationResult.Invalid -> state = state.copy(
                step = OnboardingStep.AuthenticationRequired,
                authentication = GhAuthState.Invalid(result.message),
                message = result.message,
            )
            is GhAuthenticationResult.Error -> state = state.copy(
                step = OnboardingStep.Error,
                authentication = GhAuthState.Error(result.message),
                message = result.message,
            )
        }
    }
}
