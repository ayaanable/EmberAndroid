package com.example.ui.screens.today

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Task
import com.example.ui.theme.LocalEmberColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskBottomSheet(
    taskToEdit: Task? = null,
    onDismiss: () -> Unit,
    onSave: (title: String, description: String, duration: String, category: String) -> Unit,
    onDelete: ((Long) -> Unit)? = null
) {
    val colors = LocalEmberColors.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var title by remember { mutableStateOf(taskToEdit?.title ?: "") }
    var description by remember { mutableStateOf(taskToEdit?.description ?: "") }
    var duration by remember { mutableStateOf(taskToEdit?.duration ?: "30 min") }
    var category by remember { mutableStateOf(taskToEdit?.category ?: "Personal") }

    val presetDurations = listOf("Undefined", "10 min", "20 min", "30 min", "45 min", "1 hour", "2 hours")
    val presetCategories = listOf("Personal", "Study", "Craft", "Morning reset", "Evening reflection", "Health", "Work")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.surfaceElevated,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (taskToEdit == null) "New Task" else "Edit Task",
                    color = colors.textHigh,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (taskToEdit != null && onDelete != null) {
                        IconButton(
                            onClick = {
                                onDelete(taskToEdit.id)
                                onDismiss()
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete task",
                                tint = Color(0xFFE57373),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(colors.surfaceBorder.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close sheet",
                            tint = colors.textMedium,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Title
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("Task title (e.g. Study DSA)", color = colors.textMuted) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(colors.surfaceTile)
                    .testTag("task_title_input"),
                textStyle = TextStyle(fontSize = 16.sp, color = colors.textHigh),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = colors.accentPrimary
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                placeholder = { Text("Optional details or intention", color = colors.textMuted) },
                maxLines = 3,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(colors.surfaceTile)
                    .testTag("task_desc_input"),
                textStyle = TextStyle(fontSize = 14.sp, color = colors.textHigh),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = colors.accentPrimary
                )
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Duration selector
            Text(
                text = "DURATION",
                color = colors.textMuted,
                fontSize = 11.sp,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presetDurations.forEach { dur ->
                    val isSelected = duration == dur
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(9999.dp))
                            .background(if (isSelected) colors.accentPrimary else colors.surfaceTile)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) Color.Transparent else colors.surfaceBorder,
                                shape = RoundedCornerShape(9999.dp)
                            )
                            .clickable { duration = dur }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = dur,
                            color = if (isSelected) colors.onAccent else colors.textMedium,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Category selector
            Text(
                text = "CATEGORY",
                color = colors.textMuted,
                fontSize = 11.sp,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presetCategories.forEach { cat ->
                    val isSelected = category == cat
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(9999.dp))
                            .background(if (isSelected) colors.accentPrimary else colors.surfaceTile)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) Color.Transparent else colors.surfaceBorder,
                                shape = RoundedCornerShape(9999.dp)
                            )
                            .clickable { category = cat }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = cat,
                            color = if (isSelected) colors.onAccent else colors.textMedium,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Save button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(9999.dp))
                    .background(if (title.isNotBlank()) colors.accentPrimary else colors.surfaceBorder)
                    .clickable(enabled = title.isNotBlank()) {
                        onSave(title.trim(), description.trim(), duration, category)
                        onDismiss()
                    }
                    .padding(vertical = 14.dp)
                    .testTag("submit_task_button"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (taskToEdit == null) "Create Task" else "Save Changes",
                    color = if (title.isNotBlank()) colors.onAccent else colors.textMuted,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
