package com.example.teacherscheduler.features.department.data.local

import androidx.room.*
import com.example.teacherscheduler.features.department.data.Department
import kotlinx.coroutines.flow.Flow

@Dao
interface DepartmentDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(department: Department): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(departments: List<Department>)
    
    @Update
    suspend fun update(department: Department)
    
    @Delete
    suspend fun delete(department: Department)
    
    @Query("SELECT * FROM departments_v2 WHERE id = :id")
    suspend fun getDepartmentById(id: Long): Department?
    
    @Query("SELECT * FROM departments_v2 WHERE departmentId = :departmentId")
    suspend fun getDepartmentByDepartmentId(departmentId: String): Department?
    
    @Query("SELECT * FROM departments_v2 WHERE hodId = :hodId")
    suspend fun getDepartmentsByHodId(hodId: String): List<Department>
    
    @Query("SELECT * FROM departments_v2")
    fun getAllDepartments(): Flow<List<Department>>
    
    @Query("SELECT * FROM departments_v2")
    suspend fun getAllDepartmentsSync(): List<Department>
    
    @Query("SELECT * FROM departments_v2 WHERE departmentId IN (:departmentIds)")
    suspend fun getDepartmentsByIds(departmentIds: List<String>): List<Department>
    
    @Query("DELETE FROM departments_v2 WHERE id = :id")
    suspend fun deleteById(id: Long)
    
    @Query("DELETE FROM departments_v2")
    suspend fun deleteAll()
}
