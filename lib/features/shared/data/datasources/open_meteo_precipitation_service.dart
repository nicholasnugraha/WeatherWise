import 'package:dio/dio.dart';
import 'package:flutter/painting.dart';
import 'package:flutter_map/flutter_map.dart';
import 'package:latlong2/latlong.dart';

/// A single cell in the precipitation grid.
class PrecipitationCell {
  final double latitude;
  final double longitude;

  /// Precipitation values per timestamp (mm/hour).
  final List<double> values;

  const PrecipitationCell({
    required this.latitude,
    required this.longitude,
    required this.values,
  });
}

/// Grid of precipitation forecast data from Open-Meteo.
class PrecipitationGrid {
  final List<DateTime> timestamps;
  final List<PrecipitationCell> cells;
  final int gridSize;
  final double centerLat;
  final double centerLon;
  final double spacing;

  const PrecipitationGrid({
    required this.timestamps,
    required this.cells,
    required this.gridSize,
    required this.centerLat,
    required this.centerLon,
    required this.spacing,
  });

  /// Geographic bounds of the grid.
  LatLngBounds get bounds {
    final half = spacing * (gridSize - 1) / 2;
    return LatLngBounds(
      LatLng(centerLat - half, centerLon - half),
      LatLng(centerLat + half, centerLon + half),
    );
  }

  /// Find the frame index closest to [now] (first frame whose
  /// timestamp is not before [now]). Returns 0 if all frames are
  /// in the past.
  static int findCurrentFrameIndex(
      List<DateTime> timestamps, DateTime now) {
    for (int i = 0; i < timestamps.length; i++) {
      if (!timestamps[i].isBefore(now)) {
        return i;
      }
    }
    return 0;
  }
}

/// Build heatmap polygons for a single frame of the precipitation grid.
///
/// Returns one [Polygon] per cell whose precipitation at [frameIndex]
/// is ≥ 0.1 mm/h. Each polygon is a square centred on the cell's
/// coordinates, sized to slightly overlap neighbours (0.55 × spacing)
/// to avoid visible gaps. All polygons are filled (`isFilled: true`)
/// with the colour from [precipitationColor].
///
/// Extracted from the widget so it can be unit-tested without a
/// live Flutter binding or map camera.
List<Polygon> buildHeatmapPolygons({
  required PrecipitationGrid grid,
  required int frameIndex,
}) {
  final polygons = <Polygon>[];
  final half = grid.spacing * 0.55;

  for (final cell in grid.cells) {
    final value =
        frameIndex < cell.values.length ? cell.values[frameIndex] : 0.0;
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
      // flutter_map 6.x: color alone does NOT fill a polygon.
      // isFilled must be true for PolygonPainter to draw the fill.
      isFilled: true,
      borderColor: const Color(0x00000000), // transparent
      borderStrokeWidth: 0,
    ));
  }

  return polygons;
}

/// Maps precipitation intensity (mm/h) to a heatmap color.
///
/// Scale follows standard precipitation categories:
/// 0–0.1  → transparent (no rain)
/// 0.1–0.5 → light blue (trace)
/// 0.5–2  → blue (light)
/// 2–5    → green (moderate)
/// 5–10   → yellow-orange (heavy)
/// 10–20  → deep orange (very heavy)
/// 20+    → red (extreme)
Color precipitationColor(double mmh) {
  if (mmh < 0.1) return const Color(0x004FC3F7);
  if (mmh < 0.5) return const Color(0x334FC3F7);
  if (mmh < 2.0) return const Color(0x664FC3F7);
  if (mmh < 5.0) return const Color(0x9966BB6A);
  if (mmh < 10.0) return const Color(0xCCFFA726);
  if (mmh < 20.0) return const Color(0xE6FF7043);
  return const Color(0xFFE53935);
}

/// Service that queries Open-Meteo API for precipitation grid data.
///
/// Uses batch coordinate queries (up to 10 per API call) to build a grid
/// of precipitation forecast data around a center point.
///
/// API: https://api.open-meteo.com/v1/forecast
/// No API key required for non-commercial use.
class OpenMeteoPrecipitationService {
  final Dio _dio;

