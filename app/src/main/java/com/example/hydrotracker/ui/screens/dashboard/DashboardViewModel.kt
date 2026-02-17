package com.example.hydrotracker.ui.screens.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.hydrotracker.HydroTrackApp
import com.example.hydrotracker.data.local.WaterEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter

data class DashboardUiState(
    val currentIntakeMl: Int = 0,
    val dailyGoalMl: Int = 4000,
    val entries: List<WaterEntry> = emptyList(),
    val quickAddAmounts: List<Int> = listOf(250, 330, 500, 750, 1000),
    val streak: Int = 0,
    val hapticEnabled: Boolean = true,
    val showConfetti: Boolean = false,
    val lastAddedAmount: Int? = null,
    val pacingStatus: PacingStatus = PacingStatus.ON_TRACK
)

enum class PacingStatus {
    ON_TRACK, BEHIND, AHEAD, COMPLETED
}

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as HydroTrackApp
    private val repository = app.repository
    private val settings = app.settingsDataStore

    private val _showConfetti = MutableStateFlow(false)
    private val _lastAddedAmount = MutableStateFlow<Int?>(null)

    val uiState: StateFlow<DashboardUiState> = combine(
        repository.getTodayTotal(),
        repository.getTodayEntries(),
        settings.dailyGoalMl,
        settings.quickAddAmounts,
        settings.hapticEnabled,
        _showConfetti,
        _lastAddedAmount,
        repository.getDatesGoalMet(4000)
    ) { values ->
        val currentIntake = values[0] as Int
        val entries = values[1] as List<*>
        val goalMl = values[2] as Int
        val quickAmounts = values[3] as List<*>
        val haptic = values[4] as Boolean
        val confetti = values[5] as Boolean
        val lastAdded = values[6] as Int?
        val datesGoalMet = values[7] as List<*>

        @Suppress("UNCHECKED_CAST")
        DashboardUiState(
            currentIntakeMl = currentIntake,
            dailyGoalMl = goalMl,
            entries = entries as List<WaterEntry>,
            quickAddAmounts = quickAmounts as List<Int>,
            streak = repository.calculateStreak(
                datesGoalMet as List<com.example.hydrotracker.data.local.DailyTotal>
            ),
            hapticEnabled = haptic,
            showConfetti = confetti,
            lastAddedAmount = lastAdded,
            pacingStatus = calculatePacingStatus(currentIntake, goalMl)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )

    fun addWater(amountMl: Int) {
        viewModelScope.launch {
            val previousTotal = uiState.value.currentIntakeMl
            repository.addWater(amountMl)
            _lastAddedAmount.value = amountMl

            val newTotal = previousTotal + amountMl
            if (previousTotal < uiState.value.dailyGoalMl && newTotal >= uiState.value.dailyGoalMl) {
                _showConfetti.value = true
            }

            // Clear the animation trigger after delay
            kotlinx.coroutines.delay(300)
            _lastAddedAmount.value = null
        }
    }

    fun undoLastEntry() {
        viewModelScope.launch {
            repository.undoLastEntry()
        }
    }

    fun resetDay() {
        viewModelScope.launch {
            repository.resetDay()
            _showConfetti.value = false
        }
    }

    fun dismissConfetti() {
        _showConfetti.value = false
    }

    private fun calculatePacingStatus(currentMl: Int, goalMl: Int): PacingStatus {
        if (currentMl >= goalMl) return PacingStatus.COMPLETED

        val now = LocalTime.now()
        val wakeTime = LocalTime.of(7, 0)
        val sleepTime = LocalTime.of(21, 0)

        if (now.isBefore(wakeTime) || now.isAfter(sleepTime)) return PacingStatus.ON_TRACK

        val totalMinutes = (sleepTime.toSecondOfDay() - wakeTime.toSecondOfDay()) / 60.0
        val elapsedMinutes = (now.toSecondOfDay() - wakeTime.toSecondOfDay()) / 60.0
        val expectedProgress = (elapsedMinutes / totalMinutes)
        val expectedMl = goalMl * expectedProgress
        val actualProgress = currentMl.toDouble() / expectedMl

        return when {
            actualProgress >= 1.1 -> PacingStatus.AHEAD
            actualProgress >= 0.7 -> PacingStatus.ON_TRACK
            else -> PacingStatus.BEHIND
        }
    }
}
