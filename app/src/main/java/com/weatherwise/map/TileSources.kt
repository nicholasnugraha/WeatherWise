package com.weatherwise.map

import com.weatherwise.BuildConfig

enum class WeatherLayer(val id: String, val label: String, val path: String) {
    PRECIPITATION("precipitation_new", "Hujan", "precipitation_new"),
    CLOUDS("clouds_new", "Awan", "clouds_new"),
    WIND("wind_new", "Angin", "wind_new"),
    PRESSURE("pressure_new", "Tekanan", "pressure_new")
}

object TileSources {
    fun weather(layer: WeatherLayer): TileSource = object : TileSource {
        override val id: String = layer.id
        override val attribution: String = "© OpenWeatherMap"
        override val tileBaseUrl: String = "${BuildConfig.MAP_TILE_BASE_URL.trimEnd('/')}/${layer.path}"
        override val apiKey: String? = BuildConfig.MAP_TILE_API_KEY.ifBlank { null }
    }

    fun default(): TileSource = weather(WeatherLayer.PRECIPITATION)
}
