package com.example.teacherscheduler.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.teacherscheduler.MainActivity
import com.example.teacherscheduler.R
import com.example.teacherscheduler.data.Repository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ScheduleWidgetProvider : AppWidgetProvider() {
    
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }
    
    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.widget_schedule)
        
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        views.setOnClickPendingIntent(R.id.widgetLayout, pendingIntent)
        
        CoroutineScope(Dispatchers.IO).launch {
            val repository = Repository(context)
            val today = Calendar.getInstance()
            val startOfDay = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }
            val endOfDay = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
            }
            
            val classes = repository.getClassesForDateRange(startOfDay.timeInMillis, endOfDay.timeInMillis)
            val meetings = repository.getMeetingsForDateRange(startOfDay.timeInMillis, endOfDay.timeInMillis)
            
            val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
            views.setTextViewText(R.id.widgetDate, dateFormat.format(today.time))
            views.setTextViewText(R.id.widgetClassCount, "${classes.size} Classes")
            views.setTextViewText(R.id.widgetMeetingCount, "${meetings.size} Meetings")
            
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
