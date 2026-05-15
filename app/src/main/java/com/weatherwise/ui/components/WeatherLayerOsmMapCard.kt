package com.weatherwise.ui.components

import android.content.Context
import android.graphics.Color as AndroidColor
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import com.weatherwise.map.TileSources
import kotlinx.coroutines.delay
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.MapTileProviderBasic
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.TilesOverlay

private const val PREFS_NAME = "radar_preferences"
private const val PREF_OPACITY = "last_opacity"

@Composable
fun WeatherLayerOsmMapCard(lat: Double, lon: Double, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }

    var enabled by remember { mutableStateOf(true) }
    var opacity by remember { mutableFloatStateOf(prefs.getFloat(PREF_OPACITY, 0.85f).coerceIn(0.2f, 1f)) }
    var isLoading by remember { mutableStateOf(true) }
    var tileError by remember { mutableStateOf<String?>(null) }

    val tileSource = remember { TileSources.default() }

    val mapView = remember {
        Configuration.getInstance().load(context, prefs)
        MapView(context).apply {
            setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(6.0)
            controller.setCenter(GeoPoint(lat, lon))
        }
    }

    val weatherOverlay = remember(mapView, tileSource) {
        runCatching {
            val provider = MapTileProviderBasic(context, object : OnlineTileSourceBase(
                tileSource.id,
                0,
                18,
                256,
                ".png",
                arrayOf(tileSource.tileBaseUrl.trimEnd('/')),
                tileSource.attribution
            ) {
                override fun getTileURLString(aMapTileIndex: Long): String {
                    val z = org.osmdroid.util.MapTileIndex.getZoom(aMapTileIndex)
                    val x = org.osmdroid.util.MapTileIndex.getX(aMapTileIndex)
                    val y = org.osmdroid.util.MapTileIndex.getY(aMapTileIndex)
                    return tileSource.tileUrl(x, y, z)
                }
            })
            TilesOverlay(provider, context).also {
                mapView.overlays.add(it)
            }
        }.onFailure {
            tileError = "Tile tidak tersedia sementara. Coba lagi beberapa saat."
        }.getOrNull()
    }

    DisposableEffect(mapView) {
        mapView.onResume()
        onDispose { mapView.onPause() }
    }

    LaunchedEffect(lat, lon) {
        mapView.controller.setCenter(GeoPoint(lat, lon))
    }

    LaunchedEffect(opacity) {
        prefs.edit().putFloat(PREF_OPACITY, opacity).apply()
    }

    LaunchedEffect(enabled, opacity, weatherOverlay) {
        weatherOverlay?.isEnabled = enabled
        weatherOverlay?.colorFilter = PorterDuffColorFilter(
            AndroidColor.argb((opacity * 255).toInt(), 255, 255, 255),
            PorterDuff.Mode.MULTIPLY
        )
        mapView.invalidate()
        isLoading = true
        delay(300)
        isLoading = false
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
                AndroidView(
                    factory = { mapView },
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp).height(240.dp),
                    update = {
                        it.controller.setCenter(GeoPoint(lat, lon))
                    }
                )
                if (isLoading && enabled) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color.White, trackColor = Color.White.copy(alpha = 0.25f))
                }
            }
            Text(tileSource.attribution, color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.bodySmall)
        }
    }
}
