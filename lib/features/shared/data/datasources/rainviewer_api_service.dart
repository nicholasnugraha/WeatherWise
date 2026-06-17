import 'package:dio/dio.dart';
import '../models/rainviewer_model.dart';

class RainViewerApiService {
  final Dio _dio;

  RainViewerApiService(this._dio);

  Future<RainViewerResponse> getRadarData() async {
    final response = await _dio.get(
      'https://api.rainviewer.com/public/weather-maps.json',
    );
    return RainViewerResponse.fromJson(response.data);
  }
}
