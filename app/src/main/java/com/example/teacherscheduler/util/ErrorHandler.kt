package com.example.teacherscheduler.util

import android.content.Context
import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics

object ErrorHandler {
    
    private lateinit var crashlytics: FirebaseCrashlytics
    
    fun initialize(context: Context) {
        crashlytics = FirebaseCrashlytics.getInstance()
        
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("ErrorHandler", "Uncaught exception", throwable)
            crashlytics.recordException(throwable)
            crashlytics.sendUnsentReports()
        }
    }
    
    fun logError(tag: String, message: String, throwable: Throwable? = null) {
        Log.e(tag, message, throwable)
        crashlytics.log("$tag: $message")
        throwable?.let { crashlytics.recordException(it) }
    }
    
    fun logWarning(tag: String, message: String) {
        Log.w(tag, message)
        crashlytics.log("WARNING - $tag: $message")
    }
    
    fun setUserId(userId: String) {
        crashlytics.setUserId(userId)
    }
}
