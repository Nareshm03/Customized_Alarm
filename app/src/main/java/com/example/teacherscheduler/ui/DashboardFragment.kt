package com.example.teacherscheduler.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.teacherscheduler.MainActivity
import com.example.teacherscheduler.R
import com.example.teacherscheduler.ui.calendar.EnhancedCalendarView
import com.example.teacherscheduler.util.fadeIn
import com.example.teacherscheduler.viewmodel.DashboardViewModel
import com.example.teacherscheduler.viewmodel.SharedViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DashboardFragment : Fragment() {

    private lateinit var dashboardViewModel: DashboardViewModel
    private lateinit var sharedViewModel: SharedViewModel
    
    private lateinit var textViewDate: TextView
    private lateinit var enhancedCalendarView: EnhancedCalendarView
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
        enhancedCalendarView = root.findViewById(R.id.enhancedCalendarView)
        recyclerViewClasses = root.findViewById(R.id.recyclerViewClasses)
        recyclerViewMeetings = root.findViewById(R.id.recyclerViewMeetings)
        textViewNoClasses = root.findViewById(R.id.textViewNoClasses)
        textViewNoMeetings = root.findViewById(R.id.textViewNoMeetings)
        
        // Set up RecyclerViews
        recyclerViewClasses.layoutManager = LinearLayoutManager(context)
        recyclerViewMeetings.layoutManager = LinearLayoutManager(context)
        
        // Set up calendar date change listener
        enhancedCalendarView.setOnDateSelectedListener { calendar ->
            dashboardViewModel.setSelectedDate(calendar.time)
        }
        
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        view.fadeIn()
        
        // Initialize ViewModels
        dashboardViewModel = ViewModelProvider(this)[DashboardViewModel::class.java]
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
        
        // Set FAB visibility and action
        sharedViewModel.setFabVisible(true)
        sharedViewModel.setFabAction(SharedViewModel.FabAction.ADD_CLASS)
        
        // Observe selected date
        lifecycleScope.launch {
            dashboardViewModel.selectedDate.collect { date ->
                updateDateDisplay(date)
            }
        }
        
        // Observe classes and meetings
        lifecycleScope.launch {
            dashboardViewModel.dashboardState.collect { state ->
                // Update classes
                val classes = state.todayClasses
                if (classes.isEmpty()) {
                    textViewNoClasses.visibility = View.VISIBLE
                    recyclerViewClasses.visibility = View.GONE
                } else {
                    textViewNoClasses.visibility = View.GONE
                    recyclerViewClasses.visibility = View.VISIBLE
                    recyclerViewClasses.adapter = com.example.teacherscheduler.ui.adapter.ClassAdapter(
                        onEditClick = { classItem ->
                            sharedViewModel.navigateToClassDetail(classItem.id)
                        },
                        onDeleteClick = { classItem ->
                            dashboardViewModel.deleteClass(classItem)
                        }
                    ).apply {
                        submitList(classes)
                    }
                }
                
                // Update meetings
                val meetings = state.upcomingMeetings
                if (meetings.isEmpty()) {
                    textViewNoMeetings.visibility = View.VISIBLE
                    recyclerViewMeetings.visibility = View.GONE
                } else {
                    textViewNoMeetings.visibility = View.GONE
                    recyclerViewMeetings.visibility = View.VISIBLE
                    recyclerViewMeetings.adapter = com.example.teacherscheduler.ui.adapter.MeetingAdapter(
                        onEditClick = { meeting ->
                            sharedViewModel.navigateToMeetingDetail(meeting.id)
                        },
                        onDeleteClick = { meeting ->
                            dashboardViewModel.deleteMeeting(meeting)
                        }
                    ).apply {
                        submitList(meetings)
                    }
                }
            }
        }
    }
    
    private fun updateDateDisplay(date: Date) {
        val today = Date()
        val tomorrow = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
        }.time
        
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
