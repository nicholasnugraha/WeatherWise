/// Maps OpenWeatherMap `weatherMain` codes (English) to Indonesian display
/// strings used across the dashboard, forecast, and hourly widgets.
///
/// Per `docs/prd/web-app.md` Section 16.5 IC2, all UI strings must be in
/// Bahasa Indonesia — not English descriptions from the API.
String weatherMainToIndonesian(String main) {
  switch (main.toLowerCase()) {
    case 'clear':
      return 'Cerah';
    case 'clouds':
      return 'Berawan';
    case 'rain':
      return 'Hujan';
    case 'drizzle':
      return 'Gerimis';
    case 'thunderstorm':
      return 'Hujan Petir';
    case 'snow':
      return 'Salju';
    case 'mist':
    case 'fog':
    case 'haze':
    case 'smoke':
      return 'Kabut';
    default:
      return main; // Fallback to raw value — better than empty.
  }
}

/// Determines whether the local time at the given coordinate offset is
/// currently daytime, using sunrise/sunset from the API response.
bool isDaytime({
  required int sunriseEpoch,
  required int sunsetEpoch,
  required int timezoneOffsetSeconds,
}) {
  final now = DateTime.now()
      .toUtc()
      .add(Duration(seconds: timezoneOffsetSeconds))
      .millisecondsSinceEpoch ~/
      1000;
  return now >= sunriseEpoch && now < sunsetEpoch;
}