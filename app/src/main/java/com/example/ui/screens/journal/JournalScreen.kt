package com.example.ui.screens.journal

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailyEntry
import com.example.data.model.MoodLevel
import com.example.ui.components.AvatarBadge
import com.example.ui.components.MoodSelector
import com.example.ui.components.TactileBubbleCard
import com.example.ui.theme.LocalEmberColors
import com.example.ui.viewmodel.EmberViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun JournalScreen(
    viewModel: EmberViewModel,
    onOpenProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalEmberColors.current
    val profile by viewModel.userProfile.collectAsState()
    val todayEntry by viewModel.todayEntry.collectAsState()
    val allEntries by viewModel.allEntries.collectAsState()
    val currentMood by viewModel.currentSelectedMood.collectAsState()

    var isWritingDockOpen by remember { mutableStateOf(false) }

    // Calculate greeting based on time of day - optimized to remember
    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when {
            hour in 4..11 -> "Good morning"
            hour in 12..16 -> "Good afternoon"
            else -> "Good evening"
        }
    }

    // Filter entries to exclude the onboarding guide from user statistics
    val userEntries = remember(allEntries) {
        allEntries.filter { !it.tags.contains("Guide") }
    }

    // Word count calculation - only for user-created content
    val totalWords = remember(userEntries) {
        val regex = Regex("\\s+")
        userEntries.sumOf { entry ->
            entry.journalText.split(regex).count { it.isNotBlank() }
        }
    }

    // Streak calculation - consecutive days of actual user activity
    val streakDays = remember(userEntries) {
        if (userEntries.isEmpty()) return@remember 0
        
        val sortedDates = userEntries.map { it.date }.distinct().sortedDescending()
        var currentStreak = 0
        val cal = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        
        val today = dateFormat.format(cal.time)
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterday = dateFormat.format(cal.time)
        
        // Start checking from today or yesterday
        var checkDate = if (sortedDates.first() == today) today else yesterday
        
        for (date in sortedDates) {
            if (date == checkDate) {
                currentStreak++
                // Move checkDate to previous day
                val d = dateFormat.parse(checkDate)
                cal.time = d!!
                cal.add(Calendar.DAY_OF_YEAR, -1)
                checkDate = dateFormat.format(cal.time)
            } else if (date < checkDate) {
                // Streak broken
                break
            }
        }
        currentStreak
    }

    // Previous entry for reflection
    val previousEntry = remember(userEntries) {
        userEntries.firstOrNull { it.date != viewModel.todayDateString && it.journalText.isNotBlank() }
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
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 185.dp) // Space so content scrolls comfortably clear of docks
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Top Header: Greeting + Avatar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "$greeting,",
                        color = colors.textMuted,
                        fontSize = 15.sp,
                        fontFamily = FontFamily.SansSerif
                    )
                    Text(
                        text = profile.name,
                        color = colors.textHigh,
                        fontSize = 28.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                AvatarBadge(
                    name = profile.name,
                    imagePath = profile.profileImagePath,
                    size = 44.dp,
                    onClick = onOpenProfile
                )
            }

            Spacer(modifier = Modifier.height(26.dp))

            // TODAY'S REFLECTION BUBBLE CARD (Warm vibrant accent inspired by reference image)
            TactileBubbleCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reflection_card"),
                backgroundColor = colors.accentPrimary,
                borderColor = Color.Transparent,
                cornerRadius = 32.dp,
                onClick = { isWritingDockOpen = true }
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
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(colors.onAccent.copy(alpha = 0.8f))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "TODAY'S REFLECTION",
                                color = colors.onAccent.copy(alpha = 0.85f),
                                fontSize = 11.sp,
                                letterSpacing = 1.2.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Subtle arrow
                        Text(
                            text = "↗",
                            color = colors.onAccent.copy(alpha = 0.9f),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "what grounded your attention today?",
                        color = colors.onAccent,
                        fontSize = 24.sp,
                        fontFamily = FontFamily.Serif,
                        lineHeight = 30.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "CURRENT STREAK",
                                color = colors.onAccent.copy(alpha = 0.75f),
                                fontSize = 10.sp,
                                letterSpacing = 0.8.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "$streakDays Days",
                                color = colors.onAccent,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "TOTAL WORDS",
                                color = colors.onAccent.copy(alpha = 0.75f),
                                fontSize = 10.sp,
                                letterSpacing = 0.8.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "%,d".format(totalWords),
                                color = colors.onAccent,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // STATE OF MIND BUBBLE CARD
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
                            text = "STATE OF MIND",
                            color = colors.textMuted,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Text(
                            text = MoodLevel.fromLevel(currentMood).descriptor,
                            color = colors.accentHighlight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    MoodSelector(
                        selectedMood = currentMood,
                        onMoodSelected = { viewModel.setSelectedMood(it) },
                        showDescriptor = false
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // RECENT REFLECTION CARD (Encrypted journal entry preview)
            if (previousEntry != null || todayEntry != null) {
                val displayEntry = todayEntry?.takeIf { it.journalText.isNotBlank() } ?: previousEntry
                if (displayEntry != null) {
                    TactileBubbleCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("recent_entry_card"),
                        backgroundColor = colors.surfaceTile,
                        cornerRadius = 32.dp,
                        onClick = { isWritingDockOpen = true }
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
                                        text = if (displayEntry.date == viewModel.todayDateString) "Today" else "Previous entry",
                                        color = colors.textMedium,
                                        fontSize = 12.sp
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Encrypted",
                                        tint = colors.textMuted,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Encrypted",
                                        color = colors.textMuted,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = displayEntry.journalText.take(140) + if (displayEntry.journalText.length > 140) "..." else "",
                                color = colors.textHigh,
                                fontSize = 14.sp,
                                lineHeight = 22.sp
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Entry #${displayEntry.id}",
                                    color = colors.textMuted,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "Revisit >",
                                    color = colors.accentHighlight,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        // COLLAPSED BOTTOM FLOATING WRITING DOCK (Shifted upward with balanced spacing above navigation dock)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 102.dp) // Perfectly centered between bottom dock and upper content
                .padding(horizontal = 20.dp)
                .fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(9999.dp))
                    .background(colors.surfaceElevated)
                    .border(
                        width = 1.dp,
                        color = colors.surfaceBorder.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(9999.dp)
                    )
                    .clickable { isWritingDockOpen = true }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .testTag("collapsed_writing_dock")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(colors.accentPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Pencil icon",
                                tint = colors.onAccent,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Write here...",
                                color = colors.textHigh,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Tap to open undisturbed canvas",
                                color = colors.textMuted,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(9999.dp))
                            .background(colors.accentPrimary)
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Quick entry",
                            color = colors.onAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // EXPANDED UNDISTURBED WRITING CANVAS
        WritingDockSheet(
            isOpen = isWritingDockOpen,
            initialEntry = todayEntry,
            onClose = { isWritingDockOpen = false },
            onSave = { text, mood, tags ->
                viewModel.saveJournal(text, mood, tags)
            }
        )
    }
}
