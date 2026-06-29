import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../core/responsive/breakpoints.dart';
import '../../../../core/theme/app_spacing.dart';
import '../../../forecast/presentation/providers/forecast_view_model.dart';
import '../../../home/presentation/providers/home_view_model.dart';
import '../utils/metric_formatters.dart';
import '../widgets/dashboard_hero_card.dart';
import '../widgets/dashboard_search_bar.dart';
import '../widgets/forecast_sidebar.dart';
import '../widgets/hourly_forecast_strip.dart';
import '../widgets/metric_grid.dart';

/// Dashboard screen — main landing route `/`.
///
/// Currently shows:
///   - Search bar (pill-shaped, dispatches HomeViewModel.loadWeatherByCity)
///   - Hero card (gradient blue, current weather)
///   - 24-hour forecast strip (horizontal scroll, hourly)
///   - 2x3 metric grid (UV, humidity, wind, visibility, sunrise, precip)
///   - 7-day forecast sidebar (desktop only, right column)
///
/// Forecast (hourly + daily) is sourced from ForecastViewModel and triggered
/// to load on first visit if not yet present. Default initial load: Jakarta.
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
      final homeState = ref.read(homeViewModelProvider);
      // Only auto-load on first visit (idle) — preserve user's last city.
      if (homeState.status == HomeStatus.idle) {
        ref.read(homeViewModelProvider.notifier).loadWeatherByCity(_defaultCity);
      }
      // Forecast loads after home weather resolves (city coords needed).
    });
  }

  void _maybeLoadForecast(double lat, double lon) {
    final forecastState = ref.read(forecastViewModelProvider);
    if (forecastState.status == ForecastStatus.loading) return;
    if (forecastState.status == ForecastStatus.success &&
        forecastState.forecast != null) {
      return;
    }
    ref.read(forecastViewModelProvider.notifier).loadForecast(lat, lon);
  }

  @override
  Widget build(BuildContext context) {
    // When home weather resolves, also trigger forecast load.
    ref.listen<HomeState>(homeViewModelProvider, (prev, next) {
      if (next.weather != null &&
          (prev?.weather?.lat != next.weather!.lat ||
              prev?.weather?.lon != next.weather!.lon)) {
        _maybeLoadForecast(next.weather!.lat, next.weather!.lon);
      }
    });

    final homeState = ref.watch(homeViewModelProvider);
    final forecastState = ref.watch(forecastViewModelProvider);

    // Trigger forecast load if home weather already loaded but forecast missing.
    final w = homeState.weather;
    if (w != null && forecastState.status == ForecastStatus.idle) {
      WidgetsBinding.instance.addPostFrameCallback((_) {
        _maybeLoadForecast(w.lat, w.lon);
      });
    }

    return LayoutBuilder(
      builder: (context, constraints) {
        final isDesktop = constraints.maxWidth >= Breakpoints.desktop;
        return RefreshIndicator(
          onRefresh: () async {
            await ref.read(homeViewModelProvider.notifier).refresh();
            final city = ref.read(homeViewModelProvider).weather;
            if (city != null) {
              await ref.read(forecastViewModelProvider.notifier).loadForecast(
                    city.lat,
                    city.lon,
                  );
            }
          },
          child: _DashboardBody(
            homeState: homeState,
            forecastState: forecastState,
            isDesktop: isDesktop,
            onRetry: () {
              ref.read(homeViewModelProvider.notifier).loadWeatherByCity(_defaultCity);
            },
          ),
        );
      },
    );
  }
}

class _DashboardBody extends StatelessWidget {
  const _DashboardBody({
    required this.homeState,
    required this.forecastState,
    required this.isDesktop,
    required this.onRetry,
  });

  final HomeState homeState;
  final ForecastState forecastState;
  final bool isDesktop;
  final VoidCallback onRetry;

