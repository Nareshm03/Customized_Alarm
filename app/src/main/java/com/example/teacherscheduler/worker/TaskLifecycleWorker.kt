package com.example.teacherscheduler.worker

import android.content.Context
import androidx.work.*
import com.example.teacherscheduler.data.local.AppDatabase
import com.example.teacherscheduler.model.TaskStatus
import com.example.teacherscheduler.notification.EnhancedNotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * TaskLifecycleManager - Manages task lifecycle and automated rules
 *
 * Responsibilities:
 * 1. Check for overdue tasks every 15 minutes
 * 2. Update task status to OVERDUE automatically
 * 3. Notify HOD when department tasks become overdue
 * 4. Cancel reminders for completed tasks
 */
class TaskLifecycleWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val database = AppDatabase.getDatabase(context)
    private val todoDao = database.todoDao()
    private val notificationHelper = EnhancedNotificationHelper(context)

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            checkAndUpdateOverdueTasks()
            Result.success()
        } catch (_: Exception) {
            Result.failure()
        }
    }

    private suspend fun checkAndUpdateOverdueTasks() {
        // Get all active tasks synchronously
        val allTasks = todoDao.getAllToDosSync()

        allTasks.forEach { task ->
            // Check if task should be marked overdue
            if (task.isOverdue() && !task.isCompleted && task.status != TaskStatus.COMPLETED) {
                // Update status to OVERDUE
                val updatedTask = task.copy(status = TaskStatus.OVERDUE)
                todoDao.update(updatedTask)
                
                // Notify HOD if it's a department task and notification not sent
                if (task.shouldNotifyHODOverdue()) {
                    notifyHODAboutOverdue(task)
                    // Mark notification as sent
                    todoDao.update(updatedTask.copy(overdueNotificationSent = true))
                }
            }
            
            // Cancel reminders for completed tasks
            if (task.isCompleted && !task.shouldSendReminders()) {
                notificationHelper.cancelNotification(task.id.toInt())
            }
        }
    }

    private fun notifyHODAboutOverdue(task: com.example.teacherscheduler.model.ToDo) {
        notificationHelper.sendOverdueNotificationToHOD(
            taskId = task.id,
            taskTitle = task.title,
            assignedTo = task.assignedTo,
            hodId = task.assignedBy
        )
    }

    companion object {
        private const val WORK_NAME = "task_lifecycle_worker"

        /**
         * Schedule periodic task lifecycle checks
         */
        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<TaskLifecycleWorker>(
                15, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }

        /**
         * Cancel scheduled work
         */
        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
