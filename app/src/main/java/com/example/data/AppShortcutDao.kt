package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.model.AppShortcut
import kotlinx.coroutines.flow.Flow

@Dao
interface AppShortcutDao {
    @Query("SELECT * FROM app_shortcuts ORDER BY isPinned DESC, orderIndex ASC")
    fun getAllShortcuts(): Flow<List<AppShortcut>>

    @Query("SELECT * FROM app_shortcuts WHERE isPinned = 1 ORDER BY orderIndex ASC")
    fun getPinnedShortcuts(): Flow<List<AppShortcut>>

    @Query("SELECT * FROM app_shortcuts WHERE isEssential = 1")
    fun getEssentialShortcuts(): Flow<List<AppShortcut>>

    @Query("SELECT * FROM app_shortcuts WHERE packageName = :packageName LIMIT 1")
    suspend fun getShortcut(packageName: String): AppShortcut?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(shortcut: AppShortcut)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(shortcuts: List<AppShortcut>)

    @Query("UPDATE app_shortcuts SET isPinned = :isPinned WHERE packageName = :packageName")
    suspend fun setPinned(packageName: String, isPinned: Boolean)

    @Query("UPDATE app_shortcuts SET isEssential = :isEssential WHERE packageName = :packageName")
    suspend fun setEssential(packageName: String, isEssential: Boolean)

    @Query("UPDATE app_shortcuts SET dailyLimitMinutes = :limitMinutes WHERE packageName = :packageName")
    suspend fun setDailyLimit(packageName: String, limitMinutes: Int)

    @Query("UPDATE app_shortcuts SET customLabel = :label WHERE packageName = :packageName")
    suspend fun setCustomLabel(packageName: String, label: String)

    @Query("DELETE FROM app_shortcuts WHERE packageName = :packageName")
    suspend fun delete(packageName: String)
}
