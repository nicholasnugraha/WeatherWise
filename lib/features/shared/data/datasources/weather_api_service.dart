import 'package:dio/dio.dart';
import '../models/geocoding_model.dart';
import '../models/onecall_model.dart';

class WeatherApiService {
  final Dio _dio;

  WeatherApiService(this._dio);

  /// Get complete weather data (current + hourly + daily) in one call.
  /// Endpoint: /data/2.5/onecall
  /// Docs: https://openweathermap.org/api/one-call
  ///
  /// [exclude] is a comma-separated list of parts to exclude from the response
  /// to save bandwidth. Example: 'minutely,alerts'.
  Future<OneCallResponse> getOneCall({
    required double latitude,
    required double longitude,
    required String apiKey,
    String units = 'metric',
    String lang = 'id',
    String exclude = 'minutely,alerts',
  }) async {
    final response = await _dio.get(
      '/data/2.5/onecall',
      queryParameters: {
        'lat': latitude,
        'lon': longitude,
        'appid': apiKey,
        'units': units,
        'lang': lang,
        'exclude': exclude,
      },
    );
    return OneCallResponse.fromJson(response.data);
  }

  /// Search city by name (forward geocoding).
  /// Endpoint: /geo/1.0/direct
  /// (One Call does not provide city search by name.)
  Future<List<GeocodingModel>> geocodeCity({
    required String cityName,
    int limit = 5,
    required String apiKey,
  }) async {
    final response = await _dio.get(
      '/geo/1.0/direct',
      queryParameters: {
        'q': cityName,
        'limit': limit,
        'appid': apiKey,
      },
    );
    return (response.data as List)
        .map((e) => GeocodingModel.fromJson(e))
        .toList();
  }

  /// Reverse geocoding: coordinates -> city name.
  /// Endpoint: /geo/1.0/reverse
  Future<List<GeocodingModel>> reverseGeocode({
    required double latitude,
    required double longitude,
    int limit = 1,
    required String apiKey,
  }) async {
    final response = await _dio.get(
      '/geo/1.0/reverse',
      queryParameters: {
        'lat': latitude,
        'lon': longitude,
        'limit': limit,
        'appid': apiKey,
      },
    );
    return (response.data as List)
        .map((e) => GeocodingModel.fromJson(e))
        .toList();
  }
}
