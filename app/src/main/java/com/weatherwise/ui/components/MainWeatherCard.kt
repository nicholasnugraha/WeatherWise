package com.weatherwise.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.weatherwise.model.CurrentWeather

@Composable
fun MainWeatherCard(weather: CurrentWeather) {
    val iconUrl = "https://openweathermap.org/img/wn/${weather.conditionIcon}@4x.png"

    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        // Icon cuaca dari OWM — Coil load otomatis
        AsyncImage(
            model             = iconUrl,
            contentDescription = weather.condition,
            modifier          = Modifier.size(120.dp)
        )

        // Suhu utama — besar dan bold
        Text(
            text       = "${weather.temperature.toInt()}°C",
            fontSize   = 72.sp,
            fontWeight = FontWeight.Thin,
            color      = Color.White
        )

        // Deskripsi kondisi
        Text(
            text     = weather.condition.replaceFirstChar { it.uppercase() },
            fontSize = 18.sp,
            color    = Color.White.copy(alpha = 0.9f)
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Suhu min/max
        Text(
            text     = "↑${weather.tempMax.toInt()}°  ↓${weather.tempMin.toInt()}°",
            fontSize = 14.sp,
            color    = Color.White.copy(alpha = 0.75f)
        )
    }
}