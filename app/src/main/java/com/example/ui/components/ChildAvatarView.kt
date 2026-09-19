package com.example.ui.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.theme.Primary
import com.example.ui.theme.PrimaryFixed

@Composable
fun ChildAvatarView(
    avatarUri: String?,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    emojiSize: TextUnit = 24.sp,
    fallbackEmoji: String = "👦",
    borderWidth: Dp = 0.dp,
    borderColor: Color = Primary,
    onClick: (() -> Unit)? = null
) {
    val clickableModifier = if (onClick != null) {
        modifier
            .size(size)
            .clip(CircleShape)
            .clickable { onClick() }
    } else {
        modifier
            .size(size)
            .clip(CircleShape)
    }

    val borderModifier = if (borderWidth > 0.dp) {
        clickableModifier.border(borderWidth, borderColor, CircleShape)
    } else {
        clickableModifier
    }

    Box(
        modifier = borderModifier
            .background(PrimaryFixed),
        contentAlignment = Alignment.Center
    ) {
        val hasRealPhoto = !avatarUri.isNullOrBlank() && 
            (avatarUri.startsWith("content://") || avatarUri.startsWith("file://") || avatarUri.startsWith("http"))

        if (hasRealPhoto) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(Uri.parse(avatarUri))
                    .crossfade(true)
                    .build(),
                contentDescription = "صورة الطفل الحقيقية",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
            )
        } else if (!avatarUri.isNullOrBlank()) {
            // It's an emoji avatar e.g. "👦", "👧", "🚀", etc.
            Text(
                text = avatarUri,
                fontSize = emojiSize
            )
        } else {
            // Default fallback
            Text(
                text = fallbackEmoji,
                fontSize = emojiSize
            )
        }
    }
}
