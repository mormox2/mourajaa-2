package com.example.notification

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class NotificationSchedulerTest {

    @Test
    fun testCalculateNextTriggerMillis_futureTimeToday() {
        // Mock current time: 2026-09-22 08:00:00
        val calNow = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 8)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val nowMillis = calNow.timeInMillis

        // Target: 19:30 today
        val nextTrigger = NotificationScheduler.calculateNextTriggerMillis("19:30", nowMillis)

        assertTrue("Trigger should be in the future", nextTrigger > nowMillis)

        val calTrigger = Calendar.getInstance().apply { timeInMillis = nextTrigger }
        assertEquals(19, calTrigger.get(Calendar.HOUR_OF_DAY))
        assertEquals(30, calTrigger.get(Calendar.MINUTE))
        assertEquals(calNow.get(Calendar.DAY_OF_YEAR), calTrigger.get(Calendar.DAY_OF_YEAR))
    }

    @Test
    fun testCalculateNextTriggerMillis_pastTimeRollsToTomorrow() {
        // Mock current time: 2026-09-22 20:00:00
        val calNow = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 20)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val nowMillis = calNow.timeInMillis

        // Target: 07:30 (already passed today)
        val nextTrigger = NotificationScheduler.calculateNextTriggerMillis("07:30", nowMillis)

        assertTrue("Trigger should be in the future", nextTrigger > nowMillis)

        val calTrigger = Calendar.getInstance().apply { timeInMillis = nextTrigger }
        assertEquals(7, calTrigger.get(Calendar.HOUR_OF_DAY))
        assertEquals(30, calTrigger.get(Calendar.MINUTE))

        val expectedNextDay = (calNow.get(Calendar.DAY_OF_YEAR) + 1)
        assertEquals("Should be scheduled for the next day", expectedNextDay, calTrigger.get(Calendar.DAY_OF_YEAR))
    }

    @Test
    fun testActionAndRequestCodeConstants() {
        assertEquals("com.example.notification.ACTION_MORNING_REMINDER", NotificationScheduler.ACTION_MORNING_REMINDER)
        assertEquals("com.example.notification.ACTION_PREVIOUS_DAY_REMINDER", NotificationScheduler.ACTION_PREVIOUS_DAY_REMINDER)
        assertEquals("com.example.notification.ACTION_EVENING_REVIEW", NotificationScheduler.ACTION_EVENING_REVIEW)
        assertEquals("com.example.notification.ACTION_TEST_NOTIFICATION", NotificationScheduler.ACTION_TEST_NOTIFICATION)
        assertEquals("com.example.notification.ACTION_SUBJECT_REMINDER", NotificationScheduler.ACTION_SUBJECT_REMINDER)

        assertEquals(1001, NotificationScheduler.RC_MORNING)
        assertEquals(1002, NotificationScheduler.RC_PREVIOUS_DAY)
        assertEquals(1003, NotificationScheduler.RC_EVENING_REVIEW)
        assertEquals(1004, NotificationScheduler.RC_TEST)
    }
}
