package com.weatherwise.map

import android.util.Log
import com.google.android.gms.maps.model.Tile
import com.google.android.gms.maps.model.TileProvider
import com.weatherwise.BuildConfig
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL

class OpenWeatherTileProvider(
    private val layer: String,
    private val fallbackLayer: String = DEFAULT_FALLBACK_LAYER,
    private val tileSize: Int = 256,
    private val maxRetry: Int = 1
) : TileProvider {

    companion object {
        const val DEFAULT_FALLBACK_LAYER = "clouds_new"
        private const val TAG = "OpenWeatherTileProvider"
        private val HOSTS = listOf("a", "b", "c")
    }

    override fun getTile(x: Int, y: Int, zoom: Int): Tile? {
        val primary = fetchTileBytes(layer, x, y, zoom)
        if (primary != null) return Tile(tileSize, tileSize, primary)

        if (fallbackLayer != layer) {
            val fallback = fetchTileBytes(fallbackLayer, x, y, zoom)
            if (fallback != null) return Tile(tileSize, tileSize, fallback)
        }

        return TileProvider.NO_TILE
    }

    private fun fetchTileBytes(layerName: String, x: Int, y: Int, z: Int): ByteArray? {
        repeat(maxRetry + 1) { attempt ->
            runCatching {
                val host = HOSTS[(x + y + z + attempt) % HOSTS.size]
                val url = URL(buildTileUrl(host, layerName, x, y, z))
                (url.openConnection() as HttpURLConnection).run {
                    connectTimeout = 3_000
                    readTimeout = 3_000
                    useCaches = true
                    inputStream.use { input ->
                        ByteArrayOutputStream().use { output ->
                            input.copyTo(output)
                            output.toByteArray()
                        }
                    }
                }
            }.getOrNull()?.let { return it }
        }

        Log.w(TAG, "Tile gagal dimuat untuk layer=$layerName x=$x y=$y z=$z")
        return null
    }

    private fun buildTileUrl(host: String, layerName: String, x: Int, y: Int, z: Int): String {
        return "https://$host.tile.openweathermap.org/map/$layerName/$z/$x/$y.png?appid=${BuildConfig.WEATHER_API_KEY}"
    }
}
