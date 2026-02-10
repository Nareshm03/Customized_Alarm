package com.example.teacherscheduler.ui.calendar

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.teacherscheduler.R
import com.example.teacherscheduler.databinding.ItemWeekEventBinding
import com.example.teacherscheduler.model.Class
import com.example.teacherscheduler.model.Meeting
import java.text.SimpleDateFormat
import java.util.*

/**
 * Week grid view similar to Google Calendar
 */
class WeekGridView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private lateinit var weekDaysContainer: LinearLayout
    private lateinit var eventsContainer: RecyclerView
    private lateinit var previousButton: View
    private lateinit var nextButton: View
    private lateinit var weekRangeText: TextView

    private val calendar = Calendar.getInstance()
    private val weekEvents = mutableListOf<WeekEvent>()
    private val eventsAdapter = WeekEventsAdapter()

    private val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("d", Locale.getDefault())
    private val weekRangeFormat = SimpleDateFormat("MMM d", Locale.getDefault())

    var onEventClickListener: ((Any) -> Unit)? = null

    init {
        orientation = VERTICAL
        LayoutInflater.from(context).inflate(R.layout.view_week_grid, this, true)

        weekDaysContainer = findViewById(R.id.weekDaysContainer)
        eventsContainer = findViewById(R.id.eventsRecyclerView)
        previousButton = findViewById(R.id.btnPreviousWeek)
        nextButton = findViewById(R.id.btnNextWeek)
        weekRangeText = findViewById(R.id.textWeekRange)

        setupWeekDays()
        setupRecyclerView()
        setupListeners()
        updateWeek()
    }

    private fun setupWeekDays() {
        val startOfWeek = getStartOfWeek(calendar)

        for (i in 0..6) {
            val dayCal = startOfWeek.clone() as Calendar
            dayCal.add(Calendar.DAY_OF_MONTH, i)

            val dayView = LayoutInflater.from(context).inflate(R.layout.item_week_day_header, weekDaysContainer, false)
            val dayNameText = dayView.findViewById<TextView>(R.id.textDayName)
            val dayDateText = dayView.findViewById<TextView>(R.id.textDayDate)

            dayNameText.text = dayFormat.format(dayCal.time)
            dayDateText.text = dateFormat.format(dayCal.time)

            // Highlight today
            if (isToday(dayCal)) {
                dayView.setBackgroundResource(R.drawable.week_day_today_background)
            }

            weekDaysContainer.addView(dayView)
        }
    }

    private fun setupRecyclerView() {
        eventsContainer.layoutManager = LinearLayoutManager(context)
        eventsContainer.adapter = eventsAdapter
        eventsAdapter.onEventClickListener = onEventClickListener
    }

    private fun setupListeners() {
        previousButton.setOnClickListener {
            calendar.add(Calendar.WEEK_OF_YEAR, -1)
            updateWeek()
        }

        nextButton.setOnClickListener {
            calendar.add(Calendar.WEEK_OF_YEAR, 1)
            updateWeek()
        }
    }

    fun setEvents(classes: List<Class>, meetings: List<Meeting>) {
        weekEvents.clear()

        val startOfWeek = getStartOfWeek(calendar)
        val endOfWeek = startOfWeek.clone() as Calendar
        endOfWeek.add(Calendar.DAY_OF_MONTH, 7)

        // Add classes
        classes.forEach { classItem ->
            if (classItem.isRecurring) {
                // Add recurring classes for each day they occur in this week
                for (i in 0..6) {
                    val dayCal = startOfWeek.clone() as Calendar
                    dayCal.add(Calendar.DAY_OF_MONTH, i)

                    if (classItem.daysOfWeek.contains(dayCal.get(Calendar.DAY_OF_WEEK))) {
                        weekEvents.add(WeekEvent(
                            title = classItem.subject,
                            time = formatTimeRange(classItem.startTime, classItem.endTime),
                            location = "Room ${classItem.roomNumber}",
                            date = dayCal.clone() as Calendar,
                            isClass = true,
                            originalObject = classItem
                        ))
                    }
                }
            } else {
                val classDate = Calendar.getInstance().apply { time = classItem.startDate }
                if (isInWeek(classDate, startOfWeek, endOfWeek)) {
                    weekEvents.add(WeekEvent(
                        title = classItem.subject,
                        time = formatTimeRange(classItem.startTime, classItem.endTime),
                        location = "Room ${classItem.roomNumber}",
                        date = classDate,
                        isClass = true,
                        originalObject = classItem
                    ))
                }
            }
        }

        // Add meetings
        meetings.forEach { meeting ->
            val meetingDate = Calendar.getInstance().apply { time = meeting.startDate }
            if (isInWeek(meetingDate, startOfWeek, endOfWeek)) {
                weekEvents.add(WeekEvent(
                    title = meeting.title,
                    time = formatTimeRange(meeting.startTime, meeting.endTime),
                    location = meeting.location,
                    date = meetingDate,
                    isClass = false,
                    originalObject = meeting
                ))
            }
        }

        // Sort by date and time
        weekEvents.sortBy { it.date.timeInMillis }

        // Group by day
        val groupedEvents = groupEventsByDay()
        eventsAdapter.setEvents(groupedEvents)
    }

    private fun groupEventsByDay(): List<DayEvents> {
        val grouped = mutableListOf<DayEvents>()
        val startOfWeek = getStartOfWeek(calendar)

        for (i in 0..6) {
            val dayCal = startOfWeek.clone() as Calendar
            dayCal.add(Calendar.DAY_OF_MONTH, i)

            val dayEvents = weekEvents.filter { event ->
                isSameDay(event.date, dayCal)
            }

            if (dayEvents.isNotEmpty()) {
                grouped.add(DayEvents(dayCal, dayEvents))
            }
        }

        return grouped
    }

    private fun updateWeek() {
        val startOfWeek = getStartOfWeek(calendar)
        val endOfWeek = startOfWeek.clone() as Calendar
        endOfWeek.add(Calendar.DAY_OF_MONTH, 6)

        weekRangeText.text = "${weekRangeFormat.format(startOfWeek.time)} - ${weekRangeFormat.format(endOfWeek.time)}"

        // Update day headers
        weekDaysContainer.removeAllViews()
        setupWeekDays()
    }

    private fun getStartOfWeek(cal: Calendar): Calendar {
        val start = cal.clone() as Calendar
        start.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        start.set(Calendar.HOUR_OF_DAY, 0)
        start.set(Calendar.MINUTE, 0)
        start.set(Calendar.SECOND, 0)
        start.set(Calendar.MILLISECOND, 0)
        return start
    }

    private fun isToday(cal: Calendar): Boolean {
        val today = Calendar.getInstance()
        return cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
               cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun isInWeek(date: Calendar, startOfWeek: Calendar, endOfWeek: Calendar): Boolean {
        return date.timeInMillis >= startOfWeek.timeInMillis && date.timeInMillis < endOfWeek.timeInMillis
    }

    private fun formatTimeRange(startTime: Date, endTime: Date): String {
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        return "${timeFormat.format(startTime)} - ${timeFormat.format(endTime)}"
    }

    data class WeekEvent(
        val title: String,
        val time: String,
        val location: String,
        val date: Calendar,
        val isClass: Boolean,
        val originalObject: Any
    )

    data class DayEvents(
        val date: Calendar,
        val events: List<WeekEvent>
    )

    inner class WeekEventsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private var dayEvents = listOf<DayEvents>()
        var onEventClickListener: ((Any) -> Unit)? = null

        fun setEvents(events: List<DayEvents>) {
            dayEvents = events
            notifyDataSetChanged()
        }

        override fun getItemViewType(position: Int): Int {
            // Calculate if this is a day header or event
            var currentPos = 0
            dayEvents.forEach { day ->
                if (currentPos == position) return VIEW_TYPE_DAY_HEADER
                currentPos++
                if (currentPos + day.events.size > position) return VIEW_TYPE_EVENT
                currentPos += day.events.size
            }
            return VIEW_TYPE_EVENT
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return when (viewType) {
                VIEW_TYPE_DAY_HEADER -> {
                    val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_week_day_section, parent, false)
                    DayHeaderViewHolder(view)
                }
                else -> {
                    val binding = ItemWeekEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                    EventViewHolder(binding)
                }
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (holder) {
                is DayHeaderViewHolder -> {
                    val dayIndex = getDayIndexForPosition(position)
                    holder.bind(dayEvents[dayIndex])
                }
                is EventViewHolder -> {
                    val event = getEventForPosition(position)
                    holder.bind(event)
                }
            }
        }

        override fun getItemCount(): Int {
            return dayEvents.sumOf { 1 + it.events.size } // 1 for header + events
        }

        private fun getDayIndexForPosition(position: Int): Int {
            var currentPos = 0
            dayEvents.forEachIndexed { index, day ->
                if (currentPos == position) return index
                currentPos += 1 + day.events.size
            }
            return 0
        }

        private fun getEventForPosition(position: Int): WeekEvent {
            var currentPos = 0
            dayEvents.forEach { day ->
                currentPos++ // Skip day header
                day.events.forEachIndexed { index, event ->
                    if (currentPos == position) return event
                    currentPos++
                }
            }
            return dayEvents[0].events[0] // Fallback
        }

        inner class DayHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val dayText: TextView = itemView.findViewById(R.id.textDayHeader)

            fun bind(dayEvents: DayEvents) {
                val format = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())
                dayText.text = format.format(dayEvents.date.time)
            }
        }

        inner class EventViewHolder(private val binding: ItemWeekEventBinding) : RecyclerView.ViewHolder(binding.root) {
            fun bind(event: WeekEvent) {
                binding.textEventTitle.text = event.title
                binding.textEventTime.text = event.time
                binding.textEventLocation.text = event.location

                // Color coding
                val colorRes = if (event.isClass) R.color.class_color_primary else R.color.meeting_color_primary
                binding.eventIndicator.setBackgroundColor(ContextCompat.getColor(context, colorRes))

                binding.root.setOnClickListener {
                    onEventClickListener?.invoke(event.originalObject)
                }
            }
        }
    }

    companion object {
        const val VIEW_TYPE_DAY_HEADER = 0
        const val VIEW_TYPE_EVENT = 1
    }
}
