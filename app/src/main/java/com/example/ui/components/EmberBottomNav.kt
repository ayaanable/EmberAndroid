package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalEmberColors

enum class EmberScreen(val title: String) {
    JOURNAL("Journal"),
    TODAY("Today"),
    HISTORY("History")
}

@Composable
fun EmberBottomNav(
    currentScreen: EmberScreen,
    onScreenSelected: (EmberScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalEmberColors.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(40.dp))
                .background(colors.surfaceElevated)
                .border(
                    width = 1.dp,
                    color = colors.surfaceBorder,
                    shape = RoundedCornerShape(40.dp)
                )
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                EmberNavItem(
                    screen = EmberScreen.JOURNAL,
                    isSelected = currentScreen == EmberScreen.JOURNAL,
                    onClick = { onScreenSelected(EmberScreen.JOURNAL) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("nav_tab_journal")
                )
                EmberNavItem(
                    screen = EmberScreen.TODAY,
                    isSelected = currentScreen == EmberScreen.TODAY,
                    onClick = { onScreenSelected(EmberScreen.TODAY) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("nav_tab_today")
                )
                EmberNavItem(
                    screen = EmberScreen.HISTORY,
                    isSelected = currentScreen == EmberScreen.HISTORY,
                    onClick = { onScreenSelected(EmberScreen.HISTORY) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("nav_tab_history")
                )
            }
        }
    }
}

@Composable
private fun EmberNavItem(
    screen: EmberScreen,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalEmberColors.current
    val itemColor by animateColorAsState(
        targetValue = if (isSelected) colors.accentPrimary else colors.textMuted,
        animationSpec = tween(durationMillis = 200),
        label = "nav_item_color"
    )

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Custom Minimal Outline Icon
        when (screen) {
            EmberScreen.JOURNAL -> JournalNavIcon(color = itemColor)
            EmberScreen.TODAY -> TodayNavIcon(color = itemColor, isSelected = isSelected)
            EmberScreen.HISTORY -> HistoryNavIcon(color = itemColor)
        }

        Text(
            text = screen.title.uppercase(),
            color = itemColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.4.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun JournalNavIcon(color: Color) {
    androidx.compose.foundation.Canvas(modifier = Modifier.size(20.dp)) {
        val strokeWidth = 1.6.dp.toPx()
        val w = size.width
        val h = size.height
        // Minimal open journal notebook contours
        drawRoundRect(
            color = color,
            topLeft = androidx.compose.ui.geometry.Offset(2f, 2f),
            size = androidx.compose.ui.geometry.Size(w - 4f, h - 4f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx()),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
        )
        drawLine(
            color = color,
            start = androidx.compose.ui.geometry.Offset(w / 2, 4f),
            end = androidx.compose.ui.geometry.Offset(w / 2, h - 4f),
            strokeWidth = strokeWidth
        )
    }
}

@Composable
private fun TodayNavIcon(color: Color, isSelected: Boolean = false) {
    androidx.compose.foundation.Canvas(modifier = Modifier.size(20.dp)) {
        val strokeWidth = 1.8.dp.toPx()
        val radius = (size.minDimension - 2f) / 2f
        val center = androidx.compose.ui.geometry.Offset(size.width / 2, size.height / 2)
        if (isSelected) {
            // Filled circle
            drawCircle(
                color = color,
                radius = radius,
                center = center
            )
            // Checkmark in dark accent
            val path = androidx.compose.ui.graphics.Path().apply {
                moveTo(center.x - radius * 0.45f, center.y)
                lineTo(center.x - radius * 0.1f, center.y + radius * 0.35f)
                lineTo(center.x + radius * 0.45f, center.y - radius * 0.3f)
            }
            drawPath(
                path = path,
                color = Color(0xFF121212),
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = strokeWidth,
                    cap = androidx.compose.ui.graphics.StrokeCap.Round,
                    join = androidx.compose.ui.graphics.StrokeJoin.Round
                )
            )
        } else {
            drawCircle(
                color = color,
                radius = radius,
                center = center,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
            )
            val path = androidx.compose.ui.graphics.Path().apply {
                moveTo(center.x - radius * 0.45f, center.y)
                lineTo(center.x - radius * 0.1f, center.y + radius * 0.35f)
                lineTo(center.x + radius * 0.45f, center.y - radius * 0.3f)
            }
            drawPath(
                path = path,
                color = color,
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = strokeWidth,
                    cap = androidx.compose.ui.graphics.StrokeCap.Round,
                    join = androidx.compose.ui.graphics.StrokeJoin.Round
                )
            )
        }
    }
}

@Composable
private fun HistoryNavIcon(color: Color) {
    androidx.compose.foundation.Canvas(modifier = Modifier.size(20.dp)) {
        val strokeWidth = 1.6.dp.toPx()
        val radius = (size.minDimension - 4f) / 2f
        val center = androidx.compose.ui.geometry.Offset(size.width / 2, size.height / 2)
        drawCircle(
            color = color,
            radius = radius,
            center = center,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
        )
        // Clock hands
        drawLine(
            color = color,
            start = center,
            end = androidx.compose.ui.geometry.Offset(center.x, center.y - radius * 0.55f),
            strokeWidth = strokeWidth,
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )
        drawLine(
            color = color,
            start = center,
            end = androidx.compose.ui.geometry.Offset(center.x + radius * 0.4f, center.y),
            strokeWidth = strokeWidth,
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )
    }
}
