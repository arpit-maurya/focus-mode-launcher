package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.model.FocusSession
import kotlinx.coroutines.flow.Flow

@Dao
interface FocusDao {
    @Query("SELECT * FROM focus_sessions ORDER BY startTime DESC")
    fun getAllFocusSessions(): Flow<List<FocusSession>>

    @Query("SELECT * FROM focus_sessions WHERE startTime >= :startOfDayTimestamp ORDER BY startTime DESC")
    fun getTodayFocusSessions(startOfDayTimestamp: Long): Flow<List<FocusSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: FocusSession): Long

    @Query("SELECT SUM(durationMinutes) FROM focus_sessions WHERE startTime >= :sinceTimestamp AND completedSuccessfully = 1")
    fun getTotalFocusMinutesSince(sinceTimestamp: Long): Flow<Int?>

    @Query("SELECT COUNT(*) FROM focus_sessions WHERE startTime >= :sinceTimestamp AND completedSuccessfully = 1")
    fun getCompletedSessionCountSince(sinceTimestamp: Long): Flow<Int>

    @Query("DELETE FROM focus_sessions")
    suspend fun clearAll()
}
