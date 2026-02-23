package com.example.hydrotracker.ui.navigation

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import com.example.hydrotracker.ui.performHaptic
import com.example.hydrotracker.ui.screens.dashboard.DashboardScreen
import com.example.hydrotracker.ui.screens.history.HistoryScreen
import com.example.hydrotracker.ui.screens.settings.SettingsScreen
import com.example.hydrotracker.ui.screens.tips.TipsScreen
import com.example.hydrotracker.ui.theme.IceBlue400
import com.example.hydrotracker.ui.theme.IceBlue500
import com.example.hydrotracker.ui.theme.Violet400

sealed class Screen(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    data object Dashboard : Screen("dashboard", "Home", Icons.Filled.Home, Icons.Outlined.Home)
    data object History : Screen("history", "History", Icons.Filled.History, Icons.Outlined.History)
    data object Tips : Screen("tips", "Tips", Icons.Filled.Lightbulb, Icons.Outlined.Lightbulb)
    data object Settings : Screen("settings", "Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
}

val bottomNavScreens = listOf(
    Screen.Dashboard,
    Screen.History,
    Screen.Tips,
    Screen.Settings
)

@Composable
fun HydroNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val context = LocalContext.current
    val hapticEnabled by (context.applicationContext as HydroTrackApp)
        .settingsDataStore.hapticEnabled
        .collectAsStateWithLifecycle(initialValue = true)



    Scaffold(
        containerColor = Color(0xFF060810),
        bottomBar = {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF0D1525).copy(alpha = 0.97f),
                                Color(0xFF060810).copy(alpha = 0.99f)
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.08f),
                                Color.Transparent
                            )
                        ),
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                    )
            ) {
                NavigationBar(
                    containerColor = Color.Transparent,
                    tonalElevation = 0.dp,
                    modifier = Modifier.height(72.dp)
                ) {
                    bottomNavScreens.forEach { screen ->
                        val selected =
                            currentDestination?.hierarchy?.any { it.route == screen.route } == true

                        val iconAlpha by animateFloatAsState(
                            targetValue = if (selected) 1f else 0.38f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            ),
                            label = "iconAlpha"
                        )
                        val iconSize by animateDpAsState(
                            targetValue = if (selected) 22.dp else 20.dp,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessHigh
                            ),
                            label = "iconSize"
                        )

                        NavigationBarItem(
                            icon = {
                                Box(contentAlignment = Alignment.TopCenter) {
                                    Icon(
                                        imageVector = if (selected) screen.selectedIcon
                                        else screen.unselectedIcon,
                                        contentDescription = screen.label,
                                        tint = if (selected) IceBlue400
                                        else Color.White.copy(alpha = iconAlpha),
                                        modifier = Modifier.size(iconSize)
                                    )
                                    // Active dot indicator
                                    if (selected) {
                                        Box(
                                            modifier = Modifier
                                                .size(3.dp)
                                                .offset(y = (-6).dp)
                                                .clip(CircleShape)
                                                .background(
                                                    Brush.radialGradient(
                                                        listOf(IceBlue400, Violet400)
                                                    )
                                                )
                                        )
                                    }
                                }
                            },
                            label = {
                                Text(
                                    text = screen.label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        letterSpacing = 0.4.sp
                                    ),
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selected) IceBlue400
                                    else Color.White.copy(alpha = 0.30f)
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
                                selectedIconColor = IceBlue400,
                                selectedTextColor = IceBlue400,
                                unselectedIconColor = Color.White.copy(alpha = 0.30f),
                                unselectedTextColor = Color.White.copy(alpha = 0.30f),
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
            modifier = Modifier.padding(innerPadding),
            enterTransition = { fadeIn() + slideInVertically { it / 20 } },
            exitTransition = { fadeOut() },
            popEnterTransition = { fadeIn() + slideInVertically { -(it / 20) } },
            popExitTransition = { fadeOut() + slideOutVertically { it / 20 } }
        ) {
            composable(Screen.Dashboard.route) { DashboardScreen() }
            composable(Screen.History.route) { HistoryScreen() }
            composable(Screen.Tips.route) { TipsScreen() }
            composable(Screen.Settings.route) { SettingsScreen() }
        }
    }
}
