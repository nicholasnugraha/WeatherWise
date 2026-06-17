import '../entities/current_weather.dart';
import '../entities/forecast.dart';
import '../entities/geocoding.dart';

abstract class WeatherRepository {
  Future<CurrentWeather> getCurrentWeatherByCity(String cityName);
  Future<CurrentWeather> getCurrentWeatherByCoord(double lat, double lon);
  /// Returns up to 8 daily entries (derived from One Call 3.0 daily[]).
  Future<Forecast> getForecast(double lat, double lon);
  /// Returns up to 24 hourly entries (derived from One Call 3.0 hourly[]).
  Future<Forecast> getHourlyForecast(double lat, double lon);
  Future<List<Geocoding>> searchCity(String query);
  Future<List<Geocoding>> reverseGeocode(double lat, double lon);
}
