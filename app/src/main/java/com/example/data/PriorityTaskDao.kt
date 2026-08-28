package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.model.PriorityTask
import kotlinx.coroutines.flow.Flow

@Dao
interface PriorityTaskDao {
    @Query("SELECT * FROM priority_tasks ORDER BY priorityIndex ASC, id ASC")
    fun getAllTasks(): Flow<List<PriorityTask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: PriorityTask): Long

    @Update
    suspend fun updateTask(task: PriorityTask)

    @Delete
    suspend fun deleteTask(task: PriorityTask)

    @Query("DELETE FROM priority_tasks WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE priority_tasks SET isCompleted = :completed WHERE id = :id")
    suspend fun setTaskCompleted(id: Long, completed: Boolean)
}
