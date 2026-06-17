import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:hive_flutter/hive_flutter.dart';
import '../../features/shared/data/datasources/cached_weather_entity.dart';
import '../../features/shared/data/datasources/rainviewer_api_service.dart';
import '../../features/shared/data/datasources/weather_api_service.dart';
import '../../features/shared/data/repositories/weather_repository_impl.dart';
import '../../features/shared/domain/repositories/weather_repository.dart';
import '../config/hive_config.dart';
import '../utils/location_service.dart';

final dioProvider = Provider<Dio>((ref) {
  return Dio(BaseOptions(
    baseUrl: 'https://api.openweathermap.org',
    connectTimeout: const Duration(seconds: 10),
    receiveTimeout: const Duration(seconds: 10),
    headers: {'Content-Type': 'application/json'},
  ));
});

final rainViewerDioProvider = Provider<Dio>((ref) {
  return Dio(BaseOptions(
    connectTimeout: const Duration(seconds: 10),
    receiveTimeout: const Duration(seconds: 10),
  ));
});

final weatherApiServiceProvider = Provider<WeatherApiService>((ref) {
  final dio = ref.watch(dioProvider);
  return WeatherApiService(dio);
});

final rainViewerApiServiceProvider = Provider<RainViewerApiService>((ref) {
  final dio = ref.watch(rainViewerDioProvider);
  return RainViewerApiService(dio);
});

final cacheBoxProvider = Provider<Box<CachedWeatherEntity>>((ref) {
  return Hive.box<CachedWeatherEntity>(HiveConfig.weatherBoxName);
});

final weatherRepositoryProvider = Provider<WeatherRepository>((ref) {
  final apiService = ref.watch(weatherApiServiceProvider);
  final cacheBox = ref.watch(cacheBoxProvider);
  return WeatherRepositoryImpl(
    apiService: apiService,
    cacheBox: cacheBox,
  );
});

final locationServiceProvider = Provider<LocationService>((ref) {
  return LocationService();
});
