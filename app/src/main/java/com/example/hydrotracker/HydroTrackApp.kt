package com.example.hydrotracker

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.example.hydrotracker.data.local.HydroDatabase
import com.example.hydrotracker.data.local.SettingsDataStore
import com.example.hydrotracker.data.remote.initSupabaseClient
import com.example.hydrotracker.data.repository.AuthRepository
import com.example.hydrotracker.data.repository.HydrationSyncRepository
import com.example.hydrotracker.data.repository.ProfileRepository
import com.example.hydrotracker.data.repository.WaterRepository

class HydroTrackApp : Application() {

    lateinit var repository: WaterRepository
        private set

    lateinit var settingsDataStore: SettingsDataStore
        private set

    lateinit var authRepository: AuthRepository
        private set

    lateinit var profileRepository: ProfileRepository
        private set

    lateinit var hydrationSyncRepository: HydrationSyncRepository
        private set

    override fun onCreate() {
        super.onCreate()
        initSupabaseClient(this)

        val database = HydroDatabase.getDatabase(this)
        settingsDataStore = SettingsDataStore(this)
        repository = WaterRepository(database.waterEntryDao(), settingsDataStore)
        authRepository = AuthRepository()
        profileRepository = ProfileRepository()
        hydrationSyncRepository = HydrationSyncRepository(database.waterEntryDao())

        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val reminderChannel = NotificationChannel(
            REMINDER_CHANNEL_ID,
            "Hydration Reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Reminders to drink water throughout the day"
        }

        val achievementChannel = NotificationChannel(
            ACHIEVEMENT_CHANNEL_ID,
            "Achievements",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Goal completion and streak notifications"
        }

        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(reminderChannel)
        manager.createNotificationChannel(achievementChannel)
    }

    companion object {
        const val REMINDER_CHANNEL_ID = "hydro_reminders"
        const val ACHIEVEMENT_CHANNEL_ID = "hydro_achievements"
    }
}
