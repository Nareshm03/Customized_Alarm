package com.example.teacherscheduler.data.local

import androidx.room.*
import com.example.teacherscheduler.model.DepartmentTask
import com.example.teacherscheduler.model.TaskStatus
import kotlinx.coroutines.flow.Flow

/**
 * DAO for DepartmentTask operations
 */
@Dao
interface DepartmentTaskDao {

    // ==================== CREATE ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: DepartmentTask): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tasks: List<DepartmentTask>): List<Long>

    // ==================== READ ====================

    @Query("SELECT * FROM department_tasks WHERE id = :id")
    suspend fun getTaskById(id: Long): DepartmentTask?

    @Query("SELECT * FROM department_tasks WHERE id = :id")
    fun getTaskByIdFlow(id: Long): Flow<DepartmentTask?>

    @Query("""
        SELECT * FROM department_tasks 
        WHERE departmentId = :departmentId AND isActive = 1
        ORDER BY dueDate ASC
    """)
    fun getTasksByDepartment(departmentId: Long): Flow<List<DepartmentTask>>

    @Query("""
        SELECT * FROM department_tasks 
        WHERE (assignedTo = :userId OR isGroupTask = 1) AND isActive = 1
        ORDER BY dueDate ASC
    """)
    fun getTasksByUser(userId: String): Flow<List<DepartmentTask>>

    @Query("""
        SELECT * FROM department_tasks 
        WHERE (assignedTo = :userId OR isGroupTask = 1) AND isActive = 1
        ORDER BY dueDate ASC
    """)
    suspend fun getTasksByUserSync(userId: String): List<DepartmentTask>

    @Query("""
        SELECT * FROM department_tasks 
        WHERE departmentId = :departmentId 
        AND (assignedTo = :userId OR isGroupTask = 1) 
        AND isActive = 1
        ORDER BY dueDate ASC
    """)
    fun getUserTasksInDepartment(departmentId: Long, userId: String): Flow<List<DepartmentTask>>

    @Query("""
        SELECT * FROM department_tasks 
        WHERE departmentId = :departmentId AND status = :status AND isActive = 1
        ORDER BY dueDate ASC
    """)
    fun getTasksByStatus(departmentId: Long, status: TaskStatus): Flow<List<DepartmentTask>>

    @Query("""
        SELECT * FROM department_tasks 
        WHERE (assignedTo = :userId OR isGroupTask = 1) 
        AND status = :status AND isActive = 1
        ORDER BY dueDate ASC
    """)
    fun getUserTasksByStatus(userId: String, status: TaskStatus): Flow<List<DepartmentTask>>

    @Query("""
        SELECT * FROM department_tasks 
        WHERE assignedBy = :hodId AND isActive = 1
        ORDER BY assignedDate DESC
    """)
    fun getTasksCreatedByHOD(hodId: String): Flow<List<DepartmentTask>>

    @Query("""
        SELECT * FROM department_tasks 
        WHERE departmentId = :departmentId 
        AND isGroupTask = 1 
        AND isActive = 1
        ORDER BY dueDate ASC
    """)
    fun getBulkTasks(departmentId: Long): Flow<List<DepartmentTask>>

    @Query("""
        SELECT * FROM department_tasks 
        WHERE (assignedTo = :userId OR isGroupTask = 1)
        AND status != 'COMPLETED'
        AND dueDate < :currentTime
        AND isActive = 1
        ORDER BY dueDate ASC
    """)
    fun getOverdueTasks(userId: String, currentTime: Long = System.currentTimeMillis()): Flow<List<DepartmentTask>>

    @Query("""
        SELECT * FROM department_tasks 
        WHERE (assignedTo = :userId OR isGroupTask = 1)
        AND status != 'COMPLETED'
        AND dueDate >= :startDate
        AND dueDate <= :endDate
        AND isActive = 1
        ORDER BY dueDate ASC
    """)
    fun getTasksDueInRange(userId: String, startDate: Long, endDate: Long): Flow<List<DepartmentTask>>

    // ==================== UPDATE ====================

    @Update
    suspend fun update(task: DepartmentTask)

    @Query("""
        UPDATE department_tasks 
        SET status = :status, 
            completedDate = :completedDate,
            completionNotes = :notes,
            updatedAt = :updatedAt 
        WHERE id = :taskId
    """)
    suspend fun updateTaskCompletion(
        taskId: Long,
        status: TaskStatus,
        completedDate: Long?,
        notes: String,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("UPDATE department_tasks SET status = :status, updatedAt = :updatedAt WHERE id = :taskId")
    suspend fun updateStatus(taskId: Long, status: TaskStatus, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE department_tasks SET isActive = :isActive, updatedAt = :updatedAt WHERE id = :taskId")
    suspend fun updateActiveStatus(taskId: Long, isActive: Boolean, updatedAt: Long = System.currentTimeMillis())

    // ==================== DELETE ====================

    @Delete
    suspend fun delete(task: DepartmentTask)

    @Query("DELETE FROM department_tasks WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM department_tasks WHERE departmentId = :departmentId")
    suspend fun deleteByDepartment(departmentId: Long)

    @Query("DELETE FROM department_tasks")
    suspend fun deleteAll()

    // ==================== STATISTICS ====================

    @Query("SELECT COUNT(*) FROM department_tasks WHERE departmentId = :departmentId AND isActive = 1")
    suspend fun getTaskCount(departmentId: Long): Int

    @Query("""
        SELECT COUNT(*) FROM department_tasks 
        WHERE (assignedTo = :userId OR isGroupTask = 1) 
        AND status = :status AND isActive = 1
    """)
    suspend fun getUserTaskCountByStatus(userId: String, status: TaskStatus): Int

    @Query("""
        SELECT COUNT(*) FROM department_tasks 
        WHERE (assignedTo = :userId OR isGroupTask = 1)
        AND status != 'COMPLETED'
        AND dueDate < :currentTime
        AND isActive = 1
    """)
    suspend fun getOverdueTaskCount(userId: String, currentTime: Long = System.currentTimeMillis()): Int

    @Query("""
        SELECT COUNT(*) FROM department_tasks 
        WHERE departmentId = :departmentId AND isGroupTask = 1 AND isActive = 1
    """)
    suspend fun getBulkTaskCount(departmentId: Long): Int
}

