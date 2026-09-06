package com.example.ui.screens.lock

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.example.data.security.BiometricHelper
import com.example.ui.theme.LocalEmberColors
import com.example.ui.viewmodel.EmberViewModel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun PinLockScreen(
    viewModel: EmberViewModel,
    modifier: Modifier = Modifier
) {
    val colors = LocalEmberColors.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var enteredPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val shakeOffset = remember { Animatable(0f) }

    val isBiometricAvailable = remember { BiometricHelper.isBiometricAvailable(context) }
    val isBiometricEnabled = viewModel.isBiometricEnabled()

    // Trigger biometric prompt on initial launch if enabled
    LaunchedEffect(Unit) {
        if (isBiometricAvailable && isBiometricEnabled) {
            val activity = context as? FragmentActivity
            if (activity != null) {
                BiometricHelper.showBiometricPrompt(
                    activity = activity,
                    onSuccess = {
                        viewModel.unlockByBiometric()
                    },
                    onFallbackOrError = {
                        // Stay on PIN screen
                    }
                )
            }
        }
    }

    fun triggerShake() {
        coroutineScope.launch {
            shakeOffset.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = 400
                    0f at 0
                    -20f at 50
                    20f at 100
                    -15f at 150
                    15f at 200
                    -8f at 250
                    8f at 300
                    0f at 400
                }
            )
        }
    }

    fun onDigitPress(digit: String) {
        if (enteredPin.length < 4) {
            val next = enteredPin + digit
            enteredPin = next
            errorMessage = null

            if (next.length == 4) {
                val success = viewModel.verifyPin(next)
                if (!success) {
                    errorMessage = "Incorrect PIN"
                    triggerShake()
                    enteredPin = ""
                }
            }
        }
    }

    fun onBackspacePress() {
        if (enteredPin.isNotEmpty()) {
            enteredPin = enteredPin.dropLast(1)
            errorMessage = null
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.canvas)
            .statusBarsPadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Minimal flame emblem
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(colors.accentDeep.copy(alpha = 0.3f))
                    .border(1.dp, colors.accentPrimary.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(colors.accentPrimary)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Ember",
                color = colors.textHigh,
                fontSize = 28.sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Normal
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Private & offline journal",
                color = colors.textMuted,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(36.dp))

            // 4 Animated PIN Dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.offset { IntOffset(shakeOffset.value.roundToInt(), 0) }
            ) {
                for (i in 0 until 4) {
                    val isFilled = i < enteredPin.length
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(if (isFilled) colors.accentPrimary else Color.Transparent)
                            .border(
                                width = 1.5.dp,
                                color = if (isFilled) colors.accentPrimary else colors.textMuted.copy(alpha = 0.6f),
                                shape = CircleShape
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Error message
            Box(modifier = Modifier.height(22.dp)) {
                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = Color(0xFFE57373),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // 3x4 Tactile Keypad
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val keypadRows = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9")
                )

                keypadRows.forEach { row ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        row.forEach { digit ->
                            KeypadButton(
                                label = digit,
                                modifier = Modifier.weight(1f),
                                onClick = { onDigitPress(digit) }
                            )
                        }
                    }
                }

                // Bottom row: Biometric / 0 / Backspace
                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Biometric button (left)
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isBiometricAvailable && isBiometricEnabled) {
                            IconButton(
                                onClick = {
                                    val activity = context as? FragmentActivity
                                    if (activity != null) {
                                        BiometricHelper.showBiometricPrompt(
                                            activity = activity,
                                            onSuccess = { viewModel.unlockByBiometric() },
                                            onFallbackOrError = {}
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(colors.surfaceElevated)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Fingerprint,
                                    contentDescription = "Fingerprint unlock",
                                    tint = colors.accentHighlight,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }

                    // Digit 0
                    KeypadButton(
                        label = "0",
                        modifier = Modifier.weight(1f),
                        onClick = { onDigitPress("0") }
                    )

                    // Backspace button (right)
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = { onBackspacePress() },
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(colors.surfaceElevated)
                                .testTag("pin_backspace")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Backspace,
                                contentDescription = "Backspace",
                                tint = colors.textMedium,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KeypadButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalEmberColors.current

    Box(
        modifier = modifier
            .height(64.dp)
            .clip(CircleShape)
            .background(colors.surfaceTile)
            .border(1.dp, colors.surfaceBorder.copy(alpha = 0.4f), CircleShape)
            .clickable(onClick = onClick)
            .testTag("pin_key_$label"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = colors.textHigh,
            fontSize = 22.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
