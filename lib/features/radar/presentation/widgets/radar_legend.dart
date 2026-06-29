import 'package:flutter/material.dart';

import '../../../../core/theme/app_spacing.dart';

/// Legend panel shown in the top-left of the radar map.
///
/// Shows the precipitation intensity color scale (mm/h) with labels:
/// Ringan / Sedang / Lebat / Sangat Lebat.
/// Color ramp matches [precipitationColor] in open_meteo_precipitation_service.dart.
class RadarLegend extends StatelessWidget {
  const RadarLegend({super.key});

  @override
  Widget build(BuildContext context) {
    final scheme = Theme.of(context).colorScheme;
    return Container(
      padding: const EdgeInsets.all(AppSpacing.md),
      decoration: BoxDecoration(
        color: scheme.surfaceContainerLowest,
        borderRadius: BorderRadius.circular(AppSpacing.radiusMd),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withValues(alpha: 0.10),
            blurRadius: 8,
            offset: const Offset(0, 2),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        mainAxisSize: MainAxisSize.min,
        children: [
          Text(
            'Intensitas Curah Hujan',
            style: TextStyle(
              fontFamily: 'Inter',
              fontSize: 12,
              fontWeight: FontWeight.w600,
              height: 1.333,
              letterSpacing: 0.05 * 12,
              color: scheme.onSurface,
            ),
          ),
          const SizedBox(height: AppSpacing.sm),
          // Gradient bar matching precipitationColor ramp.
          Container(
            width: 200,
            height: 8,
            decoration: BoxDecoration(
              borderRadius: BorderRadius.circular(AppSpacing.radiusFull),
              gradient: const LinearGradient(
                colors: [
                  Color(0x664FC3F7), // light blue (light rain)
                  Color(0x9966BB6A), // green (moderate)
                  Color(0xCCFFA726), // orange (heavy)
                  Color(0xFFE53935), // red (extreme)
                ],
              ),
            ),
          ),
          const SizedBox(height: AppSpacing.xs),
          SizedBox(
            width: 200,
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(
                  'Ringan',
                  style: TextStyle(
                    fontFamily: 'Inter',
                    fontSize: 11,
                    fontWeight: FontWeight.w500,
                    height: 1.333,
                    color: scheme.onSurfaceVariant,
                  ),
                ),
                Text(
                  'Sedang',
                  style: TextStyle(
                    fontFamily: 'Inter',
                    fontSize: 11,
                    fontWeight: FontWeight.w500,
                    height: 1.333,
                    color: scheme.onSurfaceVariant,
                  ),
                ),
                Text(
                  'Lebat',
                  style: TextStyle(
                    fontFamily: 'Inter',
                    fontSize: 11,
                    fontWeight: FontWeight.w500,
                    height: 1.333,
                    color: scheme.onSurfaceVariant,
                  ),
                ),
                Text(
                  'Sngt Lebat',
                  style: TextStyle(
                    fontFamily: 'Inter',
                    fontSize: 11,
                    fontWeight: FontWeight.w500,
                    height: 1.333,
                    color: scheme.onSurfaceVariant,
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
