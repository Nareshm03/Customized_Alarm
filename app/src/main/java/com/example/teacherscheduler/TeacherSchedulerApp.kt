package com.example.teacherscheduler

import android.app.Application
import android.util.Log
import com.example.teacherscheduler.util.AnalyticsTracker
import com.example.teacherscheduler.util.ErrorHandler
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TeacherSchedulerApp : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        try {
            // Check Google Play Services availability
            val googleApiAvailability = GoogleApiAvailability.getInstance()
            val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(this)

            if (resultCode == ConnectionResult.SUCCESS) {
                // Initialize analytics (requires Google Play Services)
                try {
                    AnalyticsTracker.initialize(this)
                } catch (e: Exception) {
                    Log.w("TeacherSchedulerApp", "Analytics initialization failed", e)
                }
            } else {
                Log.w("TeacherSchedulerApp", "Google Play Services not available. Code: $resultCode. App will work with limited functionality.")
            }

            // Initialize error handler
            ErrorHandler.initialize(this)

            Log.d("TeacherSchedulerApp", "App initialized successfully")
        } catch (e: Exception) {
            Log.e("TeacherSchedulerApp", "Error initializing app", e)
        }
    }
}
