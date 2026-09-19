package com.example.ui.onboarding

import android.content.Context
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.example.data.local.entity.ChildEntity
import com.example.domain.model.SchoolGradeCatalog
import com.example.ui.MainViewModel
import com.example.ui.components.ChildAvatarView
import com.example.ui.theme.Primary
import com.example.ui.theme.PrimaryFixed
import com.example.ui.theme.Secondary
import com.example.ui.theme.SurfaceContainerLow
import com.example.ui.theme.SurfaceContainerLowest
import java.io.File
import java.io.FileOutputStream

val AVATAR_EMOJI_OPTIONS = listOf(
    "👦", "👧", "🧒", "🧑‍🎓", "👩‍🎓", "⭐", "🚀", "🦁", "🦊", "🎨", "⚽", "🧠"
)

fun copyUriToAppInternalStorage(context: Context, sourceUri: Uri): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(sourceUri) ?: return null
        val fileName = "child_avatar_${System.currentTimeMillis()}.jpg"
        val destinationFile = File(context.filesDir, fileName)
        val outputStream = FileOutputStream(destinationFile)
        inputStream.copyTo(outputStream)
        inputStream.close()
        outputStream.close()
        Uri.fromFile(destinationFile).toString()
    } catch (e: Exception) {
        null
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditProfileScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val activeChild by viewModel.activeChild.collectAsState()
    val context = LocalContext.current

    var childName by remember(activeChild) { mutableStateOf(activeChild?.name ?: "أحمد") }
    var selectedGrade by remember(activeChild) { mutableStateOf(activeChild?.grade ?: "السنة الثانية ابتدائي (2AP)") }
    var avatarUri by remember(activeChild) { mutableStateOf<String?>(activeChild?.avatarUri) }
    var selectedStageFilter by remember { mutableStateOf("الكل") }
    var expandedDropdown by remember { mutableStateOf(false) }

    // Zero-permission Android Photo Picker for real photo selection
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
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "رجوع",
                    tint = Primary
                )
            }
            Column {
                Text(
                    text = "تخصيص ملف الطفل 🎨",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "تعديل الصورة الحقيقية، الرمز، والمستوى الدراسي الشامل",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Section 1: الصورة والرمز الشخصي (Photo Réelle & Avatar)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "الصورة الشخصية للطفل",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Large Avatar with Photo/Icon
                Box(contentAlignment = Alignment.BottomEnd) {
                    ChildAvatarView(
                        avatarUri = avatarUri,
                        size = 100.dp,
                        emojiSize = 52.sp,
                        borderWidth = 3.dp,
                        borderColor = Primary
                    )

                    // Small photo action button
                    Box(
                        modifier = Modifier
                            .size(34.dp)
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
                            contentDescription = "تغيير الصورة",
                            tint = androidx.compose.ui.graphics.Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Action buttons for photo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("pick_real_photo_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("إضافة صورة حقيقية", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    if (!avatarUri.isNullOrBlank() && (avatarUri?.startsWith("content://") == true || avatarUri?.startsWith("file://") == true)) {
                        OutlinedButton(
                            onClick = { avatarUri = "👦" },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("remove_real_photo_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "حذف الصورة",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("رمز افتراضي", fontSize = 12.sp)
                        }
                    }
                }

                // Or Pick from Fun Child Emojis
                Text(
                    text = "أو اختر رمزاً تعبيرياً يناسب شخصية الطفل:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AVATAR_EMOJI_OPTIONS.forEach { emoji ->
                        val isSelected = avatarUri == emoji
                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .size(44.dp)
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
                            Text(text = emoji, fontSize = 22.sp)
                        }
                    }
                }
            }
        }

        // Section 2: اسم الطفل والمستوى الدراسي الموسع (Informations & Niveau Scolaire)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "البيانات المدرسية والمستوى",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Input Name
                OutlinedTextField(
                    value = childName,
                    onValueChange = { childName = it },
                    label = { Text("اسم الطفل الكامل") },
                    placeholder = { Text("مثلاً: أحمد، رانية، سارة") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Face, contentDescription = null, tint = Primary)
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("child_name_input")
                )

                // Stage filter chips (ابتدائي، متوسط، ثانوي، تحضيري)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "تصفية حسب الطور التعليمي:",
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

                // Dropdown of Grades
                ExposedDropdownMenuBox(
                    expanded = expandedDropdown,
                    onExpandedChange = { expandedDropdown = !expandedDropdown }
                ) {
                    OutlinedTextField(
                        value = selectedGrade,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("المستوى الدراسي المحدد") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.School, contentDescription = null, tint = Primary)
                        },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .testTag("child_grade_dropdown")
                    )

                    ExposedDropdownMenu(
                        expanded = expandedDropdown,
                        onDismissRequest = { expandedDropdown = false }
                    ) {
                        filteredGrades.forEach { gradeInfo ->
                            DropdownMenuItem(
                                leadingIcon = {
                                    Text(text = gradeInfo.icon, fontSize = 18.sp)
                                },
                                text = {
                                    Column {
                                        Text(text = gradeInfo.title, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = gradeInfo.categoryAr,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
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

                // Current selected level preview banner
                val currentGradeInfo = SchoolGradeCatalog.ALL_GRADES.find { it.title == selectedGrade }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceContainerLow)
                        .padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(text = currentGradeInfo?.icon ?: "🎓", fontSize = 24.sp)
                        Column {
                            Text(
                                text = selectedGrade,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Primary
                            )
                            Text(
                                text = "طور ${currentGradeInfo?.categoryAr ?: "التعليم المدرسي"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Privacy note
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceContainerLow)
                .padding(12.dp)
        ) {
            Text(
                text = "🔒 خصوصية تامة: صورة الطفل وبياناته تُخزن داخل الذاكرة المحلية للجهاز فقط ولا تُرفع لأي خادم خارجي.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Submit Button
        Button(
            onClick = {
                if (childName.isNotBlank()) {
                    val current = activeChild
                    if (current != null) {
                        viewModel.updateChildProfile(
                            current.copy(
                                name = childName.trim(),
                                grade = selectedGrade,
                                avatarUri = avatarUri
                            )
                        )
                    } else {
                        viewModel.saveNewChild(
                            name = childName.trim(),
                            grade = selectedGrade,
                            avatarUri = avatarUri
                        )
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Primary),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("save_profile_button")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null)
                Text(
                    text = "حفظ التعديلات في ملف الطفل",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
