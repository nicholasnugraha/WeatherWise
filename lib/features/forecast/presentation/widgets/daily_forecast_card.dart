import 'package:flutter/material.dart';

import '../../../../core/theme/app_colors.dart';
import '../../../../core/theme/app_spacing.dart';
import '../../../shared/domain/entities/forecast.dart';
import '../../../home/presentation/widgets/weather_icon_helper.dart';
import '../../../dashboard/presentation/utils/weather_translation.dart';
import '../utils/forecast_formatters.dart';

/// Single daily forecast card for the Prakiraan screen.
///
/// Per Stitch `prakiraan_cuaca_detail`:
///   - Regular card: 1-col in a 3-up grid, 4 metrics in a 2×2 grid of pill-boxes
///   - Featured ("today") card: spans 2 cols, 4 metrics in a horizontal strip
///
/// The visual difference is the metric layout + the "Hari Ini" pill badge.
class DailyForecastCard extends StatelessWidget {
  const DailyForecastCard({
    super.key,
    required this.entry,
    this.featured = false,
    this.nowEpoch,
  });

  final ForecastEntry entry;

  /// If true, renders the wider featured layout (used for today).
  final bool featured;

  /// Required when [featured] is true (to compute "Hari Ini" vs date label).
  final int? nowEpoch;

  @override
  Widget build(BuildContext context) {
    // Daily entries aggregate a full day, so the day/night distinction
    // doesn't apply — use the day icon by default.
    final icon = WeatherIconHelper.getWeatherIcon(
      entry.weatherMain,
      isDay: true,
    );
    final condition = weatherMainToIndonesian(entry.weatherMain);
    final dateLabel = formatForecastDateLabel(
      entry,
      nowEpoch: nowEpoch ?? DateTime.now().millisecondsSinceEpoch ~/ 1000,
    );

    final scheme = Theme.of(context).colorScheme;

    return Card(
      child: Padding(
        padding: const EdgeInsets.all(AppSpacing.lg),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Header row: date label + featured badge (left) | condition icon (right)
            Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      if (featured)
                        Container(
                          padding: const EdgeInsets.symmetric(
                            horizontal: AppSpacing.sm,
                            vertical: 2,
                          ),
                          decoration: BoxDecoration(
                            color: scheme.primaryContainer,
                            borderRadius: BorderRadius.circular(AppSpacing.radiusFull),
                          ),
                          child: Text(
                            'HARI INI',
                            style: Theme.of(context).textTheme.labelSmall?.copyWith(
                                  color: scheme.onPrimaryContainer,
                                  fontWeight: FontWeight.w600,
                                ),
                          ),
                        ),
                      if (featured) const SizedBox(height: AppSpacing.xs),
                      Text(
                        dateLabel,
                        style: Theme.of(context).textTheme.titleMedium?.copyWith(
                              fontWeight: FontWeight.w600,
                              color: scheme.onSurface,
                            ),
                      ),
                      const SizedBox(height: AppSpacing.xs),
                      Text(
                        condition,
                        style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                              color: scheme.onSurfaceVariant,
                            ),
                      ),
                    ],
                  ),
                ),
                Icon(
                  icon,
                  color: AppColors.lightPrimaryContainer,
                  size: featured ? 40 : 32,
                ),
              ],
            ),
            const SizedBox(height: AppSpacing.md),
            // Big temperature row.
            Row(
              crossAxisAlignment: CrossAxisAlignment.baseline,
              textBaseline: TextBaseline.alphabetic,
              children: [
                Text(
                  '${entry.tempMax.round()}°',
                  style: TextStyle(
                    color: scheme.primaryContainer,
                    fontSize: featured ? 48 : 32,
                    fontWeight: FontWeight.w700,
                    height: 1.0,
                  ),
                ),
                const SizedBox(width: AppSpacing.xs),
                Text(
                  '/ ${entry.tempMin.round()}°',
                  style: TextStyle(
                    color: scheme.onSurfaceVariant,
                    fontSize: featured ? 20 : 16,
                    fontWeight: FontWeight.w400,
                  ),
                ),
              ],
            ),
            const SizedBox(height: AppSpacing.md),
            if (featured)
              _InlineMetricStrip(entry: entry)
            else
              _MetricPillGrid(entry: entry),
          ],
        ),
      ),
    );
  }
}

