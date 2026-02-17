package com.example.teacherscheduler.features.department.data.repository

import com.example.teacherscheduler.features.department.data.Department
import com.example.teacherscheduler.features.department.data.local.DepartmentDao
import kotlinx.coroutines.flow.Flow

class DepartmentRepository(private val departmentDao: DepartmentDao) {
    
    fun getAllDepartments(): Flow<List<Department>> = departmentDao.getAllDepartments()
    
    suspend fun getDepartmentById(id: Long): Department? = departmentDao.getDepartmentById(id)
    
    suspend fun getDepartmentByDepartmentId(departmentId: String): Department? = 
        departmentDao.getDepartmentByDepartmentId(departmentId)
    
    suspend fun getDepartmentsByHodId(hodId: String): List<Department> = 
        departmentDao.getDepartmentsByHodId(hodId)
    
    suspend fun insertDepartment(department: Department): Long = departmentDao.insert(department)
    
    suspend fun updateDepartment(department: Department) = departmentDao.update(department)
    
    suspend fun deleteDepartment(department: Department) = departmentDao.delete(department)
    
    suspend fun addTeacherToDepartment(departmentId: String, teacherId: String) {
        val department = getDepartmentByDepartmentId(departmentId) ?: return
        val updatedTeachers = department.teacherIds.toMutableList().apply { add(teacherId) }
        updateDepartment(department.copy(teacherIds = updatedTeachers, updatedAt = System.currentTimeMillis()))
    }
    
    suspend fun removeTeacherFromDepartment(departmentId: String, teacherId: String) {
        val department = getDepartmentByDepartmentId(departmentId) ?: return
        val updatedTeachers = department.teacherIds.toMutableList().apply { remove(teacherId) }
        updateDepartment(department.copy(teacherIds = updatedTeachers, updatedAt = System.currentTimeMillis()))
    }
}
