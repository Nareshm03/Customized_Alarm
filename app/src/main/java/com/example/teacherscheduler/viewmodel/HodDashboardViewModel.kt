package com.example.teacherscheduler.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*
import javax.inject.Inject

data class HodDashboardStats(
    val totalTeachers: Int = 0,
    val pendingTasks: Int = 0,
    val noticesPublished: Int = 0,
    val classesToday: Int = 0
)

@HiltViewModel
class HodDashboardViewModel @Inject constructor() : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    
    private val _stats = MutableStateFlow(HodDashboardStats())
    val stats: StateFlow<HodDashboardStats> = _stats.asStateFlow()

    fun loadDashboardStats(department: String) {
        viewModelScope.launch {
            val teachers = getTeachersCount(department)
            val pending = getPendingTasksCount(department)
            val notices = getNoticesCount(department)
            val classes = getClassesTodayCount(department)
            
            _stats.value = HodDashboardStats(
                totalTeachers = teachers,
                pendingTasks = pending,
                noticesPublished = notices,
                classesToday = classes
            )
        }
    }

    private suspend fun getTeachersCount(department: String): Int {
        return try {
            firestore.collection("users")
                .whereEqualTo("department", department)
                .whereEqualTo("role", "teacher")
                .get()
                .await()
                .size()
        } catch (e: Exception) {
            0
        }
    }

    private suspend fun getPendingTasksCount(department: String): Int {
        return try {
            firestore.collection("tasks")
                .whereEqualTo("departmentId", department)
                .whereEqualTo("status", "ASSIGNED")
                .get()
                .await()
                .size()
        } catch (e: Exception) {
            0
        }
    }

    private suspend fun getNoticesCount(department: String): Int {
        return try {
            firestore.collection("notices")
                .whereEqualTo("departmentId", department)
                .whereEqualTo("isActive", true)
                .get()
                .await()
                .size()
        } catch (e: Exception) {
            0
        }
    }

    private suspend fun getClassesTodayCount(department: String): Int {
        return try {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            val startOfDay = calendar.time

            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            val endOfDay = calendar.time

            firestore.collection("classes")
                .whereEqualTo("department", department)
                .whereGreaterThanOrEqualTo("startDate", startOfDay)
                .whereLessThanOrEqualTo("startDate", endOfDay)
                .get()
                .await()
                .size()
        } catch (e: Exception) {
            0
        }
    }
}
