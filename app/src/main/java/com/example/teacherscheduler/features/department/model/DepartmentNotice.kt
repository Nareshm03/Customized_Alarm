package com.example.teacherscheduler.features.department.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(
    tableName = "department_notices",
    indices = [
        Index(value = ["noticeId"], unique = true),
        Index(value = ["departmentId"]),
        Index(value = ["createdBy"]),
        Index(value = ["createdAt"])
    ]
)
@TypeConverters(DepartmentTaskConverters::class)
data class DepartmentNotice(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val noticeId: String,
    val departmentId: String,
    val title: String,
    val description: String,
    val createdBy: String,
    val createdAt: Long = System.currentTimeMillis(),
    val priority: NoticePriority = NoticePriority.NORMAL,
    val seenBy: List<String> = emptyList()
) {
    constructor() : this(
        id = 0,
        noticeId = "",
        departmentId = "",
        title = "",
        description = "",
        createdBy = "",
        createdAt = System.currentTimeMillis(),
        priority = NoticePriority.NORMAL,
        seenBy = emptyList()
    )
}
