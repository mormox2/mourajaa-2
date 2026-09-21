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
        // Sunday -> Monday
        assertEquals(DayOfWeekAr.MONDAY, DayOfWeekAr.getNextSchoolDay("SUNDAY"))
        // Monday -> Tuesday
        assertEquals(DayOfWeekAr.TUESDAY, DayOfWeekAr.getNextSchoolDay("MONDAY"))
        // Tuesday -> Wednesday
        assertEquals(DayOfWeekAr.WEDNESDAY, DayOfWeekAr.getNextSchoolDay("TUESDAY"))
        // Wednesday -> Thursday
        assertEquals(DayOfWeekAr.THURSDAY, DayOfWeekAr.getNextSchoolDay("WEDNESDAY"))
        // Thursday -> Friday
        assertEquals(DayOfWeekAr.FRIDAY, DayOfWeekAr.getNextSchoolDay("THURSDAY"))
        // Friday -> Sunday
        assertEquals(DayOfWeekAr.SUNDAY, DayOfWeekAr.getNextSchoolDay("FRIDAY"))
    }

    @Test
    fun testChildEntity_activeFlag() {
        val activeChild = com.example.data.local.entity.ChildEntity(
            id = 1L,
            name = "أيهم",
            grade = "السنة الثانية ابتدائي (2AP)",
            isActive = true
        )
        assertEquals(true, activeChild.isActive)
        assertEquals("أيهم", activeChild.name)

        val inactiveChild = activeChild.copy(isActive = false)
        assertEquals(false, inactiveChild.isActive)
    }

    @Test
    fun testScheduleTimeUtils_parseAndFormat() {
        val minutes = com.example.ui.schedule.ScheduleTimeUtils.parseTimeToMinutes("08:30")
        assertEquals(510, minutes)
        val formatted = com.example.ui.schedule.ScheduleTimeUtils.formatMinutesToTime(510)
        assertEquals("08:30", formatted)
    }

    @Test
    fun testScheduleTimeUtils_addMinutes() {
        val result1 = com.example.ui.schedule.ScheduleTimeUtils.addMinutes("08:00", 60)
        assertEquals("09:00", result1)

        val result2 = com.example.ui.schedule.ScheduleTimeUtils.addMinutes("11:45", 45)
        assertEquals("12:30", result2)

        val result3 = com.example.ui.schedule.ScheduleTimeUtils.addMinutes("13:30", 90)
        assertEquals("15:00", result3)
    }

    @Test
    fun testScheduleTimeUtils_durationCalculation() {
        val duration = com.example.ui.schedule.ScheduleTimeUtils.calculateDurationMinutes("08:00", "09:30")
        assertEquals(90, duration)

        val text = com.example.ui.schedule.ScheduleTimeUtils.formatDurationArabic(60)
        assertEquals("ساعة واحدة", text)

        val text2 = com.example.ui.schedule.ScheduleTimeUtils.formatDurationArabic(90)
        assertEquals("1 ساعة و 30 دقيقة", text2)
    }
}
