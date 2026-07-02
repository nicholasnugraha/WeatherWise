import 'dart:convert';
import 'package:hive_flutter/hive_flutter.dart';
import '../../../../core/config/api_config.dart';
import '../../domain/entities/current_weather.dart';
import '../../domain/entities/forecast.dart';
import '../../domain/entities/geocoding.dart';
import '../../domain/repositories/weather_repository.dart';
import '../datasources/cached_weather_entity.dart';
import '../datasources/weather_api_service.dart';
import '../models/geocoding_model.dart';
import '../models/onecall_model.dart';

class WeatherRepositoryImpl implements WeatherRepository {
  final WeatherApiService _apiService;
  final Box<CachedWeatherEntity> _cacheBox;

  WeatherRepositoryImpl({
    required WeatherApiService apiService,
    required Box<CachedWeatherEntity> cacheBox,
  })  : _apiService = apiService,
        _cacheBox = cacheBox;

  /// Single cache key for the full OneCall payload.
  /// Dashboard (current weather) and Forecast (daily/hourly) derive data
  /// from the SAME response, so they MUST share one cache entry.
  /// This guarantees dashboard and forecast are always in sync for the
  /// same coordinates.
  String _oneCallCacheKey(double lat, double lon) {
    return 'onecall_${lat.toStringAsFixed(2)}_${lon.toStringAsFixed(2)}';
  }

  @override
  Future<CurrentWeather> getCurrentWeatherByCity(String cityName) async {
    // 1. Resolve city -> coordinates via geocoding
    final matches = await _apiService.geocodeCity(
      cityName: cityName,
      apiKey: ApiConfig.apiKey,
      limit: 1,
    );
    if (matches.isEmpty) {
      throw Exception('Kota "$cityName" tidak ditemukan');
    }
    final first = matches.first;
    // 2. Fetch OneCall (current + hourly + daily) for those coordinates
    return _getCurrentWeatherByCoordInternal(
      lat: first.lat,
      lon: first.lon,
      cityName: first.name,
      country: first.country,
    );
  }

  @override
  Future<CurrentWeather> getCurrentWeatherByCoord(
    double lat,
    double lon,
  ) async {
    // Try to resolve display name via reverse geocoding (best effort)
    String? cityName;
    String? country;
    try {
      final rev = await _apiService.reverseGeocode(
        latitude: lat,
        longitude: lon,
        apiKey: ApiConfig.apiKey,
        limit: 1,
      );
      if (rev.isNotEmpty) {
        cityName = rev.first.name;
        country = rev.first.country;
      }
    } catch (_) {
      // Reverse geocoding is best-effort; ignore failures
    }
    return _getCurrentWeatherByCoordInternal(
      lat: lat,
      lon: lon,
      cityName: cityName,
      country: country,
    );
  }

  /// Internal: fetch OneCall and build CurrentWeather entity.
  /// Caches the full OneCall response so that getForecast() and
  /// getHourlyForecast() derive data from the SAME payload.
  Future<CurrentWeather> _getCurrentWeatherByCoordInternal({
    required double lat,
    required double lon,
    String? cityName,
    String? country,
  }) async {
    final cacheKey = _oneCallCacheKey(lat, lon);
    final cached = _cacheBox.get(cacheKey);

    OneCallResponse oneCall;
    if (cached != null && !cached.isExpired) {
      oneCall = OneCallResponse.fromJson(jsonDecode(cached.weatherJson));
    } else {
      oneCall = await _apiService.getOneCall(
        latitude: lat,
        longitude: lon,
        apiKey: ApiConfig.apiKey,
      );
      await _cacheBox.put(
        cacheKey,
        CachedWeatherEntity(
          locationKey: cacheKey,
          weatherJson: jsonEncode(oneCall.toJson()),
          timestamp: DateTime.now().millisecondsSinceEpoch ~/ 1000,
        ),
      );
    }

    return _mapOneCallToCurrentWeather(oneCall, cityName, country);
  }

  @override
  Future<Forecast> getForecast(double lat, double lon) async {
    final oneCall = await _getOneCallCached(lat, lon);
    return _mapOneCallToForecast(oneCall);
  }

  /// Lightweight forecast summary: just the first 24 hourly entries.
  /// Uses the same shared OneCall cache as current weather and daily forecast.
  Future<Forecast> getHourlyForecast(double lat, double lon) async {
    final oneCall = await _getOneCallCached(lat, lon);
    return _mapOneCallToHourly(oneCall);
  }

