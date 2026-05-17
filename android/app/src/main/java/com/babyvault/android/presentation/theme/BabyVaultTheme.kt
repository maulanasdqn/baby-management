package com.babyvault.android.presentation.theme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary              = NavyPrimary,
    onPrimary            = Color.White,
    primaryContainer     = Lavender100,
    onPrimaryContainer   = Navy900,
    secondary            = PinkAccent,
    onSecondary          = Color.White,
    secondaryContainer   = PinkLight,
    onSecondaryContainer = Color(0xFF7A1D3A),
    tertiary             = Teal500,
    onTertiary           = Color.White,
    tertiaryContainer    = Teal100,
    onTertiaryContainer  = Navy800,
    background           = WarmCream,
    onBackground         = TextPrimary,
    surface              = CardWhite,
    onSurface            = TextPrimary,
    surfaceVariant       = WarmCream,
    onSurfaceVariant     = TextSecondary,
    outline              = Color(0xFFDDDFEE),
    error                = Color(0xFFEF4444),
    onError              = Color.White,
    errorContainer       = Color(0xFFFEE2E2),
    onErrorContainer     = Color(0xFF7F1D1D),
)

@Composable
fun BabyVaultTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content,
    )
}
