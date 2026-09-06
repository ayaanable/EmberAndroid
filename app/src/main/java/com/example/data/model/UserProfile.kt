package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey
    val id: Int = 1,
    val name: String = "User",
    val profileImagePath: String? = null,
    val selectedTheme: String = "DARK", // DARK, LIGHT, SYSTEM
    val selectedAccentColour: String = "ORANGE" // ORANGE, SAGE, BLUE, PINK
)
