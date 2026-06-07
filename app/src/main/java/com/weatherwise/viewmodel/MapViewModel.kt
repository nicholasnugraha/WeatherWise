package com.weatherwise.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weatherwise.api.RainViewerApiService
import com.weatherwise.api.RetrofitClient
import com.weatherwise.model.MapFrame
import com.weatherwise.util.WeatherTileSource
import kotlinx.coroutines.launch

class MapViewModel : ViewModel() {
    
    private val _currentLayer = MutableLiveData<String>(WeatherTileSource.Layers.RADAR)
    val currentLayer: LiveData<String> = _currentLayer
    
    private val _host = MutableLiveData<String>("")
    val host: LiveData<String> = _host
    
    private val _radarFrame = MutableLiveData<MapFrame?>(null)
    val radarFrame: LiveData<MapFrame?> = _radarFrame
    
    // GPS Location state
    private val _userLatitude = MutableLiveData<Double?>(null)
    val userLatitude: LiveData<Double?> = _userLatitude
    
    private val _userLongitude = MutableLiveData<Double?>(null)
    val userLongitude: LiveData<Double?> = _userLongitude
    
    private val _isLocating = MutableLiveData<Boolean>(false)
    val isLocating: LiveData<Boolean> = _isLocating
    
    private val _locationError = MutableLiveData<String?>(null)
    val locationError: LiveData<String?> = _locationError
    
    init {
        fetchRainViewerData()
    }
    
    fun requestUserLocation(latitude: Double, longitude: Double) {
        _userLatitude.value = latitude
        _userLongitude.value = longitude
        _locationError.value = null
    }
    
    fun setLocationError(error: String) {
        _locationError.value = error
    }
    
    fun setLocating(isLocating: Boolean) {
        _isLocating.value = isLocating
    }
    
    private fun fetchRainViewerData() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.getRainViewerClient()
                    .create(RainViewerApiService::class.java)
                    .getWeatherMaps()
                    .execute()
                
                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!
                    _host.value = data.host
                    
                    // Ambil frame radar terakhir yang tersedia
                    val latestRadar = data.radar.past.lastOrNull()
                    _radarFrame.value = latestRadar
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    fun setLayer(layer: String) {
        if (_currentLayer.value != layer) {
            _currentLayer.value = layer
        }
    }
    
    fun cycleLayer() {
        val current = _currentLayer.value ?: WeatherTileSource.Layers.RADAR
        val nextLayer = when (current) {
            WeatherTileSource.Layers.RADAR -> WeatherTileSource.Layers.COVERAGE
            WeatherTileSource.Layers.COVERAGE -> WeatherTileSource.Layers.RADAR
            else -> WeatherTileSource.Layers.RADAR
        }
        setLayer(nextLayer)
    }
}
