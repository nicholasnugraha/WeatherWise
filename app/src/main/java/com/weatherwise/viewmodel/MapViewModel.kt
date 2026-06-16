package com.weatherwise.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weatherwise.api.RainViewerApiService
import com.weatherwise.api.RetrofitClient
import com.weatherwise.model.MapFrame
import com.weatherwise.util.WeatherTileSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MapViewModel : ViewModel() {
    
    private val _currentLayer = MutableStateFlow(WeatherTileSource.Layers.RADAR)
    val currentLayer: StateFlow<String> = _currentLayer.asStateFlow()
    
    private val _host = MutableStateFlow("")
    val host: StateFlow<String> = _host.asStateFlow()
    
    private val _radarFrames = MutableStateFlow<List<MapFrame>>(emptyList())
    val radarFrames: StateFlow<List<MapFrame>> = _radarFrames.asStateFlow()
    
    private val _currentFrameIndex = MutableStateFlow(0)
    val currentFrameIndex: StateFlow<Int> = _currentFrameIndex.asStateFlow()
    
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    
    // GPS Location state
    private val _userLatitude = MutableStateFlow<Double?>(null)
    val userLatitude: StateFlow<Double?> = _userLatitude.asStateFlow()
    
    private val _userLongitude = MutableStateFlow<Double?>(null)
    val userLongitude: StateFlow<Double?> = _userLongitude.asStateFlow()
    
    private val _isLocating = MutableStateFlow(false)
    val isLocating: StateFlow<Boolean> = _isLocating.asStateFlow()
    
    private val _locationError = MutableStateFlow<String?>(null)
    val locationError: StateFlow<String?> = _locationError.asStateFlow()
    
    init {
        fetchRainViewerData()
        startAnimationLoop()
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
                    
                    // Ambil beberapa frame terakhir untuk animasi (misal 5 frame)
                    val frames = data.radar.past.takeLast(5)
                    _radarFrames.value = frames
                    if (frames.isNotEmpty()) {
                        _currentFrameIndex.value = frames.size - 1
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private fun startAnimationLoop() {
        viewModelScope.launch {
            while (isActive) {
                if (_isPlaying.value && _radarFrames.value.isNotEmpty()) {
                    delay(1000) // 1 detik per frame
                    _currentFrameIndex.value = (_currentFrameIndex.value + 1) % _radarFrames.value.size
                } else {
                    delay(500) // Polling lebih lambat saat tidak play
                }
            }
        }
    }
    
    fun togglePlayPause() {
        _isPlaying.value = !_isPlaying.value
    }
    
    fun setLayer(layer: String) {
        if (_currentLayer.value != layer) {
            _currentLayer.value = layer
        }
    }
    
    fun cycleLayer() {
        val current = _currentLayer.value
        val nextLayer = when (current) {
            WeatherTileSource.Layers.RADAR -> WeatherTileSource.Layers.COVERAGE
            WeatherTileSource.Layers.COVERAGE -> WeatherTileSource.Layers.RADAR
            else -> WeatherTileSource.Layers.RADAR
        }
        setLayer(nextLayer)
    }
}