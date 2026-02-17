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
    secondaryContainer = Cyan200,
    onSecondaryContainer = Color(0xFF063B4A),
    tertiary = Green500,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD1FAE5),
    onTertiaryContainer = Green600,
    error = Red500,
    onError = Color.White,
    background = Slate50,
    onBackground = Slate900,
    surface = Color.White,
    onSurface = Slate900,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate600,
    outline = Slate300,
    outlineVariant = Slate200,
    surfaceContainerHighest = Slate100,
    surfaceContainerHigh = Slate50,
    surfaceContainer = Color.White,
    surfaceContainerLow = Color.White,
    surfaceContainerLowest = Color.White,
)

private val DarkColorScheme = darkColorScheme(
    primary = Blue400,
    onPrimary = Blue700,
    primaryContainer = Blue600,
    onPrimaryContainer = Blue100,
    secondary = Cyan400,
    onSecondary = Color(0xFF063B4A),
    secondaryContainer = Color(0xFF0C4A5E),
    onSecondaryContainer = Cyan200,
    tertiary = Green400,
    onTertiary = Color(0xFF003919),
    tertiaryContainer = Green600,
    onTertiaryContainer = Color(0xFFD1FAE5),
    error = Color(0xFFF87171),
    onError = Color(0xFF690005),
    background = Slate900,
    onBackground = Slate100,
    surface = Slate800,
    onSurface = Slate100,
    surfaceVariant = Slate700,
    onSurfaceVariant = Slate300,
    outline = Slate500,
    outlineVariant = Slate600,
    surfaceContainerHighest = Slate700,
    surfaceContainerHigh = Slate800,
    surfaceContainer = Slate800,
    surfaceContainerLow = Slate900,
    surfaceContainerLowest = Slate900,
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
