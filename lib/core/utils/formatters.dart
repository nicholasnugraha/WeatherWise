import 'package:intl/intl.dart';

class Formatters {
  static String temperature(double temp) => '${temp.round()}°C';
  
  static String windSpeed(double speed) => '${speed.toStringAsFixed(1)} m/s';
  
  static String pressure(int hPa) => '$hPa hPa';
  
  static String humidity(int percent) => '$percent%';
  
  static String clouds(int percent) => '$percent%';
  
  static String time(int timestamp, int timezone) {
    final dt = DateTime.fromMillisecondsSinceEpoch(
      (timestamp + timezone) * 1000,
      isUtc: true,
    );
    return DateFormat('HH:mm').format(dt);
  }
  
  static String date(int timestamp) {
    final dt = DateTime.fromMillisecondsSinceEpoch(timestamp * 1000);
    return DateFormat('EEE, d MMM', 'id').format(dt);
  }

  static String dayName(int timestamp) {
    final dt = DateTime.fromMillisecondsSinceEpoch(timestamp * 1000);
    final names = ['Min', 'Sen', 'Sel', 'Rab', 'Kam', 'Jum', 'Sab'];
    return names[dt.weekday % 7];
  }
}
