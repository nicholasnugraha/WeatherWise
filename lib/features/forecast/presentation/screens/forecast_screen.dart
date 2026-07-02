import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../core/localization/app_localizations.dart';
import '../../../../core/responsive/breakpoints.dart';
import '../../../../core/theme/app_spacing.dart';
import '../../../home/presentation/providers/home_view_model.dart';
import '../../../shared/domain/entities/forecast.dart';
import '../providers/forecast_view_model.dart';
import '../widgets/daily_forecast_card.dart';

/// Prakiraan Cuaca screen — `/forecast` route.
///
/// Shows the 7-day forecast as a featured today card + grid of daily cards,
/// matching Stitch `prakiraan_cuaca_detail`.
///
/// Forecast is loaded for the same city the Dashboard is currently showing
/// (sourced from `HomeViewModel`). Falls back to Jakarta on first navigation
/// if the dashboard hasn't been opened yet in this session.
class ForecastScreen extends ConsumerStatefulWidget {
  const ForecastScreen({super.key});

  @override
  ConsumerState<ForecastScreen> createState() => _ForecastScreenState();
}

class _ForecastScreenState extends ConsumerState<ForecastScreen> {
  // Jakarta as fallback when no dashboard city is loaded yet.
  static const _fallbackLat = -6.2088;
  static const _fallbackLon = 106.8456;

  void _maybeLoad() {
    final homeState = ref.read(homeViewModelProvider);
    final forecastState = ref.read(forecastViewModelProvider);

    // Skip if already loading.
    if (forecastState.status == ForecastStatus.loading) return;

    final lat = homeState.weather?.lat ?? _fallbackLat;
    final lon = homeState.weather?.lon ?? _fallbackLon;

    // If already loaded for the same coordinates, don't reload.
    final loadedForSameCoords =
        forecastState.status == ForecastStatus.success &&
            forecastState.forecast != null &&
            (forecastState.lat == lat && forecastState.lon == lon);

    if (loadedForSameCoords) return;

    ref.read(forecastViewModelProvider.notifier).loadForecast(lat, lon);
  }

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) => _maybeLoad());
  }

  @override
  Widget build(BuildContext context) {
    // If the dashboard city changes after this screen is mounted, refresh.
    ref.listen<HomeState>(homeViewModelProvider, (prev, next) {
      if (prev?.weather?.cityName != next.weather?.cityName &&
          next.weather != null) {
        ref.read(forecastViewModelProvider.notifier).loadForecast(
              next.weather!.lat,
              next.weather!.lon,
            );
      }
    });

    final state = ref.watch(forecastViewModelProvider);
    final homeState = ref.watch(homeViewModelProvider);
    final cityName = homeState.weather?.cityName ?? 'Jakarta';
    final l10n = AppLocalizations.of(context);

    return LayoutBuilder(
      builder: (context, constraints) {
        return RefreshIndicator(
          onRefresh: () async {
            final w = ref.read(homeViewModelProvider).weather;
            final lat = w?.lat ?? _fallbackLat;
            final lon = w?.lon ?? _fallbackLon;
            await ref.read(forecastViewModelProvider.notifier).loadForecast(lat, lon);
          },
          child: SingleChildScrollView(
            physics: const AlwaysScrollableScrollPhysics(),
            padding: const EdgeInsets.all(AppSpacing.lg),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                _PageHeader(cityName: cityName),
                const SizedBox(height: AppSpacing.lg),
                _buildContent(context, state, constraints.maxWidth),
              ],
            ),
          ),
        );
      },
    );
  }

  Widget _buildContent(BuildContext context, ForecastState state, double availableWidth) {
    switch (state.status) {
      case ForecastStatus.idle:
      case ForecastStatus.loading:
        if (state.forecast == null) {
          return const _LoadingState();
        }
        return _ForecastList(entries: state.forecast!.entries, availableWidth: availableWidth);
      case ForecastStatus.error:
        return _ErrorState(
          message: state.errorMessage ?? 'Terjadi kesalahan',
          onRetry: _maybeLoad,
        );
      case ForecastStatus.success:
        return _ForecastList(entries: state.forecast!.entries, availableWidth: availableWidth);
    }
  }
}

