package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.model.DailyEntry
import com.example.data.model.Task
import com.example.data.model.UserProfile
import com.example.data.security.SecurityManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class EmberRepository(
    private val database: AppDatabase,
    private val securityManager: SecurityManager
) {
    private val entryDao = database.dailyEntryDao()
    private val taskDao = database.taskDao()
    private val profileDao = database.userProfileDao()

    // --- Journal Entries ---
    fun getAllEntries(): Flow<List<DailyEntry>> {
        return entryDao.getAllEntries().map { list ->
            list.map { entry ->
                entry.copy(journalText = securityManager.decryptText(entry.journalText))
            }
        }
    }

    fun getEntryForDate(date: String): Flow<DailyEntry?> {
        return entryDao.getEntryForDate(date).map { entry ->
            entry?.copy(journalText = securityManager.decryptText(entry.journalText))
        }
    }

    suspend fun getEntryForDateOnce(date: String): DailyEntry? {
        val entry = entryDao.getEntryForDateOnce(date)
        return entry?.copy(journalText = securityManager.decryptText(entry.journalText))
    }

    fun getEntriesForMonth(monthPrefix: String): Flow<List<DailyEntry>> {
        return entryDao.getEntriesForMonth(monthPrefix).map { list ->
            list.map { entry ->
                entry.copy(journalText = securityManager.decryptText(entry.journalText))
            }
        }
    }

    suspend fun saveJournalEntry(entry: DailyEntry): Long {
        val encryptedText = securityManager.encryptText(entry.journalText)
        val encryptedEntry = entry.copy(
            journalText = encryptedText,
            updatedAt = System.currentTimeMillis()
        )
        return entryDao.insertOrUpdate(encryptedEntry)
    }

    suspend fun deleteJournalEntry(id: Long) {
        entryDao.deleteById(id)
    }

    fun getEntryCount(): Flow<Int> = entryDao.getEntryCount()

    // --- Tasks ---
    fun getTasksForDate(date: String): Flow<List<Task>> = taskDao.getTasksForDate(date)

    fun getAllTasks(): Flow<List<Task>> = taskDao.getAllTasks()

    fun getAllCompletedTasks(): Flow<List<Task>> = taskDao.getAllCompletedTasks()

    suspend fun insertTask(task: Task): Long = taskDao.insertTask(task)

    suspend fun updateTask(task: Task) = taskDao.updateTask(task)

    suspend fun deleteTask(id: Long) = taskDao.deleteTaskById(id)

    suspend fun toggleTaskCompleted(task: Task) {
        val nextCompleted = !task.completed
        val completedAt = if (nextCompleted) System.currentTimeMillis() else null
        taskDao.setTaskCompleted(task.id, nextCompleted, completedAt)
    }

    // --- User Profile ---
    fun getUserProfile(): Flow<UserProfile?> = profileDao.getUserProfile()

    suspend fun getUserProfileOnce(): UserProfile? = profileDao.getUserProfileOnce()

    suspend fun updateProfile(profile: UserProfile) {
        profileDao.insertOrUpdateProfile(profile)
    }
}
