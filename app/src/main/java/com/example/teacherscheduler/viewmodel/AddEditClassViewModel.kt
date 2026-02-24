package com.example.teacherscheduler.viewmodel

import androidx.lifecycle.ViewModel
import com.example.teacherscheduler.data.Repository
import com.example.teacherscheduler.model.Class
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AddEditClassViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    suspend fun saveClass(classItem: Class): Boolean {
        return try {
            if (classItem.id == 0L) {
                repository.insertClass(classItem)
            } else {
                repository.updateClass(classItem)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updateClass(classItem: Class): Boolean {
        return try {
            repository.updateClass(classItem)
            true
        } catch (e: Exception) {
            false
        }
    }
}

