import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../shared/domain/entities/current_weather.dart';
import '../../../shared/domain/entities/geocoding.dart';
import '../../../../core/providers/providers.dart';

enum HomeStatus { idle, loading, success, error }

class HomeState {
  final HomeStatus status;
  final CurrentWeather? weather;
  final Geocoding? location;
  final String? errorMessage;

  const HomeState({
    this.status = HomeStatus.idle,
    this.weather,
    this.location,
    this.errorMessage,
  });

  HomeState copyWith({
    HomeStatus? status,
    CurrentWeather? weather,
    Geocoding? location,
    String? errorMessage,
  }) {
    return HomeState(
      status: status ?? this.status,
      weather: weather ?? this.weather,
      location: location ?? this.location,
      errorMessage: errorMessage ?? this.errorMessage,
    );
  }
}

class HomeViewModel extends StateNotifier<HomeState> {
  final Ref _ref;

  HomeViewModel(this._ref) : super(const HomeState());

  Future<void> loadWeatherByCity(String cityName) async {
    state = state.copyWith(status: HomeStatus.loading);
    try {
      final repo = _ref.read(weatherRepositoryProvider);
      final weather = await repo.getCurrentWeatherByCity(cityName);
      final locations = await repo.reverseGeocode(weather.lat, weather.lon);
      state = state.copyWith(
        status: HomeStatus.success,
        weather: weather,
        location: locations.isNotEmpty ? locations.first : null,
      );
    } catch (e) {
      state = state.copyWith(
        status: HomeStatus.error,
        errorMessage: e.toString(),
      );
    }
  }

  Future<void> loadWeatherByLocation() async {
    state = state.copyWith(status: HomeStatus.loading);
    try {
      final locationService = _ref.read(locationServiceProvider);
      final position = await locationService.getCurrentPosition();

      if (position == null) {
        state = state.copyWith(
          status: HomeStatus.error,
          errorMessage: 'Gagal mendapatkan lokasi. Pastikan GPS aktif.',
        );
        return;
      }

      final repo = _ref.read(weatherRepositoryProvider);
      final weather = await repo.getCurrentWeatherByCoord(
        position.latitude,
        position.longitude,
      );
      final locations = await repo.reverseGeocode(
        position.latitude,
        position.longitude,
      );

      state = state.copyWith(
        status: HomeStatus.success,
        weather: weather,
        location: locations.isNotEmpty ? locations.first : null,
      );
    } catch (e) {
      state = state.copyWith(
        status: HomeStatus.error,
        errorMessage: e.toString(),
      );
    }
  }

  Future<void> refresh() async {
    if (state.weather != null) {
      await loadWeatherByCity(state.weather!.cityName);
    } else {
      await loadWeatherByCity('Jakarta');
    }
  }
}

final homeViewModelProvider =
    StateNotifierProvider<HomeViewModel, HomeState>((ref) {
  return HomeViewModel(ref);
});
