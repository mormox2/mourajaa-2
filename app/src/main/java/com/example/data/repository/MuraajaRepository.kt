package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.entity.ChildEntity
import com.example.data.local.entity.EarnedBadgeEntity
import com.example.data.local.entity.ReminderSettingsEntity
import com.example.data.local.entity.ReviewSessionEntity
import com.example.data.local.entity.ScheduleEntryEntity
import com.example.data.local.entity.SubjectEntity
import com.example.domain.model.BadgeCatalog
import com.example.domain.model.BadgeDefinition
import com.example.domain.model.BadgeItem
import com.example.domain.model.DayOfWeekAr
import com.example.domain.model.MasteryLevel
import com.example.domain.model.NightReviewPlan
import com.example.domain.model.SubjectWithReview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MuraajaRepository(private val db: AppDatabase) {

    val activeChild: Flow<ChildEntity?> = db.childDao().getActiveChild()
    val allChildren: Flow<List<ChildEntity>> = db.childDao().getAllChildren()
    val reminderSettings: Flow<ReminderSettingsEntity?> = db.reminderSettingsDao().getSettings()

    fun getSubjectsForChild(childId: Long): Flow<List<SubjectEntity>> =
        db.subjectDao().getSubjectsForChild(childId)

    fun getScheduleForDay(childId: Long, dayOfWeek: String): Flow<List<ScheduleEntryEntity>> =
        db.scheduleDao().getEntriesForDay(childId, dayOfWeek)

    fun getAllScheduleEntries(childId: Long): Flow<List<ScheduleEntryEntity>> =
        db.scheduleDao().getAllEntries(childId)

    fun getCompletedSessionsForDate(childId: Long, date: String): Flow<List<ReviewSessionEntity>> =
        db.reviewSessionDao().getSessionsForDate(childId, date)

    suspend fun calculateNightReviewPlan(childId: Long, currentDay: DayOfWeekAr): NightReviewPlan {
        val nextDay = DayOfWeekAr.getNextSchoolDay(currentDay.key)
        val entries = db.scheduleDao().getEntriesForDaySync(childId, nextDay.key)
        val subjectsList = db.subjectDao().getSubjectsForChild(childId).firstOrNull() ?: emptyList()
        val subjectMap = subjectsList.associateBy { it.id }

        val reviewItems = entries.mapNotNull { entry ->
            val sub = subjectMap[entry.subjectId] ?: return@mapNotNull null
            val mastery = MasteryLevel.fromLevel(sub.masteryLevel)
            val minutes = mastery.recommendedMinutes // From algorithm in specs (30, 25, 20, 15, 10 mins)
            SubjectWithReview(
                id = sub.id,
                childId = childId,
                name = sub.name,
                icon = sub.icon,
                mastery = mastery,
                reviewMinutes = minutes,
                lessonNote = entry.lessonNote,
                timeSlot = "${entry.startTime} - ${entry.endTime}",
                scheduledReviewTime = sub.scheduledReviewTime,
                scheduledReminderEnabled = sub.scheduledReminderEnabled
            )
        }.distinctBy { it.id }

        val totalMinutes = reviewItems.sumOf { it.reviewMinutes }
        return NightReviewPlan(
            targetDay = nextDay,
            items = reviewItems,
            totalMinutes = totalMinutes
        )
    }

    suspend fun saveChild(name: String, grade: String, avatarUri: String? = null): Long {
        db.childDao().clearActiveChild()
        val newChildId = db.childDao().insertChild(
            ChildEntity(
                name = name,
                grade = grade,
                avatarUri = avatarUri,
                isActive = true
            )
        )
        seedDefaultSubjectsForChild(newChildId, grade)
        return newChildId
    }

    suspend fun switchActiveChild(childId: Long) {
        db.childDao().clearActiveChild()
        db.childDao().setActiveChild(childId)
        val child = db.childDao().getChildById(childId)
        if (child != null) {
            val existingSubjects = db.subjectDao().getSubjectsForChildSync(childId)
            if (existingSubjects.isEmpty()) {
                seedDefaultSubjectsForChild(childId, child.grade)
            }
        }
    }

    suspend fun updateChild(child: ChildEntity) {
        db.childDao().updateChild(child)
    }

    suspend fun deleteChild(child: ChildEntity) {
        deleteChildById(child.id)
    }

    suspend fun deleteChildById(childId: Long) {
        val wasActive = db.childDao().getChildById(childId)?.isActive == true
        db.subjectDao().deleteSubjectsForChild(childId)
        db.scheduleDao().deleteScheduleForChild(childId)
        db.reviewSessionDao().deleteSessionsForChild(childId)
        db.badgeDao().deleteBadgesForChild(childId)
        db.childDao().deleteChildById(childId)

        if (wasActive) {
            val remaining = db.childDao().getActiveChildSync()
            if (remaining != null) {
                db.childDao().setActiveChild(remaining.id)
            }
        }
    }

    private suspend fun seedDefaultSubjectsForChild(childId: Long, grade: String) {
        val isPrep = grade.contains("تحضيري") || grade.contains("روضة")
        val isSecondary = grade.contains("ثانوي") || grade.contains("BAC")
        val isMiddle = grade.contains("متوسط") || grade.contains("BEM")

        val defaultSubjects = when {
            isPrep -> listOf(
                SubjectEntity(childId = childId, name = "الأنشطة اللغوية والتعبير", icon = "📖", masteryLevel = 4, defaultReviewMinutes = 15),
                SubjectEntity(childId = childId, name = "الأنشطة الحسابية والمنطق", icon = "🔢", masteryLevel = 3, defaultReviewMinutes = 15),
                SubjectEntity(childId = childId, name = "الرسم والأشغال اليدوية", icon = "🎨", masteryLevel = 5, defaultReviewMinutes = 15),
                SubjectEntity(childId = childId, name = "التربية الإسلامية والسلوك", icon = "🕌", masteryLevel = 5, defaultReviewMinutes = 10)
            )
            isSecondary -> listOf(
                SubjectEntity(childId = childId, name = "الرياضيات", icon = "📐", masteryLevel = 3, defaultReviewMinutes = 30),
                SubjectEntity(childId = childId, name = "العلوم الفيزيائية والتكنولوجية", icon = "⚡", masteryLevel = 3, defaultReviewMinutes = 25),
                SubjectEntity(childId = childId, name = "علوم الطبيعة والحياة", icon = "🧪", masteryLevel = 4, defaultReviewMinutes = 25),
                SubjectEntity(childId = childId, name = "اللغة العربية وآدابها", icon = "📖", masteryLevel = 4, defaultReviewMinutes = 20),
                SubjectEntity(childId = childId, name = "اللغة الفرنسية", icon = "🇫🇷", masteryLevel = 3, defaultReviewMinutes = 20),
                SubjectEntity(childId = childId, name = "اللغة الإنجليزية", icon = "🇬🇧", masteryLevel = 4, defaultReviewMinutes = 20)
            )
            isMiddle -> listOf(
                SubjectEntity(childId = childId, name = "اللغة العربية", icon = "📖", masteryLevel = 4, defaultReviewMinutes = 20),
                SubjectEntity(childId = childId, name = "الرياضيات", icon = "📐", masteryLevel = 3, defaultReviewMinutes = 25),
                SubjectEntity(childId = childId, name = "اللغة الفرنسية", icon = "🇫🇷", masteryLevel = 3, defaultReviewMinutes = 20),
                SubjectEntity(childId = childId, name = "العلوم الطبيعية", icon = "🧪", masteryLevel = 4, defaultReviewMinutes = 20),
                SubjectEntity(childId = childId, name = "الفيزياء والتكنولوجيا", icon = "🔬", masteryLevel = 3, defaultReviewMinutes = 20),
                SubjectEntity(childId = childId, name = "اللغة الإنجليزية", icon = "🇬🇧", masteryLevel = 4, defaultReviewMinutes = 20),
                SubjectEntity(childId = childId, name = "التربية الإسلامية", icon = "🕌", masteryLevel = 5, defaultReviewMinutes = 15)
            )
            else -> listOf(
                SubjectEntity(childId = childId, name = "اللغة العربية", icon = "📖", masteryLevel = 4, defaultReviewMinutes = 20),
                SubjectEntity(childId = childId, name = "الرياضيات", icon = "📐", masteryLevel = 3, defaultReviewMinutes = 20),
                SubjectEntity(childId = childId, name = "اللغة الفرنسية (Français)", icon = "🇫🇷", masteryLevel = 3, defaultReviewMinutes = 20),
                SubjectEntity(childId = childId, name = "التربية العلمية والتكنولوجية", icon = "🧪", masteryLevel = 4, defaultReviewMinutes = 15),
                SubjectEntity(childId = childId, name = "التربية الإسلامية", icon = "🕌", masteryLevel = 5, defaultReviewMinutes = 15),
                SubjectEntity(childId = childId, name = "اللغة الإنجليزية", icon = "🇬🇧", masteryLevel = 4, defaultReviewMinutes = 20)
            )
        }

        db.subjectDao().insertSubjects(defaultSubjects)
    }

    suspend fun updateSubjectMastery(subjectId: Long, newMastery: Int, reviewMinutes: Int) {
        val current = db.subjectDao().getSubjectById(subjectId) ?: return
        db.subjectDao().updateSubject(
            current.copy(
                masteryLevel = newMastery,
                defaultReviewMinutes = reviewMinutes
            )
        )
    }

    suspend fun updateSubjectScheduledReminder(subjectId: Long, scheduledTime: String?, enabled: Boolean) {
        val current = db.subjectDao().getSubjectById(subjectId) ?: return
        db.subjectDao().updateSubject(
            current.copy(
                scheduledReviewTime = scheduledTime,
                scheduledReminderEnabled = enabled
            )
        )
    }

    suspend fun addSubject(childId: Long, name: String, icon: String, masteryLevel: Int, reviewMinutes: Int): Long {
        return db.subjectDao().insertSubject(
            SubjectEntity(
                childId = childId,
                name = name,
                icon = icon,
                masteryLevel = masteryLevel,
                defaultReviewMinutes = reviewMinutes
            )
        )
    }

    suspend fun addScheduleEntry(
        childId: Long,
        subjectId: Long,
        dayOfWeek: String,
        startTime: String,
        endTime: String,
        note: String,
        teacher: String,
        classroom: String
    ): Long {
        return db.scheduleDao().insertEntry(
            ScheduleEntryEntity(
                childId = childId,
                subjectId = subjectId,
                dayOfWeek = dayOfWeek,
                startTime = startTime,
                endTime = endTime,
                lessonNote = note,
                teacherName = teacher,
                classroom = classroom
            )
        )
    }

    suspend fun deleteScheduleEntry(id: Long) {
        db.scheduleDao().deleteEntryById(id)
    }

    fun getEarnedBadges(childId: Long): Flow<List<EarnedBadgeEntity>> =
        db.badgeDao().getEarnedBadges(childId)

    fun getBadgesWithProgress(childId: Long): Flow<List<BadgeItem>> {
        return db.badgeDao().getEarnedBadges(childId).combine(
            db.reviewSessionDao().getAllSessions(childId)
        ) { earnedBadges, sessions ->
            val earnedMap = earnedBadges.associateBy { it.badgeCode }
            val subjects = db.subjectDao().getSubjectsForChild(childId).firstOrNull() ?: emptyList()
            val subjectMap = subjects.associateBy { it.id }

            val totalMinutes = sessions.sumOf { it.actualMinutes }
            val arabicSessions = sessions.count { subjectMap[it.subjectId]?.name?.contains("عربية") == true }
            val frenchSessions = sessions.count { subjectMap[it.subjectId]?.name?.contains("فرنسية") == true }
            val mathSessions = sessions.count {
                val name = subjectMap[it.subjectId]?.name ?: ""
                name.contains("رياضيات") || name.contains("حساب") || name.contains("علمي")
            }
            val hasPerfectRating = sessions.any { it.masteryAfter >= 5 }
            val uniqueDatesCount = sessions.map { it.date }.distinct().size

            BadgeCatalog.ALL_DEFINITIONS.map { def ->
                val earned = earnedMap[def.code]
                val isUnlocked = earned != null

                val progress = when (def.code) {
                    "first_step" -> if (sessions.isNotEmpty()) 1 else 0
                    "daily_champion" -> sessions.size.coerceAtMost(def.targetCount)
                    "focus_master" -> if (isUnlocked) 1 else 0
                    "arabic_hero" -> arabicSessions.coerceAtMost(def.targetCount)
                    "french_star" -> frenchSessions.coerceAtMost(def.targetCount)
                    "math_wizard" -> mathSessions.coerceAtMost(def.targetCount)
                    "streak_3" -> uniqueDatesCount.coerceAtMost(def.targetCount)
                    "knowledge_hour" -> totalMinutes.coerceAtMost(def.targetCount)
                    "perfect_mastery" -> if (hasPerfectRating) 1 else 0
                    "night_scholar" -> sessions.size.coerceAtMost(def.targetCount)
                    else -> if (isUnlocked) def.targetCount else 0
                }

                BadgeItem(
                    definition = def,
                    currentProgress = if (isUnlocked) def.targetCount else progress,
                    isUnlocked = isUnlocked,
                    unlockedAt = earned?.unlockedAt
                )
            }
        }
    }

    suspend fun recordReviewSession(
        childId: Long,
        subjectId: Long,
        date: String,
        plannedMinutes: Int,
        actualMinutes: Int,
        rating: Int,
        isFocusMode: Boolean = false
    ): List<BadgeDefinition> {
        db.reviewSessionDao().insertSession(
            ReviewSessionEntity(
                childId = childId,
                subjectId = subjectId,
                date = date,
                plannedMinutes = plannedMinutes,
                actualMinutes = actualMinutes,
                status = "COMPLETED",
                masteryAfter = rating
            )
        )
        // Also update subject mastery rating if child felt excellent or needs review
        val subject = db.subjectDao().getSubjectById(subjectId)
        if (subject != null) {
            db.subjectDao().updateSubject(subject.copy(masteryLevel = rating))
        }

        // Check for eligible badge unlocks
        return checkAndUnlockBadges(childId, subjectId, isFocusMode, rating)
    }

    private suspend fun checkAndUnlockBadges(
        childId: Long,
        subjectId: Long,
        isFocusMode: Boolean,
        rating: Int
    ): List<BadgeDefinition> {
        val earnedCodes = db.badgeDao().getEarnedBadgesSync(childId).map { it.badgeCode }.toSet()
        val allSessions = db.reviewSessionDao().getAllSessions(childId).firstOrNull() ?: emptyList()
        val subjects = db.subjectDao().getSubjectsForChild(childId).firstOrNull() ?: emptyList()
        val subjectMap = subjects.associateBy { it.id }

        val newlyUnlocked = mutableListOf<BadgeDefinition>()
        val nowTimeStr = try {
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        } catch (e: Exception) {
            "اليوم"
        }

        val totalMinutes = allSessions.sumOf { it.actualMinutes }
        val arabicSessions = allSessions.count { subjectMap[it.subjectId]?.name?.contains("عربية") == true }
        val frenchSessions = allSessions.count { subjectMap[it.subjectId]?.name?.contains("فرنسية") == true }
        val mathSessions = allSessions.count {
            val name = subjectMap[it.subjectId]?.name ?: ""
            name.contains("رياضيات") || name.contains("حساب") || name.contains("علمي")
        }
        val uniqueDatesCount = allSessions.map { it.date }.distinct().size

        for (def in BadgeCatalog.ALL_DEFINITIONS) {
            if (earnedCodes.contains(def.code)) continue

            val shouldUnlock = when (def.code) {
                "first_step" -> allSessions.isNotEmpty()
                "daily_champion" -> allSessions.size >= def.targetCount
                "focus_master" -> isFocusMode
                "arabic_hero" -> arabicSessions >= def.targetCount
                "french_star" -> frenchSessions >= def.targetCount
                "math_wizard" -> mathSessions >= def.targetCount
                "streak_3" -> uniqueDatesCount >= def.targetCount
                "knowledge_hour" -> totalMinutes >= def.targetCount
                "perfect_mastery" -> rating >= 5 || allSessions.any { it.masteryAfter >= 5 }
                "night_scholar" -> allSessions.size >= def.targetCount
                else -> false
            }

            if (shouldUnlock) {
                db.badgeDao().insertEarnedBadge(
                    EarnedBadgeEntity(
                        childId = childId,
                        badgeCode = def.code,
                        unlockedAt = nowTimeStr,
                        starsAwarded = def.starsAward
                    )
                )
                newlyUnlocked.add(def)
            }
        }

        return newlyUnlocked
    }

    suspend fun updateSettings(settings: ReminderSettingsEntity) {
        db.reminderSettingsDao().saveSettings(settings)
    }
}
