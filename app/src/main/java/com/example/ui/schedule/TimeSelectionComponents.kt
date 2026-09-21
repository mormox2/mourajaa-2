package com.example.ui.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.OutlineVariant
import com.example.ui.theme.Primary
import com.example.ui.theme.PrimaryContainer
import com.example.ui.theme.PrimaryFixed
import com.example.ui.theme.SurfaceContainer
import com.example.ui.theme.SurfaceContainerLow
import com.example.ui.theme.SurfaceContainerLowest

object ScheduleTimeUtils {
    fun parseTimeToMinutes(time: String): Int {
        val parts = time.split(":")
        val hour = parts.getOrNull(0)?.trim()?.toIntOrNull() ?: 0
        val minute = parts.getOrNull(1)?.trim()?.toIntOrNull() ?: 0
        return (hour * 60 + minute).coerceIn(0, 1439)
    }

    fun formatMinutesToTime(totalMinutes: Int): String {
        val normalized = ((totalMinutes % 1440) + 1440) % 1440
        val hour = normalized / 60
        val minute = normalized % 60
        return String.format("%02d:%02d", hour, minute)
    }

    fun addMinutes(time: String, minutesToAdd: Int): String {
        val currentMinutes = parseTimeToMinutes(time)
        return formatMinutesToTime(currentMinutes + minutesToAdd)
    }

    fun calculateDurationMinutes(start: String, end: String): Int {
        val startMin = parseTimeToMinutes(start)
        val endMin = parseTimeToMinutes(end)
        return if (endMin >= startMin) {
            endMin - startMin
        } else {
            (1440 - startMin) + endMin
        }
    }

    fun formatDurationArabic(minutes: Int): String {
        val hours = minutes / 60
        val mins = minutes % 60
        return when {
            hours > 0 && mins > 0 -> "$hours ساعة و $mins دقيقة"
            hours == 1 -> "ساعة واحدة"
            hours == 2 -> "ساعتان"
            hours > 2 -> "$hours ساعات"
            mins > 0 -> "$mins دقيقة"
            else -> "0 دقيقة"
        }
    }

    val standardSchoolSlots = listOf(
        "08:00" to "09:00",
        "09:00" to "10:00",
        "10:00" to "11:00",
        "11:00" to "12:00",
        "13:00" to "14:00",
        "14:00" to "15:00",
        "15:00" to "16:00",
        "16:00" to "17:00"
    )

    val quickDurations = listOf(
        30 to "+30 د",
        45 to "+45 د",
        60 to "+1 ساعة",
        90 to "+1:30 س",
        120 to "+2 س"
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleTimePickerDialog(
    title: String,
    initialTime: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val initialMinutes = ScheduleTimeUtils.parseTimeToMinutes(initialTime)
    val initialHour = initialMinutes / 60
    val initialMinute = initialMinutes % 60

    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 16.dp)
                .testTag("schedule_time_picker_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(PrimaryFixed),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إغلاق",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // TimePicker Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceContainerLowest)
                        .border(1.dp, OutlineVariant, RoundedCornerShape(16.dp))
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    TimePicker(
                        state = timePickerState,
                        colors = TimePickerDefaults.colors(
                            clockDialColor = SurfaceContainer,
                            selectorColor = Primary,
                            containerColor = SurfaceContainerLowest,
                            timeSelectorSelectedContainerColor = PrimaryFixed,
                            timeSelectorUnselectedContainerColor = SurfaceContainerLow,
                            timeSelectorSelectedContentColor = Primary,
                            timeSelectorUnselectedContentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.testTag("m3_time_picker")
                    )
                }

                // Preview Selected Time
                val selectedFormatted = String.format(
                    "%02d:%02d",
                    timePickerState.hour,
                    timePickerState.minute
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceContainerLow)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "الوقت المختار:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = selectedFormatted,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("إلغاء")
                    }

                    Button(
                        onClick = { onConfirm(selectedFormatted) },
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("confirm_time_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("تأكيد الوقت", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun TimeRangeSelectorSection(
    startTime: String,
    endTime: String,
    onTimeRangeChanged: (newStart: String, newEnd: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var pickerTarget by remember { mutableStateOf<String?>(null) } // "start" or "end"

    val durationMinutes = ScheduleTimeUtils.calculateDurationMinutes(startTime, endTime)
    val durationText = ScheduleTimeUtils.formatDurationArabic(durationMinutes)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "توقيت الحصة",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "المدة: $durationText",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = Primary
            )
        }

        // 1. Standard School Slots (Quick 1-click selection)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "الحصص المدرسية الشائعة (اختر بنقرة واحدة):",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ScheduleTimeUtils.standardSchoolSlots.forEach { (slotStart, slotEnd) ->
                    val isSelected = startTime == slotStart && endTime == slotEnd
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) PrimaryContainer else SurfaceContainerLowest)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) Primary else OutlineVariant,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { onTimeRangeChanged(slotStart, slotEnd) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("slot_${slotStart}_$slotEnd"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$slotStart - $slotEnd",
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // 2. Interactive Start and End Time Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Start Time Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { pickerTarget = "start" }
                    .testTag("card_start_time"),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, OutlineVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "من (البداية)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "تعديل وقت البداية",
                            tint = Primary,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                    Text(
                        text = startTime,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                    Text(
                        text = "اضغط لتغيير الوقت",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            // End Time Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { pickerTarget = "end" }
                    .testTag("card_end_time"),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, OutlineVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "إلى (النهاية)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "تعديل وقت النهاية",
                            tint = Primary,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                    Text(
                        text = endTime,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                    Text(
                        text = "اضغط لتغيير الوقت",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }

        // 3. Quick Duration Adjustment Chips (+30m, +45m, +1h, +1.5h, +2h)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "تعديل المدة سريعاً من وقت البداية:",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ScheduleTimeUtils.quickDurations.forEach { (mins, label) ->
                    val isSelected = durationMinutes == mins
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) PrimaryFixed else SurfaceContainerLow)
                            .clickable {
                                val newEnd = ScheduleTimeUtils.addMinutes(startTime, mins)
                                onTimeRangeChanged(startTime, newEnd)
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("duration_$mins"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }

    // Modal Time Picker Dialog
    pickerTarget?.let { target ->
        val isStart = target == "start"
        ScheduleTimePickerDialog(
            title = if (isStart) "وقت بداية الحصة" else "وقت نهاية الحصة",
            initialTime = if (isStart) startTime else endTime,
            onDismiss = { pickerTarget = null },
            onConfirm = { selectedTime ->
                if (isStart) {
                    val currentStartMin = ScheduleTimeUtils.parseTimeToMinutes(selectedTime)
                    val currentEndMin = ScheduleTimeUtils.parseTimeToMinutes(endTime)
                    val newEnd = if (currentEndMin <= currentStartMin) {
                        ScheduleTimeUtils.addMinutes(selectedTime, 60)
                    } else {
                        endTime
                    }
                    onTimeRangeChanged(selectedTime, newEnd)
                } else {
                    onTimeRangeChanged(startTime, selectedTime)
                }
                pickerTarget = null
            }
        )
    }
}
