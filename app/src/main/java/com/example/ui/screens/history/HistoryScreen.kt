package com.example.ui.screens.history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailyEntry
import com.example.data.model.MoodLevel
import com.example.data.model.Task
import com.example.ui.components.AvatarBadge
import com.example.ui.components.TactileBubbleCard
import com.example.ui.theme.LocalEmberColors
import com.example.ui.viewmodel.EmberViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: EmberViewModel,
    onOpenProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalEmberColors.current
    val allEntries by viewModel.allEntries.collectAsState()
    val allTasks by viewModel.allTasks.collectAsState()
    val profile by viewModel.userProfile.collectAsState()

    var selectedEntryForRead by remember { mutableStateOf<DailyEntry?>(null) }
    var selectedDateForRecord by remember { mutableStateOf<String?>(null) }
    var trendDaysFilter by remember { mutableIntStateOf(7) } // 7, 30, 90

    // Month navigation calendar state
    var currentCalendarMonth by remember {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        mutableStateOf(cal)
    }

    val monthHeaderFormat = remember { SimpleDateFormat("MMMM yyyy", Locale.US) }
    val monthTitle = remember(currentCalendarMonth) { monthHeaderFormat.format(currentCalendarMonth.time) }

    // Productivity stats
    val totalCompletedTasks = remember(allTasks) { allTasks.count { it.completed } }
    val completionPercentage = remember(allTasks, totalCompletedTasks) {
        if (allTasks.isNotEmpty()) {
            ((totalCompletedTasks.toFloat() / allTasks.size) * 100).toInt()
        } else {
            0
        }
    }
    // Filter to exclude onboarding guide from statistics
    val userEntries = remember(allEntries) {
        allEntries.filter { !it.tags.contains("Guide") }
    }

    // Streak calculation - consecutive days of user activity
    val currentStreak = remember(userEntries) {
        if (userEntries.isEmpty()) return@remember 0
        val sortedDates = userEntries.map { it.date }.distinct().sortedDescending()
        var streak = 0
        val cal = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val today = dateFormat.format(cal.time)
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterday = dateFormat.format(cal.time)
        var checkDate = if (sortedDates.first() == today) today else yesterday
        for (date in sortedDates) {
            if (date == checkDate) {
                streak++
                val d = dateFormat.parse(checkDate)
                cal.time = d!!
                cal.add(Calendar.DAY_OF_YEAR, -1)
                checkDate = dateFormat.format(cal.time)
            } else if (date < checkDate) break
        }
        streak
    }

    val recentReflections = remember(userEntries) {
        userEntries.filter { it.journalText.isNotBlank() }.take(10)
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 120.dp) // Space for bottom navigation
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Header: History + Avatar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "History",
                    color = colors.textHigh,
                    fontSize = 32.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Normal
                )

                AvatarBadge(
                    name = profile.name,
                    imagePath = profile.profileImagePath,
                    size = 44.dp,
                    onClick = onOpenProfile
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // PRODUCTIVITY STATS ROW
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatBubble(
                    title = "COMPLETED",
                    value = "$totalCompletedTasks",
                    subtitle = "Tasks overall",
                    modifier = Modifier.weight(1f)
                )
                StatBubble(
                    title = "RATE",
                    value = "$completionPercentage%",
                    subtitle = "Focus consistency",
                    modifier = Modifier.weight(1f)
                )
                StatBubble(
                    title = "STREAK",
                    value = "$currentStreak",
                    subtitle = "Active days",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // MINIMALIST MOOD CALENDAR
            TactileBubbleCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = colors.surfaceTile,
                cornerRadius = 32.dp
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Month selector row: < September 2026 >
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = monthTitle,
                            color = colors.textHigh,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    val newCal = currentCalendarMonth.clone() as Calendar
                                    newCal.add(Calendar.MONTH, -1)
                                    currentCalendarMonth = newCal
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                    contentDescription = "Previous month",
                                    tint = colors.textMedium
                                )
                            }
                            IconButton(
                                onClick = {
                                    val newCal = currentCalendarMonth.clone() as Calendar
                                    newCal.add(Calendar.MONTH, 1)
                                    currentCalendarMonth = newCal
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = "Next month",
                                    tint = colors.textMedium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Days of week header
                    val daysOfWeek = listOf("S", "M", "T", "W", "T", "F", "S")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        daysOfWeek.forEach { day ->
                            Text(
                                text = day,
                                color = colors.textMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Calendar Days Grid
                    CalendarGrid(
                        monthCalendar = currentCalendarMonth,
                        entries = allEntries,
                        onDayClick = { dateStr ->
                            selectedDateForRecord = dateStr
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // MINIMALIST MOOD TREND GRAPH
            TactileBubbleCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = colors.surfaceTile,
                cornerRadius = 32.dp
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "MOOD TREND",
                            color = colors.textMuted,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        // Filters: 7d, 30d, 90d
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(7, 30, 90).forEach { days ->
                                val isSelected = trendDaysFilter == days
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(9999.dp))
                                        .background(if (isSelected) colors.accentPrimary else colors.surfaceElevated)
                                        .clickable { trendDaysFilter = days }
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "${days}d",
                                        color = if (isSelected) colors.onAccent else colors.textMedium,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Custom Smooth Canvas Line Graph
                    MoodTrendCanvas(
                        entries = allEntries,
                        daysCount = trendDaysFilter,
                        accentColor = colors.accentPrimary,
                        highlightColor = colors.accentHighlight,
                        gridColor = colors.surfaceBorder.copy(alpha = 0.4f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // PAST JOURNAL REFLECTIONS LIST
            Text(
                text = "PAST REFLECTIONS",
                color = colors.textMuted,
                fontSize = 11.sp,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            recentReflections.forEach { entry ->
                PastEntryBubbleCard(
                    entry = entry,
                    onClick = { selectedEntryForRead = entry }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        // Full Entry Reader Modal
        if (selectedEntryForRead != null) {
            EntryReaderSheet(
                entry = selectedEntryForRead!!,
                onDismiss = { selectedEntryForRead = null }
            )
        }

        // Day Record Detail Sheet
        if (selectedDateForRecord != null) {
            DailyRecordSheet(
                date = selectedDateForRecord!!,
                entry = allEntries.firstOrNull { it.date == selectedDateForRecord },
                tasks = allTasks.filter { it.date == selectedDateForRecord },
                onDismiss = { selectedDateForRecord = null }
            )
        }
    }
}

@Composable
private fun StatBubble(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    val colors = LocalEmberColors.current
    TactileBubbleCard(
        modifier = modifier,
        backgroundColor = colors.surfaceTile,
        cornerRadius = 20.dp
    ) {
        Column {
            Text(
                text = title,
                color = colors.textMuted,
                fontSize = 9.sp,
                letterSpacing = 0.8.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                color = colors.textHigh,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = colors.textMuted,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun CalendarGrid(
    monthCalendar: Calendar,
    entries: List<DailyEntry>,
    onDayClick: (String) -> Unit
) {
    val colors = LocalEmberColors.current

    val (firstDayOfWeek, maxDays, monthPrefix) = remember(monthCalendar) {
        val cal = monthCalendar.clone() as Calendar
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val first = cal.get(Calendar.DAY_OF_WEEK)
        val max = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val dateFormat = SimpleDateFormat("yyyy-MM-", Locale.US)
        val prefix = dateFormat.format(monthCalendar.time)
        Triple(first, max, prefix)
    }

    val entryMap = remember(entries, monthPrefix) {
        entries.filter { 
            it.date.startsWith(monthPrefix) && !it.tags.contains("Guide") 
        }.associateBy { it.date }
    }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        for (row in 0 until 5) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                for (col in 0 until 7) {
                    val slotIndex = row * 7 + col
                    val dayNumber = slotIndex - (firstDayOfWeek - 1) + 1

                    if (dayNumber in 1..maxDays) {
                        val dayStr = if (dayNumber < 10) "0$dayNumber" else "$dayNumber"
                        val fullDate = "$monthPrefix$dayStr"
                        val entry = entryMap[fullDate]

                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .clickable { onDayClick(fullDate) },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "$dayNumber",
                                    color = if (entry != null) colors.textHigh else colors.textMuted,
                                    fontSize = 12.sp,
                                    fontWeight = if (entry != null) FontWeight.SemiBold else FontWeight.Normal
                                )

                                if (entry != null) {
                                    // Geometric dot colored according to mood
                                    val dotColor = when (entry.mood) {
                                        1 -> Color(0xFF8D6E63)
                                        2 -> Color(0xFFA1887F)
                                        3 -> colors.textMedium
                                        4 -> colors.accentHighlight
                                        5 -> colors.accentPrimary
                                        else -> colors.accentPrimary
                                    }
                                    Box(
                                        modifier = Modifier
                                            .padding(top = 2.dp)
                                            .size(4.dp)
                                            .clip(CircleShape)
                                            .background(dotColor)
                                    )
                                }
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.size(34.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun MoodTrendCanvas(
    entries: List<DailyEntry>,
    daysCount: Int,
    accentColor: Color,
    highlightColor: Color,
    gridColor: Color
) {
    // Generate trend points from user entries (excluding guides)
    val points = remember(entries, daysCount) {
        val userList = entries.filter { !it.tags.contains("Guide") }.take(daysCount).reversed()
        if (userList.size >= 2) {
            userList.map { it.mood.toFloat() }
        } else {
            emptyList()
        }
    }

    if (points.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No mood trends yet",
                color = gridColor,
                fontSize = 12.sp,
                fontStyle = FontStyle.Italic
            )
        }
        return
    }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
    ) {
        val w = size.width
        val h = size.height

        // Draw subtle horizontal grid lines
        for (i in 1..4) {
            val y = (h / 5f) * i
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(w, y),
                strokeWidth = 1f
            )
        }

        if (points.size < 2) return@Canvas

        val stepX = w / (points.size - 1)
        val minMood = 1f
        val maxMood = 5f

        val path = Path()
        val fillPath = Path()

        points.forEachIndexed { index, mood ->
            val normY = 1f - ((mood - minMood) / (maxMood - minMood)).coerceIn(0.1f, 0.95f)
            val x = index * stepX
            val y = normY * (h - 20f) + 10f

            if (index == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, h)
                fillPath.lineTo(x, y)
            } else {
                path.lineTo(x, y)
                fillPath.lineTo(x, y)
            }

            // Draw accent dot on each data point
            drawCircle(
                color = accentColor,
                radius = 3.5.dp.toPx(),
                center = Offset(x, y)
            )
        }

        fillPath.lineTo(w, h)
        fillPath.close()

        // Soft vertical gradient fill beneath line
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    accentColor.copy(alpha = 0.25f),
                    Color.Transparent
                )
            )
        )

        // Smooth main trend line
        drawPath(
            path = path,
            color = accentColor,
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

@Composable
private fun PastEntryBubbleCard(
    entry: DailyEntry,
    onClick: () -> Unit
) {
    val colors = LocalEmberColors.current
    val moodObj = MoodLevel.fromLevel(entry.mood)

    TactileBubbleCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        backgroundColor = colors.surfaceTile,
        cornerRadius = 20.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(colors.accentPrimary)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = entry.date,
                        color = colors.textMedium,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Text(
                    text = moodObj.descriptor,
                    color = colors.accentHighlight,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = entry.journalText.take(120) + if (entry.journalText.length > 120) "..." else "",
                color = colors.textHigh,
                fontSize = 14.sp,
                lineHeight = 22.sp
            )

            if (entry.tags.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    entry.tags.split(",").take(3).forEach { tag ->
                        Text(
                            text = "#${tag.trim()}",
                            color = colors.textMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EntryReaderSheet(
    entry: DailyEntry,
    onDismiss: () -> Unit
) {
    val colors = LocalEmberColors.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val moodObj = MoodLevel.fromLevel(entry.mood)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.canvas,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = entry.date,
                        color = colors.textHigh,
                        fontSize = 22.sp,
                        fontFamily = FontFamily.Serif
                    )
                    Text(
                        text = moodObj.descriptor,
                        color = colors.accentHighlight,
                        fontSize = 13.sp
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(colors.surfaceElevated)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = colors.textMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = entry.journalText,
                color = colors.textHigh,
                fontSize = 16.sp,
                lineHeight = 26.sp
            )

            if (entry.tags.isNotBlank()) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Tags: ${entry.tags}",
                    color = colors.textMuted,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DailyRecordSheet(
    date: String,
    entry: DailyEntry?,
    tasks: List<Task>,
    onDismiss: () -> Unit
) {
    val colors = LocalEmberColors.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.surfaceElevated,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Record for $date",
                    color = colors.textHigh,
                    fontSize = 18.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.SemiBold
                )

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = colors.textMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Mood
            Text(
                text = "MOOD",
                color = colors.textMuted,
                fontSize = 10.sp,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            if (entry != null) {
                Text(
                    text = "${MoodLevel.fromLevel(entry.mood).title} (${MoodLevel.fromLevel(entry.mood).descriptor})",
                    color = colors.accentHighlight,
                    fontSize = 14.sp
                )
            } else {
                Text(
                    text = "No mood recorded",
                    color = colors.textMuted,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Journal
            Text(
                text = "JOURNAL ENTRY",
                color = colors.textMuted,
                fontSize = 10.sp,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            if (entry != null && entry.journalText.isNotBlank()) {
                Text(
                    text = entry.journalText,
                    color = colors.textHigh,
                    fontSize = 14.sp,
                    lineHeight = 22.sp
                )
            } else {
                Text(
                    text = "No journal entry written",
                    color = colors.textMuted,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Tasks
            Text(
                text = "TASKS",
                color = colors.textMuted,
                fontSize = 10.sp,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (tasks.isNotEmpty()) {
                tasks.forEach { task ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = task.title,
                            color = if (task.completed) colors.textMuted else colors.textHigh,
                            fontSize = 13.sp
                        )
                        Text(
                            text = if (task.completed) "Completed" else "Missed / Incomplete",
                            color = if (task.completed) colors.accentHighlight else Color(0xFFE57373),
                            fontSize = 11.sp
                        )
                    }
                }
            } else {
                Text(
                    text = "No tasks tracked on this date",
                    color = colors.textMuted,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
