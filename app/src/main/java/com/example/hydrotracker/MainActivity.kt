package com.example.hydrotracker

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.hydrotracker.notification.ReminderWorker
import com.example.hydrotracker.ui.HapticType
import com.example.hydrotracker.ui.navigation.HydroNavigation
import com.example.hydrotracker.ui.performHaptic
import com.example.hydrotracker.ui.screens.onboarding.OnboardingScreen
import com.example.hydrotracker.ui.theme.HydroTrackerTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as HydroTrackApp
        val settings = app.settingsDataStore

        // Request notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Schedule reminders and fire launch haptic off the main thread
        lifecycleScope.launch {
            val remindersEnabled = settings.remindersEnabled.first()
            val interval = settings.reminderIntervalMin.first()
            if (remindersEnabled) {
                ReminderWorker.schedule(this@MainActivity, interval)
            }

            val hapticOn = settings.hapticEnabled.first()
            // performHaptic is safe to call from any thread; vibrator calls are non-blocking
            performHaptic(this@MainActivity, HapticType.STRONG, hapticOn)
        }

        setContent {
            val darkMode by settings.darkMode.collectAsState(initial = "system")
            val onboardingCompleted by settings.onboardingCompleted.collectAsState(initial = true)
            val scope = rememberCoroutineScope()

            val isDark = when (darkMode) {
                "dark" -> true
                "light" -> false
                else -> isSystemInDarkTheme()
            }

            HydroTrackerTheme(darkTheme = isDark) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    if (!onboardingCompleted) {
                        OnboardingScreen(
                            onComplete = { goalMl ->
                                scope.launch {
                                    settings.setDailyGoalMl(goalMl)
                                    settings.setOnboardingCompleted(true)
                                }
                            }
                        )
                    } else {
                        HydroNavigation()
                    }
                }
            }
        }
    }
}
