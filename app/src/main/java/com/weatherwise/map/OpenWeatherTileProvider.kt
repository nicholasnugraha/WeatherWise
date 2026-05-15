package com.weatherwise.map

import android.content.Context
import android.util.Log
import android.util.LruCache
import com.google.android.gms.maps.model.Tile
import com.google.android.gms.maps.model.TileProvider
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap

class OpenWeatherTileProvider(
    context: Context,
    private val tileSource: TileSource,
    private val tileSize: Int = 256,
    private val maxRetry: Int = 2,
    private val onTileError: ((String) -> Unit)? = null
) : TileProvider {

    private val appContext = context.applicationContext
    private val tileCacheDir = File(appContext.cacheDir, "map_tiles").apply { mkdirs() }
    private val memoryCache = object : LruCache<String, ByteArray>(8 * 1024 * 1024) {
        override fun sizeOf(key: String, value: ByteArray): Int = value.size
    }
    private val inFlightRequests = ConcurrentHashMap<String, Any>()

    override fun getTile(x: Int, y: Int, zoom: Int): Tile {
        val bytes = fetchTileBytes(x, y, zoom)
        return if (bytes != null) Tile(tileSize, tileSize, bytes) else TileProvider.NO_TILE
    }

    fun prefetchAround(centerX: Int, centerY: Int, zoom: Int, radius: Int = 1) {
        for (dx in -radius..radius) {
            for (dy in -radius..radius) {
                fetchTileBytes(centerX + dx, centerY + dy, zoom)
            }
        }
    }

    private fun fetchTileBytes(x: Int, y: Int, z: Int): ByteArray? {
        val cacheKey = "${tileSource.id}/$z/$x/$y"
        memoryCache.get(cacheKey)?.let { return it }
        readDiskCache(cacheKey)?.let {
            memoryCache.put(cacheKey, it)
            return it
        }

        val lock = inFlightRequests.putIfAbsent(cacheKey, Any())
        if (lock != null) return null

        try {
            repeat(maxRetry + 1) { attempt ->
                val bytes = runCatching {
                    val connection = URL(tileSource.tileUrl(x, y, z)).openConnection() as HttpURLConnection
                    connection.connectTimeout = 3_000
                    connection.readTimeout = 3_000
                    connection.inputStream.use { input ->
                        ByteArrayOutputStream().use { out ->
                            input.copyTo(out)
                            out.toByteArray()
                        }
                    }
                }.getOrNull()

                if (bytes != null) {
                    memoryCache.put(cacheKey, bytes)
                    writeDiskCache(cacheKey, bytes)
                    return bytes
                }

                if (attempt < maxRetry) Thread.sleep(250L * (attempt + 1))
            }
        } finally {
            inFlightRequests.remove(cacheKey)
        }

        val message = "Tile provider error (${tileSource.id})."
        onTileError?.invoke(message)
        Log.w("TileProvider", "Failed tile for ${tileSource.id}: x=$x y=$y z=$z")
        return null
    }

    private fun readDiskCache(cacheKey: String): ByteArray? {
        val file = File(tileCacheDir, sha256(cacheKey))
        if (!file.exists()) return null
        return runCatching { file.readBytes() }.getOrNull()
    }

    private fun writeDiskCache(cacheKey: String, bytes: ByteArray) {
        runCatching { File(tileCacheDir, sha256(cacheKey)).writeBytes(bytes) }
    }

    private fun sha256(value: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(value.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }
}
