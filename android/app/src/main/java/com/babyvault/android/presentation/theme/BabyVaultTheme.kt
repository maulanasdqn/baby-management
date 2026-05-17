package com.babyvault.android.presentation.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Teal500,
    onPrimary = Color.White,
    primaryContainer = Teal100,
    onPrimaryContainer = Teal700,
    secondary = Rose400,
    onSecondary = Color.White,
    secondaryContainer = Rose100,
    onSecondaryContainer = Color(0xFF7A1D3A),
    tertiary = Sage400,
    onTertiary = Color.White,
    tertiaryContainer = Sage100,
    onTertiaryContainer = Color(0xFF14532D),
    background = NeutralGray,
    onBackground = TextPrimary,
    surface = CardSurface,
    onSurface = TextPrimary,
    surfaceVariant = Teal100,
    onSurfaceVariant = TextSecondary,
    outline = Teal200,
)

@Composable
fun BabyVaultTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content,
    )
}
