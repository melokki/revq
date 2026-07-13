package eu.revq

import eu.revq.resources.Res
import eu.revq.resources.revq_brand_mark
import kotlin.test.Test
import kotlin.test.assertNotNull

class DesktopComposeResourceTest {
    @Test
    fun typeSafeVectorBrandMarkIsAvailableToTheDesktopUi() {
        assertNotNull(Res.drawable.revq_brand_mark)
    }
}
