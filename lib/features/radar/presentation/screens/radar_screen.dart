import 'package:flutter/material.dart';
import 'package:flutter_map/flutter_map.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:latlong2/latlong.dart';

import '../../../../core/theme/app_spacing.dart';
import '../../../home/presentation/providers/home_view_model.dart';
import '../../../shared/data/datasources/bmkg_radar_service.dart';
import '../widgets/radar_legend.dart';
import '../widgets/radar_zoom_controls.dart';

/// Peta Radar Hujan screen — `/radar` route.
///
/// Uses BMKG radar data (CMAX composite) overlaid on a dark base map.
/// Data source: https://cuaca.bmkg.go.id/data/public/sidarma/ANIMASI/
///
/// Attribution: BMKG (Badan Meteorologi, Klimatologi, dan Geofisika)
class RadarScreen extends ConsumerStatefulWidget {
  const RadarScreen({super.key});

  @override
  ConsumerState<RadarScreen> createState() => _RadarScreenState();
}

class _RadarScreenState extends ConsumerState<RadarScreen> {
  final MapController _mapController = MapController();

  @override
  void dispose() {
    _mapController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final homeState = ref.watch(homeViewModelProvider);

    // Center the map on the user's current city if known, else Jakarta.
    final center = homeState.weather != null
        ? LatLng(homeState.weather!.lat, homeState.weather!.lon)
        : const LatLng(-6.2088, 106.8456);

    // Find nearest BMKG radar station for the current location.
    final nearestStation = BmkgRadarStations.nearestTo(
      center.latitude,
      center.longitude,
    );

    return _buildBody(
      context,
      center: center,
      nearestStation: nearestStation,
    );
  }

  Widget _buildBody(
    BuildContext context, {
    required LatLng center,
    BmkgRadarStation? nearestStation,
  }) {
    final scheme = Theme.of(context).colorScheme;

    return Stack(
      children: [
        // Map fills the entire body.
        FlutterMap(
          mapController: _mapController,
          options: MapOptions(
            initialCenter: center,
            initialZoom: 5,
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
            // BMKG national composite radar overlay.
            // Animated GIF showing radar reflectivity (dBZ) across Indonesia.
            // _cacheBustUrl forces re-fetch when user presses refresh.
            OverlayImageLayer(
              overlayImages: [
                OverlayImage(
                  imageProvider: NetworkImage(
                    _cacheBustUrl ?? BmkgRadarComposite.url,
                  ),
                  bounds: BmkgRadarComposite.bounds,
                  opacity: 0.75,
                  gaplessPlayback: true,
                ),
              ],
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
                    color: scheme.primary,
                    size: 32,
                  ),
                ),
              ],
            ),
            // Attribution (required by BMKG terms of use).
            RichAttributionWidget(
              attributions: [
                TextSourceAttribution('BMKG'),
                TextSourceAttribution('© OpenStreetMap contributors'),
              ],
            ),
          ],
        ),

        // Top-left: legend overlay.
        Positioned(
          top: AppSpacing.md,
          left: AppSpacing.md,
          child: SafeArea(
            child: RadarLegend(radarData: null),
          ),
        ),

        // Top-right: location + refresh + zoom controls overlay.
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
                // Refresh reloads the radar image (URL is same but content updates).
                FloatingActionButton.small(
                  heroTag: 'refresh',
                  tooltip: 'Refresh radar',
                  onPressed: () {
                    // Force image re-fetch by changing the ImageProvider.
                    // Flutter caches network images; to bust cache we use a
                    // timestamp query parameter.
                    final cacheBustUrl =
                        '${BmkgRadarComposite.url}?t=${DateTime.now().millisecondsSinceEpoch}';
                    setState(() {
                      _cacheBustUrl = cacheBustUrl;
                    });
                  },
                  child: const Icon(Icons.refresh),
                ),
                const SizedBox(height: AppSpacing.sm),
                RadarZoomControls(mapController: _mapController),
              ],
            ),
          ),
        ),

        // Bottom-center: station info bar.
        if (nearestStation != null)
          Positioned(
            left: AppSpacing.lg,
            right: AppSpacing.lg,
            bottom: AppSpacing.lg,
            child: SafeArea(
              child: Container(
                padding: const EdgeInsets.symmetric(
                  horizontal: AppSpacing.md,
                  vertical: AppSpacing.sm,
                ),
                decoration: BoxDecoration(
                  color: scheme.surfaceContainerLowest,
                  borderRadius: BorderRadius.circular(AppSpacing.radiusFull),
                  boxShadow: [
                    BoxShadow(
                      color: Colors.black.withValues(alpha: 0.15),
                      blurRadius: 12,
                      offset: const Offset(0, 4),
                    ),
                  ],
                ),
                child: Row(
                  children: [
                    Icon(Icons.radar, color: scheme.primary, size: 20),
                    const SizedBox(width: AppSpacing.sm),
                    Expanded(
                      child: Text(
                        'Stasiun Radar: ${nearestStation.name}',
                        style: TextStyle(
                          fontFamily: 'Inter',
                          fontSize: 14,
                          fontWeight: FontWeight.w500,
                          color: scheme.onSurface,
                        ),
                      ),
                    ),
                    Text(
                      'Sumber: BMKG',
                      style: TextStyle(
                        fontFamily: 'Inter',
                        fontSize: 12,
                        fontWeight: FontWeight.w400,
                        color: scheme.onSurfaceVariant,
                      ),
                    ),
                  ],
                ),
              ),
            ),
          ),
      ],
    );
  }

  /// When non-null, use this cache-busting URL instead of the base URL.
  String? _cacheBustUrl;
}