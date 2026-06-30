import 'dart:async';

import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../core/providers/providers.dart';
import '../../../shared/data/datasources/open_meteo_precipitation_service.dart';

enum RadarStatus { idle, loading, success, error }

class RadarState {
  final RadarStatus status;
  final PrecipitationGrid? grid;
  final int currentFrameIndex;
  final bool isPlaying;
  final String? errorMessage;

  const RadarState({
    this.status = RadarStatus.idle,
    this.grid,
    this.currentFrameIndex = 0,
    this.isPlaying = false,
    this.errorMessage,
  });

  RadarState copyWith({
    RadarStatus? status,
    PrecipitationGrid? grid,
    int? currentFrameIndex,
    bool? isPlaying,
    String? errorMessage,
  }) {
    return RadarState(
      status: status ?? this.status,
      grid: grid ?? this.grid,
      currentFrameIndex: currentFrameIndex ?? this.currentFrameIndex,
      isPlaying: isPlaying ?? this.isPlaying,
      errorMessage: errorMessage,
    );
  }
}

class RadarViewModel extends StateNotifier<RadarState> {
  final Ref _ref;
  Timer? _playTimer;

  RadarViewModel(this._ref) : super(const RadarState());

  /// Fetch precipitation grid from Open-Meteo around [lat, lon].
  Future<void> loadGrid(double lat, double lon) async {
    state = state.copyWith(status: RadarStatus.loading, errorMessage: null);
    // Stop any running auto-play timer during reload.
    _playTimer?.cancel();
    _playTimer = null;
    try {
      final service = _ref.read(openMeteoPrecipitationServiceProvider);
      final grid = await service.fetchGrid(centerLat: lat, centerLon: lon);
      // Start at the frame closest to current time so the user sees
      // "now" precipitation immediately, not midnight (which is usually 0).
      final initialFrame =
          PrecipitationGrid.findCurrentFrameIndex(grid.timestamps, DateTime.now());
      state = state.copyWith(
        status: RadarStatus.success,
        grid: grid,
        currentFrameIndex: initialFrame,
        isPlaying: false,
      );
    } catch (e) {
      state = state.copyWith(
        status: RadarStatus.error,
        errorMessage: e.toString(),
      );
    }
  }

  void setFrameIndex(int index) {
    if (state.grid == null) return;
    final max = state.grid!.timestamps.length - 1;
    state = state.copyWith(currentFrameIndex: index.clamp(0, max));
  }

  void nextFrame() {
    if (state.grid == null) return;
    final max = state.grid!.timestamps.length - 1;
    final next = state.currentFrameIndex >= max ? 0 : state.currentFrameIndex + 1;
    state = state.copyWith(currentFrameIndex: next);
  }

  void previousFrame() {
    if (state.grid == null) return;
    final maxIdx = state.grid!.timestamps.length - 1;
    final prev = state.currentFrameIndex <= 0 ? maxIdx : state.currentFrameIndex - 1;
    state = state.copyWith(currentFrameIndex: prev);
  }

  /// Toggle auto-play. When playing, advances frame every 500ms and loops.
  void togglePlay() {
    if (state.isPlaying) {
      _playTimer?.cancel();
      _playTimer = null;
      state = state.copyWith(isPlaying: false);
    } else {
      state = state.copyWith(isPlaying: true);
      _playTimer = Timer.periodic(const Duration(milliseconds: 500), (_) {
        nextFrame();
      });
    }
  }

  @override
  void dispose() {
    _playTimer?.cancel();
    super.dispose();
  }
}

final radarViewModelProvider =
    StateNotifierProvider<RadarViewModel, RadarState>((ref) {
  return RadarViewModel(ref);
});
