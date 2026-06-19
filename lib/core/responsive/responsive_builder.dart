import 'package:flutter/widgets.dart';

import 'breakpoints.dart';

/// Layout values for the three breakpoints. Use [ResponsiveLayout] when a
/// widget needs different values at different widths; use [ResponsiveBuilder]
/// when the entire widget tree differs per breakpoint (e.g. mobile BottomNav
/// vs desktop NavigationRail).
class ResponsiveLayout<T> {
  const ResponsiveLayout({
    required this.mobile,
    required this.tablet,
    required this.desktop,
  });

  final T mobile;
  final T tablet;
  final T desktop;

  T resolve(double width) {
    if (Breakpoints.isDesktop(width)) return desktop;
    if (Breakpoints.isTablet(width)) return tablet;
    return mobile;
  }
}

/// Returns one of three child widgets based on the current screen width.
class ResponsiveBuilder extends StatelessWidget {
  const ResponsiveBuilder({
    super.key,
    required this.mobile,
    required this.tablet,
    required this.desktop,
  });

  final WidgetBuilder mobile;
  final WidgetBuilder tablet;
  final WidgetBuilder desktop;

  @override
  Widget build(BuildContext context) {
    return LayoutBuilder(
      builder: (context, constraints) {
        final width = constraints.maxWidth;
        if (Breakpoints.isDesktop(width)) return desktop(context);
        if (Breakpoints.isTablet(width)) return tablet(context);
        return mobile(context);
      },
    );
  }
}