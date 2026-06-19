/// Route name and path constants. Use these instead of raw strings so that
/// `go_router` calls can be statically verified.
class Routes {
  Routes._();

  // Path segments
  static const String dashboard = '/';
  static const String forecast = '/forecast';
  static const String radar = '/radar';
  static const String settings = '/settings';

  // Route names (used by GoRouter.goNamed)
  static const String dashboardName = 'dashboard';
  static const String forecastName = 'forecast';
  static const String radarName = 'radar';
  static const String settingsName = 'settings';
}