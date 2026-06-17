import 'package:flutter/material.dart';
import 'package:flutter_map/flutter_map.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:latlong2/latlong.dart';
import '../providers/radar_view_model.dart';
import '../widgets/radar_slider.dart';

class MapScreen extends ConsumerStatefulWidget {
  final double lat;
  final double lon;

  const MapScreen({
    super.key,
    required this.lat,
    required this.lon,
  });

  @override
  ConsumerState<MapScreen> createState() => _MapScreenState();
}

class _MapScreenState extends ConsumerState<MapScreen> {
  late final MapController _mapController;

  @override
  void initState() {
    super.initState();
    _mapController = MapController();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      ref.read(radarViewModelProvider.notifier).loadRadarData();
    });
  }

  @override
  Widget build(BuildContext context) {
    final radarState = ref.watch(radarViewModelProvider);
    final tileUrl = ref.read(radarViewModelProvider.notifier).getCurrentTileUrl();

    return Scaffold(
      appBar: AppBar(
        title: const Text('Peta Radar'),
      ),
      body: Column(
        children: [
          Expanded(
            child: FlutterMap(
              mapController: _mapController,
              options: MapOptions(
                initialCenter: LatLng(widget.lat, widget.lon),
                initialZoom: 7.0,
                minZoom: 3,
                maxZoom: 18,
              ),
              children: [
                // Base map (OpenStreetMap)
                TileLayer(
                  urlTemplate: 'https://tile.openstreetmap.org/{z}/{x}/{y}.png',
                  userAgentPackageName: 'com.weatherwise.flutter',
                ),
                // Radar overlay
                if (tileUrl != null)
                  TileLayer(
                    urlTemplate: tileUrl,
                    tileBuilder: (context, child, tile) => Opacity(
                      opacity: 0.6,
                      child: child,
                    ),
                  ),
                // User location marker
                MarkerLayer(
                  markers: [
                    Marker(
                      point: LatLng(widget.lat, widget.lon),
                      width: 40,
                      height: 40,
                      child: const Icon(
                        Icons.my_location,
                        color: Colors.blue,
                        size: 32,
                      ),
                    ),
                  ],
                ),
              ],
            ),
          ),
          // Radar timeline slider
          if (radarState.status == RadarStatus.success)
            RadarSlider(
              frameCount: radarState.radarData?.radar.pastFrames.length ?? 0,
              currentIndex: radarState.currentFrameIndex,
              onChanged: (index) => ref
                  .read(radarViewModelProvider.notifier)
                  .setFrameIndex(index),
            ),
        ],
      ),
    );
  }
}
