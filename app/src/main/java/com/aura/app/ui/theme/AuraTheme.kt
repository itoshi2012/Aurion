package com.aura.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.aura.app.core.OrbState

object AuraColors {
    val Background = Color(0xFF02040A)
    val Cyan = Color(0xFF00E5FF)
    val Green = Color(0xFF00FFA3)
    val Violet = Color(0xFFB388FF)
    val Magenta = Color(0xFFFF4DA6)
    val Stop = Color(0xFFD50000)
    val TextDim = Color(0xFF7C8AA5)
}

fun orbColorFor(state: OrbState): Color = when (state) {
    OrbState.IDLE -> AuraColors.Cyan
    OrbState.LISTENING -> AuraColors.Green
    OrbState.THINKING -> AuraColors.Violet
    OrbState.SPEAKING -> AuraColors.Magenta
}

@Composable
fun AuraTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = AuraColors.Cyan,
            background = AuraColors.Background,
            surface = AuraColors.Background,
            onBackground = Color.White,
            onSurface = Color.White
        ),
        content = content
    )
}
