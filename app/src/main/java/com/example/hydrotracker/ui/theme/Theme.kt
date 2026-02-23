package com.example.hydrotracker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    // Primary — ice blue
    primary = IceBlue400,
    onPrimary = Abyss,
    primaryContainer = IceBlue600,
    onPrimaryContainer = IceBlue300,

    // Secondary — violet accent
    secondary = Violet400,
    onSecondary = Abyss,
    secondaryContainer = Violet500,
    onSecondaryContainer = Violet300,

    // Tertiary — crystal green (goal / success)
    tertiary = Crystal400,
    onTertiary = Abyss,
    tertiaryContainer = Crystal500,
    onTertiaryContainer = Crystal300,

    // Error
    error = Rose400,
    onError = Abyss,
    errorContainer = Color(0xFF3B0A0A),
    onErrorContainer = Rose400,

    // Backgrounds — deep navy hierarchy
    background = Abyss,
    onBackground = Slate100,

    surface = AbyssMid,
    onSurface = Slate100,

    surfaceVariant = AbyssHigh,
    onSurfaceVariant = Slate300,

    outline = Slate600,
    outlineVariant = Slate700,

    inverseSurface = Slate200,
    inverseOnSurface = Slate900,
    inversePrimary = IceBlue600,

    // surfaceContainer family
    surfaceContainerLowest = Abyss,
    surfaceContainerLow = AbyssMid,
    surfaceContainer = AbyssHigh,
    surfaceContainerHigh = AbyssElevated,
    surfaceContainerHighest = Slate700,
)

private val LightColorScheme = lightColorScheme(
    primary = IceBlue500,
    onPrimary = Color.White,
    primaryContainer = Blue100,
    onPrimaryContainer = IceBlue600,

    secondary = Violet500,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEEF2FF),
    onSecondaryContainer = Violet500,

    tertiary = Crystal500,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD1FAE5),
    onTertiaryContainer = Crystal500,

    error = Rose500,
    onError = Color.White,
    errorContainer = Color(0xFFFFE4E6),
    onErrorContainer = Color(0xFF7F1D1D),

    background = Slate50,
    onBackground = Slate900,

    surface = Color.White,
    onSurface = Slate900,

    surfaceVariant = Slate100,
    onSurfaceVariant = Slate500,

    outline = Slate300,
    outlineVariant = Slate200,

    inverseSurface = Slate900,
    inverseOnSurface = Slate100,
    inversePrimary = IceBlue300,

    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Slate50,
    surfaceContainer = Slate100,
    surfaceContainerHigh = Slate200,
    surfaceContainerHighest = Slate300,
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
