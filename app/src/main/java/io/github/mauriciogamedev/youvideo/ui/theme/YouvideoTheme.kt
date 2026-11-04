package io.github.mauriciogamedev.youvideo.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val YouvideoColorScheme = darkColorScheme(
    primary = Color(0xFF86A9FF),
    onPrimary = Color(0xFF07152F),
    secondary = Color(0xFFB5C7FF),
    background = Color(0xFF07090D),
    onBackground = Color(0xFFF3F5FA),
    surface = Color(0xFF11151C),
    onSurface = Color(0xFFF3F5FA),
    surfaceVariant = Color(0xFF202633),
    onSurfaceVariant = Color(0xFFB7C0D0),
    outline = Color(0xFF414B5C)
)

@Composable
fun YouvideoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = YouvideoColorScheme,
        content = content
    )
}
