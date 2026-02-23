package com.example.hydrotracker.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Opacity
import androidx.compose.material.icons.outlined.Water
import androidx.compose.material.icons.outlined.WaterDrop
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hydrotracker.ui.theme.Crystal400
import com.example.hydrotracker.ui.theme.IceBlue300
import com.example.hydrotracker.ui.theme.IceBlue400
import com.example.hydrotracker.ui.theme.IceBlue500
import com.example.hydrotracker.ui.theme.Violet400

// ── Volume options — icons only, no emojis ────────────────────────────────────

private data class VolumeOption(
    val ml: Int,
    val label: String,
    val sub: String,
    val icon: ImageVector
)

private val VOLUME_OPTIONS = listOf(
    VolumeOption(250, "250 ml", "small", Icons.Outlined.WaterDrop),
    VolumeOption(500, "500 ml", "standard", Icons.Outlined.Opacity),
    VolumeOption(750, "750 ml", "large", Icons.Outlined.Water),
    VolumeOption(1000, "1 L", "bottle", Icons.Outlined.Water),
)

// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun QuickAddButtons(
    amounts: List<Int>,
    onAddWater: (Int) -> Unit,
    onCustomAdd: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(horizontal = 20.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        items(amounts) { ml ->
            val option = VOLUME_OPTIONS.firstOrNull { it.ml == ml }
            if (option != null) {
                PresetCard(option = option, onClick = { onAddWater(ml) })
            } else {
                SimpleCard(ml = ml, onClick = { onAddWater(ml) })
            }
        }

        item {
            CustomCard(onClick = onCustomAdd)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Shared pressable modifier
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun pressModifier(onClick: () -> Unit): Modifier {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.89f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "cardScale"
    )
    return Modifier
        .scale(scale)
        .pointerInput(onClick) {
            detectTapGestures(
                onPress = {
                    pressed = true
                    tryAwaitRelease()
                    pressed = false
                    onClick()
                }
            )
        }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Card variants
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PresetCard(option: VolumeOption, onClick: () -> Unit) {
    Box(
        modifier = pressModifier(onClick)
            .width(96.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.055f),
                        Color.White.copy(alpha = 0.028f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.14f),
                        Color.White.copy(alpha = 0.04f)
                    )
                ),
                shape = RoundedCornerShape(18.dp)
            )
            .padding(vertical = 16.dp, horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Icon in a tiny glowing pill
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(IceBlue500.copy(alpha = 0.15f))
                    .border(1.dp, IceBlue400.copy(alpha = 0.25f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = option.icon,
                    contentDescription = null,
                    tint = IceBlue300,
                    modifier = Modifier.size(16.dp)
                )
            }

            Text(
                text = option.label,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    letterSpacing = (-0.3).sp
                ),
                color = Color.White.copy(alpha = 0.90f)
            )

            Text(
                text = option.sub,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.sp,
                    letterSpacing = 0.4.sp
                ),
                color = Color.White.copy(alpha = 0.35f),
                fontWeight = FontWeight.Medium
            )

            // Accent bar
            Box(
                modifier = Modifier
                    .width(28.dp)
                    .height(1.5.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(IceBlue400.copy(alpha = 0.0f), IceBlue400, IceBlue400.copy(alpha = 0.0f))
                        )
                    )
            )
        }
    }
}

@Composable
private fun SimpleCard(ml: Int, onClick: () -> Unit) {
    Box(
        modifier = pressModifier(onClick)
            .width(90.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.04f))
            .border(1.dp, Color.White.copy(alpha = 0.09f), RoundedCornerShape(18.dp))
            .padding(vertical = 16.dp, horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.WaterDrop,
                contentDescription = null,
                tint = IceBlue400,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = if (ml >= 1000) "${ml / 1000}L" else "${ml}ml",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                ),
                color = Color.White.copy(alpha = 0.88f)
            )
        }
    }
}

@Composable
private fun CustomCard(onClick: () -> Unit) {
    Box(
        modifier = pressModifier(onClick)
            .width(90.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        IceBlue500.copy(alpha = 0.18f),
                        Violet400.copy(alpha = 0.10f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        IceBlue400.copy(alpha = 0.40f),
                        Violet400.copy(alpha = 0.20f)
                    )
                ),
                shape = RoundedCornerShape(18.dp)
            )
            .padding(vertical = 16.dp, horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(IceBlue500.copy(alpha = 0.22f))
                    .border(1.dp, IceBlue400.copy(alpha = 0.45f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = "Custom amount",
                    tint = IceBlue400,
                    modifier = Modifier.size(17.dp)
                )
            }

            Text(
                text = "Custom",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 0.1.sp
                ),
                color = IceBlue400
            )

            Text(
                text = "any amount",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.sp,
                    letterSpacing = 0.3.sp
                ),
                color = IceBlue400.copy(alpha = 0.55f),
                fontWeight = FontWeight.Medium
            )
        }
    }
}
