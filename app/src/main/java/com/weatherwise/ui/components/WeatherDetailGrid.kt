package com.weatherwise.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weatherwise.model.CurrentWeather

@Composable
fun WeatherDetailGrid(weather: CurrentWeather) {
    // Warna card semi-transparan — sama konsep dengan panel desktop
    val cardColor = Color.White.copy(alpha = 0.15f)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DetailCard(Icons.Default.WaterDrop, "Kelembaban",
                "${weather.humidity}%", cardColor, Modifier.weight(1f))
            DetailCard(Icons.Default.Air, "Angin",
                "${weather.windSpeed} m/s", cardColor, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DetailCard(Icons.Default.Visibility, "Visibilitas",
                "${weather.visibility / 1000} km", cardColor, Modifier.weight(1f))
            DetailCard(Icons.Default.Compress, "Tekanan",
                "${weather.pressure} hPa", cardColor, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DetailCard(Icons.Default.WbSunny, "UV Index",
                "${weather.uvIndex.toInt()}", cardColor, Modifier.weight(1f))
            DetailCard(Icons.Default.Thermostat, "Terasa",
                "${weather.feelsLike.toInt()}°C", cardColor, Modifier.weight(1f))
        }
    }
}

@Composable
private fun DetailCard(
    icon     : ImageVector,
    label    : String,
    value    : String,
    bgColor  : Color,
    modifier : Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape    = RoundedCornerShape(12.dp),
        colors   = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Column(
            modifier            = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = label,
                tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Medium)
            Text(label, fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
        }
    }
}