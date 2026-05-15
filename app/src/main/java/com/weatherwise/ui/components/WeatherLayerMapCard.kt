package com.weatherwise.ui.components

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.TileOverlay
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberTileOverlayState
import com.weatherwise.map.OpenWeatherTileProvider
import com.weatherwise.map.TileSources
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged

private const val PREFS_NAME = "radar_preferences"
private const val PREF_OPACITY = "last_opacity"

@Composable
fun WeatherLayerMapCard(lat: Double, lon: Double, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }

    var enabled by remember { mutableStateOf(true) }
    var opacity by remember { mutableFloatStateOf(prefs.getFloat(PREF_OPACITY, 0.85f).coerceIn(0.2f, 1f)) }
    var isLoading by remember { mutableStateOf(true) }
    var tileError by remember { mutableStateOf<String?>(null) }

    val tileSource = remember { TileSources.default() }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(lat, lon), 6f)
    }
    val tileProvider = remember(tileSource) {
        OpenWeatherTileProvider(context = context, tileSource = tileSource, onTileError = { tileError = it })
    }

    LaunchedEffect(opacity) {
        prefs.edit().putFloat(PREF_OPACITY, opacity).apply()
    }

    LaunchedEffect(cameraPositionState) {
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
                delay(250)
                isLoading = false
            }
    }

    Card(modifier = modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f))) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Peta Cuaca (OSM)", style = MaterialTheme.typography.titleMedium, color = Color.White)

            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Layer aktif", color = Color.White)
                Switch(checked = enabled, onCheckedChange = { enabled = it })
            }

            Text("Opacity ${(opacity * 100).toInt()}%", color = Color.White)
            Slider(value = opacity, onValueChange = { opacity = it }, valueRange = 0.2f..1f)

            tileError?.let {
                Text("Tile tidak tersedia sementara. Coba lagi beberapa saat.", color = Color(0xFFFFCDD2))
            }

            Box {
                GoogleMap(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp).height(240.dp),
                    cameraPositionState = cameraPositionState,
                    uiSettings = MapUiSettings(zoomControlsEnabled = false),
                    properties = MapProperties(isMyLocationEnabled = false)
                ) {
                    if (enabled) {
                        TileOverlay(
                            tileOverlayState = rememberTileOverlayState(),
                            tileProvider = tileProvider,
                            transparency = 1f - opacity,
                            zIndex = 1f,
                            fadeIn = true
                        )
                    }
                }
                if (isLoading && enabled) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color.White, trackColor = Color.White.copy(alpha = 0.25f))
                }
            }
            Text(tileSource.attribution, color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.bodySmall)
        }
    }
}
