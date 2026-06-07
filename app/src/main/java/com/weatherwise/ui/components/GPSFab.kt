package com.weatherwise.ui.components

import android.Manifest
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.weatherwise.util.LocationManager
import com.weatherwise.viewmodel.WeatherViewModel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

/**
 * GPS Floating Action Button for HomeScreen
 * Handles location permission, GPS request, and weather fetching by coordinates
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun GPSFab(
    viewModel: WeatherViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val locationManager = remember { LocationManager(context) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    
    // Local state for loading indicator
    var isLocating by remember { mutableStateOf(false) }
    var locationError by remember { mutableStateOf<String?>(null) }
    
    // Permission state using Accompanist
    val locationPermissionState = rememberPermissionState(
        Manifest.permission.ACCESS_FINE_LOCATION
    )
    
    // Handle location errors with snackbar
    LaunchedEffect(locationError) {
        if (locationError != null) {
            snackbarHostState.showSnackbar(
                message = locationError!!,
                duration = SnackbarDuration.Short
            )
            locationError = null
        }
    }
    
    // GPS click handler with permission check
    val onGPSClick: () -> Unit = {
        if (locationPermissionState.status.isGranted) {
            if (!locationManager.isLocationEnabled()) {
                locationError = "GPS tidak aktif. Silakan aktifkan lokasi di pengaturan."
            } else {
                isLocating = true
                coroutineScope.launch {
                    try {
                        // Try last known location first (fast)
                        val lastLocation = locationManager.getLastLocation()
                        if (lastLocation != null) {
                            viewModel.searchByCoord(lastLocation.latitude, lastLocation.longitude)
                            isLocating = false
                        } else {
                            // Fallback to current location request (slower but more accurate)
                            val currentLocation = locationManager.getCurrentLocation().firstOrNull()
                            if (currentLocation != null) {
                                viewModel.searchByCoord(currentLocation.latitude, currentLocation.longitude)
                            } else {
                                locationError = "Tidak dapat memperoleh lokasi. Coba lagi nanti."
                            }
                            isLocating = false
                        }
                    } catch (e: Exception) {
                        locationError = "Error: ${e.message}"
                        isLocating = false
                    }
                }
            }
        } else {
            // Request permission
            locationPermissionState.launchPermissionRequest()
        }
    }
    
    FloatingActionButton(
        onClick = onGPSClick,
        modifier = modifier.size(56.dp),
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    ) {
        if (isLocating) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                strokeWidth = 2.dp
            )
        } else {
            Icon(
                imageVector = Icons.Default.MyLocation,
                contentDescription = "Lokasi Saya",
                modifier = Modifier.size(28.dp)
            )
        }
    }
    
    // Snackbar host for error messages
    SnackbarHost(
        hostState = snackbarHostState,
        modifier = Modifier.size(0.dp) // Hidden, but still functional
    )
}
