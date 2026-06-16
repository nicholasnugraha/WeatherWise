package com.weatherwise.viewmodel

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.weatherwise.api.WeatherRepository
import com.weatherwise.model.GeocodingResponse
import com.weatherwise.model.WeatherAlert
import com.weatherwise.util.AppConstants
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class WeatherViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs: SharedPreferences = application.getSharedPreferences("weatherwise_prefs", android.content.Context.MODE_PRIVATE)
    private val repository = WeatherRepository(application)

    val currentWeather = repository.currentWeather
    val hourlyForecast = repository.hourlyForecast
    val dailyForecast = repository.dailyForecast
    val isLoading = repository.isLoading
    val errorMessage = repository.errorMessage
    val cityName = repository.currentCityName

    // Search & Autocomplete State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchSuggestions = MutableStateFlow<List<GeocodingResponse>>(emptyList())
    val searchSuggestions: StateFlow<List<GeocodingResponse>> = _searchSuggestions.asStateFlow()

    // Weather Alerts State
    private val _weatherAlerts = MutableStateFlow<List<WeatherAlert>>(emptyList())
    val weatherAlerts: StateFlow<List<WeatherAlert>> = _weatherAlerts.asStateFlow()

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

        // Setup search debounce
        _searchQuery
            .debounce(500)
            .filter { it.trim().length >= 2 }
            .distinctUntilChanged()
            .onEach { query ->
                fetchGeocodingSuggestions(query.trim())
            }
            .launchIn(viewModelScope)
    }

    private suspend fun fetchGeocodingSuggestions(query: String) {
        try {
            val suggestions = repository.fetchGeocoding(query)
            _searchSuggestions.value = suggestions
        } catch (e: Exception) {
            _searchSuggestions.value = emptyList()
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _searchSuggestions.value = emptyList()
        }
    }

    fun selectSuggestion(suggestion: GeocodingResponse) {
        _searchQuery.value = suggestion.name
        _searchSuggestions.value = emptyList()
        searchByCoord(suggestion.lat, suggestion.lon)
    }

    fun searchByCity(cityName: String) {
        if (cityName.isBlank()) return
        _searchQuery.value = ""
        _searchSuggestions.value = emptyList()
        prefs.edit().putString("last_city", cityName.trim()).apply()
        viewModelScope.launch {
            repository.fetchWeatherByCity(cityName.trim())
        }
    }

    fun searchByCoord(lat: Double, lon: Double) {
        _searchQuery.value = ""
        _searchSuggestions.value = emptyList()
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