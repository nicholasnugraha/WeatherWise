import 'package:dio/dio.dart';

class DioClient {
  static Dio createWeatherDio() {
    return Dio(BaseOptions(
      baseUrl: 'https://api.openweathermap.org',
      connectTimeout: const Duration(seconds: 10),
      receiveTimeout: const Duration(seconds: 10),
      headers: {'Content-Type': 'application/json'},
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
