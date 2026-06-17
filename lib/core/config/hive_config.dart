import 'package:hive_flutter/hive_flutter.dart';
import '../../features/shared/data/datasources/cached_weather_entity.dart';

class HiveConfig {
  static const String weatherBoxName = 'weather_cache';

  /// Bump this whenever the cache schema (CachedWeatherEntity JSON shape
  /// or the encoding of `weatherJson`) changes in a backwards-incompatible
  /// way. On startup we delete entries whose stored version is lower.
  static const int currentCacheSchemaVersion = 2;

  static const String _schemaVersionKey = '__schema_version__';

  static Future<void> init() async {
    await Hive.initFlutter();
    Hive.registerAdapter(CachedWeatherEntityAdapter());
    final box = await Hive.openBox<CachedWeatherEntity>(weatherBoxName);
    await _migrateOrInvalidateCache(box);
  }

  /// If the stored schema version is older than the current one, clear the
  /// box so we don't try to parse incompatible JSON (e.g. legacy entries
  /// encoded with the /data/2.5 shape after migrating to /data/3.0/onecall).
  static Future<void> _migrateOrInvalidateCache(
    Box<CachedWeatherEntity> box,
  ) async {
    final storedVersion =
        box.get(_schemaVersionKey)?.timestamp ?? 0;
    if (storedVersion < currentCacheSchemaVersion) {
      await box.clear();
      await box.put(
        _schemaVersionKey,
        CachedWeatherEntity(
          locationKey: _schemaVersionKey,
          weatherJson: '',
          timestamp: currentCacheSchemaVersion,
        ),
      );
    }
  }
}
