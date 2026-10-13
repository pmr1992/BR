package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BlackridgeColorScheme = darkColorScheme(
    primary = AmberTerminal,
    onPrimary = Color(0xFF1E1300),
    primaryContainer = Color(0xFF452B00),
    onPrimaryContainer = AmberTerminalBright,
    secondary = CyanNeon,
    onSecondary = Color(0xFF003549),
    secondaryContainer = Color(0xFF004D68),
    onSecondaryContainer = Color(0xFFBEE9FF),
    tertiary = BloodRed,
    onTertiary = Color.White,
    background = BlackridgeObsidian,
    onBackground = TextWhite,
    surface = BlackridgeSurface,
    onSurface = TextWhite,
    surfaceVariant = BlackridgeSurfaceElevated,
    onSurfaceVariant = TextMuted,
    outline = BlackridgeBorder,
    outlineVariant = Color(0xFF1E2530),
    error = BloodRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Enforce consistent gritty noir theme
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = BlackridgeColorScheme,
        typography = Typography,
        content = content
    )
}
