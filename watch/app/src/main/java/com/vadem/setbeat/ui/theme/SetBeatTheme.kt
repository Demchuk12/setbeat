package com.vadem.setbeat.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.MaterialTheme

private object VademBrand {
    val accent = Color(0xFF8472FE)
    val accentDim = Color(0xFF6855D6)
    val accentContainer = Color(0xFF2B2363)
    val onAccentContainer = Color(0xFFE4DFFF)
    val ink = Color(0xFF14151A)
}

object WorkoutColors {
    val effort = Color(0xFFDC9241)
    val recovery = Color(0xFF4FB4CB)
    val heartRate = Color(0xFFED7D91)
}

private val SetBeatColors = ColorScheme().copy(
    primary = VademBrand.accent,
    primaryDim = VademBrand.accentDim,
    primaryContainer = VademBrand.accentContainer,
    onPrimary = VademBrand.ink,
    onPrimaryContainer = VademBrand.onAccentContainer,
)

@Composable
fun SetBeatTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = SetBeatColors, content = content)
}
