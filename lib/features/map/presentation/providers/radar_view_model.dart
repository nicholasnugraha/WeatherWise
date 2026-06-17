import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../shared/data/datasources/rainviewer_api_service.dart';
import '../../../shared/data/models/rainviewer_model.dart';
import '../../../../core/providers/providers.dart';

enum RadarStatus { idle, loading, success, error }

class RadarState {
  final RadarStatus status;
  final RainViewerResponse? radarData;
  final int currentFrameIndex;
  final String? errorMessage;

  const RadarState({
    this.status = RadarStatus.idle,
    this.radarData,
    this.currentFrameIndex = 0,
    this.errorMessage,
  });

  RadarState copyWith({
    RadarStatus? status,
    RainViewerResponse? radarData,
    int? currentFrameIndex,
    String? errorMessage,
  }) {
    return RadarState(
      status: status ?? this.status,
      radarData: radarData ?? this.radarData,
      currentFrameIndex: currentFrameIndex ?? this.currentFrameIndex,
      errorMessage: errorMessage ?? this.errorMessage,
    );
  }
}

class RadarViewModel extends StateNotifier<RadarState> {
  final Ref _ref;

  RadarViewModel(this._ref) : super(const RadarState());

  Future<void> loadRadarData() async {
    state = state.copyWith(status: RadarStatus.loading);
    try {
      final api = _ref.read(rainViewerApiServiceProvider);
      final data = await api.getRadarData();
      state = state.copyWith(
        status: RadarStatus.success,
        radarData: data,
        currentFrameIndex: (data.radar.pastFrames.length - 1).clamp(0, 999),
      );
    } catch (e) {
      state = state.copyWith(
        status: RadarStatus.error,
        errorMessage: e.toString(),
      );
    }
  }

  void setFrameIndex(int index) {
    if (state.radarData != null) {
      final maxIndex = state.radarData!.radar.pastFrames.length - 1;
      state = state.copyWith(
        currentFrameIndex: index.clamp(0, maxIndex),
      );
    }
  }

  void nextFrame() {
    if (state.radarData != null) {
      final maxIndex = state.radarData!.radar.pastFrames.length - 1;
      final next = (state.currentFrameIndex + 1).clamp(0, maxIndex);
      state = state.copyWith(currentFrameIndex: next);
    }
  }

  void previousFrame() {
    final prev = (state.currentFrameIndex - 1).clamp(0, 999);
    state = state.copyWith(currentFrameIndex: prev);
  }

  String? getCurrentTileUrl() {
    if (state.radarData == null) return null;
    final frames = state.radarData!.radar.pastFrames;
    if (state.currentFrameIndex >= frames.length) return null;
    final frame = frames[state.currentFrameIndex];
    return 'https://tilecache.rainviewer.com/v2/radar/${frame.path}/256/{z}/{x}/{y}/2/1_1.png';
  }
}

final radarViewModelProvider =
    StateNotifierProvider<RadarViewModel, RadarState>((ref) {
  return RadarViewModel(ref);
});
