import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../core/theme/app_spacing.dart';
import '../../../shared/data/models/rainviewer_model.dart';
import '../../../map/presentation/providers/radar_view_model.dart';

/// Radar timeline / playback bar pinned to the bottom of the map.
///
/// Per Stitch `peta_radar_hujan_fixed_layout`:
///   - Pill-shaped white card, centered horizontally
///   - Circular play button on the left
///   - Horizontal slider with time labels under it
///   - Speed indicator (1x) on the right
///   - Current frame label highlighted in primaryContainer color
class RadarTimeline extends ConsumerStatefulWidget {
  const RadarTimeline({super.key, required this.frames});

  final List<RadarFrame> frames;

  @override
  ConsumerState<RadarTimeline> createState() => _RadarTimelineState();
}

class _RadarTimelineState extends ConsumerState<RadarTimeline> {
  bool _isPlaying = false;

  void _togglePlay() {
    setState(() => _isPlaying = !_isPlaying);
    // Auto-play is a nice-to-have; out of scope for first iteration per KISS.
  }

  @override
  Widget build(BuildContext context) {
    if (widget.frames.isEmpty) return const SizedBox.shrink();

    final scheme = Theme.of(context).colorScheme;
    final state = ref.watch(radarViewModelProvider);
    final currentIndex = state.currentFrameIndex.clamp(0, widget.frames.length - 1);

    // Show 5 labels around the current frame: -2, -1, 0, +1, +2.
    final labels = <int>[];
    for (final offset in const [-2, -1, 0, 1, 2]) {
      final i = currentIndex + offset;
      if (i >= 0 && i < widget.frames.length) labels.add(i);
    }

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
          Material(
            color: scheme.primaryContainer,
            shape: const CircleBorder(),
            child: InkWell(
              customBorder: const CircleBorder(),
              onTap: _togglePlay,
              child: SizedBox(
                width: 36,
                height: 36,
                child: Icon(
                  _isPlaying ? Icons.pause : Icons.play_arrow,
                  color: scheme.onPrimaryContainer,
                  size: 20,
                ),
              ),
            ),
          ),
          const SizedBox(width: AppSpacing.md),
          Expanded(
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                SliderTheme(
                  data: SliderTheme.of(context).copyWith(
                    trackHeight: 3,
                    overlayShape: SliderComponentShape.noOverlay,
                    thumbShape: const RoundSliderThumbShape(enabledThumbRadius: 6),
                  ),
                  child: Slider(
                    value: currentIndex.toDouble(),
                    min: 0,
                    max: (widget.frames.length - 1).toDouble(),
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
                      final time = DateTime.fromMillisecondsSinceEpoch(
                        widget.frames[i].time * 1000,
                      );
                      final hh = time.hour.toString().padLeft(2, '0');
                      final mm = time.minute.toString().padLeft(2, '0');
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
                                  ? scheme.primaryContainer
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