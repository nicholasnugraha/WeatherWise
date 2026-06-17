import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../map/presentation/screens/map_screen.dart';
import '../providers/home_view_model.dart';
import '../widgets/weather_card.dart';
import '../widgets/weather_details.dart';
import '../widgets/search_bar.dart';
import '../widgets/location_button.dart';

class HomeScreen extends ConsumerStatefulWidget {
  const HomeScreen({super.key});

  @override
  ConsumerState<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends ConsumerState<HomeScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      ref.read(homeViewModelProvider.notifier).loadWeatherByLocation();
    });
  }

  @override
  Widget build(BuildContext context) {
    final state = ref.watch(homeViewModelProvider);

    return Scaffold(
      appBar: AppBar(
        title: const Text('WeatherWise'),
        actions: [
          if (state.weather != null)
            IconButton(
              icon: const Icon(Icons.map_outlined),
              tooltip: 'Peta Radar',
              onPressed: () => Navigator.push(
                context,
                MaterialPageRoute(
                  builder: (_) => MapScreen(
                    lat: state.weather!.lat,
                    lon: state.weather!.lon,
                  ),
                ),
              ),
            ),
          LocationButton(
            onPressed: () =>
                ref.read(homeViewModelProvider.notifier).loadWeatherByLocation(),
          ),
        ],
      ),
      body: RefreshIndicator(
        onRefresh: () => ref.read(homeViewModelProvider.notifier).refresh(),
        child: _buildBody(context, state),
      ),
    );
  }

  Widget _buildBody(BuildContext context, HomeState state) {
    switch (state.status) {
      case HomeStatus.loading:
        return ListView(
          children: const [
            SizedBox(height: 200),
            Center(child: CircularProgressIndicator()),
          ],
        );
      case HomeStatus.error:
        return ListView(
          children: [
            const SizedBox(height: 100),
            Center(
              child: Padding(
                padding: const EdgeInsets.all(24),
                child: Column(
                  children: [
                    const Icon(Icons.error_outline, size: 64, color: Colors.red),
                    const SizedBox(height: 16),
                    Text(
                      state.errorMessage ?? 'Terjadi kesalahan',
                      textAlign: TextAlign.center,
                    ),
                    const SizedBox(height: 16),
                    ElevatedButton(
                      onPressed: () =>
                          ref.read(homeViewModelProvider.notifier).refresh(),
                      child: const Text('Coba Lagi'),
                    ),
                  ],
                ),
              ),
            ),
          ],
        );
      case HomeStatus.success:
        if (state.weather == null) {
          return const Center(child: Text('No data'));
        }
        return ListView(
          padding: const EdgeInsets.all(16),
          children: [
            WeatherSearchBar(
              onSearch: (city) =>
                  ref.read(homeViewModelProvider.notifier).loadWeatherByCity(city),
            ),
            const SizedBox(height: 16),
            if (state.location != null) ...[
              Text(
                state.location!.displayName,
                style: Theme.of(context).textTheme.headlineSmall,
                textAlign: TextAlign.center,
              ),
              const SizedBox(height: 8),
            ],
            WeatherCard(weather: state.weather!),
            const SizedBox(height: 16),
            WeatherDetails(weather: state.weather!),
          ],
        );
      case HomeStatus.idle:
        return ListView(
          children: [
            const SizedBox(height: 100),
            Center(
              child: Column(
                children: [
                  const Icon(Icons.wb_sunny, size: 64, color: Colors.amber),
                  const SizedBox(height: 16),
                  const Text('Selamat datang di WeatherWise!'),
                  const SizedBox(height: 16),
                  ElevatedButton.icon(
                    onPressed: () => ref
                        .read(homeViewModelProvider.notifier)
                        .loadWeatherByLocation(),
                    icon: const Icon(Icons.my_location),
                    label: const Text('Gunakan Lokasi Saya'),
                  ),
                ],
              ),
            ),
          ],
        );
    }
  }
}
