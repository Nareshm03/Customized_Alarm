package com.example.teacherscheduler.data.local

import androidx.room.*
import com.example.teacherscheduler.model.Department
import kotlinx.coroutines.flow.Flow

/**
 * DAO for Department operations
 */
@Dao
interface DepartmentDao {

    // ==================== CREATE ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(department: Department): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(departments: List<Department>): List<Long>

    // ==================== READ ====================

    @Query("SELECT * FROM departments WHERE id = :id")
    suspend fun getDepartmentById(id: Long): Department?

    @Query("SELECT * FROM departments WHERE id = :id")
    fun getDepartmentByIdFlow(id: Long): Flow<Department?>

    @Query("SELECT * FROM departments WHERE departmentCode = :code")
    suspend fun getDepartmentByCode(code: String): Department?

    @Query("SELECT * FROM departments WHERE hodId = :hodId")
    suspend fun getDepartmentByHOD(hodId: String): Department?

    @Query("SELECT * FROM departments WHERE isActive = 1 ORDER BY departmentName ASC")
    fun getAllActiveDepartments(): Flow<List<Department>>

    @Query("SELECT * FROM departments WHERE isActive = 1 ORDER BY departmentName ASC")
    suspend fun getAllActiveDepartmentsSync(): List<Department>

    @Query("SELECT * FROM departments ORDER BY departmentName ASC")
    fun getAllDepartments(): Flow<List<Department>>

    @Query("SELECT * FROM departments ORDER BY departmentName ASC")
    suspend fun getAllDepartmentsSync(): List<Department>

    @Query("""
        SELECT * FROM departments 
        WHERE (departmentName LIKE '%' || :query || '%' 
        OR departmentCode LIKE '%' || :query || '%'
        OR hodName LIKE '%' || :query || '%')
        AND isActive = 1
        ORDER BY departmentName ASC
    """)
    fun searchDepartments(query: String): Flow<List<Department>>

    // ==================== UPDATE ====================

    @Update
    suspend fun update(department: Department)

    @Query("UPDATE departments SET hodId = :hodId, hodName = :hodName, hodEmail = :hodEmail, updatedAt = :updatedAt WHERE id = :departmentId")
    suspend fun updateHOD(departmentId: Long, hodId: String, hodName: String, hodEmail: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE departments SET totalTeachers = :count, updatedAt = :updatedAt WHERE id = :departmentId")
    suspend fun updateTeacherCount(departmentId: Long, count: Int, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE departments SET totalClasses = :count, updatedAt = :updatedAt WHERE id = :departmentId")
    suspend fun updateClassCount(departmentId: Long, count: Int, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE departments SET isActive = :isActive, updatedAt = :updatedAt WHERE id = :departmentId")
    suspend fun updateActiveStatus(departmentId: Long, isActive: Boolean, updatedAt: Long = System.currentTimeMillis())

    // ==================== DELETE ====================

    @Delete
    suspend fun delete(department: Department)

    @Query("DELETE FROM departments WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM departments")
    suspend fun deleteAll()

    // ==================== STATISTICS ====================

    @Query("SELECT COUNT(*) FROM departments WHERE isActive = 1")
    suspend fun getActiveDepartmentCount(): Int

    @Query("SELECT COUNT(*) FROM departments")
    suspend fun getTotalDepartmentCount(): Int

    @Query("SELECT SUM(totalTeachers) FROM departments WHERE isActive = 1")
    suspend fun getTotalTeachersAcrossAllDepartments(): Int?

    @Query("SELECT SUM(totalClasses) FROM departments WHERE isActive = 1")
    suspend fun getTotalClassesAcrossAllDepartments(): Int?
}

