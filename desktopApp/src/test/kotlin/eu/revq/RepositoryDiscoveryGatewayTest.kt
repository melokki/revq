package eu.revq

import kotlin.test.Test
import kotlin.test.assertEquals

class RepositoryDiscoveryGatewayTest {
    @Test
    fun organizationDiscoveryRequestsMoreThanTheFirstHundredRepositories() {
        val commands = mutableListOf<List<String>>()
        val discovery = GitHubRepositoryDiscovery { command ->
            commands += command
            "acme/mobile\nacme/server"
        }

        val repositories = discovery.discoverRepositories(listOf("acme"))

        assertEquals(listOf("acme/mobile", "acme/server"), repositories)
        assertEquals(
            listOf(
                "repo", "list", "acme",
                "--limit", "1000",
                "--json", "nameWithOwner",
                "--template", "{{range .}}{{.nameWithOwner}}{{\"\\n\"}}{{end}}",
            ),
            commands.single(),
        )
    }
}
