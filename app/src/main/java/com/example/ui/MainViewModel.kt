package com.example.ui

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.ChildEntity
import com.example.data.local.entity.ReminderSettingsEntity
import com.example.data.local.entity.ScheduleEntryEntity
import com.example.data.local.entity.SubjectEntity
import com.example.data.repository.MuraajaRepository
import com.example.domain.model.BadgeCategory
import com.example.domain.model.BadgeDefinition
import com.example.domain.model.BadgeItem
import com.example.domain.model.DayOfWeekAr
import com.example.domain.model.MasteryLevel
import com.example.domain.model.NightReviewPlan
import com.example.domain.model.SubjectWithReview
import com.example.ui.focus.FocusModeHelper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import android.graphics.Bitmap
import com.example.data.remote.GeminiOcrService
import com.example.data.remote.OcrResult
import com.example.domain.ocr.ParsedScheduleItem
import com.example.notification.NotificationChannels
import com.example.notification.NotificationScheduler
import java.time.LocalDate
import java.time.format.DateTimeFormatter

sealed class Screen {
    object Splash : Screen()
    object Onboarding : Screen()
    object AddChild : Screen()
    object EditProfile : Screen()
    object Home : Screen()
    object Schedule : Screen()
    object Subjects : Screen()
    object Achievements : Screen()
    object Settings : Screen()
    object Notifications : Screen()
    data class ReviewTimer(val subjectId: Long, val subjectName: String, val minutes: Int, val icon: String) : Screen()
    data class ReviewSummary(val subjectId: Long, val subjectName: String, val actualMinutes: Int) : Screen()
}

