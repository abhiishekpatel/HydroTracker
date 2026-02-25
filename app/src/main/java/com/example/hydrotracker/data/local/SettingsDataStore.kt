package com.example.hydrotracker.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "hydro_settings")

class SettingsDataStore(private val context: Context) {

    companion object {
        val DAILY_GOAL_ML = intPreferencesKey("daily_goal_ml")
        val WAKE_TIME = stringPreferencesKey("wake_time")
        val SLEEP_TIME = stringPreferencesKey("sleep_time")
        val REMINDER_INTERVAL_MIN = intPreferencesKey("reminder_interval_min")
        val REMINDERS_ENABLED = booleanPreferencesKey("reminders_enabled")
        val HAPTIC_ENABLED = booleanPreferencesKey("haptic_enabled")
        val DARK_MODE = stringPreferencesKey("dark_mode") // "light", "dark", "system"
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val QUICK_ADD_AMOUNTS = stringPreferencesKey("quick_add_amounts") // comma-separated
    }

    val dailyGoalMl: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[DAILY_GOAL_ML] ?: 4000
    }

    val wakeTime: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[WAKE_TIME] ?: "07:00"
    }

    val sleepTime: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[SLEEP_TIME] ?: "21:00"
    }

    val reminderIntervalMin: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[REMINDER_INTERVAL_MIN] ?: 90
    }

    val remindersEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[REMINDERS_ENABLED] ?: true
    }

    val hapticEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[HAPTIC_ENABLED] ?: true
    }

    val darkMode: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[DARK_MODE] ?: "system"
    }

    val onboardingCompleted: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[ONBOARDING_COMPLETED] ?: false
    }

    val quickAddAmounts: Flow<List<Int>> = context.dataStore.data.map { prefs ->
        val raw = prefs[QUICK_ADD_AMOUNTS] ?: "250,330,500,750,1000"
        raw.split(",").mapNotNull { it.trim().toIntOrNull() }
    }

    suspend fun setDailyGoalMl(value: Int) {
        context.dataStore.edit { it[DAILY_GOAL_ML] = value }
    }

    suspend fun setWakeTime(value: String) {
        context.dataStore.edit { it[WAKE_TIME] = value }
    }

    suspend fun setSleepTime(value: String) {
        context.dataStore.edit { it[SLEEP_TIME] = value }
    }

    suspend fun setReminderIntervalMin(value: Int) {
        context.dataStore.edit { it[REMINDER_INTERVAL_MIN] = value }
    }

    suspend fun setRemindersEnabled(value: Boolean) {
        context.dataStore.edit { it[REMINDERS_ENABLED] = value }
    }

    suspend fun setHapticEnabled(value: Boolean) {
        context.dataStore.edit { it[HAPTIC_ENABLED] = value }
    }

    suspend fun setDarkMode(value: String) {
        context.dataStore.edit { it[DARK_MODE] = value }
    }

    suspend fun setOnboardingCompleted(value: Boolean) {
        context.dataStore.edit { it[ONBOARDING_COMPLETED] = value }
    }

    suspend fun setQuickAddAmounts(amounts: List<Int>) {
        context.dataStore.edit { it[QUICK_ADD_AMOUNTS] = amounts.joinToString(",") }
    }
}
