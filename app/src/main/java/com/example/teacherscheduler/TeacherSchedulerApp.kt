package com.example.teacherscheduler

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp

class TeacherSchedulerApp : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        try {
            // Initialize Firebase with error handling
            try {
                FirebaseApp.initializeApp(this)
                Log.d("TeacherSchedulerApp", "Firebase initialized successfully")
            } catch (e: Exception) {
                Log.w("TeacherSchedulerApp", "Firebase initialization failed, continuing without Firebase", e)
            }
            
            // App initialization
            Log.d("TeacherSchedulerApp", "App initialized successfully")
        } catch (e: Exception) {
            Log.e("TeacherSchedulerApp", "Error initializing app components", e)
        }
    }
}