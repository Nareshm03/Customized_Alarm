package com.example.teacherscheduler.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.teacherscheduler.R
import com.example.teacherscheduler.data.Repository
import com.example.teacherscheduler.databinding.ActivityAddEditMeetingModernBinding
import com.example.teacherscheduler.model.Meeting
import com.example.teacherscheduler.util.ConflictDetector
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ModernAddEditMeetingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddEditMeetingModernBinding
    private lateinit var repository: Repository
    private var editingMeeting: Meeting? = null
    private var selectedDate = Calendar.getInstance()
    private var startTime = Calendar.getInstance()
    private var endTime = Calendar.getInstance()
    private var selectedReminderMinutes: Int = 15
    
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

    companion object {
        const val EXTRA_MEETING_ID = "extra_meeting_id"

        fun newIntent(context: Context, meetingId: Long? = null): Intent {
            return Intent(context, ModernAddEditMeetingActivity::class.java).apply {
                meetingId?.let { putExtra(EXTRA_MEETING_ID, it) }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditMeetingModernBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = Repository(this)
        
        setupToolbar()
        setupBackPressedCallback()
        setupReminderChips()
        setupClickListeners()
        loadMeetingData()
        setDefaultDateTime()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        binding.toolbar.setNavigationOnClickListener {
            handleBackPress()
        }
    }
    
    private fun setupBackPressedCallback() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleBackPress()
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)
    }
    
    private fun handleBackPress() {
        if (hasUnsavedChanges()) {
            MaterialAlertDialogBuilder(this)
                .setTitle("Discard Changes")
                .setMessage("You have unsaved changes. Are you sure you want to discard them?")
                .setPositiveButton("Discard") { _, _ -> 
                    finish()
                }
                .setNegativeButton("Keep Editing", null)
                .show()
        } else {
            finish()
        }
    }

    private fun setupReminderChips() {
        binding.chip5min.setOnClickListener { selectedReminderMinutes = 5; updateReminderChips() }
        binding.chip10min.setOnClickListener { selectedReminderMinutes = 10; updateReminderChips() }
        binding.chip15min.setOnClickListener { selectedReminderMinutes = 15; updateReminderChips() }
        binding.chip30min.setOnClickListener { selectedReminderMinutes = 30; updateReminderChips() }
        binding.chip60min.setOnClickListener { selectedReminderMinutes = 60; updateReminderChips() }
        
        updateReminderChips()
    }
    
    private fun updateReminderChips() {
        binding.chip5min.isChecked = selectedReminderMinutes == 5
        binding.chip10min.isChecked = selectedReminderMinutes == 10
        binding.chip15min.isChecked = selectedReminderMinutes == 15
        binding.chip30min.isChecked = selectedReminderMinutes == 30
        binding.chip60min.isChecked = selectedReminderMinutes == 60
    }

    private fun setupClickListeners() {
        binding.buttonDate.setOnClickListener { showDatePicker() }
        binding.buttonStartTime.setOnClickListener { showStartTimePicker() }
        binding.buttonEndTime.setOnClickListener { showEndTimePicker() }
        binding.buttonSave.setOnClickListener { saveMeeting() }
        binding.buttonDelete.setOnClickListener { deleteMeeting() }
        binding.buttonCancel.setOnClickListener { finish() }
    }

    private fun loadMeetingData() {
        val meetingId = intent.getLongExtra(EXTRA_MEETING_ID, -1)
        if (meetingId != -1L) {
            lifecycleScope.launch {
                try {
                    val foundMeeting = repository.getMeetingById(meetingId)
                    editingMeeting = foundMeeting
                    binding.toolbar.title = "Edit Meeting"
                    binding.headerTitle.text = "Edit Meeting"
                    populateFields(foundMeeting)
                    binding.buttonDelete.visibility = View.VISIBLE
                } catch (e: Exception) {
                    binding.toolbar.title = "Add Meeting"
                    binding.headerTitle.text = "Create New Meeting"
                    binding.buttonDelete.visibility = View.GONE
                    setDefaultDateTime()
                }
            }
        } else {
            binding.toolbar.title = "Add Meeting"
            binding.headerTitle.text = "Create New Meeting"
            binding.buttonDelete.visibility = View.GONE
            setDefaultDateTime()
        }
    }

    private fun populateFields(meeting: Meeting) {
        binding.editTitle.setText(meeting.title)
        binding.editWithWhom.setText(meeting.withWhom)
        binding.editLocation.setText(meeting.location)
        binding.editNotes.setText(meeting.notes)
        binding.switchNotifications.isChecked = meeting.notificationsEnabled
        selectedReminderMinutes = meeting.reminderMinutes

        // Set dates and times
        selectedDate.time = meeting.startDate
        startTime.time = meeting.startTime
        endTime.time = meeting.endTime

        updateDateTimeButtons()
        updateReminderChips()
    }

    private fun setDefaultDateTime() {
        if (editingMeeting == null) {
            val now = Calendar.getInstance()
            now.add(Calendar.HOUR_OF_DAY, 1)
            now.set(Calendar.MINUTE, 0)
            now.set(Calendar.SECOND, 0)
            now.set(Calendar.MILLISECOND, 0)
            
            selectedDate.time = now.time
            startTime.time = now.time
            
            val endCal = Calendar.getInstance()
            endCal.time = now.time
            endCal.add(Calendar.HOUR_OF_DAY, 1)
            endTime.time = endCal.time
            
            updateDateTimeButtons()
        }
    }

    private fun showDatePicker() {
        DatePickerDialog(
            this,
            R.style.MaterialDatePickerTheme,
            { _, year, month, dayOfMonth ->
                selectedDate.set(year, month, dayOfMonth)
                updateDateTimeButtons()
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showStartTimePicker() {
        TimePickerDialog(
            this,
            R.style.MaterialTimePickerTheme,
            { _, hourOfDay, minute ->
                startTime.set(Calendar.HOUR_OF_DAY, hourOfDay)
                startTime.set(Calendar.MINUTE, minute)
                
                // Auto-adjust end time
                if (endTime.timeInMillis <= startTime.timeInMillis) {
                    endTime.timeInMillis = startTime.timeInMillis + (60 * 60 * 1000)
                }
                
                updateDateTimeButtons()
            },
            startTime.get(Calendar.HOUR_OF_DAY),
            startTime.get(Calendar.MINUTE),
            false
        ).show()
    }

    private fun showEndTimePicker() {
        TimePickerDialog(
            this,
            R.style.MaterialTimePickerTheme,
            { _, hourOfDay, minute ->
                endTime.set(Calendar.HOUR_OF_DAY, hourOfDay)
                endTime.set(Calendar.MINUTE, minute)
                
                if (endTime.timeInMillis <= startTime.timeInMillis) {
                    Toast.makeText(this, "End time must be after start time", Toast.LENGTH_SHORT).show()
                } else {
                    updateDateTimeButtons()
                }
            },
            endTime.get(Calendar.HOUR_OF_DAY),
            endTime.get(Calendar.MINUTE),
            false
        ).show()
    }

    private fun updateDateTimeButtons() {
        binding.buttonDate.text = dateFormat.format(selectedDate.time)
        binding.buttonStartTime.text = timeFormat.format(startTime.time)
        binding.buttonEndTime.text = timeFormat.format(endTime.time)
    }

    private fun saveMeeting() {
        val title = binding.editTitle.text.toString().trim()
        val withWhom = binding.editWithWhom.text.toString().trim()
        val location = binding.editLocation.text.toString().trim()
        val notes = binding.editNotes.text.toString().trim()

        if (title.isEmpty()) {
            binding.editTitle.error = "Title is required"
            binding.editTitle.requestFocus()
            return
        }

        if (withWhom.isEmpty()) {
            binding.editWithWhom.error = "Specify who you're meeting with"
            binding.editWithWhom.requestFocus()
            return
        }

        if (endTime.timeInMillis <= startTime.timeInMillis) {
            Toast.makeText(this, "End time must be after start time", Toast.LENGTH_SHORT).show()
            return
        }

        // Properly combine date and time for Meeting object
        val combinedStart = Calendar.getInstance().apply {
            time = selectedDate.time
            set(Calendar.HOUR_OF_DAY, startTime.get(Calendar.HOUR_OF_DAY))
            set(Calendar.MINUTE, startTime.get(Calendar.MINUTE))
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        val combinedEnd = Calendar.getInstance().apply {
            time = selectedDate.time
            set(Calendar.HOUR_OF_DAY, endTime.get(Calendar.HOUR_OF_DAY))
            set(Calendar.MINUTE, endTime.get(Calendar.MINUTE))
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val meeting = editingMeeting?.copy(
            title = title,
            withWhom = withWhom,
            location = location,
            notes = notes,
            startDate = combinedStart.time,
            endDate = combinedEnd.time,
            startTime = combinedStart.time,
            endTime = combinedEnd.time,
            notificationsEnabled = binding.switchNotifications.isChecked,
            reminderMinutes = selectedReminderMinutes
        ) ?: Meeting(
            title = title,
            withWhom = withWhom,
            location = location,
            notes = notes,
            startDate = combinedStart.time,
            endDate = combinedEnd.time,
            startTime = combinedStart.time,
            endTime = combinedEnd.time,
            notificationsEnabled = binding.switchNotifications.isChecked,
            reminderMinutes = selectedReminderMinutes
        )

        lifecycleScope.launch {
            try {
                val existingMeetings = repository.getAllActiveMeetingsSync()
                val existingClasses = repository.getAllActiveClassesSync()
                val conflicts = ConflictDetector.checkMeetingConflicts(
                    meeting, existingMeetings, existingClasses, editingMeeting?.id ?: -1
                )
                
                if (conflicts.isNotEmpty()) {
                    showConflictDialog(conflicts, meeting)
                } else {
                    performSave(meeting)
                }
            } catch (e: Exception) {
                Toast.makeText(this@ModernAddEditMeetingActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun showConflictDialog(conflicts: List<ConflictDetector.Conflict>, meeting: Meeting) {
        val message = StringBuilder("This meeting conflicts with:\n\n")
        conflicts.forEach { conflict ->
            message.append("• ${conflict.title} (${conflict.time})\n")
        }
        message.append("\nSave anyway?")
        
        MaterialAlertDialogBuilder(this)
            .setTitle("Schedule Conflict")
            .setMessage(message.toString())
            .setPositiveButton("Save Anyway") { _, _ -> performSave(meeting) }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun performSave(meeting: Meeting) {
        lifecycleScope.launch {
            try {
                if (editingMeeting == null) {
                    repository.insertMeeting(meeting)
                    Snackbar.make(binding.root, "Meeting added", Snackbar.LENGTH_SHORT).show()
                } else {
                    repository.updateMeeting(meeting)
                    Snackbar.make(binding.root, "Meeting updated", Snackbar.LENGTH_SHORT).show()
                }
                setResult(RESULT_OK)
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@ModernAddEditMeetingActivity, "Save failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun deleteMeeting() {
        editingMeeting?.let { meeting ->
            MaterialAlertDialogBuilder(this)
                .setTitle("Delete Meeting")
                .setMessage("Are you sure you want to delete this meeting?")
                .setPositiveButton("Delete") { _, _ ->
                    lifecycleScope.launch {
                        try {
                            repository.deleteMeeting(meeting)
                            Toast.makeText(this@ModernAddEditMeetingActivity, "Meeting deleted", Toast.LENGTH_SHORT).show()
                            setResult(RESULT_OK)
                            finish()
                        } catch (e: Exception) {
                            Toast.makeText(this@ModernAddEditMeetingActivity, "Delete failed: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun hasUnsavedChanges(): Boolean {
        if (editingMeeting != null) {
            val m = editingMeeting!!
            return binding.editTitle.text.toString() != m.title ||
                   binding.editWithWhom.text.toString() != m.withWhom ||
                   binding.editLocation.text.toString() != m.location ||
                   binding.editNotes.text.toString() != m.notes ||
                   binding.switchNotifications.isChecked != m.notificationsEnabled ||
                   selectedReminderMinutes != m.reminderMinutes
        }
        return binding.editTitle.text?.isNotEmpty() == true ||
               binding.editWithWhom.text?.isNotEmpty() == true ||
               binding.editLocation.text?.isNotEmpty() == true ||
               binding.editNotes.text?.isNotEmpty() == true
    }
}
