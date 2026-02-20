package com.example.hydrotracker.ui.screens.history

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hydrotracker.HydroTrackApp
import com.example.hydrotracker.data.local.DailyTotal
import com.example.hydrotracker.ui.HapticType
import com.example.hydrotracker.ui.performHaptic
import com.example.hydrotracker.ui.theme.Amber500
import com.example.hydrotracker.ui.theme.Blue400
import com.example.hydrotracker.ui.theme.Blue500
import com.example.hydrotracker.ui.theme.Cyan400
import com.example.hydrotracker.ui.theme.Green400
import com.example.hydrotracker.ui.theme.Green500
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val hapticEnabled by (context.applicationContext as HydroTrackApp)
        .settingsDataStore.hapticEnabled
        .collectAsStateWithLifecycle(initialValue = true)

    val isDark = MaterialTheme.colorScheme.background.run {
        (red * 0.299f + green * 0.587f + blue * 0.114f) < 0.5f
    }
    val glassBg = if (isDark) Color(0xFF1E293B).copy(alpha = 0.55f) else Color.White.copy(alpha = 0.72f)
    val glassBorder = if (isDark) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.60f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 20.dp, bottom = 96.dp)
    ) {
        // ── Page heading ──────────────────────────────────────────────────────
        Text(
            text = "History",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Your hydration journey",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp, bottom = 20.dp)
        )

        // ── Stat cards row ────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                icon = Icons.Default.LocalFireDepartment,
                label = "Streak",
                value = "${uiState.streak}",
                unit = "days",
                accentColor = Amber500,
                glassBg = glassBg,
                glassBorder = glassBorder,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                label = "7-Day Avg",
                value = String.format("%.1f", uiState.averageWeekly / 1000f),
                unit = "L / day",
                accentColor = Blue500,
                glassBg = glassBg,
                glassBorder = glassBorder,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                icon = Icons.Default.EmojiEvents,
                label = "Goals Met",
                value = "${uiState.totalDaysGoalMet}",
                unit = "days",
                accentColor = Green500,
                glassBg = glassBg,
                glassBorder = glassBorder,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Weekly bar chart card ─────────────────────────────────────────────
        GlassCard(glassBg = glassBg, glassBorder = glassBorder) {
            Column(modifier = Modifier.padding(18.dp)) {
                SectionLabel(
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    title = "Last 7 Days"
                )
                Spacer(modifier = Modifier.height(18.dp))
                WeeklyBarChart(
                    data = uiState.weeklyData,
                    goalMl = uiState.dailyGoalMl
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Monthly heatmap card ──────────────────────────────────────────────
        GlassCard(glassBg = glassBg, glassBorder = glassBorder) {
            Column(modifier = Modifier.padding(18.dp)) {
                // Header row with month navigation
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Prev month
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                            .clickable {
                                // TICK haptic — lightweight calendar nav
                                performHaptic(context, HapticType.TICK, hapticEnabled)
                                viewModel.previousMonth()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.ChevronLeft,
                            contentDescription = "Previous month",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = uiState.selectedMonth.format(
                                DateTimeFormatter.ofPattern("MMMM yyyy")
                            ),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Next month
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                            .clickable {
                                performHaptic(context, HapticType.TICK, hapticEnabled)
                                viewModel.nextMonth()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = "Next month",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                MonthlyHeatmap(
                    month = uiState.selectedMonth,
                    data = uiState.monthlyData,
                    goalMl = uiState.dailyGoalMl
                )

                // Legend
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LegendItem(color = Green500, label = "Goal met")
                    LegendItem(color = Blue400, label = "Partial")
                    LegendItem(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        label = "None"
                    )
                }
            }
        }
    }
}

// ── Reusable glass card wrapper ───────────────────────────────────────────────

@Composable
private fun GlassCard(
    glassBg: Color,
    glassBorder: Color,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(glassBg)
            .border(1.dp, glassBorder, RoundedCornerShape(20.dp))
    ) {
        content()
    }
}

// ── Stat card ─────────────────────────────────────────────────────────────────

@Composable
private fun StatCard(
    icon: ImageVector,
    label: String,
    value: String,
    unit: String,
    accentColor: Color,
    glassBg: Color,
    glassBorder: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(glassBg)
            .border(1.dp, glassBorder, RoundedCornerShape(18.dp))
            .padding(vertical = 14.dp, horizontal = 10.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Icon pill
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accentColor.copy(alpha = 0.14f))
                    .border(1.dp, accentColor.copy(alpha = 0.25f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = accentColor
            )
            Text(
                text = unit,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 14.sp
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.4.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ── Section label with icon ───────────────────────────────────────────────────

@Composable
private fun SectionLabel(icon: ImageVector, title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ── Legend item ───────────────────────────────────────────────────────────────

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp
        )
    }
}

// ── Weekly bar chart ──────────────────────────────────────────────────────────

@Composable
private fun WeeklyBarChart(
    data: List<DailyTotal>,
    goalMl: Int
) {
    val today = LocalDate.now()
    val days = (0..6).map { today.minusDays((6 - it).toLong()) }
    val dataMap = data.associateBy { it.date }
    val maxValue = (data.maxOfOrNull { it.totalMl } ?: goalMl).coerceAtLeast(goalMl)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        days.forEach { day ->
            val dateStr = day.format(DateTimeFormatter.ISO_LOCAL_DATE)
            val total = dataMap[dateStr]?.totalMl ?: 0
            val heightFraction by animateFloatAsState(
                targetValue = if (maxValue > 0) total.toFloat() / maxValue else 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "barHeight"
            )
            val isGoalMet = total >= goalMl
            val isToday = day == today

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                // Amount label
                Text(
                    text = if (total > 0) String.format("%.1f", total / 1000f) else "",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 9.sp
                )
                Spacer(modifier = Modifier.height(4.dp))

                // Bar
                Box(
                    modifier = Modifier
                        .width(26.dp)
                        .height((heightFraction * 96).dp.coerceAtLeast(if (total > 0) 4.dp else 0.dp))
                        .clip(RoundedCornerShape(topStart = 7.dp, topEnd = 7.dp))
                        .background(
                            brush = when {
                                isGoalMet -> Brush.verticalGradient(listOf(Green400, Green500))
                                total > 0 -> Brush.verticalGradient(listOf(Blue400, Cyan400))
                                else -> Brush.verticalGradient(
                                    listOf(
                                        Color.Transparent,
                                        Color.Transparent
                                    )
                                )
                            }
                        )
                        .then(
                            if (total == 0) Modifier.background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ) else Modifier
                        )
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Today indicator dot
                if (isToday) {
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                } else {
                    Spacer(modifier = Modifier.height(6.dp))
                }

                // Day label
                Text(
                    text = day.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                        .take(1),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                    color = if (isToday) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }
        }
    }
}

// ── Monthly heatmap ───────────────────────────────────────────────────────────

@Composable
private fun MonthlyHeatmap(
    month: YearMonth,
    data: List<DailyTotal>,
    goalMl: Int
) {
    val dataMap = data.associateBy { it.date }
    val firstDay = month.atDay(1)
    val daysInMonth = month.lengthOfMonth()
    // Monday = 1 … Sunday = 7
    val startDayOfWeek = firstDay.dayOfWeek.value
    val today = LocalDate.now()

    // Day-of-week header
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        listOf("M", "T", "W", "T", "F", "S", "S").forEach { day ->
            Text(
                text = day,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
                fontSize = 11.sp
            )
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    val totalSlots = startDayOfWeek - 1 + daysInMonth
    val weeks = (totalSlots + 6) / 7

    for (week in 0 until weeks) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            for (dayOfWeek in 1..7) {
                val slotIndex = week * 7 + dayOfWeek
                val dayNum = slotIndex - (startDayOfWeek - 1)

                if (dayNum in 1..daysInMonth) {
                    val date = month.atDay(dayNum)
                    val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
                    val total = dataMap[dateStr]?.totalMl ?: 0
                    val intensity =
                        if (goalMl > 0) (total.toFloat() / goalMl).coerceIn(0f, 1f) else 0f
                    val isFuture = date.isAfter(today)
                    val isToday = date == today

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(2.dp)
                            .size(30.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(
                                when {
                                    isFuture -> MaterialTheme.colorScheme.surfaceVariant.copy(
                                        alpha = 0.25f
                                    )

                                    intensity >= 1f -> Green500
                                    intensity >= 0.75f -> Blue500.copy(alpha = 0.85f)
                                    intensity >= 0.5f -> Blue400.copy(alpha = 0.60f)
                                    intensity >= 0.25f -> Blue400.copy(alpha = 0.35f)
                                    intensity > 0f -> Blue400.copy(alpha = 0.18f)
                                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(
                                        alpha = 0.40f
                                    )
                                }
                            )
                            .then(
                                if (isToday) Modifier.border(
                                    1.5.dp,
                                    MaterialTheme.colorScheme.primary,
                                    RoundedCornerShape(7.dp)
                                ) else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$dayNum",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                            color = when {
                                isFuture -> MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                    alpha = 0.35f
                                )

                                intensity >= 0.75f -> Color.White
                                isToday -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
        if (week < weeks - 1) {
            Spacer(modifier = Modifier.height(2.dp))
        }
    }
}
