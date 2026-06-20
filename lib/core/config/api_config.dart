import 'package:flutter/foundation.dart';
import 'package:flutter_dotenv/flutter_dotenv.dart';

class ApiConfig {
  static const String openWeatherBaseUrl = 'https://api.openweathermap.org/data/2.5';
  static const String onecallBaseUrl = 'https://api.openweathermap.org/data/2.5';
  static const String geocodingBaseUrl = 'https://api.openweathermap.org/geo/1.0';
  static const String rainViewerBaseUrl = 'https://api.rainviewer.com';

  /// API key resolution order:
  /// 1. --dart-define=WEATHER_API_KEY=*** (highest priority, used at build time)
  /// 2. .env bundled in the APK (loaded by main.dart via flutter_dotenv)
  /// 3. Empty string (will fail at first API call)
  static String get apiKey {
    const fromDartDefine = String.fromEnvironment('WEATHER_API_KEY');
    if (fromDartDefine.isNotEmpty) {
      return fromDartDefine;
    }
    final fromEnv = dotenv.env['WEATHER_API_KEY'] ?? '';
    if (fromEnv.isEmpty && kDebugMode) {
      debugPrint(
        '[ApiConfig] WEATHER_API_KEY is empty. Set --dart-define or fill assets/env.config.',
      );
    }
    return fromEnv;
  }

  /// Returns true if the API key was provided at build time.
  /// Use this to fail fast with a helpful error instead of getting a
  /// cryptic 401 from the server.
  static bool get isApiKeyConfigured => apiKey.isNotEmpty;

  /// Diagnostic info: how the key was resolved.
  /// Safe to log (does not include the key itself).
  static String get keySource {
    const fromDartDefine = String.fromEnvironment('WEATHER_API_KEY');
    if (fromDartDefine.isNotEmpty) return 'dart-define';
    if ((dotenv.env['WEATHER_API_KEY'] ?? '').isNotEmpty) return 'assets/env.config';
    return 'none';
  }
}
