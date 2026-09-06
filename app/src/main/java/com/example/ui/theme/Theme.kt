package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.example.data.model.AccentTheme
import com.example.data.model.AppearanceMode

@Immutable
data class EmberColors(
    val canvas: Color,
    val surfaceTile: Color,
    val surfaceElevated: Color,
    val surfaceBorder: Color,
    val textHigh: Color,
    val textMedium: Color,
    val textMuted: Color,
    val accentPrimary: Color,
    val accentHighlight: Color,
    val accentDeep: Color,
    val onAccent: Color,
    val isDark: Boolean
)

val LocalEmberColors = staticCompositionLocalOf<EmberColors> {
    error("No EmberColors provided")
}

val LocalAccentTheme = staticCompositionLocalOf<AccentTheme> {
    AccentTheme.ORANGE
}

@Composable
fun EmberTheme(
    appearanceMode: AppearanceMode = AppearanceMode.DARK,
    accentTheme: AccentTheme = AccentTheme.ORANGE,
    content: @Composable () -> Unit
) {
    val isDark = when (appearanceMode) {
        AppearanceMode.DARK -> true
        AppearanceMode.LIGHT -> false
        AppearanceMode.SYSTEM -> isSystemInDarkTheme()
    }

    val emberColors = if (isDark) {
        EmberColors(
            canvas = CanvasDark,
            surfaceTile = SurfaceTileDark,
            surfaceElevated = SurfaceElevatedDark,
            surfaceBorder = SurfaceBorderDark,
            textHigh = TextHighDark,
            textMedium = TextMediumDark,
            textMuted = TextMutedDark,
            accentPrimary = accentTheme.primaryColor,
            accentHighlight = accentTheme.highlightColor,
            accentDeep = accentTheme.deepColor,
            onAccent = accentTheme.onPrimaryColor,
            isDark = true
        )
    } else {
        EmberColors(
            canvas = CanvasLight,
            surfaceTile = SurfaceTileLight,
            surfaceElevated = SurfaceElevatedLight,
            surfaceBorder = SurfaceBorderLight,
            textHigh = TextHighLight,
            textMedium = TextMediumLight,
            textMuted = TextMutedLight,
            accentPrimary = accentTheme.primaryColor,
            accentHighlight = accentTheme.highlightColor,
            accentDeep = accentTheme.deepColor,
            onAccent = accentTheme.onPrimaryColor,
            isDark = false
        )
    }

    val colorScheme = if (isDark) {
        darkColorScheme(
            primary = accentTheme.primaryColor,
            onPrimary = accentTheme.onPrimaryColor,
            primaryContainer = accentTheme.deepColor,
            background = CanvasDark,
            onBackground = TextHighDark,
            surface = SurfaceElevatedDark,
            onSurface = TextHighDark,
            surfaceVariant = SurfaceTileDark,
            onSurfaceVariant = TextMediumDark,
            outline = SurfaceBorderDark
        )
    } else {
        lightColorScheme(
            primary = accentTheme.primaryColor,
            onPrimary = accentTheme.onPrimaryColor,
            primaryContainer = accentTheme.highlightColor,
            background = CanvasLight,
            onBackground = TextHighLight,
            surface = SurfaceElevatedLight,
            onSurface = TextHighLight,
            surfaceVariant = SurfaceTileLight,
            onSurfaceVariant = TextMediumLight,
            outline = SurfaceBorderLight
        )
    }

    CompositionLocalProvider(
        LocalEmberColors provides emberColors,
        LocalAccentTheme provides accentTheme
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = EmberTypography,
            content = content
        )
    }
}

