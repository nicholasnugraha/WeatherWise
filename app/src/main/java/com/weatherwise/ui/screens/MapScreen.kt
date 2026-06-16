package com.weatherwise.ui.screens

import android.Manifest
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.weatherwise.ui.components.GPSButton
import com.weatherwise.util.LocationManager
import com.weatherwise.util.WeatherTileSource
import com.weatherwise.util.hapticClick
import com.weatherwise.viewmodel.MapViewModel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.TilesOverlay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun MapScreen(
    viewModel: MapViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val currentLayer by viewModel.currentLayer.collectAsStateWithLifecycle()
    val host by viewModel.host.collectAsStateWithLifecycle()
    val radarFrames by viewModel.radarFrames.collectAsStateWithLifecycle()
    val currentFrameIndex by viewModel.currentFrameIndex.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val userLatitude by viewModel.userLatitude.collectAsStateWithLifecycle()
    val userLongitude by viewModel.userLongitude.collectAsStateWithLifecycle()
    val isLocating by viewModel.isLocating.collectAsStateWithLifecycle()
    val locationError by viewModel.locationError.collectAsStateWithLifecycle()
    
    // Permission state using Accompanist
    val locationPermissionState = rememberPermissionState(
        Manifest.permission.ACCESS_FINE_LOCATION
    )
    
    val locationManager = remember { LocationManager(context) }
    val mapViewRef = remember { mutableStateOf<MapView?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    
    // Handle location errors with snackbar
    LaunchedEffect(locationError) {
        if (locationError != null) {
            snackbarHostState.showSnackbar(
                message = locationError!!,
                duration = SnackbarDuration.Short
            )
            viewModel.setLocationError("") // Clear error after showing
        }
    }
    
    // Center map when user location is updated
    LaunchedEffect(userLatitude, userLongitude) {
        val lat = userLatitude
        val lon = userLongitude
        if (lat != null && lon != null) {
            mapViewRef.value?.let { mapView ->
                val userPoint = GeoPoint(lat, lon)
                mapView.controller.animateTo(userPoint, 10.0, 500L)
            }
        }
    }
    
    // GPS click handler with permission check
    val onGPSClick: () -> Unit = {
        if (locationPermissionState.status.isGranted) {
            if (!locationManager.isLocationEnabled()) {
                viewModel.setLocationError("GPS tidak aktif. Silakan aktifkan lokasi di pengaturan.")
            } else {
                viewModel.setLocating(true)
                coroutineScope.launch {
                    try {
                        // Try last known location first (fast)
                        val lastLocation = locationManager.getLastLocation()
                        if (lastLocation != null) {
                            viewModel.requestUserLocation(lastLocation.latitude, lastLocation.longitude)
                            viewModel.setLocating(false)
                        } else {
                            // Fallback to current location request (slower but more accurate)
                            val currentLocation = locationManager.getCurrentLocation().firstOrNull()
                            if (currentLocation != null) {
                                viewModel.requestUserLocation(currentLocation.latitude, currentLocation.longitude)
                            } else {
                                viewModel.setLocationError("Tidak dapat memperoleh lokasi. Coba lagi nanti.")
                            }
                            viewModel.setLocating(false)
                        }
                    } catch (e: Exception) {
                        viewModel.setLocationError("Error: ${e.message}")
                        viewModel.setLocating(false)
                    }
                }
            }
        } else {
            // Request permission
            locationPermissionState.launchPermissionRequest()
        }
    }
    
    // Permission state changes are handled by Accompanist automatically

    // Initialize OSMDroid configuration
    LaunchedEffect(Unit) {
        Configuration.getInstance().apply {
            userAgentValue = context.packageName
            load(context, context.getSharedPreferences("osmdroid", android.content.Context.MODE_PRIVATE))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Peta Cuaca") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Play/Pause Animation Button
                if (radarFrames.size > 1) {
                    FloatingActionButton(
                        onClick = { viewModel.togglePlayPause() },
                        containerColor = if (isPlaying) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = if (isPlaying) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.hapticClick { viewModel.togglePlayPause() }
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause Animation" else "Play Animation"
                        )
                    }
                }

                // GPS Location Button
                GPSButton(
                    isLocating = isLocating,
                    onClick = onGPSClick
                )
                
                // Layer Toggle Button
                FloatingActionButton(
                    onClick = { viewModel.cycleLayer() },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.hapticClick { viewModel.cycleLayer() }
                ) {
                    Icon(
                        Icons.Default.Layers,
                        contentDescription = "Ganti Layer"
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Map View
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    MapView(ctx).apply {
                        setTileSource(TileSourceFactory.MAPNIK)

                        // Default location: Jakarta, Indonesia
                        controller.setZoom(6.0)
                        controller.setCenter(GeoPoint(-6.2088, 106.8456))

                        // Enable zoom controls
                        setMultiTouchControls(true)
                        
                        // Store reference for GPS centering
                        mapViewRef.value = this
                    }
                },
                update = { mapView ->
                    // Only update if we have host data from RainViewer
                    if (host.isNotBlank() && radarFrames.isNotEmpty()) {
                        // Remove existing weather overlays
                        val overlaysToRemove = mapView.overlays.filterIsInstance<TilesOverlay>()
                        mapView.overlays.removeAll(overlaysToRemove)

                        // Get current frame for animation
                        val currentFrame = radarFrames.getOrNull(currentFrameIndex)
                        
                        // Create appropriate tile source based on current layer
                        val weatherTileSource = when (currentLayer) {
                            WeatherTileSource.Layers.RADAR -> {
                                if (currentFrame != null) {
                                    WeatherTileSource.createRadarTileSource(host, currentFrame.path)
                                } else null
                            }
                            WeatherTileSource.Layers.COVERAGE -> {
                                WeatherTileSource.createCoverageTileSource(host)
                            }
                            else -> null
                        }

                        // Add weather overlay if tile source is available
                        if (weatherTileSource != null) {
                            val weatherOverlay = TilesOverlay(
                                org.osmdroid.tileprovider.MapTileProviderBasic(mapView.context, weatherTileSource),
                                mapView.context
                            ).apply {
                                loadingBackgroundColor = android.graphics.Color.TRANSPARENT
                                loadingLineColor = android.graphics.Color.TRANSPARENT
                            }

                            mapView.overlays.add(weatherOverlay)
                        }
                        mapView.invalidate()
                    }
                }
            )

            // Layer indicator (floating on top of map)
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f),
                shape = MaterialTheme.shapes.medium,
                tonalElevation = 4.dp
            ) {
                Text(
                    text = "Layer: ${WeatherTileSource.getLayerName(currentLayer)}",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            // Loading indicator when RainViewer data hasn't loaded yet
            if (host.isBlank()) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}
