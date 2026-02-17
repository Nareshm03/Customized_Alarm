package com.example.teacherscheduler.features.department.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(
    tableName = "department_resources",
    indices = [
        Index(value = ["resourceId"], unique = true),
        Index(value = ["departmentId"]),
        Index(value = ["uploadedBy"]),
        Index(value = ["uploadDate"])
    ]
)
@TypeConverters(DepartmentTaskConverters::class)
data class DepartmentResource(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val resourceId: String,
    val title: String,
    val description: String,
    val subjectName: String,
    val uploadedBy: String,
    val departmentId: String,
    val visibility: ResourceVisibility = ResourceVisibility.DEPARTMENT,
    val fileUrl: String,
    val fileType: String,
    val uploadDate: Long = System.currentTimeMillis()
) {
    constructor() : this(
        id = 0,
        resourceId = "",
        title = "",
        description = "",
        subjectName = "",
        uploadedBy = "",
        departmentId = "",
        visibility = ResourceVisibility.DEPARTMENT,
        fileUrl = "",
        fileType = "",
        uploadDate = System.currentTimeMillis()
    )
}
