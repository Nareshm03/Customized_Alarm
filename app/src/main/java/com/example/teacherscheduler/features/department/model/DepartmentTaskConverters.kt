package com.example.teacherscheduler.features.department.model

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class DepartmentTaskConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromStringList(value: List<String>): String = gson.toJson(value)

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, type) ?: emptyList()
    }

    @TypeConverter
    fun fromStatusMap(value: Map<String, TaskStatus>): String = gson.toJson(value)

    @TypeConverter
    fun toStatusMap(value: String): Map<String, TaskStatus> {
        val type = object : TypeToken<Map<String, TaskStatus>>() {}.type
        return gson.fromJson(value, type) ?: emptyMap()
    }

    @TypeConverter
    fun fromTaskPriority(value: TaskPriority): String = value.name

    @TypeConverter
    fun toTaskPriority(value: String): TaskPriority = TaskPriority.valueOf(value)

    @TypeConverter
    fun fromTaskStatus(value: TaskStatus): String = value.name

    @TypeConverter
    fun toTaskStatus(value: String): TaskStatus = TaskStatus.valueOf(value)
    
    @TypeConverter
    fun fromNoticePriority(value: NoticePriority): String = value.name
    
    @TypeConverter
    fun toNoticePriority(value: String): NoticePriority = NoticePriority.valueOf(value)
    
    @TypeConverter
    fun fromResourceVisibility(value: ResourceVisibility): String = value.name
    
    @TypeConverter
    fun toResourceVisibility(value: String): ResourceVisibility = ResourceVisibility.valueOf(value)
}
