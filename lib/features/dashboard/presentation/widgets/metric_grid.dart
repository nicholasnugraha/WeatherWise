import 'package:flutter/material.dart';

import '../../../../core/responsive/breakpoints.dart';
import '../../../../core/theme/app_spacing.dart';
import '../utils/metric_formatters.dart';
import 'metric_card.dart';

/// 2×3 grid of metric tiles for the Dashboard.
///
/// Layout switches by viewport:
///   - Desktop (≥1024): 3 columns (matches Stitch)
///   - Tablet/Mobile:  2 columns (more readable at narrow widths)
class MetricGrid extends StatelessWidget {
  const MetricGrid({super.key, required this.metrics});

  final List<MetricData> metrics;

  @override
  Widget build(BuildContext context) {
    final width = MediaQuery.sizeOf(context).width;
    final crossAxisCount =
        width >= Breakpoints.tablet ? 3 : 2;

    return GridView.builder(
      shrinkWrap: true,
      physics: const NeverScrollableScrollPhysics(),
      gridDelegate: SliverGridDelegateWithFixedCrossAxisCount(
        crossAxisCount: crossAxisCount,
        crossAxisSpacing: AppSpacing.md,
        mainAxisSpacing: AppSpacing.md,
        // Aspect ratio tuned so cards have enough room for label + value + subtext
        // without becoming too tall. 16:11 ≈ 1.45 fits the 3-up layout nicely.
        childAspectRatio: crossAxisCount == 3 ? 1.45 : 1.25,
      ),
      itemCount: metrics.length,
      itemBuilder: (context, i) => MetricCard(data: metrics[i]),
    );
  }
}