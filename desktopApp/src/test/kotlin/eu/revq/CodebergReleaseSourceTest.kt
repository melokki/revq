package eu.revq

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class CodebergReleaseSourceTest {
    @Test
    fun `canonical release response preserves metadata and assets`() = runBlocking {
        val source = CodebergReleaseSource {
            """
            [
              {
                "tag_name":"v0.2.0",
                "name":"RevQ 0.2.0",
                "body":"Queue fixes and \"faster\" navigation",
                "published_at":"2026-07-01T08:00:00Z",
                "draft":false,
                "prerelease":false,
                "assets":[
                  {"name":"revq-0.2.0-linux-x86_64.deb","browser_download_url":"https://codeberg.org/package","size":42},
                  {"name":"SHA256SUMS","browser_download_url":"https://codeberg.org/checksums","size":64}
                ]
              }
            ]
            """.trimIndent()
        }

        val release = source.loadReleases().single()

        assertEquals("v0.2.0", release.tag)
        assertEquals("Queue fixes and \"faster\" navigation", release.notes)
        assertEquals("revq-0.2.0-linux-x86_64.deb", release.assets.first().name)
        assertEquals(42, release.assets.first().sizeBytes)
    }
}
