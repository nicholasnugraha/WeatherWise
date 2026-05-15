package com.weatherwise.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weatherwise.model.OneCallResponse
import com.weatherwise.ui.components.DailyForecastItem
import com.weatherwise.ui.components.HourlyForecastRow
import com.weatherwise.ui.components.WeatherLayerOsmMapCard
import com.weatherwise.viewmodel.WeatherViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForecastScreen(
    viewModel : WeatherViewModel,
    onBack    : () -> Unit
) {
    val currentWeather by viewModel.currentWeather.observeAsState()
    val oneCallData    by viewModel.oneCallData.observeAsState()
    val cityName       by viewModel.cityName.observeAsState("")

    // Ambil data daily — lewati hari ini (index 0)
    val dailyList = remember(oneCallData) {
        oneCallData?.daily?.let { daily ->
            if (daily.size > 1) daily.subList(1, minOf(8, daily.size))
            else emptyList()
        } ?: emptyList()
    }

    // Ambil data hourly 48 jam
    val hourlyList = remember(oneCallData) {
        oneCallData?.hourly?.take(48) ?: emptyList()
    }

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
            if (oneCallData == null) {
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
                    item {
                        TodaySummaryCard(
                            current = currentWeather,
                            today   = oneCallData?.daily?.firstOrNull()
                        )
                    }

                    // ── Forecast per jam (48 jam) ──────────────
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
                        HourlyForecastRow(hourlyList = hourlyList)
                    }

                    item {
                        Text(
                            text     = "Peta Layer Cuaca",
                            fontSize = 13.sp,
                            color    = Color.White.copy(alpha = 0.65f),
                            modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                        )
                    }
                    item {
                        val lat = currentWeather?.lat ?: 0.0
                        val lon = currentWeather?.lon ?: 0.0
                        WeatherLayerOsmMapCard(lat = lat, lon = lon)
                    }

                    // ── Forecast 7 hari ────────────────────────
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
                    itemsIndexed(dailyList) { index, day ->
                        DailyForecastItem(
                            daily      = day,
                            isFirst    = index == 0,
                            isLast     = index == dailyList.lastIndex
                        )
                    }

                    // ── Alert cuaca (jika ada) ─────────────────
                    val alerts = viewModel.getAlerts()
                    if (alerts.isNotEmpty()) {
                        item {
                            Text(
                                text     = "Peringatan Cuaca",
                                fontSize = 13.sp,
                                color    = Color(0xFFFFCC80),
                                modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                            )
                        }
                        items(alerts.size) { i ->
                            WeatherAlertCard(alert = alerts[i])
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
    current : com.weatherwise.model.CurrentWeather?,
    today   : OneCallResponse.DailyData?
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
                    // Suhu siang
                    SummaryItem(
                        label = "Siang",
                        value = "${today?.temp?.day?.toInt() ?: "--"}°"
                    )
                    // Suhu malam
                    SummaryItem(
                        label = "Malam",
                        value = "${today?.temp?.night?.toInt() ?: "--"}°"
                    )
                    // Kelembaban
                    SummaryItem(
                        label = "Lembab",
                        value = "${current?.humidity ?: "--"}%"
                    )
                    // Probabilitas hujan
                    SummaryItem(
                        label = "Hujan",
                        value = "${((today?.pop ?: 0.0) * 100).toInt()}%"
                    )
                    // UV Index
                    SummaryItem(
                        label = "UV",
                        value = "${today?.uvi?.toInt() ?: "--"}"
                    )
                }

                // Ringkasan teks dari OWM (hanya tersedia di API 3.0)
                today?.summary?.let { summary ->
                    if (summary.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text     = summary,
                            fontSize = 13.sp,
                            color    = Color.White.copy(alpha = 0.8f),
                            lineHeight = 18.sp
                        )
                    }
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

// ── Kartu Peringatan Cuaca ─────────────────────────────────────
@Composable
private fun WeatherAlertCard(alert: OneCallResponse.AlertData) {
    Card(
        shape  = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE65100).copy(alpha = 0.85f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector        = Icons.Default.ArrowBackIosNew, // ganti dengan Warning
                    contentDescription = null,
                    tint               = Color.White,
                    modifier           = Modifier.size(16.dp)
                )
                Text(
                    text       = alert.event ?: "Peringatan Cuaca",
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = Color.White
                )
            }
            alert.description?.let { desc ->
                if (desc.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text      = desc,
                        fontSize  = 12.sp,
                        color     = Color.White.copy(alpha = 0.85f),
                        maxLines  = 3,
                        overflow  = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text     = "Sumber: ${alert.senderName ?: "BMKG"}",
                fontSize = 10.sp,
                color    = Color.White.copy(alpha = 0.55f)
            )
        }
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