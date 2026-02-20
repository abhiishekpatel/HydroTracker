package com.example.hydrotracker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Blue500,
    onPrimary = Color.White,
    primaryContainer = Blue100,
    onPrimaryContainer = Blue700,
    secondary = Cyan500,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCFFAFE),
    onSecondaryContainer = Color(0xFF0C4A5E),
    tertiary = Green500,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD1FAE5),
    onTertiaryContainer = Green600,
    error = Red500,
    onError = Color.White,
    errorContainer = Color(0xFFFFE4E6),
    onErrorContainer = Color(0xFF7F1D1D),
    background = Slate50,
    onBackground = Slate900,
    surface = Color.White,
    onSurface = Slate900,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate600,
    outline = Slate300,
    outlineVariant = Slate200,
    inverseSurface = Slate900,
    inverseOnSurface = Slate100,
    inversePrimary = Blue300,
    surfaceContainerHighest = Slate100,
    surfaceContainerHigh = Slate50,
    surfaceContainer = Color.White,
    surfaceContainerLow = Color.White,
    surfaceContainerLowest = Color.White,
)

private val DarkColorScheme = darkColorScheme(
    // Primary — vivid sky blue, readable on dark surfaces
    primary = Blue400,
    onPrimary = DeepNavy700,
    primaryContainer = Blue600,
    onPrimaryContainer = Blue200,

    // Secondary — cyan accent
    secondary = Cyan400,
    onSecondary = Color(0xFF063B4A),
    secondaryContainer = Color(0xFF0C4A5E),
    onSecondaryContainer = Cyan300,

    // Tertiary — emerald (goal met / success)
    tertiary = Green400,
    onTertiary = Color(0xFF003919),
    tertiaryContainer = Green600,
    onTertiaryContainer = Color(0xFFD1FAE5),

    // Error
    error = Red400,
    onError = Color(0xFF7F1D1D),
    errorContainer = Color(0xFF450A0A),
    onErrorContainer = Red400,

    // ── Backgrounds — deep navy hierarchy ─────────────────────────────────────
    // background: the very bottom layer — deepest colour
    background = DeepNavy,
    onBackground = Slate100,

    // surface: cards, sheets, dialogs — one step lighter
    surface = DeepNavy800,
    onSurface = Slate100,

    // surfaceVariant: input fields, chips, non-interactive containers
    surfaceVariant = Slate800,
    onSurfaceVariant = Slate400,

    // outlines
    outline = Slate600,
    outlineVariant = Slate700,

    // inverse (used for snackbars, tooltips)
    inverseSurface = Slate200,
    inverseOnSurface = Slate900,
    inversePrimary = Blue600,

    // ── surfaceContainer family (M3 tonal surface layers) ─────────────────────
    // Lowest → Highest follow deep-navy → slightly lighter progression
    surfaceContainerLowest = DeepNavy,          // same as background
    surfaceContainerLow = DeepNavy800,       // barely lifted
    surfaceContainer = DeepNavy700,       // standard card
    surfaceContainerHigh = Slate800,          // elevated card / modal
    surfaceContainerHighest = Slate700,         // highest elevation
)

@Composable
fun HydroTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
