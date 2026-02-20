package com.example.hydrotracker.ui.screens.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.example.hydrotracker.ui.theme.Blue400
import com.example.hydrotracker.ui.theme.Blue500
import com.example.hydrotracker.ui.theme.Cyan400
import com.example.hydrotracker.ui.theme.Green400
import com.example.hydrotracker.ui.theme.Green500
import com.example.hydrotracker.ui.theme.Slate500
import com.example.hydrotracker.ui.theme.Slate600
import com.example.hydrotracker.ui.theme.Slate700
import com.example.hydrotracker.ui.theme.Slate800
import java.time.format.DateTimeFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showResetDialog by remember { mutableStateOf(false) }
    var showCustomDialog by remember { mutableStateOf(false) }
    var customAmount by remember { mutableStateOf("") }

    // Goal-reached haptic — SUCCESS pattern fires once when confetti appears
    LaunchedEffect(uiState.showConfetti) {
        if (uiState.showConfetti) {
            performHaptic(context, HapticType.SUCCESS, uiState.hapticEnabled)
        }
    }

    val progress = (uiState.currentIntakeMl.toFloat() / uiState.dailyGoalMl).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "progress"
    )

    val isDark = MaterialTheme.colorScheme.background.run {
        (red * 0.299f + green * 0.587f + blue * 0.114f) < 0.5f
    }

    // Glass surface colours that adapt to theme
    val glassBg = if (isDark)
        Color(0xFF1E293B).copy(alpha = 0.55f)
    else
        Color.White.copy(alpha = 0.72f)

    val glassBorder = if (isDark)
        Color.White.copy(alpha = 0.08f)
    else
        Color.White.copy(alpha = 0.60f)

    Box(modifier = Modifier.fillMaxSize()) {

        // Subtle wave fill behind content
        WaveBackground(
            progress = progress,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(20.dp))

            // ── Top bar ──────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Streak pill
                if (uiState.streak > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                brush = Brush.horizontalGradient(
                                    listOf(
                                        Amber500.copy(alpha = 0.18f),
                                        Color(0xFFFBBF24).copy(alpha = 0.12f)
                                    )
                                )
                            )
                            .border(
                                width = 1.dp,
                                color = Amber500.copy(alpha = 0.30f),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = Amber500,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${uiState.streak} day streak",
                            style = MaterialTheme.typography.labelLarge,
                            color = Amber500,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(4.dp))
                }

                // Action icon buttons
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Undo
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(glassBg)
                            .border(1.dp, glassBorder, CircleShape)
                            .clickable {
                                performHaptic(context, HapticType.TICK, uiState.hapticEnabled)
                                viewModel.undoLastEntry()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Undo,
                            contentDescription = "Undo last entry",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    // Reset
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(glassBg)
                            .border(1.dp, glassBorder, CircleShape)
                            .clickable {
                                performHaptic(context, HapticType.TICK, uiState.hapticEnabled)
                                showResetDialog = true
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.RestartAlt,
                            contentDescription = "Reset day",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ── Water progress ring ───────────────────────────────────────────
            WaterProgressRing(
                currentMl = uiState.currentIntakeMl,
                goalMl = uiState.dailyGoalMl,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ── Motivational message ──────────────────────────────────────────
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + slideInVertically()
            ) {
                Text(
                    text = getMotivationalMessage(progress),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ── Pacing pill ───────────────────────────────────────────────────
            val pacingColor = when (uiState.pacingStatus) {
                PacingStatus.AHEAD, PacingStatus.COMPLETED -> Green500
                PacingStatus.ON_TRACK -> Blue500
                PacingStatus.BEHIND -> Amber500
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(pacingColor.copy(alpha = 0.10f))
                    .border(1.dp, pacingColor.copy(alpha = 0.22f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Icon(
                    Icons.Outlined.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(15.dp),
                    tint = pacingColor
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = when (uiState.pacingStatus) {
                        PacingStatus.AHEAD -> "Ahead of schedule"
                        PacingStatus.ON_TRACK -> "On track"
                        PacingStatus.BEHIND -> "Behind schedule"
                        PacingStatus.COMPLETED -> "Goal reached! 🎉"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = pacingColor
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Progress summary card ─────────────────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = glassBg
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, glassBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Consumed stat
                        Column(horizontalAlignment = Alignment.Start) {
                            Text(
                                text = "CONSUMED",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 0.8.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = formatMl(uiState.currentIntakeMl),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (uiState.currentIntakeMl >= uiState.dailyGoalMl)
                                    Green400 else MaterialTheme.colorScheme.primary
                            )
                        }

                        // Vertical divider
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(36.dp)
                                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                        )

                        // Remaining stat
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "REMAINING",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 0.8.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = formatMl(
                                    (uiState.dailyGoalMl - uiState.currentIntakeMl).coerceAtLeast(0)
                                ),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Progress track
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(7.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animatedProgress)
                                .height(7.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    brush = Brush.horizontalGradient(
                                        if (uiState.currentIntakeMl >= uiState.dailyGoalMl)
                                            listOf(Green400, Green500)
                                        else
                                            listOf(Blue400, Cyan400)
                                    )
                                )
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "${(progress * 100).toInt()}% of ${formatMl(uiState.dailyGoalMl)} daily goal",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // ── Quick add section label ───────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .height(1.dp)
                        .weight(1f)
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color.Transparent, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                            )
                        )
                )
                Text(
                    text = "  ADD WATER  ",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.2.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Box(
                    modifier = Modifier
                        .height(1.dp)
                        .weight(1f)
                        .background(
                            Brush.horizontalGradient(
                                listOf(MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), Color.Transparent)
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ── Quick add buttons ─────────────────────────────────────────────
            QuickAddButtons(
                amounts = uiState.quickAddAmounts,
                onAddWater = { amount ->
                    // MEDIUM haptic for every water-add
                    performHaptic(context, HapticType.MEDIUM, uiState.hapticEnabled)
                    viewModel.addWater(amount)
                },
                onCustomAdd = {
                    performHaptic(context, HapticType.CLICK, uiState.hapticEnabled)
                    showCustomDialog = true
                }
            )

            Spacer(modifier = Modifier.height(22.dp))

            // ── Today's log card ──────────────────────────────────────────────
            AnimatedVisibility(
                visible = uiState.entries.isNotEmpty(),
                enter = fadeIn() + slideInVertically { it / 2 }
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = glassBg),
                    border = androidx.compose.foundation.BorderStroke(1.dp, glassBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Today's Log",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 10.dp)
                        )
                        uiState.entries.take(5).forEach { entry ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.radialGradient(listOf(Blue400, Cyan400))
                                            )
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "+${formatMl(entry.amountMl)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Text(
                                    text = formatTime(entry.timestamp),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (uiState.entries.indexOf(entry) < uiState.entries.take(5).lastIndex) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(
                                            MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
                                        )
                                )
                            }
                        }
                        if (uiState.entries.size > 5) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "+ ${uiState.entries.size - 5} more entries today",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }

        // ── Confetti overlay ─────────────────────────────────────────────────
        ConfettiEffect(
            show = uiState.showConfetti,
            onComplete = { viewModel.dismissConfetti() },
            modifier = Modifier.fillMaxSize()
        )

        // ── Reset confirmation dialog ─────────────────────────────────────────
        if (showResetDialog) {
            AlertDialog(
                onDismissRequest = { showResetDialog = false },
                shape = RoundedCornerShape(24.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                title = {
                    Text(
                        "Reset Today?",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        "All water entries for today will be removed. This cannot be undone.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            // HEAVY haptic for destructive action
                            performHaptic(context, HapticType.HEAVY, uiState.hapticEnabled)
                            viewModel.resetDay()
                            showResetDialog = false
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Reset", fontWeight = FontWeight.SemiBold)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            performHaptic(context, HapticType.TICK, uiState.hapticEnabled)
                            showResetDialog = false
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }

        // ── Custom amount dialog ──────────────────────────────────────────────
        if (showCustomDialog) {
            AlertDialog(
                onDismissRequest = {
                    showCustomDialog = false
                    customAmount = ""
                },
                shape = RoundedCornerShape(24.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                title = {
                    Text(
                        "Custom Amount",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column {
                        Text(
                            "Enter the amount in millilitres:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        androidx.compose.material3.OutlinedTextField(
                            value = customAmount,
                            onValueChange = { value ->
                                if (value.all { it.isDigit() } && value.length <= 5) {
                                    customAmount = value
                                }
                            },
                            label = { Text("ml") },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                            )
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val amount = customAmount.toIntOrNull()
                            if (amount != null && amount > 0) {
                                performHaptic(context, HapticType.MEDIUM, uiState.hapticEnabled)
                                viewModel.addWater(amount)
                            }
                            showCustomDialog = false
                            customAmount = ""
                        }
                    ) {
                        Text("Add", fontWeight = FontWeight.SemiBold)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            performHaptic(context, HapticType.TICK, uiState.hapticEnabled)
                            showCustomDialog = false
                            customAmount = ""
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun formatMl(ml: Int): String =
    if (ml >= 1000) String.format("%.1fL", ml / 1000f) else "${ml}ml"

private fun getMotivationalMessage(progress: Float): String = when {
    progress <= 0f -> "Let's start hydrating 💧"
    progress < 0.25f -> "Great start — keep it up!"
    progress < 0.50f -> "You're building momentum 🚀"
    progress < 0.75f -> "Halfway there — stay strong!"
    progress < 1.00f -> "Almost at your goal 🔥"
    else -> "Goal crushed! Incredible 🏆"
}

private fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
