import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../features/dashboard/presentation/screens/dashboard_screen.dart';
import '../../features/forecast/presentation/screens/forecast_screen.dart';
import '../../features/radar/presentation/screens/radar_screen.dart';
import '../../features/settings/presentation/screens/settings_screen.dart';
import '../widgets/app_shell.dart';
import 'routes.dart';

/// Provides the root [GoRouter] for the web app.
///
/// Kept in a Riverpod provider so screens / tests can override navigation
/// (e.g. for deep-link handling or back-button restoration).
final routerProvider = Provider<GoRouter>((ref) {
  return GoRouter(
    initialLocation: Routes.dashboard,
    debugLogDiagnostics: false,
    routes: [
      // Shell route — wraps every destination with the adaptive app shell
      // (NavigationRail on tablet/desktop, BottomNavigationBar on mobile).
      ShellRoute(
        builder: (context, state, child) => AppShell(
          currentLocation: state.matchedLocation,
          child: child,
        ),
        routes: [
          GoRoute(
            path: Routes.dashboard,
            name: Routes.dashboardName,
            pageBuilder: (context, state) => const NoTransitionPage(
              child: DashboardScreen(),
            ),
          ),
          GoRoute(
            path: Routes.forecast,
            name: Routes.forecastName,
            pageBuilder: (context, state) => const NoTransitionPage(
              child: ForecastScreen(),
            ),
          ),
          GoRoute(
            path: Routes.radar,
            name: Routes.radarName,
            pageBuilder: (context, state) => const NoTransitionPage(
              child: RadarScreen(),
            ),
          ),
          GoRoute(
            path: Routes.settings,
            name: Routes.settingsName,
            pageBuilder: (context, state) => const NoTransitionPage(
              child: SettingsScreen(),
            ),
          ),
        ],
      ),
    ],
    errorBuilder: (context, state) => Scaffold(
      appBar: AppBar(title: const Text('Tidak ditemukan')),
      body: Center(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            const Text('Halaman yang kamu cari tidak ada.'),
            const SizedBox(height: 16),
            FilledButton(
              onPressed: () => context.go(Routes.dashboard),
              child: const Text('Kembali ke Dashboard'),
            ),
          ],
        ),
      ),
    ),
  );
});