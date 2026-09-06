package com.example

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import com.example.data.model.AccentTheme
import com.example.data.model.AppearanceMode
import com.example.ui.components.EmberBottomNav
import com.example.ui.components.EmberScreen
import com.example.ui.screens.history.HistoryScreen
import com.example.ui.screens.journal.JournalScreen
import com.example.ui.screens.lock.PinLockScreen
import com.example.ui.screens.profile.ProfileBottomSheet
import com.example.ui.screens.today.TodayScreen
import com.example.ui.theme.EmberTheme
import com.example.ui.viewmodel.EmberViewModel

class MainActivity : FragmentActivity() {

    private val viewModel: EmberViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val userProfile by viewModel.userProfile.collectAsState()
            val isLocked by viewModel.isLocked.collectAsState()

            val appearanceMode = AppearanceMode.fromName(userProfile.selectedTheme)
            val accentTheme = AccentTheme.fromName(userProfile.selectedAccentColour)

            EmberTheme(
                appearanceMode = appearanceMode,
                accentTheme = accentTheme
            ) {
                var currentScreen by remember { mutableStateOf(EmberScreen.JOURNAL) }
                var showProfileSheet by remember { mutableStateOf(false) }

                Crossfade(
                    targetState = isLocked,
                    animationSpec = tween(durationMillis = 300),
                    label = "lock_crossfade"
                ) { locked ->
                    if (locked) {
                        PinLockScreen(viewModel = viewModel)
                    } else {
                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            bottomBar = {
                                EmberBottomNav(
                                    currentScreen = currentScreen,
                                    onScreenSelected = { screen -> currentScreen = screen }
                                )
                            }
                        ) { innerPadding ->
                            // innerPadding is handled with edge-to-edge padding in individual screens
                            Box(modifier = Modifier.fillMaxSize()) {
                                Crossfade(
                                    targetState = currentScreen,
                                    animationSpec = tween(durationMillis = 250),
                                    label = "screen_crossfade"
                                ) { screen ->
                                    when (screen) {
                                        EmberScreen.JOURNAL -> {
                                            JournalScreen(
                                                viewModel = viewModel,
                                                onOpenProfile = { showProfileSheet = true }
                                            )
                                        }
                                        EmberScreen.TODAY -> {
                                            TodayScreen(
                                                viewModel = viewModel,
                                                onOpenProfile = { showProfileSheet = true }
                                            )
                                        }
                                        EmberScreen.HISTORY -> {
                                            HistoryScreen(
                                                viewModel = viewModel,
                                                onOpenProfile = { showProfileSheet = true }
                                            )
                                        }
                                    }
                                }

                                if (showProfileSheet) {
                                    ProfileBottomSheet(
                                        viewModel = viewModel,
                                        onDismiss = { showProfileSheet = false }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.onAppForegrounded()
    }

    override fun onStop() {
        super.onStop()
        viewModel.onAppBackgrounded()
    }
}
