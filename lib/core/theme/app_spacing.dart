/// Spacing, radius, and breakpoint constants from
/// `docs/design/screens/...` and `weatherwise_design_system/DESIGN.md`.
///
/// Base unit is 4px. All spacing/gutter tokens are multiples of this unit.
class AppSpacing {
  AppSpacing._();

  // ---------------------------------------------------------------------------
  // Spacing (4px base unit)
  // ---------------------------------------------------------------------------

  /// Base unit. Use this for tight, sub-gutter spacing (e.g. icon-to-label).
  static const double unit = 4.0;

  /// xs = 1 unit
  static const double xs = 4.0;

  /// sm = 2 units
  static const double sm = 8.0;

  /// md = 4 units (default card internal padding)
  static const double md = 16.0;

  /// lg = 6 units
  static const double lg = 24.0;

  /// xl = 8 units
  static const double xl = 32.0;

  /// xxl = 12 units
  static const double xxl = 48.0;

  // ---------------------------------------------------------------------------
  // Layout-specific tokens
  // ---------------------------------------------------------------------------

  /// Gutter (gap) between cards / columns on mobile.
  static const double gutterMobile = 16.0;

  /// Gutter (gap) between cards / columns on desktop.
  static const double gutterDesktop = 24.0;

  /// Safe margin from screen edge on desktop (left/right of max-width container).
  static const double marginSafe = 24.0;

  /// Max content width on desktop (center column).
  static const double maxWidth = 1200.0;

  // ---------------------------------------------------------------------------
  // Radius (per DESIGN.md "Shapes")
  // ---------------------------------------------------------------------------

  /// sm = 0.5rem
  static const double radiusSm = 8.0;

  /// DEFAULT = 1rem (chips, search bars)
  static const double radiusMd = 16.0;

  /// lg = 1.5rem (cards per DESIGN.md "Data Containers")
  static const double radiusLg = 24.0;

  /// xl = 2rem
  static const double radiusXl = 32.0;

  /// full = pill / stadium shape (buttons, chips)
  static const double radiusFull = 9999.0;
}