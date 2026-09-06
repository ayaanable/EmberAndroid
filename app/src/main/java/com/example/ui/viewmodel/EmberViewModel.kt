package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.AccentTheme
import com.example.data.model.AppearanceMode
import com.example.data.model.DailyEntry
import com.example.data.model.Task
import com.example.data.model.UserProfile
import com.example.data.repository.EmberRepository
import com.example.data.security.LockTimeoutPolicy
import com.example.data.security.SecurityManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class EmberViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val securityManager = SecurityManager.getInstance(application)
    val repository = EmberRepository(database, securityManager)

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val todayDateString: String = dateFormat.format(Date())

    val isLocked: StateFlow<Boolean> = securityManager.isLocked

    val userProfile: StateFlow<UserProfile> = repository.getUserProfile()
        .map { it ?: UserProfile(name = "User", selectedTheme = "DARK", selectedAccentColour = "ORANGE") }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserProfile(name = "User", selectedTheme = "DARK", selectedAccentColour = "ORANGE")
        )

    val allEntries: StateFlow<List<DailyEntry>> = repository.getAllEntries()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val todayEntry: StateFlow<DailyEntry?> = repository.getEntryForDate(todayDateString)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val todayTasks: StateFlow<List<Task>> = repository.getTasksForDate(todayDateString)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allTasks: StateFlow<List<Task>> = repository.getAllTasks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Current selected mood on Home screen (defaults to today's entry mood or 4)
    private val _currentSelectedMood = MutableStateFlow(4)
    val currentSelectedMood: StateFlow<Int> = _currentSelectedMood.asStateFlow()

    private val entryMutex = Mutex()

    init {
        viewModelScope.launch {
            todayEntry.collect { entry ->
                if (entry != null) {
                    _currentSelectedMood.value = entry.mood
                }
            }
        }
    }

    fun setSelectedMood(mood: Int) {
        _currentSelectedMood.value = mood
        viewModelScope.launch {
            entryMutex.withLock {
                val existing = repository.getEntryForDateOnce(todayDateString)
                if (existing != null) {
                    repository.saveJournalEntry(existing.copy(mood = mood))
                } else {
                    repository.saveJournalEntry(
                        DailyEntry(
                            date = todayDateString,
                            mood = mood,
                            moodIntensity = 1.0f,
                            journalText = ""
                        )
                    )
                }
            }
        }
    }

    fun saveJournal(text: String, mood: Int, tags: List<String>) {
        viewModelScope.launch {
            entryMutex.withLock {
                val existing = repository.getEntryForDateOnce(todayDateString)
                val tagsStr = tags.joinToString(", ")
                if (existing != null) {
                    repository.saveJournalEntry(
                        existing.copy(
                            journalText = text,
                            mood = mood,
                            tags = tagsStr
                        )
                    )
                } else {
                    repository.saveJournalEntry(
                        DailyEntry(
                            date = todayDateString,
                            mood = mood,
                            moodIntensity = 1.0f,
                            journalText = text,
                            tags = tagsStr
                        )
                    )
                }
            }
        }
    }

    // --- Task Actions ---
    fun toggleTask(task: Task) {
        viewModelScope.launch {
            repository.toggleTaskCompleted(task)
        }
    }

    fun addTask(
        title: String,
        description: String,
        duration: String,
        category: String,
        date: String = todayDateString
    ) {
        viewModelScope.launch {
            repository.insertTask(
                Task(
                    date = date,
                    title = title,
                    description = description,
                    duration = duration,
                    category = category,
                    completed = false
                )
            )
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            repository.updateTask(task)
        }
    }

    fun deleteTask(id: Long) {
        viewModelScope.launch {
            repository.deleteTask(id)
        }
    }

    // --- User Profile & Theme ---
    fun updateProfileName(name: String) {
        viewModelScope.launch {
            val current = userProfile.value
            repository.updateProfile(current.copy(name = name))
        }
    }

    fun updateProfilePicture(imagePath: String?) {
        viewModelScope.launch {
            val current = userProfile.value
            repository.updateProfile(current.copy(profileImagePath = imagePath))
        }
    }

    fun setAppearanceMode(mode: AppearanceMode) {
        viewModelScope.launch {
            val current = userProfile.value
            repository.updateProfile(current.copy(selectedTheme = mode.name))
        }
    }

    fun setAccentTheme(theme: AccentTheme) {
        viewModelScope.launch {
            val current = userProfile.value
            repository.updateProfile(current.copy(selectedAccentColour = theme.name))
        }
    }

    // --- Security & PIN ---
    fun isAppLockEnabled(): Boolean = securityManager.isAppLockEnabled()

    fun isBiometricEnabled(): Boolean = securityManager.isBiometricEnabled()

    fun getLockTimeoutPolicy(): LockTimeoutPolicy = securityManager.getLockTimeoutPolicy()

    fun setBiometricEnabled(enabled: Boolean) {
        securityManager.setBiometricEnabled(enabled)
    }

    fun setLockTimeoutPolicy(policy: LockTimeoutPolicy) {
        securityManager.setLockTimeoutPolicy(policy)
    }

    fun disableAppLock() {
        securityManager.setAppLockEnabled(false)
    }

    fun saveNewPin(pin: String): Boolean {
        return securityManager.savePin(pin)
    }

    fun verifyPin(pin: String): Boolean {
        return securityManager.verifyPin(pin)
    }

    fun unlockByBiometric() {
        securityManager.unlockByBiometric()
    }

    fun lockNow() {
        securityManager.lockNow()
    }

    fun onAppForegrounded() {
        securityManager.onAppForegrounded()
    }

    fun onAppBackgrounded() {
        securityManager.onAppBackgrounded()
    }
}
