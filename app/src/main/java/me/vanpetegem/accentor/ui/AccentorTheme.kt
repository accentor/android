package me.vanpetegem.accentor.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun AccentorTheme(content: @Composable () -> Unit) {
    val lightColors = lightColors(
        primary = Color(0xFF2196F3),
        primaryVariant = Color(0xFF1976D2),
        secondary = Color(0xFFEF9A9A),
        secondaryVariant = Color(0xFFF44336),
    )
    val darkColors = darkColors(
        primary = Color(0xFF90CAF9),
        primaryVariant = Color(0xFF1976D2),
        secondary = Color(0xFFEF9A9A),
        secondaryVariant = Color(0xFFEF9A9A),
    )
    val colors = if (isSystemInDarkTheme()) darkColors else lightColors
    MaterialTheme(colors = colors, content = content)
}
