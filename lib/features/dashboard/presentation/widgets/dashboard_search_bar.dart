import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../core/localization/app_localizations.dart';
import '../../../../core/providers/providers.dart';
import '../../../../core/theme/app_spacing.dart';
import '../../../home/presentation/providers/home_view_model.dart';
import '../../../shared/domain/entities/geocoding.dart';

/// Pill-shaped city search bar with autocomplete suggestions.
///
/// Per Stitch `weatherwise_dashboard`:
///   - Pill shape (radiusFull)
///   - Leading search icon
///   - On submit: dispatches `loadWeatherByCity` via HomeViewModel
///   - Shows loading spinner while searching
///
/// Search suggestions:
///   - Debounced (300ms) geocoding lookup via OpenWeatherMap /geo/1.0/direct
///   - Max 5 results, scrollable
///   - Tap a suggestion to load that city's weather
class DashboardSearchBar extends ConsumerStatefulWidget {
  const DashboardSearchBar({super.key});

  @override
  ConsumerState<DashboardSearchBar> createState() => _DashboardSearchBarState();
}

class _DashboardSearchBarState extends ConsumerState<DashboardSearchBar> {
  final _controller = TextEditingController();
  final _focusNode = FocusNode();
  Timer? _debounce;

  List<Geocoding> _suggestions = [];
  bool _isSearching = false;
  bool _showSuggestions = false;

  @override
  void dispose() {
    _controller.dispose();
    _focusNode.dispose();
    _debounce?.cancel();
    super.dispose();
  }

  void _onTextChanged(String value) {
    _debounce?.cancel();
    final query = value.trim();
    if (query.isEmpty) {
      setState(() {
        _suggestions = [];
        _showSuggestions = false;
      });
      return;
    }
    // Debounce: wait 300ms after last keystroke before calling API.
    _debounce = Timer(const Duration(milliseconds: 300), () {
      _fetchSuggestions(query);
    });
  }

  Future<void> _fetchSuggestions(String query) async {
    setState(() => _isSearching = true);
    try {
      final repo = ref.read(weatherRepositoryProvider);
      final results = await repo.searchCity(query);
      if (!mounted) return;
      setState(() {
        _suggestions = results;
        _showSuggestions = results.isNotEmpty;
        _isSearching = false;
      });
    } catch (_) {
      if (!mounted) return;
      setState(() {
        _suggestions = [];
        _showSuggestions = false;
        _isSearching = false;
      });
    }
  }

  void _selectCity(Geocoding city) {
    _controller.text = city.displayName;
    _focusNode.unfocus();
    setState(() {
      _suggestions = [];
      _showSuggestions = false;
    });
    ref.read(homeViewModelProvider.notifier).loadWeatherByCity(city.name);
  }

  void _submit(String value) {
    final query = value.trim();
    if (query.isEmpty) return;
    setState(() {
      _suggestions = [];
      _showSuggestions = false;
    });
    ref.read(homeViewModelProvider.notifier).loadWeatherByCity(query);
    _focusNode.unfocus();
  }

  @override
  Widget build(BuildContext context) {
    final state = ref.watch(homeViewModelProvider);
    final isLoading = state.status == HomeStatus.loading;
    final scheme = Theme.of(context).colorScheme;
    final l10n = AppLocalizations.of(context);

    return Column(
      mainAxisSize: MainAxisSize.min,
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        TextField(
          controller: _controller,
          focusNode: _focusNode,
          enabled: !isLoading,
          onChanged: _onTextChanged,
          onSubmitted: _submit,
          textInputAction: TextInputAction.search,
          decoration: InputDecoration(
            hintText: l10n.searchHint,
            prefixIcon: Icon(
              Icons.search,
              color: scheme.onSurfaceVariant,
            ),
            suffixIcon: isLoading
                ? Padding(
                    padding: const EdgeInsets.all(AppSpacing.md),
                    child: SizedBox(
                      width: 16,
                      height: 16,
                      child: CircularProgressIndicator(
                        strokeWidth: 2,
                        color: scheme.primary,
                      ),
                    ),
                  )
                : _controller.text.isNotEmpty
                    ? IconButton(
                        icon: Icon(Icons.clear, color: scheme.onSurfaceVariant),
                        onPressed: () {
                          _controller.clear();
                          _onTextChanged('');
                        },
                      )
                    : null,
          ),
        ),
        if (_showSuggestions) _buildSuggestions(scheme),
      ],
    );
  }

  Widget _buildSuggestions(ColorScheme scheme) {
    final children = <Widget>[];
    for (int i = 0; i < _suggestions.length; i++) {
      final city = _suggestions[i];
      children.add(
        InkWell(
          onTap: () => _selectCity(city),
          child: Padding(
            padding: const EdgeInsets.symmetric(
              horizontal: AppSpacing.md,
              vertical: AppSpacing.sm + 4,
            ),
            child: Row(
              children: [
                Icon(Icons.location_on,
                    size: 18, color: scheme.onSurfaceVariant),
                const SizedBox(width: AppSpacing.sm),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        city.name,
                        style: TextStyle(
                          fontFamily: 'Inter',
                          fontSize: 14,
                          fontWeight: FontWeight.w500,
                          color: scheme.onSurface,
                        ),
                      ),
                      if (city.state != null || city.country.isNotEmpty)
                        Text(
                          [city.state, city.country]
                              .where((e) => e != null && e.isNotEmpty)
                              .join(', '),
                          style: TextStyle(
                            fontFamily: 'Inter',
                            fontSize: 12,
                            fontWeight: FontWeight.w400,
                            color: scheme.onSurfaceVariant,
                          ),
                        ),
                    ],
                  ),
                ),
                if (_isSearching)
                  SizedBox(
                    width: 14,
                    height: 14,
                    child: CircularProgressIndicator(
                      strokeWidth: 2,
                      color: scheme.primary,
                    ),
                  ),
              ],
            ),
          ),
        ),
      );
      if (i < _suggestions.length - 1) {
        children.add(Divider(height: 1, color: scheme.outlineVariant));
      }
    }

    return Container(
      margin: const EdgeInsets.only(top: 4),
      decoration: BoxDecoration(
        color: scheme.surface,
        borderRadius: BorderRadius.circular(AppSpacing.radiusMd),
        border: Border.all(color: scheme.outlineVariant),
        boxShadow: [
          BoxShadow(
            color: scheme.shadow.withValues(alpha: 0.08),
            blurRadius: 8,
            offset: const Offset(0, 4),
          ),
        ],
      ),
      constraints: const BoxConstraints(maxHeight: 240),
      child: ListView(
        shrinkWrap: true,
        padding: EdgeInsets.zero,
        children: children,
      ),
    );
  }
}