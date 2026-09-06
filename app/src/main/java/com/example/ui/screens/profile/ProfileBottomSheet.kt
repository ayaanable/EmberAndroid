package com.example.ui.screens.profile

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import androidx.compose.ui.unit.sp
import com.example.data.model.AccentTheme
import com.example.data.model.AppearanceMode
import com.example.data.security.BiometricHelper
import com.example.data.security.LockTimeoutPolicy
import com.example.ui.components.AvatarBadge
import com.example.ui.components.TactileBubbleCard
import com.example.ui.screens.lock.PinSetupDialog
import com.example.ui.theme.LocalEmberColors
import com.example.ui.viewmodel.EmberViewModel
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileBottomSheet(
    viewModel: EmberViewModel,
    onDismiss: () -> Unit
) {
    val colors = LocalEmberColors.current
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val profile by viewModel.userProfile.collectAsState()
    var isEditingName by remember { mutableStateOf(false) }
    var enteredName by remember { mutableStateOf(profile.name) }

    var showPinSetup by remember { mutableStateOf(false) }
    var showPhotoOptions by remember { mutableStateOf(false) }

    val currentAppearance = AppearanceMode.fromName(profile.selectedTheme)
    val currentAccent = AccentTheme.fromName(profile.selectedAccentColour)

    var appLockEnabled by remember { mutableStateOf(viewModel.isAppLockEnabled()) }
    var biometricEnabled by remember { mutableStateOf(viewModel.isBiometricEnabled()) }
    var lockTimeout by remember { mutableStateOf(viewModel.getLockTimeoutPolicy()) }

    // Android Photo Picker Launcher (zero-permission)
    val galleryPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            saveImageFromUri(context, uri) { path ->
                viewModel.updateProfilePicture(path)
            }
        }
    }

    // Camera Launcher (returns Bitmap without requiring storage permissions)
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            saveImageFromBitmap(context, bitmap) { path ->
                viewModel.updateProfilePicture(path)
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.canvas,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            // Header: Title & Close
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Profile & Settings",
                    color = colors.textHigh,
                    fontSize = 20.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.SemiBold
                )

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(colors.surfaceElevated)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close settings",
                        tint = colors.textMedium,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 1. PROFILE PICTURE & NAME
            TactileBubbleCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = colors.surfaceTile,
                cornerRadius = 24.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AvatarBadge(
                        name = profile.name,
                        imagePath = profile.profileImagePath,
                        size = 76.dp,
                        onClick = { showPhotoOptions = true }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Change photo",
                        color = colors.accentHighlight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable { showPhotoOptions = true }
                    )

                    // Photo options row
                    if (showPhotoOptions) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(9999.dp))
                                    .background(colors.surfaceElevated)
                                    .clickable {
                                        showPhotoOptions = false
                                        cameraLauncher.launch(null)
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = "Take picture",
                                        tint = colors.textMedium,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "Camera",
                                        color = colors.textMedium,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(start = 4.dp)
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(9999.dp))
                                    .background(colors.surfaceElevated)
                                    .clickable {
                                        showPhotoOptions = false
                                        galleryPickerLauncher.launch(
                                            androidx.activity.result.PickVisualMediaRequest(
                                                ActivityResultContracts.PickVisualMedia.ImageOnly
                                            )
                                        )
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.PhotoLibrary,
                                        contentDescription = "Gallery",
                                        tint = colors.textMedium,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "Gallery",
                                        color = colors.textMedium,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(start = 4.dp)
                                    )
                                }
                            }

                            if (!profile.profileImagePath.isNullOrBlank()) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(9999.dp))
                                        .background(colors.surfaceElevated)
                                        .clickable {
                                            showPhotoOptions = false
                                            deleteSavedImage(context, profile.profileImagePath)
                                            viewModel.updateProfilePicture(null)
                                        }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Remove photo",
                                            tint = Color(0xFFE57373),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = "Remove",
                                            color = Color(0xFFE57373),
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(start = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Editable Name
                    if (!isEditingName) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable {
                                enteredName = profile.name
                                isEditingName = true
                            }
                        ) {
                            Text(
                                text = profile.name,
                                color = colors.textHigh,
                                fontSize = 20.sp,
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Normal
                            )
                            Text(
                                text = " (edit)",
                                color = colors.textMuted,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 6.dp)
                            )
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = enteredName,
                                onValueChange = { enteredName = it },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(colors.surfaceElevated),
                                textStyle = TextStyle(fontSize = 15.sp, color = colors.textHigh),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(9999.dp))
                                    .background(colors.accentPrimary)
                                    .clickable {
                                        if (enteredName.isNotBlank()) {
                                            viewModel.updateProfileName(enteredName.trim())
                                        }
                                        isEditingName = false
                                    }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "Done",
                                    color = colors.onAccent,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. APPEARANCE (Dark, Light, System)
            Text(
                text = "APPEARANCE",
                color = colors.textMuted,
                fontSize = 11.sp,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AppearanceMode.entries.forEach { mode ->
                    val isSelected = currentAppearance == mode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(18.dp))
                            .background(if (isSelected) colors.accentPrimary else colors.surfaceTile)
                            .clickable { viewModel.setAppearanceMode(mode) }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = mode.label,
                            color = if (isSelected) colors.onAccent else colors.textMedium,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3. ACCENT THEMES (Warm Orange, Sage Green, Muted Blue, Soft Pastel Pink)
            Text(
                text = "ACCENT THEME",
                color = colors.textMuted,
                fontSize = 11.sp,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AccentTheme.entries.forEach { theme ->
                    val isSelected = currentAccent == theme
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { viewModel.setAccentTheme(theme) }
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .border(
                                    width = if (isSelected) 2.5.dp else 0.dp,
                                    color = if (isSelected) colors.textHigh else Color.Transparent,
                                    shape = CircleShape
                                )
                                .padding(4.dp)
                                .clip(CircleShape)
                                .background(theme.primaryColor),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = theme.onPrimaryColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = theme.title.split(" ").last(),
                            color = if (isSelected) colors.textHigh else colors.textMuted,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // 4. SECURITY (App Lock, Biometric, Timeout, Change PIN)
            Text(
                text = "SECURITY",
                color = colors.textMuted,
                fontSize = 11.sp,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            TactileBubbleCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = colors.surfaceTile,
                cornerRadius = 24.dp
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // App Lock Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "App Lock",
                                color = colors.textHigh,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Secure journal with biometric and 4-digit PIN",
                                color = colors.textMuted,
                                fontSize = 11.sp
                            )
                        }

                        Switch(
                            checked = appLockEnabled,
                            onCheckedChange = { enable ->
                                if (enable) {
                                    showPinSetup = true
                                } else {
                                    viewModel.disableAppLock()
                                    appLockEnabled = false
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = colors.onAccent,
                                checkedTrackColor = colors.accentPrimary
                            )
                        )
                    }

                    if (appLockEnabled) {
                        Spacer(modifier = Modifier.height(16.dp))

                        // Fingerprint toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Fingerprint,
                                    contentDescription = "Fingerprint",
                                    tint = colors.accentHighlight,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Fingerprint Unlock",
                                    color = colors.textHigh,
                                    fontSize = 14.sp
                                )
                            }

                            Switch(
                                checked = biometricEnabled,
                                onCheckedChange = {
                                    biometricEnabled = it
                                    viewModel.setBiometricEnabled(it)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = colors.onAccent,
                                    checkedTrackColor = colors.accentPrimary
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Change PIN button
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showPinSetup = true }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Change 4-digit PIN",
                                color = colors.accentHighlight,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = ">",
                                color = colors.textMuted,
                                fontSize = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Lock Timeout Policy
                        Text(
                            text = "LOCK TIMEOUT",
                            color = colors.textMuted,
                            fontSize = 10.sp,
                            letterSpacing = 0.8.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        LockTimeoutPolicy.entries.forEach { policy ->
                            val isSelected = lockTimeout == policy
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        lockTimeout = policy
                                        viewModel.setLockTimeoutPolicy(policy)
                                    }
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = policy.label,
                                    color = if (isSelected) colors.textHigh else colors.textMedium,
                                    fontSize = 13.sp
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = colors.accentPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 5. PRIVACY MANIFESTO
            TactileBubbleCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = colors.surfaceTile,
                cornerRadius = 24.dp
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Privacy Shield",
                            tint = colors.accentHighlight,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "100% OFFLINE & PRIVATE",
                            color = colors.accentHighlight,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Your journal entries, moods, tasks, and reflections exist strictly on your phone. Ember uses zero cloud databases, zero telemetry, and zero remote servers.",
                        color = colors.textMedium,
                        fontSize = 13.sp,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Hardware KeyStore encryption protects your sensitive thoughts on-device.",
                        color = colors.textMuted,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 6. ABOUT
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFF161618))
                        .border(1.dp, colors.surfaceBorder, RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_ember_logo),
                        contentDescription = "Ember App Icon",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(54.dp)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Ember v1.0",
                    color = colors.textHigh,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "A sanctuary for quiet reflection",
                    color = colors.textMuted,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(30.dp))
        }

        // PIN Setup / Change Dialog
        if (showPinSetup) {
            PinSetupDialog(
                onDismiss = {
                    showPinSetup = false
                    appLockEnabled = viewModel.isAppLockEnabled()
                },
                onPinConfirmed = { pin ->
                    viewModel.saveNewPin(pin)
                    appLockEnabled = true
                }
            )
        }
    }
}

private fun saveImageFromUri(context: Context, uri: Uri, onSaved: (String) -> Unit) {
    try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return
        val outputFile = File(context.filesDir, "profile_avatar.jpg")
        FileOutputStream(outputFile).use { output ->
            inputStream.copyTo(output)
        }
        onSaved(outputFile.absolutePath)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun saveImageFromBitmap(context: Context, bitmap: Bitmap, onSaved: (String) -> Unit) {
    try {
        val outputFile = File(context.filesDir, "profile_avatar.jpg")
        FileOutputStream(outputFile).use { output ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, output)
        }
        onSaved(outputFile.absolutePath)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun deleteSavedImage(context: Context, path: String?) {
    if (path == null) return
    try {
        val file = File(path)
        if (file.exists()) {
            file.delete()
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
