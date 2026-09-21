package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.BadgeDao
import com.example.data.local.dao.ChildDao
import com.example.data.local.dao.ReminderSettingsDao
import com.example.data.local.dao.ReviewSessionDao
import com.example.data.local.dao.ScheduleDao
import com.example.data.local.dao.SubjectDao
import com.example.data.local.entity.ChildEntity
import com.example.data.local.entity.EarnedBadgeEntity
import com.example.data.local.entity.ReminderSettingsEntity
import com.example.data.local.entity.ReviewSessionEntity
import com.example.data.local.entity.ScheduleEntryEntity
import com.example.data.local.entity.SubjectEntity
import androidx.room.migration.Migration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ChildEntity::class,
        SubjectEntity::class,
        ScheduleEntryEntity::class,
        ReviewSessionEntity::class,
        ReminderSettingsEntity::class,
        EarnedBadgeEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun childDao(): ChildDao
    abstract fun subjectDao(): SubjectDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun reviewSessionDao(): ReviewSessionDao
    abstract fun reminderSettingsDao(): ReminderSettingsDao
    abstract fun badgeDao(): BadgeDao

    companion object {
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE children ADD COLUMN isActive INTEGER NOT NULL DEFAULT 0")
                db.execSQL("UPDATE children SET isActive = 1 WHERE id = (SELECT id FROM children ORDER BY id ASC LIMIT 1)")
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "muraaja_database"
                )
                    .addMigrations(MIGRATION_3_4)
                    .fallbackToDestructiveMigration(true)
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }
        }

        suspend fun populateInitialData(db: AppDatabase) {
            val childDao = db.childDao()
            val subjectDao = db.subjectDao()
            val scheduleDao = db.scheduleDao()
            val settingsDao = db.reminderSettingsDao()

            // 1. Initial Child: Ahmed
            val childId = childDao.insertChild(
                ChildEntity(
                    id = 1,
                    name = "أحمد",
                    grade = "السنة الثانية ابتدائي",
                    isActive = true
                )
            )

            // 2. Initial Subjects (Arabic, French, Science, English) with mastery levels
            val subArabicId = subjectDao.insertSubject(
                SubjectEntity(
                    id = 1,
                    childId = childId,
                    name = "اللغة العربية",
                    icon = "📖",
                    defaultReviewMinutes = 20,
                    masteryLevel = 4 // 80% جيد جداً -> 15-20 mins
                )
            )
            val subFrenchId = subjectDao.insertSubject(
                SubjectEntity(
                    id = 2,
                    childId = childId,
                    name = "اللغة الفرنسية (Français)",
                    icon = "🇫🇷",
                    defaultReviewMinutes = 20,
                    masteryLevel = 3 // 60% متوسط -> 25 mins
                )
            )
            val subScienceId = subjectDao.insertSubject(
                SubjectEntity(
                    id = 3,
                    childId = childId,
                    name = "العلوم العامة",
                    icon = "🧪",
                    defaultReviewMinutes = 15,
                    masteryLevel = 5 // 100% ممتاز -> 10-15 mins
                )
            )
            val subEnglishId = subjectDao.insertSubject(
                SubjectEntity(
                    id = 4,
                    childId = childId,
                    name = "اللغة الإنجليزية",
                    icon = "🇬🇧",
                    defaultReviewMinutes = 20,
                    masteryLevel = 4 // 80% جيد جداً
                )
            )

            // 3. Weekly School Schedule from specs:
            // الإثنين: 09:00 إنجليزية، 10:00 عربية
            scheduleDao.insertEntry(
                ScheduleEntryEntity(
                    childId = childId,
                    subjectId = subEnglishId,
                    dayOfWeek = "MONDAY",
                    startTime = "09:00",
                    endTime = "10:00",
                    lessonNote = "Grammar & Vocabulary",
                    teacherName = "Mr. Smith",
                    classroom = "Lab 1"
                )
            )
            scheduleDao.insertEntry(
                ScheduleEntryEntity(
                    childId = childId,
                    subjectId = subArabicId,
                    dayOfWeek = "MONDAY",
                    startTime = "10:00",
                    endTime = "11:00",
                    lessonNote = "قراءة وتعبير",
                    teacherName = "أ. المنصوري",
                    classroom = "4B"
                )
            )

            // الثلاثاء: 09:00 عربية، 10:00 فرنسية
            scheduleDao.insertEntry(
                ScheduleEntryEntity(
                    childId = childId,
                    subjectId = subArabicId,
                    dayOfWeek = "TUESDAY",
                    startTime = "09:00",
                    endTime = "10:00",
                    lessonNote = "نص «في حديقة الحيّ» + كراس التمارين ص 14",
                    teacherName = "أ. أحمد المنصوري",
                    classroom = "4B (الطابق الأول)"
                )
            )
            scheduleDao.insertEntry(
                ScheduleEntryEntity(
                    childId = childId,
                    subjectId = subFrenchId,
                    dayOfWeek = "TUESDAY",
                    startTime = "10:00",
                    endTime = "11:00",
                    lessonNote = "Apprentissage de la comptine + vocabulaire",
                    teacherName = "أ. مريم الشريف",
                    classroom = "مختبر اللغات 02"
                )
            )

            // الأربعاء: 09:00 فرنسية، 10:00 عربية
            scheduleDao.insertEntry(
                ScheduleEntryEntity(
                    childId = childId,
                    subjectId = subFrenchId,
                    dayOfWeek = "WEDNESDAY",
                    startTime = "09:00",
                    endTime = "10:00",
                    lessonNote = "إحضار دفتر الأنشطة الملون",
                    teacherName = "أ. مريم الشريف",
                    classroom = "مختبر اللغات 02"
                )
            )
            scheduleDao.insertEntry(
                ScheduleEntryEntity(
                    childId = childId,
                    subjectId = subArabicId,
                    dayOfWeek = "WEDNESDAY",
                    startTime = "10:00",
                    endTime = "11:00",
                    lessonNote = "تسميع المقطوعة الإنشادية",
                    teacherName = "أ. أحمد المنصوري",
                    classroom = "4B"
                )
            )

            // الخميس: 09:00 علوم، 10:00 عربية
            scheduleDao.insertEntry(
                ScheduleEntryEntity(
                    childId = childId,
                    subjectId = subScienceId,
                    dayOfWeek = "THURSDAY",
                    startTime = "09:00",
                    endTime = "10:00",
                    lessonNote = "دورة حياة النباتات",
                    teacherName = "أ. فتحي",
                    classroom = "معمل العلوم"
                )
            )
            scheduleDao.insertEntry(
                ScheduleEntryEntity(
                    childId = childId,
                    subjectId = subArabicId,
                    dayOfWeek = "THURSDAY",
                    startTime = "10:00",
                    endTime = "11:00",
                    lessonNote = "إملاء وتمارين خط",
                    teacherName = "أ. أحمد المنصوري",
                    classroom = "4B"
                )
            )

            // الجمعة: 09:00 عربية، 10:00 علوم
            scheduleDao.insertEntry(
                ScheduleEntryEntity(
                    childId = childId,
                    subjectId = subArabicId,
                    dayOfWeek = "FRIDAY",
                    startTime = "09:00",
                    endTime = "10:00",
                    lessonNote = "مراجعة شاملة للقصة",
                    teacherName = "أ. أحمد المنصوري",
                    classroom = "4B"
                )
            )
            scheduleDao.insertEntry(
                ScheduleEntryEntity(
                    childId = childId,
                    subjectId = subScienceId,
                    dayOfWeek = "FRIDAY",
                    startTime = "10:00",
                    endTime = "11:00",
                    lessonNote = "تطبيق عملي لمكونات التربة",
                    teacherName = "أ. فتحي",
                    classroom = "معمل العلوم"
                )
            )

            // 4. Default Settings
            settingsDao.saveSettings(
                ReminderSettingsEntity(
                    id = 1,
                    previousDayEnabled = true,
                    previousDayTime = "19:00",
                    morningEnabled = true,
                    morningTime = "07:30",
                    reviewTimeEnabled = true,
                    reviewTime = "19:30",
                    defaultReviewMinutes = 20,
                    darkMode = false,
                    parentPin = "1234",
                    focusModePinRequired = true
                )
            )

            // 5. Initial Earned Badges for child
            val badgeDao = db.badgeDao()
            badgeDao.insertEarnedBadge(
                EarnedBadgeEntity(
                    childId = childId,
                    badgeCode = "first_step",
                    unlockedAt = "منذ يومين",
                    starsAwarded = 50
                )
            )
            badgeDao.insertEarnedBadge(
                EarnedBadgeEntity(
                    childId = childId,
                    badgeCode = "focus_master",
                    unlockedAt = "أمس في 19:45",
                    starsAwarded = 100
                )
            )

            // 6. Initial Completed Review Sessions
            val reviewSessionDao = db.reviewSessionDao()
            reviewSessionDao.insertSession(
                ReviewSessionEntity(
                    childId = childId,
                    subjectId = subArabicId,
                    date = "2026-09-17",
                    plannedMinutes = 20,
                    actualMinutes = 20,
                    status = "COMPLETED",
                    masteryAfter = 5
                )
            )
            reviewSessionDao.insertSession(
                ReviewSessionEntity(
                    childId = childId,
                    subjectId = subFrenchId,
                    date = "2026-09-18",
                    plannedMinutes = 25,
                    actualMinutes = 25,
                    status = "COMPLETED",
                    masteryAfter = 4
                )
            )
        }
    }
}
