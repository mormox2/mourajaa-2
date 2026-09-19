package com.example.ui.settings

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.data.local.entity.ReminderSettingsEntity
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.focus.ChangeParentPinDialog
import com.example.ui.theme.Primary
import com.example.ui.theme.PrimaryFixed
import com.example.ui.theme.Secondary
import com.example.ui.theme.SurfaceContainer
import com.example.ui.theme.SurfaceContainerLow
import com.example.ui.theme.SurfaceContainerLowest
import com.example.ui.theme.Tertiary
import com.example.ui.theme.TertiaryFixed

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onNavigateToAddChild: () -> Unit
) {
    val activeChild by viewModel.activeChild.collectAsState()
    val settings by viewModel.reminderSettings.collectAsState()
    val parentPin by viewModel.parentPin.collectAsState()
    val badges by viewModel.badges.collectAsState()
    val totalStars by viewModel.totalEarnedStars.collectAsState()
    val unlockedCount by viewModel.unlockedBadgesCount.collectAsState()

    var showEditTimeDialog by remember { mutableStateOf<String?>(null) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showChangePinDialog by remember { mutableStateOf(false) }
    var showPinPreview by remember { mutableStateOf(false) }

    val currentSettings = settings ?: ReminderSettingsEntity()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Text(
            text = "الإعدادات",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        // 1. Group: بيانات الطفل
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
            shape = RoundedCornerShape(14.dp),
            elevation = CardDefaults.cardElevation(1.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = Primary)
                    Text(
                        text = "👦 ملف الطفل",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { viewModel.navigateTo(Screen.EditProfile) }
                            .padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        com.example.ui.components.ChildAvatarView(
                            avatarUri = activeChild?.avatarUri,
                            size = 52.dp,
                            emojiSize = 28.sp,
                            borderWidth = 2.dp,
                            borderColor = Primary
                        )
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = activeChild?.name ?: "أحمد",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "✏️",
                                    fontSize = 14.sp
                                )
                            }
                            Text(
                                text = activeChild?.grade ?: "السنة الثانية ابتدائي (2AP)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        TextButton(
                            onClick = { viewModel.navigateTo(Screen.EditProfile) },
                            modifier = Modifier.testTag("edit_child_profile_button")
                        ) {
                            Text(text = "تخصيص", color = Primary, fontWeight = FontWeight.Bold)
                        }
                        TextButton(onClick = onNavigateToAddChild) {
                            Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null, tint = Primary)
                            Spacer(modifier = Modifier.size(4.dp))
                            Text(text = "إضافة", color = Primary, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Child Badges and Rewards Status
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceContainerLow)
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = "🏆", fontSize = 22.sp)
                            Column {
                                Text(
                                    text = "الأوسمة: $unlockedCount / ${badges.size} مكتملة",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "مجموع النجوم: $totalStars ⭐",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Primary
                                )
                            }
                        }

                        TextButton(onClick = { viewModel.navigateTo(Screen.Achievements) }) {
                            Text(text = "عرض الأوسمة 🌟", fontWeight = FontWeight.Bold, color = Primary)
                        }
                    }
                }
            }
        }

        // 2. Group: التذكيرات والإشعارات
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
            shape = RoundedCornerShape(14.dp),
            elevation = CardDefaults.cardElevation(1.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Notifications, contentDescription = null, tint = Primary)
                    Text(
                        text = "🔔 التذكيرات اليومية",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Reminder 1: تذكير مواد الغد
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "تذكير مواد الغد",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "الوقت: ${currentSettings.previousDayTime}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Primary
                        )
                    }
                    Switch(
                        checked = currentSettings.previousDayEnabled,
                        onCheckedChange = {
                            viewModel.updateSettings(currentSettings.copy(previousDayEnabled = it))
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = Primary)
                    )
                }

                // Reminder 2: التذكير الصباحي
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "التذكير الصباحي وتجهيز الحقيبة",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "الوقت: ${currentSettings.morningTime}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Primary
                        )
                    }
                    Switch(
                        checked = currentSettings.morningEnabled,
                        onCheckedChange = {
                            viewModel.updateSettings(currentSettings.copy(morningEnabled = it))
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = Primary)
                    )
                }

                // Reminder 3: تذكير وقت المراجعة
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "تذكير وقت المراجعة المسائية",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "الوقت: ${currentSettings.reviewTime}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Primary
                        )
                    }
                    Switch(
                        checked = currentSettings.reviewTimeEnabled,
                        onCheckedChange = {
                            viewModel.updateSettings(currentSettings.copy(reviewTimeEnabled = it))
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = Primary)
                    )
                }
            }
        }

        // 3. Group: إعدادات المراجعة العامة
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
            shape = RoundedCornerShape(14.dp),
            elevation = CardDefaults.cardElevation(1.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Timer, contentDescription = null, tint = Primary)
                    Text(
                        text = "📚 خيارات المراجعة",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "مدة المراجعة الافتراضية",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "المعدل: ${currentSettings.defaultReviewMinutes} دقيقة لكل مادة",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceContainerLow)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${currentSettings.defaultReviewMinutes} دقيقة",
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                    }
                }
            }
        }

        // 4. Group: أمان ووضع التركيز (رمز مرور ولي الأمر)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("parent_pin_settings_card"),
            colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
            shape = RoundedCornerShape(14.dp),
            elevation = CardDefaults.cardElevation(1.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = Primary)
                        Text(
                            text = "🔐 حماية ولي الأمر ووضع التركيز",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Text(
                    text = "يُطلب رمز المرور (PIN) عند محاولة الطفل إنهاء قفل الهاتف أو الخروج من وضع التركيز للألعاب وتطبيقات الهاتف الأخرى.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Current PIN display with show/hide toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceContainerLow)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "رمز المرور الحالي",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (showPinPreview) parentPin else "••••",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Primary,
                            letterSpacing = 4.sp
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = { showPinPreview = !showPinPreview },
                            modifier = Modifier.testTag("toggle_pin_visibility_button")
                        ) {
                            Icon(
                                imageVector = if (showPinPreview) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (showPinPreview) "إخفاء الرمز" else "معاينة الرمز",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = { showChangePinDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Primary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("change_pin_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Key,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "تغيير الرمز",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. Group: التطبيق واللغة
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
            shape = RoundedCornerShape(14.dp),
            elevation = CardDefaults.cardElevation(1.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Tune, contentDescription = null, tint = Primary)
                    Text(
                        text = "🌐 لغة ومظهر التطبيق",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "اللغة الأساسية", style = MaterialTheme.typography.titleSmall)
                    Text(
                        text = "العربية (RTL) ✓",
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "الوضع الداكن (Dark Mode)", style = MaterialTheme.typography.titleSmall)
                    Switch(
                        checked = currentSettings.darkMode,
                        onCheckedChange = {
                            viewModel.updateSettings(currentSettings.copy(darkMode = it))
                        }
                    )
                }
            }
        }

        // 5. Group: حول التطبيق
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showAboutDialog = true },
            colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
            shape = RoundedCornerShape(14.dp),
            elevation = CardDefaults.cardElevation(1.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = Primary)
                Column {
                    Text(
                        text = "حول تطبيق «مراجعة»",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "الإصدار 1.0 • معًا نحو مستقبل أفضل • يعمل كلياً بدون إنترنت (Offline-First)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(60.dp))
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("تطبيق «مراجعة» — الإصدار 1.0", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "تطبيق «مراجعة» يساعد الوالد على متابعة الدراسة اليومية للطفل اعتماداً على جدول المدرسة.\n\n" +
                    "• إجابة 3 أسئلة يومية: ماذا درس طفلي اليوم؟ ماذا سيدرس غداً؟ ماذا نراجع الليلة؟\n" +
                    "• خصوصية وأمان كاملين: كل البيانات محلية ومخزنة عبر Room على هاتفك فقط.\n" +
                    "• مصمم بواجهات Material 3 حديثة تدعم اللغة العربية بالكامل."
                )
            },
            confirmButton = {
                Button(
                    onClick = { showAboutDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text("إغلاق")
                }
            }
        )
    }

    if (showChangePinDialog) {
        ChangeParentPinDialog(
            currentSavedPin = parentPin,
            onDismiss = { showChangePinDialog = false },
            onPinUpdated = { newPin ->
                viewModel.updateParentPin(newPin)
                showChangePinDialog = false
            }
        )
    }
}
