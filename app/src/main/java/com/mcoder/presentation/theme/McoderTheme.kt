package com.mcoder.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * App theme wrapper with dark default.
 */
@Composable
fun McoderTheme(
    theme: String,
    content: @Composable () -> Unit
) {
    val darkColors = darkColorScheme(
        primary = Color(0xFF8AB4F8),
        secondary = Color(0xFF5F6368),
        background = Color(0xFF0F0F10),
        surface = Color(0xFF1B1B1D),
        onPrimary = Color(0xFF0F0F10),
        onBackground = Color(0xFFEAEAEA),
        onSurface = Color(0xFFEAEAEA)
    )
    val lightColors = lightColorScheme(
        primary = Color(0xFF1A73E8),
        secondary = Color(0xFF5F6368)
    )

    val useDark = when (theme.lowercase()) {
        "light" -> false
        "dark" -> true
        else -> true
    }

    MaterialTheme(
        colorScheme = if (useDark) darkColors else lightColors,
        content = content
    )
}
