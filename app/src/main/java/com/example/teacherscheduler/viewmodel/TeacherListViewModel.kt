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
import javax.inject.Inject

data class Teacher(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val teacherId: String = "",
    val department: String = ""
)

@HiltViewModel
class TeacherListViewModel @Inject constructor() : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    
    private val _teachers = MutableStateFlow<List<Teacher>>(emptyList())
    val teachers: StateFlow<List<Teacher>> = _teachers.asStateFlow()

    fun loadTeachers(department: String) {
        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("users")
                    .whereEqualTo("department", department)
                    .whereEqualTo("role", "teacher")
                    .get()
                    .await()

                _teachers.value = snapshot.documents.mapNotNull { doc ->
                    Teacher(
                        uid = doc.id,
                        name = doc.getString("name") ?: "",
                        email = doc.getString("email") ?: "",
                        teacherId = doc.getString("teacherId") ?: doc.id,
                        department = doc.getString("department") ?: ""
                    )
                }
            } catch (e: Exception) {
                _teachers.value = emptyList()
            }
        }
    }
}
