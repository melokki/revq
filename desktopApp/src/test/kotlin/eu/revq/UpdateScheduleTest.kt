package eu.revq

import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.test.Test
import kotlin.test.assertEquals

class UpdateScheduleTest {
    @Test
    fun `daily update check targets the next local nine oclock`() {
        val zone = ZoneId.of("Europe/Bucharest")

        assertEquals(
            ZonedDateTime.of(2026, 7, 13, 9, 0, 0, 0, zone),
            nextDailyUpdateCheck(ZonedDateTime.of(2026, 7, 13, 8, 30, 0, 0, zone)),
        )
        assertEquals(
            ZonedDateTime.of(2026, 7, 14, 9, 0, 0, 0, zone),
            nextDailyUpdateCheck(ZonedDateTime.of(2026, 7, 13, 9, 1, 0, 0, zone)),
        )
    }
}
