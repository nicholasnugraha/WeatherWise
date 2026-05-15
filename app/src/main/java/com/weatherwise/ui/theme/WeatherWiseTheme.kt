// ui/theme/WeatherWiseTheme.kt
package com.weatherwise.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Warna primer — biru cuaca, sesuai dengan tema desktop WeatherWise
private val WeatherBlue      = Color(0xFF1565C0)
private val WeatherBlueDark  = Color(0xFF42A5F5)
private val WeatherSkyLight  = Color(0xFFE3F2FD)
private val WeatherSkyDark   = Color(0xFF0D47A1)

private val LightColorScheme = lightColorScheme(
    primary          = WeatherBlue,
    onPrimary        = Color.White,
    primaryContainer = WeatherSkyLight,
    background       = Color(0xFFF8FAFB),
    surface          = Color.White,
    onBackground     = Color(0xFF1A1C1E),
    onSurface        = Color(0xFF1A1C1E),
)

private val DarkColorScheme = darkColorScheme(
    primary          = WeatherBlueDark,
    onPrimary        = Color(0xFF003A70),
    primaryContainer = WeatherSkyDark,
    background       = Color(0xFF1A1C1E),
    surface          = Color(0xFF2B2D30),
    onBackground     = Color(0xFFE2E2E6),
    onSurface        = Color(0xFFE2E2E6),
)

@Composable
fun WeatherWiseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content     = content
    )
}