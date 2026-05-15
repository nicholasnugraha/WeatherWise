package com.weatherwise.map

import android.content.Context
import android.util.Log
import android.util.LruCache
import com.google.android.gms.maps.model.Tile
import com.google.android.gms.maps.model.TileProvider
import com.weatherwise.config.OpenWeatherTileConfig
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class OpenWeatherTileProvider(
    context: Context,
    private val layer: String,
    private val fallbackLayer: String = DEFAULT_FALLBACK_LAYER,
    private val tileSize: Int = 256,
    private val maxRetry: Int = 1
) : TileProvider {

    private val appContext = context.applicationContext
    private val tileCacheDir = File(appContext.cacheDir, "owm_tiles").apply { mkdirs() }
    private val memoryCache = object : LruCache<String, ByteArray>(MEMORY_CACHE_BYTES) {
        override fun sizeOf(key: String, value: ByteArray): Int = value.size
    }
    private val prefetchExecutor = Executors.newSingleThreadExecutor()
    private val inFlightRequests = ConcurrentHashMap<String, Any>()
    private val recentRequests = ConcurrentHashMap<String, Long>()

    companion object {
        const val DEFAULT_FALLBACK_LAYER = "clouds_new"
        private const val TAG = "OpenWeatherTileProvider"
        private const val MEMORY_CACHE_BYTES = 8 * 1024 * 1024
        private const val DISK_CACHE_MAX_BYTES = 40L * 1024L * 1024L
        private const val DUPLICATE_WINDOW_MS = 1_500L

        private val layerTtlMs = mapOf(
            "precipitation_new" to TimeUnit.MINUTES.toMillis(10),
            "clouds_new" to TimeUnit.MINUTES.toMillis(30),
            "temp_new" to TimeUnit.MINUTES.toMillis(10),
            "wind_new" to TimeUnit.MINUTES.toMillis(10)
        )
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

    fun prefetchAround(centerX: Int, centerY: Int, zoom: Int, radius: Int = 1) {
        prefetchExecutor.execute {
            for (dx in -radius..radius) {
                for (dy in -radius..radius) {
                    fetchTileBytes(layer, centerX + dx, centerY + dy, zoom)
                }
            }
        }
    }

    private fun fetchTileBytes(layerName: String, x: Int, y: Int, z: Int): ByteArray? {
        val cacheKey = "$layerName/$z/$x/$y"
        memoryCache.get(cacheKey)?.let { return it }

        readValidDiskCache(cacheKey, layerName)?.let {
            memoryCache.put(cacheKey, it)
            return it
        }

        val now = System.currentTimeMillis()
        val lastRequestAt = recentRequests[cacheKey] ?: 0L
        if (now - lastRequestAt < DUPLICATE_WINDOW_MS) {
            return null
        }

        val lock = inFlightRequests.putIfAbsent(cacheKey, Any())
        if (lock != null) return null
        recentRequests[cacheKey] = now

        try {
        repeat(maxRetry + 1) { attempt ->
            runCatching {
                val url = URL(buildTileUrl(layerName, x, y, z))
                (url.openConnection() as HttpURLConnection).run {
                    connectTimeout = 3_000
                    readTimeout = 3_000
                    useCaches = true
                    setRequestProperty("Cache-Control", "max-age=${getTtlMillis(layerName) / 1000}")
                    inputStream.use { input ->
                        ByteArrayOutputStream().use { output ->
                            input.copyTo(output)
                            output.toByteArray().also { bytes ->
                                memoryCache.put(cacheKey, bytes)
                                writeDiskCache(cacheKey, bytes)
                            }
                        }
                    }
                }
            }.getOrNull()?.let { return it }
        }
        } finally {
            inFlightRequests.remove(cacheKey)
        }

        Log.w(TAG, "Tile gagal dimuat untuk layer=$layerName x=$x y=$y z=$z")
        return null
    }

    private fun readValidDiskCache(cacheKey: String, layerName: String): ByteArray? {
        val file = File(tileCacheDir, sha256(cacheKey))
        if (!file.exists()) return null
        val age = System.currentTimeMillis() - file.lastModified()
        if (age > getTtlMillis(layerName)) {
            file.delete()
            return null
        }
        return runCatching {
            file.setLastModified(System.currentTimeMillis())
            file.readBytes()
        }.getOrNull()
    }

    private fun writeDiskCache(cacheKey: String, bytes: ByteArray) {
        runCatching {
            File(tileCacheDir, sha256(cacheKey)).writeBytes(bytes)
            evictDiskCacheIfNeeded()
        }
    }

    private fun evictDiskCacheIfNeeded() {
        val files = tileCacheDir.listFiles()?.toList().orEmpty()
        var totalSize = files.sumOf { it.length() }
        if (totalSize <= DISK_CACHE_MAX_BYTES) return

        files.sortedBy { it.lastModified() }.forEach { file ->
            if (totalSize <= DISK_CACHE_MAX_BYTES) return
            val size = file.length()
            if (file.delete()) totalSize -= size
        }
    }

    private fun getTtlMillis(layerName: String): Long =
        layerTtlMs[layerName] ?: TimeUnit.MINUTES.toMillis(15)

    private fun sha256(value: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(value.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }

    private fun buildTileUrl(layerName: String, x: Int, y: Int, z: Int): String {
        val base = OpenWeatherTileConfig.TILE_BASE_URL.removeSuffix("/map")
        return "$base/map/$layerName/$z/$x/$y.png?appid=${OpenWeatherTileConfig.API_KEY}"
    }
}
