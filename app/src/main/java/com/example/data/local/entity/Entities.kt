package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "children")
data class ChildEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val grade: String,
    val avatarUri: String? = null
)

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val childId: Long,
    val name: String,
    val icon: String, // Emoji or icon name like 📖, 🇫🇷, 🧪, 🇬🇧, 📐
    val defaultReviewMinutes: Int = 20,
    val masteryLevel: Int = 3, // 1: 20% (يحتاج مراجعة), 2: 40% (متوسط), 3: 60% (جيد), 4: 80% (جيد جداً), 5: 100% (ممتاز)
    val scheduledReviewTime: String? = null, // e.g. "18:30"
    val scheduledReminderEnabled: Boolean = false
)

@Entity(tableName = "schedule_entries")
data class ScheduleEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val childId: Long,
    val subjectId: Long,
    val dayOfWeek: String, // "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"
    val startTime: String, // "09:00"
    val endTime: String,   // "10:00"
    val lessonNote: String = "",
    val teacherName: String = "",
    val classroom: String = ""
)

@Entity(tableName = "review_sessions")
data class ReviewSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val childId: Long,
    val subjectId: Long,
    val date: String, // "YYYY-MM-DD"
    val plannedMinutes: Int,
    val actualMinutes: Int,
    val status: String, // "COMPLETED", "IN_PROGRESS", "SKIPPED"
    val masteryAfter: Int // 1 to 5 rating
)

@Entity(tableName = "reminder_settings")
data class ReminderSettingsEntity(
    @PrimaryKey
    val id: Long = 1,
    val previousDayEnabled: Boolean = true,
    val previousDayTime: String = "19:00",
    val morningEnabled: Boolean = true,
    val morningTime: String = "07:30",
    val reviewTimeEnabled: Boolean = true,
    val reviewTime: String = "19:30",
    val defaultReviewMinutes: Int = 20,
    val darkMode: Boolean = false,
    val parentPin: String = "1234",
    val focusModePinRequired: Boolean = true
)

@Entity(tableName = "earned_badges")
data class EarnedBadgeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val childId: Long,
    val badgeCode: String,
    val unlockedAt: String,
    val starsAwarded: Int
)

