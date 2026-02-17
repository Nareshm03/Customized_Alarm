package com.example.teacherscheduler.features.department.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.teacherscheduler.data.local.Converters

@Entity(
    tableName = "departments_v2",
    indices = [
        Index(value = ["departmentId"], unique = true),
        Index(value = ["hodId"])
    ]
)
@TypeConverters(Converters::class)
data class Department(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val departmentId: String,
    val departmentName: String,
    val hodId: String,
    val teacherIds: List<String> = emptyList(),
    
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    constructor() : this(
        id = 0,
        departmentId = "",
        departmentName = "",
        hodId = "",
        teacherIds = emptyList()
    )
}
