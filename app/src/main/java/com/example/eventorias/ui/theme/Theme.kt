package com.example.eventorias.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = red,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = Color.Black, // Dark background for dark theme
    surface = Color.DarkGray, // Dark surface for dark theme
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = app_white
)

private val LightColorScheme = lightColorScheme(
    primary = red,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = Color.Black, // Dark background for light theme (override if needed)
    surface = Color.DarkGray, // Dark surface for light theme (override if needed)
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = app_white
)

@Composable
fun EventoriasTheme(
    useDarkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable() () -> Unit
) {
    val colors = if (!useDarkTheme) {
        DarkColorScheme
    } else {
        DarkColorScheme
    }

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}