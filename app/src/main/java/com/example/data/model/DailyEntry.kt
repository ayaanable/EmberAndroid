package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "daily_entries",
    indices = [Index(value = ["date"], unique = true)]
)
data class DailyEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String, // YYYY-MM-DD
    val mood: Int, // 1 to 5
    val moodIntensity: Float = 1.0f,
    val journalText: String, // Encrypted payload or plain if encryption disabled
    val tags: String = "", // Comma-separated
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
