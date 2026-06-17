import 'package:flutter/material.dart';

class RadarSlider extends StatelessWidget {
  final int frameCount;
  final int currentIndex;
  final ValueChanged<int> onChanged;

  const RadarSlider({
    super.key,
    required this.frameCount,
    required this.currentIndex,
    required this.onChanged,
  });

  @override
  Widget build(BuildContext context) {
    if (frameCount <= 1) return const SizedBox.shrink();

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
      decoration: BoxDecoration(
        color: Theme.of(context).colorScheme.surface,
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.1),
            blurRadius: 4,
            offset: const Offset(0, -2),
          ),
        ],
      ),
      child: Row(
        children: [
          IconButton(
            icon: const Icon(Icons.skip_previous),
            onPressed: currentIndex > 0
                ? () => onChanged(currentIndex - 1)
                : null,
          ),
          Expanded(
            child: Slider(
              value: currentIndex.toDouble(),
              min: 0,
              max: (frameCount - 1).toDouble(),
              divisions: frameCount - 1,
              label: 'Frame ${currentIndex + 1}/$frameCount',
              onChanged: (value) => onChanged(value.round()),
            ),
          ),
          IconButton(
            icon: const Icon(Icons.skip_next),
            onPressed: currentIndex < frameCount - 1
                ? () => onChanged(currentIndex + 1)
                : null,
          ),
        ],
      ),
    );
  }
}
