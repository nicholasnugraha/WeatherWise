import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../domain/entities/app_settings.dart';
import '../../data/repositories/settings_repository.dart';

enum SettingsStatus { idle, loading, loaded }

class SettingsState {
  final SettingsStatus status;
  final AppSettings settings;

  const SettingsState({
    this.status = SettingsStatus.idle,
    this.settings = const AppSettings(),
  });

  SettingsState copyWith({
    SettingsStatus? status,
    AppSettings? settings,
  }) {
    return SettingsState(
      status: status ?? this.status,
      settings: settings ?? this.settings,
    );
  }
}

class SettingsViewModel extends StateNotifier<SettingsState> {
  final SettingsRepository _repository;

  SettingsViewModel(this._repository) : super(const SettingsState());

  Future<void> load() async {
    state = state.copyWith(status: SettingsStatus.loading);
    final settings = await _repository.load();
    state = SettingsState(status: SettingsStatus.loaded, settings: settings);
  }

  Future<void> updateThemeMode(ThemeMode themeMode) async {
    final updated = state.settings.copyWith(themeMode: themeMode);
    state = state.copyWith(settings: updated);
    await _repository.save(updated);
  }

  Future<void> updateLanguageCode(String languageCode) async {
    final updated = state.settings.copyWith(languageCode: languageCode);
    state = state.copyWith(settings: updated);
    await _repository.save(updated);
  }

  Future<void> updateDefaultCity(String defaultCity) async {
    final updated = state.settings.copyWith(defaultCity: defaultCity);
    state = state.copyWith(settings: updated);
    await _repository.save(updated);
  }
}

final settingsRepositoryProvider = Provider<SettingsRepository>((ref) {
  // This provider is overridden in main.dart with the opened repository.
  throw UnimplementedError('SettingsRepository must be overridden');
});

final settingsViewModelProvider =
    StateNotifierProvider<SettingsViewModel, SettingsState>((ref) {
  final repository = ref.watch(settingsRepositoryProvider);
  return SettingsViewModel(repository);
});
