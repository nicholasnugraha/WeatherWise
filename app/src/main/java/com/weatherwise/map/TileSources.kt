package com.weatherwise.map

import com.weatherwise.BuildConfig

object TileSources {
    val osm = object : TileSource {
        override val id: String = "osm"
        override val attribution: String = "© OpenStreetMap contributors"
        override val tileBaseUrl: String = BuildConfig.MAP_TILE_BASE_URL
        override val apiKey: String? = BuildConfig.MAP_TILE_API_KEY.ifBlank { null }
    }

    fun default(): TileSource = osm
}
