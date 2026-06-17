class ApiConfig {
  static const String openWeatherBaseUrl = 'https://api.openweathermap.org/data/2.5';
  static const String geocodingBaseUrl = 'https://api.openweathermap.org/geo/1.0';
  static const String rainViewerBaseUrl = 'https://api.rainviewer.com';

  // API Key loaded from environment variable at build time
  // Use: flutter run --dart-define=WEATHER_API_KEY=your_key
  static const String apiKey = String.fromEnvironment('WEATHER_API_KEY');
}
