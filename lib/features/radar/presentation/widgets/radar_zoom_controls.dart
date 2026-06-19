import 'package:flutter/material.dart';
import 'package:flutter_map/flutter_map.dart';

import '../../../../core/theme/app_spacing.dart';

/// Vertical zoom-in / zoom-out buttons overlaid in the top-right of the map.
///
/// Per Stitch `peta_radar_hujan_fixed_layout`: stacked square buttons
/// with + and - icons. 44x44 touch targets per PRD 16.6.
class RadarZoomControls extends StatelessWidget {
  const RadarZoomControls({super.key, required this.mapController});

  final MapController mapController;

  @override
  Widget build(BuildContext context) {
    final scheme = Theme.of(context).colorScheme;
    return Column(
      mainAxisSize: MainAxisSize.min,
      children: [
        _ZoomButton(
          icon: Icons.add,
          onTap: () {
            final camera = mapController.camera;
            mapController.move(
              camera.center,
              (camera.zoom + 1).clamp(3.0, 18.0),
            );
          },
        ),
        const SizedBox(height: 2),
        _ZoomButton(
          icon: Icons.remove,
          onTap: () {
            final camera = mapController.camera;
            mapController.move(
              camera.center,
              (camera.zoom - 1).clamp(3.0, 18.0),
            );
          },
        ),
      ],
    );
  }
}

class _ZoomButton extends StatelessWidget {
  const _ZoomButton({required this.icon, required this.onTap});
  final IconData icon;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    final scheme = Theme.of(context).colorScheme;
    return Material(
      color: scheme.surfaceContainerLowest,
      borderRadius: BorderRadius.circular(AppSpacing.radiusSm),
      elevation: 2,
      child: InkWell(
        borderRadius: BorderRadius.circular(AppSpacing.radiusSm),
        onTap: onTap,
        child: Container(
          width: 44,
          height: 44,
          alignment: Alignment.center,
          child: Icon(icon, color: scheme.onSurface, size: 22),
        ),
      ),
    );
  }
}