package com.example.teacherscheduler.data.local

import androidx.room.TypeConverter
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
}