  /// Shared OneCall fetch/parse with unified cache key.
  /// Dashboard, 7-day forecast, and hourly forecast all read from the same
  /// cached payload to guarantee data consistency.
  Future<OneCallResponse> _getOneCallCached(double lat, double lon) async {
    final cacheKey = _oneCallCacheKey(lat, lon);
    final cached = _cacheBox.get(cacheKey);

    if (cached != null && !cached.isExpired) {
      return OneCallResponse.fromJson(jsonDecode(cached.weatherJson));
    }

    final oneCall = await _apiService.getOneCall(
      latitude: lat,
      longitude: lon,
      apiKey: ApiConfig.apiKey,
    );
    await _cacheBox.put(
      cacheKey,
      CachedWeatherEntity(
        locationKey: cacheKey,
        weatherJson: jsonEncode(oneCall.toJson()),
        timestamp: DateTime.now().millisecondsSinceEpoch ~/ 1000,
      ),
    );
    return oneCall;
  }

  @override
  Future<List<Geocoding>> searchCity(String query) async {
    final models = await _apiService.geocodeCity(
      cityName: query,
      apiKey: ApiConfig.apiKey,
    );
    return _mapGeocodingList(models);
  }

  @override
  Future<List<Geocoding>> reverseGeocode(double lat, double lon) async {
    final models = await _apiService.reverseGeocode(
      latitude: lat,
      longitude: lon,
      apiKey: ApiConfig.apiKey,
    );
    return _mapGeocodingList(models);
  }

  // ---- Mappers ----

  CurrentWeather _mapOneCallToCurrentWeather(
    OneCallResponse response,
    String? fallbackCityName,
    String? fallbackCountry,
  ) {
    final current = response.current;
    if (current == null) {
      throw Exception('OneCall response missing "current" block');
    }
    final w = current.weather.isNotEmpty
        ? current.weather.first
        : const OneCallWeather(
            id: 800, main: 'Clear', description: 'clear sky', icon: '01d');
    return CurrentWeather(
      cityName: fallbackCityName ?? response.timezone,
      country: fallbackCountry ?? '',
      lat: response.lat,
      lon: response.lon,
      temp: current.temp,
      feelsLike: current.feelsLike,
      // OneCall 3.0 current object has no min/max — pull from daily[0].temp.
      tempMin: (response.daily != null && response.daily!.isNotEmpty)
          ? response.daily!.first.temp.min
          : current.temp,
      tempMax: (response.daily != null && response.daily!.isNotEmpty)
          ? response.daily!.first.temp.max
          : current.temp,
      humidity: current.humidity,
      pressure: current.pressure,
      windSpeed: current.windSpeed,
      windDeg: current.windDeg,
      clouds: current.clouds,
      weatherMain: w.main,
      weatherDescription: w.description,
      weatherIcon: w.icon,
      sunrise: current.sunrise,
      sunset: current.sunset,
      timezone: response.timezoneOffset,
      timestamp: current.dt,
      uvi: current.uvi,
      visibility: current.visibility,
      dewPoint: current.dewPoint,
      // daily[0].rain is rain total for today (past 24h in local TZ).
      precipitationMm: (response.daily != null && response.daily!.isNotEmpty)
          ? response.daily!.first.rain
          : null,
    );
  }

  Forecast _mapOneCallToForecast(OneCallResponse response) {
    final daily = response.daily ?? const <OneCallDaily>[];
    // Use the first 8 daily entries (One Call 3.0 returns up to 8 days)
    final entries = daily.take(8).map((d) {
      final w = d.weather.isNotEmpty
          ? d.weather.first
          : const OneCallWeather(
              id: 800, main: 'Clear', description: 'clear sky', icon: '01d');
      return ForecastEntry(
        timestamp: d.dt,
        temp: d.temp.day,
        feelsLike: d.feelsLike.day,
        tempMin: d.temp.min,
        tempMax: d.temp.max,
        humidity: d.humidity,
        windSpeed: d.windSpeed,
        clouds: d.clouds,
        weatherMain: w.main,
        weatherDescription: w.description,
        weatherIcon: w.icon,
        rainProbability: (d.pop ?? 0) * 100,
        uvi: d.uvi,
        precipitationMm: d.rain,
      );
    }).toList();

    return Forecast(
      cityName: response.timezone,
      country: '',
      entries: entries,
    );
  }

  Forecast _mapOneCallToHourly(OneCallResponse response) {
    final hourly = response.hourly ?? const <OneCallHourly>[];
    // First 24 hourly entries
    final entries = hourly.take(24).map((h) {
      final w = h.weather.isNotEmpty
          ? h.weather.first
          : const OneCallWeather(
              id: 800, main: 'Clear', description: 'clear sky', icon: '01d');
      return ForecastEntry(
        timestamp: h.dt,
        temp: h.temp,
        feelsLike: h.feelsLike,
        tempMin: h.temp,
        tempMax: h.temp,
        humidity: h.humidity,
        windSpeed: h.windSpeed,
        clouds: h.clouds,
        weatherMain: w.main,
        weatherDescription: w.description,
        weatherIcon: w.icon,
        rainProbability: (h.pop ?? 0) * 100,
        uvi: h.uvi,
      );
    }).toList();

    return Forecast(
      cityName: response.timezone,
      country: '',
      entries: entries,
    );
  }

  List<Geocoding> _mapGeocodingList(List<GeocodingModel> models) {
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
}
