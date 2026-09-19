package com.example

import com.example.domain.model.DayOfWeekAr
import com.example.domain.model.MasteryLevel
import org.junit.Assert.assertEquals
import org.junit.Test

class MuraajaDomainTest {

    @Test
    fun testMasteryLevel_recommendedMinutes() {
        assertEquals(30, MasteryLevel.NEEDS_REVIEW.recommendedMinutes)
        assertEquals(25, MasteryLevel.MEDIUM.recommendedMinutes)
        assertEquals(20, MasteryLevel.GOOD.recommendedMinutes)
        assertEquals(15, MasteryLevel.VERY_GOOD.recommendedMinutes)
        assertEquals(10, MasteryLevel.EXCELLENT.recommendedMinutes)
    }

    @Test
    fun testDayOfWeek_nextSchoolDay() {
        // Monday -> Tuesday
        assertEquals(DayOfWeekAr.TUESDAY, DayOfWeekAr.getNextSchoolDay("MONDAY"))
        // Tuesday -> Wednesday
        assertEquals(DayOfWeekAr.WEDNESDAY, DayOfWeekAr.getNextSchoolDay("TUESDAY"))
        // Wednesday -> Thursday
        assertEquals(DayOfWeekAr.THURSDAY, DayOfWeekAr.getNextSchoolDay("WEDNESDAY"))
        // Thursday -> Friday
        assertEquals(DayOfWeekAr.FRIDAY, DayOfWeekAr.getNextSchoolDay("THURSDAY"))
        // Friday -> Monday
        assertEquals(DayOfWeekAr.MONDAY, DayOfWeekAr.getNextSchoolDay("FRIDAY"))
    }
}
