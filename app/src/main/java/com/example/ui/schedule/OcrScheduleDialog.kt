package com.example.ui.schedule

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.entity.SubjectEntity
import com.example.domain.model.DayOfWeekAr
import com.example.domain.ocr.ParsedScheduleItem
import com.example.ui.MainViewModel
import com.example.ui.OcrUiState
import com.example.ui.theme.OutlineVariant
import com.example.ui.theme.Primary
import com.example.ui.theme.PrimaryContainer
import com.example.ui.theme.PrimaryFixed
import com.example.ui.theme.SurfaceContainer
import com.example.ui.theme.SurfaceContainerHigh
import com.example.ui.theme.SurfaceContainerLow
import com.example.ui.theme.SurfaceContainerLowest
import com.example.ui.theme.TertiaryContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OcrScheduleDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val ocrState by viewModel.ocrState.collectAsState()
    val reminderSettings by viewModel.reminderSettings.collectAsState()
    val subjects by viewModel.subjects.collectAsState()
    val context = LocalContext.current

    // Camera Launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            viewModel.scanScheduleImage(bitmap)
        }
    }

    // Gallery Launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                if (bitmap != null) {
                    viewModel.scanScheduleImage(bitmap)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    var showApiKeyDialog by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = {
            viewModel.resetOcrState()
            onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(24.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            when (val state = ocrState) {
                is OcrUiState.Idle -> {
                    OcrSourceSelectionView(
                        onLaunchCamera = { cameraLauncher.launch(null) },
                        onLaunchGallery = {
                            galleryLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        onConfigureApiKey = { showApiKeyDialog = true },
                        hasApiKey = !reminderSettings?.geminiApiKey.isNullOrBlank(),
                        onDismiss = onDismiss
                    )
                }

                is OcrUiState.Scanning -> {
                    OcrScanningLoadingView(
                        statusMessage = state.statusMessage,
                        onCancel = { viewModel.resetOcrState() }
                    )
                }

                is OcrUiState.Error -> {
                    OcrErrorView(
                        message = state.message,
                        isApiKeyMissing = state.isApiKeyMissing,
                        onRetry = { viewModel.resetOcrState() },
                        onConfigureApiKey = { showApiKeyDialog = true },
                        onDismiss = {
                            viewModel.resetOcrState()
                            onDismiss()
                        }
                    )
                }

                is OcrUiState.Review -> {
                    OcrReviewScheduleView(
                        initialItems = state.items,
                        availableSubjects = subjects,
                        onConfirmImport = { selected, replaceExisting ->
                            viewModel.confirmOcrImport(selected, replaceExisting)
                            onDismiss()
                        },
                        onRetake = { viewModel.resetOcrState() },
                        onDismiss = {
                            viewModel.resetOcrState()
                            onDismiss()
                        }
                    )
                }
            }
        }
    }

    if (showApiKeyDialog) {
        GeminiApiKeyDialog(
            currentKey = reminderSettings?.geminiApiKey ?: "",
            onSave = { key ->
                viewModel.saveGeminiApiKey(key)
                showApiKeyDialog = false
            },
            onDismiss = { showApiKeyDialog = false }
        )
    }
}

@Composable
private fun OcrSourceSelectionView(
    onLaunchCamera: () -> Unit,
    onLaunchGallery: () -> Unit,
    onConfigureApiKey: () -> Unit,
    hasApiKey: Boolean,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
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
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(PrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "المسح الذكي للجدول (OCR)",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "استخراج جدول المدرسة تلقائياً بالذكاء الاصطناعي",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "إغلاق")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Option 1: Camera
            Card(
                onClick = onLaunchCamera,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ocr_camera_option"),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Primary.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "تصوير الجدول بالكاميرا",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "التقط صورة لجدول المدرسة الورقي مباشرة",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Option 2: Gallery
            Card(
                onClick = onLaunchGallery,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ocr_gallery_option"),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, OutlineVariant)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(TertiaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "اختيار صورة من المعرض",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "استيراد صورة أو لقطة شاشة مخزنة في الهاتف",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Advice / Tips Card
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, OutlineVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "💡 نصائح للحصول على أفضل دقة:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• التقط الصورة في إضاءة جيدة وتأكد من وضوح الحروف.\n" +
                                "• اجعل الجدول مستوياً وبزاوية تصوير عمودية ومباشرة.\n" +
                                "• يدعم النظام الجداول باللغة العربية والفرنسية والثنائية.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Footer / API Key config
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onConfigureApiKey) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (hasApiKey) Primary else MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = if (hasApiKey) "مفتاح الذكاء الاصطناعي (مفعل)" else "تحديد مفتاح Gemini API",
                        fontSize = 12.sp,
                        color = if (hasApiKey) Primary else MaterialTheme.colorScheme.error
                    )
                }
            }

            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    }
}

@Composable
private fun OcrScanningLoadingView(
    statusMessage: String,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(PrimaryContainer),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(60.dp),
                color = Primary,
                strokeWidth = 4.dp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "جاري مسح وتحليل الجدول...",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = statusMessage,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "الذكاء الاصطناعي يتعرف على الأيام، الساعات، والمواد باللغتين العربية والفرنسية",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedButton(onClick = onCancel) {
            Text("إلغاء العملية")
        }
    }
}