class _PageHeader extends StatelessWidget {
  const _PageHeader({required this.cityName});
  final String cityName;

  @override
  Widget build(BuildContext context) {
    final scheme = Theme.of(context).colorScheme;
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          children: [
            Icon(Icons.location_on_outlined, size: 18, color: scheme.onSurfaceVariant),
            const SizedBox(width: AppSpacing.xs),
            Text(
              '$cityName, Indonesia',
              style: TextStyle(
                fontFamily: 'Inter',
                fontSize: 16,
                fontWeight: FontWeight.w400,
                height: 1.5,
                color: scheme.onSurfaceVariant,
              ),
            ),
          ],
        ),
        const SizedBox(height: AppSpacing.sm),
        Text(
          'Prakiraan 7 Hari Ke Depan',
          style: TextStyle(
            fontFamily: 'Inter',
            fontSize: 32,
            fontWeight: FontWeight.w600,
            height: 1.25,
            color: scheme.onSurface,
          ),
        ),
        const SizedBox(height: AppSpacing.xs),
        Text(
          'Detail cuaca harian untuk membantu kamu merencanakan minggu ini dengan lebih baik.',
          style: TextStyle(
            fontFamily: 'Inter',
            fontSize: 16,
            fontWeight: FontWeight.w400,
            height: 1.5,
            color: scheme.onSurfaceVariant,
          ),
        ),
      ],
    );
  }
}

class _ForecastList extends StatelessWidget {
  const _ForecastList({required this.entries, required this.availableWidth});
  final List<ForecastEntry> entries;
  final double availableWidth;

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);
    if (entries.isEmpty) {
      return Padding(
        padding: const EdgeInsets.symmetric(vertical: AppSpacing.xxl),
        child: Center(child: Text(l10n.noForecastData)),
      );
    }

    final nowEpoch = DateTime.now().millisecondsSinceEpoch ~/ 1000;
    final today = entries.first;
    final rest = entries.skip(1).toList();

    final crossAxisCount =
        availableWidth >= Breakpoints.tablet ? 3 : 2;

    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        DailyForecastCard(entry: today, featured: true, nowEpoch: nowEpoch),
        const SizedBox(height: AppSpacing.md),
        GridView.builder(
          shrinkWrap: true,
          physics: const NeverScrollableScrollPhysics(),
          gridDelegate: SliverGridDelegateWithFixedCrossAxisCount(
            crossAxisCount: crossAxisCount,
            crossAxisSpacing: AppSpacing.md,
            mainAxisSpacing: AppSpacing.md,
            // Card has header + big temp + 2x2 metric grid — needs more height.
            childAspectRatio: crossAxisCount == 3 ? 0.78 : 0.72,
          ),
          itemCount: rest.length,
          itemBuilder: (context, i) => DailyForecastCard(
            entry: rest[i],
            nowEpoch: nowEpoch,
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
    final l10n = AppLocalizations.of(context);
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: AppSpacing.xxl),
      child: Column(
        children: [
          CircularProgressIndicator(
            color: Theme.of(context).colorScheme.primary,
          ),
          const SizedBox(height: AppSpacing.md),
          Text(
            l10n.loadingForecast,
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
            style: TextStyle(
              fontFamily: 'Inter',
              fontSize: 16,
              fontWeight: FontWeight.w400,
              height: 1.5,
              color: scheme.onSurface,
            ),
          ),
          const SizedBox(height: AppSpacing.lg),
          TextButton.icon(
            onPressed: onRetry,
            icon: const Icon(Icons.refresh),
            label: Text(AppLocalizations.of(context).retry),
          ),
        ],
      ),
    );
  }
}