package com.weatherwise.viewmodel

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.weatherwise.api.WeatherRepository
import com.weatherwise.util.AppConstants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WeatherViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs: SharedPreferences = application.getSharedPreferences("weatherwise_prefs", android.content.Context.MODE_PRIVATE)
    private val repository = WeatherRepository(application)

    val currentWeather = repository.currentWeather
    val hourlyForecast = repository.hourlyForecast
    val dailyForecast = repository.dailyForecast
    val isLoading = repository.isLoading
    val errorMessage = repository.errorMessage
    val cityName = repository.currentCityName

    private val _isDataReady = MutableStateFlow(false)
    val isDataReady: StateFlow<Boolean> = _isDataReady.asStateFlow()

    init {
        repository.setUnits(prefs.getString("units", "metric") ?: "metric")
        
        // Load cached data first for instant UI
        viewModelScope.launch {
            repository.loadCachedWeather()
        }
        
        // If no cache, load last city
        viewModelScope.launch {
            val lastCity = prefs.getString("last_city", AppConstants.DEFAULT_CITY) ?: AppConstants.DEFAULT_CITY
            if (repository.currentWeather.value == null) {
                repository.fetchWeatherByCity(lastCity)
            }
        }
    }

    fun searchByCity(cityName: String) {
        if (cityName.isBlank()) return
        prefs.edit().putString("last_city", cityName.trim()).apply()
        viewModelScope.launch {
            repository.fetchWeatherByCity(cityName.trim())
        }
    }

    fun searchByCoord(lat: Double, lon: Double) {
        viewModelScope.launch {
            repository.fetchWeatherByCoord(lat, lon)
        }
    }

    fun refresh() {
        viewModelScope.launch {
            repository.refresh()
        }
    }

    fun clearError() {
        repository.clearError()
    }

    fun toggleUnits() {
        val current = prefs.getString("units", "metric") ?: "metric"
        val next = if (current == "metric") "imperial" else "metric"
        prefs.edit().putString("units", next).apply()
        repository.setUnits(next)
        viewModelScope.launch {
            repository.refresh()
        }
    }

    fun isCelsius(): Boolean = (prefs.getString("units", "metric") ?: "metric") == "metric"
}
