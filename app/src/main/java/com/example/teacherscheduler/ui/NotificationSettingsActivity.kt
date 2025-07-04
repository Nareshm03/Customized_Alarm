package com.example.teacherscheduler.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.teacherscheduler.data.SettingsManager
import com.example.teacherscheduler.databinding.ActivityNotificationSettingsBinding
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch

class NotificationSettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNotificationSettingsBinding
    private lateinit var settingsManager: SettingsManager
    private val selectedIntervals = mutableSetOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        settingsManager = SettingsManager(this)
        
        setupToolbar()
        setupClickListeners()
        loadCurrentSettings()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupClickListeners() {
        binding.buttonSaveSettings.setOnClickListener {
            saveSettings()
        }
        
        // Setup reminder interval chips
        val intervalChips = mapOf(
            binding.chip60min to 60,
            binding.chip45min to 45,
            binding.chip30min to 30,
            binding.chip15min to 15,
            binding.chip5min to 5,
            binding.chip2min to 2,
            binding.chip1min to 1,
            binding.chipExactTime to 0
        )
        
        intervalChips.forEach { (chip, minutes) ->
            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedIntervals.add(minutes)
                } else {
                    selectedIntervals.remove(minutes)
                }
            }
        }
    }

    private fun loadCurrentSettings() {
        lifecycleScope.launch {
            try {
                // Load global notification settings
                binding.switchGlobalNotifications.isChecked = settingsManager.areNotificationsEnabled()
                binding.switchSoundVibration.isChecked = settingsManager.isSoundEnabled()
                binding.switchClassNotifications.isChecked = settingsManager.areClassNotificationsEnabled()
                binding.switchMeetingNotifications.isChecked = settingsManager.areMeetingNotificationsEnabled()
                
                // Load reminder intervals
                val savedIntervals = settingsManager.getReminderIntervals()
                selectedIntervals.clear()
                selectedIntervals.addAll(savedIntervals)
                
                // Update chip states
                updateChipStates()
                
            } catch (e: Exception) {
                Toast.makeText(this@NotificationSettingsActivity, 
                    "Error loading settings: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateChipStates() {
        binding.chip60min.isChecked = selectedIntervals.contains(60)
        binding.chip45min.isChecked = selectedIntervals.contains(45)
        binding.chip30min.isChecked = selectedIntervals.contains(30)
        binding.chip15min.isChecked = selectedIntervals.contains(15)
        binding.chip5min.isChecked = selectedIntervals.contains(5)
        binding.chip2min.isChecked = selectedIntervals.contains(2)
        binding.chip1min.isChecked = selectedIntervals.contains(1)
        binding.chipExactTime.isChecked = selectedIntervals.contains(0)
    }

    private fun saveSettings() {
        lifecycleScope.launch {
            try {
                // Save all settings
                settingsManager.setNotificationsEnabled(binding.switchGlobalNotifications.isChecked)
                settingsManager.setSoundEnabled(binding.switchSoundVibration.isChecked)
                settingsManager.setClassNotificationsEnabled(binding.switchClassNotifications.isChecked)
                settingsManager.setMeetingNotificationsEnabled(binding.switchMeetingNotifications.isChecked)
                settingsManager.setReminderIntervals(selectedIntervals.toList())
                
                Toast.makeText(this@NotificationSettingsActivity, 
                    "Settings saved successfully", Toast.LENGTH_SHORT).show()
                
                setResult(RESULT_OK)
                finish()
                
            } catch (e: Exception) {
                Toast.makeText(this@NotificationSettingsActivity, 
                    "Error saving settings: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}