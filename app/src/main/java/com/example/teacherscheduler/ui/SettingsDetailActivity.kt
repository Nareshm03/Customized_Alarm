package com.example.teacherscheduler.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.example.teacherscheduler.R
import com.example.teacherscheduler.databinding.ActivitySettingsDetailBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SettingsDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsDetailBinding
    private var isFirebaseLoggedIn = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Make it immersive (modern approach instead of deprecated FLAG_FULLSCREEN)
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)

        binding = ActivitySettingsDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up UI components
        setupUI()

        // Set up listeners
        setupListeners()

        // Load saved settings
        loadSettings()
    }

    private fun setupUI() {
        // Set up back button
        binding.backButton.setOnClickListener {
            finish()
        }

        // Update Firebase login status
        updateFirebaseLoginStatus()
    }

    private fun setupListeners() {
        // Notification switches
        binding.classNotificationSwitch.setOnCheckedChangeListener { _, isChecked ->
            saveNotificationSetting("class_notifications_enabled", isChecked)
        }

        binding.meetingNotificationSwitch.setOnCheckedChangeListener { _, isChecked ->
            saveNotificationSetting("meeting_notifications_enabled", isChecked)
        }

        // Auto sync switch
        binding.autoSyncSwitch.setOnCheckedChangeListener { _, isChecked ->
            saveNotificationSetting("auto_sync_enabled", isChecked)
        }

        // Sync now button
        binding.syncNowButton.setOnClickListener {
            syncData()
        }

        // Firebase login button
        binding.firebaseLoginButton.setOnClickListener {
            toggleFirebaseLogin()
        }

        // Save button
        binding.saveButton.setOnClickListener {
            saveAllSettings()
            finish()
        }
    }

    private fun loadSettings() {
        val prefs = getSharedPreferences("app_settings", MODE_PRIVATE)

        // Load notification settings
        binding.classNotificationSwitch.isChecked =
            prefs.getBoolean("class_notifications_enabled", true)
        binding.meetingNotificationSwitch.isChecked =
            prefs.getBoolean("meeting_notifications_enabled", true)

        // Load auto sync setting
        binding.autoSyncSwitch.isChecked =
            prefs.getBoolean("auto_sync_enabled", true)

        // Load Firebase login status
        isFirebaseLoggedIn = prefs.getBoolean("firebase_logged_in", false)
        updateFirebaseLoginStatus()

        // Load last sync time
        val lastSyncTime = prefs.getString("last_sync_time", null)
        if (lastSyncTime != null) {
            binding.lastSyncTextView.text = getString(R.string.last_sync_time, lastSyncTime)
        } else {
            binding.lastSyncTextView.text = getString(R.string.last_sync_never)
        }
    }

    private fun saveNotificationSetting(key: String, value: Boolean) {
        val prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
        prefs.edit {
            putBoolean(key, value)
        }
    }

    private fun saveAllSettings() {
        val prefs = getSharedPreferences("app_settings", MODE_PRIVATE)

        prefs.edit {
            putBoolean("class_notifications_enabled", binding.classNotificationSwitch.isChecked)
            putBoolean("meeting_notifications_enabled", binding.meetingNotificationSwitch.isChecked)
            putBoolean("auto_sync_enabled", binding.autoSyncSwitch.isChecked)
            putBoolean("firebase_logged_in", isFirebaseLoggedIn)
        }

        Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show()
    }

    private fun syncData() {
        if (!isFirebaseLoggedIn) {
            Toast.makeText(this, "Please login to Firebase first", Toast.LENGTH_SHORT).show()
            return
        }

        // Simulate sync
        Toast.makeText(this, "Syncing data with Firebase...", Toast.LENGTH_SHORT).show()

        // Update last sync time
        val currentTime = getCurrentTimeFormatted()
        binding.lastSyncTextView.text = getString(R.string.last_sync_time, currentTime)

        // Save last sync time
        val prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
        prefs.edit {
            putString("last_sync_time", currentTime)
        }
    }

    private fun getCurrentTimeFormatted(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun toggleFirebaseLogin() {
        isFirebaseLoggedIn = !isFirebaseLoggedIn

        if (isFirebaseLoggedIn) {
            // Simulate Firebase login
            Toast.makeText(this, "Logging in to Firebase...", Toast.LENGTH_SHORT).show()
            // In a real app, you would implement Firebase Authentication here
        } else {
            // Simulate Firebase logout
            Toast.makeText(this, "Logged out from Firebase", Toast.LENGTH_SHORT).show()
            // In a real app, you would implement Firebase Authentication logout here
        }

        updateFirebaseLoginStatus()
    }

    private fun updateFirebaseLoginStatus() {
        if (isFirebaseLoggedIn) {
            binding.firebaseLoginStatus.text = "Logged in"
            binding.firebaseLoginButton.text = getString(R.string.logout_from_firebase)
        } else {
            binding.firebaseLoginStatus.text = getString(R.string.firebase_login_status)
            binding.firebaseLoginButton.text = getString(R.string.login_to_firebase)
        }
    }
}