package com.example.teacherscheduler.features.department.data.local

import androidx.room.*
import com.example.teacherscheduler.features.department.model.DepartmentResource
import kotlinx.coroutines.flow.Flow

@Dao
interface DepartmentResourceDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(resource: DepartmentResource): Long
    
    @Update
    suspend fun update(resource: DepartmentResource)
    
    @Query("SELECT * FROM department_resources WHERE resourceId = :resourceId")
    suspend fun getResourceByResourceId(resourceId: String): DepartmentResource?
    
    @Query("SELECT * FROM department_resources WHERE departmentId = :departmentId AND visibility = 'DEPARTMENT' ORDER BY uploadDate DESC")
    fun getDepartmentResources(departmentId: String): Flow<List<DepartmentResource>>
    
    @Query("SELECT * FROM department_resources WHERE uploadedBy = :userId AND visibility = 'PRIVATE' ORDER BY uploadDate DESC")
    fun getPrivateResources(userId: String): Flow<List<DepartmentResource>>
    
    @Query("DELETE FROM department_resources WHERE resourceId = :resourceId")
    suspend fun deleteByResourceId(resourceId: String)
}
