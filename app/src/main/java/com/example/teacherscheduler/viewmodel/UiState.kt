package com.example.teacherscheduler.viewmodel

sealed class UiState<out T> {
    data object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

data class DashboardData(
    val greeting: String = "",
    val todayClassesCount: Int = 0,
    val upcomingMeetingsCount: Int = 0,
    val activeToDosCount: Int = 0,
    val unseenNoticesCount: Int = 0,
    val todayClasses: List<com.example.teacherscheduler.model.Class> = emptyList(),
    val upcomingMeetings: List<com.example.teacherscheduler.model.Meeting> = emptyList(),
    val urgentToDos: List<com.example.teacherscheduler.model.ToDo> = emptyList(),
    val todayHours: Double = 0.0,
    val weekClassesCount: Int = 0,
    val weekMeetingsCount: Int = 0,
    val weekHours: Double = 0.0,
    val nextEventTitle: String = "",
    val nextEventTime: String = "",
    val insights: List<String> = emptyList(),
    val productivityScore: Int = 0,
    val hodAnalytics: HodDepartmentAnalytics? = null
)

data class HodDepartmentAnalytics(
    val departmentId: Long,
    val departmentName: String = "",
    val totalDepartmentTasks: Int = 0,
    val completedTasks: Int = 0,
    val pendingTasks: Int = 0,
    val overdueTasks: Int = 0,
    val completionPercentage: Float = 0f
)

data class ClassesData(
    val classes: List<com.example.teacherscheduler.model.Class> = emptyList()
)

data class MeetingsData(
    val meetings: List<com.example.teacherscheduler.model.Meeting> = emptyList()
)

data class ToDosData(
    val todos: List<com.example.teacherscheduler.model.ToDo> = emptyList(),
    val pendingCount: Int = 0,
    val overdueCount: Int = 0
)

data class NoticeData(
    val notices: List<com.example.teacherscheduler.model.Notice> = emptyList(),
    val unseenCount: Int = 0,
    val isHOD: Boolean = false
)

data class ResourceData(
    val resources: List<com.example.teacherscheduler.model.Resource> = emptyList(),
    val subjects: List<String> = emptyList()
)