import 'package:freezed_annotation/freezed_annotation.dart';

part 'rainviewer_model.freezed.dart';
part 'rainviewer_model.g.dart';

@freezed
class RainViewerResponse with _$RainViewerResponse {
  const factory RainViewerResponse({
    required String version,
    required RadarData radar,
  }) = _RainViewerResponse;

  factory RainViewerResponse.fromJson(Map<String, dynamic> json) =>
      _$RainViewerResponseFromJson(json);
}

@freezed
class RadarData with _$RadarData {
  const factory RadarData({
    // RainViewer API returns `past` and `nowcast` as Lists of RadarFrame objects,
    // not as integers. Map them directly to our frame lists.
    @JsonKey(name: 'past', fromJson: _framesFromJson) required List<RadarFrame> pastFrames,
    @JsonKey(name: 'nowcast', fromJson: _framesFromJson) List<RadarFrame>? nowcastFrames,
  }) = _RadarData;

  factory RadarData.fromJson(Map<String, dynamic> json) =>
      _$RadarDataFromJson(json);
}

List<RadarFrame> _framesFromJson(dynamic data) {
  if (data is List) {
    return data.map((e) => RadarFrame.fromJson(e as Map<String, dynamic>)).toList();
  }
  return [];
}

@freezed
class RadarFrame with _$RadarFrame {
  const factory RadarFrame({
    required int time,
    required String path,
  }) = _RadarFrame;

  factory RadarFrame.fromJson(Map<String, dynamic> json) =>
      _$RadarFrameFromJson(json);
}
