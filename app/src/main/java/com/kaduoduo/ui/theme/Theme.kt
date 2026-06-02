package com.kaduoduo.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val KaduoduoColors = lightColorScheme(
    primary = Color(0xFF126A5A),
    secondary = Color(0xFF6B5E2E),
    tertiary = Color(0xFF8E4B35),
    background = Color(0xFFF8FAF8),
    surface = Color(0xFFFFFFFF)
)

@Composable
fun KaduoduoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = KaduoduoColors,
        content = content
    )
}
