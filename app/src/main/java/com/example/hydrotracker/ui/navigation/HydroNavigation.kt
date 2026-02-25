package com.example.hydrotracker.ui.navigation

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.outlined.Equalizer
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.hydrotracker.HydroTrackApp
import com.example.hydrotracker.ui.HapticType
import com.example.hydrotracker.ui.auth.AuthViewModel
import com.example.hydrotracker.ui.performHaptic
import com.example.hydrotracker.ui.profile.ProfileScreen
import com.example.hydrotracker.ui.screens.dashboard.DashboardScreen
import com.example.hydrotracker.ui.screens.history.HistoryScreen
import com.example.hydrotracker.ui.screens.settings.SettingsScreen
import com.example.hydrotracker.ui.theme.HydroBlue
import com.example.hydrotracker.ui.theme.HydroTextSecondary

sealed class Screen(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    data object Dashboard : Screen("dashboard", "Dashboard", Icons.Filled.WaterDrop, Icons.Outlined.WaterDrop)
    data object History : Screen("history", "History", Icons.Filled.Equalizer, Icons.Outlined.Equalizer)
    data object Settings : Screen("settings", "Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
    data object Profile : Screen("profile", "Profile", Icons.Filled.Person, Icons.Outlined.Person)
}

val bottomNavScreens = listOf(
    Screen.Dashboard,
    Screen.History,
    Screen.Settings,
    Screen.Profile
)

@Composable
fun HydroNavigation(authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val context = LocalContext.current
    val hapticEnabled by (context.applicationContext as HydroTrackApp)
        .settingsDataStore.hapticEnabled
        .collectAsStateWithLifecycle(initialValue = true)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Surface(
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp,
                    modifier = Modifier.height(64.dp)
                ) {
                    bottomNavScreens.forEach { screen ->
                        val selected =
                            currentDestination?.hierarchy?.any { it.route == screen.route } == true

                        val iconAlpha by animateFloatAsState(
                            targetValue = if (selected) 1f else 0.55f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            ),
                            label = "iconAlpha"
                        )
                        val iconSize by animateFloatAsState(
                            targetValue = if (selected) 24f else 22f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessHigh
                            ),
                            label = "iconSize"
                        )

                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = if (selected) screen.selectedIcon
                                    else screen.unselectedIcon,
                                    contentDescription = screen.label,
                                    tint = if (selected) HydroBlue
                                    else HydroTextSecondary.copy(alpha = iconAlpha),
                                    modifier = Modifier.size(iconSize.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = screen.label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp
                                    ),
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (selected) HydroBlue
                                    else HydroTextSecondary.copy(alpha = iconAlpha)
                                )
                            },
                            selected = selected,
                            onClick = {
                                performHaptic(context, HapticType.TICK, hapticEnabled)
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = HydroBlue,
                                selectedTextColor = HydroBlue,
                                unselectedIconColor = HydroTextSecondary,
                                unselectedTextColor = HydroTextSecondary,
                                indicatorColor = Color.Transparent
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier,
            enterTransition = { fadeIn() + slideInVertically { it / 30 } },
            exitTransition = { fadeOut() },
            popEnterTransition = { fadeIn() + slideInVertically { -(it / 30) } },
            popExitTransition = { fadeOut() + slideOutVertically { it / 30 } }
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    bottomPadding = innerPadding.calculateBottomPadding(),
                    onViewAllLogs = {
                        navController.navigate(Screen.History.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            composable(Screen.History.route) {
                HistoryScreen(bottomPadding = innerPadding.calculateBottomPadding())
            }
            composable(Screen.Settings.route) {
                SettingsScreen(bottomPadding = innerPadding.calculateBottomPadding())
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    authViewModel = authViewModel,
                    bottomPadding = innerPadding.calculateBottomPadding()
                )
            }
        }
    }
}
