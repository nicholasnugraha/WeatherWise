import 'package:freezed_annotation/freezed_annotation.dart';

part 'geocoding.freezed.dart';

@freezed
class Geocoding with _$Geocoding {
  const factory Geocoding({
    required String name,
    required String displayName,
    required double lat,
    required double lon,
    required String country,
    String? state,
  }) = _Geocoding;
}
