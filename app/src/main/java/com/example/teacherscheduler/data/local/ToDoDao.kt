package com.example.teacherscheduler.data.local

import androidx.room.*
import com.example.teacherscheduler.model.ToDo
import com.example.teacherscheduler.model.TaskStatus
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

    @Query("DELETE FROM todos WHERE id = :id")
    suspend fun deleteById(id: Long)

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

    // ==================== STEP 3: DEPARTMENT TASK QUERIES ====================

    /**
     * Get all personal tasks (private to user)
     */
    @Query("""
        SELECT * FROM todos 
        WHERE isActive = 1 
        AND taskType = 'PERSONAL'
        ORDER BY isCompleted ASC, 
        CASE priority WHEN 3 THEN 0 WHEN 2 THEN 1 WHEN 1 THEN 2 ELSE 3 END, 
        dueDate ASC
    """)
    fun getPersonalTasks(): Flow<List<ToDo>>

    /**
     * Get all department tasks for a user (assigned to them or bulk tasks)
     */
    @Query("""
        SELECT * FROM todos 
        WHERE isActive = 1 
        AND isDepartmentTask = 1
        AND (assignedTo = :userId OR isBulkTask = 1)
        ORDER BY isCompleted ASC,
        CASE priority WHEN 3 THEN 0 WHEN 2 THEN 1 WHEN 1 THEN 2 ELSE 3 END,
        dueDate ASC
    """)
    fun getDepartmentTasksForUser(userId: String): Flow<List<ToDo>>

    /**
     * Get all tasks for a user (personal + department)
     */
    @Query("""
        SELECT * FROM todos 
        WHERE isActive = 1 
        AND (taskType = 'PERSONAL' OR (isDepartmentTask = 1 AND (assignedTo = :userId OR isBulkTask = 1)))
        ORDER BY isCompleted ASC,
        CASE priority WHEN 3 THEN 0 WHEN 2 THEN 1 WHEN 1 THEN 2 ELSE 3 END,
        dueDate ASC
    """)
    fun getAllTasksForUser(userId: String): Flow<List<ToDo>>

    /**
     * Get department tasks by department ID
     */
    @Query("""
        SELECT * FROM todos 
        WHERE isActive = 1 
        AND departmentId = :departmentId
        AND isDepartmentTask = 1
        ORDER BY isCompleted ASC, dueDate ASC
    """)
    fun getDepartmentTasks(departmentId: Long): Flow<List<ToDo>>

    /**
     * Get department tasks by department ID (Sync)
     */
    @Query("""
        SELECT * FROM todos 
        WHERE isActive = 1 
        AND departmentId = :departmentId
        AND isDepartmentTask = 1
        ORDER BY isCompleted ASC, dueDate ASC
    """)
    suspend fun getDepartmentTasksSync(departmentId: Long): List<ToDo>

    /**
     * Get bulk tasks (assigned to all teachers)
     */
    @Query("""
        SELECT * FROM todos 
        WHERE isActive = 1 
        AND isBulkTask = 1
        AND isDepartmentTask = 1
        ORDER BY isCompleted ASC, dueDate ASC
    """)
    fun getBulkTasks(): Flow<List<ToDo>>

    /**
     * Get tasks created by HOD
     */
    @Query("""
        SELECT * FROM todos 
        WHERE isActive = 1 
        AND isDepartmentTask = 1
        AND assignedBy = :hodId
        ORDER BY createdAt DESC
    """)
    fun getTasksCreatedByHOD(hodId: String): Flow<List<ToDo>>

    /**
     * Get incomplete department tasks for user
     */
    @Query("""
        SELECT * FROM todos 
        WHERE isActive = 1 
        AND isCompleted = 0
        AND isDepartmentTask = 1
        AND (assignedTo = :userId OR isBulkTask = 1)
        ORDER BY CASE priority WHEN 3 THEN 0 WHEN 2 THEN 1 WHEN 1 THEN 2 ELSE 3 END,
        dueDate ASC
    """)
    fun getIncompleteDepartmentTasks(userId: String): Flow<List<ToDo>>

    /**
     * Get overdue department tasks for user
     */
    @Query("""
        SELECT * FROM todos 
        WHERE isActive = 1 
        AND isCompleted = 0
        AND isDepartmentTask = 1
        AND (assignedTo = :userId OR isBulkTask = 1)
        AND dueDate IS NOT NULL 
        AND dueDate < :currentDate
        ORDER BY dueDate ASC
    """)
    fun getOverdueDepartmentTasks(userId: String, currentDate: Long): Flow<List<ToDo>>

    /**
     * Count department tasks by completion status
     */
    @Query("""
        SELECT COUNT(*) FROM todos 
        WHERE isActive = 1 
        AND isDepartmentTask = 1
        AND (assignedTo = :userId OR isBulkTask = 1)
        AND isCompleted = :isCompleted
    """)
    suspend fun countDepartmentTasks(userId: String, isCompleted: Boolean): Int

    /**
     * Update task with completion notes
     */
    @Query("""
        UPDATE todos 
        SET isCompleted = :isCompleted, 
            completedAt = :completedAt,
            completionNotes = :notes
        WHERE id = :id
    """)
    suspend fun updateTaskCompletion(id: Long, isCompleted: Boolean, completedAt: Long?, notes: String)

    // ==================== STEP 4: LIFECYCLE STATUS QUERIES ====================

    /**
     * Update task status (lifecycle transition)
     */
    @Query("""
        UPDATE todos 
        SET status = :status,
            startedAt = :startedAt,
            lastStatusChange = :timestamp
        WHERE id = :taskId
    """)
    suspend fun updateTaskStatus(
        taskId: Long,
        status: TaskStatus,
        startedAt: Long? = null,
        timestamp: Long = System.currentTimeMillis()
    )

    /**
     * Mark task as completed with status update
     */
    @Query("""
        UPDATE todos 
        SET isCompleted = 1,
            status = 'COMPLETED',
            completedAt = :completedAt,
            completionNotes = :notes,
            lastStatusChange = :timestamp
        WHERE id = :taskId
    """)
    suspend fun completeTask(
        taskId: Long,
        completedAt: Long = System.currentTimeMillis(),
        notes: String = "",
        timestamp: Long = System.currentTimeMillis()
    )

    /**
     * Start task (transition to IN_PROGRESS)
     */
    @Query("""
        UPDATE todos 
        SET status = 'IN_PROGRESS',
            startedAt = :startedAt,
            lastStatusChange = :timestamp
        WHERE id = :taskId
    """)
    suspend fun startTask(
        taskId: Long,
        startedAt: Long = System.currentTimeMillis(),
        timestamp: Long = System.currentTimeMillis()
    )

    /**
     * Mark overdue notification as sent
     */
    @Query("""
        UPDATE todos 
        SET overdueNotificationSent = 1
        WHERE id = :taskId
    """)
    suspend fun markOverdueNotificationSent(taskId: Long)

    /**
     * Get tasks by lifecycle status
     */
    @Query("""
        SELECT * FROM todos 
        WHERE isActive = 1 
        AND status = :status
        AND (taskType = 'PERSONAL' OR (isDepartmentTask = 1 AND (assignedTo = :userId OR isBulkTask = 1)))
        ORDER BY dueDate ASC
    """)
    fun getTasksByStatus(userId: String, status: TaskStatus): Flow<List<ToDo>>

    /**
     * Get overdue tasks that need HOD notification
     */
    @Query("""
        SELECT * FROM todos 
        WHERE isActive = 1 
        AND isDepartmentTask = 1
        AND isCompleted = 0
        AND dueDate IS NOT NULL 
        AND dueDate < :currentDate
        AND overdueNotificationSent = 0
        AND assignedBy != ''
        ORDER BY dueDate ASC
    """)
    suspend fun getOverdueTasksForHODNotification(currentDate: Long = System.currentTimeMillis()): List<ToDo>

    /**
     * Get tasks that should send reminders (not completed)
     */
    @Query("""
        SELECT * FROM todos 
        WHERE isActive = 1 
        AND isCompleted = 0
        AND notificationsEnabled = 1
        AND status != 'COMPLETED'
        AND dueDate IS NOT NULL
        AND dueDate > :currentDate
        ORDER BY dueDate ASC
    """)
    suspend fun getTasksForReminders(currentDate: Long = System.currentTimeMillis()): List<ToDo>

    /**
     * Get lifecycle statistics for user
     */
    @Query("""
        SELECT status, COUNT(*) as count
        FROM todos 
        WHERE isActive = 1 
        AND (taskType = 'PERSONAL' OR (isDepartmentTask = 1 AND (assignedTo = :userId OR isBulkTask = 1)))
        GROUP BY status
    """)
    suspend fun getLifecycleStatistics(userId: String): Map<@MapColumn(columnName = "status") String, @MapColumn(columnName = "count") Int>

    /**
     * Get department tasks by status for HOD dashboard
     */
    @Query("""
        SELECT * FROM todos 
        WHERE isActive = 1 
        AND isDepartmentTask = 1
        AND departmentId = :departmentId
        AND status = :status
        ORDER BY dueDate ASC
    """)
    fun getDepartmentTasksByStatus(departmentId: Long, status: TaskStatus): Flow<List<ToDo>>
}
