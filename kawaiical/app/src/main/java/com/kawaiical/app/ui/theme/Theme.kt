package com.kawaiical.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = DeepPink,
    onPrimary = SoftWhite,
    primaryContainer = BlushPink,
    onPrimaryContainer = Color(0xFF6E2E45),
    secondary = DeepBlue,
    onSecondary = SoftWhite,
    secondaryContainer = CloudBlue,
    onSecondaryContainer = Color(0xFF27506B),
    tertiary = Lavender,
    onTertiary = Color(0xFF41316B),
    tertiaryContainer = Color(0xFFEDE6FB),
    onTertiaryContainer = Color(0xFF41316B),
    background = Color(0xFFFFF9FB),
    onBackground = InkDark,
    surface = SoftWhite,
    onSurface = InkDark,
    surfaceVariant = MistPink,
    onSurfaceVariant = InkSoft,
    outline = Color(0xFFE8D5DE),
    outlineVariant = Color(0xFFF3E6EC),
    error = Color(0xFFE5537A),
    onError = SoftWhite,
)

private val DarkColors = darkColorScheme(
    primary = PastelPink,
    onPrimary = Color(0xFF4A2233),
    primaryContainer = CharcoalPinkContainer,
    onPrimaryContainer = BlushPink,
    secondary = BabyBlue,
    onSecondary = Color(0xFF1E3A50),
    secondaryContainer = CharcoalBlueContainer,
    onSecondaryContainer = CloudBlue,
    tertiary = Lavender,
    onTertiary = Color(0xFF2E2350),
    tertiaryContainer = Color(0xFF383050),
    onTertiaryContainer = Color(0xFFE4DBF8),
    background = Charcoal,
    onBackground = MoonWhite,
    surface = CharcoalSurface,
    onSurface = MoonWhite,
    surfaceVariant = CharcoalRaised,
    onSurfaceVariant = MoonSoft,
    outline = Color(0xFF473F55),
    outlineVariant = Color(0xFF352F42),
    error = Color(0xFFFF8AA8),
    onError = Color(0xFF4A1626),
)

@Composable
fun KawaiiCalTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = KawaiiTypography,
        shapes = KawaiiShapes,
        content = content,
    )
}
