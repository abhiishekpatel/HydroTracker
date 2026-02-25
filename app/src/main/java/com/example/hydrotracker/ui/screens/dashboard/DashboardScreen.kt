package com.example.hydrotracker.ui.screens.dashboard

import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hydrotracker.data.local.WaterEntry
import com.example.hydrotracker.ui.HapticType
import com.example.hydrotracker.ui.components.ConfettiEffect
import com.example.hydrotracker.ui.performHaptic
import com.example.hydrotracker.ui.theme.HydroBlue
import com.example.hydrotracker.ui.theme.HydroBlueContainer
import com.example.hydrotracker.ui.theme.HydroSuccess
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = viewModel(),
    bottomPadding: Dp = 0.dp,
    onViewAllLogs: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val view = LocalView.current

    var showResetDialog by remember { mutableStateOf(false) }
    var showCustomDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.showConfetti) {
        if (uiState.showConfetti) performHaptic(context, HapticType.SUCCESS, uiState.hapticEnabled)
    }

    val progress = (uiState.currentIntakeMl.toFloat() / uiState.dailyGoalMl).coerceIn(0f, 1f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(bottom = bottomPadding + 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Top header ────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "Welcome back,",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = timeGreeting(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                IconButton(
                    onClick = { performHaptic(context, HapticType.HEAVY, uiState.hapticEnabled) }
                ) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ── Circular progress ring ────────────────────────────────────────
            HydroCircularProgress(
                currentMl = uiState.currentIntakeMl,
                goalMl = uiState.dailyGoalMl,
                progress = progress,
                goalMet = uiState.currentIntakeMl >= uiState.dailyGoalMl
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── Motivational text ─────────────────────────────────────────────
            Text(
                text = motivationalMessage(progress),
                style = MaterialTheme.typography.bodyMedium,
                color = HydroBlue,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // ── Quick Add section ─────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Text(
                    text = "Quick Add",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickAddCard(
                        amount = 250,
                        isHighlighted = false,
                        modifier = Modifier
                            .weight(1f)
                            .height(96.dp),
                        onClick = {
                            performHaptic(context, HapticType.HEAVY, uiState.hapticEnabled)
                            viewModel.addWater(250)
                        }
                    )
                    QuickAddCard(
                        amount = 500,
                        isHighlighted = true,
                        modifier = Modifier
                            .weight(1f)
                            .height(96.dp),
                        onClick = {
                            performHaptic(context, HapticType.HEAVY, uiState.hapticEnabled)
                            viewModel.addWater(500)
                        }
                    )
                    CustomAddCard(
                        modifier = Modifier
                            .weight(1f)
                            .height(96.dp),
                        onClick = {
                            performHaptic(context, HapticType.HEAVY, uiState.hapticEnabled)
                            showCustomDialog = true
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ── Daily Log section ─────────────────────────────────────────────
            AnimatedVisibility(
                visible = uiState.entries.isNotEmpty(),
                enter = fadeIn() + slideInVertically { it / 2 }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Daily Log",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        TextButton(onClick = {
                            performHaptic(context, HapticType.HEAVY, uiState.hapticEnabled)
                            onViewAllLogs()
                        }) {
                            Text(
                                text = "View All",
                                style = MaterialTheme.typography.bodySmall,
                                color = HydroBlue,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        uiState.entries.take(4).forEach { entry ->
                            LogEntryCard(entry)
                        }
                    }

                    if (uiState.entries.size > 4) {
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = {
                                performHaptic(context, HapticType.HEAVY, uiState.hapticEnabled)
                                viewModel.undoLastEntry()
                            },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text(
                                "Undo last entry",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else if (uiState.entries.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        TextButton(
                            onClick = {
                                performHaptic(context, HapticType.HEAVY, uiState.hapticEnabled)
                                viewModel.undoLastEntry()
                            },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text(
                                "Undo last entry",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // ── Confetti ──────────────────────────────────────────────────────────
        ConfettiEffect(
            show = uiState.showConfetti,
            onComplete = { viewModel.dismissConfetti() },
            modifier = Modifier.fillMaxSize()
        )

        // ── Reset dialog ──────────────────────────────────────────────────────
        if (showResetDialog) {
            AlertDialog(
                onDismissRequest = { showResetDialog = false },
                shape = RoundedCornerShape(20.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                title = {
                    Text(
                        "Reset today?",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                text = {
                    Text(
                        "All water entries for today will be cleared.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            performHaptic(context, HapticType.HEAVY, uiState.hapticEnabled)
                            viewModel.resetDay()
                            showResetDialog = false
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Reset", fontWeight = FontWeight.SemiBold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        performHaptic(context, HapticType.HEAVY, uiState.hapticEnabled)
                        showResetDialog = false
                    }) {
                        Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            )
        }

        // ── Custom amount dialog ──────────────────────────────────────────────
        if (showCustomDialog) {
            val stepMl = 50
            val minMl = 50
            val maxMl = 1500
            val steps = (maxMl - minMl) / stepMl - 1

            var sliderValue by remember { mutableFloatStateOf(250f) }
            var lastHapticStep by remember { mutableIntStateOf(250 / stepMl) }

            AlertDialog(
                onDismissRequest = {
                    showCustomDialog = false
                    sliderValue = 250f
                },
                shape = RoundedCornerShape(20.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                title = {
                    Text(
                        "Custom Amount",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = fmtMl(sliderValue.toInt()),
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-1.5).sp
                            ),
                            color = HydroBlue,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "drag to set amount",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(18.dp))

                        Slider(
                            value = sliderValue,
                            onValueChange = { raw ->
                                val snapped = (Math.round(raw.toDouble() / stepMl) * stepMl)
                                    .coerceIn(minMl.toLong(), maxMl.toLong()).toFloat()
                                sliderValue = snapped
                                val step = (snapped / stepMl).toInt()
                                if (step != lastHapticStep) {
                                    lastHapticStep = step
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        view.performHapticFeedback(
                                            HapticFeedbackConstants.CLOCK_TICK,
                                            HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
                                        )
                                    } else {
                                        view.performHapticFeedback(
                                            HapticFeedbackConstants.VIRTUAL_KEY,
                                            HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
                                        )
                                    }
                                }
                            },
                            valueRange = minMl.toFloat()..maxMl.toFloat(),
                            steps = steps,
                            colors = SliderDefaults.colors(
                                thumbColor = HydroBlue,
                                activeTrackColor = HydroBlue,
                                inactiveTrackColor = MaterialTheme.colorScheme.outline,
                                activeTickColor = Color.Transparent,
                                inactiveTickColor = Color.Transparent
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                fmtMl(minMl),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                fmtMl(maxMl),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val amount = sliderValue.toInt()
                            if (amount > 0) {
                                performHaptic(context, HapticType.HEAVY, uiState.hapticEnabled)
                                viewModel.addWater(amount)
                            }
                            showCustomDialog = false
                            sliderValue = 250f
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = HydroBlue)
                    ) {
                        Text("Add ${fmtMl(sliderValue.toInt())}", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        performHaptic(context, HapticType.HEAVY, uiState.hapticEnabled)
                        showCustomDialog = false
                        sliderValue = 250f
                    }) {
                        Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Circular progress ring composable
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun HydroCircularProgress(
    currentMl: Int,
    goalMl: Int,
    progress: Float,
    goalMet: Boolean,
    modifier: Modifier = Modifier
) {
    val animProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "arcProgress"
    )
    val arcColor = if (goalMet) HydroSuccess else HydroBlue
    val trackColor = MaterialTheme.colorScheme.outlineVariant

    Box(
        modifier = modifier.size(240.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 18.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val topLeft = Offset(
                (size.width - diameter) / 2f,
                (size.height - diameter) / 2f
            )
            val arcSize = Size(diameter, diameter)

            // Track
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            // Progress arc
            if (animProgress > 0.005f) {
                drawArc(
                    color = arcColor,
                    startAngle = -90f,
                    sweepAngle = 360f * animProgress,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.WaterDrop,
                contentDescription = null,
                tint = arcColor,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "$currentMl",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    letterSpacing = (-1).sp
                )
                Text(
                    text = "ml",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 7.dp, start = 3.dp)
                )
            }
            Text(
                text = "of ${goalMl}ml goal",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Quick Add cards
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun QuickAddCard(
    amount: Int,
    isHighlighted: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isHighlighted) HydroBlue else MaterialTheme.colorScheme.surface
    val contentColor = if (isHighlighted) Color.White else MaterialTheme.colorScheme.onSurface
    val iconTint = if (isHighlighted) Color.White else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = bgColor,
        shadowElevation = if (isHighlighted) 4.dp else 1.dp,
        border = if (!isHighlighted) BorderStroke(1.dp, MaterialTheme.colorScheme.outline) else null,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.LocalCafe,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "${amount}ml",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.SemiBold,
                color = contentColor
            )
        }
    }
}

@Composable
private fun CustomAddCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Custom",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Log entry card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun LogEntryCard(entry: WaterEntry) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Blue circle icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(HydroBlueContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.WaterDrop,
                    contentDescription = null,
                    tint = HydroBlue,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = drinkName(entry.amountMl),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = drinkLabel(entry.amountMl),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "+${fmtMl(entry.amountMl)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = HydroBlue
                )
                Text(
                    text = fmtTime(entry.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Helpers
// ─────────────────────────────────────────────────────────────────────────────

private fun timeGreeting(): String {
    val hour = LocalTime.now().hour
    return when (hour) {
        in 5..11 -> "Good Morning!"
        in 12..16 -> "Good Afternoon!"
        else -> "Good Evening!"
    }
}

private fun fmtMl(ml: Int): String =
    if (ml >= 1000) String.format("%.1fL", ml / 1000f) else "${ml}ml"

private fun fmtTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun drinkName(amountMl: Int): String = when {
    amountMl <= 200 -> "Small Sip"
    amountMl <= 350 -> "Water"
    amountMl <= 600 -> "Water"
    amountMl <= 900 -> "Tea"
    else -> "Water Bottle"
}

private fun drinkLabel(amountMl: Int): String = when {
    amountMl <= 200 -> "Quick hydration"
    amountMl <= 350 -> "Hydration boost"
    amountMl <= 600 -> "Regular drink"
    amountMl <= 900 -> "Morning routine"
    else -> "Super hydration"
}

private fun motivationalMessage(progress: Float): String = when {
    progress <= 0f -> "Start your hydration journey!"
    progress < 0.25f -> "Every sip counts, keep going!"
    progress < 0.50f -> "Keep drinking! You're doing great."
    progress < 0.75f -> "More than halfway, nice work!"
    progress < 1.00f -> "Almost at your goal, finish strong!"
    else -> "Goal crushed! Amazing work today!"
}
