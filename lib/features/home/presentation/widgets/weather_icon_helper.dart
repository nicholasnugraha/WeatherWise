import 'package:flutter/material.dart';

class WeatherIconHelper {
  static IconData getWeatherIcon(String weatherMain, {bool isDay = true}) {
    switch (weatherMain.toLowerCase()) {
      case 'clear':
        return isDay ? Icons.wb_sunny : Icons.nightlight_round;
      case 'clouds':
        return Icons.cloud;
      case 'rain':
      case 'drizzle':
        return Icons.water_drop;
      case 'thunderstorm':
        return Icons.flash_on;
      case 'snow':
        return Icons.ac_unit;
      case 'mist':
      case 'fog':
      case 'haze':
      case 'smoke':
        return Icons.blur_on;
      default:
        return Icons.cloud;
    }
  }

  static Color getWeatherColor(String weatherMain) {
    switch (weatherMain.toLowerCase()) {
      case 'clear':
        return Colors.amber;
      case 'clouds':
        return Colors.blueGrey;
      case 'rain':
      case 'drizzle':
        return Colors.blue;
      case 'thunderstorm':
        return Colors.deepPurple;
      case 'snow':
        return Colors.lightBlue;
      default:
        return Colors.blue;
    }
  }
}
