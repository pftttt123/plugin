package com.setupnotebook.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val RacingRed = Color(0xFFFF2D2D)
val PureBlack = Color(0xFF000000)
val Surface1 = Color(0xFF121212)
val Surface2 = Color(0xFF1A1A1A)
val Surface3 = Color(0xFF222222)
val TextPrimary = Color(0xFFECECEC)
val TextSecondary = Color(0xFF9E9E9E)
val OutlineGrey = Color(0xFF2C2C2C)

private val OledColorScheme = darkColorScheme(
    primary = RacingRed,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF3A0D0D),
    onPrimaryContainer = Color(0xFFFFB3B3),
    secondary = Color(0xFFB0B0B0),
    onSecondary = Color.Black,
    secondaryContainer = Surface3,
    onSecondaryContainer = TextPrimary,
    tertiary = Color(0xFFFF7043),
    background = PureBlack,
    onBackground = TextPrimary,
    surface = Surface1,
    onSurface = TextPrimary,
    surfaceVariant = Surface2,
    onSurfaceVariant = TextSecondary,
    surfaceContainerLowest = PureBlack,
    surfaceContainerLow = Color(0xFF0B0B0B),
    surfaceContainer = Color(0xFF0E0E0E),
    surfaceContainerHigh = Surface1,
    surfaceContainerHighest = Surface2,
    outline = OutlineGrey,
    outlineVariant = Color(0xFF1F1F1F),
    error = Color(0xFFFF6659),
    onError = Color.Black,
)

/** Monospace style used for every numeric value in the app. */
val MonoValue = TextStyle(
    fontFamily = FontFamily.Monospace,
    fontWeight = FontWeight.SemiBold,
    fontSize = 15.sp,
)

val MonoValueSmall = MonoValue.copy(fontSize = 13.sp)
val MonoValueLarge = MonoValue.copy(fontSize = 18.sp)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun SetupNotebookTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = OledColorScheme,
        shapes = AppShapes,
        content = content,
    )
}
