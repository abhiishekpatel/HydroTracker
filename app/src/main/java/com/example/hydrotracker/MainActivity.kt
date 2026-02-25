package com.example.hydrotracker

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.hydrotracker.notification.ReminderWorker
import com.example.hydrotracker.ui.HapticType
import com.example.hydrotracker.ui.auth.AuthState
import com.example.hydrotracker.ui.auth.AuthViewModel
import com.example.hydrotracker.ui.auth.LoginScreen
import com.example.hydrotracker.ui.auth.SignupScreen
import com.example.hydrotracker.ui.auth.SplashScreen
import com.example.hydrotracker.ui.navigation.HydroNavigation
import com.example.hydrotracker.ui.performHaptic
import com.example.hydrotracker.ui.screens.onboarding.OnboardingScreen
import com.example.hydrotracker.ui.theme.HydroTrackerTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as HydroTrackApp
        val settings = app.settingsDataStore

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        lifecycleScope.launch {
            val remindersEnabled = settings.remindersEnabled.first()
            val interval = settings.reminderIntervalMin.first()
            if (remindersEnabled) {
                ReminderWorker.schedule(this@MainActivity, interval)
            }

            val hapticOn = settings.hapticEnabled.first()
            performHaptic(this@MainActivity, HapticType.MEDIUM, hapticOn)
        }

        setContent {
            val darkMode by settings.darkMode.collectAsState(initial = "light")
            val onboardingCompleted by settings.onboardingCompleted.collectAsState(initial = true)
            val authState by authViewModel.authState.collectAsState()
            val scope = rememberCoroutineScope()

            val isDark = when (darkMode) {
                "dark" -> true
                "light" -> false
                else -> isSystemInDarkTheme()
            }

            HydroTrackerTheme(darkTheme = isDark) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    when (authState) {
                        is AuthState.Loading -> SplashScreen()

                        is AuthState.Authenticated -> {
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
                                HydroNavigation(authViewModel = authViewModel)
                            }
                        }

                        is AuthState.Unauthenticated, is AuthState.Error -> {
                            val authNavController = rememberNavController()
                            NavHost(
                                navController = authNavController,
                                startDestination = "login"
                            ) {
                                composable("login") {
                                    LoginScreen(
                                        authViewModel = authViewModel,
                                        onNavigateToSignUp = {
                                            authNavController.navigate("signup")
                                        }
                                    )
                                }
                                composable("signup") {
                                    SignupScreen(
                                        authViewModel = authViewModel,
                                        onNavigateToLogin = {
                                            authNavController.popBackStack()
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
