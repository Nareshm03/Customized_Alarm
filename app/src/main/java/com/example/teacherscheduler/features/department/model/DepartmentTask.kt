package com.example.teacherscheduler.features.department.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(
    tableName = "department_tasks",
    indices = [
        Index(value = ["taskId"], unique = true),
        Index(value = ["departmentId"]),
        Index(value = ["createdBy"]),
        Index(value = ["deadline"])
    ]
)
@TypeConverters(DepartmentTaskConverters::class)
data class DepartmentTask(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val taskId: String,
    val title: String,
    val description: String,
    val createdBy: String,
    val departmentId: String,
    val assignedTeacherIds: List<String> = emptyList(),
    val deadline: Long,
    val reminderMinutesBefore: Int = 15,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val statusPerTeacher: Map<String, TaskStatus> = emptyMap(),
    
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    constructor() : this(
        id = 0,
        taskId = "",
        title = "",
        description = "",
        createdBy = "",
        departmentId = "",
        assignedTeacherIds = emptyList(),
        deadline = 0L,
        reminderMinutesBefore = 15,
        priority = TaskPriority.MEDIUM,
        statusPerTeacher = emptyMap()
    )
    
    fun isOverdue(): Boolean = System.currentTimeMillis() > deadline
    
    fun getTeacherStatus(teacherId: String): TaskStatus = 
        statusPerTeacher[teacherId] ?: TaskStatus.ASSIGNED
    
    fun isCompletedByTeacher(teacherId: String): Boolean = 
        getTeacherStatus(teacherId) == TaskStatus.COMPLETED
    
    fun isAssignedToTeacher(teacherId: String): Boolean = 
        assignedTeacherIds.contains(teacherId)
}
