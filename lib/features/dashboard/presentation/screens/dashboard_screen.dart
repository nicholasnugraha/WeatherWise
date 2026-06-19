import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../core/theme/app_spacing.dart';
import '../../../home/presentation/providers/home_view_model.dart';
import '../utils/metric_formatters.dart';
import '../widgets/dashboard_hero_card.dart';
import '../widgets/dashboard_search_bar.dart';
import '../widgets/metric_grid.dart';

/// Dashboard screen — main landing route `/`.
///
/// Currently shows:
///   - Search bar (pill-shaped, dispatches HomeViewModel.loadWeatherByCity)
///   - Hero card (gradient blue, current weather)
///
/// Future Milestones will add:
///   - 24-hour forecast strip
///   - 2x3 metric grid (UV, Humidity, Wind, Visibility, Sunrise, Precip)
///   - 7-day forecast sidebar (desktop only)
///
/// Default initial load: Jakarta (per PRD open question Q7 default).
/// Avoids the browser geolocation permission flow on first visit.
class DashboardScreen extends ConsumerStatefulWidget {
  const DashboardScreen({super.key});

  @override
  ConsumerState<DashboardScreen> createState() => _DashboardScreenState();
}

class _DashboardScreenState extends ConsumerState<DashboardScreen> {
  static const _defaultCity = 'Jakarta';

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      final state = ref.read(homeViewModelProvider);
      // Only auto-load on first visit (idle) — preserve user's last city
      // across navigations within the session.
      if (state.status == HomeStatus.idle) {
        ref.read(homeViewModelProvider.notifier).loadWeatherByCity(_defaultCity);
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    final state = ref.watch(homeViewModelProvider);

    return Scaffold(
      appBar: AppBar(
        title: const Text('Dashboard'),
        toolbarHeight: 72,
      ),
      body: RefreshIndicator(
        onRefresh: () => ref.read(homeViewModelProvider.notifier).refresh(),
        child: SingleChildScrollView(
          physics: const AlwaysScrollableScrollPhysics(),
          padding: const EdgeInsets.all(AppSpacing.lg),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              const DashboardSearchBar(),
              const SizedBox(height: AppSpacing.lg),
              _buildContent(context, state),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildContent(BuildContext context, HomeState state) {
    switch (state.status) {
      case HomeStatus.idle:
      case HomeStatus.loading:
        if (state.weather == null) {
          return const _LoadingState();
        }
        // If we already have weather but are refreshing, show the hero card
        // (it will re-render when the new data arrives).
        return DashboardHeroCard(
          weather: state.weather!,
          location: state.location,
        );
      case HomeStatus.error:
        return _ErrorState(
          message: state.errorMessage ?? 'Terjadi kesalahan',
          onRetry: () =>
              ref.read(homeViewModelProvider.notifier).loadWeatherByCity(_defaultCity),
        );
      case HomeStatus.success:
        return Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            DashboardHeroCard(
              weather: state.weather!,
              location: state.location,
            ),
            const SizedBox(height: AppSpacing.lg),
            MetricGrid(metrics: buildMetricData(state.weather!)),
          ],
        );
    }
  }
}

class _LoadingState extends StatelessWidget {
  const _LoadingState();

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: AppSpacing.xxl),
      child: Column(
        children: [
          CircularProgressIndicator(
            color: Theme.of(context).colorScheme.primary,
          ),
          const SizedBox(height: AppSpacing.md),
          Text(
            'Memuat cuaca...',
            style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                  color: Theme.of(context).colorScheme.onSurfaceVariant,
                ),
          ),
        ],
      ),
    );
  }
}

class _ErrorState extends StatelessWidget {
  const _ErrorState({required this.message, required this.onRetry});
  final String message;
  final VoidCallback onRetry;

  @override
  Widget build(BuildContext context) {
    final scheme = Theme.of(context).colorScheme;
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: AppSpacing.xxl),
      child: Column(
        children: [
          Icon(Icons.error_outline, size: 64, color: scheme.error),
          const SizedBox(height: AppSpacing.md),
          Text(
            message,
            textAlign: TextAlign.center,
            style: Theme.of(context).textTheme.bodyMedium,
          ),
          const SizedBox(height: AppSpacing.lg),
          FilledButton.icon(
            onPressed: onRetry,
            icon: const Icon(Icons.refresh),
            label: const Text('Coba Lagi'),
          ),
        ],
      ),
    );
  }
}