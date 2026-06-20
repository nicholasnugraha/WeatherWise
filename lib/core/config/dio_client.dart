import 'package:dio/dio.dart';

class DioClient {
  static Dio createWeatherDio() {
    return Dio(BaseOptions(
      baseUrl: 'https://api.openweathermap.org',
      connectTimeout: const Duration(seconds: 10),
      receiveTimeout: const Duration(seconds: 10),
      // No Content-Type header — GET requests don't need it, and sending it
      // triggers a CORS preflight (OPTIONS) that OWM doesn't support (405).
    ));
  }

  static Dio createRainViewerDio() {
    return Dio(BaseOptions(
      baseUrl: 'https://api.rainviewer.com',
      connectTimeout: const Duration(seconds: 10),
      receiveTimeout: const Duration(seconds: 10),
    ));
  }
}
