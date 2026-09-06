package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LocalEmberColors

@Composable
fun TactileBubbleCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 32.dp,
    backgroundColor: Color? = null,
    borderColor: Color? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val colors = LocalEmberColors.current
    val bg = backgroundColor ?: colors.surfaceTile
    val border = borderColor ?: colors.surfaceBorder

    val shape = remember(cornerRadius) { RoundedCornerShape(cornerRadius) }

    val baseModifier = modifier
        .clip(shape)
        .background(bg)
        .border(width = 1.dp, color = border, shape = shape)

    Box(
        modifier = if (onClick != null) {
            baseModifier.clickable(onClick = onClick)
        } else {
            baseModifier
        }.padding(20.dp)
    ) {
        content()
    }
}
