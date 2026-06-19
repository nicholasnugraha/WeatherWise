import 'package:flutter/widgets.dart';

import '../theme/app_spacing.dart';

/// Device-class breakpoints per `docs/prd/web-app.md` Section 16.3.
class Breakpoints {
  Breakpoints._();

  /// < 600px — phone form factor. Uses BottomNavigationBar.
  static const double mobile = 600;

  /// 600px – 1024px — tablet. Uses compact NavigationRail.
  static const double tablet = 1024;

  /// ≥ 1024px — desktop. Uses full NavigationRail with labels + 7-day sidebar.
  static const double desktop = 1280;

  static bool isMobile(double width) => width < mobile;
  static bool isTablet(double width) => width >= mobile && width < desktop;
  static bool isDesktop(double width) => width >= desktop;
}

/// Sidebar widths for desktop layout.
class LayoutSizes {
  LayoutSizes._();

  /// Width of the left navigation rail / sidebar.
  static const double sidebar = 240.0;

  /// Width of the right 7-day forecast sidebar (desktop only).
  static const double forecastSidebar = 320.0;

  /// Width of the compact NavigationRail on tablet.
  static const double compactRail = 80.0;

  /// Center column max width.
  static const double centerMaxWidth = AppSpacing.maxWidth;
}