  OpenMeteoPrecipitationService(this._dio);

  /// Generate grid coordinates around a center point.
  ///
  /// Returns (lats, lons, spacing) where lats and lons are parallel
  /// arrays in row-major order (row 0 = south, row N-1 = north).
  ///
  /// Default: 9×9 grid over 2° (~222 km) → ~28 km per cell.
  /// This focuses on the city + immediate surroundings for better
  /// local forecast accuracy than a wide regional grid.
  static (List<double>, List<double>, double) generateGridCoords({
    required double centerLat,
    required double centerLon,
    int gridSize = 9,
    double coverageDeg = 2.0,
  }) {
    final spacing = coverageDeg / (gridSize - 1);
    final halfExtent = coverageDeg / 2;
    final lats = <double>[];
    final lons = <double>[];
    for (int row = 0; row < gridSize; row++) {
      for (int col = 0; col < gridSize; col++) {
        lats.add(centerLat - halfExtent + row * spacing);
        lons.add(centerLon - halfExtent + col * spacing);
      }
    }
    return (lats, lons, spacing);
  }

  /// Fetch precipitation grid around [centerLat, centerLon].
  ///
  /// [gridSize] points per side (gridSize² total points).
  /// [coverageDeg] total degrees covered in each direction.
  /// [forecastDays] number of days of forecast (1=24h, 2=48h).
  ///
  /// Default: 9×9 grid over 2° (~222 km²) → ~28 km/cell.
  /// Focused on city area for better local accuracy.
  Future<PrecipitationGrid> fetchGrid({
    required double centerLat,
    required double centerLon,
    int gridSize = 9,
    double coverageDeg = 2.0,
    int forecastDays = 1,
  }) async {
    final (lats, lons, spacing) = generateGridCoords(
      centerLat: centerLat,
      centerLon: centerLon,
      gridSize: gridSize,
      coverageDeg: coverageDeg,
    );

    // Batch query: up to 10 coordinate pairs per API call.
    final allPrecipArrays = <List<double>>[];
    var timestamps = <DateTime>[];

    for (int i = 0; i < lats.length; i += 10) {
      final end = (i + 10).clamp(0, lats.length);
      final batchLats = lats.sublist(i, end);
      final batchLons = lons.sublist(i, end);

      final response = await _dio.get(
        'https://api.open-meteo.com/v1/forecast',
        queryParameters: {
          'latitude': batchLats.map((l) => l.toStringAsFixed(4)).join(','),
          'longitude': batchLons.map((l) => l.toStringAsFixed(4)).join(','),
          'hourly': 'precipitation',
          'forecast_days': forecastDays,
          'timezone': 'auto',
        },
      );

      // Multi-coordinate response is a JSON array.
      final List<dynamic> items =
          response.data is List ? response.data : [response.data];

      for (final item in items) {
        final hourly = item['hourly'] as Map<String, dynamic>;
        final times = (hourly['time'] as List).cast<String>();
        final precip = (hourly['precipitation'] as List)
            .map((e) => (e as num?)?.toDouble() ?? 0.0)
            .toList();

        // Parse timestamps once (from first batch, first coordinate).
        if (timestamps.isEmpty) {
          timestamps = times.map((t) => DateTime.parse(t)).toList();
        }

        allPrecipArrays.add(precip);
      }
    }

    // Build cells.
    final cells = <PrecipitationCell>[];
    for (int i = 0; i < lats.length; i++) {
      cells.add(PrecipitationCell(
        latitude: lats[i],
        longitude: lons[i],
        values: allPrecipArrays[i],
      ));
    }

    return PrecipitationGrid(
      timestamps: timestamps,
      cells: cells,
      gridSize: gridSize,
      centerLat: centerLat,
      centerLon: centerLon,
      spacing: spacing,
    );
  }
}
