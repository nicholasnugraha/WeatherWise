import 'package:flutter/material.dart';
import 'package:flutter_map/flutter_map.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:latlong2/latlong.dart';

import '../../../../core/localization/app_localizations.dart';
import '../../../../core/theme/app_spacing.dart';
import '../../../home/presentation/providers/home_view_model.dart';
import '../../../map/presentation/providers/radar_view_model.dart';
import '../../../shared/data/datasources/open_meteo_precipitation_service.dart';
import '../widgets/radar_legend.dart';
import '../widgets/radar_timeline.dart';
import '../widgets/radar_zoom_controls.dart';

/// Peta Radar Hujan screen — `/radar` route.
///
/// Uses Open-Meteo precipitation forecast data rendered as a heatmap
/// overlay on a dark base map. A 7×7 grid of forecast points is queried
/// around the user's location, and each cell is colored by precipitation
/// intensity (mm/h).
///
/// Data source: https://api.open-meteo.com/v1/forecast (no API key needed)
class RadarScreen extends ConsumerStatefulWidget {
  const RadarScreen({super.key});

  @override
  ConsumerState<RadarScreen> createState() => _RadarScreenState();
}

class _RadarScreenState extends ConsumerState<RadarScreen> {
  final MapController _mapController = MapController();
  LatLng? _lastLoadedCenter;

  @override
  void dispose() {
    _mapController.dispose();
    super.dispose();
  }

  /// Trigger grid loading when weather data becomes available or city changes.
  ///
  /// Previously used a `_gridLoaded` boolean flag that prevented the grid
  /// from reloading when the user searched a new city (e.g. via Dashboard
  /// search). The grid was stuck at the default location because:
  ///   1. `_gridLoaded` was never reset, so `_loadGridIfNeeded` returned early.
  ///   2. The `RadarStatus.idle` check also blocked reloads — the ViewModel's
  ///      status stays at `success` after the first load, never going back
  ///      to `idle`.
  ///
  /// Fix: track the last loaded center. Reload whenever the center changes
  /// (new city) OR when no grid has been loaded yet.
  void _loadGridIfNeeded(LatLng center) {
    // Skip if we already loaded for this exact center (within ~1m tolerance).
    if (_lastLoadedCenter != null &&
        (_lastLoadedCenter!.latitude - center.latitude).abs() < 0.0001 &&
        (_lastLoadedCenter!.longitude - center.longitude).abs() < 0.0001) {
      return;
    }
    _lastLoadedCenter = center;
    WidgetsBinding.instance.addPostFrameCallback((_) {
      ref.read(radarViewModelProvider.notifier).loadGrid(
            center.latitude,
            center.longitude,
          );
    });
  }

  @override
  Widget build(BuildContext context) {
    final homeState = ref.watch(homeViewModelProvider);
    final radarState = ref.watch(radarViewModelProvider);

    final center = homeState.weather != null
        ? LatLng(homeState.weather!.lat, homeState.weather!.lon)
        : const LatLng(-6.2088, 106.8456);

    // Trigger grid loading (post-frame to avoid side-effects during build).
    _loadGridIfNeeded(center);

    return _buildBody(context, center: center, radarState: radarState);
  }

  Widget _buildBody(
    BuildContext context, {
    required LatLng center,
    required RadarState radarState,
  }) {
    final scheme = Theme.of(context).colorScheme;
    final l10n = AppLocalizations.of(context);

    // Build heatmap polygons for current frame.
    final polygons = <Polygon>[];
    if (radarState.grid != null) {
      polygons.addAll(buildHeatmapPolygons(
        grid: radarState.grid!,
        frameIndex: radarState.currentFrameIndex,
      ));
    }

    return Stack(
      children: [
        // --- Map ---
        FlutterMap(
          mapController: _mapController,
          options: MapOptions(
            initialCenter: center,
            initialZoom: 9,
            minZoom: 3,
            maxZoom: 18,
            backgroundColor: const Color(0xFF0B1117),
          ),
          children: [
            // Dark base map (CartoDB Dark Matter).
            TileLayer(
              urlTemplate:
                  'https://cartodb-basemaps-{s}.global.ssl.fastly.net/dark_all/{z}/{x}/{y}.png',
              subdomains: const ['a', 'b', 'c', 'd'],
              userAgentPackageName: 'com.weatherwise.flutter',
            ),
            // Precipitation heatmap overlay.
            if (polygons.isNotEmpty)
              PolygonLayer(polygons: polygons),
            // User location marker.
            MarkerLayer(
              markers: [
                Marker(
                  point: center,
                  width: 36,
                  height: 36,
                  child: Icon(
                    Icons.my_location,
                    color: scheme.primary,
                    size: 32,
                  ),
                ),
              ],
            ),
            // Attribution.
            RichAttributionWidget(
              attributions: [
                TextSourceAttribution('Open-Meteo'),
                TextSourceAttribution('© OpenStreetMap contributors'),
              ],
            ),
          ],
        ),

        // --- Loading overlay ---
        if (radarState.status == RadarStatus.loading)
          Positioned.fill(
            child: Container(
              color: Colors.black54,
              child: Center(
                child: CircularProgressIndicator(color: scheme.primary),
              ),
            ),
          ),

        // --- Error overlay ---
        if (radarState.status == RadarStatus.error)
          Positioned.fill(
            child: Container(
              color: Colors.black54,
              child: Center(
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    Icon(Icons.error_outline, color: scheme.error, size: 48),
                    const SizedBox(height: AppSpacing.md),
                    Text(
                      l10n.errorGeneric,
                      style: TextStyle(
                        fontFamily: 'Inter',
                        color: scheme.onSurface,
                        fontSize: 16,
                      ),
                    ),
                    const SizedBox(height: AppSpacing.sm),
                    ElevatedButton(
                      onPressed: () {
                        _lastLoadedCenter = null;
                        _loadGridIfNeeded(center);
                      },
                      child: Text(l10n.retry),
                    ),
                  ],
                ),
              ),
            ),
          ),

        // --- Legend (top-left) ---
        Positioned(
          top: AppSpacing.md,
          left: AppSpacing.md,
          child: SafeArea(child: const RadarLegend()),
        ),

        // --- Controls (top-right) ---
        Positioned(
          top: AppSpacing.md,
          right: AppSpacing.md,
          child: SafeArea(
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                FloatingActionButton.small(
                  heroTag: 'location',
                  tooltip: l10n.radarMyLocation,
                  onPressed: () => _mapController.move(center, 10),
                  child: const Icon(Icons.my_location),
                ),
                const SizedBox(height: AppSpacing.sm),
                FloatingActionButton.small(
                  heroTag: 'refresh',
                  tooltip: l10n.radarReload,
                  onPressed: () => ref
                      .read(radarViewModelProvider.notifier)
                      .loadGrid(center.latitude, center.longitude),
                  child: const Icon(Icons.refresh),
                ),
                const SizedBox(height: AppSpacing.sm),
                RadarZoomControls(mapController: _mapController),
              ],
            ),
          ),
        ),

        // --- Timeline (bottom) ---
        if (radarState.grid != null &&
            radarState.status == RadarStatus.success)
          Positioned(
            left: AppSpacing.lg,
            right: AppSpacing.lg,
            bottom: AppSpacing.lg,
            child: SafeArea(
              child: RadarTimeline(
                timestamps: radarState.grid!.timestamps,
                currentIndex: radarState.currentFrameIndex,
                isPlaying: radarState.isPlaying,
              ),
            ),
          ),
      ],
    );
  }
}
