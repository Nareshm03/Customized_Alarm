package com.example.teacherscheduler.data

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Single source of truth for data changes
 * Ensures instant updates across all screens
 */
object DataEventManager {
    
    sealed class DataEvent {
        object ClassAdded : DataEvent()
        object ClassUpdated : DataEvent()
        object ClassDeleted : DataEvent()
        object MeetingAdded : DataEvent()
        object MeetingUpdated : DataEvent()
        object MeetingDeleted : DataEvent()
        object TaskCompleted : DataEvent()
        object TaskUpdated : DataEvent()
        object NoticeRead : DataEvent()
        object DashboardRefresh : DataEvent()
    }
    
    private val _events = MutableSharedFlow<DataEvent>(replay = 0)
    val events = _events.asSharedFlow()
    
    suspend fun emit(event: DataEvent) {
        _events.emit(event)
    }
}
