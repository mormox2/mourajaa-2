package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.Screen
import com.example.ui.theme.Primary
import com.example.ui.theme.PrimaryContainer
import com.example.ui.theme.PrimaryFixed
import com.example.ui.theme.SurfaceContainerLowest

@Composable
fun MuraajaTopBar(
    childName: String = "أحمد",
    grade: String = "السنة الثانية ابتدائي",
    avatarUri: String? = null,
    totalStars: Int = 150,
    onNotificationClick: () -> Unit = {},
    onAchievementsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    isFocusMode: Boolean = false,
    onUnlockClick: () -> Unit = {}
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile & Greetings
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onProfileClick() }
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Child Avatar Badge (Supports real photo or emoji)
                ChildAvatarView(
                    avatarUri = avatarUri,
                    size = 46.dp,
                    emojiSize = 24.sp,
                    borderWidth = 1.5.dp,
                    borderColor = Primary
                )

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "صباح الخير",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(text = "👋", fontSize = 16.sp)
                    }
                    Text(
                        text = "$childName - $grade",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Star points chip (Achievements shortcut)
                Surface(
                    onClick = onAchievementsClick,
                    shape = RoundedCornerShape(16.dp),
                    color = PrimaryFixed,
                    modifier = Modifier.testTag("topbar_stars_badge")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(text = "⭐", fontSize = 12.sp)
                        Text(
                            text = "$totalStars",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                    }
                }

                if (isFocusMode) {
                    Surface(
                        onClick = onUnlockClick,
                        shape = RoundedCornerShape(16.dp),
                        color = PrimaryFixed,
                        modifier = Modifier.testTag("topbar_focus_badge")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "قفل نشط 🔓",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Primary
                            )
                        }
                    }
                }

                // Notification Bell with indicator dot
                Box {
                    IconButton(
                        onClick = onNotificationClick,
                        modifier = Modifier.testTag("notifications_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "التنبيهات",
                            tint = Primary
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.error)
                            .align(Alignment.TopEnd)
                            .padding(top = 8.dp, end = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MuraajaBottomNav(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit
) {
    Surface(
        color = SurfaceContainerLowest,
        shadowElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tab 1: الرئيسية (Home)
            BottomNavItem(
                title = "الرئيسية",
                icon = Icons.Default.Home,
                isSelected = currentScreen is Screen.Home,
                onClick = { onNavigate(Screen.Home) }
            )

            // Tab 2: الجدول (Schedule)
            BottomNavItem(
                title = "الجدول",
                icon = Icons.Default.CalendarToday,
                isSelected = currentScreen is Screen.Schedule,
                onClick = { onNavigate(Screen.Schedule) }
            )

            // Tab 3: الإنجازات (Achievements)
            BottomNavItem(
                title = "الإنجازات",
                icon = Icons.Default.EmojiEvents,
                isSelected = currentScreen is Screen.Achievements,
                onClick = { onNavigate(Screen.Achievements) }
            )

            // Tab 4: المواد (Subjects)
            BottomNavItem(
                title = "المواد",
                icon = Icons.Default.MenuBook,
                isSelected = currentScreen is Screen.Subjects,
                onClick = { onNavigate(Screen.Subjects) }
            )

            // Tab 5: الإعدادات (Settings)
            BottomNavItem(
                title = "الإعدادات",
                icon = Icons.Default.Settings,
                isSelected = currentScreen is Screen.Settings,
                onClick = { onNavigate(Screen.Settings) }
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgModifier = if (isSelected) {
        Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(PrimaryFixed)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    } else {
        Modifier
            .padding(horizontal = 6.dp, vertical = 4.dp)
    }

    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .then(bgModifier),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = if (isSelected) PrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = title,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) PrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
