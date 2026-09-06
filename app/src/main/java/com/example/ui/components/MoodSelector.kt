package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MoodLevel
import com.example.ui.theme.LocalEmberColors

@Composable
fun MoodSelector(
    selectedMood: Int,
    onMoodSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    showDescriptor: Boolean = true
) {
    val colors = LocalEmberColors.current
    val currentMoodObj = MoodLevel.fromLevel(selectedMood)

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MoodLevel.entries.forEach { mood ->
                val isSelected = mood.level == selectedMood
                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.22f else 1.0f,
                    animationSpec = spring(dampingRatio = 0.75f),
                    label = "mood_scale"
                )

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("mood_button_${mood.level}")
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onMoodSelected(mood.level) }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .scale(scale)
                            .border(
                                width = if (isSelected) 1.5.dp else 0.dp,
                                color = if (isSelected) colors.accentPrimary.copy(alpha = 0.8f) else Color.Transparent,
                                shape = CircleShape
                            )
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AbstractMoodNode(
                            level = mood.level,
                            isSelected = isSelected,
                            accentColor = colors.accentPrimary,
                            neutralColor = colors.textMedium,
                            mutedColor = colors.textMuted.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }

        if (showDescriptor) {
            Text(
                text = currentMoodObj.descriptor,
                color = colors.accentHighlight,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun AbstractMoodNode(
    level: Int,
    isSelected: Boolean,
    accentColor: Color,
    neutralColor: Color,
    mutedColor: Color
) {
    val targetColor by animateColorAsState(
        targetValue = if (isSelected) accentColor else neutralColor,
        label = "node_color"
    )

    Canvas(modifier = Modifier.size(24.dp)) {
        val center = Offset(size.width / 2, size.height / 2)
        val r = size.minDimension / 2f

        when (level) {
            1 -> {
                // Tier 1: Very Low - Clean outline ring with central subtle dot
                drawCircle(
                    color = targetColor,
                    radius = r * 0.75f,
                    center = center,
                    style = Stroke(width = 1.8.dp.toPx())
                )
                drawCircle(
                    color = targetColor.copy(alpha = 0.5f),
                    radius = r * 0.22f,
                    center = center,
                    style = Fill
                )
            }
            2 -> {
                // Tier 2: Low - Two concentric solid rings
                drawCircle(
                    color = targetColor,
                    radius = r * 0.85f,
                    center = center,
                    style = Stroke(width = 1.6.dp.toPx())
                )
                drawCircle(
                    color = targetColor,
                    radius = r * 0.45f,
                    center = center,
                    style = Stroke(width = 1.6.dp.toPx())
                )
            }
            3 -> {
                // Tier 3: Neutral - Single centered disc node
                drawCircle(
                    color = targetColor,
                    radius = r * 0.5f,
                    center = center,
                    style = Fill
                )
            }
            4 -> {
                // Tier 4: Good - Outer ring with inner warm dot
                drawCircle(
                    color = targetColor,
                    radius = r * 0.85f,
                    center = center,
                    style = Stroke(width = 1.5.dp.toPx())
                )
                drawCircle(
                    color = targetColor,
                    radius = r * 0.45f,
                    center = center,
                    style = Fill
                )
            }
            5 -> {
                // Tier 5: Great - Luminous concentric rings with solid core
                drawCircle(
                    color = targetColor.copy(alpha = 0.4f),
                    radius = r * 0.95f,
                    center = center,
                    style = Stroke(width = 1.5.dp.toPx())
                )
                drawCircle(
                    color = targetColor,
                    radius = r * 0.65f,
                    center = center,
                    style = Stroke(width = 1.5.dp.toPx())
                )
                drawCircle(
                    color = targetColor,
                    radius = r * 0.35f,
                    center = center,
                    style = Fill
                )
            }
        }
    }
}
