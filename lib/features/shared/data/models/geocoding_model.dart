import 'package:freezed_annotation/freezed_annotation.dart';

part 'geocoding_model.freezed.dart';
part 'geocoding_model.g.dart';

@freezed
class GeocodingModel with _$GeocodingModel {
  const factory GeocodingModel({
    required String name,
    @JsonKey(name: 'local_names') Map<String, String>? localNames,
    required double lat,
    required double lon,
    required String country,
    String? state,
  }) = _GeocodingModel;

  factory GeocodingModel.fromJson(Map<String, dynamic> json) =>
      _$GeocodingModelFromJson(json);
}

extension GeocodingModelExtension on GeocodingModel {
  String get displayName => localNames?['id'] ?? name;
  String get fullDisplayName {
    final stateStr = state != null ? ', $state' : '';
    return '$displayName$stateStr, $country';
  }
}
