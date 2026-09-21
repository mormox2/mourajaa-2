package com.example.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.AlarmManagerCompat
import com.example.data.local.entity.ReminderSettingsEntity
import java.util.Calendar

object NotificationScheduler {

    const val ACTION_MORNING_REMINDER = "com.example.notification.ACTION_MORNING_REMINDER"
    const val ACTION_PREVIOUS_DAY_REMINDER = "com.example.notification.ACTION_PREVIOUS_DAY_REMINDER"
    const val ACTION_EVENING_REVIEW = "com.example.notification.ACTION_EVENING_REVIEW"
    const val ACTION_SUBJECT_REMINDER = "com.example.notification.ACTION_SUBJECT_REMINDER"
    const val ACTION_TEST_NOTIFICATION = "com.example.notification.ACTION_TEST_NOTIFICATION"

    const val EXTRA_SUBJECT_ID = "extra_subject_id"
    const val EXTRA_SUBJECT_NAME = "extra_subject_name"

    const val RC_MORNING = 1001
    const val RC_PREVIOUS_DAY = 1002
    const val RC_EVENING_REVIEW = 1003
    const val RC_TEST = 1004
    const val RC_SUBJECT_BASE = 2000

    fun scheduleAllReminders(context: Context, settings: ReminderSettingsEntity?) {
        if (settings == null) return

        // 1. Morning Reminder (تجهيز الحقيبة المدرسية)
        if (settings.morningEnabled) {
            scheduleDailyAlarm(context, settings.morningTime, ACTION_MORNING_REMINDER, RC_MORNING)
        } else {
            cancelAlarm(context, ACTION_MORNING_REMINDER, RC_MORNING)
        }

        // 2. Previous Day Reminder (مواد الغد)
        if (settings.previousDayEnabled) {
            scheduleDailyAlarm(context, settings.previousDayTime, ACTION_PREVIOUS_DAY_REMINDER, RC_PREVIOUS_DAY)
        } else {
            cancelAlarm(context, ACTION_PREVIOUS_DAY_REMINDER, RC_PREVIOUS_DAY)
        }

        // 3. Evening Review Reminder (وقت المراجعة المسائية)
        if (settings.reviewTimeEnabled) {
            scheduleDailyAlarm(context, settings.reviewTime, ACTION_EVENING_REVIEW, RC_EVENING_REVIEW)
        } else {
            cancelAlarm(context, ACTION_EVENING_REVIEW, RC_EVENING_REVIEW)
        }
    }

    fun scheduleSubjectReminder(
        context: Context,
        subjectId: Long,
        subjectName: String,
        timeStr: String,
        enabled: Boolean
    ) {
        val requestCode = RC_SUBJECT_BASE + (subjectId % 1000).toInt()
        if (enabled) {
            val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
                action = ACTION_SUBJECT_REMINDER
                putExtra(EXTRA_SUBJECT_ID, subjectId)
                putExtra(EXTRA_SUBJECT_NAME, subjectName)
            }
            scheduleAlarmAtTime(context, timeStr, intent, requestCode)
        } else {
            cancelAlarm(context, ACTION_SUBJECT_REMINDER, requestCode)
        }
    }

    fun sendImmediateTestNotification(context: Context) {
        val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
            action = ACTION_TEST_NOTIFICATION
        }
        context.sendBroadcast(intent)
    }

    private fun scheduleDailyAlarm(context: Context, timeStr: String, action: String, requestCode: Int) {
        val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
            this.action = action
        }
        scheduleAlarmAtTime(context, timeStr, intent, requestCode)
    }

    private fun scheduleAlarmAtTime(context: Context, timeStr: String, intent: Intent, requestCode: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val triggerAtMillis = calculateNextTriggerMillis(timeStr)

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, flags)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    AlarmManagerCompat.setExactAndAllowWhileIdle(
                        alarmManager,
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                AlarmManagerCompat.setExactAndAllowWhileIdle(
                    alarmManager,
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            // Fallback to inexact alarm if exact alarm permission was revoked by user
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    fun cancelAlarm(context: Context, action: String, requestCode: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
            this.action = action
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_NO_CREATE
        }
        val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, flags)
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    /**
     * Calculates the next timestamp in milliseconds for an "HH:mm" formatted time.
     * If the specified time has already passed today, returns the timestamp for tomorrow.
     */
    fun calculateNextTriggerMillis(timeStr: String, nowMillis: Long = System.currentTimeMillis()): Long {
        val parts = timeStr.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 19
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0

        val calendar = Calendar.getInstance().apply {
            timeInMillis = nowMillis
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (calendar.timeInMillis <= nowMillis) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        return calendar.timeInMillis
    }
}
