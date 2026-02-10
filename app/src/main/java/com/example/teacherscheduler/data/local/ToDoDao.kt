package com.example.teacherscheduler.data.local

import androidx.room.*
import com.example.teacherscheduler.model.ToDo
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface ToDoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(todo: ToDo): Long

    @Update
    suspend fun update(todo: ToDo)

    @Delete
    suspend fun delete(todo: ToDo)

    @Query("SELECT * FROM todos WHERE isActive = 1 AND isCompleted = 0 ORDER BY CASE priority WHEN 3 THEN 0 WHEN 2 THEN 1 WHEN 1 THEN 2 ELSE 3 END, dueDate ASC")
    fun getAllActiveToDos(): Flow<List<ToDo>>

    @Query("SELECT * FROM todos WHERE isActive = 1 ORDER BY isCompleted ASC, CASE priority WHEN 3 THEN 0 WHEN 2 THEN 1 WHEN 1 THEN 2 ELSE 3 END, dueDate ASC")
    fun getAllToDos(): Flow<List<ToDo>>

    @Query("SELECT * FROM todos WHERE isActive = 1 AND isCompleted = 1 ORDER BY completedAt DESC")
    fun getCompletedToDos(): Flow<List<ToDo>>

    @Query("SELECT * FROM todos WHERE isActive = 1 AND isCompleted = 0 AND dueDate IS NOT NULL AND dueDate < :currentDate ORDER BY dueDate ASC")
    fun getOverdueToDos(currentDate: Long): Flow<List<ToDo>>

    @Query("SELECT * FROM todos WHERE isActive = 1 AND isCompleted = 0 AND dueDate IS NOT NULL AND dueDate BETWEEN :startDate AND :endDate ORDER BY dueDate ASC")
    fun getToDosForDateRange(startDate: Long, endDate: Long): Flow<List<ToDo>>

    @Query("SELECT * FROM todos WHERE isActive = 1 AND isCompleted = 0 AND category = :category ORDER BY CASE priority WHEN 3 THEN 0 WHEN 2 THEN 1 WHEN 1 THEN 2 ELSE 3 END, dueDate ASC")
    fun getToDosByCategory(category: String): Flow<List<ToDo>>

    @Query("SELECT * FROM todos WHERE isActive = 1 AND isCompleted = 0 AND priority = :priority ORDER BY dueDate ASC")
    fun getToDosByPriority(priority: Int): Flow<List<ToDo>>

    @Query("SELECT * FROM todos WHERE id = :id")
    suspend fun getToDoById(id: Long): ToDo?

    @Query("SELECT * FROM todos WHERE isActive = 1 ORDER BY isCompleted ASC, CASE priority WHEN 3 THEN 0 WHEN 2 THEN 1 WHEN 1 THEN 2 ELSE 3 END, dueDate ASC")
    suspend fun getAllToDosSync(): List<ToDo>

    @Query("SELECT COUNT(*) FROM todos WHERE isActive = 1 AND isCompleted = 0")
    fun getActiveToDosCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM todos WHERE isActive = 1 AND isCompleted = 0 AND dueDate IS NOT NULL AND dueDate < :currentDate")
    fun getOverdueToDosCount(currentDate: Long): Flow<Int>

    @Query("UPDATE todos SET isCompleted = :isCompleted, completedAt = :completedAt WHERE id = :id")
    suspend fun updateCompletionStatus(id: Long, isCompleted: Boolean, completedAt: Long?)

    @Query("DELETE FROM todos WHERE isActive = 0")
    suspend fun deleteInactiveToDos()

    @Query("SELECT DISTINCT category FROM todos WHERE isActive = 1 AND category != '' ORDER BY category ASC")
    fun getAllCategories(): Flow<List<String>>
}

