package com.example.teacherscheduler.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.teacherscheduler.R
import com.example.teacherscheduler.viewmodel.DashboardViewModel
import com.example.teacherscheduler.viewmodel.SharedViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DashboardFragment : Fragment() {

    private lateinit var dashboardViewModel: DashboardViewModel
    private lateinit var sharedViewModel: SharedViewModel
    
    private lateinit var textViewDate: TextView
    private lateinit var calendarView: CalendarView
    private lateinit var recyclerViewClasses: RecyclerView
    private lateinit var recyclerViewMeetings: RecyclerView
    private lateinit var textViewNoClasses: TextView
    private lateinit var textViewNoMeetings: TextView
    
    private val dateFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_dashboard, container, false)
        
        // Initialize views
        textViewDate = root.findViewById(R.id.textViewDate)
        calendarView = root.findViewById(R.id.calendarView)
        recyclerViewClasses = root.findViewById(R.id.recyclerViewClasses)
        recyclerViewMeetings = root.findViewById(R.id.recyclerViewMeetings)
        textViewNoClasses = root.findViewById(R.id.textViewNoClasses)
        textViewNoMeetings = root.findViewById(R.id.textViewNoMeetings)
        
        // Set up RecyclerViews
        recyclerViewClasses.layoutManager = LinearLayoutManager(context)
        recyclerViewMeetings.layoutManager = LinearLayoutManager(context)
        
        // Set up calendar date change listener
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)
            dashboardViewModel.setSelectedDate(calendar.time)
        }
        
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize ViewModels
        dashboardViewModel = ViewModelProvider(this)[DashboardViewModel::class.java]
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
        
        // Set FAB visibility and action
        sharedViewModel.setFabVisible(true)
        sharedViewModel.setFabAction(SharedViewModel.FabAction.ADD_CLASS)
        
        // Observe selected date
        dashboardViewModel.selectedDate.observe(viewLifecycleOwner) { date ->
            updateDateDisplay(date)
        }
        
        // Observe classes for selected date
        dashboardViewModel.classesForSelectedDate.observe(viewLifecycleOwner) { classes ->
            // Update classes adapter
            if (classes.isEmpty()) {
                textViewNoClasses.visibility = View.VISIBLE
                recyclerViewClasses.visibility = View.GONE
            } else {
                textViewNoClasses.visibility = View.GONE
                recyclerViewClasses.visibility = View.VISIBLE
                // Set adapter for recyclerViewClasses
                recyclerViewClasses.adapter = com.example.teacherscheduler.ui.adapter.ClassAdapter(
                    onEditClick = { classItem ->
                        // Handle class click
                        sharedViewModel.navigateToClassDetail(classItem.id)
                    },
                    onDeleteClick = { classItem ->
                        // Handle delete click - you can implement this later if needed
                        dashboardViewModel.deleteClass(classItem)
                    }
                ).apply {
                    submitList(classes)
                }
            }
        }
        
        // Observe meetings for selected date
        dashboardViewModel.meetingsForSelectedDate.observe(viewLifecycleOwner) { meetings ->
            // Update meetings adapter
            if (meetings.isEmpty()) {
                textViewNoMeetings.visibility = View.VISIBLE
                recyclerViewMeetings.visibility = View.GONE
            } else {
                textViewNoMeetings.visibility = View.GONE
                recyclerViewMeetings.visibility = View.VISIBLE
                // Set adapter for recyclerViewMeetings
                recyclerViewMeetings.adapter = com.example.teacherscheduler.ui.adapter.MeetingAdapter(
                    onEditClick = { meeting ->
                        // Handle meeting click
                        sharedViewModel.navigateToMeetingDetail(meeting.id)
                    },
                    onDeleteClick = { meeting ->
                        // Handle delete click - you can implement this later if needed
                        dashboardViewModel.deleteMeeting(meeting)
                    }
                ).apply {
                    submitList(meetings)
                }
            }
        }
    }
    
    private fun updateDateDisplay(date: Date) {
        val today = dashboardViewModel.getTodayDate()
        val tomorrow = dashboardViewModel.getTomorrowDate()
        
        val calendar1 = Calendar.getInstance()
        calendar1.time = date
        
        val calendar2 = Calendar.getInstance()
        calendar2.time = today
        
        val calendar3 = Calendar.getInstance()
        calendar3.time = tomorrow
        
        val isToday = calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR) &&
                calendar1.get(Calendar.DAY_OF_YEAR) == calendar2.get(Calendar.DAY_OF_YEAR)
        
        val isTomorrow = calendar1.get(Calendar.YEAR) == calendar3.get(Calendar.YEAR) &&
                calendar1.get(Calendar.DAY_OF_YEAR) == calendar3.get(Calendar.DAY_OF_YEAR)
        
        textViewDate.text = when {
            isToday -> getString(R.string.today)
            isTomorrow -> getString(R.string.tomorrow)
            else -> dateFormat.format(date)
        }
    }
}