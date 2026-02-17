package com.example.hydrotracker.data.repository

import com.example.hydrotracker.data.local.DailyTotal
import com.example.hydrotracker.data.local.SettingsDataStore
import com.example.hydrotracker.data.local.WaterEntry
import com.example.hydrotracker.data.local.WaterEntryDao
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class WaterRepository(
    private val dao: WaterEntryDao,
    val settings: SettingsDataStore
) {
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun todayString(): String = LocalDate.now().format(dateFormatter)

    suspend fun addWater(amountMl: Int): Long {
        val today = todayString()
        val entry = WaterEntry(
            amountMl = amountMl,
            timestamp = System.currentTimeMillis(),
            date = today
        )
        return dao.insert(entry)
    }

    suspend fun undoLastEntry(): WaterEntry? {
        val today = todayString()
        val lastEntry = dao.getLastEntryForDate(today)
        if (lastEntry != null) {
            dao.delete(lastEntry)
        }
        return lastEntry
    }

    suspend fun resetDay() {
        dao.deleteAllForDate(todayString())
    }

    fun getTodayEntries(): Flow<List<WaterEntry>> {
        return dao.getEntriesForDate(todayString())
    }

    fun getTodayTotal(): Flow<Int> {
        return dao.getTotalForDate(todayString())
    }

    fun getDailyTotals(startDate: String, endDate: String): Flow<List<DailyTotal>> {
        return dao.getDailyTotals(startDate, endDate)
    }

    fun getDatesGoalMet(goalMl: Int): Flow<List<DailyTotal>> {
        return dao.getDatesGoalMet(goalMl)
    }

    fun calculateStreak(datesGoalMet: List<DailyTotal>): Int {
        if (datesGoalMet.isEmpty()) return 0

        val today = LocalDate.now()
        val dates = datesGoalMet.map { LocalDate.parse(it.date, dateFormatter) }.toSet()

        // Check if today or yesterday is in the set (streak can continue)
        val startDate = when {
            today in dates -> today
            today.minusDays(1) in dates -> today.minusDays(1)
            else -> return 0
        }

        var streak = 0
        var current = startDate
        while (current in dates) {
            streak++
            current = current.minusDays(1)
        }
        return streak
    }
}
