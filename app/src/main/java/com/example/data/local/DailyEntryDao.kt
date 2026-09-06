package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.DailyEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyEntryDao {
    @Query("SELECT * FROM daily_entries ORDER BY date DESC, id DESC")
    fun getAllEntries(): Flow<List<DailyEntry>>

    @Query("SELECT * FROM daily_entries WHERE date = :date LIMIT 1")
    fun getEntryForDate(date: String): Flow<DailyEntry?>

    @Query("SELECT * FROM daily_entries WHERE date = :date LIMIT 1")
    suspend fun getEntryForDateOnce(date: String): DailyEntry?

    @Query("SELECT * FROM daily_entries WHERE date LIKE :monthPrefix || '%' ORDER BY date ASC")
    fun getEntriesForMonth(monthPrefix: String): Flow<List<DailyEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entry: DailyEntry): Long

    @Update
    suspend fun update(entry: DailyEntry)

    @Query("DELETE FROM daily_entries WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM daily_entries")
    fun getEntryCount(): Flow<Int>
}
