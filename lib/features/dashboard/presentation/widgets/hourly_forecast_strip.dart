import 'package:flutter/material.dart';

import '../../../../core/theme/app_spacing.dart';
import '../../../shared/domain/entities/forecast.dart';
import '../../../home/presentation/widgets/weather_icon_helper.dart';
import '../../../dashboard/presentation/utils/weather_translation.dart';

/// 24-hour forecast strip for the Dashboard.
///
/// Per Stitch `weatherwise_dashboard_light_mode_consistent_layout`:
///   - Section header "24-HOUR FORECAST" (label-sm, all-caps)
///   - Horizontal scrolling list of time slots
///   - Each slot: time label + weather icon + temperature
///   - First slot: "Sekarang" highlighted (filled background)
///
/// Accepts the hourly forecast entries (from ForecastViewModel.hourly).
class HourlyForecastStrip extends StatelessWidget {
  const HourlyForecastStrip({super.key, required this.entries});

  final List<ForecastEntry> entries;

  @override
  Widget build(BuildContext context) {
    if (entries.isEmpty) {
      return const SizedBox.shrink();
    }

    final scheme = Theme.of(context).colorScheme;

    return Card(
      child: Padding(
        padding: const EdgeInsets.all(AppSpacing.lg),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              'PRAKIRAAN 24 JAM',
              style: Theme.of(context).textTheme.labelSmall?.copyWith(
                    color: scheme.onSurfaceVariant,
                  ),
            ),
            const SizedBox(height: AppSpacing.md),
            SizedBox(
              height: 96,
              child: ListView.separated(
                scrollDirection: Axis.horizontal,
                itemCount: entries.length,
                separatorBuilder: (_, __) =>
                    const SizedBox(width: AppSpacing.sm),
                itemBuilder: (context, i) => _HourlySlot(
                  entry: entries[i],
                  isFirst: i == 0,
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _HourlySlot extends StatelessWidget {
  const _HourlySlot({required this.entry, required this.isFirst});
  final ForecastEntry entry;
  final bool isFirst;

  @override
  Widget build(BuildContext context) {
    final scheme = Theme.of(context).colorScheme;
    final hour = DateTime.fromMillisecondsSinceEpoch(entry.timestamp * 1000).hour;
    final timeLabel = isFirst ? 'Sekarang' : '$hour:00';

    return Container(
      width: 64,
      decoration: BoxDecoration(
        color: isFirst ? scheme.primaryContainer : Colors.transparent,
        borderRadius: BorderRadius.circular(AppSpacing.radiusMd),
      ),
      padding: const EdgeInsets.symmetric(vertical: AppSpacing.sm),
      child: Column(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(
            timeLabel,
            style: Theme.of(context).textTheme.labelSmall?.copyWith(
                  color: isFirst ? scheme.onPrimaryContainer : scheme.onSurfaceVariant,
                  fontWeight: isFirst ? FontWeight.w600 : FontWeight.w500,
                ),
          ),
          Icon(
            WeatherIconHelper.getWeatherIcon(entry.weatherMain, isDay: true),
            color: isFirst ? scheme.onPrimaryContainer : scheme.onSurfaceVariant,
            size: 24,
          ),
          Text(
            '${entry.temp.round()}°',
            style: Theme.of(context).textTheme.titleSmall?.copyWith(
                  color: isFirst ? scheme.onPrimaryContainer : scheme.onSurface,
                  fontWeight: FontWeight.w600,
                ),
          ),
        ],
      ),
    );
  }
}

/// Convenience to translate weatherMain for hourly strip tooltips if needed.
/// Currently unused but kept here for future tooltip / detail views.
String hourlyConditionText(String main) => weatherMainToIndonesian(main);