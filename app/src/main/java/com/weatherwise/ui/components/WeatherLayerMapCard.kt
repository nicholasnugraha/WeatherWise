package com.weatherwise.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.TileOverlay
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberTileOverlayState
import com.weatherwise.map.OpenWeatherTileProvider

@Composable
fun WeatherLayerMapCard(
    lat: Double,
    lon: Double,
    modifier: Modifier = Modifier
) {
    val layers = listOf("precipitation_new" to "Precipitation", "clouds_new" to "Clouds")
    var selectedLayer by remember { mutableStateOf(layers.first().first) }
    var enabled by remember { mutableStateOf(true) }
    var opacity by remember { mutableFloatStateOf(0.75f) }

    val cameraPositionState = rememberCameraPositionState {
        position = com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(LatLng(lat, lon), 6f)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Weather Layer Map", style = MaterialTheme.typography.titleMedium, color = Color.White)

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

            GoogleMap(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .height(240.dp),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(zoomControlsEnabled = false),
                properties = MapProperties(isMyLocationEnabled = false)
            ) {
                if (enabled) {
                    TileOverlay(
                        tileOverlayState = rememberTileOverlayState(),
                        tileProvider = OpenWeatherTileProvider(layer = selectedLayer),
                        transparency = 1f - opacity,
                        zIndex = 2f,
                        fadeIn = true
                    )
                }
            }

            Text(
                text = "Map data © OpenWeather (Weather Maps 2.0).",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.75f)
            )
        }
    }
}
