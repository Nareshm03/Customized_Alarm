package com.example.teacherscheduler.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teacherscheduler.firebase.FirebaseService
import com.example.teacherscheduler.model.GlobalUserState
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val firebaseService: FirebaseService
) : ViewModel() {

    private val _globalUserState = MutableStateFlow(GlobalUserState())
    val globalUserState: StateFlow<GlobalUserState> = _globalUserState.asStateFlow()

    // Case-insensitive HOD check — Firestore stores "hod", enum name is "HOD"
    val isHOD: StateFlow<Boolean> = _globalUserState.map {
        it.role.equals("hod", ignoreCase = true)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    init {
        // Auto-fetch profile whenever the ViewModel is created (e.g. app cold-start to MainActivity)
        viewModelScope.launch {
            fetchRoleFromFirestore()
        }
    }

    suspend fun fetchRoleFromFirestore() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val profile = firebaseService.getUserProfile()
        if (profile != null) {
            _globalUserState.value = GlobalUserState(
                uid = uid,
                name = profile["name"] as? String ?: "",
                department = profile["department"] as? String ?: "",
                role = profile["role"] as? String ?: "teacher"
            )
        }
    }

    fun updateRole(role: String) {
        viewModelScope.launch {
            val current = _globalUserState.value
            firebaseService.saveUserProfile(
                name = current.name,
                email = FirebaseAuth.getInstance().currentUser?.email ?: "",
                role = role,
                department = current.department
            )
            _globalUserState.value = current.copy(role = role)
        }
    }
}
