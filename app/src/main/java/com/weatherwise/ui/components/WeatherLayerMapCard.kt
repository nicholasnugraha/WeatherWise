package com.weatherwise.ui.components

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.TileOverlay
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberTileOverlayState
import com.weatherwise.config.OpenWeatherTileConfig
import com.weatherwise.map.OpenWeatherTileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private const val PREFS_NAME = "radar_preferences"
private const val PREF_LAYER = "last_layer"
private const val PREF_OPACITY = "last_opacity"

@Composable
fun WeatherLayerMapCard(
    lat: Double,
    lon: Double,
    modifier: Modifier = Modifier
) {
    val layers = listOf(
        "precipitation_new" to "Radar",
        "clouds_new" to "Clouds",
        "wind_new" to "Wind",
        "pressure_new" to "Pressure"
    )
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }

    var selectedLayer by remember {
        mutableStateOf(prefs.getString(PREF_LAYER, layers.first().first) ?: layers.first().first)
    }
    var enabled by remember { mutableStateOf(true) }
    var opacity by remember {
        mutableFloatStateOf(prefs.getFloat(PREF_OPACITY, 0.75f).coerceIn(0.2f, 1f))
    }
    var isLoading by remember { mutableStateOf(true) }
    var lastUpdatedText by remember { mutableStateOf<String?>(null) }
    var radarError by remember { mutableStateOf<String?>(null) }

    val cameraPositionState = rememberCameraPositionState {
        position = com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(LatLng(lat, lon), 6f)
    }
    val tileProvider = remember(selectedLayer) {
        OpenWeatherTileProvider(context = context, layer = selectedLayer)
    }

    LaunchedEffect(selectedLayer, opacity) {
        prefs.edit().putString(PREF_LAYER, selectedLayer).putFloat(PREF_OPACITY, opacity).apply()
    }

    LaunchedEffect(selectedLayer) {
        isLoading = true
        radarError = null
        val metadata = fetchLayerMetadata(selectedLayer)
        lastUpdatedText = metadata.lastUpdated
        radarError = metadata.errorMessage
        delay(500)
        isLoading = false
    }

    LaunchedEffect(cameraPositionState, selectedLayer) {
        snapshotFlow { cameraPositionState.position }
            .debounce(200)
            .distinctUntilChanged()
            .collect { pos ->
                isLoading = true
                val center = pos.target
                val zoom = pos.zoom.toInt().coerceIn(0, 18)
                val world = 1 shl zoom
                val sinLat = kotlin.math.sin(Math.toRadians(center.latitude))
                val x = (((center.longitude + 180.0) / 360.0) * world).toInt()
                val y = ((0.5 - kotlin.math.ln((1 + sinLat) / (1 - sinLat)) / (4 * Math.PI)) * world).toInt()
                tileProvider.prefetchAround(x, y, zoom, radius = 1)
                delay(350)
                isLoading = false
            }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Peta Radar Cuaca", style = MaterialTheme.typography.titleMedium, color = Color.White)
            Button(onClick = {
                enabled = true
                selectedLayer = "precipitation_new"
            }) { Text("Radar") }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                layers.forEach { (layerKey, label) ->
                    FilterChip(
                        selected = selectedLayer == layerKey,
                        onClick = { selectedLayer = layerKey },
                        label = { Text(label) }
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Layer aktif", color = Color.White)
                Switch(checked = enabled, onCheckedChange = { enabled = it })
            }

            Text("Opacity ${(opacity * 100).toInt()}%", color = Color.White)
            Slider(value = opacity, onValueChange = { opacity = it }, valueRange = 0.2f..1f)

            lastUpdatedText?.let {
                Text("Last updated: $it", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))
            }

            radarError?.let {
                Text(it, color = Color(0xFFFFCDD2), style = MaterialTheme.typography.bodyMedium)
            }

            Box {
                GoogleMap(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                        .height(240.dp),
                    cameraPositionState = cameraPositionState,
                    uiSettings = MapUiSettings(zoomControlsEnabled = false),
                    properties = MapProperties(isMyLocationEnabled = false)
                ) {
                    if (enabled && radarError == null) {
                        TileOverlay(
                            tileOverlayState = rememberTileOverlayState(),
                            tileProvider = tileProvider,
                            transparency = 1f - opacity,
                            zIndex = 2f,
                            fadeIn = true
                        )
                    }
                }
                if (isLoading && enabled) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.Center),
                        color = Color.White,
                        trackColor = Color.White.copy(alpha = 0.25f)
                    )
                }
            }
        }
    }
}

data class RadarLayerMetadata(
    val lastUpdated: String?,
    val errorMessage: String?
)

private suspend fun fetchLayerMetadata(layer: String): RadarLayerMetadata = withContext(Dispatchers.IO) {
    val base = OpenWeatherTileConfig.TILE_BASE_URL.removeSuffix("/map")
    val metadataUrl = "$base/map/$layer.json?appid=${OpenWeatherTileConfig.API_KEY}"
    runCatching {
        val connection = (URL(metadataUrl).openConnection() as HttpURLConnection).apply {
            connectTimeout = 4_000
            readTimeout = 4_000
        }
        connection.useCaches = false
        val code = connection.responseCode
        when (code) {
            200 -> {
                val updatedUnix = connection.getHeaderField("X-Last-Updated")?.toLongOrNull()
                val updated = updatedUnix?.let {
                    DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")
                        .withZone(ZoneId.systemDefault())
                        .format(Instant.ofEpochSecond(it))
                }
                RadarLayerMetadata(updated, null)
            }
            429 -> RadarLayerMetadata(null, "batas kuota tercapai")
            else -> RadarLayerMetadata(null, "data radar tidak tersedia")
        }
    }.getOrElse {
        RadarLayerMetadata(null, "data radar tidak tersedia")
    }
}
