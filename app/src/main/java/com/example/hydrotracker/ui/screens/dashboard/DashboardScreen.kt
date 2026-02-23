package com.example.hydrotracker.ui.screens.dashboard

import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hydrotracker.ui.HapticType
import com.example.hydrotracker.ui.components.ConfettiEffect
import com.example.hydrotracker.ui.components.QuickAddButtons
import com.example.hydrotracker.ui.components.WaterProgressRing
import com.example.hydrotracker.ui.components.WaveBackground
import com.example.hydrotracker.ui.components.rememberGyroTilt
import com.example.hydrotracker.ui.performHaptic
import com.example.hydrotracker.ui.theme.Amber400
import com.example.hydrotracker.ui.theme.Crystal400
import com.example.hydrotracker.ui.theme.Crystal500
import com.example.hydrotracker.ui.theme.IceBlue300
import com.example.hydrotracker.ui.theme.IceBlue400
import com.example.hydrotracker.ui.theme.IceBlue500
import com.example.hydrotracker.ui.theme.Violet400
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val view = LocalView.current

    var showResetDialog by remember { mutableStateOf(false) }
    var showCustomDialog by remember { mutableStateOf(false) }

    val gyroTilt by rememberGyroTilt()

    LaunchedEffect(uiState.showConfetti) {
        if (uiState.showConfetti) performHaptic(context, HapticType.SUCCESS, uiState.hapticEnabled)
    }

    val progress = (uiState.currentIntakeMl.toFloat() / uiState.dailyGoalMl).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "progress",
    )

    Box(modifier = Modifier.fillMaxSize()) {

        // ── Layer 1: Physics water background ─────────────────────────────────
        WaveBackground(
            progress = progress,
            gyroTilt = gyroTilt,
            modifier = Modifier.fillMaxSize(),
        )

        // ── Layer 2: Scrollable content ───────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            Spacer(modifier = Modifier.height(20.dp))

            // ── Top bar ───────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Left: streak pill or wordmark
                if (uiState.streak > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Amber400.copy(alpha = 0.10f))
                            .border(1.dp, Amber400.copy(alpha = 0.22f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                    ) {
                        Icon(
                            Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = Amber400,
                            modifier = Modifier.size(13.dp),
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "${uiState.streak} day streak",
                            style = MaterialTheme.typography.labelMedium,
                            color = Amber400,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                } else {
                    Text(
                        text = "HYDROTRACK",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            letterSpacing = 2.2.sp,
                        ),
                        color = Color.White.copy(alpha = 0.22f),
                        fontWeight = FontWeight.Bold,
                    )
                }

                // Right: icon action buttons
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DashIconButton(onClick = {
                        performHaptic(context, HapticType.TICK, uiState.hapticEnabled)
                        viewModel.undoLastEntry()
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.Undo,
                            contentDescription = "Undo",
                            tint = Color.White.copy(alpha = 0.50f),
                            modifier = Modifier.size(17.dp),
                        )
                    }
                    DashIconButton(onClick = {
                        performHaptic(context, HapticType.TICK, uiState.hapticEnabled)
                        showResetDialog = true
                    }) {
                        Icon(
                            Icons.Outlined.RestartAlt,
                            contentDescription = "Reset",
                            tint = Color.White.copy(alpha = 0.50f),
                            modifier = Modifier.size(17.dp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // ── Hero orb ──────────────────────────────────────────────────────
            WaterProgressRing(
                currentMl = uiState.currentIntakeMl,
                goalMl = uiState.dailyGoalMl,
                gyroTilt = gyroTilt,
                ringSize = 264.dp,
            )

            Spacer(modifier = Modifier.height(22.dp))

            // ── Motivational text ─────────────────────────────────────────────
            Text(
                text = motivationalMessage(progress),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.40f),
                fontWeight = FontWeight.Medium,
            )

            Spacer(modifier = Modifier.height(10.dp))

            // ── Pacing badge ──────────────────────────────────────────────────
            val pacingColor = when (uiState.pacingStatus) {
                PacingStatus.AHEAD, PacingStatus.COMPLETED -> Crystal400
                PacingStatus.ON_TRACK -> IceBlue400
                PacingStatus.BEHIND -> Amber400
            }
            val pacingLabel = when (uiState.pacingStatus) {
                PacingStatus.AHEAD -> "Ahead of schedule"
                PacingStatus.ON_TRACK -> "On track"
                PacingStatus.BEHIND -> "Behind schedule"
                PacingStatus.COMPLETED -> "Daily goal complete"
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(pacingColor.copy(alpha = 0.08f))
                    .border(1.dp, pacingColor.copy(alpha = 0.18f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 11.dp, vertical = 5.dp),
            ) {
                Icon(
                    Icons.Outlined.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(11.dp),
                    tint = pacingColor,
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = pacingLabel,
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.2.sp),
                    fontWeight = FontWeight.SemiBold,
                    color = pacingColor,
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            // ── Stats + progress — single unified glass card ──────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.White.copy(alpha = 0.055f),
                                Color.White.copy(alpha = 0.030f),
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            listOf(
                                Color.White.copy(alpha = 0.12f),
                                Color.White.copy(alpha = 0.04f),
                            )
                        ),
                        shape = RoundedCornerShape(22.dp),
                    )
                    .padding(horizontal = 20.dp, vertical = 18.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {

                    // Consumed / Remaining row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        StatColumn(
                            label = "CONSUMED",
                            value = fmtMl(uiState.currentIntakeMl),
                            valueColor = if (uiState.currentIntakeMl >= uiState.dailyGoalMl)
                                Crystal400 else IceBlue400,
                        )

                        // Thin vertical rule
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(36.dp)
                                .background(Color.White.copy(alpha = 0.08f))
                        )

                        StatColumn(
                            label = "REMAINING",
                            value = fmtMl(
                                (uiState.dailyGoalMl - uiState.currentIntakeMl).coerceAtLeast(0)
                            ),
                            valueColor = Color.White.copy(alpha = 0.70f),
                            align = Alignment.End,
                        )
                    }

                    // Thin separator
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color.White.copy(alpha = 0.06f))
                    )

                    // Progress bar row
                    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = "DAILY GOAL",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    letterSpacing = 1.1.sp,
                                ),
                                color = Color.White.copy(alpha = 0.28f),
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = "${(progress * 100).toInt()}% · ${fmtMl(uiState.dailyGoalMl)}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    letterSpacing = 0.2.sp,
                                ),
                                color = if (uiState.currentIntakeMl >= uiState.dailyGoalMl)
                                    Crystal400 else IceBlue400,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                        // Track
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(Color.White.copy(alpha = 0.07f)),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(animatedProgress)
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(
                                        if (uiState.currentIntakeMl >= uiState.dailyGoalMl)
                                            Brush.horizontalGradient(listOf(Crystal400, Crystal500))
                                        else
                                            Brush.horizontalGradient(listOf(IceBlue400, Violet400))
                                    ),
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // ── Add water label + buttons ─────────────────────────────────────
            SectionLabel("ADD WATER")
            Spacer(modifier = Modifier.height(14.dp))

            QuickAddButtons(
                amounts = uiState.quickAddAmounts,
                onAddWater = { amount ->
                    performHaptic(context, HapticType.MEDIUM, uiState.hapticEnabled)
                    viewModel.addWater(amount)
                },
                onCustomAdd = {
                    performHaptic(context, HapticType.CLICK, uiState.hapticEnabled)
                    showCustomDialog = true
                },
            )

            Spacer(modifier = Modifier.height(30.dp))

            // ── Today's log ───────────────────────────────────────────────────
            AnimatedVisibility(
                visible = uiState.entries.isNotEmpty(),
                enter = fadeIn() + slideInVertically { it / 2 },
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                ) {
                    SectionLabel("TODAY")
                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color.White.copy(alpha = 0.035f))
                            .border(
                                1.dp,
                                Color.White.copy(alpha = 0.07f),
                                RoundedCornerShape(18.dp),
                            ),
                    ) {
                        Column {
                            uiState.entries.take(5).forEachIndexed { idx, entry ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 13.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(7.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    Brush.radialGradient(
                                                        listOf(IceBlue300, IceBlue500)
                                                    )
                                                )
                                        )
                                        Text(
                                            text = "+${fmtMl(entry.amountMl)}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = IceBlue400,
                                        )
                                    }
                                    Text(
                                        text = fmtTime(entry.timestamp),
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 11.sp
                                        ),
                                        color = Color.White.copy(alpha = 0.28f),
                                    )
                                }
                                if (idx < (uiState.entries.take(5).size - 1)) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp)
                                            .height(1.dp)
                                            .background(Color.White.copy(alpha = 0.05f))
                                    )
                                }
                            }
                            if (uiState.entries.size > 5) {
                                Text(
                                    text = "+${uiState.entries.size - 5} more entries",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp
                                    ),
                                    color = Color.White.copy(alpha = 0.22f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 12.dp),
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(90.dp))
        }

        // ── Confetti ──────────────────────────────────────────────────────────
        ConfettiEffect(
            show = uiState.showConfetti,
            onComplete = { viewModel.dismissConfetti() },
            modifier = Modifier.fillMaxSize(),
        )

        // ── Reset dialog ──────────────────────────────────────────────────────
        if (showResetDialog) {
            AlertDialog(
                onDismissRequest = { showResetDialog = false },
                shape = RoundedCornerShape(24.dp),
                containerColor = Color(0xFF0F1B2D),
                title = {
                    Text(
                        "Reset today?",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.90f),
                    )
                },
                text = {
                    Text(
                        "All water entries for today will be cleared.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.45f),
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            performHaptic(context, HapticType.HEAVY, uiState.hapticEnabled)
                            viewModel.resetDay()
                            showResetDialog = false
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error,
                        ),
                    ) {
                        Text("Reset", fontWeight = FontWeight.SemiBold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        performHaptic(context, HapticType.TICK, uiState.hapticEnabled)
                        showResetDialog = false
                    }) {
                        Text("Cancel", color = Color.White.copy(alpha = 0.40f))
                    }
                },
            )
        }

        // ── Custom amount dialog with haptic-per-step slider ──────────────────
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
                shape = RoundedCornerShape(24.dp),
                containerColor = Color(0xFF0F1B2D),
                title = {
                    Text(
                        "Custom Amount",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.90f),
                    )
                },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = fmtMl(sliderValue.toInt()),
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-1.5).sp,
                            ),
                            color = IceBlue400,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "drag to set amount",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.28f),
                        )
                        Spacer(modifier = Modifier.height(18.dp))

                        // Slider with per-step haptic via View.performHapticFeedback —
                        // the only reliable cross-device haptic inside onValueChange.
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
                                            HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING,
                                        )
                                    } else {
                                        view.performHapticFeedback(
                                            HapticFeedbackConstants.VIRTUAL_KEY,
                                            HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING,
                                        )
                                    }
                                }
                            },
                            valueRange = minMl.toFloat()..maxMl.toFloat(),
                            steps = steps,
                            colors = SliderDefaults.colors(
                                thumbColor = IceBlue400,
                                activeTrackColor = IceBlue400,
                                inactiveTrackColor = Color.White.copy(alpha = 0.08f),
                                activeTickColor = Color.Transparent,
                                inactiveTickColor = Color.Transparent,
                            ),
                            modifier = Modifier.fillMaxWidth(),
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                fmtMl(minMl),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.22f),
                            )
                            Text(
                                fmtMl(maxMl),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.22f),
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val amount = sliderValue.toInt()
                            if (amount > 0) {
                                performHaptic(context, HapticType.MEDIUM, uiState.hapticEnabled)
                                viewModel.addWater(amount)
                            }
                            showCustomDialog = false
                            sliderValue = 250f
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = IceBlue400),
                    ) {
                        Text("Add ${fmtMl(sliderValue.toInt())}", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        performHaptic(context, HapticType.TICK, uiState.hapticEnabled)
                        showCustomDialog = false
                        sliderValue = 250f
                    }) {
                        Text("Cancel", color = Color.White.copy(alpha = 0.38f))
                    }
                },
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Reusable sub-components
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun DashIconButton(
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.86f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "btnScale",
    )
    Box(
        modifier = Modifier
            .scale(scale)
            .size(36.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.05f))
            .border(1.dp, Color.White.copy(alpha = 0.09f), CircleShape)
            .pointerInput(onClick) {
                detectTapGestures(
                    onPress = {
                        pressed = true
                        tryAwaitRelease()
                        pressed = false
                        onClick()
                    }
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
private fun StatColumn(
    label: String,
    value: String,
    valueColor: Color,
    align: Alignment.Horizontal = Alignment.Start,
) {
    Column(horizontalAlignment = align) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 9.sp,
                letterSpacing = 1.1.sp,
            ),
            color = Color.White.copy(alpha = 0.28f),
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp,
            ),
            color = valueColor,
        )
    }
}

