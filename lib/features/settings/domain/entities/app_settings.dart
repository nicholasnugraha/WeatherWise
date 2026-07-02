import 'package:flutter/material.dart';

/// User-configurable app settings.
class AppSettings {
  final ThemeMode themeMode;
  final String languageCode;
  final String defaultCity;

  const AppSettings({
    this.themeMode = ThemeMode.system,
    this.languageCode = 'id',
    this.defaultCity = 'Jakarta',
  });

  AppSettings copyWith({
    ThemeMode? themeMode,
    String? languageCode,
    String? defaultCity,
  }) {
    return AppSettings(
      themeMode: themeMode ?? this.themeMode,
      languageCode: languageCode ?? this.languageCode,
      defaultCity: defaultCity ?? this.defaultCity,
    );
  }
}
