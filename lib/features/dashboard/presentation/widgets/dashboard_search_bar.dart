import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../core/theme/app_spacing.dart';
import '../../../home/presentation/providers/home_view_model.dart';

/// Pill-shaped city search bar for the Dashboard top bar.
///
/// Per Stitch `weatherwise_dashboard`:
///   - Pill shape (radiusFull)
///   - Leading search icon
///   - On submit: dispatches `loadWeatherByCity` via HomeViewModel
///   - Shows loading spinner while searching
class DashboardSearchBar extends ConsumerStatefulWidget {
  const DashboardSearchBar({super.key});

  @override
  ConsumerState<DashboardSearchBar> createState() => _DashboardSearchBarState();
}

class _DashboardSearchBarState extends ConsumerState<DashboardSearchBar> {
  final _controller = TextEditingController();
  final _focusNode = FocusNode();

  @override
  void dispose() {
    _controller.dispose();
    _focusNode.dispose();
    super.dispose();
  }

  void _submit(String value) {
    final query = value.trim();
    if (query.isEmpty) return;
    ref.read(homeViewModelProvider.notifier).loadWeatherByCity(query);
    _focusNode.unfocus();
  }

  @override
  Widget build(BuildContext context) {
    final state = ref.watch(homeViewModelProvider);
    final isLoading = state.status == HomeStatus.loading;

    return TextField(
      controller: _controller,
      focusNode: _focusNode,
      enabled: !isLoading,
      onSubmitted: _submit,
      textInputAction: TextInputAction.search,
      decoration: InputDecoration(
        hintText: 'Cari kota...',
        prefixIcon: Icon(
          Icons.search,
          color: Theme.of(context).colorScheme.onSurfaceVariant,
        ),
        suffixIcon: isLoading
            ? Padding(
                padding: const EdgeInsets.all(AppSpacing.md),
                child: SizedBox(
                  width: 16,
                  height: 16,
                  child: CircularProgressIndicator(
                    strokeWidth: 2,
                    color: Theme.of(context).colorScheme.primary,
                  ),
                ),
              )
            : null,
      ),
    );
  }
}