package com.example.hydrotracker.ui.screens.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.hydrotracker.HydroTrackApp
import com.example.hydrotracker.data.local.DailyTotal
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

data class HistoryUiState(
    val weeklyData: List<DailyTotal> = emptyList(),
    val monthlyData: List<DailyTotal> = emptyList(),
    val streak: Int = 0,
    val dailyGoalMl: Int = 4000,
    val selectedMonth: YearMonth = YearMonth.now(),
    val averageWeekly: Int = 0,
    val averageMonthly: Int = 0,
    val totalDaysGoalMet: Int = 0
)

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as HydroTrackApp
    private val repository = app.repository
    private val settings = app.settingsDataStore
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    private val _selectedMonth = MutableStateFlow(YearMonth.now())

    val uiState: StateFlow<HistoryUiState> = combine(
        getWeeklyData(),
        _selectedMonth.flatMapLatest { month -> getMonthlyData(month) },
        settings.dailyGoalMl,
        repository.getDatesGoalMet(4000),
        _selectedMonth
    ) { weekly, monthly, goalMl, datesGoalMet, selectedMonth ->
        val streak = repository.calculateStreak(datesGoalMet)
        val avgWeekly = if (weekly.isNotEmpty()) weekly.sumOf { it.totalMl } / weekly.size else 0
        val avgMonthly = if (monthly.isNotEmpty()) monthly.sumOf { it.totalMl } / monthly.size else 0

        HistoryUiState(
            weeklyData = weekly,
            monthlyData = monthly,
            streak = streak,
            dailyGoalMl = goalMl,
            selectedMonth = selectedMonth,
            averageWeekly = avgWeekly,
            averageMonthly = avgMonthly,
            totalDaysGoalMet = datesGoalMet.size
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HistoryUiState()
    )

    fun selectMonth(month: YearMonth) {
        _selectedMonth.value = month
    }

    fun previousMonth() {
        _selectedMonth.value = _selectedMonth.value.minusMonths(1)
    }

    fun nextMonth() {
        _selectedMonth.value = _selectedMonth.value.plusMonths(1)
    }

    private fun getWeeklyData() = repository.getDailyTotals(
        startDate = LocalDate.now().minusDays(6).format(dateFormatter),
        endDate = LocalDate.now().format(dateFormatter)
    )

    private fun getMonthlyData(month: YearMonth) = repository.getDailyTotals(
        startDate = month.atDay(1).format(dateFormatter),
        endDate = month.atEndOfMonth().format(dateFormatter)
    )
}
