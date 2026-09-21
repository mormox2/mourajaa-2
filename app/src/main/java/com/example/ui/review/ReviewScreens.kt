package com.example.ui.review

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.focus.ParentPinDialog
import com.example.ui.theme.ErrorColor
import com.example.ui.theme.Primary
import com.example.ui.theme.PrimaryContainer
import com.example.ui.theme.PrimaryFixed
import com.example.ui.theme.Secondary
import com.example.ui.theme.SecondaryFixed
import com.example.ui.theme.SurfaceContainer
import com.example.ui.theme.SurfaceContainerHigh
import com.example.ui.theme.SurfaceContainerLow
import com.example.ui.theme.SurfaceContainerLowest
import com.example.ui.theme.Tertiary
import com.example.ui.theme.TertiaryFixed
import kotlinx.coroutines.delay

@Composable
fun ReviewTimerScreen(
    subjectId: Long,
    subjectName: String,
    totalMinutes: Int,
    icon: String,
    viewModel: MainViewModel,
    onExit: () -> Unit,
    onFinished: (Int) -> Unit
) {
    var remainingSeconds by remember { mutableIntStateOf(totalMinutes * 60) }
    var isRunning by remember { mutableStateOf(true) }

    val isFocusModeActive by viewModel.isFocusModeActive.collectAsState()
    val parentPin by viewModel.parentPin.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity

    var showUnlockPinDialog by remember { mutableStateOf(false) }
    var actionAfterUnlock by remember { mutableStateOf<(() -> Unit)?>(null) }

    val handleExit = {
        if (isFocusModeActive) {
            actionAfterUnlock = onExit
            showUnlockPinDialog = true
        } else {
            onExit()
        }
    }

    BackHandler {
        handleExit()
    }

    // Count down effect
    LaunchedEffect(isRunning, remainingSeconds) {
        if (isRunning && remainingSeconds > 0) {
            delay(1000L)
            remainingSeconds--
        } else if (remainingSeconds <= 0) {
            onFinished(totalMinutes)
        }
    }

    val totalSeconds = totalMinutes * 60
    val progress = (remainingSeconds.toFloat() / totalSeconds.toFloat()).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "progress")

    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 1. Top Screen Header
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Exit button
                TextButton(
                    onClick = handleExit,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(SurfaceContainerLowest)
                        .padding(horizontal = 6.dp)
                        .testTag("timer_exit_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "خروج",
                        tint = Primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = "خروج",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Active session badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(PrimaryFixed)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(PrimaryContainer)
                        )
                        Text(
                            text = "جلسة تركيز نشطة",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                    }
                }

                IconButton(
                    onClick = { /* Soft ambient sounds */ },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(SurfaceContainerLowest)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "صوت",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Current Module Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(text = icon, fontSize = 28.sp)
                        Column {
                            Text(
                                text = subjectName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "جلسة المراجعة المسائية • تركيز كامل",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(SurfaceContainer)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "مستمر",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                    }
                }
            }

            // Focus Mode (Lock Phone) Button
            Button(
                onClick = {
                    if (isFocusModeActive) {
                        actionAfterUnlock = null
                        showUnlockPinDialog = true
                    } else {
                        viewModel.activateFocusMode(activity)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFocusModeActive) Primary else PrimaryFixed
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("timer_focus_mode_toggle_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (isFocusModeActive) Icons.Default.LockOpen else Icons.Default.Lock,
                        contentDescription = null,
                        tint = if (isFocusModeActive) Color.White else Primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = if (isFocusModeActive)
                            "وضع التركيز نشط 🔒 • إلغاء القفل برمز ولي الأمر 🔓"
                        else
                            "قفل الهاتف وتثبيت التطبيق (وضع التركيز) 🔒",
                        fontWeight = FontWeight.Bold,
                        color = if (isFocusModeActive) Color.White else Primary,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // 2. Center Circular Timer & Metrics
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(240.dp)
            ) {
                // Circular Progress Canvas
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Track background
                    drawCircle(
                        color = SurfaceContainerHigh,
                        radius = size.minDimension / 2 - 12.dp.toPx(),
                        style = Stroke(width = 12.dp.toPx())
                    )
                    // Active indicator arc
                    drawArc(
                        color = PrimaryContainer,
                        startAngle = -90f,
                        sweepAngle = 360f * animatedProgress,
                        useCenter = false,
                        style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Digital countdown readout
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "الوقت المتبقي",
                        style = MaterialTheme.typography.labelMedium,
                        color = Primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = timeFormatted,
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "من $totalMinutes:00 دقيقة",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Box(
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceContainerHigh)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        val percentageLeft = (progress * 100).toInt()
                        Text(
                            text = "متبقي $percentageLeft%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Controls: Stop, Pause/Play, +5 Min
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Stop Button
                IconButton(
                    onClick = handleExit,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(SurfaceContainerLowest)
                        .testTag("timer_stop_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "إنهاء",
                        tint = ErrorColor,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.size(20.dp))

                // Play / Pause Button
                IconButton(
                    onClick = { isRunning = !isRunning },
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(PrimaryContainer)
                        .testTag("timer_pause_play_button")
                ) {
                    Icon(
                        imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isRunning) "إيقاف مؤقت" else "متابعة",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.size(20.dp))

                // +5 Minutes quick add
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(SurfaceContainerLowest)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    TextButton(onClick = { remainingSeconds += 300 }) {
                        Text(
                            text = "+5د",
                            fontWeight = FontWeight.Bold,
                            color = Secondary,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        // 3. Focus Guidance & Next Step
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Calm advice note
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SecondaryFixed),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "همسة تركيز: ركّز في هذه الفترة وحاول ألا تستخدم الهاتف أو تتشتت.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Next step CTA
            Button(
                onClick = { onFinished(totalMinutes - (remainingSeconds / 60)) },
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("complete_session_button")
            ) {
                Text(
                    text = "إنهاء جلسة المراجعة الآن ✓",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }

    if (showUnlockPinDialog) {
        ParentPinDialog(
            correctPin = parentPin,
            title = "فك قفل الهاتف",
            subtitle = "أدخل رمز مرور ولي الأمر لإنهاء وضع التركيز",
            onDismiss = { showUnlockPinDialog = false },
            onSuccess = {
                viewModel.deactivateFocusMode(activity)
                showUnlockPinDialog = false
                actionAfterUnlock?.invoke()
            }
        )
    }
}

@Composable
fun ReviewSummaryScreen(
    subjectId: Long,
    subjectName: String,
    actualMinutes: Int,
    viewModel: MainViewModel,
    onDone: () -> Unit
) {
    var selectedRating by remember { mutableIntStateOf(4) }
    val isFocusModeActive by viewModel.isFocusModeActive.collectAsState()
    val celebrationBadge by viewModel.celebrationBadge.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(top = 24.dp)
        ) {
            Text(text = "🎉", fontSize = 48.sp)
            Text(
                text = "أحسنت يا بطل!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "لقد أتممت مراجعة $subjectName بنجاح.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(PrimaryFixed)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "المدة المنجزة: $actualMinutes دقيقة",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceContainerLow)
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "+50 ⭐ مكافأة",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Secondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "كيف كانت المراجعة؟",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Rating options (4 emojis)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                listOf(
                    1 to ("😕" to "صعبة"),
                    2 to ("😐" to "عادية"),
                    4 to ("🙂" to "جيدة"),
                    5 to ("😃" to "ممتازة")
                ).forEach { (rating, pair) ->
                    val (emoji, label) = pair
                    val isSelected = selectedRating == rating
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) PrimaryFixed else SurfaceContainerLowest)
                            .padding(12.dp)
                    ) {
                        TextButton(onClick = { selectedRating = rating }) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = emoji, fontSize = 32.sp)
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // Confirm button
        Button(
            onClick = {
                viewModel.finishReviewSession(
                    subjectId = subjectId,
                    actualMinutes = actualMinutes,
                    rating = selectedRating,
                    isFocusMode = isFocusModeActive
                )
                // If no instant badge unlocked, proceed
                if (celebrationBadge == null) {
                    onDone()
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Primary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("finish_review_summary_button")
        ) {
            Text(
                text = "حفظ وإنهاء الجلسة 🎉",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }

    // Badge Unlocked Celebration Dialog
    celebrationBadge?.let { def ->
        androidx.compose.ui.window.Dialog(
            onDismissRequest = {
                viewModel.dismissCelebrationBadge()
                onDone()
            }
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(PrimaryFixed)
                            .border(3.dp, Primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = def.icon, fontSize = 48.sp)
                    }

                    Text(
                        text = "🏆 وسام جديد ومستحق!",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )

                    Text(
                        text = def.titleAr,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = def.descriptionAr,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(PrimaryFixed)
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "+${def.starsAward} نجمة تميز إضافية ⭐",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                viewModel.dismissCelebrationBadge()
                                onDone()
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("العودة للرئيسية")
                        }
                        Button(
                            onClick = {
                                viewModel.dismissCelebrationBadge()
                                viewModel.navigateTo(Screen.Achievements)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Primary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("عرض الأوسمة 🏆")
                        }
                    }
                }
            }
        }
    }
}
