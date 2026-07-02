import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../features/dashboard/presentation/screens/dashboard_screen.dart';
import '../../features/forecast/presentation/screens/forecast_screen.dart';
import '../../features/radar/presentation/screens/radar_screen.dart';
import '../../features/settings/presentation/screens/settings_screen.dart';
import '../localization/app_localizations.dart';
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
    errorBuilder: (context, state) {
      final l10n = AppLocalizations.of(context);
      return Scaffold(
        appBar: AppBar(title: Text(l10n.notFoundTitle)),
        body: Center(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Text(l10n.notFoundMessage),
              const SizedBox(height: 16),
              FilledButton(
                onPressed: () => context.go(Routes.dashboard),
                child: Text(l10n.backToDashboard),
              ),
            ],
          ),
        ),
      );
    },
  );
});