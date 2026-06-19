import 'package:flutter/material.dart';

import '../../../../core/theme/app_spacing.dart';
import '../../../shared/data/models/rainviewer_model.dart';

/// Legend panel shown in the top-left of the radar map.
///
/// Per Stitch `peta_radar_hujan_fixed_layout`:
///   - Title: "Intensitas Curah Hujan"
///   - Horizontal gradient bar (cold -> warm, matching RainViewer)
///   - Three labels: Ringan / Sedang / Lebat
class RadarLegend extends StatelessWidget {
  const RadarLegend({super.key, required this.radarData});

  /// Provided so the legend can show "terakhir diperbarui X ago" hint
  /// in a future iteration; not currently rendered.
  final RainViewerResponse? radarData;

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
            style: Theme.of(context).textTheme.labelSmall?.copyWith(
                  color: scheme.onSurface,
                  fontWeight: FontWeight.w600,
                ),
          ),
          const SizedBox(height: AppSpacing.sm),
          // Gradient bar matching RainViewer color ramp.
          Container(
            width: 200,
            height: 8,
            decoration: BoxDecoration(
              borderRadius: BorderRadius.circular(AppSpacing.radiusFull),
              gradient: const LinearGradient(
                colors: [
                  Color(0xFF4FC3F7), // Ringan — light blue
                  Color(0xFF66BB6A), // Sedang — green
                  Color(0xFFFFA726), // Lebat — orange
                  Color(0xFFE53935), // Sangat lebat — red
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
                  style: Theme.of(context).textTheme.labelSmall?.copyWith(
                        color: scheme.onSurfaceVariant,
                      ),
                ),
                Text(
                  'Sedang',
                  style: Theme.of(context).textTheme.labelSmall?.copyWith(
                        color: scheme.onSurfaceVariant,
                      ),
                ),
                Text(
                  'Lebat',
                  style: Theme.of(context).textTheme.labelSmall?.copyWith(
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