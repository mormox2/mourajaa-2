package com.example.ui.schedule

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import com.example.data.local.entity.ScheduleEntryEntity
import com.example.data.local.entity.SubjectEntity
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.example.domain.model.DayOfWeekAr
import com.example.ui.MainViewModel
import com.example.ui.theme.OutlineVariant
import com.example.ui.theme.Primary
import com.example.ui.theme.PrimaryContainer
import com.example.ui.theme.PrimaryFixed
import com.example.ui.theme.Secondary
import com.example.ui.theme.SurfaceContainer
import com.example.ui.theme.SurfaceContainerLow
import com.example.ui.theme.SurfaceContainerLowest
import com.example.ui.theme.TertiaryContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(viewModel: MainViewModel) {
    val selectedDay by viewModel.selectedDay.collectAsState()
    val allEntries by viewModel.allScheduleEntries.collectAsState()
    val subjects by viewModel.subjects.collectAsState()

    val subjectMap = subjects.associateBy { it.id }
    val dayEntries = allEntries.filter { it.dayOfWeek.equals(selectedDay.key, ignoreCase = true) }

    var showAddDialog by remember { mutableStateOf(false) }
    var entryToEdit by remember { mutableStateOf<ScheduleEntryEntity?>(null) }
    var entryToDelete by remember { mutableStateOf<Long?>(null) }
    var showOcrDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
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
                    Text(text = "📅", fontSize = 24.sp)
                    Column {
                        Text(
                            text = "جدول المدرسة",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "تنظيم الحصص والواجبات اليومية",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = { showOcrDialog = true },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(SurfaceContainer)
                            .testTag("ocr_camera_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = "المسح الذكي للجدول (OCR)",
                            tint = Primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = { showAddDialog = true },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(SurfaceContainer)
                            .testTag("add_schedule_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "إضافة حصة",
                            tint = Primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // Days Tabs (M3 Pill Selector)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                DayOfWeekAr.entries.forEach { day ->
                    val isSelected = day == selectedDay
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) PrimaryContainer else SurfaceContainerLowest)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) Primary else OutlineVariant,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { viewModel.setSelectedDay(day) }
                            .padding(vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = day.dayNumber,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) PrimaryFixed else MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = day.shortAr,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                    }
                }
            }

            // Day Status Summary Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(TertiaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.TaskAlt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "جدول يوم ${selectedDay.titleAr}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${dayEntries.size} حصص مبرمجة",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceContainerLowest)
                            .border(1.dp, OutlineVariant, RoundedCornerShape(16.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "منتظم",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                    }
                }
            }

            // Class Cards List
            if (dayEntries.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(text = "📅", fontSize = 32.sp)
                        Text(
                            text = "لا توجد حصص مضافة لهذا اليوم",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "اضغط على زر (+) في الأسفل لإضافة حصة جديدة لهذا اليوم",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            androidx.compose.material3.OutlinedButton(
                                onClick = { showOcrDialog = true }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoCamera,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("مسح بالكاميرا")
                            }
                            Button(
                                onClick = { showAddDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Primary)
                            ) {
                                Text("إضافة يدوية")
                            }
                        }
                    }
                }
            } else {
                dayEntries.forEach { entry ->
                    val subject = subjectMap[entry.subjectId]
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { entryToEdit = entry }
                            .testTag("schedule_card_${entry.id}"),
                        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                        shape = RoundedCornerShape(14.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Top Row Time & Actions (Edit + Delete)
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
                                        text = "${entry.startTime} - ${entry.endTime}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Primary
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    IconButton(
                                        onClick = { entryToEdit = entry },
                                        modifier = Modifier
                                            .size(30.dp)
                                            .testTag("edit_entry_${entry.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "تعديل الحصة والتوقيت",
                                            tint = Primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = { entryToDelete = entry.id },
                                        modifier = Modifier
                                            .size(30.dp)
                                            .testTag("delete_entry_${entry.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "حذف الحصة",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            // Subject Name & Icon
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(text = subject?.icon ?: "📖", fontSize = 28.sp)
                                Column {
                                    Text(
                                        text = subject?.name ?: "مادة",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (entry.lessonNote.isNotEmpty()) {
                                        Text(
                                            text = entry.lessonNote,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            // Teacher & Classroom Details
                            if (entry.teacherName.isNotEmpty() || entry.classroom.isNotEmpty()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    if (entry.teacherName.isNotEmpty()) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Person,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.outline,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = entry.teacherName,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    if (entry.classroom.isNotEmpty()) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.MeetingRoom,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.outline,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = entry.classroom,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(70.dp))
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = PrimaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
                .testTag("fab_add_class")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "إضافة حصة")
                Text(
                    text = "إضافة حصة",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }

    // Add / Edit Class Dialog
    if (showAddDialog || entryToEdit != null) {
        val isEditing = entryToEdit != null
        ScheduleEntryDialog(
            isEditing = isEditing,
            initialEntry = entryToEdit,
            dayTitle = selectedDay.titleAr,
            subjects = subjects,
            onDismiss = {
                showAddDialog = false
                entryToEdit = null
            },
            onConfirm = { subjectId, startTime, endTime, note, teacher, classroom ->
                if (isEditing) {
                    entryToEdit?.let { existing ->
                        viewModel.updateScheduleEntry(
                            id = existing.id,
                            subjectId = subjectId,
                            dayOfWeek = existing.dayOfWeek,
                            startTime = startTime,
                            endTime = endTime,
                            note = note,
                            teacher = teacher,
                            classroom = classroom
                        )
                    }
                } else {
                    viewModel.addScheduleEntry(
                        subjectId = subjectId,
                        dayOfWeek = selectedDay.key,
                        startTime = startTime,
                        endTime = endTime,
                        note = note,
                        teacher = teacher,
                        classroom = classroom
                    )
                }
                showAddDialog = false
                entryToEdit = null
            }
        )
    }

    // Confirmation for Delete
    if (entryToDelete != null) {
        AlertDialog(
            onDismissRequest = { entryToDelete = null },
            title = { Text("حذف الحصة") },
            text = { Text("هل أنت متأكد من رغبتك في حذف هذه الحصة من جدول المدرسة؟") },
            confirmButton = {
                Button(
                    onClick = {
                        entryToDelete?.let { viewModel.deleteScheduleEntry(it) }
                        entryToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("نعم، حذف")
                }
            },
            dismissButton = {
                TextButton(onClick = { entryToDelete = null }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // OCR Timetable Scanner Dialog
    if (showOcrDialog) {
        OcrScheduleDialog(
            viewModel = viewModel,
            onDismiss = { showOcrDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleEntryDialog(
    isEditing: Boolean,
    initialEntry: ScheduleEntryEntity?,
    dayTitle: String,
    subjects: List<SubjectEntity>,
    onDismiss: () -> Unit,
    onConfirm: (
        subjectId: Long,
        startTime: String,
        endTime: String,
        note: String,
        teacher: String,
        classroom: String
    ) -> Unit
) {
    var selectedSubjectId by remember {
        mutableStateOf(initialEntry?.subjectId ?: subjects.firstOrNull()?.id ?: 1L)
    }
    var startTime by remember {
        mutableStateOf(initialEntry?.startTime ?: "08:00")
    }
    var endTime by remember {
        mutableStateOf(initialEntry?.endTime ?: "09:00")
    }
    var note by remember {
        mutableStateOf(initialEntry?.lessonNote ?: "")
    }
    var teacher by remember {
        mutableStateOf(initialEntry?.teacherName ?: "")
    }
    var classroom by remember {
        mutableStateOf(initialEntry?.classroom ?: "")
    }
    var expandedDropdown by remember { mutableStateOf(false) }

    val selectedSubject = subjects.find { it.id == selectedSubjectId }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (isEditing) Icons.Default.Edit else Icons.Default.Add,
                    contentDescription = null,
                    tint = Primary
                )
                Text(
                    text = if (isEditing) "تعديل الحصة ($dayTitle)" else "إضافة حصة جديدة ($dayTitle)",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                // Subject Selector
                ExposedDropdownMenuBox(
                    expanded = expandedDropdown,
                    onExpandedChange = { expandedDropdown = !expandedDropdown }
                ) {
                    OutlinedTextField(
                        value = "${selectedSubject?.icon ?: "📖"} ${selectedSubject?.name ?: "اختر مادة"}",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("المادة") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedDropdown,
                        onDismissRequest = { expandedDropdown = false }
                    ) {
                        subjects.forEach { subject ->
                            DropdownMenuItem(
                                text = { Text("${subject.icon} ${subject.name}") },
                                onClick = {
                                    selectedSubjectId = subject.id
                                    expandedDropdown = false
                                }
                            )
                        }
                    }
                }

                // Simplified Time Selection with Slots, M3 Pickers, and Durations
                TimeRangeSelectorSection(
                    startTime = startTime,
                    endTime = endTime,
                    onTimeRangeChanged = { newStart, newEnd ->
                        startTime = newStart
                        endTime = newEnd
                    }
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("ملاحظة الدرس / الواجب") },
                    placeholder = { Text("مثلاً: حل تمارين ص 22") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = teacher,
                    onValueChange = { teacher = it },
                    label = { Text("اسم الأستاذ (اختياري)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = classroom,
                    onValueChange = { classroom = it },
                    label = { Text("القاعة (اختياري)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        selectedSubjectId,
                        startTime,
                        endTime,
                        note,
                        teacher,
                        classroom
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                modifier = Modifier.testTag("save_schedule_entry_button")
            ) {
                Text(if (isEditing) "حفظ التعديلات" else "حفظ الحصة")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}
