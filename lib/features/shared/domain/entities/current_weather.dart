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
  }) = _CurrentWeather;
}
