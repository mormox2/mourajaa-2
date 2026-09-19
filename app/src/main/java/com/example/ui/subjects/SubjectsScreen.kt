package com.example.ui.subjects

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.SubjectEntity
import com.example.domain.model.MasteryLevel
import com.example.ui.MainViewModel
import com.example.ui.components.SubjectScheduleTimePickerDialog
import com.example.ui.theme.OutlineVariant
import com.example.ui.theme.Primary
import com.example.ui.theme.PrimaryContainer
import com.example.ui.theme.PrimaryFixed
import com.example.ui.theme.Secondary
import com.example.ui.theme.StarGold
import com.example.ui.theme.SurfaceContainer
import com.example.ui.theme.SurfaceContainerLow
import com.example.ui.theme.SurfaceContainerLowest
import com.example.ui.theme.Tertiary
import com.example.ui.theme.TertiaryFixed

@Composable
fun SubjectsScreen(viewModel: MainViewModel) {
    val subjects by viewModel.subjects.collectAsState()

    var subjectToEdit by remember { mutableStateOf<SubjectEntity?>(null) }
    var subjectToSchedule by remember { mutableStateOf<SubjectEntity?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "المواد الدراسية",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(text = "📚", fontSize = 24.sp)
                }
                Text(
                    text = "تحديد مستوى الإتقان وزمن المراجعة الموصى به لكل مادة",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Quick Summary Metrics Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Metric 1
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(PrimaryFixed),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoStories,
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "إجمالي المواد",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${subjects.size} مواد مسجلة",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Metric 2
                val avgMastery = if (subjects.isNotEmpty()) {
                    subjects.map { it.masteryLevel * 20 }.average().toInt()
                } else 80

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(TertiaryFixed),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = Tertiary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "معدل الإتقان",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$avgMastery% جيد جداً",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Tertiary
                            )
                        }
                    }
                }
            }

            // Subjects Cards List
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                subjects.forEach { subject ->
                    val mastery = MasteryLevel.fromLevel(subject.masteryLevel)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { subjectToEdit = subject }
                            .testTag("subject_card_${subject.id}"),
                        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                        shape = RoundedCornerShape(14.dp),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(SurfaceContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = subject.icon, fontSize = 24.sp)
                                    }
                                    Column {
                                        Text(
                                            text = subject.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${mastery.percentage}% - ${mastery.labelAr}",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (mastery.stars >= 4) Tertiary else Primary
                                        )
                                    }
                                }

                                // Rating Stars Display
                                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                    for (i in 1..5) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = if (i <= subject.masteryLevel) StarGold else OutlineVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            // Mastery Progress Bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(SurfaceContainer)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(fraction = (subject.masteryLevel * 0.2f).coerceIn(0.1f, 1f))
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(if (subject.masteryLevel >= 4) Tertiary else Primary)
                                )
                            }

                            // Scheduled Time & Review time row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SurfaceContainerLow)
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Timer,
                                        contentDescription = null,
                                        tint = Primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "المراجعة: ${mastery.recommendedMinutes} د",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                // Scheduled Time Pill Button
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (subject.scheduledReminderEnabled) PrimaryFixed else SurfaceContainer)
                                        .clickable { subjectToSchedule = subject }
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                        .testTag("schedule_time_button_${subject.id}"),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (subject.scheduledReminderEnabled) Icons.Default.NotificationsActive else Icons.Default.AccessTime,
                                        contentDescription = "جدولة التذكير",
                                        tint = if (subject.scheduledReminderEnabled) Primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = if (subject.scheduledReminderEnabled && subject.scheduledReviewTime != null) {
                                            "تذكير: ${subject.scheduledReviewTime}"
                                        } else {
                                            "جدولة تذكير ⏰"
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (subject.scheduledReminderEnabled) Primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(70.dp))
        }

        // Add Subject Floating Action Button
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = PrimaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
                .testTag("fab_add_subject")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "إضافة مادة")
        }
    }

    // Edit Subject Dialog
    if (subjectToEdit != null) {
        val subject = subjectToEdit!!
        var currentRating by remember { mutableIntStateOf(subject.masteryLevel) }
        var reviewMinutes by remember { mutableIntStateOf(subject.defaultReviewMinutes) }

        val level = MasteryLevel.fromLevel(currentRating)

        AlertDialog(
            onDismissRequest = { subjectToEdit = null },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = subject.icon, fontSize = 24.sp)
                    Text(text = subject.name, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "مستوى الإتقان الحالي:",
                        style = MaterialTheme.typography.titleSmall
                    )

                    // 5 Rating Options (Emoticons & Labels)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(
                            1 to "😕 يحتاج مراجعة (30 دقيقة)",
                            2 to "😐 متوسط (25 دقيقة)",
                            3 to "🙂 جيد (20 دقيقة)",
                            4 to "😃 جيد جداً (15 دقيقة)",
                            5 to "🤩 ممتاز (10 دقائق)"
                        ).forEach { (rating, label) ->
                            val isSelected = currentRating == rating
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) PrimaryFixed else SurfaceContainerLowest)
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) Primary else OutlineVariant,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        currentRating = rating
                                        reviewMinutes = MasteryLevel.fromLevel(rating).recommendedMinutes
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Primary else MaterialTheme.colorScheme.onSurface
                                )
                                if (isSelected) {
                                    Text("✓", color = Primary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Text(
                        text = "مدة المراجعة الافتراضية: $reviewMinutes دقيقة",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateSubjectMastery(subject.id, currentRating, reviewMinutes)
                        subjectToEdit = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text("حفظ التعديل")
                }
            },
            dismissButton = {
                TextButton(onClick = { subjectToEdit = null }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Add Subject Dialog
    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var icon by remember { mutableStateOf("📖") }
        var rating by remember { mutableIntStateOf(3) }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("إضافة مادة دراسية جديدة") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("اسم المادة") },
                        placeholder = { Text("مثلاً: التربية الإسلامية") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("رمز المادة:", style = MaterialTheme.typography.labelLarge)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        listOf("📖", "🇫🇷", "🧪", "🇬🇧", "📐", "🕌", "🎨", "⚽").forEach { emoji ->
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (icon == emoji) PrimaryFixed else SurfaceContainer)
                                    .clickable { icon = emoji },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = emoji, fontSize = 20.sp)
                            }
                        }
                    }

                    Text("مستوى الإتقان الأولي:", style = MaterialTheme.typography.labelLarge)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        for (i in 1..5) {
                            IconButton(onClick = { rating = i }) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = if (i <= rating) StarGold else OutlineVariant
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            val recMinutes = MasteryLevel.fromLevel(rating).recommendedMinutes
                            viewModel.addSubject(name, icon, rating, recMinutes)
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text("إضافة")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Material 3 TimePicker Dialog for Subject Review Scheduling
    if (subjectToSchedule != null) {
        val subject = subjectToSchedule!!
        SubjectScheduleTimePickerDialog(
            subjectId = subject.id,
            subjectName = subject.name,
            subjectIcon = subject.icon,
            initialTime = subject.scheduledReviewTime ?: "18:30",
            initialEnabled = subject.scheduledReminderEnabled,
            onDismiss = { subjectToSchedule = null },
            onConfirm = { time, enabled ->
                viewModel.updateSubjectScheduledReminder(subject.id, time, enabled)
                subjectToSchedule = null
            }
        )
    }
}
