import 'package:flutter/material.dart';
import '../../../shared/domain/entities/forecast.dart';
import '../../../home/presentation/widgets/weather_icon_helper.dart';

class HourlyForecast extends StatelessWidget {
  final List<ForecastEntry> entries;

  const HourlyForecast({super.key, required this.entries});

  @override
  Widget build(BuildContext context) {
    // Take first 24 entries (8 per day, 3 days, but show 24 hours = 8 entries)
    final hourlyData = entries.take(8).toList();
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
              'Prakiraan Per Jam',
              style: theme.textTheme.titleMedium?.copyWith(
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 12),
            SizedBox(
              height: 120,
              child: ListView.separated(
                scrollDirection: Axis.horizontal,
                itemCount: hourlyData.length,
                separatorBuilder: (_, __) => const SizedBox(width: 8),
                itemBuilder: (context, index) {
                  final entry = hourlyData[index];
                  final time = DateTime.fromMillisecondsSinceEpoch(
                    entry.timestamp * 1000,
                  );
                  return _HourlyItem(
                    time: '${time.hour.toString().padLeft(2, '0')}:00',
                    temp: entry.temp.round(),
                    icon: WeatherIconHelper.getWeatherIcon(entry.weatherMain),
                    color: WeatherIconHelper.getWeatherColor(entry.weatherMain),
                    isFirst: index == 0,
                  );
                },
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _HourlyItem extends StatelessWidget {
  final String time;
  final int temp;
  final IconData icon;
  final Color color;
  final bool isFirst;

  const _HourlyItem({
    required this.time,
    required this.temp,
    required this.icon,
    required this.color,
    this.isFirst = false,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      width: 64,
      padding: const EdgeInsets.symmetric(vertical: 8),
      decoration: BoxDecoration(
        color: isFirst
            ? Theme.of(context).colorScheme.primary.withOpacity(0.1)
            : null,
        borderRadius: BorderRadius.circular(12),
      ),
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Text(
            isFirst ? 'Now' : time,
            style: TextStyle(
              fontSize: 12,
              fontWeight: isFirst ? FontWeight.bold : FontWeight.normal,
            ),
          ),
          const SizedBox(height: 8),
          Icon(icon, color: color, size: 28),
          const SizedBox(height: 8),
          Text(
            '$temp°',
            style: const TextStyle(
              fontWeight: FontWeight.bold,
              fontSize: 16,
            ),
          ),
        ],
      ),
    );
  }
}
