package com.example.teacherscheduler.ui

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.example.teacherscheduler.R
import com.example.teacherscheduler.data.Repository
import com.example.teacherscheduler.databinding.FragmentSettingsBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SettingsFragment : DialogFragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private var isFirebaseLoggedIn = false
    private lateinit var repository: Repository
    
    override fun getTheme(): Int {
        return R.style.FullScreenDialogStyle
    }
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.apply {
            // Set layout to match parent (full screen)
            setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            )
            // Remove the default dialog background dimming
            clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            // Set the dialog to appear as a full screen
            addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)
            // Set the background to transparent
            setBackgroundDrawableResource(android.R.color.transparent)
            // Set window to full screen using modern API
            activity?.window?.let { window ->
                WindowCompat.setDecorFitsSystemWindows(window, false)
                val controller = WindowInsetsControllerCompat(window, decorView)
                controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
            // Apply animation
            attributes?.windowAnimations = R.style.SettingsDialogAnimation
        }
        return dialog
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        
        // Handle back button press
        dialog?.setOnKeyListener { _, keyCode, event ->
            if (keyCode == android.view.KeyEvent.KEYCODE_BACK && event.action == android.view.KeyEvent.ACTION_UP) {
                // Save settings and dismiss with animation
                saveSettings()
                dismiss()
                return@setOnKeyListener true
            }
            false
        }
        
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize repository
        repository = Repository(requireContext())
        
        // Set up UI components
        setupUI()
        
        // Set up listeners
        setupListeners()
        
        // Load saved settings
        loadSettings()
        
        // Ensure dialog takes up full screen
        dialog?.window?.apply {
            setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            )
        }
    }
    
    private fun setupUI() {
        // Set up notification switches
        binding.switchClassNotifications.isChecked = true
        binding.switchMeetingNotifications.isChecked = true
        
        // Set up auto-sync switch
        binding.switchAutoSync.isChecked = true
        
        // Set up sync now button
        binding.buttonSyncNow.setOnClickListener {
            syncData()
        }
        
        // Update Firebase login status
        updateFirebaseLoginStatus()
    }
    
    private fun getCurrentTimeFormatted(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }
    
    private fun setupListeners() {
        // Firebase login button
        binding.buttonFirebaseLogin.setOnClickListener {
            toggleFirebaseLogin()
        }
        
        // Save button
        binding.buttonSaveSettings.setOnClickListener {
            saveSettings()
            dismiss()
        }
        
        // Close button - using findViewById since buttonClose might not be in the binding
        val buttonClose = view?.findViewById<android.widget.ImageView>(R.id.buttonClose)
        buttonClose?.setOnClickListener {
            saveSettings()
            dismiss()
        }
    }
    
    private fun loadSettings() {
        val prefs = requireContext().getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        
        // Load notification settings
        binding.switchClassNotifications.isChecked = 
            prefs.getBoolean("class_notifications_enabled", true)
        binding.switchMeetingNotifications.isChecked = 
            prefs.getBoolean("meeting_notifications_enabled", true)
        
        // Load auto sync setting
        binding.switchAutoSync.isChecked = 
            prefs.getBoolean("auto_sync_enabled", true)
            
        // Load Firebase login status
        isFirebaseLoggedIn = prefs.getBoolean("firebase_logged_in", false)
        updateFirebaseLoginStatus()
        
        // Load last sync time
        val lastSyncTime = prefs.getString("last_sync_time", null)
        if (lastSyncTime != null) {
            binding.textViewLastSyncTime.text = getString(R.string.last_sync_time, lastSyncTime)
        } else {
            binding.textViewLastSyncTime.text = getString(R.string.last_sync_never)
        }
    }
    
    private fun saveSettings() {
        // Get notification settings
        val classNotificationsEnabled = binding.switchClassNotifications.isChecked
        val meetingNotificationsEnabled = binding.switchMeetingNotifications.isChecked
        
        // Get auto-sync setting
        val autoSyncEnabled = binding.switchAutoSync.isChecked
        
        // Save settings to preferences
        val prefs = requireContext().getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean("class_notifications_enabled", classNotificationsEnabled)
            .putBoolean("meeting_notifications_enabled", meetingNotificationsEnabled)
            .putBoolean("auto_sync_enabled", autoSyncEnabled)
            .putBoolean("firebase_logged_in", isFirebaseLoggedIn)
            .apply()
        
        // Show confirmation
        Toast.makeText(requireContext(), "Settings saved", Toast.LENGTH_SHORT).show()
        
        // Apply settings
        applySettings(classNotificationsEnabled, meetingNotificationsEnabled, autoSyncEnabled)
    }
    
    private fun applySettings(
        classNotificationsEnabled: Boolean,
        meetingNotificationsEnabled: Boolean,
        autoSyncEnabled: Boolean
    ) {
        // Apply notification settings
        Log.d("SettingsFragment", "Class notifications: $classNotificationsEnabled")
        Log.d("SettingsFragment", "Meeting notifications: $meetingNotificationsEnabled")
        
        // Apply auto-sync setting
        Log.d("SettingsFragment", "Auto-sync: $autoSyncEnabled")
        Log.d("SettingsFragment", "Firebase logged in: $isFirebaseLoggedIn")
        
        // TODO: Implement actual settings application
    }
    
    private fun syncData() {
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            Toast.makeText(requireContext(), "Please login to Firebase first", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Show syncing message
        Toast.makeText(requireContext(), "Syncing data with Firebase...", Toast.LENGTH_SHORT).show()
        
        // Disable sync button during sync
        binding.buttonSyncNow.isEnabled = false
        
        // Launch coroutine to perform sync
        lifecycleScope.launch {
            try {
                // Perform cloud sync
                val syncTimestamp = repository.performSync()
                
                // Update UI on main thread
                withContext(Dispatchers.Main) {
                    if (syncTimestamp > 0) {
                        // Update last sync time display
                        val lastSyncTime = formatSyncTime(syncTimestamp)
                        binding.textViewLastSyncTime.text = getString(R.string.last_sync_time, lastSyncTime)
                        
                        Toast.makeText(requireContext(), "Sync completed successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        // Show debug info when sync fails
                        val debugInfo = repository.performSyncDebug()
                        android.util.Log.d("SettingsFragment", debugInfo)
                        
                        Toast.makeText(requireContext(), "Sync failed. Check logs for details.", Toast.LENGTH_LONG).show()
                    }
                    
                    // Re-enable sync button
                    binding.buttonSyncNow.isEnabled = true
                }
            } catch (e: Exception) {
                // Handle any exceptions
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error during sync: ${e.message}", Toast.LENGTH_SHORT).show()
                    binding.buttonSyncNow.isEnabled = true
                }
            }
        }
    }
    
    private fun toggleFirebaseLogin() {
        val auth = FirebaseAuth.getInstance()
        
        if (auth.currentUser != null) {
            // User is logged in, so log them out
            auth.signOut()
            isFirebaseLoggedIn = false
            Toast.makeText(requireContext(), "Logged out from Firebase", Toast.LENGTH_SHORT).show()
        } else {
            // User is not logged in, launch the login activity
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
            // We'll update the login status when the fragment resumes
        }
        
        updateFirebaseLoginStatus()
    }
    
    override fun onResume() {
        super.onResume()
        // Check Firebase login status
        val auth = FirebaseAuth.getInstance()
        isFirebaseLoggedIn = auth.currentUser != null
        updateFirebaseLoginStatus()
        
        // Ensure dialog is full screen on resume
        dialog?.window?.apply {
            setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            )
        }
    }
    
    private fun updateFirebaseLoginStatus() {
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        
        if (user != null) {
            isFirebaseLoggedIn = true
            binding.textViewFirebaseLoginStatus.text = "Logged in as ${user.email}"
            binding.buttonFirebaseLogin.text = getString(R.string.logout_from_firebase)
        } else {
            isFirebaseLoggedIn = false
            binding.textViewFirebaseLoginStatus.text = getString(R.string.firebase_login_status)
            binding.buttonFirebaseLogin.text = getString(R.string.login_to_firebase)
        }
    }
    
    private fun formatSyncTime(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
        return dateFormat.format(Date(timestamp))
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}