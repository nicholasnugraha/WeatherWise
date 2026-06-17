import 'package:hive_flutter/hive_flutter.dart';

part 'cached_weather_entity.g.dart';

@HiveType(typeId: 0)
class CachedWeatherEntity extends HiveObject {
  @HiveField(0)
  final String locationKey;

  @HiveField(1)
  final String weatherJson;

  @HiveField(2)
  final int timestamp;

  CachedWeatherEntity({
    required this.locationKey,
    required this.weatherJson,
    required this.timestamp,
  });

  bool get isExpired {
    final now = DateTime.now().millisecondsSinceEpoch ~/ 1000;
    return (now - timestamp) > 900; // 15 minutes
  }
}
