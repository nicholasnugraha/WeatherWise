package com.weatherwise.api

import android.content.Context
import android.util.Log
import com.weatherwise.data.CachedWeatherEntity
import com.weatherwise.data.WeatherDatabase
import com.weatherwise.model.CurrentWeather
import com.weatherwise.model.CurrentWeatherResponse
import com.weatherwise.model.ForecastDaily
import com.weatherwise.model.ForecastHourly
import com.weatherwise.model.GeocodingResponse
import com.weatherwise.service.WeatherParser
import com.weatherwise.util.AppConstants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import retrofit2.HttpException
import java.io.IOException

class WeatherRepository(private val context: Context) {
    private val weatherService = RetrofitClient.getWeatherServiceKotlin()
    private val weatherDao = WeatherDatabase.getDatabase(context).weatherDao()

    private val _currentWeather = MutableStateFlow<CurrentWeather?>(null)
    val currentWeather: StateFlow<CurrentWeather?> = _currentWeather.asStateFlow()

    private val _hourlyForecast = MutableStateFlow<List<ForecastHourly>>(emptyList())
    val hourlyForecast: StateFlow<List<ForecastHourly>> = _hourlyForecast.asStateFlow()

    private val _dailyForecast = MutableStateFlow<List<ForecastDaily>>(emptyList())
    val dailyForecast: StateFlow<List<ForecastDaily>> = _dailyForecast.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _currentCityName = MutableStateFlow("")
    val currentCityName: StateFlow<String> = _currentCityName.asStateFlow()

    private var lastLat = 0.0
    private var lastLon = 0.0
    private var units = AppConstants.UNITS

    suspend fun fetchWeatherByCity(cityName: String) {
        _isLoading.value = true
        _errorMessage.value = null

        try {
            val geoResponse = weatherService.geocodeCity(cityName, 5, AppConstants.API_KEY)
            if (geoResponse.isSuccessful && geoResponse.body()?.isNotEmpty() == true) {
                val geo = geoResponse.body()!![0]
                lastLat = geo.lat
                lastLon = geo.lon
                _currentCityName.value = geo.getDisplayName()
                fetchWeatherByCoord(geo.lat, geo.lon)
            } else {
                _isLoading.value = false
                _errorMessage.value = "Kota \"$cityName\" tidak ditemukan."
            }
        } catch (e: Exception) {
            _isLoading.value = false
            _errorMessage.value = handleNetworkError(e)
            Log.e("WeatherRepository", "Geocoding failed", e)
        }
    }

    suspend fun fetchWeatherByCoord(lat: Double, lon: Double) {
        _isLoading.value = true
        lastLat = lat
        lastLon = lon

        try {
            // 1. Fetch Current Weather
            val currentResponse = weatherService.getCurrentWeatherByCoord(lat, lon, AppConstants.API_KEY, units, AppConstants.LANG)
            if (currentResponse.isSuccessful && currentResponse.body() != null) {
                val parsed = WeatherParser.parseCurrentWeather(currentResponse.body()!!)
                _currentWeather.value = parsed
                
                if (!parsed.cityName.isNullOrEmpty()) {
                    _currentCityName.value = parsed.cityName!!
                }

                // Cache to Room
                weatherDao.insertCachedWeather(CachedWeatherEntity(
                    cityName = parsed.cityName ?: "Unknown",
                    lat = lat,
                    lon = lon,
                    temperature = parsed.temperature,
                    condition = parsed.condition ?: "",
                    conditionIcon = parsed.conditionIcon ?: "",
                    humidity = parsed.humidity,
                    windSpeed = parsed.windSpeed,
                    timestamp = System.currentTimeMillis()
                ))

                // 2. Fetch Forecast (replaces OneCall for free tier compatibility)
                val forecastResponse = weatherService.getForecast(lat, lon, AppConstants.API_KEY, units, AppConstants.LANG)
                if (forecastResponse.isSuccessful && forecastResponse.body() != null) {
                    parseForecastData(forecastResponse.body()!!)
                } else {
                    Log.w("WeatherRepository", "Forecast failed: ${forecastResponse.code()}")
                }
            } else {
                _errorMessage.value = parseHttpError(currentResponse.code())
            }
        } catch (e: Exception) {
            _errorMessage.value = handleNetworkError(e)
            Log.e("WeatherRepository", "Fetch weather failed", e)
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun loadCachedWeather() {
        val cached = weatherDao.getCachedWeather()
        if (cached != null) {
            // Reconstruct a basic CurrentWeather object from cache
            val cachedWeather = CurrentWeather().apply {
                this.cityName = cached.cityName
                this.temperature = cached.temperature
                this.condition = cached.condition
                this.conditionIcon = cached.conditionIcon
                this.humidity = cached.humidity
                this.windSpeed = cached.windSpeed
            }
            _currentWeather.value = cachedWeather
            _currentCityName.value = cached.cityName
            lastLat = cached.lat
            lastLon = cached.lon
            
            // Trigger a background refresh
            fetchWeatherByCoord(cached.lat, cached.lon)
        }
    }

    private fun parseForecastData(response: com.weatherwise.model.ForecastResponse) {
        // Map to hourly (next 24 hours = 8 items of 3 hours)
        val hourly = response.list.take(8).map { item ->
            ForecastHourly(
                dt = item.dt,
                temp = item.main.temp,
                condition = item.weather[0].main,
                conditionIcon = item.weather[0].icon,
                humidity = item.main.humidity,
                windSpeed = item.wind.speed
            )
        }
        _hourlyForecast.value = hourly

        // Map to daily (group by date, get min/max)
        val dailyMap = mutableMapOf<String, ForecastDaily>()
        for (item in response.list) {
            val dateKey = item.dt_txt.substring(0, 10) // "YYYY-MM-DD"
            val temp = item.main.temp
            val condition = item.weather[0].main
            val icon = item.weather[0].icon
            
            if (!dailyMap.containsKey(dateKey)) {
                dailyMap[dateKey] = ForecastDaily(
                    dt = item.dt,
                    dateStr = dateKey,
                    tempMax = temp,
                    tempMin = temp,
                    condition = condition,
                    conditionIcon = icon,
                    humidity = item.main.humidity
                )
            } else {
                val existing = dailyMap[dateKey]!!
                dailyMap[dateKey] = existing.copy(
                    tempMax = maxOf(existing.tempMax, temp),
                    tempMin = minOf(existing.tempMin, temp)
                )
            }
        }
        _dailyForecast.value = dailyMap.values.toList().take(7)
    }

    private fun parseHttpError(code: Int): String = when (code) {
        401 -> "API Key tidak valid."
        404 -> "Data tidak ditemukan."
        429 -> "Terlalu banyak permintaan."
        else -> "Terjadi kesalahan (kode $code)."
    }

    private fun handleNetworkError(e: Exception): String = when (e) {
        is IOException -> "Tidak ada koneksi internet."
        is HttpException -> "Gagal terhubung ke server."
        else -> "Terjadi kesalahan: ${e.message}"
    }

    fun setUnits(newUnits: String) {
        units = newUnits
    }

    suspend fun refresh() {
        if (lastLat != 0.0 || lastLon != 0.0) {
            fetchWeatherByCoord(lastLat, lastLon)
        } else {
            _errorMessage.value = "Belum ada data untuk di-refresh."
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    suspend fun fetchGeocoding(query: String): List<GeocodingResponse> {
        return try {
            val response = weatherService.geocodeCity(query, 5, AppConstants.API_KEY)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("WeatherRepository", "Geocoding search failed", e)
            emptyList()
        }
    }
}
