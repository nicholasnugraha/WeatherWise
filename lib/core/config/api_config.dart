class ApiConfig {
  static const String openWeatherBaseUrl = 'https://api.openweathermap.org/data/2.5';
  static const String onecallBaseUrl = 'https://api.openweathermap.org/data/3.0';
  static const String geocodingBaseUrl = 'https://api.openweathermap.org/geo/1.0';
  static const String rainViewerBaseUrl = 'https://api.rainviewer.com';

  /// API Key loaded from environment variable at build time.
  /// Use one of:
  ///   flutter run  --dart-define=WEATHER_API_KEY=your_key
  ///   flutter build apk --release --dart-define=WEATHER_API_KEY=your_key
  static const String apiKey = String.fromEnvironment('WEATHER_API_KEY');

  /// Returns true if the API key was provided at build time.
  /// Use this to fail fast with a helpful error instead of getting a
  /// cryptic 401 from the server.
  static bool get isApiKeyConfigured => apiKey.isNotEmpty;
}
