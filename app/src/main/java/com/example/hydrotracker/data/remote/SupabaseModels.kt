package com.example.hydrotracker.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RemoteProfile(
    val id: String,
    val name: String,
    @SerialName("daily_goal_ml") val dailyGoalMl: Int = 2000,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class RemoteHydrationLog(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("amount_ml") val amountMl: Int,
    val timestamp: Long,
    val date: String
)
