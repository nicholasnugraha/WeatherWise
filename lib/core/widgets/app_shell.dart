import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

import '../responsive/breakpoints.dart';
import '../responsive/responsive_builder.dart';
import '../routing/routes.dart';
import '../theme/app_spacing.dart';

/// Adaptive app shell — wraps every GoRouter route.
///
/// - Desktop / tablet: NavigationRail on the left
/// - Mobile: BottomNavigationBar at the bottom
///
/// The brand mark and Settings entry live in the NavigationRail (desktop only).
class AppShell extends StatelessWidget {
  const AppShell({
    super.key,
    required this.child,
    required this.currentLocation,
  });

  final Widget child;
  final String currentLocation;

  // ---------------------------------------------------------------------------
  // Navigation destinations (single source of truth — used by both rail + bar)
  // ---------------------------------------------------------------------------

  static const _destinations = <_NavDest>[
    _NavDest(
      label: 'Dashboard',
      icon: Icons.dashboard_outlined,
      selectedIcon: Icons.dashboard,
      route: Routes.dashboard,
    ),
    _NavDest(
      label: 'Prakiraan',
      icon: Icons.cloud_outlined,
      selectedIcon: Icons.cloud,
      route: Routes.forecast,
    ),
    _NavDest(
      label: 'Peta Radar',
      icon: Icons.map_outlined,
      selectedIcon: Icons.map,
      route: Routes.radar,
    ),
  ];

  int get _selectedIndex {
    final i = _destinations.indexWhere((d) => d.route == currentLocation);
    return i < 0 ? 0 : i;
  }

  void _go(BuildContext context, _NavDest dest) {
    if (currentLocation != dest.route) {
      context.go(dest.route);
    }
  }

  // ---------------------------------------------------------------------------
  // Layouts
  // ---------------------------------------------------------------------------

  Widget _desktopSidebar(BuildContext context) {
    final scheme = Theme.of(context).colorScheme;
    return Container(
      width: LayoutSizes.sidebar,
      color: Theme.of(context).navigationRailTheme.backgroundColor,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          const SizedBox(height: AppSpacing.lg),
          // Brand mark
          Padding(
            padding: const EdgeInsets.symmetric(
              horizontal: AppSpacing.lg,
              vertical: AppSpacing.md,
            ),
            child: Row(
              children: [
                Icon(Icons.cloud, color: scheme.primaryContainer, size: 28),
                const SizedBox(width: AppSpacing.sm),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        'WeatherWise',
                        style: TextStyle(
                          fontFamily: 'Inter',
                          fontSize: 18,
                          fontWeight: FontWeight.w600,
                          height: 1.333,
                        ),
                      ),
                      Text(
                        'Global Forecasts',
                        style: TextStyle(
                              fontFamily: 'Inter',
                              fontSize: 12,
                              fontWeight: FontWeight.w500,
                              height: 1.333,
                              letterSpacing: 0.05 * 12,
                              color: scheme.onSurfaceVariant,
                            ),
                      ),
                    ],
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(height: AppSpacing.md),
          // Nav items
          ..._destinations.map(
            (d) => _NavRailItem(
              dest: d,
              selected: currentLocation == d.route,
              onTap: () => _go(context, d),
            ),
          ),
          const Spacer(),
          const Divider(height: 1),
          // Settings at the bottom
          Padding(
            padding: const EdgeInsets.all(AppSpacing.md),
            child: _NavRailItem(
              dest: const _NavDest(
                label: 'Pengaturan',
                icon: Icons.settings_outlined,
                selectedIcon: Icons.settings,
                route: Routes.settings,
              ),
              selected: false,
              onTap: () => context.go(Routes.settings),
            ),
          ),
          const SizedBox(height: AppSpacing.md),
        ],
      ),
    );
  }

  Widget _tabletRail(BuildContext context) {
    return NavigationRail(
      selectedIndex: _selectedIndex,
      onDestinationSelected: (i) => _go(context, _destinations[i]),
      labelType: NavigationRailLabelType.all,
      destinations: _destinations
          .map(
            (d) => NavigationRailDestination(
              icon: Icon(d.icon),
              selectedIcon: Icon(d.selectedIcon),
              label: Text(d.label),
            ),
          )
          .toList(),
    );
  }

  Widget _mobileBottomBar(BuildContext context) {
    return NavigationBar(
      selectedIndex: _selectedIndex,
      onDestinationSelected: (i) => _go(context, _destinations[i]),
      destinations: _destinations
          .map(
            (d) => NavigationDestination(
              icon: Icon(d.icon),
              selectedIcon: Icon(d.selectedIcon),
              label: d.label,
            ),
          )
          .toList(),
    );
  }

  @override
  Widget build(BuildContext context) {
    return ResponsiveBuilder(
      mobile: (_) => Scaffold(
        body: child,
        bottomNavigationBar: _mobileBottomBar(context),
      ),
      tablet: (_) => Scaffold(
        body: Row(
          children: [
            _tabletRail(context),
            const VerticalDivider(width: 1),
            Expanded(child: child),
          ],
        ),
      ),
      desktop: (_) => Scaffold(
        body: Row(
          children: [
            _desktopSidebar(context),
            const VerticalDivider(width: 1),
            Expanded(child: child),
          ],
        ),
      ),
    );
  }
}

class _NavDest {
  const _NavDest({
    required this.label,
    required this.icon,
    required this.selectedIcon,
    required this.route,
  });

  final String label;
  final IconData icon;
  final IconData selectedIcon;
  final String route;
}

class _NavRailItem extends StatelessWidget {
  const _NavRailItem({
    required this.dest,
    required this.selected,
    required this.onTap,
  });

  final _NavDest dest;
  final bool selected;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    final scheme = Theme.of(context).colorScheme;
    return Padding(
      padding: const EdgeInsets.symmetric(
        horizontal: AppSpacing.md,
        vertical: AppSpacing.xs,
      ),
      child: Material(
        color: selected ? scheme.primaryContainer : Colors.transparent,
        borderRadius: BorderRadius.circular(AppSpacing.radiusMd),
        child: InkWell(
          borderRadius: BorderRadius.circular(AppSpacing.radiusMd),
          onTap: onTap,
          child: Padding(
            padding: const EdgeInsets.symmetric(
              horizontal: AppSpacing.md,
              vertical: AppSpacing.md,
            ),
            child: Row(
              children: [
                Icon(
                  selected ? dest.selectedIcon : dest.icon,
                  color: selected ? scheme.onPrimaryContainer : scheme.onSurfaceVariant,
                  size: 22,
                ),
                const SizedBox(width: AppSpacing.md),
                Text(
                  dest.label,
                  style: TextStyle(
                    color: selected ? scheme.onPrimaryContainer : scheme.onSurface,
                    fontWeight: selected ? FontWeight.w600 : FontWeight.w400,
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}