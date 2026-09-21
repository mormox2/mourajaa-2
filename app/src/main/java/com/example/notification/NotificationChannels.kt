package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build

object NotificationChannels {

    const val CHANNEL_DAILY_REMINDERS = "muraaja_daily_reminders"
    const val CHANNEL_SPECIAL_ALERTS = "muraaja_special_alerts"

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                ?: return

            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                .build()

            // 1. Daily reminders channel (High priority for morning prep & evening review)
            val remindersChannel = NotificationChannel(
                CHANNEL_DAILY_REMINDERS,
                "تذكيرات المراجعة والحقيبة المدرسية",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "إشعارات تجهيز المحفظة الصباحية ومواعيد المراجعة المسائية ومواد الغد"
                enableVibration(true)
                enableLights(true)
                setSound(defaultSoundUri, audioAttributes)
            }

            // 2. Special alerts / Achievements channel
            val alertsChannel = NotificationChannel(
                CHANNEL_SPECIAL_ALERTS,
                "إشعارات التحفيز والأوسمة",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "تنبيهات التقدم الأسبوعي والأوسمة المكتسبة"
                enableVibration(true)
            }

            notificationManager.createNotificationChannels(listOf(remindersChannel, alertsChannel))
        }
    }
}
