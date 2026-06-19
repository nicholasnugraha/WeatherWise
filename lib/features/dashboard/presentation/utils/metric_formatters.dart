import '../../../shared/domain/entities/current_weather.dart';

/// Formats a Unix-epoch timestamp + timezone offset to local "HH:mm" time.
///
/// Example: epoch=1700000000, offset=25200 (WIB) → "10:13".
String formatEpochTime(int epochSeconds, int timezoneOffsetSeconds) {
  final localMs = (epochSeconds + timezoneOffsetSeconds) * 1000;
  final dt = DateTime.fromMillisecondsSinceEpoch(localMs, isUtc: true);
  final hh = dt.hour.toString().padLeft(2, '0');
  final mm = dt.minute.toString().padLeft(2, '0');
  return '$hh:$mm';
}

/// Formats visibility (meters) into a human-readable string.
/// Caps at 10 km per OpenWeather's API maximum.
String formatVisibility(int meters) {
  if (meters >= 10000) return '10 km';
  final km = meters / 1000;
  return '${km.toStringAsFixed(1)} km';
}

/// Converts wind direction (degrees, 0=N, 90=E) to 8-point compass label.
String windCompass(int degrees) {
  // Normalize to 0-360.
  final d = ((degrees % 360) + 360) % 360;
  // 8 sectors of 45 degrees each, centered on each cardinal direction.
  const sectors = ['N', 'NE', 'E', 'SE', 'S', 'SW', 'W', 'NW'];
  final index = ((d + 22.5) ~/ 45) % 8;
  return sectors[index];
}

/// Converts a UV index number into a human-readable category.
String uviLabel(double uvi) {
  if (uvi < 3) return 'Rendah';
  if (uvi < 6) return 'Sedang';
  if (uvi < 8) return 'Tinggi';
  if (uvi < 11) return 'Sangat Tinggi';
  return 'Ekstrem';
}

/// Converts Kelvin dew-point to Celsius, formatted with degree symbol.
String formatDewPoint(double kelvin) {
  final celsius = kelvin - 273.15;
  return '${celsius.round()}°';
}

/// Converts m/s wind speed (OpenWeather default) to km/h.
/// OWM One Call 3.0 returns wind_speed in m/s unless `units=imperial`.
String formatWindKmh(double mps) => '${(mps * 3.6).round()} km/h';

/// Subtext for visibility card based on visibility distance.
String visibilitySubtext(int meters) {
  if (meters >= 10000) return 'Pandangan sangat jelas.';
  if (meters >= 5000) return 'Pandangan cukup jelas.';
  if (meters >= 2000) return 'Pandangan terbatas.';
  return 'Pandangan sangat rendah.';
}

/// Builds the 6 metric data rows for the dashboard grid from the
/// current weather snapshot. Centralized here so the dashboard widget
/// stays presentational only.
List<MetricData> buildMetricData(CurrentWeather weather) {
  return [
    MetricData(
      label: 'INDEKS UV',
      value: weather.uvi.toStringAsFixed(1),
      subtext: uviLabel(weather.uvi),
    ),
    MetricData(
      label: 'KELEMBAPAN',
      value: '${weather.humidity}%',
      subtext: weather.dewPoint != null
          ? 'Titik embun ${formatDewPoint(weather.dewPoint!)} saat ini.'
          : 'Kelembapan relatif.',
    ),
    MetricData(
      label: 'ANGIN',
      value: formatWindKmh(weather.windSpeed),
      subtext: windCompass(weather.windDeg),
    ),
    MetricData(
      label: 'JARAK PANDANG',
      value: formatVisibility(weather.visibility),
      subtext: visibilitySubtext(weather.visibility),
    ),
    MetricData(
      label: 'MATAHARI TERBIT',
      value: formatEpochTime(weather.sunrise, weather.timezone),
      subtext:
          'Matahari terbenam: ${formatEpochTime(weather.sunset, weather.timezone)}',
    ),
    MetricData(
      label: 'CURAH HUJAN',
      value: weather.precipitationMm != null
          ? '${weather.precipitationMm!.toStringAsFixed(1)} mm'
          : '0 mm',
      subtext: 'dalam 24 jam terakhir.',
    ),
  ];
}

/// Plain-data struct for one metric card.
class MetricData {
  const MetricData({
    required this.label,
    required this.value,
    required this.subtext,
  });

  final String label;
  final String value;
  final String subtext;
}