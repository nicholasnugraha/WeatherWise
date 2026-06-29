import 'package:flutter/material.dart';

import '../../../../core/theme/app_spacing.dart';
import '../utils/metric_formatters.dart';

/// Single metric tile for the dashboard 2×3 grid.
///
/// Per Stitch `weatherwise_dashboard`:
///   - Label: 12px / w500 / uppercase / +0.05em letter-spacing (label-sm)
///   - Value: 28px / w600 (headline-md)
///   - Subtext: 14px / w400 / onSurfaceVariant
class MetricCard extends StatelessWidget {
  const MetricCard({super.key, required this.data});

  final MetricData data;

  @override
  Widget build(BuildContext context) {
    final scheme = Theme.of(context).colorScheme;
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(AppSpacing.lg),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          mainAxisSize: MainAxisSize.min,
          children: [
            Text(
              data.label,
              style: TextStyle(
                    fontFamily: 'Inter',
                    fontSize: 12,
                    fontWeight: FontWeight.w500,
                    height: 1.333,
                    letterSpacing: 0.05 * 12,
                    color: scheme.onSurfaceVariant,
                  ),
            ),
            const SizedBox(height: AppSpacing.sm),
            Text(
              data.value,
              style: TextStyle(
                    fontFamily: 'Inter',
                    fontSize: 24,
                    fontWeight: FontWeight.w600,
                    height: 1.33,
                    color: scheme.onSurface,
                  ),
            ),
            const SizedBox(height: AppSpacing.xs),
            Text(
              data.subtext,
              style: TextStyle(
                    fontFamily: 'Inter',
                    fontSize: 14,
                    fontWeight: FontWeight.w400,
                    height: 1.43,
                    color: scheme.onSurfaceVariant,
                  ),
            ),
          ],
        ),
      ),
    );
  }
}