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
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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
    private val selectedReminders = mutableSetOf<Int>()
    
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
        // The chips are now predefined in the layout, just set up their listeners
        binding.chip5min.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) selectedReminders.add(5) else selectedReminders.remove(5)
        }
        binding.chip10min.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) selectedReminders.add(10) else selectedReminders.remove(10)
        }
        binding.chip15min.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) selectedReminders.add(15) else selectedReminders.remove(15)
        }
        binding.chip30min.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) selectedReminders.add(30) else selectedReminders.remove(30)
        }
        binding.chip60min.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) selectedReminders.add(60) else selectedReminders.remove(60)
        }
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
                    // Meeting not found, treat as new meeting
                    binding.toolbar.title = "Add Meeting"
                    binding.headerTitle.text = "Create New Meeting"
                    binding.buttonDelete.visibility = View.GONE
                    setDefaultDateTime()
                }
            }
        } else {
            // Adding a new meeting
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

        // Set dates and times
        selectedDate.time = meeting.startDate
        startTime.time = meeting.startDate
        endTime.time = meeting.endDate

        updateDateTimeButtons()
    }

    private fun setDefaultDateTime() {
        if (editingMeeting == null) {
            // Set default to next hour
            val now = Calendar.getInstance()
            now.add(Calendar.HOUR_OF_DAY, 1)
            now.set(Calendar.MINUTE, 0)
            now.set(Calendar.SECOND, 0)
            
            selectedDate.time = now.time
            startTime.time = now.time
            
            // End time is 1 hour later
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
                // Update start and end times to use the same date
                startTime.set(Calendar.YEAR, year)
                startTime.set(Calendar.MONTH, month)
                startTime.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                endTime.set(Calendar.YEAR, year)
                endTime.set(Calendar.MONTH, month)
                endTime.set(Calendar.DAY_OF_MONTH, dayOfMonth)
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
                
                // Auto-adjust end time to be 1 hour later if it's before start time
                if (endTime.timeInMillis <= startTime.timeInMillis) {
                    endTime.timeInMillis = startTime.timeInMillis
                    endTime.add(Calendar.HOUR_OF_DAY, 1)
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
                
                // Validate that end time is after start time
                if (endTime.timeInMillis <= startTime.timeInMillis) {
                    Toast.makeText(this, "End time must be after start time", Toast.LENGTH_SHORT).show()
                    return@TimePickerDialog
                }
                
                updateDateTimeButtons()
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
            binding.editWithWhom.error = "Please specify who you're meeting with"
            binding.editWithWhom.requestFocus()
            return
        }

        if (endTime.timeInMillis <= startTime.timeInMillis) {
            Toast.makeText(this, "End time must be after start time", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val meeting = editingMeeting?.copy(
                    title = title,
                    withWhom = withWhom,
                    location = location,
                    notes = notes,
                    startDate = startTime.time,
                    endDate = endTime.time,
                    startTime = startTime.time,
                    endTime = endTime.time,
                    notificationsEnabled = binding.switchNotifications.isChecked
                ) ?: Meeting(
                    id = 0,
                    title = title,
                    withWhom = withWhom,
                    location = location,
                    notes = notes,
                    startDate = startTime.time,
                    endDate = endTime.time,
                    startTime = startTime.time,
                    endTime = endTime.time,
                    notificationsEnabled = binding.switchNotifications.isChecked
                )

                if (editingMeeting == null) {
                    repository.insertMeeting(meeting)
                    Toast.makeText(this@ModernAddEditMeetingActivity, "Meeting added successfully", Toast.LENGTH_SHORT).show()
                } else {
                    repository.updateMeeting(meeting)
                    Toast.makeText(this@ModernAddEditMeetingActivity, "Meeting updated successfully", Toast.LENGTH_SHORT).show()
                }

                setResult(RESULT_OK)
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@ModernAddEditMeetingActivity, "Error saving meeting: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun deleteMeeting() {
        editingMeeting?.let { meeting ->
            MaterialAlertDialogBuilder(this)
                .setTitle("Delete Meeting")
                .setMessage("Are you sure you want to delete this meeting? This action cannot be undone.")
                .setPositiveButton("Delete") { _, _ ->
                    lifecycleScope.launch {
                        try {
                            repository.deleteMeeting(meeting)
                            Toast.makeText(this@ModernAddEditMeetingActivity, "Meeting deleted successfully", Toast.LENGTH_SHORT).show()
                            setResult(RESULT_OK)
                            finish()
                        } catch (e: Exception) {
                            Toast.makeText(this@ModernAddEditMeetingActivity, "Error deleting meeting: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun hasUnsavedChanges(): Boolean {
        // Check if there are unsaved changes
        val hasChanges = binding.editTitle.text?.isNotEmpty() == true ||
                binding.editWithWhom.text?.isNotEmpty() == true ||
                binding.editLocation.text?.isNotEmpty() == true ||
                binding.editNotes.text?.isNotEmpty() == true

        return hasChanges && editingMeeting == null
    }
}