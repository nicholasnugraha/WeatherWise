import 'package:flutter/material.dart';

import '../../../../core/theme/app_spacing.dart';

/// Placeholder radar screen for Milestone 1 (Foundation).
///
/// Milestone 2 will replace this with the flutter_map + RainViewer overlay,
/// legend panel, zoom controls, and timeline slider per Stitch
/// `peta_radar_hujan_fixed_layout`.
class RadarScreen extends StatelessWidget {
  const RadarScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final scheme = Theme.of(context).colorScheme;
    return Scaffold(
      appBar: AppBar(title: const Text('Peta Radar')),
      body: Center(
        child: Padding(
          padding: const EdgeInsets.all(AppSpacing.lg),
          child: Card(
            child: Padding(
              padding: const EdgeInsets.all(AppSpacing.xl),
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  Icon(Icons.map, size: 64, color: scheme.primaryContainer),
                  const SizedBox(height: AppSpacing.lg),
                  Text(
                    'Peta Radar Hujan',
                    style: Theme.of(context).textTheme.headlineLarge,
                  ),
                  const SizedBox(height: AppSpacing.sm),
                  Text(
                    'Map + RainViewer overlay + timeline akan diisi di Milestone 2.',
                    textAlign: TextAlign.center,
                    style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                          color: scheme.onSurfaceVariant,
                        ),
                  ),
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }
}