@Composable
private fun OcrErrorView(
    message: String,
    isApiKeyMissing: Boolean,
    onRetry: () -> Unit,
    onConfigureApiKey: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.errorContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = if (isApiKeyMissing) "مطلوب مفتاح Gemini API" else "تعذر استخراج الجدول",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        if (isApiKeyMissing) {
            Button(
                onClick = onConfigureApiKey,
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Key, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("إدخال مفتاح Gemini API الآن")
            }
            Spacer(modifier = Modifier.height(10.dp))
        } else {
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("إعادة المحاولة بصورة أخرى")
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("إغلاق")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OcrReviewScheduleView(
    initialItems: List<ParsedScheduleItem>,
    availableSubjects: List<SubjectEntity>,
    onConfirmImport: (selected: List<ParsedScheduleItem>, replaceExisting: Boolean) -> Unit,
    onRetake: () -> Unit,
    onDismiss: () -> Unit
) {
    val items = remember { mutableStateListOf(*initialItems.toTypedArray()) }
    var replaceExisting by remember { mutableStateOf(true) }

    val selectedCount = items.count { it.isSelected }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(text = "🎉", fontSize = 22.sp)
                    Text(
                        text = "تم استخراج ${items.size} حصة",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "حدد الحصص المراد إضافتها للجدول وراجع الأوقات والمواد",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "إغلاق")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Replace vs Merge Option
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (replaceExisting) "استبدال الجدول الحالي كاملاً" else "دمج الحصص مع الجدول الحالي",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (replaceExisting) "سيتم حذف الحصص القديمة واعتماد هذا الجدول الجديد" else "ستُضاف الحصص الجديدة مع الحفاظ على الحصص السابقة",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
                Switch(
                    checked = replaceExisting,
                    onCheckedChange = { replaceExisting = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = Primary)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Extracted classes list
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items, key = { it.tempId }) { item ->
                val itemIndex = items.indexOf(item)
                OcrScheduleItemCard(
                    item = item,
                    availableSubjects = availableSubjects,
                    onToggleSelect = { isChecked ->
                        items[itemIndex] = item.copy(isSelected = isChecked)
                    },
                    onUpdateSubject = { newSubject ->
                        items[itemIndex] = item.copy(
                            matchedSubjectId = newSubject.id,
                            matchedSubjectName = newSubject.name,
                            icon = newSubject.icon
                        )
                    },
                    onDelete = {
                        items.removeAt(itemIndex)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onRetake,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("إعادة التصوير")
            }

            Button(
                onClick = {
                    val selected = items.filter { it.isSelected }
                    onConfirmImport(selected, replaceExisting)
                },
                enabled = selectedCount > 0,
                modifier = Modifier
                    .weight(2f)
                    .testTag("confirm_ocr_import_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "تأكيد واستيراد ($selectedCount حصة)",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OcrScheduleItemCard(
    item: ParsedScheduleItem,
    availableSubjects: List<SubjectEntity>,
    onToggleSelect: (Boolean) -> Unit,
    onUpdateSubject: (SubjectEntity) -> Unit,
    onDelete: () -> Unit
) {
    var subjectMenuExpanded by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (item.isSelected) SurfaceContainerLowest else SurfaceContainerLow.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (item.isSelected) Primary.copy(alpha = 0.4f) else OutlineVariant
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.isSelected,
                onCheckedChange = onToggleSelect,
                colors = CheckboxDefaults.colors(checkedColor = Primary)
            )

            Spacer(modifier = Modifier.width(6.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Day & Time Tag
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(PrimaryContainer)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = item.dayOfWeek.titleAr,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "${item.startTime} - ${item.endTime}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Subject Selector / Display
                ExposedDropdownMenuBox(
                    expanded = subjectMenuExpanded,
                    onExpandedChange = { subjectMenuExpanded = it }
                ) {
                    Row(
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceContainer)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(text = item.icon, fontSize = 16.sp)
                        Text(
                            text = item.matchedSubjectName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    ExposedDropdownMenu(
                        expanded = subjectMenuExpanded,
                        onDismissRequest = { subjectMenuExpanded = false }
                    ) {
                        availableSubjects.forEach { sub ->
                            DropdownMenuItem(
                                text = {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(text = sub.icon)
                                        Text(text = sub.name)
                                    }
                                },
                                onClick = {
                                    onUpdateSubject(sub)
                                    subjectMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                // Optional Teacher / Classroom
                if (item.teacherName.isNotBlank() || item.classroom.isNotBlank()) {
                    Row(
                        modifier = Modifier.padding(top = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (item.teacherName.isNotBlank()) {
                            Text(
                                text = "👤 ${item.teacherName}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                        if (item.classroom.isNotBlank()) {
                            Text(
                                text = "🚪 ${item.classroom}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "حذف الحصة",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun GeminiApiKeyDialog(
    currentKey: String,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var keyText by remember { mutableStateOf(currentKey) }
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "مفتاح Google Gemini API",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "لتحليل وقراءة صورة جدول المدرسة بتقنية الذكاء الاصطناعي، يرجى إدخال مفتاحك المجاني من منصة Google AI Studio.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = keyText,
                    onValueChange = { keyText = it },
                    label = { Text("Gemini API Key") },
                    placeholder = { Text("AIzaSy...") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("gemini_api_key_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary
                    )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val browserIntent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://aistudio.google.com/app/apikey")
                            )
                            context.startActivity(browserIntent)
                        }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "احصل على مفتاح مجاني من Google AI Studio",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("إلغاء")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onSave(keyText) },
                        enabled = keyText.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("حفظ المفتاح")
                    }
                }
            }
        }
    }
}
