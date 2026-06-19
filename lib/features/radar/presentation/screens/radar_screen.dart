import 'package:flutter/material.dart';
import 'package:flutter_map/flutter_map.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:latlong2/latlong.dart';

import '../../../../core/theme/app_spacing.dart';
import '../../../home/presentation/providers/home_view_model.dart';
import '../../../map/presentation/providers/radar_view_model.dart';
import '../widgets/radar_legend.dart';
import '../widgets/radar_timeline.dart';
import '../widgets/radar_zoom_controls.dart';

/// Peta Radar Hujan screen — `/radar` route.
///
/// Per Stitch `peta_radar_hujan_fixed_layout`:
///   - Full-screen dark map (CartoDB Dark Matter tiles)
///   - RainViewer radar overlay (past frames, current selectable)
///   - Top-left legend: "Intensitas Curah Hujan" gradient bar
///   - Top-right zoom controls (+ / -)
///   - Bottom-center timeline: play / slider / time labels / 1x speed
///   - User location marker (from HomeViewModel.current city's lat/lon)
class RadarScreen extends ConsumerStatefulWidget {
  const RadarScreen({super.key});

  @override
  ConsumerState<RadarScreen> createState() => _RadarScreenState();
}

class _RadarScreenState extends ConsumerState<RadarScreen> {
  final MapController _mapController = MapController();

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      final state = ref.read(radarViewModelProvider);
      if (state.status == RadarStatus.idle) {
        ref.read(radarViewModelProvider.notifier).loadRadarData();
      }
    });
  }

  @override
  void dispose() {
    _mapController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final radarState = ref.watch(radarViewModelProvider);
    final homeState = ref.watch(homeViewModelProvider);

    // Center the map on the user's current city if known, else Jakarta.
    final center = homeState.weather != null
        ? LatLng(homeState.weather!.lat, homeState.weather!.lon)
        : const LatLng(-6.2088, 106.8456);

    final tileUrl = radarState.status == RadarStatus.success
        ? ref.read(radarViewModelProvider.notifier).getCurrentTileUrl()
        : null;

    return Scaffold(
      appBar: AppBar(
        title: const Text('Peta Radar'),
        toolbarHeight: 72,
        actions: [
          IconButton(
            tooltip: 'Lokasi saya',
            icon: const Icon(Icons.my_location),
            onPressed: () => _mapController.move(center, 8),
          ),
          IconButton(
            tooltip: 'Refresh',
            icon: const Icon(Icons.refresh),
            onPressed: () => ref
                .read(radarViewModelProvider.notifier)
                .loadRadarData(),
          ),
          const SizedBox(width: AppSpacing.sm),
        ],
      ),
      body: _buildBody(
        context,
        radarState: radarState,
        center: center,
        tileUrl: tileUrl,
      ),
    );
  }

  Widget _buildBody(
    BuildContext context, {
    required RadarState radarState,
    required LatLng center,
    required String? tileUrl,
  }) {
    if (radarState.status == RadarStatus.error) {
      return _ErrorState(
        message: radarState.errorMessage ?? 'Gagal memuat data radar',
        onRetry: () =>
            ref.read(radarViewModelProvider.notifier).loadRadarData(),
      );
    }

    return Stack(
      children: [
        // Map fills the entire body.
        FlutterMap(
          mapController: _mapController,
          options: MapOptions(
            initialCenter: center,
            initialZoom: 7,
            minZoom: 3,
            maxZoom: 18,
            backgroundColor: const Color(0xFF0B1117), // matches surface dark
          ),
          children: [
            // Dark base map (CartoDB Dark Matter — matches Stitch look).
            TileLayer(
              urlTemplate:
                  'https://cartodb-basemaps-{s}.global.ssl.fastly.net/dark_all/{z}/{x}/{y}.png',
              subdomains: const ['a', 'b', 'c', 'd'],
              userAgentPackageName: 'com.weatherwise.flutter',
            ),
            // RainViewer radar overlay (only when frame URL is available).
            if (tileUrl != null)
              TileLayer(
                urlTemplate: tileUrl,
                userAgentPackageName: 'com.weatherwise.flutter',
                tileBuilder: (context, child, tile) => Opacity(
                  opacity: 0.7,
                  child: child,
                ),
              ),
            // User location marker.
            MarkerLayer(
              markers: [
                Marker(
                  point: center,
                  width: 36,
                  height: 36,
                  child: Icon(
                    Icons.my_location,
                    color: Theme.of(context).colorScheme.primary,
                    size: 32,
                  ),
                ),
              ],
            ),
          ],
        ),

        // Top-left: legend overlay.
        Positioned(
          top: AppSpacing.md,
          left: AppSpacing.md,
          child: SafeArea(
            child: RadarLegend(radarData: radarState.radarData),
          ),
        ),

        // Top-right: zoom controls overlay.
        Positioned(
          top: AppSpacing.md,
          right: AppSpacing.md,
          child: SafeArea(
            child: RadarZoomControls(mapController: _mapController),
          ),
        ),

        // Bottom-center: timeline slider overlay.
        if (radarState.radarData != null)
          Positioned(
            left: AppSpacing.lg,
            right: AppSpacing.lg,
            bottom: AppSpacing.lg,
            child: SafeArea(
              child: RadarTimeline(
                frames: radarState.radarData!.radar.pastFrames,
              ),
            ),
          ),

        // Loading indicator overlay (subtle, top center).
        if (radarState.status == RadarStatus.loading)
          const Positioned(
            top: AppSpacing.lg,
            left: 0,
            right: 0,
            child: Center(child: CircularProgressIndicator()),
          ),
      ],
    );
  }
}

class _ErrorState extends StatelessWidget {
  const _ErrorState({required this.message, required this.onRetry});
  final String message;
  final VoidCallback onRetry;

  @override
  Widget build(BuildContext context) {
    final scheme = Theme.of(context).colorScheme;
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(AppSpacing.xl),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(Icons.cloud_off, size: 64, color: scheme.onSurfaceVariant),
            const SizedBox(height: AppSpacing.md),
            Text(
              message,
              textAlign: TextAlign.center,
              style: Theme.of(context).textTheme.bodyMedium,
            ),
            const SizedBox(height: AppSpacing.lg),
            FilledButton.icon(
              onPressed: onRetry,
              icon: const Icon(Icons.refresh),
              label: const Text('Coba Lagi'),
            ),
          ],
        ),
      ),
    );
  }
}