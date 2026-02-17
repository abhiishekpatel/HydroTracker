package com.example.hydrotracker.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WaterEntryDao {

    @Insert
    suspend fun insert(entry: WaterEntry): Long

    @Delete
    suspend fun delete(entry: WaterEntry)

    @Query("DELETE FROM water_entries WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM water_entries WHERE date = :date ORDER BY timestamp DESC")
    fun getEntriesForDate(date: String): Flow<List<WaterEntry>>

    @Query("SELECT COALESCE(SUM(amountMl), 0) FROM water_entries WHERE date = :date")
    fun getTotalForDate(date: String): Flow<Int>

    @Query("SELECT * FROM water_entries WHERE date = :date ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastEntryForDate(date: String): WaterEntry?

    @Query("DELETE FROM water_entries WHERE date = :date")
    suspend fun deleteAllForDate(date: String)

    @Query("""
        SELECT date, SUM(amountMl) as totalMl
        FROM water_entries
        WHERE date >= :startDate AND date <= :endDate
        GROUP BY date
        ORDER BY date ASC
    """)
    fun getDailyTotals(startDate: String, endDate: String): Flow<List<DailyTotal>>

    @Query("""
        SELECT COUNT(DISTINCT date) FROM (
            SELECT date, SUM(amountMl) as total
            FROM water_entries
            GROUP BY date
            HAVING total >= :goalMl
        )
    """)
    fun getDaysGoalMet(goalMl: Int): Flow<Int>

    @Query("""
        SELECT date, SUM(amountMl) as totalMl
        FROM water_entries
        GROUP BY date
        HAVING totalMl >= :goalMl
        ORDER BY date DESC
    """)
    fun getDatesGoalMet(goalMl: Int): Flow<List<DailyTotal>>
}

data class DailyTotal(
    val date: String,
    val totalMl: Int
)
