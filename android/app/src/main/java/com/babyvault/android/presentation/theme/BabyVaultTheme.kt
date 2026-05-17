package com.babyvault.android.presentation.theme

import android.os.Build
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = Teal500,
    onPrimary = Color.White,
    primaryContainer = Teal100,
    onPrimaryContainer = Teal700,
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
    surfaceVariant = Teal100,
    onSurfaceVariant = TextSecondary,
    outline = Teal200,
)

@Composable
fun BabyVaultTheme(content: @Composable () -> Unit) {
    val colorScheme = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val context = LocalContext.current
        dynamicLightColorScheme(context)
    } else {
        LightColorScheme
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
