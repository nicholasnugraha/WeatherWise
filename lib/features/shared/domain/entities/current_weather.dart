import 'package:freezed_annotation/freezed_annotation.dart';

part 'current_weather.freezed.dart';

@freezed
class CurrentWeather with _$CurrentWeather {
  const factory CurrentWeather({
    required String cityName,
    required String country,
    required double lat,
    required double lon,
    required double temp,
    required double feelsLike,
    required double tempMin,
    required double tempMax,
    required int humidity,
    required int pressure,
    required double windSpeed,
    required int windDeg,
    required int clouds,
    required String weatherMain,
    required String weatherDescription,
    required String weatherIcon,
    required int sunrise,
    required int sunset,
    required int timezone,
    required int timestamp,

    /// UV index (from OpenWeather One Call `current.uvi`).
    required double uvi,

    /// Visibility in meters (from One Call `current.visibility`, max 10000).
    required int visibility,

    /// Dew point in Kelvin (from One Call `current.dew_point`).
    /// Null if not returned by the API.
    double? dewPoint,

    /// Total precipitation (rain) in mm over the past 24h, derived from
    /// `daily[0].rain`. Null if the API did not return a rain total.
    @Default(null) double? precipitationMm,
  }) = _CurrentWeather;
}