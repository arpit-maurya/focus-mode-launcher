package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.model.BlockedNotification
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Query("SELECT * FROM blocked_notifications ORDER BY timestamp DESC")
    fun getAllBlockedNotifications(): Flow<List<BlockedNotification>>

    @Query("SELECT * FROM blocked_notifications WHERE timestamp >= :sinceTimestamp ORDER BY timestamp DESC")
    fun getBlockedSince(sinceTimestamp: Long): Flow<List<BlockedNotification>>

    @Query("SELECT COUNT(*) FROM blocked_notifications WHERE timestamp >= :sinceTimestamp")
    fun getBlockedCountSince(sinceTimestamp: Long): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlockedNotification(notification: BlockedNotification): Long

    @Query("UPDATE blocked_notifications SET isRead = 1 WHERE isRead = 0")
    suspend fun markAllAsRead()

    @Query("DELETE FROM blocked_notifications")
    suspend fun clearAll()
}
