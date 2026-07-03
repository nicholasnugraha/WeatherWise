import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../core/localization/app_localizations.dart';
import '../../../../core/theme/app_spacing.dart';
import '../providers/settings_view_model.dart';

/// Settings screen with 4 sections:
///   - Tema (Light / Dark / System)
///   - Bahasa (ID / EN)
///   - Kota Default
///   - Tentang
class SettingsScreen extends ConsumerStatefulWidget {
  const SettingsScreen({super.key});

  @override
  ConsumerState<SettingsScreen> createState() => _SettingsScreenState();
}

class _SettingsScreenState extends ConsumerState<SettingsScreen> {
  final _cityController = TextEditingController();

  @override
  void dispose() {
    _cityController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final settingsState = ref.watch(settingsViewModelProvider);
    final settings = settingsState.settings;
    final viewModel = ref.read(settingsViewModelProvider.notifier);
    final l10n = AppLocalizations.of(context);
    // Sync text controller with persisted default city only when it is empty
    // (initial load). Avoid overwriting while the user is typing.
    if (_cityController.text.isEmpty) {
      _cityController.text = settings.defaultCity;
    }

    return Scaffold(
      appBar: AppBar(title: Text(l10n.settingsTitle)),
      body: ListView(
        padding: const EdgeInsets.all(AppSpacing.lg),
        children: [
          // Theme
          _SectionTitle(title: l10n.settingsTheme),
          _ChoiceTile<String>(
            value: 'system',
            groupValue: _themeToString(settings.themeMode),
            title: l10n.settingsThemeSystem,
            icon: Icons.brightness_auto,
            onChanged: (value) => viewModel.updateThemeMode(_stringToTheme(value!)),
          ),
          _ChoiceTile<String>(
            value: 'light',
            groupValue: _themeToString(settings.themeMode),
            title: l10n.settingsThemeLight,
            icon: Icons.brightness_7,
            onChanged: (value) => viewModel.updateThemeMode(_stringToTheme(value!)),
          ),
          _ChoiceTile<String>(
            value: 'dark',
            groupValue: _themeToString(settings.themeMode),
            title: l10n.settingsThemeDark,
            icon: Icons.brightness_2,
            onChanged: (value) => viewModel.updateThemeMode(_stringToTheme(value!)),
          ),

          const SizedBox(height: AppSpacing.lg),

          // Language
          _SectionTitle(title: l10n.settingsLanguage),
          _ChoiceTile<String>(
            value: 'id',
            groupValue: settings.languageCode,
            title: l10n.settingsLanguageIndonesian,
            icon: Icons.language,
            onChanged: (value) => viewModel.updateLanguageCode(value!),
          ),
          _ChoiceTile<String>(
            value: 'en',
            groupValue: settings.languageCode,
            title: l10n.settingsLanguageEnglish,
            icon: Icons.language,
            onChanged: (value) => viewModel.updateLanguageCode(value!),
          ),

          const SizedBox(height: AppSpacing.lg),

          // Default City
          _SectionTitle(title: l10n.settingsDefaultCity),
          TextField(
            controller: _cityController,
            decoration: InputDecoration(
              hintText: l10n.settingsDefaultCityHint,
            ),
            onSubmitted: (value) => viewModel.updateDefaultCity(value.trim()),
          ),
          const SizedBox(height: AppSpacing.md),
          FilledButton.icon(
            onPressed: () =>
                viewModel.updateDefaultCity(_cityController.text.trim()),
            icon: const Icon(Icons.save),
            label: Text(l10n.settingsSave),
          ),

          const SizedBox(height: AppSpacing.lg),

          // About
          _SectionTitle(title: l10n.settingsAbout),
          Card(
            child: Padding(
              padding: const EdgeInsets.all(AppSpacing.md),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    'WeatherWise',
                    style: Theme.of(context).textTheme.titleMedium,
                  ),
                  const SizedBox(height: AppSpacing.sm),
                  Text('${l10n.settingsVersion}: 1.0.0'),
                  const SizedBox(height: AppSpacing.sm),
                  Text(l10n.settingsAttribution),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }

  static String _themeToString(ThemeMode mode) {
    return switch (mode) {
      ThemeMode.system => 'system',
      ThemeMode.light => 'light',
      ThemeMode.dark => 'dark',
    };
  }

  static ThemeMode _stringToTheme(String value) {
    return switch (value) {
      'light' => ThemeMode.light,
      'dark' => ThemeMode.dark,
      _ => ThemeMode.system,
    };
  }
}

class _SectionTitle extends StatelessWidget {
  const _SectionTitle({required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(bottom: AppSpacing.sm),
      child: Text(
        title,
        style: Theme.of(context).textTheme.titleMedium?.copyWith(
              fontWeight: FontWeight.w600,
            ),
      ),
    );
  }
}

class _ChoiceTile<T> extends StatelessWidget {
  const _ChoiceTile({
    required this.value,
    required this.groupValue,
    required this.title,
    required this.icon,
    required this.onChanged,
  });

  final T value;
  final T groupValue;
  final String title;
  final IconData icon;
  final ValueChanged<T?> onChanged;

  @override
  Widget build(BuildContext context) {
    return RadioListTile<T>(
      value: value,
      groupValue: groupValue,
      onChanged: onChanged,
      title: Text(title),
      secondary: Icon(icon),
      contentPadding: EdgeInsets.zero,
    );
  }
}
