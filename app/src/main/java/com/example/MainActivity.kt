package com.example

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.achievements.AchievementsScreen
import com.example.ui.components.MuraajaBottomNav
import com.example.ui.components.MuraajaTopBar
import com.example.ui.focus.ParentPinDialog
import com.example.ui.home.HomeScreen
import com.example.ui.onboarding.AddChildScreen
import com.example.ui.review.ReviewSummaryScreen
import com.example.ui.review.ReviewTimerScreen
import com.example.ui.schedule.ScheduleScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.subjects.SubjectsScreen
import com.example.ui.theme.MuraajaTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settings by viewModel.reminderSettings.collectAsState()
            val isDark = settings?.darkMode ?: false

            MuraajaTheme(darkTheme = isDark) {
                // Ensure RTL layout as specified
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    MuraajaApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun MuraajaApp(viewModel: MainViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val activeChild by viewModel.activeChild.collectAsState()
    val isFocusModeActive by viewModel.isFocusModeActive.collectAsState()
    val parentPin by viewModel.parentPin.collectAsState()
    val totalStars by viewModel.totalEarnedStars.collectAsState()
    val allChildren by viewModel.allChildren.collectAsState()

    val context = LocalContext.current
    val activity = context as? Activity
    var showUnlockPinDialog by remember { mutableStateOf(false) }

    // Intercept back button when in Focus Mode on non-timer screens (timer handles its own back)
    BackHandler(enabled = isFocusModeActive && currentScreen !is Screen.ReviewTimer) {
        showUnlockPinDialog = true
    }

    // Screens that should not show standard top/bottom navigation bar (full-screen focus)
    val isFullScreen = currentScreen is Screen.ReviewTimer ||
            currentScreen is Screen.ReviewSummary ||
            currentScreen is Screen.AddChild ||
            currentScreen is Screen.EditProfile

    Scaffold(
        topBar = {
            if (!isFullScreen) {
                MuraajaTopBar(
                    childName = activeChild?.name ?: "أحمد",
                    grade = activeChild?.grade ?: "السنة الثانية ابتدائي",
                    avatarUri = activeChild?.avatarUri,
                    totalStars = totalStars,
                    allChildren = allChildren,
                    onSwitchChild = { id -> viewModel.switchChild(id) },
                    onAddChildClick = { viewModel.navigateTo(Screen.AddChild) },
                    onNotificationClick = { viewModel.navigateTo(Screen.Settings) },
                    onAchievementsClick = { viewModel.navigateTo(Screen.Achievements) },
                    onProfileClick = { viewModel.navigateTo(Screen.EditProfile) },
                    isFocusMode = isFocusModeActive,
                    onUnlockClick = { showUnlockPinDialog = true }
                )
            }
        },
        bottomBar = {
            if (!isFullScreen) {
                MuraajaBottomNav(
                    currentScreen = currentScreen,
                    onNavigate = { viewModel.navigateTo(it) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            when (val screen = currentScreen) {
                is Screen.Home -> {
                    HomeScreen(
                        viewModel = viewModel,
                        onNavigateToTimer = { id, name, mins, icon ->
                            viewModel.navigateTo(
                                Screen.ReviewTimer(
                                    subjectId = id,
                                    subjectName = name,
                                    minutes = mins,
                                    icon = icon
                                )
                            )
                        },
                        onNavigateToSchedule = { viewModel.navigateTo(Screen.Schedule) }
                    )
                }
                is Screen.Schedule -> {
                    ScheduleScreen(viewModel = viewModel)
                }
                is Screen.Achievements -> {
                    AchievementsScreen(viewModel = viewModel)
                }
                is Screen.Subjects -> {
                    SubjectsScreen(viewModel = viewModel)
                }
                is Screen.Settings -> {
                    SettingsScreen(
                        viewModel = viewModel,
                        onNavigateToAddChild = { viewModel.navigateTo(Screen.AddChild) }
                    )
                }
                is Screen.AddChild -> {
                    AddChildScreen(
                        viewModel = viewModel,
                        onBack = { viewModel.navigateTo(Screen.Home) }
                    )
                }
                is Screen.EditProfile -> {
                    com.example.ui.onboarding.EditProfileScreen(
                        viewModel = viewModel,
                        onBack = { viewModel.navigateTo(Screen.Settings) }
                    )
                }
                is Screen.ReviewTimer -> {
                    ReviewTimerScreen(
                        subjectId = screen.subjectId,
                        subjectName = screen.subjectName,
                        totalMinutes = screen.minutes,
                        icon = screen.icon,
                        viewModel = viewModel,
                        onExit = { viewModel.navigateTo(Screen.Home) },
                        onFinished = { actualMins ->
                            viewModel.navigateTo(
                                Screen.ReviewSummary(
                                    subjectId = screen.subjectId,
                                    subjectName = screen.subjectName,
                                    actualMinutes = if (actualMins > 0) actualMins else screen.minutes
                                )
                            )
                        }
                    )
                }
                is Screen.ReviewSummary -> {
                    ReviewSummaryScreen(
                        subjectId = screen.subjectId,
                        subjectName = screen.subjectName,
                        actualMinutes = screen.actualMinutes,
                        viewModel = viewModel,
                        onDone = { viewModel.navigateTo(Screen.Home) }
                    )
                }
                else -> {
                    HomeScreen(
                        viewModel = viewModel,
                        onNavigateToTimer = { id, name, mins, icon ->
                            viewModel.navigateTo(Screen.ReviewTimer(id, name, mins, icon))
                        },
                        onNavigateToSchedule = { viewModel.navigateTo(Screen.Schedule) }
                    )
                }
            }
        }
    }

    if (showUnlockPinDialog) {
        ParentPinDialog(
            correctPin = parentPin,
            title = "فك قفل الهاتف",
            subtitle = "أدخل رمز مرور ولي الأمر لإلغاء وضع التركيز والخروج من التطبيق",
            onDismiss = { showUnlockPinDialog = false },
            onSuccess = {
                viewModel.deactivateFocusMode(activity)
                showUnlockPinDialog = false
            }
        )
    }
}
