package com.weatherwise.ui.components

import android.annotation.SuppressLint
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.weatherwise.BuildConfig

@Composable
fun WeatherLayerOsmMapCard(lat: Double, lon: Double, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val apiKey = BuildConfig.WEATHER_API_KEY

    val html = remember(apiKey) { buildLeafletHtml(apiKey) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Peta Cuaca", style = MaterialTheme.typography.titleMedium, color = Color.White)
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .height(260.dp),
                factory = {
                    WebView(context).apply {
                        setupForLeaflet()
                        loadDataWithBaseURL("https://localhost/", html, "text/html", "utf-8", null)
                    }
                },
                update = { webView ->
                    webView.evaluateJavascript("window.updateWeatherMap($lat, $lon);", null)
                }
            )
            Text(
                text = "© OpenStreetMap contributors · © OpenWeatherMap",
                color = Color.White.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }

    LaunchedEffect(apiKey) {
        // Keep composable stable; warning message rendered inside HTML if key is empty.
    }
}

@SuppressLint("SetJavaScriptEnabled")
private fun WebView.setupForLeaflet() {
    webViewClient = WebViewClient()
    settings.apply {
        javaScriptEnabled = true
        domStorageEnabled = true
        cacheMode = WebSettings.LOAD_DEFAULT
        allowFileAccess = false
        allowContentAccess = false
    }
    setBackgroundColor(android.graphics.Color.TRANSPARENT)
}

private fun buildLeafletHtml(apiKey: String): String = """
<!doctype html>
<html>
<head>
  <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
  <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
  <style>
    html, body { margin:0; padding:0; background:transparent; }
    #map { width:100%; height:100vh; }
    .warn { position:absolute; top:8px; left:8px; right:8px; z-index:9999; background:#b71c1c; color:#fff; padding:8px; border-radius:8px; font:12px sans-serif; }
  </style>
</head>
<body>
  <div id="map"></div>
  <div id="warn" class="warn" style="display:none;">API key OpenWeather belum diset.</div>
  <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
  <script>
    const apiKey = "$apiKey";
    if (!apiKey) document.getElementById('warn').style.display = 'block';

    const map = L.map('map', { zoomControl: false, attributionControl: false }).setView([0,0], 6);
    L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', { maxZoom: 19 }).addTo(map);

    const weather = L.tileLayer(
      `https://tile.openweathermap.org/map/precipitation_new/{z}/{x}/{y}.png?appid=${apiKey}`,
      { maxZoom: 19, opacity: 0.85 }
    ).addTo(map);

    window.updateWeatherMap = function(lat, lon) {
      if (!Number.isFinite(lat) || !Number.isFinite(lon)) return;
      map.setView([lat, lon], 6, { animate: false });
    }
  </script>
</body>
</html>
""".trimIndent()
