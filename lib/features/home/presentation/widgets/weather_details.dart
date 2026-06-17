import 'package:flutter/material.dart';
import '../../../shared/domain/entities/current_weather.dart';

class WeatherDetails extends StatelessWidget {
  final CurrentWeather weather;

  const WeatherDetails({super.key, required this.weather});

  @override
  Widget build(BuildContext context) {
    return Card(
      elevation: 2,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              'Detail Cuaca',
              style: Theme.of(context).textTheme.titleMedium?.copyWith(
                    fontWeight: FontWeight.bold,
                  ),
            ),
            const SizedBox(height: 16),
            Row(
              children: [
                Expanded(child: _DetailItem(
                  icon: Icons.water_drop_outlined,
                  label: 'Kelembapan',
                  value: '${weather.humidity}%',
                )),
                Expanded(child: _DetailItem(
                  icon: Icons.air,
                  label: 'Angin',
                  value: '${weather.windSpeed.toStringAsFixed(1)} m/s',
                )),
              ],
            ),
            const SizedBox(height: 12),
            Row(
              children: [
                Expanded(child: _DetailItem(
                  icon: Icons.compress,
                  label: 'Tekanan',
                  value: '${weather.pressure} hPa',
                )),
                Expanded(child: _DetailItem(
                  icon: Icons.cloud_outlined,
                  label: 'Awan',
                  value: '${weather.clouds}%',
                )),
              ],
            ),
            const SizedBox(height: 12),
            Row(
              children: [
                Expanded(child: _DetailItem(
                  icon: Icons.wb_sunny_outlined,
                  label: 'Matahari Terbit',
                  value: _formatTime(weather.sunrise, weather.timezone),
                )),
                Expanded(child: _DetailItem(
                  icon: Icons.nightlight_outlined,
                  label: 'Matahari Terbenam',
                  value: _formatTime(weather.sunset, weather.timezone),
                )),
              ],
            ),
          ],
        ),
      ),
    );
  }

  String _formatTime(int timestamp, int timezone) {
    final dt = DateTime.fromMillisecondsSinceEpoch(
      (timestamp + timezone) * 1000,
      isUtc: true,
    );
    return '${dt.hour.toString().padLeft(2, '0')}:${dt.minute.toString().padLeft(2, '0')}';
  }
}

class _DetailItem extends StatelessWidget {
  final IconData icon;
  final String label;
  final String value;

  const _DetailItem({
    required this.icon,
    required this.label,
    required this.value,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Row(
      children: [
        Icon(icon, size: 24, color: theme.colorScheme.primary),
        const SizedBox(width: 8),
        Expanded(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                label,
                style: theme.textTheme.bodySmall?.copyWith(
                  color: theme.colorScheme.onSurface.withOpacity(0.6),
                ),
              ),
              Text(
                value,
                style: theme.textTheme.bodyMedium?.copyWith(
                  fontWeight: FontWeight.w600,
                ),
              ),
            ],
          ),
        ),
      ],
    );
  }
}
