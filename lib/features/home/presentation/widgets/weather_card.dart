import 'package:flutter/material.dart';
import '../../../shared/domain/entities/current_weather.dart';
import 'weather_icon_helper.dart';

class WeatherCard extends StatelessWidget {
  final CurrentWeather weather;

  const WeatherCard({super.key, required this.weather});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final isDay = _isDayTime();

    return Card(
      elevation: 4,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
      child: Padding(
        padding: const EdgeInsets.all(24),
        child: Column(
          children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.center,
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Icon(
                  WeatherIconHelper.getWeatherIcon(weather.weatherMain, isDay: isDay),
                  size: 80,
                  color: WeatherIconHelper.getWeatherColor(weather.weatherMain),
                ),
                const SizedBox(width: 16),
                Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      '${weather.temp.round()}°C',
                      style: theme.textTheme.displayMedium?.copyWith(
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                    Text(
                      'Terasa ${weather.feelsLike.round()}°C',
                      style: theme.textTheme.bodyLarge?.copyWith(
                        color: theme.colorScheme.onSurface.withOpacity(0.6),
                      ),
                    ),
                  ],
                ),
              ],
            ),
            const SizedBox(height: 16),
            Text(
              weather.weatherDescription,
              style: theme.textTheme.titleMedium?.copyWith(
                fontWeight: FontWeight.w500,
              ),
            ),
            const SizedBox(height: 8),
            Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                _MinMaxTemp(
                  icon: Icons.arrow_upward,
                  label: 'Max',
                  temp: weather.tempMax,
                  color: Colors.red,
                ),
                const SizedBox(width: 24),
                _MinMaxTemp(
                  icon: Icons.arrow_downward,
                  label: 'Min',
                  temp: weather.tempMin,
                  color: Colors.blue,
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  bool _isDayTime() {
    final now = DateTime.now().millisecondsSinceEpoch ~/ 1000 + weather.timezone;
    return now > weather.sunrise && now < weather.sunset;
  }
}

class _MinMaxTemp extends StatelessWidget {
  final IconData icon;
  final String label;
  final double temp;
  final Color color;

  const _MinMaxTemp({
    required this.icon,
    required this.label,
    required this.temp,
    required this.color,
  });

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        Icon(icon, size: 16, color: color),
        const SizedBox(width: 4),
        Text(
          '$label ${temp.round()}°C',
          style: TextStyle(color: color, fontWeight: FontWeight.w500),
        ),
      ],
    );
  }
}
