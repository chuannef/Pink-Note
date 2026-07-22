package com.pinknote.app.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = RoseDeep,
    secondary = PastelPink,
    tertiary = OvulationGreen,
    background = SoftBlush,
    surface = androidx.compose.ui.graphics.Color.White
)

private val DarkColors = darkColorScheme(
    primary = PastelPink,
    secondary = RoseDeep,
    tertiary = OvulationGreen
)

@Composable
fun PinkNoteTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
