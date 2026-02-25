package com.example.hydrotracker.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.hydrotracker.HydroTrackApp
import com.example.hydrotracker.data.remote.RemoteProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class ProfileUiState(
    val name: String = "",
    val email: String = "",
    val dailyGoalMl: Int = 2000,
    val todayTotalMl: Int = 0,
    val isLoading: Boolean = false,
    val editName: String = "",
    val editGoalMl: Int = 2000,
    val isSaving: Boolean = false
)

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as HydroTrackApp
    private val settings = app.settingsDataStore
    private val repository = app.repository
    private val authRepository = app.authRepository
    private val profileRepository = app.profileRepository

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                settings.userName,
                settings.userEmail,
                settings.dailyGoalMl,
                repository.getTodayTotal()
            ) { name, email, goal, todayTotal ->
                ProfileUiState(
                    name = name,
                    email = email,
                    dailyGoalMl = goal,
                    todayTotalMl = todayTotal,
                    editName = name,
                    editGoalMl = goal
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun updateEditName(value: String) {
        _uiState.value = _uiState.value.copy(editName = value)
    }

    fun updateEditGoal(value: Int) {
        _uiState.value = _uiState.value.copy(editGoalMl = value)
    }

    fun saveProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            val userId = authRepository.currentUserId()
            val name = _uiState.value.editName
            val goal = _uiState.value.editGoalMl

            settings.setUserName(name)
            settings.setDailyGoalMl(goal)

            if (userId != null) {
                val profile = RemoteProfile(id = userId, name = name, dailyGoalMl = goal)
                profileRepository.upsertProfile(profile)
            }
            _uiState.value = _uiState.value.copy(isSaving = false)
        }
    }

    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            repository.clearAllData()
            settings.clearUserInfo()
            onComplete()
        }
    }
}
