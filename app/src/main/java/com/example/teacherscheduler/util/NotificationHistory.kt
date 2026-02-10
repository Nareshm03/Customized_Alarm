package com.example.teacherscheduler.util

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object NotificationHistory {
    
    private const val PREFS_NAME = "notification_history"
    private const val KEY_HISTORY = "history"
    private const val MAX_HISTORY = 50
    
    data class NotificationRecord(
        val title: String,
        val message: String,
        val timestamp: Long,
        val type: String
    )
    
    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    fun addRecord(context: Context, title: String, message: String, type: String) {
        val history = getHistory(context).toMutableList()
        history.add(0, NotificationRecord(title, message, System.currentTimeMillis(), type))
        
        if (history.size > MAX_HISTORY) {
            history.removeAt(history.size - 1)
        }
        
        val json = Gson().toJson(history)
        getPrefs(context).edit().putString(KEY_HISTORY, json).apply()
    }
    
    fun getHistory(context: Context): List<NotificationRecord> {
        val json = getPrefs(context).getString(KEY_HISTORY, null) ?: return emptyList()
        val type = object : TypeToken<List<NotificationRecord>>() {}.type
        return Gson().fromJson(json, type)
    }
    
    fun clearHistory(context: Context) {
        getPrefs(context).edit().clear().apply()
    }
}
