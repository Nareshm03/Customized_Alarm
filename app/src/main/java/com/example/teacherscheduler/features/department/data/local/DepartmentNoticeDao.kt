package com.example.teacherscheduler.features.department.data.local

import androidx.room.*
import com.example.teacherscheduler.features.department.model.DepartmentNotice
import kotlinx.coroutines.flow.Flow

@Dao
interface DepartmentNoticeDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notice: DepartmentNotice): Long
    
    @Update
    suspend fun update(notice: DepartmentNotice)
    
    @Query("SELECT * FROM department_notices WHERE noticeId = :noticeId")
    suspend fun getNoticeByNoticeId(noticeId: String): DepartmentNotice?
    
    @Query("SELECT * FROM department_notices WHERE departmentId = :departmentId ORDER BY createdAt DESC")
    fun getNoticesByDepartment(departmentId: String): Flow<List<DepartmentNotice>>
    
    @Query("SELECT * FROM department_notices WHERE departmentId = :departmentId ORDER BY createdAt DESC")
    suspend fun getNoticesByDepartmentSync(departmentId: String): List<DepartmentNotice>
    
    @Query("DELETE FROM department_notices WHERE noticeId = :noticeId")
    suspend fun deleteByNoticeId(noticeId: String)
}
