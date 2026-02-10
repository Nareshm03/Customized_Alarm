package com.example.teacherscheduler.ui.calendar

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.teacherscheduler.R
import com.example.teacherscheduler.model.Class
import com.example.teacherscheduler.model.Meeting
import java.text.SimpleDateFormat
import java.util.*

/**
 * Custom calendar view with event indicators and color coding
 */
class EnhancedCalendarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private lateinit var monthYearText: TextView
    private lateinit var daysRecyclerView: RecyclerView
    private lateinit var previousButton: View
    private lateinit var nextButton: View

    private val calendar = Calendar.getInstance()
    private val daysAdapter = CalendarDaysAdapter()

    private var classes: List<Class> = emptyList()
    private var meetings: List<Meeting> = emptyList()
    private var onDateSelectedListener: ((Calendar) -> Unit)? = null

    private val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

    init {
        orientation = VERTICAL
        LayoutInflater.from(context).inflate(R.layout.view_enhanced_calendar, this, true)

        monthYearText = findViewById(R.id.textMonthYear)
        daysRecyclerView = findViewById(R.id.recyclerDays)
        previousButton = findViewById(R.id.btnPrevious)
        nextButton = findViewById(R.id.btnNext)

        setupRecyclerView()
        setupListeners()
        updateCalendar()
    }

    private fun setupRecyclerView() {
        daysRecyclerView.layoutManager = GridLayoutManager(context, 7)
        daysRecyclerView.adapter = daysAdapter
        daysAdapter.onDateClickListener = { day ->
            calendar.set(Calendar.DAY_OF_MONTH, day)
            onDateSelectedListener?.invoke(calendar.clone() as Calendar)
            updateCalendar()
        }
    }

    private fun setupListeners() {
        previousButton.setOnClickListener {
            calendar.add(Calendar.MONTH, -1)
            updateCalendar()
        }

        nextButton.setOnClickListener {
            calendar.add(Calendar.MONTH, 1)
            updateCalendar()
        }
    }

    fun setEvents(classes: List<Class>, meetings: List<Meeting>) {
        this.classes = classes
        this.meetings = meetings
        updateCalendar()
    }

    fun setOnDateSelectedListener(listener: (Calendar) -> Unit) {
        onDateSelectedListener = listener
    }

    private fun updateCalendar() {
        monthYearText.text = monthYearFormat.format(calendar.time)

        val daysInMonth = getDaysInMonth()
        daysAdapter.setDays(daysInMonth, calendar.get(Calendar.DAY_OF_MONTH))
    }

    private fun getDaysInMonth(): List<CalendarDay> {
        val days = mutableListOf<CalendarDay>()

        val tempCal = calendar.clone() as Calendar
        tempCal.set(Calendar.DAY_OF_MONTH, 1)

        val firstDayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK)
        val daysInMonth = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH)

        // Add empty cells for days before the first of the month
        val offset = if (firstDayOfWeek == Calendar.SUNDAY) 6 else firstDayOfWeek - Calendar.MONDAY
        repeat(offset) {
            days.add(CalendarDay(0, false, false, 0, 0))
        }

        // Add days of the month
        for (day in 1..daysInMonth) {
            tempCal.set(Calendar.DAY_OF_MONTH, day)
            val isToday = isToday(tempCal)
            val eventCounts = getEventCountsForDay(tempCal)

            days.add(CalendarDay(
                day = day,
                isToday = isToday,
                isSelected = day == calendar.get(Calendar.DAY_OF_MONTH),
                classCount = eventCounts.first,
                meetingCount = eventCounts.second
            ))
        }

        return days
    }

    private fun isToday(cal: Calendar): Boolean {
        val today = Calendar.getInstance()
        return cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
               cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
    }

    private fun getEventCountsForDay(cal: Calendar): Pair<Int, Int> {
        var classCount = 0
        var meetingCount = 0

        classes.forEach { classItem ->
            if (classItem.isRecurring) {
                val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
                if (classItem.daysOfWeek.contains(dayOfWeek)) {
                    classCount++
                }
            } else {
                val classDate = Calendar.getInstance().apply { time = classItem.startDate }
                if (isSameDay(cal, classDate)) {
                    classCount++
                }
            }
        }

        meetings.forEach { meeting ->
            val meetingDate = Calendar.getInstance().apply { time = meeting.startDate }
            if (isSameDay(cal, meetingDate)) {
                meetingCount++
            }
        }

        return Pair(classCount, meetingCount)
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    data class CalendarDay(
        val day: Int,
        val isToday: Boolean,
        val isSelected: Boolean,
        val classCount: Int,
        val meetingCount: Int
    )

    inner class CalendarDaysAdapter : RecyclerView.Adapter<CalendarDaysAdapter.DayViewHolder>() {

        private var days = listOf<CalendarDay>()
        private var selectedDay = -1
        var onDateClickListener: ((Int) -> Unit)? = null

        fun setDays(newDays: List<CalendarDay>, selected: Int) {
            days = newDays
            selectedDay = selected
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): DayViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_calendar_day, parent, false)
            return DayViewHolder(view)
        }

        override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
            holder.bind(days[position])
        }

        override fun getItemCount() = days.size

        inner class DayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val dayText: TextView = itemView.findViewById(R.id.textDay)
            private val indicatorContainer: LinearLayout = itemView.findViewById(R.id.indicatorContainer)
            private val classIndicator: View = itemView.findViewById(R.id.classIndicator)
            private val meetingIndicator: View = itemView.findViewById(R.id.meetingIndicator)

            fun bind(calendarDay: CalendarDay) {
                if (calendarDay.day == 0) {
                    dayText.text = ""
                    itemView.isClickable = false
                    indicatorContainer.visibility = View.GONE
                    return
                }

                dayText.text = calendarDay.day.toString()
                itemView.isClickable = true

                // Style based on state
                when {
                    calendarDay.isSelected -> {
                        itemView.setBackgroundResource(R.drawable.calendar_day_selected)
                        dayText.setTextColor(ContextCompat.getColor(context, R.color.white))
                    }
                    calendarDay.isToday -> {
                        itemView.setBackgroundResource(R.drawable.calendar_day_today)
                        dayText.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary))
                    }
                    else -> {
                        itemView.setBackgroundResource(R.drawable.calendar_day_normal)
                        dayText.setTextColor(ContextCompat.getColor(context, R.color.colorTextPrimary))
                    }
                }

                // Show event indicators
                if (calendarDay.classCount > 0 || calendarDay.meetingCount > 0) {
                    indicatorContainer.visibility = View.VISIBLE
                    classIndicator.visibility = if (calendarDay.classCount > 0) View.VISIBLE else View.GONE
                    meetingIndicator.visibility = if (calendarDay.meetingCount > 0) View.VISIBLE else View.GONE
                } else {
                    indicatorContainer.visibility = View.GONE
                }

                itemView.setOnClickListener {
                    if (calendarDay.day > 0) {
                        onDateClickListener?.invoke(calendarDay.day)
                    }
                }
            }
        }
    }
}

