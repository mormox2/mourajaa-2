package com.example.notification

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.data.local.AppDatabase
import com.example.domain.model.DayOfWeekAr
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

class ReminderBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return

        // Ensure notification channels exist
        NotificationChannels.createChannels(context)

        when (action) {
            NotificationScheduler.ACTION_TEST_NOTIFICATION -> {
                showNotification(
                    context = context,
                    notificationId = 999,
                    title = "🔔 تجربة إشعارات مراجعة",
                    message = "نظام التنبيهات يعمل بنجاح على هاتفك! ستصلك التذكيرات في مواعيدها المحددة.",
                    channelId = NotificationChannels.CHANNEL_DAILY_REMINDERS
                )
            }
            NotificationScheduler.ACTION_MORNING_REMINDER -> {
                handleMorningReminder(context)
            }
            NotificationScheduler.ACTION_PREVIOUS_DAY_REMINDER -> {
                handlePreviousDayReminder(context)
            }
            NotificationScheduler.ACTION_EVENING_REVIEW -> {
                handleEveningReviewReminder(context)
            }
            NotificationScheduler.ACTION_SUBJECT_REMINDER -> {
                val subjectId = intent.getLongExtra(NotificationScheduler.EXTRA_SUBJECT_ID, 0L)
                val subjectName = intent.getStringExtra(NotificationScheduler.EXTRA_SUBJECT_NAME) ?: "المادة"
                val notifId = (200 + (subjectId % 100)).toInt()
                showNotification(
                    context = context,
                    notificationId = notifId,
                    title = "⏰ موعد مراجعة $subjectName",
                    message = "حان وقت الجلسة المجدولة لمادة $subjectName. هيا نبدأ لترسيخ المفاهيم!",
                    channelId = NotificationChannels.CHANNEL_DAILY_REMINDERS
                )
            }
        }
    }

    private fun handleMorningReminder(context: Context) {
        val pendingResult = goAsync()
        val scope = CoroutineScope(Dispatchers.IO)

        scope.launch {
            try {
                val db = AppDatabase.getDatabase(context, scope)
                val child = db.childDao().getActiveChildSync()
                val childName = child?.name ?: "البطل"
                val todayDayKey = getTodayDayOfWeekKey()

                val entries = if (child != null) {
                    db.scheduleDao().getEntriesForDaySync(child.id, todayDayKey)
                } else emptyList()

                val subjectMap = if (child != null) {
                    db.subjectDao().getSubjectsForChildSync(child.id).associateBy { it.id }
                } else emptyMap()

                val subjectNames = entries.mapNotNull { subjectMap[it.subjectId]?.name }
                    .distinct()

                val body = if (subjectNames.isNotEmpty()) {
                    val formattedList = subjectNames.joinToString("، ")
                    "صباح الخير! حصص اليوم لـ $childName: $formattedList. لا تنسَ تجهيز المحفظة والدفاتر!"
                } else {
                    "صباح الخير لـ $childName! تأكد من تجهيز أدواتك ليوم دراسي ممتع وموفق."
                }

                showNotification(
                    context = context,
                    notificationId = 101,
                    title = "🎒 صباح الخير! تجهيز المحفظة المدرسية",
                    message = body,
                    channelId = NotificationChannels.CHANNEL_DAILY_REMINDERS
                )

                // Reschedule for next day
                val settings = db.reminderSettingsDao().getSettingsSync()
                NotificationScheduler.scheduleAllReminders(context, settings)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun handlePreviousDayReminder(context: Context) {
        val pendingResult = goAsync()
        val scope = CoroutineScope(Dispatchers.IO)

        scope.launch {
            try {
                val db = AppDatabase.getDatabase(context, scope)
                val child = db.childDao().getActiveChildSync()
                val childName = child?.name ?: "البطل"
                val todayDayKey = getTodayDayOfWeekKey()
                val nextDayKey = DayOfWeekAr.getNextSchoolDay(todayDayKey).key

                val entries = if (child != null) {
                    db.scheduleDao().getEntriesForDaySync(child.id, nextDayKey)
                } else emptyList()

                val subjectMap = if (child != null) {
                    db.subjectDao().getSubjectsForChildSync(child.id).associateBy { it.id }
                } else emptyMap()

                val subjectNames = entries.mapNotNull { subjectMap[it.subjectId]?.name }
                    .distinct()

                val body = if (subjectNames.isNotEmpty()) {
                    val formattedList = subjectNames.joinToString("، ")
                    "تذكير تحضير الغد: مبرمج لمواد $formattedList لـ $childName. وقت مراجعة الدروس وحل الواجبات!"
                } else {
                    "تذكير مواد الغد: لا توجد حصص جديدة مبرمجة للغد لـ $childName. وقت مناسب للمراجعة الشاملة!"
                }

                showNotification(
                    context = context,
                    notificationId = 102,
                    title = "📚 تذكير تحضير ومراجعة مواد الغد",
                    message = body,
                    channelId = NotificationChannels.CHANNEL_DAILY_REMINDERS
                )

                val settings = db.reminderSettingsDao().getSettingsSync()
                NotificationScheduler.scheduleAllReminders(context, settings)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun handleEveningReviewReminder(context: Context) {
        val pendingResult = goAsync()
        val scope = CoroutineScope(Dispatchers.IO)

        scope.launch {
            try {
                val db = AppDatabase.getDatabase(context, scope)
                val child = db.childDao().getActiveChildSync()
                val childName = child?.name ?: "البطل"

                showNotification(
                    context = context,
                    notificationId = 103,
                    title = "⭐ حان وقت المراجعة المسائية!",
                    message = "موعد جلسة المراجعة اليومية لـ $childName. افتح التطبيق لبدء المذاكرة وحصد النجوم!",
                    channelId = NotificationChannels.CHANNEL_DAILY_REMINDERS
                )

                val settings = db.reminderSettingsDao().getSettingsSync()
                NotificationScheduler.scheduleAllReminders(context, settings)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun showNotification(
        context: Context,
        notificationId: Int,
        title: String,
        message: String,
        channelId: String
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getActivity(context, notificationId, intent, flags)

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification_bell)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(notificationId, builder.build())
        } catch (e: SecurityException) {
            // In case POST_NOTIFICATIONS permission was revoked
            e.printStackTrace()
        }
    }

    private fun getTodayDayOfWeekKey(): String {
        return try {
            when (LocalDate.now().dayOfWeek) {
                java.time.DayOfWeek.MONDAY -> "MONDAY"
                java.time.DayOfWeek.TUESDAY -> "TUESDAY"
                java.time.DayOfWeek.WEDNESDAY -> "WEDNESDAY"
                java.time.DayOfWeek.THURSDAY -> "THURSDAY"
                java.time.DayOfWeek.FRIDAY -> "FRIDAY"
                java.time.DayOfWeek.SATURDAY -> "MONDAY"
                java.time.DayOfWeek.SUNDAY -> "MONDAY"
                else -> "TUESDAY"
            }
        } catch (e: Exception) {
            "MONDAY"
        }
    }
}
