package com.example.arigo.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = GreenPrimary,
    onPrimary = White,
    primaryContainer = GreenSurface,
    onPrimaryContainer = GreenDark,
    secondary = GreenLight,
    onSecondary = White,
    background = White,
    onBackground = BlackText,
    surface = White,
    onSurface = BlackText,
    surfaceVariant = OffWhite,
    onSurfaceVariant = GrayText,
    outline = LightGray,
    error = AqiBad,
    onError = White
)

private val DarkColors = darkColorScheme(
    primary = GreenLight,
    onPrimary = BlackText,
    primaryContainer = GreenDark,
    onPrimaryContainer = GreenSurface,
    secondary = MintGreen,
    onSecondary = BlackText,
    background = DarkBackground,
    onBackground = White,
    surface = DarkSurface,
    onSurface = White,
    surfaceVariant = DarkCard,
    onSurfaceVariant = LightGray,
    outline = DividerGray,
    error = AqiBad,
    onError = White
)

@Composable
fun AriGoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = AriGoTypography,
        content = content
    )
}
