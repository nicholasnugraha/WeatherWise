import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../shared/domain/entities/forecast.dart';
import '../../../../core/providers/providers.dart';

enum ForecastStatus { idle, loading, success, error }

class ForecastState {
  final ForecastStatus status;
  /// Daily entries (up to 8)
  final Forecast? forecast;
  /// Hourly entries (up to 24)
  final Forecast? hourly;
  final String? errorMessage;

  const ForecastState({
    this.status = ForecastStatus.idle,
    this.forecast,
    this.hourly,
    this.errorMessage,
  });

  ForecastState copyWith({
    ForecastStatus? status,
    Forecast? forecast,
    Forecast? hourly,
    String? errorMessage,
  }) {
    return ForecastState(
      status: status ?? this.status,
      forecast: forecast ?? this.forecast,
      hourly: hourly ?? this.hourly,
      errorMessage: errorMessage ?? this.errorMessage,
    );
  }
}

class ForecastViewModel extends StateNotifier<ForecastState> {
  final Ref _ref;

  ForecastViewModel(this._ref) : super(const ForecastState());

  Future<void> loadForecast(double lat, double lon) async {
    state = state.copyWith(status: ForecastStatus.loading);
    try {
      final repo = _ref.read(weatherRepositoryProvider);
      // Both calls hit the same cached OneCall payload after the first one.
      final forecast = await repo.getForecast(lat, lon);
      final hourly = await repo.getHourlyForecast(lat, lon);
      state = state.copyWith(
        status: ForecastStatus.success,
        forecast: forecast,
        hourly: hourly,
      );
    } catch (e) {
      state = state.copyWith(
        status: ForecastStatus.error,
        errorMessage: e.toString(),
      );
    }
  }
}

final forecastViewModelProvider =
    StateNotifierProvider<ForecastViewModel, ForecastState>((ref) {
  return ForecastViewModel(ref);
});
