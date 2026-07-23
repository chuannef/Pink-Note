package com.pinknote.app.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val LightColors = lightColorScheme(
    primary = RoseDeep,
    secondary = PastelPink,
    tertiary = OvulationGreen,
    background = PetalMist,
    surface = CreamWhite,
    surfaceVariant = BlushSurface,
    onPrimary = CreamWhite,
    onSecondary = RoseInk,
    onBackground = RoseInk,
    onSurface = RoseInk,
    onSurfaceVariant = RoseMuted,
    outline = RoseLine,
    primaryContainer = BlushSurface,
    onPrimaryContainer = RoseInk
)

private val DarkColors = darkColorScheme(
    primary = PastelPink,
    secondary = RoseDeep,
    tertiary = OvulationGreen,
    background = androidx.compose.ui.graphics.Color(0xFF21151B),
    surface = androidx.compose.ui.graphics.Color(0xFF2B1A22),
    surfaceVariant = androidx.compose.ui.graphics.Color(0xFF3A242E),
    onBackground = androidx.compose.ui.graphics.Color(0xFFFFEEF5),
    onSurface = androidx.compose.ui.graphics.Color(0xFFFFEEF5)
)

private val PinkTypography = Typography(
    displaySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 40.sp,
        lineHeight = 44.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 34.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 30.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 21.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 18.sp
    )
)

@Composable
fun PinkNoteTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = PinkTypography,
        content = content
    )
}
