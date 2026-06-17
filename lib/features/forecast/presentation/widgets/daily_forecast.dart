import 'package:flutter/material.dart';
import '../../../shared/domain/entities/forecast.dart';
import '../../../home/presentation/widgets/weather_icon_helper.dart';

class DailyForecast extends StatelessWidget {
  final List<ForecastEntry> entries;

  const DailyForecast({super.key, required this.entries});

  @override
  Widget build(BuildContext context) {
    final dailyData = _groupByDay(entries);
    final theme = Theme.of(context);

    return Card(
      elevation: 2,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              'Prakiraan 5 Hari',
              style: theme.textTheme.titleMedium?.copyWith(
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 12),
            ...dailyData.map((entry) => _DailyItem(
                  date: entry.dayName,
                  description: entry.description,
                  icon: WeatherIconHelper.getWeatherIcon(entry.weatherMain),
                  color: WeatherIconHelper.getWeatherColor(entry.weatherMain),
                  tempMax: entry.tempMax.round(),
                  tempMin: entry.tempMin.round(),
                  rainProb: entry.rainProb.round(),
                )),
          ],
        ),
      ),
    );
  }

  List<_DailyData> _groupByDay(List<ForecastEntry> entries) {
    final Map<String, List<ForecastEntry>> grouped = {};

    for (final entry in entries) {
      final dt = DateTime.fromMillisecondsSinceEpoch(entry.timestamp * 1000);
      final key = '${dt.year}-${dt.month}-${dt.day}';
      grouped.putIfAbsent(key, () => []).add(entry);
    }

    final days = <_DailyData>[];
    final weekdays = ['Sen', 'Sel', 'Rab', 'Kam', 'Jum', 'Sab', 'Min'];

    for (final entry in grouped.entries.take(5)) {
      final items = entry.value;
      final dt = DateTime.fromMillisecondsSinceEpoch(
        items.first.timestamp * 1000,
      );

      final temps = items.map((e) => e.temp).toList();
      final midIndex = items.length ~/ 2;

      days.add(_DailyData(
        dayName: weekdays[dt.weekday - 1],
        tempMax: temps.reduce((a, b) => a > b ? a : b),
        tempMin: temps.reduce((a, b) => a < b ? a : b),
        weatherMain: items[midIndex].weatherMain,
        description: items[midIndex].weatherDescription,
        rainProb: items.map((e) => e.rainProbability).reduce(
              (a, b) => a > b ? a : b,
            ),
      ));
    }

    return days;
  }
}

class _DailyData {
  final String dayName;
  final double tempMax;
  final double tempMin;
  final String weatherMain;
  final String description;
  final double rainProb;

  _DailyData({
    required this.dayName,
    required this.tempMax,
    required this.tempMin,
    required this.weatherMain,
    required this.description,
    required this.rainProb,
  });
}

class _DailyItem extends StatelessWidget {
  final String date;
  final String description;
  final IconData icon;
  final Color color;
  final int tempMax;
  final int tempMin;
  final int rainProb;

  const _DailyItem({
    required this.date,
    required this.description,
    required this.icon,
    required this.color,
    required this.tempMax,
    required this.tempMin,
    required this.rainProb,
  });

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 8),
      child: Row(
        children: [
          SizedBox(
            width: 40,
            child: Text(
              date,
              style: const TextStyle(fontWeight: FontWeight.w600),
            ),
          ),
          Icon(icon, color: color, size: 28),
          const SizedBox(width: 12),
          Expanded(
            child: Text(
              description,
              style: const TextStyle(fontSize: 13),
              maxLines: 1,
              overflow: TextOverflow.ellipsis,
            ),
          ),
          if (rainProb > 20) ...[
            Icon(Icons.water_drop, size: 14, color: Colors.blue.shade300),
            Text(
              ' $rainProb%',
              style: TextStyle(
                fontSize: 12,
                color: Colors.blue.shade300,
              ),
            ),
            const SizedBox(width: 8),
          ],
          Text(
            '$tempMin°',
            style: TextStyle(
              color: Colors.blue.shade300,
              fontSize: 13,
            ),
          ),
          const Text(' / '),
          Text(
            '$tempMax°',
            style: TextStyle(
              color: Colors.red.shade300,
              fontWeight: FontWeight.bold,
              fontSize: 13,
            ),
          ),
        ],
      ),
    );
  }
}
