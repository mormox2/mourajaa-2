package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.ChildEntity
import com.example.data.local.entity.EarnedBadgeEntity
import com.example.data.local.entity.ReminderSettingsEntity
import com.example.data.local.entity.ReviewSessionEntity
import com.example.data.local.entity.ScheduleEntryEntity
import com.example.data.local.entity.SubjectEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChildDao {
    @Query("SELECT * FROM children ORDER BY id ASC")
    fun getAllChildren(): Flow<List<ChildEntity>>

    @Query("SELECT * FROM children WHERE id = :id LIMIT 1")
    suspend fun getChildById(id: Long): ChildEntity?

    @Query("SELECT * FROM children ORDER BY id ASC LIMIT 1")
    fun getActiveChild(): Flow<ChildEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChild(child: ChildEntity): Long

    @Update
    suspend fun updateChild(child: ChildEntity)

    @Delete
    suspend fun deleteChild(child: ChildEntity)
}

@Dao
interface SubjectDao {
    @Query("SELECT * FROM subjects WHERE childId = :childId ORDER BY id ASC")
    fun getSubjectsForChild(childId: Long): Flow<List<SubjectEntity>>

    @Query("SELECT * FROM subjects WHERE id = :id LIMIT 1")
    suspend fun getSubjectById(id: Long): SubjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: SubjectEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubjects(subjects: List<SubjectEntity>)

    @Update
    suspend fun updateSubject(subject: SubjectEntity)

    @Delete
    suspend fun deleteSubject(subject: SubjectEntity)
}

@Dao
interface ScheduleDao {
    @Query("SELECT * FROM schedule_entries WHERE childId = :childId ORDER BY startTime ASC")
    fun getAllEntries(childId: Long): Flow<List<ScheduleEntryEntity>>

    @Query("SELECT * FROM schedule_entries WHERE childId = :childId AND dayOfWeek = :dayOfWeek ORDER BY startTime ASC")
    fun getEntriesForDay(childId: Long, dayOfWeek: String): Flow<List<ScheduleEntryEntity>>

    @Query("SELECT * FROM schedule_entries WHERE childId = :childId AND dayOfWeek = :dayOfWeek ORDER BY startTime ASC")
    suspend fun getEntriesForDaySync(childId: Long, dayOfWeek: String): List<ScheduleEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: ScheduleEntryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntries(entries: List<ScheduleEntryEntity>)

    @Update
    suspend fun updateEntry(entry: ScheduleEntryEntity)

    @Delete
    suspend fun deleteEntry(entry: ScheduleEntryEntity)

    @Query("DELETE FROM schedule_entries WHERE id = :id")
    suspend fun deleteEntryById(id: Long)
}

@Dao
interface ReviewSessionDao {
    @Query("SELECT * FROM review_sessions WHERE childId = :childId ORDER BY id DESC")
    fun getAllSessions(childId: Long): Flow<List<ReviewSessionEntity>>

    @Query("SELECT * FROM review_sessions WHERE childId = :childId AND date = :date")
    fun getSessionsForDate(childId: Long, date: String): Flow<List<ReviewSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ReviewSessionEntity): Long
}

@Dao
interface ReminderSettingsDao {
    @Query("SELECT * FROM reminder_settings WHERE id = 1 LIMIT 1")
    fun getSettings(): Flow<ReminderSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: ReminderSettingsEntity)
}

@Dao
interface BadgeDao {
    @Query("SELECT * FROM earned_badges WHERE childId = :childId ORDER BY id DESC")
    fun getEarnedBadges(childId: Long): Flow<List<EarnedBadgeEntity>>

    @Query("SELECT * FROM earned_badges WHERE childId = :childId ORDER BY id DESC")
    suspend fun getEarnedBadgesSync(childId: Long): List<EarnedBadgeEntity>

    @Query("SELECT COUNT(*) FROM earned_badges WHERE childId = :childId AND badgeCode = :badgeCode")
    suspend fun isBadgeEarned(childId: Long, badgeCode: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEarnedBadge(badge: EarnedBadgeEntity): Long
}

