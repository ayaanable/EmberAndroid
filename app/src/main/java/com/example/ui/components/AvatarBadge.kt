package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.theme.LocalEmberColors
import java.io.File

@Composable
fun AvatarBadge(
    name: String,
    imagePath: String?,
    size: Dp = 40.dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalEmberColors.current
    val initial = if (name.isNotBlank()) name.take(1).uppercase() else "E"
    val context = LocalContext.current

    val imageFile = remember(imagePath) {
        if (!imagePath.isNullOrBlank()) File(imagePath) else null
    }
    val hasValidImage = remember(imageFile) {
        imageFile?.exists() == true
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(colors.surfaceElevated)
            .border(
                width = 1.2.dp,
                color = colors.accentPrimary.copy(alpha = 0.5f),
                shape = CircleShape
            )
            .clickable(onClick = onClick)
            .testTag("profile_avatar_button"),
        contentAlignment = Alignment.Center
    ) {
        if (hasValidImage && imageFile != null) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(imageFile)
                    .crossfade(true)
                    .build(),
                contentDescription = "User profile picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
            )
        } else {
            // Minimal initials avatar using selected accent color
            Box(
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(colors.accentDeep.copy(alpha = 0.35f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initial,
                    color = colors.accentHighlight,
                    fontSize = (size.value * 0.42f).sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
