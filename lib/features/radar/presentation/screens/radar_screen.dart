import 'package:flutter/material.dart';
import 'package:flutter_map/flutter_map.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:latlong2/latlong.dart';

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
  bool _gridLoaded = false;

  @override
  void dispose() {
    _mapController.dispose();
    super.dispose();
  }

  /// Trigger grid loading when weather data becomes available.
  /// Called from build() because didChangeDependencies fires before
  /// the cached weather data is loaded from Hive.
  void _loadGridIfNeeded(LatLng center) {
    if (_gridLoaded) return;
    final radarState = ref.read(radarViewModelProvider);

    if (radarState.status == RadarStatus.idle) {
      _gridLoaded = true;
      WidgetsBinding.instance.addPostFrameCallback((_) {
        ref.read(radarViewModelProvider.notifier).loadGrid(
              center.latitude,
              center.longitude,
            );
      });
    }
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

    // Build heatmap polygons for current frame.
    final polygons = <Polygon>[];
    if (radarState.grid != null) {
      final grid = radarState.grid!;
      final frameIdx = radarState.currentFrameIndex;
      // Slight overlap (0.55 × spacing) to avoid gaps between cells.
      final half = grid.spacing * 0.55;

      for (final cell in grid.cells) {
        final value =
            frameIdx < cell.values.length ? cell.values[frameIdx] : 0.0;
        final color = precipitationColor(value);
        if (color.alpha == 0) continue; // skip cells with no rain

        polygons.add(Polygon(
          points: [
            LatLng(cell.latitude - half, cell.longitude - half),
            LatLng(cell.latitude - half, cell.longitude + half),
            LatLng(cell.latitude + half, cell.longitude + half),
            LatLng(cell.latitude + half, cell.longitude - half),
          ],
          color: color,
          borderColor: Colors.transparent,
          borderStrokeWidth: 0,
        ));
      }
    }

    return Stack(
      children: [
        // --- Map ---
        FlutterMap(
          mapController: _mapController,
          options: MapOptions(
            initialCenter: center,
            initialZoom: 6,
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
                      'Gagal memuat data curah hujan',
                      style: TextStyle(
                        fontFamily: 'Inter',
                        color: scheme.onSurface,
                        fontSize: 16,
                      ),
                    ),
                    const SizedBox(height: AppSpacing.sm),
                    ElevatedButton(
                      onPressed: () {
                        _gridLoaded = false;
                        _loadGridIfNeeded(center);
                      },
                      child: const Text('Coba Lagi'),
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
                  tooltip: 'Lokasi saya',
                  onPressed: () => _mapController.move(center, 8),
                  child: const Icon(Icons.my_location),
                ),
                const SizedBox(height: AppSpacing.sm),
                FloatingActionButton.small(
                  heroTag: 'refresh',
                  tooltip: 'Muat ulang data',
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
