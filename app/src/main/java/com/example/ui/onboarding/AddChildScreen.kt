package com.example.ui.onboarding

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.SchoolGradeCatalog
import com.example.ui.MainViewModel
import com.example.ui.components.ChildAvatarView
import com.example.ui.theme.Primary
import com.example.ui.theme.PrimaryFixed
import com.example.ui.theme.SurfaceContainerLow

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddChildScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var childName by remember { mutableStateOf("") }
    var selectedGrade by remember { mutableStateOf("السنة الأولى ابتدائي (1AP)") }
    var avatarUri by remember { mutableStateOf<String?>("👦") }
    var expandedDropdown by remember { mutableStateOf(false) }
    var selectedStageFilter by remember { mutableStateOf("الكل") }

    // Real photo picker using ActivityResultContracts.PickVisualMedia
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val localPath = copyUriToAppInternalStorage(context, uri)
            avatarUri = localPath ?: uri.toString()
        }
    }

    val stages = listOf("الكل", "تحضيري", "ابتدائي", "متوسط", "ثانوي")
    val filteredGrades = remember(selectedStageFilter) {
        if (selectedStageFilter == "الكل") {
            SchoolGradeCatalog.ALL_GRADES
        } else {
            SchoolGradeCatalog.ALL_GRADES.filter { it.stage == selectedStageFilter }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Top Bar Back
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "رجوع",
                    tint = Primary
                )
            }
            Text(
                text = "إضافة طفل جديد 👦👧",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        // Avatar Preview & Real Photo Selector
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(contentAlignment = Alignment.BottomEnd) {
                ChildAvatarView(
                    avatarUri = avatarUri,
                    size = 90.dp,
                    emojiSize = 48.sp,
                    borderWidth = 3.dp,
                    borderColor = Primary
                )

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Primary)
                        .clickable {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = "اختيار صورة",
                        tint = androidx.compose.ui.graphics.Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("إضافة صورة حقيقية للطفل", fontSize = 12.sp)
                }
            }

            // Emoji avatars
            Text(
                text = "أو اختر رمزاً تعبيرياً:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            FlowRow(
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                AVATAR_EMOJI_OPTIONS.take(8).forEach { emoji ->
                    val isSelected = avatarUri == emoji
                    Box(
                        modifier = Modifier
                            .padding(3.dp)
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) PrimaryFixed else SurfaceContainerLow)
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) Primary else androidx.compose.ui.graphics.Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { avatarUri = emoji },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = emoji, fontSize = 20.sp)
                    }
                }
            }
        }

        // Inputs
        OutlinedTextField(
            value = childName,
            onValueChange = { childName = it },
            label = { Text("اسم الطفل الكامل") },
            placeholder = { Text("مثلاً: رانية") },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Face, contentDescription = null, tint = Primary)
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("new_child_name_input")
        )

        // Stage Filter
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "المرحلة التعليمية:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                stages.forEach { stage ->
                    val isSelected = selectedStageFilter == stage
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedStageFilter = stage },
                        label = { Text(stage, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryFixed,
                            selectedLabelColor = Primary
                        )
                    )
                }
            }
        }

        // Grade Dropdown
        ExposedDropdownMenuBox(
            expanded = expandedDropdown,
            onExpandedChange = { expandedDropdown = !expandedDropdown }
        ) {
            OutlinedTextField(
                value = selectedGrade,
                onValueChange = {},
                readOnly = true,
                label = { Text("المستوى الدراسي") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.School, contentDescription = null, tint = Primary)
                },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
                    .testTag("new_child_grade_dropdown")
            )
            ExposedDropdownMenu(
                expanded = expandedDropdown,
                onDismissRequest = { expandedDropdown = false }
            ) {
                filteredGrades.forEach { gradeInfo ->
                    DropdownMenuItem(
                        leadingIcon = { Text(text = gradeInfo.icon, fontSize = 18.sp) },
                        text = {
                            Column {
                                Text(gradeInfo.title, fontWeight = FontWeight.Bold)
                                Text(gradeInfo.categoryAr, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        },
                        onClick = {
                            selectedGrade = gradeInfo.title
                            expandedDropdown = false
                        }
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceContainerLow)
                .padding(14.dp)
        ) {
            Text(
                text = "💡 ملاحظة: لا نطلب أي حساب خارجي؛ جميع البيانات والصور تُحفظ محلياً على جهازك بكل أمان وخصوصية.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Action CTA
        Button(
            onClick = {
                if (childName.isNotBlank()) {
                    viewModel.saveNewChild(childName.trim(), selectedGrade, avatarUri)
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Primary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("submit_child_button")
        ) {
            Text(
                text = "حفظ وإضافة ملف الطفل",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
