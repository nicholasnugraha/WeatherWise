import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../providers/forecast_view_model.dart';
import '../widgets/hourly_forecast.dart';
import '../widgets/daily_forecast.dart';

class ForecastScreen extends ConsumerStatefulWidget {
  final double lat;
  final double lon;

  const ForecastScreen({
    super.key,
    required this.lat,
    required this.lon,
  });

  @override
  ConsumerState<ForecastScreen> createState() => _ForecastScreenState();
}

class _ForecastScreenState extends ConsumerState<ForecastScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      ref.read(forecastViewModelProvider.notifier).loadForecast(
            widget.lat,
            widget.lon,
          );
    });
  }

  @override
  Widget build(BuildContext context) {
    final state = ref.watch(forecastViewModelProvider);

    return Scaffold(
      appBar: AppBar(
        title: const Text('Prakiraan Cuaca'),
      ),
      body: RefreshIndicator(
        onRefresh: () => ref
            .read(forecastViewModelProvider.notifier)
            .loadForecast(widget.lat, widget.lon),
        child: _buildBody(context, state),
      ),
    );
  }

  Widget _buildBody(BuildContext context, ForecastState state) {
    switch (state.status) {
      case ForecastStatus.loading:
      case ForecastStatus.idle:
        return ListView(
          children: const [
            SizedBox(height: 200),
            Center(child: CircularProgressIndicator()),
          ],
        );
      case ForecastStatus.error:
        return ListView(
          children: [
            const SizedBox(height: 100),
            Center(
              child: Column(
                children: [
                  const Icon(Icons.error_outline, size: 64, color: Colors.red),
                  const SizedBox(height: 16),
                  Text(state.errorMessage ?? 'Terjadi kesalahan'),
                ],
              ),
            ),
          ],
        );
      case ForecastStatus.success:
        if (state.forecast == null) {
          return const Center(child: Text('No data'));
        }
        return ListView(
          padding: const EdgeInsets.all(16),
          children: [
            if (state.hourly != null) ...[
              HourlyForecast(entries: state.hourly!.entries),
              const SizedBox(height: 16),
            ],
            DailyForecast(entries: state.forecast!.entries),
          ],
        );
    }
  }
}
