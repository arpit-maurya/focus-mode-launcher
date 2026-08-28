package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.model.AppShortcut
import com.example.model.BlockedNotification
import com.example.model.FocusSession
import com.example.model.PriorityTask

@Database(
    entities = [
        AppShortcut::class,
        FocusSession::class,
        BlockedNotification::class,
        PriorityTask::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun focusDao(): FocusDao
    abstract fun notificationDao(): NotificationDao
    abstract fun priorityTaskDao(): PriorityTaskDao
    abstract fun appShortcutDao(): AppShortcutDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "distraction_free_eink_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
