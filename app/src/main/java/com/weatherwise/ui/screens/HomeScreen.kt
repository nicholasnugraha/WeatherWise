package com.weatherwise.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.weatherwise.model.CurrentWeather
import com.weatherwise.model.OneCallResponse
import com.weatherwise.ui.components.*
import com.weatherwise.viewmodel.WeatherViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: WeatherViewModel,
    onSeeFullForecast: () -> Unit
) {
    // ── Observasi LiveData dari ViewModel ──────────────────────
    val currentWeather by viewModel.currentWeather.observeAsState()
    val oneCallData    by viewModel.oneCallData.observeAsState()
    val cityName       by viewModel.cityName.observeAsState("")
    val isLoading      by viewModel.isLoading.observeAsState(false)
    val errorMessage   by viewModel.errorMessage.observeAsState()

    // ── State lokal untuk search bar ───────────────────────────
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    // ── Pull-to-refresh state ──────────────────────────────────
    val pullRefreshState = rememberPullToRefreshState()
    if (pullRefreshState.isRefreshing) {
        LaunchedEffect(Unit) {
            viewModel.refresh()
            pullRefreshState.endRefresh()
        }
    }

    // ── Warna background gradient berdasarkan kondisi cuaca ────
    val gradientColors = getWeatherGradient(currentWeather?.conditionIcon)

    Box(modifier = Modifier.fillMaxSize()) {

        // Background gradient dinamis
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(gradientColors))
        )

        // Konten utama
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(top = 48.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ── Search Bar ─────────────────────────────────────
            SearchBar(
                query         = searchQuery,
                onQueryChange = { searchQuery = it },
                onSearch      = {
                    if (searchQuery.isNotBlank()) {
                        viewModel.searchByCity(searchQuery.trim())
                        isSearchActive = false
                    }
                },
                placeholder   = "Cari kota...",
                modifier      = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ── Loading State ──────────────────────────────────
            if (isLoading) {
                CircularProgressIndicator(
                    color    = Color.White,
                    modifier = Modifier.padding(32.dp)
                )
            }

            // ── Error State ────────────────────────────────────
            errorMessage?.let { msg ->
                ErrorBanner(
                    message  = msg,
                    onDismiss = { viewModel.clearError() }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // ── Konten Cuaca (tampil jika data tersedia) ───────
            if (!isLoading && currentWeather != null) {

                // Nama kota + negara
                CityHeader(
                    cityName = currentWeather!!.cityName ?: cityName,
                    country  = currentWeather!!.country ?: ""
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Card utama: suhu besar + ikon + kondisi
                MainWeatherCard(weather = currentWeather!!)

                Spacer(modifier = Modifier.height(16.dp))

                // Detail: humidity, wind, UV, tekanan, dll.
                WeatherDetailGrid(weather = currentWeather!!)

                Spacer(modifier = Modifier.height(16.dp))

                // Sunrise & Sunset
                SunriseSunsetCard(weather = currentWeather!!)

                Spacer(modifier = Modifier.height(16.dp))

                // Forecast hourly — scroll horizontal 48 jam
                oneCallData?.hourly?.let { hourlyList ->
                    HourlyForecastRow(hourlyList = hourlyList.take(24))
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Tombol lihat forecast lengkap
                OutlinedButton(
                    onClick = onSeeFullForecast,
                    colors  = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    ),
                    border  = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Lihat Prakiraan 7 Hari →")
                }
            }

            // ── Empty State (belum ada data) ───────────────────
            if (!isLoading && currentWeather == null && errorMessage == null) {
                EmptyWeatherState()
            }
        }

        // Pull-to-refresh indicator
        PullToRefreshContainer(
            state    = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

// ── Helper: gradient warna berdasarkan icon OWM ────────────────
private fun getWeatherGradient(icon: String?): List<Color> {
    return when (icon?.take(2)) {
        "01" -> listOf(Color(0xFFFFA726), Color(0xFFFF7043)) // cerah — oranye hangat
        "02" -> listOf(Color(0xFF42A5F5), Color(0xFF1565C0)) // sebagian berawan
        "03",
        "04" -> listOf(Color(0xFF78909C), Color(0xFF455A64)) // berawan — abu
        "09",
        "10" -> listOf(Color(0xFF1E88E5), Color(0xFF0D47A1)) // hujan — biru gelap
        "11" -> listOf(Color(0xFF5E35B1), Color(0xFF1A237E)) // petir — ungu gelap
        "13" -> listOf(Color(0xFFB3E5FC), Color(0xFF4FC3F7)) // salju — biru muda
        "50" -> listOf(Color(0xFFB0BEC5), Color(0xFF607D8B)) // kabut — abu muda
        else -> listOf(Color(0xFF1565C0), Color(0xFF0D47A1)) // default biru
    }
}