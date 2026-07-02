import 'package:flutter/material.dart';
import 'package:hive_flutter/hive_flutter.dart';

import '../../domain/entities/app_settings.dart';

/// Repository that persists user settings in a dedicated Hive box.
class SettingsRepository {
  static const String _boxName = 'settings';

  static const String _themeModeKey = 'themeMode';
  static const String _languageCodeKey = 'languageCode';
  static const String _defaultCityKey = 'defaultCity';

  final LazyBox<dynamic> _box;

  const SettingsRepository._(this._box);

  static Future<SettingsRepository> open() async {
    final box = await Hive.openLazyBox<dynamic>(_boxName);
    return SettingsRepository._(box);
  }

  Future<AppSettings> load() async {
    final themeIndex = await _box.get(_themeModeKey) as int?;
    final languageCode = await _box.get(_languageCodeKey) as String?;
    final defaultCity = await _box.get(_defaultCityKey) as String?;

    return AppSettings(
      themeMode: _themeModeFromIndex(themeIndex),
      languageCode: languageCode ?? 'id',
      defaultCity: defaultCity ?? 'Jakarta',
    );
  }

  Future<void> save(AppSettings settings) async {
    await _box.put(_themeModeKey, settings.themeMode.index);
    await _box.put(_languageCodeKey, settings.languageCode);
    await _box.put(_defaultCityKey, settings.defaultCity);
  }

  static ThemeMode _themeModeFromIndex(int? index) {
    return switch (index) {
      0 => ThemeMode.system,
      1 => ThemeMode.light,
      2 => ThemeMode.dark,
      _ => ThemeMode.system,
    };
  }
}
