package com.babyvault.android.presentation.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Purple500,
    onPrimary = Color.White,
    primaryContainer = Purple100,
    onPrimaryContainer = Purple700,
    secondary = Peach400,
    onSecondary = Color.White,
    secondaryContainer = Peach100,
    onSecondaryContainer = Color(0xFF5C2A15),
    tertiary = Mint400,
    onTertiary = Color.White,
    tertiaryContainer = Mint100,
    onTertiaryContainer = Color(0xFF004D33),
    background = NeutralGray,
    onBackground = TextPrimary,
    surface = CardSurface,
    onSurface = TextPrimary,
    surfaceVariant = Purple100,
    onSurfaceVariant = TextSecondary,
    outline = Purple200,
)

@Composable
fun BabyVaultTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content,
    )
}