  @override
  Widget build(BuildContext context) {
    final weather = homeState.weather;

    // No data at all → show error or loading state.
    if (weather == null) {
      if (homeState.status == HomeStatus.error) {
        return _ErrorState(
          message: homeState.errorMessage ?? 'Terjadi kesalahan',
          onRetry: onRetry,
        );
      }
      return const _LoadingState();
    }

    // Weather available — show content. If status is error (e.g. refresh
    // failed), show stale data with a small error banner.
    final hasError = homeState.status == HomeStatus.error;

    // Daily entries (7) for sidebar + metric data for grid.
    final dailyEntries =
        forecastState.forecast?.entries ?? const <dynamic>[];
    final hourlyEntries = forecastState.hourly?.entries ?? const <dynamic>[];

    final centerColumn = SingleChildScrollView(
      physics: const AlwaysScrollableScrollPhysics(),
      padding: const EdgeInsets.all(AppSpacing.lg),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          if (hasError)
            _ErrorBanner(
              message: homeState.errorMessage ?? 'Terjadi kesalahan saat memuat data',
            ),
          const DashboardSearchBar(),
          const SizedBox(height: AppSpacing.lg),
          DashboardHeroCard(
            weather: weather,
            location: homeState.location,
          ),
          if (hourlyEntries.isNotEmpty) ...[
            const SizedBox(height: AppSpacing.lg),
            HourlyForecastStrip(entries: hourlyEntries.cast()),
          ],
          const SizedBox(height: AppSpacing.lg),
          MetricGrid(metrics: buildMetricData(weather)),
        ],
      ),
    );

    if (!isDesktop) {
      return centerColumn;
    }

    // Desktop: 3-column layout — center column + 7-day sidebar.
    return Row(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Expanded(child: centerColumn),
        const SizedBox(width: AppSpacing.md),
        SizedBox(
          width: LayoutSizes.forecastSidebar,
          child: SingleChildScrollView(
            padding: const EdgeInsets.fromLTRB(
              0,
              AppSpacing.lg,
              AppSpacing.lg,
              AppSpacing.lg,
            ),
            child: dailyEntries.isEmpty
                ? const SizedBox.shrink()
                : ForecastSidebar(entries: dailyEntries.cast()),
          ),
        ),
      ],
    );
  }
}

class _LoadingState extends StatelessWidget {
  const _LoadingState();

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Padding(
        padding: const EdgeInsets.symmetric(vertical: AppSpacing.xxl),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            CircularProgressIndicator(
              color: Theme.of(context).colorScheme.primary,
            ),
            const SizedBox(height: AppSpacing.md),
            Text(
              'Memuat cuaca...',
              style: TextStyle(
                    fontFamily: 'Inter',
                    fontSize: 16,
                    fontWeight: FontWeight.w400,
                    height: 1.5,
                    color: Theme.of(context).colorScheme.onSurfaceVariant,
                  ),
            ),
          ],
        ),
      ),
    );
  }
}

class _ErrorBanner extends StatelessWidget {
  const _ErrorBanner({required this.message});
  final String message;

  @override
  Widget build(BuildContext context) {
    final scheme = Theme.of(context).colorScheme;
    return Container(
      margin: const EdgeInsets.only(bottom: AppSpacing.md),
      padding: const EdgeInsets.symmetric(
        horizontal: AppSpacing.md,
        vertical: AppSpacing.sm,
      ),
      decoration: BoxDecoration(
        color: scheme.errorContainer,
        borderRadius: BorderRadius.circular(AppSpacing.radiusSm),
      ),
      child: Row(
        children: [
          Icon(Icons.error_outline, size: 18, color: scheme.onErrorContainer),
          const SizedBox(width: AppSpacing.sm),
          Expanded(
            child: Text(
              message,
              style: TextStyle(
                fontFamily: 'Inter',
                fontSize: 14,
                fontWeight: FontWeight.w400,
                height: 1.43,
                color: scheme.onErrorContainer,
              ),
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
    return Center(
      child: Padding(
        padding: const EdgeInsets.symmetric(vertical: AppSpacing.xxl),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(Icons.error_outline, size: 64, color: scheme.error),
            const SizedBox(height: AppSpacing.md),
            Text(
              message,
              textAlign: TextAlign.center,
              style: TextStyle(
                fontFamily: 'Inter',
                fontSize: 16,
                fontWeight: FontWeight.w400,
                height: 1.5,
              ),
            ),
            const SizedBox(height: AppSpacing.lg),
            FilledButton.icon(
              onPressed: onRetry,
              icon: const Icon(Icons.refresh),
              label: const Text('Coba Lagi'),
            ),
          ],
        ),
      ),
    );
  }
}