package com.example.ui.screens.journal

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailyEntry
import com.example.ui.components.MoodSelector
import com.example.ui.theme.LocalEmberColors

@Composable
fun WritingDockSheet(
    isOpen: Boolean,
    initialEntry: DailyEntry?,
    onClose: () -> Unit,
    onSave: (text: String, mood: Int, tags: List<String>) -> Unit
) {
    AnimatedVisibility(
        visible = isOpen,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it })
    ) {
        val colors = LocalEmberColors.current

        var journalText by remember { mutableStateOf("") }
        var currentMood by remember { mutableIntStateOf(4) }
        val selectedTags = remember { mutableStateListOf<String>() }
        var customTagInput by remember { mutableStateOf("") }
        var isAddingCustomTag by remember { mutableStateOf(false) }

        val defaultTags = listOf("Study", "College", "Friends", "Family", "Work", "Health", "Personal")

        LaunchedEffect(initialEntry, isOpen) {
            if (isOpen) {
                journalText = initialEntry?.journalText ?: ""
                currentMood = initialEntry?.mood ?: 4
                selectedTags.clear()
                if (!initialEntry?.tags.isNullOrBlank()) {
                    val split = initialEntry?.tags?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }
                    if (split != null) {
                        selectedTags.addAll(split)
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.canvas)
                .statusBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .navigationBarsPadding()
                    .imePadding()
            ) {
                // Top Bar: Close / Encrypted badge / Save
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(colors.surfaceElevated)
                            .testTag("close_writing_dock")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close writing canvas",
                            tint = colors.textMedium
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(9999.dp))
                            .background(colors.surfaceElevated)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Encrypted storage",
                            tint = colors.accentHighlight,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Encrypted",
                            color = colors.textMedium,
                            fontSize = 11.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(9999.dp))
                            .background(colors.accentPrimary)
                            .clickable {
                                onSave(journalText, currentMood, selectedTags.toList())
                                onClose()
                            }
                            .padding(horizontal = 18.dp, vertical = 8.dp)
                            .testTag("save_journal_entry")
                    ) {
                        Text(
                            text = "Save",
                            color = colors.onAccent,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "State of Mind",
                    color = colors.textMuted,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(colors.surfaceTile)
                        .padding(16.dp)
                ) {
                    MoodSelector(
                        selectedMood = currentMood,
                        onMoodSelected = { currentMood = it },
                        showDescriptor = true
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Tags",
                    color = colors.textMuted,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    defaultTags.forEach { tag ->
                        val isSelected = selectedTags.contains(tag)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(9999.dp))
                                .background(if (isSelected) colors.accentPrimary else colors.surfaceTile)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) Color.Transparent else colors.surfaceBorder,
                                    shape = RoundedCornerShape(9999.dp)
                                )
                                .clickable {
                                    if (isSelected) selectedTags.remove(tag) else selectedTags.add(tag)
                                }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = tag,
                                color = if (isSelected) colors.onAccent else colors.textMedium,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }

                    selectedTags.filter { !defaultTags.contains(it) }.forEach { customTag ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(9999.dp))
                                .background(colors.accentPrimary)
                                .clickable { selectedTags.remove(customTag) }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = customTag,
                                color = colors.onAccent,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    if (!isAddingCustomTag) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(9999.dp))
                                .background(colors.surfaceElevated)
                                .clickable { isAddingCustomTag = true }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add custom tag",
                                    tint = colors.textMedium,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Add tag",
                                    color = colors.textMedium,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(9999.dp))
                                .background(colors.surfaceElevated)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            OutlinedTextField(
                                value = customTagInput,
                                onValueChange = { customTagInput = it },
                                placeholder = { Text("tag", fontSize = 11.sp, color = colors.textMuted) },
                                singleLine = true,
                                textStyle = TextStyle(fontSize = 12.sp, color = colors.textHigh),
                                modifier = Modifier.width(90.dp),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                )
                            )
                            IconButton(
                                onClick = {
                                    if (customTagInput.isNotBlank()) {
                                        selectedTags.add(customTagInput.trim())
                                        customTagInput = ""
                                        isAddingCustomTag = false
                                    }
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Confirm tag",
                                    tint = colors.accentHighlight,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Writing Area
                Text(
                    text = "Journal Canvas",
                    color = colors.textMuted,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = journalText,
                    onValueChange = { journalText = it },
                    placeholder = {
                        Text(
                            text = "Write here without distraction... thoughts, stillness, daily reflections.",
                            color = colors.textMuted.copy(alpha = 0.6f),
                            fontSize = 16.sp,
                            fontFamily = FontFamily.Serif,
                            lineHeight = 26.sp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(colors.surfaceTile)
                        .testTag("journal_input_canvas"),
                    textStyle = TextStyle(
                        fontSize = 16.sp,
                        fontFamily = FontFamily.Serif,
                        color = colors.textHigh,
                        lineHeight = 26.sp
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = colors.accentPrimary
                    )
                )

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}
