import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../features/settings/presentation/providers/settings_view_model.dart';

/// Minimal app localization (ID / EN).
///
/// Only the most visible UI strings are translated. Remaining screens can be
/// expanded incrementally without breaking the API.
class AppLocalizations {
  final String languageCode;

  const AppLocalizations._(this.languageCode);

  static AppLocalizations of(BuildContext context) {
    final widget = Localizations.of<AppLocalizations>(
      context,
      AppLocalizations,
    );
    return widget ?? const AppLocalizations._('id');
  }

  static AppLocalizations fromCode(String code) => AppLocalizations._(code);

  String _get(String id, String en) =>
      languageCode == 'en' ? en : _idMap[id] ?? en;

  static final _idMap = <String, String>{
    'appTitle': 'WeatherWise',
    'navDashboard': 'Dashboard',
    'navForecast': 'Prakiraan',
    'navRadar': 'Peta Radar',
    'navSettings': 'Pengaturan',
    'settingsTitle': 'Pengaturan',
    'settingsTheme': 'Tema',
    'settingsThemeSystem': 'Sistem',
    'settingsThemeLight': 'Terang',
    'settingsThemeDark': 'Gelap',
    'settingsLanguage': 'Bahasa',
    'settingsLanguageIndonesian': 'Bahasa Indonesia',
    'settingsLanguageEnglish': 'English',
    'settingsDefaultCity': 'Kota Default',
    'settingsDefaultCityHint': 'Contoh: Jakarta',
    'settingsSave': 'Simpan',
    'settingsAbout': 'Tentang',
    'settingsVersion': 'Versi',
    'settingsAttribution': 'Data cuaca oleh Open-Meteo dan OpenWeatherMap',
    'searchHint': 'Cari kota...',
    'errorGeneric': 'Terjadi kesalahan',
    'retry': 'Coba Lagi',
    'radarTitle': 'Peta Radar',
    'radarMyLocation': 'Lokasi saya',
    'radarReload': 'Muat ulang data',
    'radarPlay': 'Putar',
    'radarPause': 'Jeda',
    'forecastTitle': 'Prakiraan',
    'noForecastData': 'Tidak ada data prakiraan.',
    'loadingForecast': 'Memuat prakiraan...',
    'notFoundTitle': 'Tidak ditemukan',
    'notFoundMessage': 'Halaman yang kamu cari tidak ada.',
    'backToDashboard': 'Kembali ke Dashboard',
  };

  String get appTitle => _get('appTitle', 'WeatherWise');
  String get navDashboard => _get('navDashboard', 'Dashboard');
  String get navForecast => _get('navForecast', 'Forecast');
  String get navRadar => _get('navRadar', 'Radar Map');
  String get navSettings => _get('navSettings', 'Settings');
  String get settingsTitle => _get('settingsTitle', 'Settings');
  String get settingsTheme => _get('settingsTheme', 'Theme');
  String get settingsThemeSystem => _get('settingsThemeSystem', 'System');
  String get settingsThemeLight => _get('settingsThemeLight', 'Light');
  String get settingsThemeDark => _get('settingsThemeDark', 'Dark');
  String get settingsLanguage => _get('settingsLanguage', 'Language');
  String get settingsLanguageIndonesian =>
      _get('settingsLanguageIndonesian', 'Bahasa Indonesia');
  String get settingsLanguageEnglish =>
      _get('settingsLanguageEnglish', 'English');
  String get settingsDefaultCity => _get('settingsDefaultCity', 'Default City');
  String get settingsDefaultCityHint =>
      _get('settingsDefaultCityHint', 'e.g. Jakarta');
  String get settingsSave => _get('settingsSave', 'Save');
  String get settingsAbout => _get('settingsAbout', 'About');
  String get settingsVersion => _get('settingsVersion', 'Version');
  String get settingsAttribution =>
      _get('settingsAttribution', 'Weather data by Open-Meteo and OpenWeatherMap');
  String get searchHint => _get('searchHint', 'Search city...');
  String get errorGeneric => _get('errorGeneric', 'Something went wrong');
  String get retry => _get('retry', 'Retry');
  String get radarTitle => _get('radarTitle', 'Radar Map');
  String get radarMyLocation => _get('radarMyLocation', 'My location');
  String get radarReload => _get('radarReload', 'Reload data');
  String get radarPlay => _get('radarPlay', 'Play');
  String get radarPause => _get('radarPause', 'Pause');
  String get forecastTitle => _get('forecastTitle', 'Forecast');
  String get noForecastData =>
      _get('noForecastData', 'No forecast data available.');
  String get loadingForecast =>
      _get('loadingForecast', 'Loading forecast...');
  String get notFoundTitle => _get('notFoundTitle', 'Not found');
  String get notFoundMessage =>
      _get('notFoundMessage', 'The page you are looking for does not exist.');
  String get backToDashboard =>
      _get('backToDashboard', 'Back to Dashboard');
}

/// Riverpod provider that rebuilds when the active language changes.
final appLocalizationsProvider = Provider<AppLocalizations>((ref) {
  final languageCode = ref.watch(
    settingsViewModelProvider.select((s) => s.settings.languageCode),
  );
  return AppLocalizations.fromCode(languageCode);
});