sealed class OcrUiState {
    object Idle : OcrUiState()
    data class Scanning(val statusMessage: String) : OcrUiState()
    data class Review(val items: List<ParsedScheduleItem>) : OcrUiState()
    data class Error(val message: String, val isApiKeyMissing: Boolean = false) : OcrUiState()
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application, viewModelScope)
    val repository = MuraajaRepository(db)

    companion object {
        fun getTodayDayOfWeek(): DayOfWeekAr {
            return try {
                when (LocalDate.now().dayOfWeek) {
                    java.time.DayOfWeek.MONDAY -> DayOfWeekAr.MONDAY
                    java.time.DayOfWeek.TUESDAY -> DayOfWeekAr.TUESDAY
                    java.time.DayOfWeek.WEDNESDAY -> DayOfWeekAr.WEDNESDAY
                    java.time.DayOfWeek.THURSDAY -> DayOfWeekAr.THURSDAY
                    java.time.DayOfWeek.FRIDAY -> DayOfWeekAr.FRIDAY
                    java.time.DayOfWeek.SATURDAY -> DayOfWeekAr.MONDAY // Weekend defaults to next school day (Monday)
                    java.time.DayOfWeek.SUNDAY -> DayOfWeekAr.MONDAY
                    else -> DayOfWeekAr.TUESDAY
                }
            } catch (e: Exception) {
                DayOfWeekAr.TUESDAY
            }
        }
    }

    private val _currentScreen = MutableStateFlow<Screen>(Screen.Home)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // Day filter for Schedule screen (defaults to today's school day)
    private val _selectedDay = MutableStateFlow(getTodayDayOfWeek())
    val selectedDay: StateFlow<DayOfWeekAr> = _selectedDay.asStateFlow()

    val activeChild: StateFlow<ChildEntity?> = repository.activeChild
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun currentChildId(): Long = activeChild.value?.id ?: 1L

    val allChildren: StateFlow<List<ChildEntity>> = repository.allChildren
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val subjects: StateFlow<List<SubjectEntity>> = activeChild
        .flatMapLatest { child ->
            val id = child?.id ?: 1L
            repository.getSubjectsForChild(id)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reminderSettings: StateFlow<ReminderSettingsEntity?> = repository.reminderSettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Today's classes based on current date
    private val _currentDay = MutableStateFlow(getTodayDayOfWeek())
    val currentDay: StateFlow<DayOfWeekAr> = _currentDay.asStateFlow()

    private val _nightPlan = MutableStateFlow<NightReviewPlan?>(null)
    val nightPlan: StateFlow<NightReviewPlan?> = _nightPlan.asStateFlow()

    // All schedule entries for active child
    @OptIn(ExperimentalCoroutinesApi::class)
    val allScheduleEntries: StateFlow<List<ScheduleEntryEntity>> = activeChild
        .flatMapLatest { child ->
            val id = child?.id ?: 1L
            repository.getAllScheduleEntries(id)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Completed reviews today count
    @OptIn(ExperimentalCoroutinesApi::class)
    val completedReviewsToday: StateFlow<Int> = activeChild
        .flatMapLatest { child ->
            val id = child?.id ?: 1L
            repository.getCompletedSessionsForDate(id, getTodayDateString())
        }.map { list -> list.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Focus Mode (Lock phone & Pin app) state
    private val _isFocusModeActive = MutableStateFlow(false)
    val isFocusModeActive: StateFlow<Boolean> = _isFocusModeActive.asStateFlow()

    val todayClasses: StateFlow<List<ScheduleEntryEntity>> = combine(allScheduleEntries, _currentDay) { entries, day ->
        entries.filter { it.dayOfWeek.equals(day.key, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tomorrowClasses: StateFlow<List<ScheduleEntryEntity>> = combine(allScheduleEntries, _currentDay) { entries, day ->
        val nextDay = DayOfWeekAr.getNextSchoolDay(day.key)
        entries.filter { it.dayOfWeek.equals(nextDay.key, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val urgentSubjects: StateFlow<List<SubjectEntity>> = subjects.map { list ->
        list.filter { it.masteryLevel <= 2 }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Parent PIN (default "1234")
    val parentPin: StateFlow<String> = reminderSettings
        .combine(MutableStateFlow("1234")) { settings, default ->
            settings?.parentPin ?: default
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "1234")

    fun activateFocusMode(activity: Activity?) {
        FocusModeHelper.enableFocusMode(activity)
        _isFocusModeActive.value = true
    }

    fun deactivateFocusMode(activity: Activity?) {
        FocusModeHelper.disableFocusMode(activity)
        _isFocusModeActive.value = false
    }

    fun updateParentPin(newPin: String) {
        viewModelScope.launch {
            val current = reminderSettings.value ?: ReminderSettingsEntity()
            repository.updateSettings(current.copy(parentPin = newPin))
        }
    }

    init {
        NotificationChannels.createChannels(application)

        viewModelScope.launch {
            activeChild.collect {
                refreshNightReviewPlan()
            }
        }

        viewModelScope.launch {
            reminderSettings.collect { settings ->
                NotificationScheduler.scheduleAllReminders(getApplication(), settings)
            }
        }
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun setSelectedDay(day: DayOfWeekAr) {
        _selectedDay.value = day
    }

    fun setCurrentDay(day: DayOfWeekAr) {
        _currentDay.value = day
        refreshNightReviewPlan()
    }

    fun refreshNightReviewPlan() {
        viewModelScope.launch {
            val plan = repository.calculateNightReviewPlan(currentChildId(), _currentDay.value)
            _nightPlan.value = plan
        }
    }

    fun addScheduleEntry(
        subjectId: Long,
        dayOfWeek: String,
        startTime: String,
        endTime: String,
        note: String,
        teacher: String,
        classroom: String
    ) {
        viewModelScope.launch {
            repository.addScheduleEntry(
                childId = currentChildId(),
                subjectId = subjectId,
                dayOfWeek = dayOfWeek,
                startTime = startTime,
                endTime = endTime,
                note = note,
                teacher = teacher,
                classroom = classroom
            )
            refreshNightReviewPlan()
        }
    }

    fun updateScheduleEntry(
        id: Long,
        subjectId: Long,
        dayOfWeek: String,
        startTime: String,
        endTime: String,
        note: String,
        teacher: String,
        classroom: String
    ) {
        viewModelScope.launch {
            repository.updateScheduleEntry(
                id = id,
                childId = currentChildId(),
                subjectId = subjectId,
                dayOfWeek = dayOfWeek,
                startTime = startTime,
                endTime = endTime,
                note = note,
                teacher = teacher,
                classroom = classroom
            )
            refreshNightReviewPlan()
        }
    }

    fun deleteScheduleEntry(id: Long) {
        viewModelScope.launch {
            repository.deleteScheduleEntry(id)
            refreshNightReviewPlan()
        }
    }

    fun updateSubjectMastery(subjectId: Long, newMastery: Int, reviewMinutes: Int) {
        viewModelScope.launch {
            repository.updateSubjectMastery(subjectId, newMastery, reviewMinutes)
            refreshNightReviewPlan()
        }
    }

    fun updateSubjectScheduledReminder(subjectId: Long, scheduledTime: String?, enabled: Boolean) {
        viewModelScope.launch {
            repository.updateSubjectScheduledReminder(subjectId, scheduledTime, enabled)
            val sub = subjects.value.find { it.id == subjectId }
            val subName = sub?.name ?: "المادة"
            if (scheduledTime != null) {
                NotificationScheduler.scheduleSubjectReminder(
                    context = getApplication(),
                    subjectId = subjectId,
                    subjectName = subName,
                    timeStr = scheduledTime,
                    enabled = enabled
                )
            }
            refreshNightReviewPlan()
        }
    }

    fun addSubject(name: String, icon: String, masteryLevel: Int, reviewMinutes: Int) {
        viewModelScope.launch {
            repository.addSubject(
                childId = currentChildId(),
                name = name,
                icon = icon,
                masteryLevel = masteryLevel,
                reviewMinutes = reviewMinutes
            )
            refreshNightReviewPlan()
        }
    }

    // Badges & Rewards System
    @OptIn(ExperimentalCoroutinesApi::class)
    val badges: StateFlow<List<BadgeItem>> = activeChild
        .flatMapLatest { child ->
            val id = child?.id ?: 1L
            repository.getBadgesWithProgress(id)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalEarnedStars: StateFlow<Int> = badges.map { list ->
        list.filter { it.isUnlocked }.sumOf { it.definition.starsAward }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 150)

    val unlockedBadgesCount: StateFlow<Int> = badges.map { list ->
        list.count { it.isUnlocked }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 2)

    private val _celebrationBadge = MutableStateFlow<BadgeDefinition?>(null)
    val celebrationBadge: StateFlow<BadgeDefinition?> = _celebrationBadge.asStateFlow()

    fun dismissCelebrationBadge() {
        _celebrationBadge.value = null
    }

    fun finishReviewSession(subjectId: Long, actualMinutes: Int, rating: Int, isFocusMode: Boolean = false) {
        viewModelScope.launch {
            val newlyUnlocked = repository.recordReviewSession(
                childId = currentChildId(),
                subjectId = subjectId,
                date = getTodayDateString(),
                plannedMinutes = actualMinutes,
                actualMinutes = actualMinutes,
                rating = rating,
                isFocusMode = isFocusMode
            )
            if (newlyUnlocked.isNotEmpty()) {
                _celebrationBadge.value = newlyUnlocked.first()
            }
            refreshNightReviewPlan()
        }
    }


    fun updateSettings(settings: ReminderSettingsEntity) {
        viewModelScope.launch {
            repository.updateSettings(settings)
            NotificationScheduler.scheduleAllReminders(getApplication(), settings)
        }
    }

    fun sendTestNotification() {
        NotificationScheduler.sendImmediateTestNotification(getApplication())
    }

    fun updateReminderTimes(
        morningTime: String? = null,
        previousDayTime: String? = null,
        reviewTime: String? = null
    ) {
        viewModelScope.launch {
            val current = reminderSettings.value ?: ReminderSettingsEntity()
            val updated = current.copy(
                morningTime = morningTime ?: current.morningTime,
                previousDayTime = previousDayTime ?: current.previousDayTime,
                reviewTime = reviewTime ?: current.reviewTime
            )
            repository.updateSettings(updated)
            NotificationScheduler.scheduleAllReminders(getApplication(), updated)
        }
    }

    fun saveNewChild(name: String, grade: String, avatarUri: String? = null) {
        viewModelScope.launch {
            repository.saveChild(name, grade, avatarUri)
            refreshNightReviewPlan()
            _currentScreen.value = Screen.Home
        }
    }

    fun switchChild(childId: Long) {
        viewModelScope.launch {
            repository.switchActiveChild(childId)
            refreshNightReviewPlan()
        }
    }

    fun deleteChild(childId: Long) {
        viewModelScope.launch {
            repository.deleteChildById(childId)
            refreshNightReviewPlan()
        }
    }

    fun updateChildProfile(child: ChildEntity) {
        viewModelScope.launch {
            repository.updateChild(child)
            _currentScreen.value = Screen.Settings
        }
    }

    fun updateActiveChildAvatar(avatarUri: String?) {
        viewModelScope.launch {
            val current = activeChild.value ?: return@launch
            repository.updateChild(current.copy(avatarUri = avatarUri))
        }
    }

    // --- OCR Timetable Scanner Engine ---
    private val geminiOcrService = GeminiOcrService()

    private val _ocrState = MutableStateFlow<OcrUiState>(OcrUiState.Idle)
    val ocrState: StateFlow<OcrUiState> = _ocrState.asStateFlow()

    fun scanScheduleImage(bitmap: Bitmap) {
        val configuredKey = reminderSettings.value?.geminiApiKey?.takeIf { it.isNotBlank() }
            ?: GeminiOcrService.getBuildConfigApiKey()

        if (configuredKey.isNullOrBlank()) {
            _ocrState.value = OcrUiState.Error(
                message = "مفتاح الذكاء الاصطناعي (Gemini API) غير محدد. يرجى إدخال المفتاح للبدء في مسح الجدول.",
                isApiKeyMissing = true
            )
            return
        }

        _ocrState.value = OcrUiState.Scanning("جاري تحليل جدول المدرسة بالذكاء الاصطناعي...")
        viewModelScope.launch {
            val result = geminiOcrService.analyzeTimetableImage(
                bitmap = bitmap,
                apiKey = configuredKey,
                existingSubjects = subjects.value
            )
            when (result) {
                is OcrResult.Success -> {
                    _ocrState.value = OcrUiState.Review(result.items)
                }
                is OcrResult.Error -> {
                    _ocrState.value = OcrUiState.Error(result.message, result.isApiKeyMissing)
                }
            }
        }
    }

    fun confirmOcrImport(selectedItems: List<ParsedScheduleItem>, replaceExisting: Boolean) {
        viewModelScope.launch {
            repository.importScheduleBatch(
                childId = currentChildId(),
                items = selectedItems,
                replaceExisting = replaceExisting
            )
            refreshNightReviewPlan()
            _ocrState.value = OcrUiState.Idle
        }
    }

    fun saveGeminiApiKey(apiKey: String) {
        viewModelScope.launch {
            repository.updateGeminiApiKey(apiKey.trim())
        }
    }

    fun resetOcrState() {
        _ocrState.value = OcrUiState.Idle
    }

    private fun getTodayDateString(): String {
        return try {
            LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        } catch (e: Exception) {
            "2026-09-22"
        }
    }
}
