package com.example.hydrotracker.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Water
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hydrotracker.ui.theme.Blue400
import com.example.hydrotracker.ui.theme.Blue500
import com.example.hydrotracker.ui.theme.Cyan400

// Maps each preset volume to a display label and an emoji icon
private data class VolumeOption(
    val ml: Int,
    val label: String,
    val sub: String,
    val emoji: String
)

private val VOLUME_OPTIONS = listOf(
    VolumeOption(250, "250 ml", "small glass", "💧"),
    VolumeOption(500, "500 ml", "standard", "🥤"),
    VolumeOption(750, "750 ml", "large glass", "🫗"),
    VolumeOption(1000, "1 L", "full bottle", "🍶"),
)

@Composable
fun QuickAddButtons(
    amounts: List<Int>,
    onAddWater: (Int) -> Unit,
    onCustomAdd: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.run {
        (red * 0.299f + green * 0.587f + blue * 0.114f) < 0.5f
    }

    val glassBg = if (isDark)
        Color(0xFF1E293B).copy(alpha = 0.55f)
    else
        Color.White.copy(alpha = 0.72f)

    val glassBorder = if (isDark)
        Color.White.copy(alpha = 0.08f)
    else
        Color.White.copy(alpha = 0.60f)

    Column(modifier = modifier.fillMaxWidth()) {

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(horizontal = 20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Preset amounts — match against our VolumeOption list for richer display,
            // fall back to a minimal card for any custom preset amounts in settings.
            items(amounts) { ml ->
                val option = VOLUME_OPTIONS.firstOrNull { it.ml == ml }
                if (option != null) {
                    RichQuickAddCard(
                        option = option,
                        glassBg = glassBg,
                        glassBorder = glassBorder,
                        onClick = { onAddWater(ml) }
                    )
                } else {
                    SimpleQuickAddCard(
                        ml = ml,
                        glassBg = glassBg,
                        glassBorder = glassBorder,
                        onClick = { onAddWater(ml) }
                    )
                }
            }

            // Custom add button
            item {
                CustomAddCard(
                    glassBg = glassBg,
                    glassBorder = glassBorder,
                    onClick = onCustomAdd
                )
            }
        }
    }
}

// ── Rich preset card ──────────────────────────────────────────────────────────

@Composable
private fun RichQuickAddCard(
    option: VolumeOption,
    glassBg: Color,
    glassBorder: Color,
    onClick: () -> Unit
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.91f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "cardScale"
    )

    Box(
        modifier = Modifier
            .scale(scale)
            .width(100.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(glassBg)
            .border(1.dp, glassBorder, RoundedCornerShape(20.dp))
            .clickable {
                onClick()
            }
            .padding(vertical = 16.dp, horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Top shine highlight
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.18f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Emoji icon
            Text(
                text = option.emoji,
                fontSize = 26.sp,
                lineHeight = 28.sp
            )

            // Amount label
            Text(
                text = option.label,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    letterSpacing = (-0.3).sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            // Sub-label
            Text(
                text = option.sub,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.sp,
                    letterSpacing = 0.3.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                fontWeight = FontWeight.Medium
            )

            // Bottom accent bar
            Box(
                modifier = Modifier
                    .width(32.dp)
                    .height(2.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(
                        Brush.horizontalGradient(listOf(Blue400, Cyan400))
                    )
            )
        }
    }
}

// ── Simple fallback card (for non-standard preset amounts) ────────────────────

@Composable
private fun SimpleQuickAddCard(
    ml: Int,
    glassBg: Color,
    glassBorder: Color,
    onClick: () -> Unit
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.91f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "simpleCardScale"
    )

    Box(
        modifier = Modifier
            .scale(scale)
            .width(90.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(glassBg)
            .border(1.dp, glassBorder, RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .padding(vertical = 16.dp, horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                Icons.Outlined.Water,
                contentDescription = null,
                tint = Blue400,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = if (ml >= 1000) "${ml / 1000}L" else "${ml}ml",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    letterSpacing = (-0.3).sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Box(
                modifier = Modifier
                    .width(28.dp)
                    .height(2.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(
                        Brush.horizontalGradient(listOf(Blue400, Cyan400))
                    )
            )
        }
    }
}

// ── Custom add card ───────────────────────────────────────────────────────────

@Composable
private fun CustomAddCard(
    glassBg: Color,
    glassBorder: Color,
    onClick: () -> Unit
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.91f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "customCardScale"
    )

    Box(
        modifier = Modifier
            .scale(scale)
            .width(90.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Blue500.copy(alpha = 0.18f),
                        Blue400.copy(alpha = 0.10f)
                    )
                )
            )
            .border(
                1.dp,
                Blue400.copy(alpha = 0.30f),
                RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 16.dp, horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Circle add icon
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(
                        Brush.radialGradient(listOf(Blue400.copy(alpha = 0.35f), Color.Transparent))
                    )
                    .border(1.dp, Blue400.copy(alpha = 0.45f), RoundedCornerShape(999.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Custom amount",
                    tint = Blue400,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "Custom",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 0.1.sp
                ),
                color = Blue400
            )

            Text(
                text = "any amount",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.sp,
                    letterSpacing = 0.2.sp
                ),
                color = Blue400.copy(alpha = 0.65f),
                fontWeight = FontWeight.Medium
            )
        }
    }
}
