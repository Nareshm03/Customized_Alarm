package com.example.teacherscheduler.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.teacherscheduler.R
import com.example.teacherscheduler.databinding.FragmentTimetableBinding
import com.example.teacherscheduler.databinding.ItemTimetableEventBinding
import com.example.teacherscheduler.viewmodel.EventType
import com.example.teacherscheduler.viewmodel.TimetableEvent
import com.example.teacherscheduler.viewmodel.TimetableViewModel
import com.example.teacherscheduler.util.GoogleCalendarSync
import com.example.teacherscheduler.model.ClassItem
import com.example.teacherscheduler.model.MeetingItem
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class TimetableFragment : Fragment() {
    private var _binding: FragmentTimetableBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TimetableViewModel by viewModels()
    private val hourHeight = 60 // 60dp per hour (1dp per minute)

    private var currentWeekStart = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    private val dateFormatter = SimpleDateFormat("MMM d", Locale.getDefault())
    private val monthYearFormatter = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    private val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTimetableBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupTimeAxis()
        setupGridLines()
        setupWeekNavigation()
        updateWeekDisplay()
        setupObservers()
        setupListeners()
    }

    private fun setupTimeAxis() {
        binding.timeAxis.removeAllViews()
        for (i in 0..23) {
            val textView = TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dpToPx(hourHeight)
                )
                text = String.format(Locale.getDefault(), "%02d:00", i)
                gravity = android.view.Gravity.TOP or android.view.Gravity.CENTER_HORIZONTAL
                setPadding(dpToPx(4), dpToPx(4), dpToPx(4), 0)
                textSize = 11f
                setTextColor(ContextCompat.getColor(requireContext(), R.color.timetable_time_text))
            }
            binding.timeAxis.addView(textView)
        }
    }

    private fun setupGridLines() {
        binding.gridLinesContainer.removeAllViews()
        for (hour in 0..23) {
            val lineView = View(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dpToPx(hourHeight)
                )
                setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.timetable_grid_line))
            }

            // Add top border
            val borderView = View(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1
                )
                setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.timetable_grid_line))
            }

            val container = LinearLayout(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dpToPx(hourHeight)
                )
                orientation = LinearLayout.VERTICAL
                addView(borderView)
                addView(lineView)
            }

            binding.gridLinesContainer.addView(container)
        }
    }

    private fun setupWeekNavigation() {
        binding.btnPreviousWeek.setOnClickListener {
            currentWeekStart.add(Calendar.WEEK_OF_YEAR, -1)
            updateWeekDisplay()
            viewModel.loadEventsForWeek(currentWeekStart)
        }

        binding.btnNextWeek.setOnClickListener {
            currentWeekStart.add(Calendar.WEEK_OF_YEAR, 1)
            updateWeekDisplay()
            viewModel.loadEventsForWeek(currentWeekStart)
        }

        binding.btnTodayWeek.setOnClickListener {
            currentWeekStart = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            updateWeekDisplay()
            viewModel.loadEventsForWeek(currentWeekStart)
        }
    }

    private fun updateWeekDisplay() {
        val weekEnd = currentWeekStart.clone() as Calendar
        weekEnd.add(Calendar.DAY_OF_WEEK, 6)

        val weekText = "${dateFormatter.format(currentWeekStart.time)} - ${dateFormatter.format(weekEnd.time)}"
        binding.textCurrentWeek.text = weekText
        binding.textCurrentMonth.text = monthYearFormatter.format(currentWeekStart.time)

        // Update day headers with dates
        val dayCalendar = currentWeekStart.clone() as Calendar
        binding.dateMon.text = dayCalendar.get(Calendar.DAY_OF_MONTH).toString()

        dayCalendar.add(Calendar.DAY_OF_MONTH, 1)
        binding.dateTue.text = dayCalendar.get(Calendar.DAY_OF_MONTH).toString()

        dayCalendar.add(Calendar.DAY_OF_MONTH, 1)
        binding.dateWed.text = dayCalendar.get(Calendar.DAY_OF_MONTH).toString()

        dayCalendar.add(Calendar.DAY_OF_MONTH, 1)
        binding.dateThu.text = dayCalendar.get(Calendar.DAY_OF_MONTH).toString()

        dayCalendar.add(Calendar.DAY_OF_MONTH, 1)
        binding.dateFri.text = dayCalendar.get(Calendar.DAY_OF_MONTH).toString()

        dayCalendar.add(Calendar.DAY_OF_MONTH, 1)
        binding.dateSat.text = dayCalendar.get(Calendar.DAY_OF_MONTH).toString()

        dayCalendar.add(Calendar.DAY_OF_MONTH, 1)
        binding.dateSun.text = dayCalendar.get(Calendar.DAY_OF_MONTH).toString()
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.events.collect { events ->
                renderEvents(events)
            }
        }
    }

    private fun renderEvents(events: List<TimetableEvent>) {
        binding.eventContainer.removeAllViews()
        
        val screenWidth = resources.displayMetrics.widthPixels - dpToPx(68) // 60 for time + 8 for padding
        val colWidth = screenWidth / 7

        events.forEach { event ->
            val eventBinding = ItemTimetableEventBinding.inflate(layoutInflater, binding.eventContainer, false)

            // Format time display
            val timeText = "${timeFormatter.format(event.startTime.time)} - ${timeFormatter.format(event.endTime.time)}"
            eventBinding.eventTime.text = timeText
            eventBinding.eventTitle.text = event.title
            eventBinding.eventSubtitle.text = event.subtitle

            // Show department for classes
            if (event.type == EventType.CLASS) {
                eventBinding.eventDepartment.visibility = View.VISIBLE
                val classItem = event.originalObject as? ClassItem
                eventBinding.eventDepartment.text = classItem?.department ?: ""
            } else {
                eventBinding.eventDepartment.visibility = View.GONE
            }

            // Color coding based on event type
            val indicatorColor = if (event.type == EventType.CLASS) {
                ContextCompat.getColor(requireContext(), R.color.class_color_primary)
            } else {
                ContextCompat.getColor(requireContext(), R.color.meeting_color_primary)
            }
            eventBinding.eventTypeIndicator.setBackgroundColor(indicatorColor)

            // Set card background color
            val cardColor = if (event.type == EventType.CLASS) {
                ContextCompat.getColor(requireContext(), R.color.class_color_secondary)
            } else {
                ContextCompat.getColor(requireContext(), R.color.meeting_color_secondary)
            }
            eventBinding.root.setCardBackgroundColor(cardColor)

            // Calculate position
            val params = RelativeLayout.LayoutParams(
                (colWidth * 0.95).toInt(), // Slightly smaller for better separation
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            // Calculate day position
            val dayOfWeek = event.startTime.get(Calendar.DAY_OF_WEEK)
            val dayIndex = when (dayOfWeek) {
                Calendar.MONDAY -> 0
                Calendar.TUESDAY -> 1
                Calendar.WEDNESDAY -> 2
                Calendar.THURSDAY -> 3
                Calendar.FRIDAY -> 4
                Calendar.SATURDAY -> 5
                Calendar.SUNDAY -> 6
                else -> 0
            }

            params.leftMargin = (dayIndex * colWidth) + dpToPx(2)

            // Calculate time position
            val startMinutes = event.startTime.get(Calendar.HOUR_OF_DAY) * 60 + event.startTime.get(Calendar.MINUTE)
            val endMinutes = event.endTime.get(Calendar.HOUR_OF_DAY) * 60 + event.endTime.get(Calendar.MINUTE)
            val duration = endMinutes - startMinutes

            params.topMargin = dpToPx(startMinutes)
            params.height = dpToPx(duration.coerceAtLeast(30)) // Minimum 30 minutes height

            // Click listener to view/edit event
            eventBinding.root.setOnClickListener {
                when (event.type) {
                    EventType.CLASS -> {
                        val intent = Intent(requireContext(), ModernAddEditClassActivity::class.java)
                        intent.putExtra(ModernAddEditClassActivity.EXTRA_CLASS_ID, event.id)
                        startActivity(intent)
                    }
                    EventType.MEETING -> {
                        val intent = Intent(requireContext(), ModernAddEditMeetingActivity::class.java)
                        intent.putExtra(ModernAddEditMeetingActivity.EXTRA_MEETING_ID, event.id)
                        startActivity(intent)
                    }
                }
            }

            binding.eventContainer.addView(eventBinding.root, params)
        }
    }

    private fun setupListeners() {
        binding.fabSync.setOnClickListener {
            syncToCalendar()
        }

        binding.btnGetStarted.setOnClickListener {
            // Navigate to classes tab to add first class
            // Note: Navigation disabled as this fragment is not currently used in the app
            // (activity as? MainActivity)?.switchToTab(2) // Classes tab
        }
    }

    private fun syncToCalendar() {
        val eventsList = viewModel.events.value
        if (eventsList.isEmpty()) {
            Snackbar.make(binding.root, "No events to sync", Snackbar.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            var successCount = 0
            eventsList.forEach { event ->
                val result = when (event.type) {
                    EventType.CLASS -> {
                        val classItem = event.originalObject as ClassItem
                        // Map ClassItem to com.example.teacherscheduler.model.Class entity
                        val classModel = com.example.teacherscheduler.model.Class(
                            id = classItem.id,
                            subject = classItem.subject,
                            department = classItem.department,
                            roomNumber = classItem.roomNumber,
                            startDate = classItem.startDate,
                            endDate = classItem.endDate,
                            startTime = classItem.startTime,
                            endTime = classItem.endTime,
                            isRecurring = classItem.isRecurring,
                            daysOfWeek = classItem.daysOfWeek,
                            notificationsEnabled = classItem.notificationsEnabled,
                            reminderMinutes = classItem.reminderMinutes,
                            description = classItem.description,
                            semesterId = classItem.semesterId
                        )
                        GoogleCalendarSync.syncClassToCalendar(requireContext(), classModel)
                    }
                    EventType.MEETING -> {
                        val meetingItem = event.originalObject as MeetingItem
                        // Map MeetingItem to com.example.teacherscheduler.model.Meeting entity
                        val meetingModel = com.example.teacherscheduler.model.Meeting(
                            id = meetingItem.id,
                            title = meetingItem.title,
                            withWhom = meetingItem.with,
                            location = meetingItem.location,
                            startDate = meetingItem.date,
                            endDate = meetingItem.date,
                            startTime = meetingItem.startTime,
                            endTime = meetingItem.endTime,
                            notificationsEnabled = meetingItem.notificationsEnabled,
                            reminderMinutes = meetingItem.reminderMinutes,
                            notes = meetingItem.notes,
                            semesterId = meetingItem.semesterId
                        )
                        GoogleCalendarSync.syncMeetingToCalendar(requireContext(), meetingModel)
                    }
                }
                if (result != null) successCount++
            }
            
            if (isAdded) {
                Snackbar.make(binding.root, "Synced $successCount events to Google Calendar", Snackbar.LENGTH_LONG)
                    .setAction("VIEW") {
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            data = android.net.Uri.parse("content://com.android.calendar/time")
                        }
                        startActivity(intent)
                    }
                    .show()
            }
        }
    }

    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