/// Featured-card metric layout: 4 metrics in a horizontal strip with dividers.
class _InlineMetricStrip extends StatelessWidget {
  const _InlineMetricStrip({required this.entry});
  final ForecastEntry entry;

  @override
  Widget build(BuildContext context) {
    final scheme = Theme.of(context).colorScheme;
    return DecoratedBox(
      decoration: BoxDecoration(
        border: Border(
          top: BorderSide(color: scheme.outlineVariant.withValues(alpha: 0.5)),
        ),
      ),
      child: Padding(
        padding: const EdgeInsets.only(top: AppSpacing.md),
        child: Row(
          children: [
            _InlineMetricItem(
              icon: Icons.water_drop_outlined,
              label: 'Hujan',
              value: formatRainProbability(entry.rainProbability),
            ),
            _Divider(),
            _InlineMetricItem(
              icon: Icons.opacity,
              label: 'Lembap',
              value: '${entry.humidity}%',
            ),
            _Divider(),
            _InlineMetricItem(
              icon: Icons.air,
              label: 'Angin',
              value: formatWindKmh(entry.windSpeed),
            ),
            _Divider(),
            _InlineMetricItem(
              icon: Icons.wb_sunny_outlined,
              label: 'UV',
              value: uviLabel(entry.uvi),
            ),
          ],
        ),
      ),
    );
  }
}

class _InlineMetricItem extends StatelessWidget {
  const _InlineMetricItem({
    required this.icon,
    required this.label,
    required this.value,
  });
  final IconData icon;
  final String label;
  final String value;

  @override
  Widget build(BuildContext context) {
    final scheme = Theme.of(context).colorScheme;
    return Expanded(
      child: Column(
        children: [
          Icon(icon, size: 18, color: scheme.onSurfaceVariant),
          const SizedBox(height: AppSpacing.xs),
          Text(
            label,
            style: Theme.of(context).textTheme.labelSmall?.copyWith(
                  color: scheme.onSurfaceVariant,
                ),
          ),
          const SizedBox(height: 2),
          Text(
            value,
            style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                  fontWeight: FontWeight.w600,
                  color: scheme.onSurface,
                ),
          ),
        ],
      ),
    );
  }
}

class _Divider extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Container(
      width: 1,
      height: 32,
      color: Theme.of(context).colorScheme.outlineVariant.withValues(alpha: 0.5),
    );
  }
}

/// Regular-card metric layout: 4 metrics in a 2×2 grid of pill-boxes.
class _MetricPillGrid extends StatelessWidget {
  const _MetricPillGrid({required this.entry});
  final ForecastEntry entry;

  @override
  Widget build(BuildContext context) {
    return GridView.count(
      shrinkWrap: true,
      physics: const NeverScrollableScrollPhysics(),
      crossAxisCount: 2,
      mainAxisSpacing: AppSpacing.xs,
      crossAxisSpacing: AppSpacing.xs,
      childAspectRatio: 3.5,
      children: [
        _PillBox(icon: Icons.water_drop_outlined, value: formatRainProbability(entry.rainProbability)),
        _PillBox(icon: Icons.air, value: formatWindKmh(entry.windSpeed)),
        _PillBox(icon: Icons.opacity, value: '${entry.humidity}%'),
        _PillBox(icon: Icons.wb_sunny_outlined, value: uviLabel(entry.uvi)),
      ],
    );
  }
}

class _PillBox extends StatelessWidget {
  const _PillBox({required this.icon, required this.value});
  final IconData icon;
  final String value;

  @override
  Widget build(BuildContext context) {
    final scheme = Theme.of(context).colorScheme;
    return DecoratedBox(
      decoration: BoxDecoration(
        color: scheme.surfaceContainerHigh,
        borderRadius: BorderRadius.circular(AppSpacing.radiusFull),
      ),
      child: Padding(
        padding: const EdgeInsets.symmetric(
          horizontal: AppSpacing.md,
          vertical: AppSpacing.xs,
        ),
        child: Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(icon, size: 14, color: scheme.onSurfaceVariant),
            const SizedBox(width: AppSpacing.xs),
            Flexible(
              child: Text(
                value,
                overflow: TextOverflow.ellipsis,
                style: Theme.of(context).textTheme.labelSmall?.copyWith(
                      color: scheme.onSurface,
                      fontWeight: FontWeight.w500,
                    ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}