@Composable
private fun SectionLabel(label: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color.Transparent, Color.White.copy(alpha = 0.09f))
                    )
                )
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 9.sp,
                letterSpacing = 1.6.sp,
            ),
            color = Color.White.copy(alpha = 0.25f),
            fontWeight = FontWeight.Bold,
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color.White.copy(alpha = 0.09f), Color.Transparent)
                    )
                )
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Pure helpers
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Formats a millilitre value for display.
 * Values >= 1000 ml are shown as litres with one decimal place (e.g. "1.5L").
 * Values < 1000 ml are shown as whole millilitres (e.g. "250ml").
 */
private fun fmtMl(ml: Int): String =
    if (ml >= 1000) String.format("%.1fL", ml / 1000f)
    else "${ml}ml"

/**
 * Formats a Unix-millisecond timestamp as a human-readable time string,
 * e.g. "9:42 AM".
 */
private fun fmtTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

/**
 * Returns a short motivational message based on the current hydration progress.
 */
private fun motivationalMessage(progress: Float): String = when {
    progress <= 0f    -> "Start your hydration journey 💧"
    progress < 0.25f  -> "Every sip counts, keep going!"
    progress < 0.50f  -> "You're building great habits!"
    progress < 0.75f  -> "More than halfway there, nice work!"
    progress < 1.00f  -> "Almost at your goal, finish strong!"
    else              -> "Goal crushed! Amazing work today 🎉"
}