package com.example.hydrotracker.ui.screens.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hydrotracker.ui.components.ConfettiEffect
import com.example.hydrotracker.ui.components.QuickAddButtons
import com.example.hydrotracker.ui.components.WaterProgressRing
import com.example.hydrotracker.ui.components.WaveBackground
import com.example.hydrotracker.ui.HapticType
import com.example.hydrotracker.ui.performHaptic
import com.example.hydrotracker.ui.theme.Amber500
import com.example.hydrotracker.ui.theme.Blue500
import com.example.hydrotracker.ui.theme.Green500

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showResetDialog by remember { mutableStateOf(false) }
    var showCustomDialog by remember { mutableStateOf(false) }
    var customAmount by remember { mutableStateOf("") }

    // SUCCESS pulse when daily goal is first reached
    LaunchedEffect(uiState.showConfetti) {
        if (uiState.showConfetti) {
            performHaptic(context, HapticType.SUCCESS, uiState.hapticEnabled)
        }
    }

    val progress = (uiState.currentIntakeMl.toFloat() / uiState.dailyGoalMl).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "linearProgress"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Wave background
        WaveBackground(
            progress = progress,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Header with streak and actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Streak badge
                if (uiState.streak > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.primaryContainer,
                                RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = Amber500,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${uiState.streak} day streak",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Row {
                    IconButton(onClick = {
                        performHaptic(context, HapticType.STRONG, uiState.hapticEnabled)
                        viewModel.undoLastEntry()
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.Undo,
                            contentDescription = "Undo last entry",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = {
                        performHaptic(context, HapticType.STRONG, uiState.hapticEnabled)
                        showResetDialog = true
                    }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Reset day",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Progress ring
            WaterProgressRing(
                currentMl = uiState.currentIntakeMl,
                goalMl = uiState.dailyGoalMl,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Motivational message
            Text(
                text = getMotivationalMessage(progress),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Pacing indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(
                        when (uiState.pacingStatus) {
                            PacingStatus.AHEAD -> Green500.copy(alpha = 0.1f)
                            PacingStatus.ON_TRACK -> Blue500.copy(alpha = 0.1f)
                            PacingStatus.BEHIND -> Amber500.copy(alpha = 0.1f)
                            PacingStatus.COMPLETED -> Green500.copy(alpha = 0.1f)
                        },
                        RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Icon(
                    Icons.Outlined.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = when (uiState.pacingStatus) {
                        PacingStatus.AHEAD -> Green500
                        PacingStatus.ON_TRACK -> Blue500
                        PacingStatus.BEHIND -> Amber500
                        PacingStatus.COMPLETED -> Green500
                    }
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = when (uiState.pacingStatus) {
                        PacingStatus.AHEAD -> "Ahead of schedule"
                        PacingStatus.ON_TRACK -> "On track"
                        PacingStatus.BEHIND -> "Behind schedule"
                        PacingStatus.COMPLETED -> "Goal reached!"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = when (uiState.pacingStatus) {
                        PacingStatus.AHEAD -> Green500
                        PacingStatus.ON_TRACK -> Blue500
                        PacingStatus.BEHIND -> Amber500
                        PacingStatus.COMPLETED -> Green500
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Linear progress bar card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${uiState.currentIntakeMl}ml consumed",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${(uiState.dailyGoalMl - uiState.currentIntakeMl).coerceAtLeast(0)}ml remaining",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        color = if (uiState.currentIntakeMl >= uiState.dailyGoalMl) Green500 else Blue500,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${(progress * 100).toInt()}% of daily goal",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Quick add buttons
            QuickAddButtons(
                amounts = uiState.quickAddAmounts,
                onAddWater = { amount ->
                    performHaptic(context, HapticType.STRONG, uiState.hapticEnabled)
                    viewModel.addWater(amount)
                },
                onCustomAdd = {
                    performHaptic(context, HapticType.STRONG, uiState.hapticEnabled)
                    showCustomDialog = true
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Today's log
            if (uiState.entries.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Today's Log",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        uiState.entries.take(5).forEach { entry ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "+${entry.amountMl}ml",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = formatTime(entry.timestamp),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        if (uiState.entries.size > 5) {
                            Text(
                                text = "+${uiState.entries.size - 5} more entries",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }

        // Confetti overlay
        ConfettiEffect(
            show = uiState.showConfetti,
            onComplete = { viewModel.dismissConfetti() },
            modifier = Modifier.fillMaxSize()
        )

        // Reset confirmation dialog
        if (showResetDialog) {
            AlertDialog(
                onDismissRequest = { showResetDialog = false },
                title = { Text("Reset Today's Progress") },
                text = { Text("Are you sure you want to reset all water intake entries for today? This cannot be undone.") },
                confirmButton = {
                    TextButton(onClick = {
                        performHaptic(context, HapticType.STRONG, uiState.hapticEnabled)
                        viewModel.resetDay()
                        showResetDialog = false
                    }) {
                        Text("Reset", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        performHaptic(context, HapticType.STRONG, uiState.hapticEnabled)
                        showResetDialog = false
                    }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Custom amount dialog
        if (showCustomDialog) {
            AlertDialog(
                onDismissRequest = {
                    showCustomDialog = false
                    customAmount = ""
                },
                title = { Text("Custom Amount") },
                text = {
                    Column {
                        Text("Enter amount in millilitres:")
                        Spacer(modifier = Modifier.height(8.dp))
                        androidx.compose.material3.OutlinedTextField(
                            value = customAmount,
                            onValueChange = { value ->
                                if (value.all { it.isDigit() } && value.length <= 5) {
                                    customAmount = value
                                }
                            },
                            label = { Text("ml") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        val amount = customAmount.toIntOrNull()
                        if (amount != null && amount > 0) {
                            performHaptic(context, HapticType.STRONG, uiState.hapticEnabled)
                            viewModel.addWater(amount)
                        }
                        showCustomDialog = false
                        customAmount = ""
                    }) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        performHaptic(context, HapticType.STRONG, uiState.hapticEnabled)
                        showCustomDialog = false
                        customAmount = ""
                    }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

private fun getMotivationalMessage(progress: Float): String {
    return when {
        progress >= 1f -> "You crushed it today!"
        progress >= 0.75f -> "Almost there, keep going!"
        progress >= 0.5f -> "Halfway there, great progress!"
        progress >= 0.25f -> "Good start, keep it up!"
        progress > 0f -> "Every sip counts!"
        else -> "Start hydrating!"
    }
}

private fun formatTime(timestamp: Long): String {
    val sdf = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(timestamp))
}

