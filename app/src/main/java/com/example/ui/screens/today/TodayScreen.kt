package com.example.ui.screens.today

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Task
import com.example.ui.components.AvatarBadge
import com.example.ui.components.TactileBubbleCard
import com.example.ui.theme.LocalEmberColors
import com.example.ui.viewmodel.EmberViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TodayScreen(
    viewModel: EmberViewModel,
    onOpenProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalEmberColors.current
    val todayTasks by viewModel.todayTasks.collectAsState()
    val profile by viewModel.userProfile.collectAsState()

    var showAddTaskSheet by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<Task?>(null) }

    // Date label e.g. "Monday 23"
    val dateHeader = remember {
        val format = SimpleDateFormat("EEEE d", Locale.US)
        format.format(Date())
    }

    val (totalTasks, completedCount, progress) = remember(todayTasks) {
        val total = todayTasks.size
        val completed = todayTasks.count { it.completed }
        val prog = if (total > 0) completed.toFloat() / total else 0f
        Triple(total, completed, prog)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.canvas)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Header: "Today" with Integrated Progress + Add Button + Avatar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Today",
                        color = colors.textHigh,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.5).sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "$completedCount / $totalTasks",
                            color = colors.accentPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .width(120.dp)
                                .height(5.dp)
                                .clip(RoundedCornerShape(9999.dp)),
                            color = colors.accentPrimary,
                            trackColor = colors.surfaceTile,
                            strokeCap = StrokeCap.Round
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Circular "+" button
                    IconButton(
                        onClick = {
                            taskToEdit = null
                            showAddTaskSheet = true
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(colors.surfaceElevated)
                            .border(1.dp, colors.surfaceBorder, CircleShape)
                            .testTag("add_task_header_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add new task",
                            tint = colors.accentPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(colors.surfaceElevated)
                            .border(1.dp, colors.surfaceBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        AvatarBadge(
                            name = profile.name,
                            imagePath = profile.profileImagePath,
                            size = 38.dp,
                            onClick = onOpenProfile
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // TASK BUBBLE GRID (Staggered cells with 32.dp rounded corners)
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalItemSpacing = 12.dp,
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("task_staggered_grid")
                    .padding(bottom = 90.dp) // Clearance for bottom navigation
            ) {
                items(todayTasks, key = { it.id }) { task ->
                    TaskBubbleTile(
                        task = task,
                        onToggle = { viewModel.toggleTask(task) },
                        onEdit = {
                            taskToEdit = task
                            showAddTaskSheet = true
                        }
                    )
                }

                // "+ Add task" bubble card at the end of the grid
                item(span = StaggeredGridItemSpan.FullLine) {
                    AddTaskBubbleTile(
                        onClick = {
                            taskToEdit = null
                            showAddTaskSheet = true
                        }
                    )
                }
            }
        }

        // Add / Edit Task Bottom Sheet
        if (showAddTaskSheet) {
            TaskBottomSheet(
                taskToEdit = taskToEdit,
                onDismiss = {
                    showAddTaskSheet = false
                    taskToEdit = null
                },
                onSave = { title, desc, dur, cat ->
                    if (taskToEdit == null) {
                        viewModel.addTask(title, desc, dur, cat)
                    } else {
                        viewModel.updateTask(
                            taskToEdit!!.copy(
                                title = title,
                                description = desc,
                                duration = dur,
                                category = cat
                            )
                        )
                    }
                },
                onDelete = { id ->
                    viewModel.deleteTask(id)
                }
            )
        }
    }
}

@Composable
fun TaskBubbleTile(
    task: Task,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalEmberColors.current

    // Incomplete tasks: Bright natural terracotta orange (#F27D26), onAccent dark text (#121212)
    // Completed tasks: Muted warm charcoal-earth (#2A2724) with #8E8E8E text
    val tileBg by animateColorAsState(
        targetValue = if (!task.completed) {
            colors.accentPrimary
        } else {
            colors.surfaceTile.copy(alpha = 0.8f)
        },
        animationSpec = tween(250),
        label = "tile_bg"
    )

    val textColor by animateColorAsState(
        targetValue = if (!task.completed) colors.onAccent else colors.textMuted,
        animationSpec = tween(250),
        label = "text_color"
    )

    val subTextColor by animateColorAsState(
        targetValue = if (!task.completed) colors.onAccent.copy(alpha = 0.7f) else colors.textMuted.copy(alpha = 0.8f),
        animationSpec = tween(250),
        label = "subtext_color"
    )

    val shape = RoundedCornerShape(32.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(tileBg)
            .border(
                width = 1.dp,
                color = if (!task.completed) Color.Transparent else colors.surfaceBorder,
                shape = shape
            )
            .clickable(onClick = onToggle)
            .padding(20.dp)
            .testTag("task_bubble_${task.id}")
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Row: Title & Toggle Circle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = task.title,
                    color = textColor,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 22.sp,
                    textDecoration = if (task.completed) TextDecoration.LineThrough else TextDecoration.None,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                )

                // Completion Circle Indicator
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(
                            if (task.completed) {
                                colors.accentPrimary
                            } else {
                                Color.Transparent
                            }
                        )
                        .border(
                            width = 2.dp,
                            color = if (task.completed) Color.Transparent else colors.onAccent.copy(alpha = 0.35f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (task.completed) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed",
                            tint = Color(0xFF121212),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            if (task.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = task.description,
                    color = subTextColor,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Bottom status: Category • Duration uppercase & edit icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val tagText = buildString {
                    append(task.category.uppercase())
                    if (task.duration.isNotBlank()) {
                        append(" • ")
                        append(task.duration.uppercase())
                    }
                }

                Text(
                    text = tagText,
                    color = subTextColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.8.sp
                )

                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit task",
                        tint = subTextColor,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AddTaskBubbleTile(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalEmberColors.current
    val shape = RoundedCornerShape(32.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.surfaceElevated)
            .border(
                width = 2.dp,
                color = colors.surfaceBorder,
                shape = shape
            )
            .clickable(onClick = onClick)
            .padding(vertical = 22.dp, horizontal = 20.dp)
            .testTag("add_task_tile"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(colors.surfaceTile),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    tint = colors.textHigh,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = "ADD TASK",
                color = colors.textMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.4.sp
            )
        }
    }
}
