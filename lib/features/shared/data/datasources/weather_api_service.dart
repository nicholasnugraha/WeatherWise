import 'package:dio/dio.dart';
import '../models/current_weather_model.dart';
import '../models/forecast_model.dart';
import '../models/geocoding_model.dart';

class WeatherApiService {
  final Dio _dio;

  WeatherApiService(this._dio);

  Future<CurrentWeatherModel> getCurrentWeatherByCity({
    required String cityName,
    required String apiKey,
    String units = 'metric',
    String lang = 'id',
  }) async {
    final response = await _dio.get(
      '/data/2.5/weather',
      queryParameters: {
        'q': cityName,
        'appid': apiKey,
        'units': units,
        'lang': lang,
      },
    );
    return CurrentWeatherModel.fromJson(response.data);
  }

  Future<CurrentWeatherModel> getCurrentWeatherByCoord({
    required double latitude,
    required double longitude,
    required String apiKey,
    String units = 'metric',
    String lang = 'id',
  }) async {
    final response = await _dio.get(
      '/data/2.5/weather',
      queryParameters: {
        'lat': latitude,
        'lon': longitude,
        'appid': apiKey,
        'units': units,
        'lang': lang,
      },
    );
    return CurrentWeatherModel.fromJson(response.data);
  }

  Future<ForecastResponse> getForecast({
    required double latitude,
    required double longitude,
    required String apiKey,
    String units = 'metric',
    String lang = 'id',
  }) async {
    final response = await _dio.get(
      '/data/2.5/forecast',
      queryParameters: {
        'lat': latitude,
        'lon': longitude,
        'appid': apiKey,
        'units': units,
        'lang': lang,
      },
    );
    return ForecastResponse.fromJson(response.data);
  }

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
