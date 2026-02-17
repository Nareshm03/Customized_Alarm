package com.example.teacherscheduler.features.department.data.local

import androidx.room.*
import com.example.teacherscheduler.features.department.model.DepartmentTask
import kotlinx.coroutines.flow.Flow

@Dao
interface DepartmentTaskDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: DepartmentTask): Long
    
    @Update
    suspend fun update(task: DepartmentTask)
    
    @Delete
    suspend fun delete(task: DepartmentTask)
    
    @Query("SELECT * FROM department_tasks WHERE taskId = :taskId")
    suspend fun getTaskByTaskId(taskId: String): DepartmentTask?
    
    @Query("SELECT * FROM department_tasks WHERE departmentId = :departmentId ORDER BY deadline ASC")
    fun getTasksByDepartment(departmentId: String): Flow<List<DepartmentTask>>
    
    @Query("SELECT * FROM department_tasks WHERE createdBy = :hodId ORDER BY deadline ASC")
    fun getTasksByHOD(hodId: String): Flow<List<DepartmentTask>>
    
    @Query("SELECT * FROM department_tasks WHERE assignedTeacherIds LIKE '%' || :teacherId || '%' ORDER BY deadline ASC")
    fun getTasksForTeacher(teacherId: String): Flow<List<DepartmentTask>>
    
    @Query("SELECT * FROM department_tasks")
    suspend fun getAllTasksSync(): List<DepartmentTask>
    
    @Query("DELETE FROM department_tasks WHERE taskId = :taskId")
    suspend fun deleteByTaskId(taskId: String)
}
