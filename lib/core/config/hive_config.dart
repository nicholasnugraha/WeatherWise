import 'package:hive_flutter/hive_flutter.dart';
import '../../features/shared/data/datasources/cached_weather_entity.dart';

class HiveConfig {
  static const String weatherBoxName = 'weather_cache';

  static Future<void> init() async {
    await Hive.initFlutter();
    Hive.registerAdapter(CachedWeatherEntityAdapter());
    await Hive.openBox<CachedWeatherEntity>(weatherBoxName);
  }
}
