package com.example.teacherscheduler.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.teacherscheduler.MainActivity
import com.example.teacherscheduler.R
import com.example.teacherscheduler.data.Repository
import com.example.teacherscheduler.databinding.FragmentDashboardBinding
import com.example.teacherscheduler.ui.adapter.ClassAdapter
import com.example.teacherscheduler.ui.adapter.MeetingAdapter
import com.example.teacherscheduler.viewmodel.DashboardViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class EnhancedDashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var dashboardViewModel: DashboardViewModel
    private lateinit var repository: Repository
    private lateinit var classAdapter: ClassAdapter
    private lateinit var meetingAdapter: MeetingAdapter
    
    private val dateFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    
    private val refreshHandler = Handler(Looper.getMainLooper())
    private val refreshRunnable = object : Runnable {
        override fun run() {
            loadDashboardData()
            // Refresh every 30 seconds
            refreshHandler.postDelayed(this, 30000)
        }
    }
    
    private val refreshReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.example.teacherscheduler.REFRESH_UI") {
                // Refresh the dashboard data when an item is marked as done
                loadDashboardData()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        repository = Repository(requireContext())
        dashboardViewModel = ViewModelProvider(this)[DashboardViewModel::class.java]
        
        setupScrolling()
        setupViews()
        setupRecyclerViews()
        setupClickListeners()
        setupCalendar()
        loadDashboardData()
        updateWelcomeMessage()
    }
    
    private fun setupScrolling() {
        // Enable nested scrolling programmatically (safer than XML)
        binding.root.isNestedScrollingEnabled = true
    }

    private fun setupViews() {
        // Set current date
        binding.textViewDate.text = dateFormat.format(Date())
    }

    private fun setupRecyclerViews() {
        // Setup Classes RecyclerView
        classAdapter = ClassAdapter(
            onEditClick = { classItem ->
                val intent = ModernAddEditClassActivity.newIntent(requireContext(), classItem.id)
                startActivity(intent)
            },
            onDeleteClick = { classItem ->
                deleteClass(classItem.id)
            }
        )
        
        binding.recyclerViewClasses.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = classAdapter
            isNestedScrollingEnabled = false
        }

        // Setup Meetings RecyclerView
        meetingAdapter = MeetingAdapter(
            onEditClick = { meeting ->
                val intent = Intent(requireContext(), ModernAddEditMeetingActivity::class.java)
                intent.putExtra(ModernAddEditMeetingActivity.EXTRA_MEETING_ID, meeting.id)
                startActivity(intent)
            },
            onDeleteClick = { meeting ->
                deleteMeeting(meeting.id)
            }
        )
        
        binding.recyclerViewMeetings.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = meetingAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun setupClickListeners() {
        // Quick action buttons
        binding.buttonAddClass.setOnClickListener {
            val intent = Intent(requireContext(), ModernAddEditClassActivity::class.java)
            startActivity(intent)
        }
        
        binding.buttonAddMeeting.setOnClickListener {
            val intent = Intent(requireContext(), ModernAddEditMeetingActivity::class.java)
            startActivity(intent)
        }
        
        // Statistics cards click listeners
        val todayClassesCard = binding.statsContainer.getChildAt(0)
        todayClassesCard.setOnClickListener {
            // Navigate to classes tab
            (activity as? MainActivity)?.switchToTab(1)
        }
        
        val upcomingMeetingsCard = binding.statsContainer.getChildAt(1)
        upcomingMeetingsCard.setOnClickListener {
            // Navigate to meetings tab
            (activity as? MainActivity)?.switchToTab(2)
        }
    }

    private fun setupCalendar() {
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = Calendar.getInstance()
            selectedDate.set(year, month, dayOfMonth)
            loadDataForDate(selectedDate.timeInMillis)
        }
    }

    private fun updateWelcomeMessage() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        
        val greeting = when (hour) {
            in 5..11 -> "Good Morning!"
            in 12..16 -> "Good Afternoon!"
            in 17..20 -> "Good Evening!"
            else -> "Good Night!"
        }
        
        binding.textViewWelcome.text = greeting
    }

    private fun loadDashboardData() {
        lifecycleScope.launch {
            try {
                // Update status of completed events first
                updateCompletedEvents()
                
                // Load today's data
                val today = Calendar.getInstance()
                loadDataForDate(today.timeInMillis)
                
                // Load statistics
                loadStatistics()
                
            } catch (e: Exception) {
                // Handle error
                showError("Error loading dashboard data: ${e.message}")
            }
        }
    }

    private fun loadDataForDate(dateInMillis: Long) {
        lifecycleScope.launch {
            try {
                // Get start and end of the selected day
                val startOfDay = Calendar.getInstance().apply {
                    timeInMillis = dateInMillis
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                
                val endOfDay = Calendar.getInstance().apply {
                    timeInMillis = dateInMillis
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }
                
                // Load classes for the selected date
                val classes = repository.getClassesForDateRange(
                    startOfDay.timeInMillis, 
                    endOfDay.timeInMillis
                )
                
                // Load meetings for the selected date
                val meetings = repository.getMeetingsForDateRange(
                    startOfDay.timeInMillis, 
                    endOfDay.timeInMillis
                )
                
                // Update UI
                classAdapter.submitList(classes)
                meetingAdapter.submitList(meetings)
                
                // Show/hide empty states
                binding.textViewNoClasses.visibility = if (classes.isEmpty()) View.VISIBLE else View.GONE
                binding.recyclerViewClasses.visibility = if (classes.isEmpty()) View.GONE else View.VISIBLE
                
                binding.textViewNoMeetings.visibility = if (meetings.isEmpty()) View.VISIBLE else View.GONE
                binding.recyclerViewMeetings.visibility = if (meetings.isEmpty()) View.GONE else View.VISIBLE
                
            } catch (e: Exception) {
                showError("Error loading data for selected date: ${e.message}")
            }
        }
    }

    private fun loadStatistics() {
        lifecycleScope.launch {
            try {
                // Get today's date range
                val today = Calendar.getInstance()
                val startOfDay = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                
                val endOfDay = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }
                
                // Count today's classes (filter for classes happening today)
                val allTodayClasses = repository.getClassesForDateRange(
                    startOfDay.timeInMillis, 
                    endOfDay.timeInMillis
                )
                
                // Filter classes that are actually scheduled for today
                val todayClasses = allTodayClasses.filter { classItem ->
                    if (classItem.isRecurring) {
                        // For recurring classes, check if today matches the days of week
                        val todayDayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
                        classItem.daysOfWeek.contains(todayDayOfWeek)
                    } else {
                        // For non-recurring classes, check if the date matches
                        val classDate = Calendar.getInstance().apply { timeInMillis = classItem.startDate.time }
                        val today = Calendar.getInstance()
                        classDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                        classDate.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
                    }
                }
                
                // Count upcoming meetings (from now onwards, next 7 days)
                val now = System.currentTimeMillis()
                val nextWeek = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, 7)
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }
                
                // Get all meetings in the next 7 days and filter for upcoming ones
                val allMeetings = repository.getMeetingsForDateRange(
                    now,
                    nextWeek.timeInMillis
                )
                
                // Filter meetings that haven't started yet
                val upcomingMeetings = allMeetings.filter { meeting ->
                    meeting.getStartDateTime() > now
                }
                
                // Update statistics cards
                binding.todayClassesCount.text = todayClasses.size.toString()
                binding.upcomingMeetingsCount.text = upcomingMeetings.size.toString()
                
            } catch (e: Exception) {
                showError("Error loading statistics: ${e.message}")
            }
        }
    }

    private fun deleteClass(classId: Long) {
        lifecycleScope.launch {
            try {
                val classItem = repository.getClassById(classId)
                if (classItem != null) {
                    repository.deleteClass(classItem)
                    loadDashboardData() // Refresh data
                } else {
                    showError("Class not found")
                }
            } catch (e: Exception) {
                showError("Error deleting class: ${e.message}")
            }
        }
    }

    private fun deleteMeeting(meetingId: Long) {
        lifecycleScope.launch {
            try {
                val meeting = repository.getMeetingById(meetingId)
                repository.deleteMeeting(meeting)
                loadDashboardData() // Refresh data
            } catch (e: Exception) {
                showError("Error deleting meeting: ${e.message}")
            }
        }
    }

    private fun updateCompletedEvents() {
        lifecycleScope.launch {
            try {
                val now = System.currentTimeMillis()
                
                // Get all active classes and check if they've ended
                val activeClasses = repository.getAllActiveClassesSync()
                for (classItem in activeClasses) {
                    if (classItem.getEndDateTime() < now) {
                        // Class has ended, mark as inactive
                        val updatedClass = classItem.copy(isActive = false)
                        repository.updateClass(updatedClass)
                    }
                }
                
                // Get all active meetings and check if they've ended
                val activeMeetings = repository.getAllActiveMeetingsSync()
                for (meeting in activeMeetings) {
                    if (meeting.getEndDateTime() < now) {
                        // Meeting has ended, mark as inactive
                        val updatedMeeting = meeting.copy(isActive = false)
                        repository.updateMeeting(updatedMeeting)
                    }
                }
                
            } catch (e: Exception) {
                android.util.Log.e("DashboardFragment", "Error updating completed events: ${e.message}")
            }
        }
    }

    private fun showError(message: String) {
        // You can implement a proper error handling mechanism here
        // For now, we'll just log it or show a toast
        android.util.Log.e("DashboardFragment", message)
    }

    override fun onResume() {
        super.onResume()
        // Refresh data when returning to the fragment
        loadDashboardData()
        // Start auto-refresh
        refreshHandler.post(refreshRunnable)
        
        // Register broadcast receiver for completion updates
        val filter = IntentFilter("com.example.teacherscheduler.REFRESH_UI")
        requireContext().registerReceiver(refreshReceiver, filter)
    }
    
    override fun onPause() {
        super.onPause()
        // Stop auto-refresh when fragment is not visible
        refreshHandler.removeCallbacks(refreshRunnable)
        
        // Unregister broadcast receiver
        try {
            requireContext().unregisterReceiver(refreshReceiver)
        } catch (e: IllegalArgumentException) {
            // Receiver was not registered, ignore
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Clean up auto-refresh
        refreshHandler.removeCallbacks(refreshRunnable)
        _binding = null
    }
}