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
import com.example.teacherscheduler.databinding.ActivityAddEditClassBinding
import com.example.teacherscheduler.model.Class
import com.example.teacherscheduler.notification.EnhancedNotificationHelper
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AddEditClassActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddEditClassBinding
    private lateinit var repository: Repository
    private lateinit var notificationHelper: EnhancedNotificationHelper
    private var editingClass: Class? = null
    private var startDate = Calendar.getInstance().apply {
        android.util.Log.d("CalendarInit", "startDate timezone: ${timeZone.displayName}")
    }
    private var endDate = Calendar.getInstance().apply {
        android.util.Log.d("CalendarInit", "endDate timezone: ${timeZone.displayName}")
    }
    private var startTime = Calendar.getInstance().apply {
        android.util.Log.d("CalendarInit", "startTime timezone: ${timeZone.displayName}")
    }
    private var endTime = Calendar.getInstance().apply {
        android.util.Log.d("CalendarInit", "endTime timezone: ${timeZone.displayName}")
    }

    private var selectedReminderMinutes: Int = 15

    companion object {
        const val EXTRA_CLASS_ID = "extra_class_id"

        fun newIntent(context: Context, classId: Long? = null): Intent {
            return Intent(context, AddEditClassActivity::class.java).apply {
                classId?.let { putExtra(EXTRA_CLASS_ID, it) }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditClassBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = Repository(this)
        notificationHelper = EnhancedNotificationHelper(this)
        setupToolbar()
        setupSpinners()
        loadClassData()
        setupDateTimePickers()
        setupButtons()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupSpinners() {
        // No spinners to setup
    }

    private fun loadClassData() {
        val classId = intent.getLongExtra(EXTRA_CLASS_ID, -1)
        if (classId != -1L) {
            lifecycleScope.launch {
                editingClass = repository.getClassById(classId)
                editingClass?.let { classItem ->
                    binding.toolbarTitle.text = getString(R.string.title_edit_class)
                    populateFields(classItem)
                    binding.buttonDelete.isEnabled = true
                }
            }
        } else {
            binding.toolbarTitle.text = getString(R.string.title_add_class)
            binding.buttonDelete.isEnabled = false
            setDefaultDateTime()
        }
    }

    private fun populateFields(classItem: Class) {
        binding.editSubject.setText(classItem.subject)
        binding.editDepartment.setText(classItem.department)
        binding.editRoomNumber.setText(classItem.roomNumber)
        binding.checkboxRecurring.isChecked = classItem.isRecurring

        // Set reminder minutes
        selectedReminderMinutes = 15

        // Debug logging for loading existing data
        val timeZone = java.util.TimeZone.getDefault()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.getDefault())
        dateFormat.timeZone = timeZone
        
        android.util.Log.d("AddEditClassActivity", "=== LOADING EXISTING CLASS DATA ===")
        android.util.Log.d("AddEditClassActivity", "Subject: ${classItem.subject}")
        android.util.Log.d("AddEditClassActivity", "Device timezone: ${timeZone.displayName} (${timeZone.id})")
        android.util.Log.d("AddEditClassActivity", "DB Start date: ${dateFormat.format(classItem.startDate)}")
        android.util.Log.d("AddEditClassActivity", "DB Start time: ${dateFormat.format(classItem.startTime)}")
        android.util.Log.d("AddEditClassActivity", "DB End time: ${dateFormat.format(classItem.endTime)}")

        // Set dates and times
        startDate.time = classItem.startDate
        endDate.time = classItem.endDate
        startTime.time = classItem.startTime
        endTime.time = classItem.endTime

        android.util.Log.d("AddEditClassActivity", "After setting Calendar objects:")
        android.util.Log.d("AddEditClassActivity", "Calendar start date: ${dateFormat.format(startDate.time)}")
        android.util.Log.d("AddEditClassActivity", "Calendar start time: ${dateFormat.format(startTime.time)}")
        android.util.Log.d("AddEditClassActivity", "Calendar end time: ${dateFormat.format(endTime.time)}")

        updateDateTimeDisplays()
    }

    private fun setDefaultDateTime() {
        val now = Calendar.getInstance()
        startDate.time = now.time
        endDate.time = now.time
        startTime.time = now.time
        endTime.add(Calendar.HOUR_OF_DAY, 1)

        updateDateTimeDisplays()
    }

    private fun setupDateTimePickers() {
        // Date Picker
        binding.buttonDate.setOnClickListener {
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    startDate.set(year, month, day)
                    endDate.set(year, month, day)
                    
                    // FIXED: Update time objects with the new date
                    val currentStartHour = startTime.get(Calendar.HOUR_OF_DAY)
                    val currentStartMinute = startTime.get(Calendar.MINUTE)
                    val currentEndHour = endTime.get(Calendar.HOUR_OF_DAY)
                    val currentEndMinute = endTime.get(Calendar.MINUTE)
                    
                    startTime.set(year, month, day, currentStartHour, currentStartMinute)
                    startTime.set(Calendar.SECOND, 0)
                    startTime.set(Calendar.MILLISECOND, 0)
                    
                    endTime.set(year, month, day, currentEndHour, currentEndMinute)
                    endTime.set(Calendar.SECOND, 0)
                    endTime.set(Calendar.MILLISECOND, 0)
                    
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
                    // COMPLETE FIX: Create new Calendar instance to avoid timezone issues
                    val timeZone = java.util.TimeZone.getDefault()
                    startTime = Calendar.getInstance(timeZone).apply {
                        set(Calendar.YEAR, startDate.get(Calendar.YEAR))
                        set(Calendar.MONTH, startDate.get(Calendar.MONTH))
                        set(Calendar.DAY_OF_MONTH, startDate.get(Calendar.DAY_OF_MONTH))
                        set(Calendar.HOUR_OF_DAY, hour)
                        set(Calendar.MINUTE, minute)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    
                    // Debug log the time picker values
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.getDefault())
                    android.util.Log.d("TimePicker", "=== TIME PICKER CALLBACK ===")
                    android.util.Log.d("TimePicker", "Selected hour: $hour, minute: $minute")
                    android.util.Log.d("TimePicker", "Device timezone: ${timeZone.displayName}")
                    android.util.Log.d("TimePicker", "Created startTime: ${dateFormat.format(startTime.time)}")
                    android.util.Log.d("TimePicker", "StartTime milliseconds: ${startTime.timeInMillis}")
                    
                    updateDateTimeDisplays()
                },
                startTime.get(Calendar.HOUR_OF_DAY),
                startTime.get(Calendar.MINUTE),
                false
            ).show()
        }

        // End Time Picker
        binding.buttonEndTime.setOnClickListener {
            TimePickerDialog(
                this,
                { _, hour, minute ->
                    // COMPLETE FIX: Create new Calendar instance to avoid timezone issues
                    val timeZone = java.util.TimeZone.getDefault()
                    endTime = Calendar.getInstance(timeZone).apply {
                        set(Calendar.YEAR, startDate.get(Calendar.YEAR))
                        set(Calendar.MONTH, startDate.get(Calendar.MONTH))
                        set(Calendar.DAY_OF_MONTH, startDate.get(Calendar.DAY_OF_MONTH))
                        set(Calendar.HOUR_OF_DAY, hour)
                        set(Calendar.MINUTE, minute)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    
                    // Debug log the time picker values
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.getDefault())
                    android.util.Log.d("TimePicker", "=== END TIME PICKER CALLBACK ===")
                    android.util.Log.d("TimePicker", "Selected end hour: $hour, minute: $minute")
                    android.util.Log.d("TimePicker", "Device timezone: ${timeZone.displayName}")
                    android.util.Log.d("TimePicker", "Created endTime: ${dateFormat.format(endTime.time)}")
                    android.util.Log.d("TimePicker", "EndTime milliseconds: ${endTime.timeInMillis}")
                    
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

        binding.buttonDate.text = dateFormat.format(startDate.time).uppercase()
        binding.buttonStartTime.text = timeFormat.format(startTime.time).uppercase()
        binding.buttonEndTime.text = timeFormat.format(endTime.time).uppercase()
    }

    private fun setupButtons() {
        binding.buttonSave.setOnClickListener {
            saveClass()
        }

        binding.buttonCancel.setOnClickListener {
            finish()
        }

        binding.buttonDelete.setOnClickListener {
            showDeleteConfirmation()
        }
    }

    private fun saveClass() {
        val subject = binding.editSubject.text.toString().trim()
        val department = binding.editDepartment.text.toString().trim()
        val roomNumber = binding.editRoomNumber.text.toString().trim()

        if (subject.isEmpty() || department.isEmpty() || roomNumber.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
            return
        }



        val classItem = Class(
            id = editingClass?.id ?: 0,

            subject = subject,
            department = department,
            roomNumber = roomNumber,
            startDate = startDate.time,
            endDate = endDate.time,
            startTime = startTime.time,
            endTime = endTime.time,
            isRecurring = binding.checkboxRecurring.isChecked,
            reminderMinutes = selectedReminderMinutes
        )

        lifecycleScope.launch {
            try {
                // Debug logging for time values
                val timeZone = java.util.TimeZone.getDefault()
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.getDefault())
                dateFormat.timeZone = timeZone
                
                android.util.Log.d("AddEditClassActivity", "=== SAVING CLASS TIME VALUES ===")
                android.util.Log.d("AddEditClassActivity", "Subject: $subject")
                android.util.Log.d("AddEditClassActivity", "Device timezone: ${timeZone.displayName} (${timeZone.id})")
                android.util.Log.d("AddEditClassActivity", "Start date: ${dateFormat.format(startDate.time)}")
                android.util.Log.d("AddEditClassActivity", "Start time: ${dateFormat.format(startTime.time)}")
                android.util.Log.d("AddEditClassActivity", "End time: ${dateFormat.format(endTime.time)}")
                android.util.Log.d("AddEditClassActivity", "Calendar start date milliseconds: ${startDate.timeInMillis}")
                android.util.Log.d("AddEditClassActivity", "Calendar start time milliseconds: ${startTime.timeInMillis}")
                
                if (editingClass != null) {
                    repository.updateClass(classItem)
                    Toast.makeText(this@AddEditClassActivity, "Class updated successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    repository.insertClass(classItem)
                    Toast.makeText(this@AddEditClassActivity, "Class added successfully!", Toast.LENGTH_SHORT).show()
                }

                // Schedule notification if notifications are enabled
                if (classItem.notificationsEnabled) {
                    notificationHelper.scheduleClassNotifications(classItem)
                }

                setResult(RESULT_OK)
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@AddEditClassActivity, "Error saving class: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDeleteConfirmation() {
        editingClass?.let { classItem ->
            AlertDialog.Builder(this)
                .setTitle("Delete Class")
                .setMessage("Are you sure you want to delete ${classItem.subject}?")
                .setPositiveButton("Delete") { _, _ ->
                    lifecycleScope.launch {
                        repository.deleteClass(classItem)
                        Toast.makeText(this@AddEditClassActivity, "Class deleted", Toast.LENGTH_SHORT).show()
                        setResult(RESULT_OK)
                        finish()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }
}