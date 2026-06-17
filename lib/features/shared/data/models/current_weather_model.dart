import 'package:freezed_annotation/freezed_annotation.dart';

part 'current_weather_model.freezed.dart';
part 'current_weather_model.g.dart';

@freezed
class CurrentWeatherModel with _$CurrentWeatherModel {
  const factory CurrentWeatherModel({
    required CoordModel coord,
    required List<WeatherModel> weather,
    required MainModel main,
    required WindModel wind,
    required CloudsModel clouds,
    @JsonKey(name: 'dt') required int timestamp,
    required SysModel sys,
    required int timezone,
    required int id,
    required String name,
  }) = _CurrentWeatherModel;

  factory CurrentWeatherModel.fromJson(Map<String, dynamic> json) =>
      _$CurrentWeatherModelFromJson(json);
}

@freezed
class CoordModel with _$CoordModel {
  const factory CoordModel({
    required double lon,
    required double lat,
  }) = _CoordModel;

  factory CoordModel.fromJson(Map<String, dynamic> json) =>
      _$CoordModelFromJson(json);
}

@freezed
class WeatherModel with _$WeatherModel {
  const factory WeatherModel({
    required int id,
    required String main,
    required String description,
    required String icon,
  }) = _WeatherModel;

  factory WeatherModel.fromJson(Map<String, dynamic> json) =>
      _$WeatherModelFromJson(json);
}

@freezed
class MainModel with _$MainModel {
  const factory MainModel({
    required double temp,
    @JsonKey(name: 'feels_like') required double feelsLike,
    @JsonKey(name: 'temp_min') required double tempMin,
    @JsonKey(name: 'temp_max') required double tempMax,
    required int pressure,
    required int humidity,
    @JsonKey(name: 'sea_level') int? seaLevel,
    @JsonKey(name: 'grnd_level') int? grndLevel,
  }) = _MainModel;

  factory MainModel.fromJson(Map<String, dynamic> json) =>
      _$MainModelFromJson(json);
}

@freezed
class WindModel with _$WindModel {
  const factory WindModel({
    required double speed,
    required int deg,
    double? gust,
  }) = _WindModel;

  factory WindModel.fromJson(Map<String, dynamic> json) =>
      _$WindModelFromJson(json);
}

@freezed
class CloudsModel with _$CloudsModel {
  const factory CloudsModel({
    required int all,
  }) = _CloudsModel;

  factory CloudsModel.fromJson(Map<String, dynamic> json) =>
      _$CloudsModelFromJson(json);
}

@freezed
class SysModel with _$SysModel {
  const factory SysModel({
    required int type,
    required int id,
    required String country,
    required int sunrise,
    required int sunset,
  }) = _SysModel;

  factory SysModel.fromJson(Map<String, dynamic> json) =>
      _$SysModelFromJson(json);
}
