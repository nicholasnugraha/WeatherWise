import 'package:flutter_map/flutter_map.dart';
import 'package:latlong2/latlong.dart';

/// BMKG radar station metadata.
///
/// BMKG operates ~17+ weather radars across Indonesia. Each station provides
/// an animated GIF (CMAX product) via:
///   https://cuaca.bmkg.go.id/data/public/sidarma/ANIMASI/CMAX_{Station}.gif
///
/// Radar range is ~240km from the station. The CMAX product shows maximum
/// reflectivity (dBZ) composited from multiple elevation angles.
class BmkgRadarStation {
  const BmkgRadarStation({
    required this.name,
    required this.urlName,
    required this.center,
    this.rangeKm = 240,
  });

  final String name;
  final String urlName;
  final LatLng center;
  final double rangeKm;

  /// GIF URL on the BMKG cuaca server.
  String get gifUrl =>
      'https://cuaca.bmkg.go.id/data/public/sidarma/ANIMASI/CMAX_$urlName.gif';

  /// Approximate geographic bounds for this station's radar coverage.
  /// Uses a simple lat/lon offset from the center based on range.
  /// At Indonesia's latitude, 1° lat ≈ 111km, 1° lon ≈ 111*cos(lat) km.
  LatLngBounds get bounds {
    final latOffset = rangeKm / 111.0;
    final lonOffset = rangeKm / (111.0 * 0.99); // ~cos(8°) ≈ 0.99 for Indonesia
    return LatLngBounds(
      LatLng(center.latitude - latOffset, center.longitude - lonOffset),
      LatLng(center.latitude + latOffset, center.longitude + lonOffset),
    );
  }
}

/// All known BMKG radar stations with public CMAX images.
///
/// Station coordinates are approximate (city/airport locations).
/// Data source: https://cuaca.bmkg.go.id/data/public/sidarma/ANIMASI/
class BmkgRadarStations {
  BmkgRadarStations._();

  static const all = <BmkgRadarStation>[
    BmkgRadarStation(name: 'Medan', urlName: 'Medan', center: LatLng(3.55, 98.67)),
    BmkgRadarStation(name: 'Padang', urlName: 'Padang', center: LatLng(-0.32, 100.35)),
    BmkgRadarStation(name: 'Palembang', urlName: 'Palembang', center: LatLng(-2.93, 104.72)),
    BmkgRadarStation(name: 'Pontianak', urlName: 'Pontianak', center: LatLng(-0.03, 109.32)),
    BmkgRadarStation(name: 'Sintang', urlName: 'Sintang', center: LatLng(0.13, 111.48)),
    BmkgRadarStation(name: 'Pangkalan Bun', urlName: 'Pangkalanbun', center: LatLng(-2.73, 111.62)),
    BmkgRadarStation(name: 'Banjarmasin', urlName: 'Banjarmasin', center: LatLng(-3.32, 114.59)),
    BmkgRadarStation(name: 'Balikpapan', urlName: 'Balikpapan', center: LatLng(-1.27, 116.82)),
    BmkgRadarStation(name: 'Surabaya', urlName: 'Surabaya', center: LatLng(-7.38, 112.76)),
    BmkgRadarStation(name: 'Denpasar', urlName: 'Denpasar', center: LatLng(-8.75, 115.17)),
    BmkgRadarStation(name: 'Kupang', urlName: 'Kupang', center: LatLng(-10.17, 123.61)),
    BmkgRadarStation(name: 'Manado', urlName: 'Manado', center: LatLng(1.55, 124.92)),
    BmkgRadarStation(name: 'Ambon', urlName: 'Ambon', center: LatLng(-3.70, 128.08)),
    BmkgRadarStation(name: 'Sorong', urlName: 'Sorong', center: LatLng(-0.88, 131.25)),
    BmkgRadarStation(name: 'Biak', urlName: 'Biak', center: LatLng(-1.19, 136.11)),
    BmkgRadarStation(name: 'Jayapura', urlName: 'Jayapura', center: LatLng(-2.59, 140.66)),
    BmkgRadarStation(name: 'Merauke', urlName: 'Merauke', center: LatLng(-8.46, 140.38)),
  ];

  /// Find the nearest station to a given coordinate.
  static BmkgRadarStation? nearestTo(double lat, double lon) {
    const distance = Distance();
    var nearest = all.first;
    var minDist = double.infinity;
    for (final s in all) {
      final d = distance.as(LengthUnit.Kilometer, s.center, LatLng(lat, lon));
      if (d < minDist) {
        minDist = d;
        nearest = s;
      }
    }
    return minDist <= 240 ? nearest : null;
  }
}

/// The national composite radar image covering all of Indonesia.
/// URL: https://cuaca.bmkg.go.id/data/public/sidarma/ANIMASI/CMAX_Indonesia.gif
///
/// Geographic bounds are approximate (estimated from Indonesia's extent).
/// The image is 1165×624 px, animated GIF with ~117 frames.
class BmkgRadarComposite {
  BmkgRadarComposite._();

  static const String url =
      'https://cuaca.bmkg.go.id/data/public/sidarma/ANIMASI/CMAX_Indonesia.gif';

  /// Approximate geographic bounds for the Indonesia composite.
  /// Indonesia spans roughly 94°E–142°E, 6°N–12°S.
  static final LatLngBounds bounds = LatLngBounds(
    const LatLng(-12.0, 94.0), // southwest
    const LatLng(8.0, 142.0),  // northeast
  );
}
