package com.weatherwise.util

import org.osmdroid.tileprovider.tilesource.XYTileSource

/**
 * Utility object untuk membuat tile source dari RainViewer
 * (Gratis, tanpa API key)
 */
object WeatherTileSource {
    
    // Available weather layers dari RainViewer
    object Layers {
        const val RADAR = "radar"          // Radar composite (curah hujan)
        const val COVERAGE = "coverage"    // Cakupan radar
    }
    
    /**
     * Membuat XYTileSource untuk RainViewer tiles
     * 
     * @param host Host dari RainViewer API (misalnya "https://tilecache.rainviewer.com")
     * @param path Path frame dari RainViewer API (misalnya "/v2/radar/1609401600")
     * @param colorScheme Skema warna (default: 2 = Original dark)
     * @param smooth Apakah smoothing aktif (default: 1)
     * @param snow Apakah warna salju aktif (default: 1)
     * @return XYTileSource yang bisa digunakan di OSMDroid MapView
     */
    fun createRadarTileSource(
        host: String,
        path: String,
        colorScheme: Int = 2,
        smooth: Int = 1,
        snow: Int = 1
    ): XYTileSource {
        // Format URL RainViewer: {host}{path}/256/{z}/{x}/{y}/{color}/{options}.png
        val baseUrl = "$host$path/256/"
        
        return XYTileSource(
            "RainViewer_$path",      // Name (unique per frame)
            3,                        // Min zoom level
            7,                        // Max zoom level (RainViewer max is 7)
            256,                      // Tile size in pixels
            ".png",                   // File extension
            arrayOf(baseUrl),         // Base URLs
            "/${colorScheme}/${smooth}_${snow}"  // Append color and options
        )
    }
    
    /**
     * Membuat XYTileSource untuk coverage layer
     */
    fun createCoverageTileSource(host: String): XYTileSource {
        val baseUrl = "${host}v2/coverage/0/256/"
        
        return XYTileSource(
            "RainViewer_Coverage",
            3,
            7,
            256,
            ".png",
            arrayOf(baseUrl),
            "/0/0_0"
        )
    }
    
    /**
     * Get human-readable name untuk layer
     */
    fun getLayerName(layer: String): String {
        return when (layer) {
            Layers.RADAR -> "Radar Hujan"
            Layers.COVERAGE -> "Cakupan Radar"
            else -> "Lainnya"
        }
    }
    
    /**
     * Color schemes yang tersedia dari RainViewer
     */
    object ColorSchemes {
        const val ORIGINAL_DARK = 2
        const val UNIVERSAL_BLUE = 3
        const val ORIGINAL_CH = 5
        const val METEOBLUE = 6
        const val NEXRAD = 7
        const val THE_WEATHER_NETWORK = 8
    }
}
