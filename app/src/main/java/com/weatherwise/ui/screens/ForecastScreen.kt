package com.weatherwise.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weatherwise.model.CurrentWeather
import com.weatherwise.model.ForecastDaily
import com.weatherwise.model.ForecastHourly
import com.weatherwise.ui.components.DailyForecastItem
import com.weatherwise.ui.components.HourlyForecastRow
import com.weatherwise.viewmodel.WeatherViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForecastScreen(
    viewModel : WeatherViewModel,
    onBack    : () -> Unit
) {
    val currentWeather by viewModel.currentWeather.collectAsStateWithLifecycle()
    val hourlyForecast by viewModel.hourlyForecast.collectAsStateWithLifecycle()
    val dailyForecast  by viewModel.dailyForecast.collectAsStateWithLifecycle()
    val cityName       by viewModel.cityName.collectAsStateWithLifecycle("")

    // Gradient sama dengan HomeScreen berdasarkan icon cuaca
    val gradientColors = remember(currentWeather?.conditionIcon) {
        getWeatherGradient(currentWeather?.conditionIcon)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(gradientColors))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Top App Bar ────────────────────────────────────
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text       = "Prakiraan Cuaca",
                            fontSize   = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color      = Color.White
                        )
                        if (cityName.isNotEmpty()) {
                            Text(
                                text     = cityName,
                                fontSize = 13.sp,
                                color    = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector        = Icons.Default.ArrowBackIosNew,
                            contentDescription = "Kembali",
                            tint               = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )

            // ── Konten Utama ───────────────────────────────────
            if (dailyForecast.isEmpty() && currentWeather == null) {
                // Empty state jika data belum tersedia
                Box(
                    modifier         = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            } else {
                LazyColumn(
                    contentPadding        = PaddingValues(
                        start  = 16.dp,
                        end    = 16.dp,
                        top    = 8.dp,
                        bottom = 32.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    // ── Ringkasan hari ini ─────────────────────
                    if (currentWeather != null && dailyForecast.isNotEmpty()) {
                        item {
                            TodaySummaryCard(
                                current = currentWeather!!,
                                today   = dailyForecast.firstOrNull()
                            )
                        }
                    }

                    // ── Forecast per jam ──────────────
                    if (hourlyForecast.isNotEmpty()) {
                        item {
                            Text(
                                text     = "Per Jam",
                                fontSize = 13.sp,
                                color    = Color.White.copy(alpha = 0.65f),
                                modifier = Modifier.padding(
                                    top    = 4.dp,
                                    bottom = 2.dp
                                )
                            )
                        }
                        item {
                            HourlyForecastRow(hourlyList = hourlyForecast)
                        }
                    }

                    // ── Forecast 7 hari ────────────────────────
                    if (dailyForecast.isNotEmpty()) {
                        item {
                            Text(
                                text     = "7 Hari ke Depan",
                                fontSize = 13.sp,
                                color    = Color.White.copy(alpha = 0.65f),
                                modifier = Modifier.padding(
                                    top    = 4.dp,
                                    bottom = 2.dp
                                )
                            )
                        }
                        itemsIndexed(dailyForecast) { index, day ->
                            DailyForecastItem(
                                daily      = day,
                                isFirst    = index == 0,
                                isLast     = index == dailyForecast.lastIndex
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Ringkasan Hari Ini ─────────────────────────────────────────
@Composable
private fun TodaySummaryCard(
    current : CurrentWeather,
    today   : ForecastDaily?
) {
    androidx.compose.foundation.shape.RoundedCornerShape(16.dp).let { shape ->
        Card(
            shape  = shape,
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.15f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text       = "Hari Ini",
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = Color.White
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    // Suhu siang (max)
                    SummaryItem(
                        label = "Tertinggi",
                        value = "${today?.tempMax?.toInt() ?: current.temperature.toInt()}°"
                    )
                    // Suhu malam (min)
                    SummaryItem(
                        label = "Terendah",
                        value = "${today?.tempMin?.toInt() ?: current.temperature.toInt()}°"
                    )
                    // Kelembaban
                    SummaryItem(
                        label = "Lembab",
                        value = "${current.humidity}%"
                    )
                    // Angin
                    SummaryItem(
                        label = "Angin",
                        value = "${current.windSpeed} m/s"
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Medium,
            color = Color.White)
        Text(label, fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.6f))
    }
}

// ── Helper gradient (sama dengan HomeScreen) ───────────────────
private fun getWeatherGradient(icon: String?): List<Color> {
    return when (icon?.take(2)) {
        "01" -> listOf(Color(0xFFFFA726), Color(0xFFFF7043))
        "02" -> listOf(Color(0xFF42A5F5), Color(0xFF1565C0))
        "03",
        "04" -> listOf(Color(0xFF78909C), Color(0xFF455A64))
        "09",
        "10" -> listOf(Color(0xFF1E88E5), Color(0xFF0D47A1))
        "11" -> listOf(Color(0xFF5E35B1), Color(0xFF1A237E))
        "13" -> listOf(Color(0xFFB3E5FC), Color(0xFF4FC3F7))
        "50" -> listOf(Color(0xFFB0BEC5), Color(0xFF607D8B))
        else -> listOf(Color(0xFF1565C0), Color(0xFF0D47A1))
    }
}
