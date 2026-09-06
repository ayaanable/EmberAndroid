package com.example.data.model

import androidx.compose.ui.graphics.Color

enum class AccentTheme(
    val title: String,
    val primaryColor: Color,
    val highlightColor: Color,
    val deepColor: Color,
    val onPrimaryColor: Color
) {
    ORANGE(
        title = "Warm Orange",
        primaryColor = Color(0xFFF27D26),
        highlightColor = Color(0xFFFFA159),
        deepColor = Color(0xFFC95F12),
        onPrimaryColor = Color(0xFF121212)
    ),
    SAGE(
        title = "Sage Green",
        primaryColor = Color(0xFF7E9A78),
        highlightColor = Color(0xFF9AB494),
        deepColor = Color(0xFF5E7858),
        onPrimaryColor = Color(0xFF151D14)
    ),
    BLUE(
        title = "Muted Blue",
        primaryColor = Color(0xFF6B8EAA),
        highlightColor = Color(0xFF88A9C4),
        deepColor = Color(0xFF4F708B),
        onPrimaryColor = Color(0xFF0F1D28)
    ),
    PINK(
        title = "Soft Pastel Pink",
        primaryColor = Color(0xFFD89A9E),
        highlightColor = Color(0xFFE8B6B9),
        deepColor = Color(0xFFBA7C81),
        onPrimaryColor = Color(0xFF2B1315)
    );

    companion object {
        fun fromName(name: String?): AccentTheme {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: ORANGE
        }
    }
}

enum class AppearanceMode(val label: String) {
    DARK("Dark"),
    LIGHT("Light"),
    SYSTEM("System");

    companion object {
        fun fromName(name: String?): AppearanceMode {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: DARK
        }
    }
}

enum class MoodLevel(
    val level: Int,
    val title: String,
    val descriptor: String
) {
    VERY_LOW(1, "Very Low", "Deep heavy"),
    LOW(2, "Low", "Unsettled"),
    NEUTRAL(3, "Neutral", "Equilibrium"),
    GOOD(4, "Good", "Reflective & calm"),
    GREAT(5, "Great", "Radiant clarity");

    companion object {
        fun fromLevel(level: Int): MoodLevel {
            return entries.firstOrNull { it.level == level } ?: NEUTRAL
        }
    }
}
