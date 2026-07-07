package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = SlatePrimaryDark,
    secondary = SlatePrimaryDark,
    tertiary = SuccessGreen,
    background = SlateBgDark,
    surface = SlateSurfDark,
    primaryContainer = Color(0xFF00325C),
    onPrimaryContainer = Color(0xFFD1E4FF),
    secondaryContainer = Color(0xFF004780),
    onSecondaryContainer = Color(0xFFE2E8F0),
    surfaceVariant = Color(0xFF242C3F),
    onSurfaceVariant = Color(0xFF94A3B8),
    onPrimary = SlateBgDark,
    onSecondary = SlateBgDark,
    onTertiary = SlateBgDark,
    onBackground = SlateSecondaryDark,
    onSurface = SlateSecondaryDark,
    outline = GrayBorderDark
)

private val LightColorScheme = lightColorScheme(
    primary = SlatePrimaryLight,
    secondary = SlatePrimaryLight,
    tertiary = SuccessGreen,
    background = SlateBgLight,
    surface = SlateSurfLight,
    primaryContainer = Color(0xFFD1E4FF),
    onPrimaryContainer = Color(0xFF001D36),
    secondaryContainer = Color(0xFFE1E2EC),
    onSecondaryContainer = Color(0xFF001D36),
    surfaceVariant = Color(0xFFF3F4F9),
    onSurfaceVariant = Color(0xFF43474E),
    onPrimary = SlateSurfLight,
    onSecondary = SlateSurfLight,
    onTertiary = SlateSurfLight,
    onBackground = SlateSecondaryLight,
    onSurface = SlateSecondaryLight,
    outline = GrayBorderLight
)

@Composable
fun MyApplicationTheme(
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
