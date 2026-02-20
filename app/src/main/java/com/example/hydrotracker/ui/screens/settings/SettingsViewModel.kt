package com.example.hydrotracker.ui.screens.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.hydrotracker.HydroTrackApp
import com.example.hydrotracker.notification.ReminderWorker
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val dailyGoalMl: Int = 4000,
    val wakeTime: String = "07:00",
    val sleepTime: String = "21:00",
    val reminderIntervalMin: Int = 90,
    val remindersEnabled: Boolean = true,
    val hapticEnabled: Boolean = true,
    val darkMode: String = "system",
    val quickAddAmounts: List<Int> = listOf(250, 330, 500, 750, 1000)
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as HydroTrackApp
    private val settings = app.settingsDataStore

    val uiState: StateFlow<SettingsUiState> = combine(
        settings.dailyGoalMl,
        settings.wakeTime,
        settings.sleepTime,
        settings.reminderIntervalMin,
        settings.remindersEnabled,
        settings.hapticEnabled,
        settings.darkMode,
        settings.quickAddAmounts
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        SettingsUiState(
            dailyGoalMl = values[0] as Int,
            wakeTime = values[1] as String,
            sleepTime = values[2] as String,
            reminderIntervalMin = values[3] as Int,
            remindersEnabled = values[4] as Boolean,
            hapticEnabled = values[5] as Boolean,
            darkMode = values[6] as String,
            quickAddAmounts = values[7] as List<Int>
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    fun setDailyGoal(ml: Int) {
        viewModelScope.launch { settings.setDailyGoalMl(ml) }
    }

    fun setWakeTime(time: String) {
        viewModelScope.launch { settings.setWakeTime(time) }
    }

    fun setSleepTime(time: String) {
        viewModelScope.launch { settings.setSleepTime(time) }
    }

    fun setReminderInterval(min: Int) {
        viewModelScope.launch {
            settings.setReminderIntervalMin(min)
            if (settings.remindersEnabled.first()) {
                ReminderWorker.schedule(app, min)
            }
        }
    }

    fun setRemindersEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settings.setRemindersEnabled(enabled)
            if (enabled) {
                ReminderWorker.schedule(app, settings.reminderIntervalMin.first())
            } else {
                ReminderWorker.cancel(app)
            }
        }
    }

    fun setHapticEnabled(enabled: Boolean) {
        viewModelScope.launch { settings.setHapticEnabled(enabled) }
    }

    fun setDarkMode(mode: String) {
        viewModelScope.launch { settings.setDarkMode(mode) }
    }
}
