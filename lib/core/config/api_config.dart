import 'package:flutter_dotenv/flutter_dotenv.dart';

class ApiConfig {
  static const String openWeatherBaseUrl = 'https://api.openweathermap.org/data/2.5';
  static const String onecallBaseUrl = 'https://api.openweathermap.org/data/3.0';
  static const String geocodingBaseUrl = 'https://api.openweathermap.org/geo/1.0';
  static const String rainViewerBaseUrl = 'https://api.rainviewer.com';

  /// API key loaded from the bundled .env file (assets/.env).
  /// Make sure .env exists in the project root and is listed under
  /// `flutter.assets` in pubspec.yaml.
  ///
  /// In a development run, place your real key in `.env` and never
  /// commit it. In CI / production, the value is read from the
  /// bundled asset shipped with the APK.
  static String get apiKey {
    final value = dotenv.env['WEATHER_API_KEY'] ?? '';
    return value;
  }

  /// Returns true if the API key was provided at build time.
  /// Use this to fail fast with a helpful error instead of getting a
  /// cryptic 401 from the server.
  static bool get isApiKeyConfigured => apiKey.isNotEmpty;
}
