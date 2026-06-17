import 'package:flutter/material.dart';
import '../../../shared/domain/entities/forecast.dart';
import '../../../home/presentation/widgets/weather_icon_helper.dart';

class DailyForecast extends StatelessWidget {
  final List<ForecastEntry> entries;

  const DailyForecast({super.key, required this.entries});

  @override
  Widget build(BuildContext context) {
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
              'Prakiraan 8 Hari',
              style: theme.textTheme.titleMedium?.copyWith(
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 12),
            ...entries.take(8).map((entry) => _DailyItem(
                  date: _formatDay(entry.timestamp),
                  description: entry.weatherDescription,
                  icon: WeatherIconHelper.getWeatherIcon(entry.weatherMain),
                  color: WeatherIconHelper.getWeatherColor(entry.weatherMain),
                  tempMax: entry.tempMax.round(),
                  tempMin: entry.tempMin.round(),
                  rainProb: entry.rainProbability.round(),
                )),
          ],
        ),
      ),
    );
  }

  String _formatDay(int unixSeconds) {
    final dt = DateTime.fromMillisecondsSinceEpoch(unixSeconds * 1000);
    final now = DateTime.now();
    final today = DateTime(now.year, now.month, now.day);
    final that = DateTime(dt.year, dt.month, dt.day);
    final diff = that.difference(today).inDays;
    const weekdays = ['Min', 'Sen', 'Sel', 'Rab', 'Kam', 'Jum', 'Sab'];
    if (diff == 0) return 'Hari ini';
    if (diff == 1) return 'Besok';
    return weekdays[dt.weekday % 7];
  }
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
            width: 64,
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
