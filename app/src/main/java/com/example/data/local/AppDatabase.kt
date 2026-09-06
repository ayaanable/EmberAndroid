package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.DailyEntry
import com.example.data.model.Task
import com.example.data.model.UserProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Database(
    entities = [DailyEntry::class, Task::class, UserProfile::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dailyEntryDao(): DailyEntryDao
    abstract fun taskDao(): TaskDao
    abstract fun userProfileDao(): UserProfileDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ember_database.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Use the context to get the instance we're currently building
                            // This ensures the seeding coroutine has a valid reference
                            CoroutineScope(Dispatchers.IO).launch {
                                val dbInstance = getInstance(context.applicationContext)
                                populateInitialData(dbInstance)
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Robust migration to handle potential existing duplicate dates
                // 1. Create a temporary table with the unique constraint
                db.execSQL("""
                    CREATE TABLE daily_entries_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        date TEXT NOT NULL,
                        mood INTEGER NOT NULL,
                        moodIntensity REAL NOT NULL,
                        journalText TEXT NOT NULL,
                        tags TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                """.trimIndent())
                
                // 2. Create the unique index
                db.execSQL("CREATE UNIQUE INDEX index_daily_entries_date ON daily_entries_new (date)")

                // 3. Copy data from old table, keeping the most recently updated entry for each date
                db.execSQL("""
                    INSERT INTO daily_entries_new (id, date, mood, moodIntensity, journalText, tags, createdAt, updatedAt)
                    SELECT id, date, mood, moodIntensity, journalText, tags, createdAt, MAX(updatedAt)
                    FROM daily_entries
                    GROUP BY date
                """.trimIndent())

                // 4. Swap tables
                db.execSQL("DROP TABLE daily_entries")
                db.execSQL("ALTER TABLE daily_entries_new RENAME TO daily_entries")
            }
        }

        private suspend fun populateInitialData(db: AppDatabase) {
            val profileDao = db.userProfileDao()
            profileDao.insertOrUpdateProfile(
                UserProfile(
                    id = 1,
                    name = "User",
                    profileImagePath = null,
                    selectedTheme = "DARK",
                    selectedAccentColour = "ORANGE"
                )
            )

            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -1)
            val yesterdayStr = dateFormat.format(cal.time)

            // Seed a single introductory journal entry that acts as an onboarding guide
            val entryDao = db.dailyEntryDao()
            entryDao.insertOrUpdate(
                DailyEntry(
                    date = yesterdayStr,
                    mood = 5,
                    moodIntensity = 1.0f,
                    journalText = """
                        Welcome to Ember! Here is how you can use your new private space:
                        
                        📔 Write daily reflections in the Journal tab.
                        🔐 Everything is encrypted and stays only on your device.
                        📊 Track your mood and view trends in the History tab.
                        ✅ Manage daily tasks and build streaks in the Today tab.
                        ✨ Personalize your theme and accent colors in Profile.
                    """.trimIndent(),
                    tags = "Guide",
                    createdAt = System.currentTimeMillis() - 1000 * 60 * 60 * 24
                )
            )
        }
    }
}
