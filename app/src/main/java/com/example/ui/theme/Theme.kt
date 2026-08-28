package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

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
    onError = Color.White,
    errorContainer = GoreunsumDangerContainer,
    onErrorContainer = GoreunsumDanger
)

private val GoreunsumShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(34.dp)
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        shapes = GoreunsumShapes,
        content = content
    )
}
