package com.example.teacherscheduler.util

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

object AnalyticsTracker {
    
    private lateinit var analytics: FirebaseAnalytics
    
    fun initialize(context: Context) {
        analytics = FirebaseAnalytics.getInstance(context)
    }
    
    fun logClassCreated() {
        analytics.logEvent("class_created", null)
    }
    
    fun logMeetingCreated() {
        analytics.logEvent("meeting_created", null)
    }
    
    fun logBackupCreated() {
        analytics.logEvent("backup_created", null)
    }
    
    fun logSearchUsed(query: String) {
        val bundle = Bundle().apply {
            putString("search_query", query)
        }
        analytics.logEvent("search_used", bundle)
    }
    
    fun logConflictDetected(count: Int) {
        val bundle = Bundle().apply {
            putInt("conflict_count", count)
        }
        analytics.logEvent("conflict_detected", bundle)
    }
}
