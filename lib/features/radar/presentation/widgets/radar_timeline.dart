import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../core/localization/app_localizations.dart';
import '../../../../core/theme/app_spacing.dart';
import '../../../map/presentation/providers/radar_view_model.dart';

/// Radar timeline / playback bar pinned to the bottom of the map.
///
/// Shows a play/pause button, a scrubable slider, and time labels for
/// the precipitation forecast frames. Auto-play advances every 500ms.
class RadarTimeline extends ConsumerStatefulWidget {
  const RadarTimeline({
    super.key,
    required this.timestamps,
    required this.currentIndex,
    required this.isPlaying,
  });

  final List<DateTime> timestamps;
  final int currentIndex;
  final bool isPlaying;

  @override
  ConsumerState<RadarTimeline> createState() => _RadarTimelineState();
}

class _RadarTimelineState extends ConsumerState<RadarTimeline> {
  @override
  Widget build(BuildContext context) {
    if (widget.timestamps.isEmpty) return const SizedBox.shrink();

    final scheme = Theme.of(context).colorScheme;
    final currentIndex =
        widget.currentIndex.clamp(0, widget.timestamps.length - 1);

    // Show 5 labels around the current frame: -2, -1, 0, +1, +2.
    final labels = <int>[];
    for (final offset in const [-2, -1, 0, 1, 2]) {
      final i = currentIndex + offset;
      if (i >= 0 && i < widget.timestamps.length) labels.add(i);
    }

    final current = widget.timestamps[currentIndex];
    final now = DateTime.now();
    final isNow = (current.year == now.year &&
        current.month == now.month &&
        current.day == now.day &&
        current.hour == now.hour);

    return Container(
      padding: const EdgeInsets.symmetric(
        horizontal: AppSpacing.md,
        vertical: AppSpacing.sm,
      ),
      decoration: BoxDecoration(
        color: scheme.surfaceContainerLowest,
        borderRadius: BorderRadius.circular(AppSpacing.radiusFull),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withValues(alpha: 0.15),
            blurRadius: 12,
            offset: const Offset(0, 4),
          ),
        ],
      ),
      child: Row(
        children: [
          // Play / Pause button
          // Use a fixed-size circle + GestureDetector instead of IconButton.
          // IconButton without explicit sizing sometimes renders an empty
          // circle on Flutter web, especially after hot reload / resize.
          Tooltip(
            message: widget.isPlaying
                ? AppLocalizations.of(context).radarPause
                : AppLocalizations.of(context).radarPlay,
            child: GestureDetector(
              onTap: () =>
                  ref.read(radarViewModelProvider.notifier).togglePlay(),
              child: Container(
                width: 40,
                height: 40,
                decoration: BoxDecoration(
                  color: scheme.primaryContainer,
                  shape: BoxShape.circle,
                ),
                alignment: Alignment.center,
                child: Icon(
                  widget.isPlaying ? Icons.pause : Icons.play_arrow,
                  color: scheme.onPrimaryContainer,
                  size: 24,
                ),
              ),
            ),
          ),
          const SizedBox(width: AppSpacing.md),
          // Slider + time labels
          Expanded(
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                SliderTheme(
                  data: SliderTheme.of(context).copyWith(
                    trackHeight: 3,
                    overlayShape: SliderComponentShape.noOverlay,
                    thumbShape:
                        const RoundSliderThumbShape(enabledThumbRadius: 6),
                  ),
                  child: Slider(
                    value: currentIndex.toDouble(),
                    min: 0,
                    max: (widget.timestamps.length - 1).toDouble(),
                    onChanged: (v) => ref
                        .read(radarViewModelProvider.notifier)
                        .setFrameIndex(v.round()),
                  ),
                ),
                SizedBox(
                  height: 16,
                  child: Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: labels.map((i) {
                      final isCurrent = i == currentIndex;
                      final t = widget.timestamps[i];
                      final hh = t.hour.toString().padLeft(2, '0');
                      final mm = '00';
                      return Text(
                        '$hh:$mm',
                        style: TextStyle(
                          fontFamily: 'Inter',
                          fontSize: 12,
                          fontWeight:
                              isCurrent ? FontWeight.w700 : FontWeight.w400,
                          height: 1.333,
                          letterSpacing: 0.05 * 12,
                          color: isCurrent
                              ? scheme.primary
                              : scheme.onSurfaceVariant,
                        ),
                      );
                    }).toList(),
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(width: AppSpacing.md),
          // Speed indicator
          Text(
            '1x',
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
    );
  }
}
