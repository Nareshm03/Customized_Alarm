package com.example.teacherscheduler.data.local

import androidx.room.TypeConverter
import com.example.teacherscheduler.model.TaskStatus
import com.example.teacherscheduler.model.TaskType
import com.example.teacherscheduler.model.ToDo
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date

/**
 * Type converters for Room database
 */
class Converters {
    private val gson = Gson()
    
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { 
            val date = Date(it)
            val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", java.util.Locale.getDefault())
            android.util.Log.d("DatabaseConverter", "Reading from DB - Timestamp: $it -> Date: ${dateFormat.format(date)}")
            date
        }
    }
    
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.let {
            val timestamp = it.time
            val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", java.util.Locale.getDefault())
            android.util.Log.d("DatabaseConverter", "Writing to DB - Date: ${dateFormat.format(it)} -> Timestamp: $timestamp")
            timestamp
        }
    }
    
    @TypeConverter
    fun fromIntList(value: List<Int>?): String {
        return gson.toJson(value ?: emptyList<Int>())
    }
    
    @TypeConverter
    fun toIntList(value: String): List<Int> {
        val listType = object : TypeToken<List<Int>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }

    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return gson.toJson(value ?: emptyList<String>())
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }

    @TypeConverter
    fun fromPriority(priority: ToDo.Priority): Int {
        return priority.value
    }

    @TypeConverter
    fun toPriority(value: Int): ToDo.Priority {
        return ToDo.Priority.fromValue(value)
    }

    @TypeConverter
    fun fromTaskType(type: TaskType): String {
        return type.name
    }

    @TypeConverter
    fun toTaskType(value: String): TaskType {
        return try {
            TaskType.valueOf(value)
        } catch (e: Exception) {
            TaskType.PERSONAL
        }
    }

    @TypeConverter
    fun fromTaskStatus(status: TaskStatus): String {
        return status.name
    }

    @TypeConverter
    fun toTaskStatus(value: String): TaskStatus {
        return try {
            TaskStatus.valueOf(value)
        } catch (e: Exception) {
            TaskStatus.ASSIGNED
        }
    }
}