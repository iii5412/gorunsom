package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = GoreunsumPrimary,
    onPrimary = Color.White,
    primaryContainer = GoreunsumPrimaryContainer,
    onPrimaryContainer = GoreunsumOnPrimaryContainer,
    secondary = GoreunsumExhale,
    onSecondary = Color.White,
    tertiary = GoreunsumInhale,
    onTertiary = Color.White,
    background = GoreunsumBackground,
    onBackground = GoreunsumTextPrimary,
    surface = GoreunsumSurface,
    onSurface = GoreunsumTextPrimary,
    surfaceVariant = GoreunsumSurfaceVariant,
    onSurfaceVariant = GoreunsumTextSecondary,
    outline = GoreunsumOutline,
    error = GoreunsumDanger,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
