package com.example.teacherscheduler.util

import android.util.Log
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace

object PerformanceMonitor {
    
    private val traces = mutableMapOf<String, Trace>()
    
    fun startTrace(name: String) {
        try {
            val trace = FirebasePerformance.getInstance().newTrace(name)
            trace.start()
            traces[name] = trace
            Log.d("PerformanceMonitor", "Started trace: $name")
        } catch (e: Exception) {
            Log.w("PerformanceMonitor", "Failed to start trace: $name", e)
        }
    }
    
    fun stopTrace(name: String) {
        try {
            traces[name]?.let { trace ->
                trace.stop()
                traces.remove(name)
                Log.d("PerformanceMonitor", "Stopped trace: $name")
            }
        } catch (e: Exception) {
            Log.w("PerformanceMonitor", "Failed to stop trace: $name", e)
        }
    }
    
    fun addMetric(traceName: String, metricName: String, value: Long) {
        try {
            traces[traceName]?.putMetric(metricName, value)
        } catch (e: Exception) {
            Log.w("PerformanceMonitor", "Failed to add metric", e)
        }
    }
}
