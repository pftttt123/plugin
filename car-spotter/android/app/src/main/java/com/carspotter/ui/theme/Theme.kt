package com.carspotter.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Slate = Color(0xFF1F2933)
private val Teal = Color(0xFF2BB3A3)
private val Amber = Color(0xFFF5A623)

private val LightColors = lightColorScheme(
    primary = Teal,
    secondary = Amber,
    background = Color(0xFFF7F9FB),
)

private val DarkColors = darkColorScheme(
    primary = Teal,
    secondary = Amber,
    background = Slate,
)

@Composable
fun CarSpotterTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography(),
        content = content,
    )
}
