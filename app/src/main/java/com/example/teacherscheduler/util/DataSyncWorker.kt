package com.example.teacherscheduler.util

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.teacherscheduler.data.Repository

class DataSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        val repository = Repository(applicationContext)
        return try {
            repository.performSync()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}