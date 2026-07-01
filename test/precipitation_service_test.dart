import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import 'package:flutter_map/flutter_map.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:latlong2/latlong.dart';
import 'package:mocktail/mocktail.dart';
import 'package:weatherwise_flutter/features/shared/data/datasources/open_meteo_precipitation_service.dart';

class MockDio extends Mock implements Dio {}

void main() {
  group('precipitationColor', () {
    test('returns transparent (alpha=0) for 0 mm/h', () {
      final color = precipitationColor(0.0);
      expect(color.alpha, equals(0));
    });

    test('returns transparent for values below 0.1 mm/h', () {
      final color = precipitationColor(0.05);
      expect(color.alpha, equals(0));
    });

    test('returns non-transparent for 0.1 mm/h (trace)', () {
      final color = precipitationColor(0.1);
      expect(color.alpha, greaterThan(0));
      expect(color.alpha, equals(0x33)); // 20% opacity
    });

    test('returns 40% opacity for 0.5–2 mm/h (light rain)', () {
      final color = precipitationColor(1.0);
      expect(color.alpha, equals(0x66)); // 40% opacity
    });

    test('returns 60% opacity for 2–5 mm/h (moderate)', () {
      final color = precipitationColor(3.0);
      expect(color.alpha, equals(0x99)); // 60% opacity
    });

    test('returns 80% opacity for 5–10 mm/h (heavy)', () {
      final color = precipitationColor(7.0);
      expect(color.alpha, equals(0xCC)); // 80% opacity
    });

    test('returns 90% opacity for 10–20 mm/h (very heavy)', () {
      final color = precipitationColor(15.0);
      expect(color.alpha, equals(0xE6)); // 90% opacity
    });

    test('returns 100% opacity for 20+ mm/h (extreme)', () {
      final color = precipitationColor(25.0);
      expect(color.alpha, equals(0xFF)); // 100% opacity
    });

    test('alpha increases monotonically with precipitation', () {
      final vals = [0.0, 0.2, 1.0, 3.0, 7.0, 15.0, 25.0];
      final alphas = vals.map((v) => precipitationColor(v).alpha).toList();
      for (int i = 1; i < alphas.length; i++) {
        expect(alphas[i], greaterThanOrEqualTo(alphas[i - 1]),
            reason: 'alpha should not decrease as precipitation increases');
      }
    });
  });

  group('generateGridCoords', () {
    test('generates gridSize² coordinates', () {
      final (lats, lons, _) = OpenMeteoPrecipitationService.generateGridCoords(
        centerLat: -6.2,
        centerLon: 106.8,
        gridSize: 7,
        coverageDeg: 8.0,
      );
      expect(lats.length, equals(49)); // 7×7
      expect(lons.length, equals(49));
    });

    test('center coordinate is at grid center index', () {
      final (lats, lons, _) = OpenMeteoPrecipitationService.generateGridCoords(
        centerLat: -6.2,
        centerLon: 106.8,
        gridSize: 7,
        coverageDeg: 8.0,
      );
      // Center index for 7×7 row-major grid = (3*7 + 3) = 24
      final centerIdx = 3 * 7 + 3;
      expect(lats[centerIdx], closeTo(-6.2, 0.001));
      expect(lons[centerIdx], closeTo(106.8, 0.001));
    });

    test('first coordinate is SW corner (center - halfExtent)', () {
      final (lats, lons, _) = OpenMeteoPrecipitationService.generateGridCoords(
        centerLat: -6.2,
        centerLon: 106.8,
        gridSize: 7,
        coverageDeg: 8.0,
      );
      // SW corner = center - 4° in both directions
      expect(lats[0], closeTo(-10.2, 0.01)); // -6.2 - 4.0
      expect(lons[0], closeTo(102.8, 0.01)); // 106.8 - 4.0
    });

    test('last coordinate is NE corner (center + halfExtent)', () {
      final (lats, lons, _) = OpenMeteoPrecipitationService.generateGridCoords(
        centerLat: -6.2,
        centerLon: 106.8,
        gridSize: 7,
        coverageDeg: 8.0,
      );
      final lastIdx = 48; // 7×7 - 1
      expect(lats[lastIdx], closeTo(-2.2, 0.01)); // -6.2 + 4.0
      expect(lons[lastIdx], closeTo(110.8, 0.01)); // 106.8 + 4.0
    });

    test('spacing is coverageDeg / (gridSize - 1)', () {
      final (_, _, spacing) = OpenMeteoPrecipitationService.generateGridCoords(
        centerLat: 0,
        centerLon: 0,
        gridSize: 7,
        coverageDeg: 8.0,
      );
      expect(spacing, closeTo(8.0 / 6.0, 0.0001)); // 1.3333
    });

    test('supports different grid sizes', () {
      for (final size in [3, 5, 9, 11]) {
        final (lats, lons, _) =
            OpenMeteoPrecipitationService.generateGridCoords(
          centerLat: 0,
          centerLon: 0,
          gridSize: size,
          coverageDeg: 10.0,
        );
        expect(lats.length, equals(size * size));
        expect(lons.length, equals(size * size));
      }
    });

    test('default parameters produce 9×9 grid with 2° coverage', () {
      // Verify the focused city grid: 81 points, ~28 km spacing
      final (lats, lons, spacing) =
          OpenMeteoPrecipitationService.generateGridCoords(
        centerLat: -6.2,
        centerLon: 106.8,
      );
      expect(lats.length, equals(81)); // 9×9
      expect(lons.length, equals(81));
      expect(spacing, closeTo(2.0 / 8.0, 0.0001)); // 0.25°
      // 0.25° × 111 km/° ≈ 28 km per cell
      expect(spacing * 111, lessThan(30)); // under 30 km
      expect(spacing * 111, greaterThan(27)); // over 27 km
    });
  });

  group('PrecipitationGrid.findCurrentFrameIndex', () {
    test('returns 0 if all frames are in the future', () {
      final now = DateTime(2026, 6, 30, 0, 0);
      final timestamps = [
        DateTime(2026, 6, 30, 0, 0),
        DateTime(2026, 6, 30, 1, 0),
        DateTime(2026, 6, 30, 2, 0),
      ];
      final idx = PrecipitationGrid.findCurrentFrameIndex(timestamps, now);
      expect(idx, equals(0));
    });

    test('returns index of first frame >= now', () {
      final now = DateTime(2026, 6, 30, 14, 30);
      final timestamps = [
        DateTime(2026, 6, 30, 12, 0),
        DateTime(2026, 6, 30, 13, 0),
        DateTime(2026, 6, 30, 14, 0),
        DateTime(2026, 6, 30, 15, 0), // first >= 14:30
        DateTime(2026, 6, 30, 16, 0),
      ];
      final idx = PrecipitationGrid.findCurrentFrameIndex(timestamps, now);
      expect(idx, equals(3));
    });

    test('returns 0 if all frames are in the past', () {
      final now = DateTime(2026, 7, 1, 0, 0);
      final timestamps = [
        DateTime(2026, 6, 30, 22, 0),
        DateTime(2026, 6, 30, 23, 0),
      ];
      final idx = PrecipitationGrid.findCurrentFrameIndex(timestamps, now);
      expect(idx, equals(0));
    });

    test('returns 0 for empty timestamps', () {
      final now = DateTime.now();
      final idx =
          PrecipitationGrid.findCurrentFrameIndex([], now);
      expect(idx, equals(0));
    });

    test('does NOT return 0 (midnight) when current time is afternoon', () {
      // This is the regression test for the original bug:
      // currentFrameIndex was hardcoded to 0, showing midnight data
      // (which has 0 precipitation) instead of current hour.
      final now = DateTime(2026, 6, 30, 15, 0);
      final timestamps = List.generate(
        24,
        (i) => DateTime(2026, 6, 30, i, 0),
      );
      final idx = PrecipitationGrid.findCurrentFrameIndex(timestamps, now);
      expect(idx, equals(15));
      expect(idx, isNot(equals(0))); // Regression: must not be 0
    });
  });

  group('OpenMeteoPrecipitationService.fetchGrid', () {
    late MockDio mockDio;

    setUp(() {
      mockDio = MockDio();
      registerFallbackValue(RequestOptions(path: ''));
    });

    /// Helper: generate a mock Open-Meteo API response object.
    Map<String, dynamic> mockApiResponse({
      required double lat,
      required double lon,
      required List<double> precip,
    }) {
      return {
        'latitude': lat,
        'longitude': lon,
        'hourly': {
          'time': List.generate(
              precip.length, (i) => '2026-06-30T${i.toString().padLeft(2, '0')}:00'),
          'precipitation': precip,
        },
      };
    }

    /// Helper: set up mock Dio to return dynamic responses based on
    /// the number of coordinates in the request.
    void setupMockDio({List<double>? precipPerCell}) {
      when(() => mockDio.get(any(),
              queryParameters: any(named: 'queryParameters')))
          .thenAnswer((invocation) async {
        final qp = invocation.namedArguments[#queryParameters] as Map;
        final latStr = qp['latitude'] as String;
        final coordCount = latStr.split(',').length;
        final precip = precipPerCell ?? [0.0, 0.5];
        final data = List.generate(
          coordCount,
          (i) => mockApiResponse(
            lat: i.toDouble(),
            lon: i.toDouble(),
            precip: precip,
          ),
        );
        return Response(
          data: data,
          statusCode: 200,
          requestOptions: RequestOptions(path: ''),
        );
      });
    }

    test('parses multi-coordinate (array) response correctly', () async {
      setupMockDio(precipPerCell: [0.0, 1.5]);

      final service = OpenMeteoPrecipitationService(mockDio);
      final grid = await service.fetchGrid(
        centerLat: -6.0,
        centerLon: 106.5,
        gridSize: 3,
        coverageDeg: 2.0,
      );

      expect(grid.cells.length, equals(9)); // 3×3
      // Each cell should have 2 hours of data
      for (final cell in grid.cells) {
        expect(cell.values.length, equals(2));
      }
    });

    test('handles null precipitation values gracefully', () async {
      when(() => mockDio.get(any(),
              queryParameters: any(named: 'queryParameters')))
          .thenAnswer((invocation) async {
        final qp = invocation.namedArguments[#queryParameters] as Map;
        final latStr = qp['latitude'] as String;
        final coordCount = latStr.split(',').length;
        final data = List.generate(
          coordCount,
          (i) => {
            'latitude': i.toDouble(),
            'longitude': i.toDouble(),
            'hourly': {
              'time': ['2026-06-30T00:00', '2026-06-30T01:00'],
              'precipitation': [null, 0.5], // null should become 0.0
            },
          },
        );
        return Response(
          data: data,
          statusCode: 200,
          requestOptions: RequestOptions(path: ''),
        );
      });

      final service = OpenMeteoPrecipitationService(mockDio);
      final grid = await service.fetchGrid(
        centerLat: -6.0,
        centerLon: 106.0,
        gridSize: 3,
        coverageDeg: 2.0,
      );

      // null should be converted to 0.0
      expect(grid.cells[0].values[0], equals(0.0));
      expect(grid.cells[0].values[1], equals(0.5));
    });

    test('makes correct number of batch calls for 7×7 grid', () async {
      setupMockDio(precipPerCell: [0.0]);

      final service = OpenMeteoPrecipitationService(mockDio);
      await service.fetchGrid(
        centerLat: 0,
        centerLon: 0,
        gridSize: 7,
        coverageDeg: 8.0,
      );

      // 49 points / 10 per batch = 5 calls
      verify(() => mockDio.get(any(),
              queryParameters: any(named: 'queryParameters')))
          .called(5);
    });

    test('default parameters make 9 batch calls for 9×9 grid', () async {
      setupMockDio(precipPerCell: [0.0]);

      final service = OpenMeteoPrecipitationService(mockDio);
      await service.fetchGrid(centerLat: 0, centerLon: 0);

      // 81 points / 10 per batch = 9 calls
      verify(() => mockDio.get(any(),
              queryParameters: any(named: 'queryParameters')))
          .called(9);
    });
  });

  group('PrecipitationGrid.bounds', () {
    test('returns correct geographic bounds', () {
      final grid = PrecipitationGrid(
        timestamps: [],
        cells: [],
        gridSize: 7,
        centerLat: -6.2,
        centerLon: 106.8,
        spacing: 1.3333,
      );
      final bounds = grid.bounds;
      // half = 1.3333 * 6 / 2 = 4.0
      expect(bounds.south, closeTo(-10.2, 0.01));
      expect(bounds.north, closeTo(-2.2, 0.01));
      expect(bounds.west, closeTo(102.8, 0.01));
      expect(bounds.east, closeTo(110.8, 0.01));
    });
  });

  group('buildHeatmapPolygons', () {
    /// Helper: build a small 2×2 grid with custom precipitation values.
    PrecipitationGrid makeGrid(List<List<double>> valuesPerCell,
        {double centerLat = -6.0, double centerLon = 106.0}) {
      final (lats, lons, spacing) =
          OpenMeteoPrecipitationService.generateGridCoords(
        centerLat: centerLat,
        centerLon: centerLon,
        gridSize: 2,
        coverageDeg: 2.0,
      );
      final cells = <PrecipitationCell>[];
      for (int i = 0; i < lats.length; i++) {
        cells.add(PrecipitationCell(
          latitude: lats[i],
          longitude: lons[i],
          values: valuesPerCell[i],
        ));
      }
      return PrecipitationGrid(
        timestamps: List.generate(
          valuesPerCell.first.length,
          (i) => DateTime(2026, 7, 1, i),
        ),
        cells: cells,
        gridSize: 2,
        centerLat: centerLat,
        centerLon: centerLon,
        spacing: spacing,
      );
    }

    test('returns empty list when all cells have 0 precipitation', () {
      final grid = makeGrid([
        [0.0, 0.0],
        [0.0, 0.0],
        [0.0, 0.0],
        [0.0, 0.0],
      ]);
      final polygons = buildHeatmapPolygons(grid: grid, frameIndex: 0);
      expect(polygons, isEmpty);
    });

    test('returns polygons only for cells with precipitation >= 0.1', () {
      final grid = makeGrid([
        [0.0, 5.0], // cell 0: no rain at frame 0, rain at frame 1
        [0.05, 0.05], // cell 1: below threshold (0.05 < 0.1)
        [3.0, 0.0], // cell 2: rain at frame 0
        [0.0, 0.0], // cell 3: no rain
      ]);
      // Frame 0: only cell 2 has rain (3.0 mm/h)
      final polygons0 = buildHeatmapPolygons(grid: grid, frameIndex: 0);
      expect(polygons0.length, equals(1));

      // Frame 1: only cell 0 has rain (5.0 mm/h)
      final polygons1 = buildHeatmapPolygons(grid: grid, frameIndex: 1);
      expect(polygons1.length, equals(1));
    });

    test('all polygons have isFilled = true (flutter_map 6.x fix)', () {
      // This is the regression test for the root cause bug:
      // Polygon.color was set but isFilled defaulted to false,
      // so PolygonPainter never drew the fill → invisible heatmap.
      final grid = makeGrid([
        [5.0, 10.0],
        [0.5, 2.0],
        [0.1, 0.0],
        [0.0, 25.0],
      ]);
      final polygons = buildHeatmapPolygons(grid: grid, frameIndex: 0);
      expect(polygons, isNotEmpty);
      for (final p in polygons) {
        expect(p.isFilled, isTrue,
            reason: 'Polygon must have isFilled=true to be visible in flutter_map 6.x');
      }
    });

    test('polygon color matches precipitationColor for each value', () {
      final grid = makeGrid([
        [0.5, 0.0],
        [5.0, 0.0],
        [15.0, 0.0],
        [25.0, 0.0],
      ]);
      final polygons = buildHeatmapPolygons(grid: grid, frameIndex: 0);
      expect(polygons.length, equals(4));

      final expectedColors = [
        precipitationColor(0.5),
        precipitationColor(5.0),
        precipitationColor(15.0),
        precipitationColor(25.0),
      ];
      for (int i = 0; i < polygons.length; i++) {
        expect(polygons[i].color, equals(expectedColors[i]));
      }
    });

    test('polygon coordinates form a square around cell center', () {
      final grid = makeGrid([
        [5.0, 0.0],
        [0.0, 0.0],
        [0.0, 0.0],
        [0.0, 0.0],
      ]);
      final polygons = buildHeatmapPolygons(grid: grid, frameIndex: 0);
      expect(polygons.length, equals(1));

      final cell = grid.cells[0];
      final half = grid.spacing * 0.55;
      final pts = polygons[0].points;

      expect(pts.length, equals(4));
      // SW corner
      expect(pts[0].latitude, closeTo(cell.latitude - half, 0.0001));
      expect(pts[0].longitude, closeTo(cell.longitude - half, 0.0001));
      // SE corner
      expect(pts[1].latitude, closeTo(cell.latitude - half, 0.0001));
      expect(pts[1].longitude, closeTo(cell.longitude + half, 0.0001));
      // NE corner
      expect(pts[2].latitude, closeTo(cell.latitude + half, 0.0001));
      expect(pts[2].longitude, closeTo(cell.longitude + half, 0.0001));
      // NW corner
      expect(pts[3].latitude, closeTo(cell.latitude + half, 0.0001));
      expect(pts[3].longitude, closeTo(cell.longitude - half, 0.0001));
    });

    test('handles frameIndex out of bounds gracefully', () {
      final grid = makeGrid([
        [5.0, 3.0], // 2 values per cell
        [5.0, 3.0],
        [5.0, 3.0],
        [5.0, 3.0],
      ]);
      // frameIndex 5 is out of bounds (only 2 values) → value defaults to 0.0
      // → all cells transparent → empty list
      final polygons = buildHeatmapPolygons(grid: grid, frameIndex: 5);
      expect(polygons, isEmpty);
    });

    test('polygons have transparent border and zero border width', () {
      final grid = makeGrid([
        [5.0, 0.0],
        [0.0, 0.0],
        [0.0, 0.0],
        [0.0, 0.0],
      ]);
      final polygons = buildHeatmapPolygons(grid: grid, frameIndex: 0);
      expect(polygons.length, equals(1));
      expect(polygons[0].borderColor, equals(const Color(0x00000000)));
      expect(polygons[0].borderStrokeWidth, equals(0));
    });
  });
}
