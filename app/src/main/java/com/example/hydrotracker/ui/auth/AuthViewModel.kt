package com.example.hydrotracker.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.hydrotracker.HydroTrackApp
import com.example.hydrotracker.data.remote.RemoteProfile
import com.example.hydrotracker.notification.SyncWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    data object Loading : AuthState()
    data object Authenticated : AuthState()
    data object Unauthenticated : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as HydroTrackApp
    private val authRepository = app.authRepository
    private val profileRepository = app.profileRepository
    private val hydrationSyncRepository = app.hydrationSyncRepository
    private val settings = app.settingsDataStore
    private val repository = app.repository

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            _authState.value = if (authRepository.isLoggedIn()) {
                AuthState.Authenticated
            } else {
                AuthState.Unauthenticated
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            authRepository.login(email, password)
                .onSuccess {
                    val userId = authRepository.currentUserId() ?: run {
                        _authState.value = AuthState.Error("Login failed: no user ID")
                        return@launch
                    }
                    settings.setUserId(userId)
                    settings.setUserEmail(email)

                    val profile = profileRepository.fetchProfile()
                    if (profile != null) {
                        settings.setUserName(profile.name)
                        settings.setDailyGoalMl(profile.dailyGoalMl)
                    }

                    runCatching { hydrationSyncRepository.mergeFromCloud() }
                    SyncWorker.schedule(getApplication())
                    _authState.value = AuthState.Authenticated
                }
                .onFailure {
                    _authState.value = AuthState.Error(it.message ?: "Login failed")
                }
        }
    }

    fun signUp(name: String, email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            authRepository.signUp(email, password)
                .onSuccess {
                    val userId = authRepository.currentUserId() ?: run {
                        _authState.value = AuthState.Error("Sign up failed: no user ID")
                        return@launch
                    }
                    settings.setUserId(userId)
                    settings.setUserName(name)
                    settings.setUserEmail(email)

                    val profile = RemoteProfile(id = userId, name = name, dailyGoalMl = 2000)
                    profileRepository.upsertProfile(profile)

                    SyncWorker.schedule(getApplication())
                    _authState.value = AuthState.Authenticated
                }
                .onFailure {
                    _authState.value = AuthState.Error(it.message ?: "Sign up failed")
                }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            repository.clearAllData()
            settings.clearUserInfo()
            SyncWorker.cancel(getApplication())
            _authState.value = AuthState.Unauthenticated
        }
    }
}
