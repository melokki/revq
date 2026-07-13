package eu.revq

import kotlin.test.Test
import kotlin.test.assertEquals

class RepositoryScopeHealthTest {
    @Test
    fun `scope validation isolates archived inaccessible missing and transferred repositories`() {
        val health = validateRepositoryScope(
            savedRepositories = setOf("acme/api", "acme/old", "acme/private", "acme/gone"),
            discoveredRepositories = listOf(
                DiscoveredRepository("acme/api", "acme"),
                DiscoveredRepository("acme/old", "acme", archived = true),
                DiscoveredRepository("newco/private", "newco"),
            ),
            inaccessibleRepositories = setOf("acme/private"),
            relocatedRepositories = mapOf("acme/private" to "newco/private"),
        )

        assertEquals(RepositoryHealth.Active, health.getValue("acme/api"))
        assertEquals(RepositoryHealth.Archived, health.getValue("acme/old"))
        assertEquals(RepositoryHealth.Relocated("newco/private"), health.getValue("acme/private"))
        assertEquals(RepositoryHealth.Missing, health.getValue("acme/gone"))
    }
}
