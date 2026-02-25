package com.example.hydrotracker.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "water_entries")
data class WaterEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amountMl: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val date: String, // YYYY-MM-DD
    val syncId: String? = null,
    val isSynced: Boolean = false
)
