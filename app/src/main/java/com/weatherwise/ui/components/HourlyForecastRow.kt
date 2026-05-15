package com.weatherwise.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.weatherwise.model.OneCallResponse
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun HourlyForecastRow(hourlyList: List<OneCallResponse.HourlyData>) {
    Card(
        shape  = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Per Jam", fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 8.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                items(hourlyList) { hour ->
                    HourlyItem(hour)
                }
            }
        }
    }
}

@Composable
private fun HourlyItem(hour: OneCallResponse.HourlyData) {
    val time = Instant.ofEpochSecond(hour.dt)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("HH:mm"))
    val iconUrl = "https://openweathermap.org/img/wn/${hour.conditionIcon}@2x.png"

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(time, fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
        AsyncImage(model = iconUrl, contentDescription = null,
            modifier = Modifier.size(36.dp))
        Text("${hour.temp.toInt()}°", fontSize = 14.sp, color = Color.White)
        // Probabilitas hujan jika > 10%
        if (hour.pop > 0.1) {
            Text("${(hour.pop * 100).toInt()}%",
                fontSize = 11.sp, color = Color(0xFF90CAF9))
        }
    }
}