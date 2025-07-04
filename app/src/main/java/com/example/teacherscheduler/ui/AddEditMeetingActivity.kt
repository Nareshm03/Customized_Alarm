package com.example.teacherscheduler.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.teacherscheduler.R
import androidx.lifecycle.lifecycleScope
import com.example.teacherscheduler.data.Repository
import com.example.teacherscheduler.data.SettingsManager
import com.example.teacherscheduler.notification.EnhancedNotificationHelper
import kotlinx.coroutines.launch
import com.example.teacherscheduler.databinding.ActivityAddEditMeetingBinding
import com.example.teacherscheduler.model.Meeting
import java.text.SimpleDateFormat
import java.util.*

class AddEditMeetingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddEditMeetingBinding
    private lateinit var repository: Repository
    private lateinit var notificationHelper: EnhancedNotificationHelper
    private var editingMeeting: Meeting? = null
    private var startDate = Calendar.getInstance()
    private var endDate = Calendar.getInstance()
    private var startTime = Calendar.getInstance()
    private var endTime = Calendar.getInstance()

    private val reminderValues = arrayOf(5, 10, 15, 30)

    companion object {
        const val EXTRA_MEETING_ID = "extra_meeting_id"
        
        fun newIntent(context: Context, meetingId: Long? = null): Intent {
            return Intent(context, AddEditMeetingActivity::class.java).apply {
                meetingId?.let { putExtra(EXTRA_MEETING_ID, it) }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditMeetingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        repository = Repository(this)
        notificationHelper = EnhancedNotificationHelper(this)
        setupToolbar()

        loadMeetingData()
        setupDateTimePickers()
        setupButtons()
    }
    

    


    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun loadMeetingData() {
        val meetingId = intent.getLongExtra(EXTRA_MEETING_ID, -1)
        if (meetingId != -1L) {
            lifecycleScope.launch {
                try {
                    val foundMeeting = repository.getMeetingById(meetingId)
                    editingMeeting = foundMeeting
                    binding.toolbarTitle.text = getString(R.string.edit_meeting)
                    populateFields(foundMeeting)
                    binding.buttonDelete.isEnabled = true
                } catch (e: Exception) {
                    // Meeting not found, treat as new meeting
                    binding.toolbarTitle.text = getString(R.string.add_meeting)
                    binding.buttonDelete.isEnabled = false
                    setDefaultDateTime()
                }
            }
        } else {
            // Adding a new meeting
            binding.toolbarTitle.text = getString(R.string.add_meeting)
            binding.buttonDelete.isEnabled = false
            setDefaultDateTime()
        }
    }

    private fun populateFields(meeting: Meeting) {
        binding.editTitle.setText(meeting.title)
        binding.editWithWhom.setText(meeting.withWhom)
        binding.editLocation.setText(meeting.location)
        binding.editNotes.setText(meeting.notes)
        binding.switchNotifications.isChecked = meeting.notificationsEnabled
        
        // Set dates and times from the meeting item
        startDate.time = meeting.startDate
        endDate.time = meeting.endDate
        startTime.time = meeting.startTime
        endTime.time = meeting.endTime
        

        

        
        // Update UI displays
        updateDateTimeDisplays()
    }

    private fun setDefaultDateTime() {
        // Set default to current date and time
        val now = Calendar.getInstance()
        startDate.time = now.time
        endDate.time = now.time
        
        // Set start time to nearest hour
        startTime.set(Calendar.MINUTE, 0)
        startTime.set(Calendar.SECOND, 0)
        startTime.set(Calendar.MILLISECOND, 0)
        
        // Set end time to 1 hour after start
        endTime.time = startTime.time
        endTime.add(Calendar.HOUR_OF_DAY, 1)
        
        updateDateTimeDisplays()
    }

    private fun setupDateTimePickers() {
        // Start Date Picker
        binding.buttonStartDate.setOnClickListener {
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    startDate.set(year, month, day)
                    
                    // FIXED: Update time objects with the new date
                    val currentStartHour = startTime.get(Calendar.HOUR_OF_DAY)
                    val currentStartMinute = startTime.get(Calendar.MINUTE)
                    
                    startTime.set(year, month, day, currentStartHour, currentStartMinute)
                    startTime.set(Calendar.SECOND, 0)
                    startTime.set(Calendar.MILLISECOND, 0)
                    
                    updateDateTimeDisplays()
                },
                startDate.get(Calendar.YEAR),
                startDate.get(Calendar.MONTH),
                startDate.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // Start Time Picker
        binding.buttonStartTime.setOnClickListener {
            TimePickerDialog(
                this,
                { _, hour, minute ->
                    // FIXED: Properly set time without timezone issues
                    startTime.set(Calendar.YEAR, startDate.get(Calendar.YEAR))
                    startTime.set(Calendar.MONTH, startDate.get(Calendar.MONTH))
                    startTime.set(Calendar.DAY_OF_MONTH, startDate.get(Calendar.DAY_OF_MONTH))
                    startTime.set(Calendar.HOUR_OF_DAY, hour)
                    startTime.set(Calendar.MINUTE, minute)
                    startTime.set(Calendar.SECOND, 0)
                    startTime.set(Calendar.MILLISECOND, 0)
                    
                    // Debug log the time picker values
                    val timeZone = java.util.TimeZone.getDefault()
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.getDefault())
                    android.util.Log.d("MeetingTimePicker", "Selected hour: $hour, minute: $minute")
                    android.util.Log.d("MeetingTimePicker", "Set startTime to: ${dateFormat.format(startTime.time)}")
                    
                    updateDateTimeDisplays()
                },
                startTime.get(Calendar.HOUR_OF_DAY),
                startTime.get(Calendar.MINUTE),
                false
            ).show()
        }

        // End Date Picker
        binding.buttonEndDate.setOnClickListener {
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    endDate.set(year, month, day)
                    
                    // FIXED: Update end time with the new date
                    val currentEndHour = endTime.get(Calendar.HOUR_OF_DAY)
                    val currentEndMinute = endTime.get(Calendar.MINUTE)
                    
                    endTime.set(year, month, day, currentEndHour, currentEndMinute)
                    endTime.set(Calendar.SECOND, 0)
                    endTime.set(Calendar.MILLISECOND, 0)
                    
                    updateDateTimeDisplays()
                },
                endDate.get(Calendar.YEAR),
                endDate.get(Calendar.MONTH),
                endDate.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // End Time Picker
        binding.buttonEndTime.setOnClickListener {
            TimePickerDialog(
                this,
                { _, hour, minute ->
                    // FIXED: Properly set time without timezone issues
                    endTime.set(Calendar.YEAR, endDate.get(Calendar.YEAR))
                    endTime.set(Calendar.MONTH, endDate.get(Calendar.MONTH))
                    endTime.set(Calendar.DAY_OF_MONTH, endDate.get(Calendar.DAY_OF_MONTH))
                    endTime.set(Calendar.HOUR_OF_DAY, hour)
                    endTime.set(Calendar.MINUTE, minute)
                    endTime.set(Calendar.SECOND, 0)
                    endTime.set(Calendar.MILLISECOND, 0)
                    
                    // Debug log the time picker values
                    val timeZone = java.util.TimeZone.getDefault()
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.getDefault())
                    android.util.Log.d("MeetingTimePicker", "Selected end hour: $hour, minute: $minute")
                    android.util.Log.d("MeetingTimePicker", "Set endTime to: ${dateFormat.format(endTime.time)}")
                    
                    updateDateTimeDisplays()
                },
                endTime.get(Calendar.HOUR_OF_DAY),
                endTime.get(Calendar.MINUTE),
                false
            ).show()
        }
    }

    private fun updateDateTimeDisplays() {
        val dateFormat = SimpleDateFormat("EEE, MMM dd, yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        
        binding.buttonStartDate.text = dateFormat.format(startDate.time).uppercase()
        binding.buttonStartTime.text = timeFormat.format(startTime.time).uppercase()
        binding.buttonEndDate.text = dateFormat.format(endDate.time).uppercase()
        binding.buttonEndTime.text = timeFormat.format(endTime.time).uppercase()
    }

    private fun setupButtons() {
        binding.buttonSave.setOnClickListener {
            saveMeeting()
        }
        
        binding.buttonCancel.setOnClickListener {
            finish()
        }
        
        binding.buttonDelete.setOnClickListener {
            showDeleteConfirmation()
        }
    }

    private fun saveMeeting() {
        val title = binding.editTitle.text.toString().trim()
        val withWhom = binding.editWithWhom.text.toString().trim()
        val location = binding.editLocation.text.toString().trim()
        val notes = binding.editNotes.text.toString().trim()
        
        if (title.isEmpty() || withWhom.isEmpty()) {
            Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show()
            return
        }
        
        val meeting = Meeting(
            id = editingMeeting?.id ?: 0,

            title = title,
            withWhom = withWhom,
            location = location,
            notes = notes,
            startDate = startDate.time,
            endDate = endDate.time,
            startTime = startTime.time,
            endTime = endTime.time,
            isActive = true,
            notificationsEnabled = binding.switchNotifications.isChecked,
            reminderMinutes = 15
        )
        
        lifecycleScope.launch {
            try {
                if (editingMeeting != null) {
                    repository.updateMeeting(meeting)
                    Toast.makeText(this@AddEditMeetingActivity, "Meeting updated successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    repository.insertMeeting(meeting)
                    Toast.makeText(this@AddEditMeetingActivity, "Meeting added successfully!", Toast.LENGTH_SHORT).show()
                }
                
                // Schedule notification if notifications are enabled
                if (meeting.notificationsEnabled) {
                    notificationHelper.scheduleMeetingNotifications(meeting)
                }
                
                setResult(RESULT_OK)
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@AddEditMeetingActivity, "Error saving meeting: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDeleteConfirmation() {
        editingMeeting?.let { meeting ->
            AlertDialog.Builder(this)
                .setTitle("Delete Meeting")
                .setMessage("Are you sure you want to delete ${meeting.title}?")
                .setPositiveButton("Delete") { _, _ ->
                    lifecycleScope.launch {
                        repository.deleteMeeting(meeting)
                        Toast.makeText(this@AddEditMeetingActivity, "Meeting deleted", Toast.LENGTH_SHORT).show()
                        setResult(RESULT_OK)
                        finish()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }
}