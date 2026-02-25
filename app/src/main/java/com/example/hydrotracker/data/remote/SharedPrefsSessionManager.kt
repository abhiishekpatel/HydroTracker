package com.example.hydrotracker.data.remote

import android.content.Context
import io.github.jan.supabase.auth.SessionManager
import io.github.jan.supabase.auth.user.UserSession
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SharedPrefsSessionManager(context: Context) : SessionManager {

    private val prefs = context.getSharedPreferences("supabase_session", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    override suspend fun saveSession(session: UserSession) {
        prefs.edit().putString(KEY, json.encodeToString(session)).apply()
    }

    override suspend fun loadSession(): UserSession? {
        val str = prefs.getString(KEY, null) ?: return null
        return runCatching { json.decodeFromString<UserSession>(str) }.getOrNull()
    }

    override suspend fun deleteSession() {
        prefs.edit().remove(KEY).apply()
    }

    companion object {
        private const val KEY = "session"
    }
}
