import 'dart:convert';
import 'package:hive_flutter/hive_flutter.dart';
import '../../../../core/config/api_config.dart';
import '../../domain/entities/current_weather.dart';
import '../../domain/entities/forecast.dart';
import '../../domain/entities/geocoding.dart';
import '../../domain/repositories/weather_repository.dart';
import '../datasources/cached_weather_entity.dart';
import '../datasources/weather_api_service.dart';
import '../models/current_weather_model.dart';
import '../models/geocoding_model.dart';

class WeatherRepositoryImpl implements WeatherRepository {
  final WeatherApiService _apiService;
  final Box<CachedWeatherEntity> _cacheBox;

  WeatherRepositoryImpl({
    required WeatherApiService apiService,
    required Box<CachedWeatherEntity> cacheBox,
  })  : _apiService = apiService,
        _cacheBox = cacheBox;

  @override
  Future<CurrentWeather> getCurrentWeatherByCity(String cityName) async {
    const cacheKey = 'city';
    final cached = _cacheBox.get(cacheKey);

    if (cached != null && !cached.isExpired) {
      final model = CurrentWeatherModel.fromJson(jsonDecode(cached.weatherJson));
      return _mapToCurrentWeather(model);
    }

    final model = await _apiService.getCurrentWeatherByCity(
      cityName: cityName,
      apiKey: ApiConfig.apiKey,
    );

    await _cacheBox.put(
      cacheKey,
      CachedWeatherEntity(
        locationKey: cacheKey,
        weatherJson: jsonEncode(model.toJson()),
        timestamp: DateTime.now().millisecondsSinceEpoch ~/ 1000,
      ),
    );

    return _mapToCurrentWeather(model);
  }

  @override
  Future<CurrentWeather> getCurrentWeatherByCoord(double lat, double lon) async {
    const cacheKey = 'coord';
    final cached = _cacheBox.get(cacheKey);

    if (cached != null && !cached.isExpired) {
      final model = CurrentWeatherModel.fromJson(jsonDecode(cached.weatherJson));
      return _mapToCurrentWeather(model);
    }

    final model = await _apiService.getCurrentWeatherByCoord(
      latitude: lat,
      longitude: lon,
      apiKey: ApiConfig.apiKey,
    );

    await _cacheBox.put(
      cacheKey,
      CachedWeatherEntity(
        locationKey: cacheKey,
        weatherJson: jsonEncode(model.toJson()),
        timestamp: DateTime.now().millisecondsSinceEpoch ~/ 1000,
      ),
    );

    return _mapToCurrentWeather(model);
  }

  @override
  Future<Forecast> getForecast(double lat, double lon) async {
    final response = await _apiService.getForecast(
      latitude: lat,
      longitude: lon,
      apiKey: ApiConfig.apiKey,
    );

    return Forecast(
      cityName: response.city.name,
      country: response.city.country,
      entries: response.list.map((item) => ForecastEntry(
        timestamp: item.dt,
        temp: item.main.temp,
        feelsLike: item.main.feelsLike,
        tempMin: item.main.tempMin,
        tempMax: item.main.tempMax,
        humidity: item.main.humidity,
        windSpeed: item.wind.speed,
        clouds: item.clouds.all,
        weatherMain: item.weather.first.main,
        weatherDescription: item.weather.first.description,
        weatherIcon: item.weather.first.icon,
        rainProbability: item.pop * 100,
      )).toList(),
    );
  }

  @override
  Future<List<Geocoding>> searchCity(String query) async {
    final models = await _apiService.geocodeCity(
      cityName: query,
      apiKey: ApiConfig.apiKey,
    );

    return models
        .map((m) => Geocoding(
              name: m.name,
              displayName: m.displayName,
              lat: m.lat,
              lon: m.lon,
              country: m.country,
              state: m.state,
            ))
        .toList();
  }

  @override
  Future<List<Geocoding>> reverseGeocode(double lat, double lon) async {
    final models = await _apiService.reverseGeocode(
      latitude: lat,
      longitude: lon,
      apiKey: ApiConfig.apiKey,
    );

    return models
        .map((m) => Geocoding(
              name: m.name,
              displayName: m.displayName,
              lat: m.lat,
              lon: m.lon,
              country: m.country,
              state: m.state,
            ))
        .toList();
  }

  CurrentWeather _mapToCurrentWeather(CurrentWeatherModel model) {
    return CurrentWeather(
      cityName: model.name,
      country: model.sys.country,
      lat: model.coord.lat,
      lon: model.coord.lon,
      temp: model.main.temp,
      feelsLike: model.main.feelsLike,
      tempMin: model.main.tempMin,
      tempMax: model.main.tempMax,
      humidity: model.main.humidity,
      pressure: model.main.pressure,
      windSpeed: model.wind.speed,
      windDeg: model.wind.deg,
      clouds: model.clouds.all,
      weatherMain: model.weather.first.main,
      weatherDescription: model.weather.first.description,
      weatherIcon: model.weather.first.icon,
      sunrise: model.sys.sunrise,
      sunset: model.sys.sunset,
      timezone: model.timezone,
      timestamp: model.timestamp,
    );
  }
}
