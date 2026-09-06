package com.example.ui.screens.lock

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalEmberColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinSetupDialog(
    onDismiss: () -> Unit,
    onPinConfirmed: (String) -> Unit
) {
    val colors = LocalEmberColors.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var step by remember { mutableStateOf(1) } // 1 = Create, 2 = Confirm
    var firstPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val currentInput = if (step == 1) firstPin else confirmPin

    fun onDigit(d: String) {
        if (step == 1) {
            if (firstPin.length < 4) {
                firstPin += d
                if (firstPin.length == 4) {
                    step = 2
                    errorMessage = null
                }
            }
        } else {
            if (confirmPin.length < 4) {
                confirmPin += d
                if (confirmPin.length == 4) {
                    if (confirmPin == firstPin) {
                        onPinConfirmed(confirmPin)
                        onDismiss()
                    } else {
                        errorMessage = "PINs do not match. Try again."
                        step = 1
                        firstPin = ""
                        confirmPin = ""
                    }
                }
            }
        }
    }

    fun onBackspace() {
        if (step == 1) {
            if (firstPin.isNotEmpty()) firstPin = firstPin.dropLast(1)
        } else {
            if (confirmPin.isNotEmpty()) {
                confirmPin = confirmPin.dropLast(1)
            } else {
                step = 1
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.surfaceElevated,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (step == 1) "Create 4-Digit PIN" else "Confirm PIN",
                    color = colors.textHigh,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cancel",
                        tint = colors.textMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (step == 1) "Enter a 4-digit PIN to secure Ember." else "Re-enter your 4-digit PIN to confirm.",
                color = colors.textMuted,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 4 Dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until 4) {
                    val isFilled = i < currentInput.length
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(if (isFilled) colors.accentPrimary else Color.Transparent)
                            .border(
                                width = 1.5.dp,
                                color = if (isFilled) colors.accentPrimary else colors.textMuted.copy(alpha = 0.5f),
                                shape = CircleShape
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Box(modifier = Modifier.height(20.dp)) {
                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = Color(0xFFE57373),
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Numeric Keypad
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9")
                ).forEach { row ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(18.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        row.forEach { digit ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                                    .clip(CircleShape)
                                    .background(colors.surfaceTile)
                                    .clickable { onDigit(digit) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = digit,
                                    color = colors.textHigh,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.weight(1f))

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .clip(CircleShape)
                            .background(colors.surfaceTile)
                            .clickable { onDigit("0") },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "0",
                            color = colors.textHigh,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .clip(CircleShape)
                            .background(colors.surfaceElevated)
                            .clickable { onBackspace() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Backspace,
                            contentDescription = "Backspace",
                            tint = colors.textMedium,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
