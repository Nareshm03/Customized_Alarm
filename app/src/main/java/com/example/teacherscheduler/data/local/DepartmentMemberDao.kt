package com.example.teacherscheduler.data.local

import androidx.room.*
import com.example.teacherscheduler.model.DepartmentMember
import kotlinx.coroutines.flow.Flow

/**
 * DAO for DepartmentMember operations
 */
@Dao
interface DepartmentMemberDao {

    // ==================== CREATE ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(member: DepartmentMember): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(members: List<DepartmentMember>): List<Long>

    // ==================== READ ====================

    @Query("SELECT * FROM department_members WHERE id = :id")
    suspend fun getMemberById(id: Long): DepartmentMember?

    @Query("SELECT * FROM department_members WHERE departmentId = :departmentId AND isActive = 1")
    fun getMembersByDepartment(departmentId: Long): Flow<List<DepartmentMember>>

    @Query("SELECT * FROM department_members WHERE departmentId = :departmentId AND isActive = 1")
    suspend fun getMembersByDepartmentSync(departmentId: Long): List<DepartmentMember>

    @Query("SELECT * FROM department_members WHERE userId = :userId AND isActive = 1")
    fun getMembersByUser(userId: String): Flow<List<DepartmentMember>>

    @Query("SELECT * FROM department_members WHERE userId = :userId AND isActive = 1")
    suspend fun getMembersByUserSync(userId: String): List<DepartmentMember>

    @Query("SELECT * FROM department_members WHERE departmentId = :departmentId AND userId = :userId")
    suspend fun getMembership(departmentId: Long, userId: String): DepartmentMember?

    @Query("SELECT * FROM department_members WHERE departmentId = :departmentId AND role = :role AND isActive = 1")
    fun getMembersByRole(departmentId: Long, role: String): Flow<List<DepartmentMember>>

    @Query("SELECT * FROM department_members WHERE departmentId = :departmentId AND role = 'HOD' AND isActive = 1 LIMIT 1")
    suspend fun getHOD(departmentId: Long): DepartmentMember?

    @Query("SELECT * FROM department_members WHERE departmentId = :departmentId AND role = 'TEACHER' AND isActive = 1")
    fun getTeachers(departmentId: Long): Flow<List<DepartmentMember>>

    @Query("SELECT * FROM department_members WHERE departmentId = :departmentId AND role = 'TEACHER' AND isActive = 1")
    suspend fun getTeachersSync(departmentId: Long): List<DepartmentMember>

    // ==================== UPDATE ====================

    @Update
    suspend fun update(member: DepartmentMember)

    @Query("UPDATE department_members SET role = :role, updatedAt = :updatedAt WHERE id = :memberId")
    suspend fun updateRole(memberId: Long, role: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE department_members SET isActive = :isActive, leftDate = :leftDate, updatedAt = :updatedAt WHERE id = :memberId")
    suspend fun updateActiveStatus(memberId: Long, isActive: Boolean, leftDate: Long?, updatedAt: Long = System.currentTimeMillis())

    // ==================== DELETE ====================

    @Delete
    suspend fun delete(member: DepartmentMember)

    @Query("DELETE FROM department_members WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM department_members WHERE departmentId = :departmentId")
    suspend fun deleteByDepartment(departmentId: Long)

    @Query("DELETE FROM department_members WHERE userId = :userId")
    suspend fun deleteByUser(userId: String)

    @Query("DELETE FROM department_members")
    suspend fun deleteAll()

    // ==================== STATISTICS ====================

    @Query("SELECT COUNT(*) FROM department_members WHERE departmentId = :departmentId AND isActive = 1")
    suspend fun getActiveMemberCount(departmentId: Long): Int

    @Query("SELECT COUNT(*) FROM department_members WHERE departmentId = :departmentId AND role = 'TEACHER' AND isActive = 1")
    suspend fun getTeacherCount(departmentId: Long): Int

    @Query("SELECT COUNT(*) FROM department_members WHERE userId = :userId AND isActive = 1")
    suspend fun getUserDepartmentCount(userId: String): Int
}

