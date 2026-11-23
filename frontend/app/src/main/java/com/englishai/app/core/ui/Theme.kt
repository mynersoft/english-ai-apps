package com.englishai.app.core.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Colors = lightColorScheme(
    primary = Color(0xFF1F5FD6),
    onPrimary = Color.White,
    secondary = Color(0xFF0F8F6F),
    background = Color(0xFFF7F8FB),
    surface = Color.White,
    error = Color(0xFFC62828)
)

@Composable
fun EnglishAiTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Colors, content = content)
}
