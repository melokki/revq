package eu.revq

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class GitHubPreflightGatewayTest {
    @Test
    fun `preflight reports authenticated account without exposing token output`() = runBlocking {
        val commands = mutableListOf<List<String>>()
        val gateway = ProcessGitHubPreflightGateway { command ->
            commands += command
            when (command.drop(1)) {
                listOf("--version") -> CommandResult(0, "gh version 2.75.0")
                listOf("auth", "status", "--active", "--json", "hosts") -> CommandResult(
                    0,
                    """{"hosts":{"github.com":[{"active":true,"login":"bogdan","token":"secret"}]}}""",
                )
                else -> error("Unexpected command: $command")
            }
        }

        assertEquals(GhDependencyResult.Available, gateway.checkDependency())
        assertEquals(
            GhAuthenticationResult.Authenticated(GitHubIdentity("bogdan", "github.com")),
            gateway.checkAuthentication(),
        )
        assertEquals(listOf("gh", "--version"), commands.first())
    }

    @Test
    fun `dependency retry resolves a newly installed executable location`() = runBlocking {
        var executable: String? = null
        val gateway = ResolvingGitHubPreflightGateway(
            resolveExecutable = { executable },
            execute = CommandExecutor { command ->
                assertEquals("/opt/homebrew/bin/gh", command.first())
                CommandResult(0, "gh version 2.75.0")
            },
        )

        assertEquals(GhDependencyResult.Missing, gateway.checkDependency())

        executable = "/opt/homebrew/bin/gh"

        assertEquals(GhDependencyResult.Available, gateway.checkDependency())
    }
}
