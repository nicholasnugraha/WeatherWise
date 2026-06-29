import 'package:flutter/material.dart';

import '../../../../core/theme/app_colors.dart';
import '../../../../core/theme/app_spacing.dart';
import '../../../shared/domain/entities/forecast.dart';
import '../../../home/presentation/widgets/weather_icon_helper.dart';
import '../../../dashboard/presentation/utils/weather_translation.dart';
import '../../../forecast/presentation/utils/forecast_formatters.dart';

/// 7-day forecast sidebar for the Desktop Dashboard.
///
/// Per Stitch `weatherwise_dashboard_light_mode_consistent_layout`:
///   - Header "7-DAY FORECAST" (label-sm, all-caps, with calendar icon)
///   - 7 rows: day label | weather icon | temp + small range bar
///   - First row ("Hari Ini") highlighted
///
/// This widget is desktop-only — wrap it in a ResponsiveBuilder.
class ForecastSidebar extends StatelessWidget {
  const ForecastSidebar({super.key, required this.entries});

  final List<ForecastEntry> entries;

  @override
  Widget build(BuildContext context) {
    final scheme = Theme.of(context).colorScheme;

    return Container(
      decoration: BoxDecoration(
        color: scheme.surfaceContainerLow,
        borderRadius: BorderRadius.circular(AppSpacing.radiusLg),
        border: Border.all(
          color: scheme.outlineVariant.withValues(alpha: 0.10),
        ),
      ),
      padding: const EdgeInsets.all(AppSpacing.lg),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Icon(Icons.calendar_today_outlined,
                  size: 16, color: scheme.onSurfaceVariant),
              const SizedBox(width: AppSpacing.xs),
              Text(
                'PRAKIRAAN 7 HARI',
                style: TextStyle(
                      fontFamily: 'Inter',
                      fontSize: 12,
                      fontWeight: FontWeight.w500,
                      height: 1.333,
                      letterSpacing: 0.05 * 12,
                      color: scheme.onSurfaceVariant,
                    ),
              ),
            ],
          ),
          const SizedBox(height: AppSpacing.md),
          ...entries.take(7).toList().asMap().entries.map(
                (e) => _ForecastSidebarRow(
                  entry: e.value,
                  isFirst: e.key == 0,
                  nowEpoch:
                      DateTime.now().millisecondsSinceEpoch ~/ 1000,
                ),
              ),
        ],
      ),
    );
  }
}

class _ForecastSidebarRow extends StatelessWidget {
  const _ForecastSidebarRow({
    required this.entry,
    required this.isFirst,
    required this.nowEpoch,
  });

  final ForecastEntry entry;
  final bool isFirst;
  final int nowEpoch;

  @override
  Widget build(BuildContext context) {
    final scheme = Theme.of(context).colorScheme;
    final dateLabel = formatForecastDateLabel(entry, nowEpoch: nowEpoch);

    return Padding(
      padding: const EdgeInsets.symmetric(vertical: AppSpacing.sm),
      child: Row(
        children: [
          // Day label
          SizedBox(
            width: 72,
            child: Text(
              dateLabel,
              style: TextStyle(
                    fontFamily: 'Inter',
                    fontSize: 16,
                    fontWeight: isFirst ? FontWeight.w600 : FontWeight.w400,
                    height: 1.5,
                    color: scheme.onSurface,
                  ),
            ),
          ),
          // Weather icon + condition
          Expanded(
            child: Row(
              children: [
                Icon(
                  WeatherIconHelper.getWeatherIcon(entry.weatherMain, isDay: true),
                  color: scheme.onSurfaceVariant,
                  size: 20,
                ),
                const SizedBox(width: AppSpacing.sm),
                Expanded(
                  child: Text(
                    weatherMainToIndonesian(entry.weatherMain),
                    style: TextStyle(
                          fontFamily: 'Inter',
                          fontSize: 14,
                          fontWeight: FontWeight.w400,
                          height: 1.43,
                          color: scheme.onSurfaceVariant,
                        ),
                    overflow: TextOverflow.ellipsis,
                  ),
                ),
              ],
            ),
          ),
          // Temperature range: small "min..max" bar + numbers
          SizedBox(
            width: 100,
            child: _TempRange(
              min: entry.tempMin,
              max: entry.tempMax,
            ),
          ),
        ],
      ),
    );
  }
}

/// Compact "min..max" temperature range with a thin gradient bar underneath.
class _TempRange extends StatelessWidget {
  const _TempRange({required this.min, required this.max});
  final double min;
  final double max;

  @override
  Widget build(BuildContext context) {
    final scheme = Theme.of(context).colorScheme;
    return Row(
      mainAxisAlignment: MainAxisAlignment.end,
      children: [
        Text(
          '${min.round()}°',
          style: TextStyle(
                fontFamily: 'Inter',
                fontSize: 14,
                fontWeight: FontWeight.w400,
                height: 1.43,
                color: scheme.onSurfaceVariant,
              ),
        ),
        const SizedBox(width: AppSpacing.xs),
        Container(
          width: 32,
          height: 4,
          decoration: BoxDecoration(
            borderRadius: BorderRadius.circular(AppSpacing.radiusFull),
            gradient: const LinearGradient(
              colors: [
                AppColors.accentPrecip, // cold
                AppColors.accentSunny,  // hot
              ],
            ),
          ),
        ),
        const SizedBox(width: AppSpacing.xs),
        Text(
          '${max.round()}°',
          style: TextStyle(
                fontFamily: 'Inter',
                fontSize: 14,
                fontWeight: FontWeight.w600,
                height: 1.2,
                color: scheme.onSurface,
              ),
        ),
      ],
    );
  }
}