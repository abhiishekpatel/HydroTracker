package com.example.hydrotracker.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.hydrotracker.HydroTrackApp
import com.example.hydrotracker.data.remote.RemoteProfile
import com.example.hydrotracker.notification.SyncWorker
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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

    // True when Supabase requires email confirmation before login is allowed
    private val _emailConfirmationSent = MutableStateFlow(false)
    val emailConfirmationSent: StateFlow<Boolean> = _emailConfirmationSent.asStateFlow()

    init {
        checkSession()
    }

    /**
     * Waits for the Auth plugin to finish loading the session from SharedPrefs, then
     * sets the initial auth state. This replaces the old isLoggedIn() check which
     * always returned false on app restart (MemorySessionManager had no persisted data).
     */
    private fun checkSession() {
        viewModelScope.launch {
            // LoadingFromStorage is the initial state while the plugin reads SharedPrefs.
            // We wait until it resolves to Authenticated or NotAuthenticated.
            val status = authRepository.getSessionStatus()
                .first { it !is SessionStatus.Initializing }

            if (status is SessionStatus.Authenticated) {
                SyncWorker.schedule(getApplication())
                _authState.value = AuthState.Authenticated
            } else {
                _authState.value = AuthState.Unauthenticated
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

                    // Fetch existing profile or create one (handles first login after email confirmation)
                    val profile = profileRepository.fetchProfile()
                    if (profile != null) {
                        settings.setUserName(profile.name)
                        settings.setDailyGoalMl(profile.dailyGoalMl)
                    } else {
                        val storedName = settings.userName.first()
                            .ifEmpty { email.substringBefore("@") }
                        profileRepository.upsertProfile(
                            RemoteProfile(id = userId, name = storedName, dailyGoalMl = 2000)
                        )
                        settings.setUserName(storedName)
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
            // Store name early so login() can use it if email confirmation is required
            settings.setUserName(name)
            settings.setUserEmail(email)

            authRepository.signUp(email, password)
                .onSuccess {
                    val userId = authRepository.currentUserId()
                    if (userId == null) {
                        // Supabase requires email confirmation — session not created yet
                        _emailConfirmationSent.value = true
                        _authState.value = AuthState.Unauthenticated
                        return@launch
                    }
                    settings.setUserId(userId)
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

    fun dismissEmailConfirmation() {
        _emailConfirmationSent.value = false
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
