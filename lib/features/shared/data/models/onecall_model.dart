import 'package:freezed_annotation/freezed_annotation.dart';

part 'onecall_model.freezed.dart';
part 'onecall_model.g.dart';

/// Response model for OpenWeatherMap One Call API 3.0
/// Endpoint: /data/3.0/onecall
/// Docs: https://openweathermap.org/api/one-call-3
@freezed
class OneCallResponse with _$OneCallResponse {
  const factory OneCallResponse({
    required double lat,
    required double lon,
    required String timezone,
    @JsonKey(name: 'timezone_offset') required int timezoneOffset,
    @JsonKey(name: 'current') OneCallCurrent? current,
    @JsonKey(name: 'hourly') List<OneCallHourly>? hourly,
    @JsonKey(name: 'daily') List<OneCallDaily>? daily,
  }) = _OneCallResponse;

  factory OneCallResponse.fromJson(Map<String, dynamic> json) =>
      _$OneCallResponseFromJson(json);
}

@freezed
class OneCallCurrent with _$OneCallCurrent {
  const factory OneCallCurrent({
    required int dt,
    required int sunrise,
    required int sunset,
    required double temp,
    @JsonKey(name: 'feels_like') required double feelsLike,
    required int pressure,
    required int humidity,
    @JsonKey(name: 'dew_point') double? dewPoint,
    required int clouds,
    required double uvi,
    required int visibility,
    @JsonKey(name: 'wind_speed') required double windSpeed,
    @JsonKey(name: 'wind_deg') required int windDeg,
    @JsonKey(name: 'wind_gust') double? windGust,
    required List<OneCallWeather> weather,
  }) = _OneCallCurrent;

  factory OneCallCurrent.fromJson(Map<String, dynamic> json) =>
      _$OneCallCurrentFromJson(json);
}

@freezed
class OneCallHourly with _$OneCallHourly {
  const factory OneCallHourly({
    required int dt,
    required double temp,
    @JsonKey(name: 'feels_like') required double feelsLike,
    required int pressure,
    required int humidity,
    @JsonKey(name: 'dew_point') double? dewPoint,
    required int clouds,
    required double uvi,
    required int visibility,
    @JsonKey(name: 'wind_speed') required double windSpeed,
    @JsonKey(name: 'wind_deg') required int windDeg,
    @JsonKey(name: 'wind_gust') double? windGust,
    required List<OneCallWeather> weather,
    /// Probability of precipitation (0-1)
    @JsonKey(name: 'pop') double? pop,
  }) = _OneCallHourly;

  factory OneCallHourly.fromJson(Map<String, dynamic> json) =>
      _$OneCallHourlyFromJson(json);
}

@freezed
class OneCallDaily with _$OneCallDaily {
  const factory OneCallDaily({
    required int dt,
    required int sunrise,
    required int sunset,
    @JsonKey(name: 'moonrise') int? moonrise,
    @JsonKey(name: 'moonset') int? moonset,
    @JsonKey(name: 'moon_phase') double? moonPhase,
    required OneCallTemp temp,
    @JsonKey(name: 'feels_like') required OneCallFeelsLike feelsLike,
    required int pressure,
    required int humidity,
    @JsonKey(name: 'dew_point') double? dewPoint,
    @JsonKey(name: 'wind_speed') required double windSpeed,
    @JsonKey(name: 'wind_deg') required int windDeg,
    @JsonKey(name: 'wind_gust') double? windGust,
    required List<OneCallWeather> weather,
    required int clouds,
    required double uvi,
    /// Probability of precipitation (0-1)
    @JsonKey(name: 'pop') double? pop,
    /// Precipitation in mm
    @JsonKey(name: 'rain') double? rain,
    @JsonKey(name: 'snow') double? snow,
  }) = _OneCallDaily;

  factory OneCallDaily.fromJson(Map<String, dynamic> json) =>
      _$OneCallDailyFromJson(json);
}

@freezed
class OneCallTemp with _$OneCallTemp {
  const factory OneCallTemp({
    required double day,
    required double min,
    required double max,
    required double night,
    required double eve,
    required double morn,
  }) = _OneCallTemp;

  factory OneCallTemp.fromJson(Map<String, dynamic> json) =>
      _$OneCallTempFromJson(json);
}

@freezed
class OneCallFeelsLike with _$OneCallFeelsLike {
  const factory OneCallFeelsLike({
    required double day,
    required double night,
    required double eve,
    required double morn,
  }) = _OneCallFeelsLike;

  factory OneCallFeelsLike.fromJson(Map<String, dynamic> json) =>
      _$OneCallFeelsLikeFromJson(json);
}

@freezed
class OneCallWeather with _$OneCallWeather {
  const factory OneCallWeather({
    required int id,
    required String main,
    required String description,
    required String icon,
  }) = _OneCallWeather;

  factory OneCallWeather.fromJson(Map<String, dynamic> json) =>
      _$OneCallWeatherFromJson(json);
}
