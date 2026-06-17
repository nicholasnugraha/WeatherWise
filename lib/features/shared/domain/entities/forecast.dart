import 'package:freezed_annotation/freezed_annotation.dart';

part 'forecast.freezed.dart';

@freezed
class Forecast with _$Forecast {
  const factory Forecast({
    required String cityName,
    required String country,
    required List<ForecastEntry> entries,
  }) = _Forecast;
}

@freezed
class ForecastEntry with _$ForecastEntry {
  const factory ForecastEntry({
    required int timestamp,
    required double temp,
    required double feelsLike,
    required double tempMin,
    required double tempMax,
    required int humidity,
    required double windSpeed,
    required int clouds,
    required String weatherMain,
    required String weatherDescription,
    required String weatherIcon,
    required double rainProbability,
  }) = _ForecastEntry;
}
