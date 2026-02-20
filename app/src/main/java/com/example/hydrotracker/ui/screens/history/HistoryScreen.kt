package com.example.hydrotracker.ui.screens.history

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hydrotracker.HydroTrackApp
import com.example.hydrotracker.ui.HapticType
import com.example.hydrotracker.ui.performHaptic
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hydrotracker.data.local.DailyTotal
import com.example.hydrotracker.ui.theme.Amber500
import com.example.hydrotracker.ui.theme.Blue400
import com.example.hydrotracker.ui.theme.Blue500
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(
            text = "History",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Stats row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                icon = Icons.Default.LocalFireDepartment,
                label = "Streak",
                value = "${uiState.streak}",
                unit = "days",
                color = Amber500,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                label = "7-Day Avg",
                value = String.format("%.1f", uiState.averageWeekly / 1000f),
                unit = "L/day",
                color = Blue500,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                icon = Icons.Default.EmojiEvents,
                label = "Goals Met",
                value = "${uiState.totalDaysGoalMet}",
                unit = "days",
                color = Green500,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Weekly bar chart
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Last 7 Days",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(16.dp))
                WeeklyBarChart(
                    data = uiState.weeklyData,
                    goalMl = uiState.dailyGoalMl
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Monthly calendar heatmap
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        performHaptic(context, HapticType.STRONG, hapticEnabled)
                        viewModel.previousMonth()
                    }) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Previous month")
                    }
                    Text(
                        text = uiState.selectedMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    IconButton(onClick = {
                        performHaptic(context, HapticType.STRONG, hapticEnabled)
                        viewModel.nextMonth()
                    }) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Next month")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                MonthlyHeatmap(
                    month = uiState.selectedMonth,
                    data = uiState.monthlyData,
                    goalMl = uiState.dailyGoalMl
                )
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
private fun StatCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    unit: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )
            Text(
                text = unit,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

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
            .height(140.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        days.forEach { day ->
            val dateStr = day.format(DateTimeFormatter.ISO_LOCAL_DATE)
            val total = dataMap[dateStr]?.totalMl ?: 0
            val heightFraction = if (maxValue > 0) total.toFloat() / maxValue else 0f
            val isGoalMet = total >= goalMl
            val isToday = day == today

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                // Amount label
                Text(
                    text = if (total > 0) String.format("%.1f", total / 1000f) else "-",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                // Bar
                Box(
                    modifier = Modifier
                        .width(28.dp)
                        .height((heightFraction * 90).dp.coerceAtLeast(4.dp))
                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                        .background(
                            if (isGoalMet) Green500
                            else if (total > 0) Blue400
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                )
                Spacer(modifier = Modifier.height(6.dp))
                // Day label
                Text(
                    text = day.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                    color = if (isToday) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }
        }
    }

    // Goal line label
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.End
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(Green500, CircleShape)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "Goal met",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp
        )
    }
}

@Composable
private fun MonthlyHeatmap(
    month: YearMonth,
    data: List<DailyTotal>,
    goalMl: Int
) {
    val dataMap = data.associateBy { it.date }
    val firstDay = month.atDay(1)
    val daysInMonth = month.lengthOfMonth()
    val startDayOfWeek = firstDay.dayOfWeek.value // Monday=1, Sunday=7
    val today = LocalDate.now()

    // Day of week headers
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
                modifier = Modifier.weight(1f)
            )
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Calendar grid
    var dayCounter = 1
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
                    val intensity = if (goalMl > 0) (total.toFloat() / goalMl).coerceIn(0f, 1f) else 0f
                    val isFuture = date.isAfter(today)

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(2.dp)
                            .size(32.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                when {
                                    isFuture -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                    intensity >= 1f -> Green500
                                    intensity >= 0.75f -> Blue500.copy(alpha = 0.8f)
                                    intensity >= 0.5f -> Blue400.copy(alpha = 0.6f)
                                    intensity >= 0.25f -> Blue400.copy(alpha = 0.3f)
                                    intensity > 0f -> Blue400.copy(alpha = 0.15f)
                                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$dayNum",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = when {
                                isFuture -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                intensity >= 0.75f -> androidx.compose.ui.graphics.Color.White
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
