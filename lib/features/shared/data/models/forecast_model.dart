import 'package:freezed_annotation/freezed_annotation.dart';
import 'current_weather_model.dart';

part 'forecast_model.freezed.dart';
part 'forecast_model.g.dart';

@freezed
class ForecastResponse with _$ForecastResponse {
  const factory ForecastResponse({
    required String cod,
    required int cnt,
    required List<ForecastItem> list,
    required CityModel city,
  }) = _ForecastResponse;

  factory ForecastResponse.fromJson(Map<String, dynamic> json) =>
      _$ForecastResponseFromJson(json);
}

@freezed
class ForecastItem with _$ForecastItem {
  const factory ForecastItem({
    required int dt,
    required ForecastMain main,
    required List<WeatherModel> weather,
    required ForecastClouds clouds,
    required ForecastWind wind,
    required int visibility,
    required double pop,
    ForecastRain? rain,
    required ForecastSys sys,
    @JsonKey(name: 'dt_txt') required String dtTxt,
  }) = _ForecastItem;

  factory ForecastItem.fromJson(Map<String, dynamic> json) =>
      _$ForecastItemFromJson(json);
}

@freezed
class ForecastMain with _$ForecastMain {
  const factory ForecastMain({
    required double temp,
    @JsonKey(name: 'feels_like') required double feelsLike,
    @JsonKey(name: 'temp_min') required double tempMin,
    @JsonKey(name: 'temp_max') required double tempMax,
    required int pressure,
    @JsonKey(name: 'sea_level') required int seaLevel,
    @JsonKey(name: 'grnd_level') required int grndLevel,
    required int humidity,
    @JsonKey(name: 'temp_kf') double? tempKf,
  }) = _ForecastMain;

  factory ForecastMain.fromJson(Map<String, dynamic> json) =>
      _$ForecastMainFromJson(json);
}

@freezed
class ForecastClouds with _$ForecastClouds {
  const factory ForecastClouds({
    required int all,
  }) = _ForecastClouds;

  factory ForecastClouds.fromJson(Map<String, dynamic> json) =>
      _$ForecastCloudsFromJson(json);
}

@freezed
class ForecastWind with _$ForecastWind {
  const factory ForecastWind({
    required double speed,
    required int deg,
    required double gust,
  }) = _ForecastWind;

  factory ForecastWind.fromJson(Map<String, dynamic> json) =>
      _$ForecastWindFromJson(json);
}

@freezed
class ForecastRain with _$ForecastRain {
  const factory ForecastRain({
    @JsonKey(name: '3h') double? h3,
  }) = _ForecastRain;

  factory ForecastRain.fromJson(Map<String, dynamic> json) =>
      _$ForecastRainFromJson(json);
}

@freezed
class ForecastSys with _$ForecastSys {
  const factory ForecastSys({
    required String pod,
  }) = _ForecastSys;

  factory ForecastSys.fromJson(Map<String, dynamic> json) =>
      _$ForecastSysFromJson(json);
}

@freezed
class CityModel with _$CityModel {
  const factory CityModel({
    required int id,
    required String name,
    required CoordModel coord,
    required String country,
    required int population,
    required int timezone,
    required int sunrise,
    required int sunset,
  }) = _CityModel;

  factory CityModel.fromJson(Map<String, dynamic> json) =>
      _$CityModelFromJson(json);
}
