import 'package:flutter/material.dart';

import 'app_localizations.dart';

/// Custom delegate that provides [AppLocalizations] for the active locale.
///
/// The active locale is driven by the user's language setting in
/// [SettingsViewModel], set on [MaterialApp.locale].
class AppLocalizationsDelegate extends LocalizationsDelegate<AppLocalizations> {
  const AppLocalizationsDelegate();

  @override
  bool isSupported(Locale locale) =>
      <String>['id', 'en'].contains(locale.languageCode);

  @override
  Future<AppLocalizations> load(Locale locale) async {
    return AppLocalizations.fromCode(locale.languageCode);
  }

  @override
  bool shouldReload(covariant LocalizationsDelegate<AppLocalizations> old) =>
      false;
}
