package me.vanpetegem.accentor.ui

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

@Composable
fun AccentorTheme(content: @Composable () -> Unit) {
    val lightColors: ColorScheme
    val darkColors: ColorScheme
    if (Build.VERSION.SDK_INT >= 31) {
        lightColors = lightColorScheme(
            primary = Color(0xFF2196F3),
            secondary = Color(0xFFF44336),
        )
        darkColors = darkColorScheme(
            primary = Color(0xFF90CAF9),
            secondary = Color(0xFFEF9A9A),
        )
    } else {
        val context = LocalContext.current
        lightColors = dynamicLightColorScheme(context)
        darkColors = dynamicDarkColorScheme(context)
    }
    val colorScheme = if (isSystemInDarkTheme()) darkColors else lightColors
    MaterialTheme(colorScheme = colorScheme, content = content)
}
