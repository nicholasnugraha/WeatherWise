import 'package:flutter/material.dart';

/// Design tokens extracted from Stitch `weatherwise_design_system/DESIGN.md`
/// and `atmosphere/DESIGN.md`. All values match the source spec exactly —
/// do not invent new tokens; add to this file when the design system grows.
class AppColors {
  AppColors._();

  // ---------------------------------------------------------------------------
  // Light Mode (per DESIGN.md "weatherwise_design_system")
  // ---------------------------------------------------------------------------

  static const lightPrimary = Color(0xFF00629D);           // Deep Sky Blue
  static const lightPrimaryContainer = Color(0xFF00A3FF);  // Brand Sky Blue (CTAs, hero)
  static const lightOnPrimary = Color(0xFFFFFFFF);
  static const lightOnPrimaryContainer = Color(0xFF00375A);

  static const lightSecondary = Color(0xFF565F6A);          // Mid gray-blue
  static const lightOnSecondary = Color(0xFFFFFFFF);
  static const lightSecondaryContainer = Color(0xFFDAE3F0);
  static const lightOnSecondaryContainer = Color(0xFF5C6570);

  static const lightTertiary = Color(0xFF904D00);
  static const lightOnTertiary = Color(0xFFFFFFFF);
  static const lightTertiaryContainer = Color(0xFFEB8104); // Sunny accent
  static const lightOnTertiaryContainer = Color(0xFF522900);

  static const lightSurface = Color(0xFFF7F9FF);           // Page background
  static const lightOnSurface = Color(0xFF171C22);         // Primary text
  static const lightOnSurfaceVariant = Color(0xFF3F4852);  // Secondary text

  static const lightSurfaceContainerLowest = Color(0xFFFFFFFF);
  static const lightSurfaceContainerLow = Color(0xFFF0F4FC);
  static const lightSurfaceContainer = Color(0xFFEAEEF6);  // Card background
  static const lightSurfaceContainerHigh = Color(0xFFE4E8F0);
  static const lightSurfaceContainerHighest = Color(0xFFDFE3EA);

  static const lightOutline = Color(0xFF6F7883);
  static const lightOutlineVariant = Color(0xFFBEC7D4);    // Card borders (10% opacity in use)

  static const lightError = Color(0xFFBA1A1A);
  static const lightOnError = Color(0xFFFFFFFF);

  // ---------------------------------------------------------------------------
  // Dark Mode
  // ---------------------------------------------------------------------------

  static const darkPrimary = Color(0xFF98CBFF);            // Inverse primary (lighter blue)
  static const darkPrimaryContainer = Color(0xFF1C252E);   // Charcoal Blue
  static const darkOnPrimary = Color(0xFF00375A);
  static const darkOnPrimaryContainer = Color(0xFF00A3FF); // Bright accent on dark

  static const darkSecondary = Color(0xFFBEC7D3);
  static const darkOnSecondary = Color(0xFF131C25);
  static const darkSecondaryContainer = Color(0xFF3F4852);
  static const darkOnSecondaryContainer = Color(0xFFBEC7D3);

  static const darkTertiary = Color(0xFFFFB77D);
  static const darkOnTertiary = Color(0xFF4D2700);
  static const darkTertiaryContainer = Color(0xFF522900);
  static const darkOnTertiaryContainer = Color(0xFFFFB77D);

  static const darkSurface = Color(0xFF0B1117);             // Deep navy-black
  static const darkOnSurface = Color(0xFFEDF1F9);           // Light gray for text
  static const darkOnSurfaceVariant = Color(0xFFBEC7D4);

  static const darkSurfaceContainerLowest = Color(0xFF0F141A);
  static const darkSurfaceContainerLow = Color(0xFF141A21);
  static const darkSurfaceContainer = Color(0xFF1C252E);   // Card background (dark)
  static const darkSurfaceContainerHigh = Color(0xFF222A33);
  static const darkSurfaceContainerHighest = Color(0xFF28323C);

  static const darkOutline = Color(0xFF8C95A0);
  static const darkOutlineVariant = Color(0xFF3F4852);

  static const darkError = Color(0xFFFFB4AB);
  static const darkOnError = Color(0xFF690005);

  // ---------------------------------------------------------------------------
  // Dynamic Weather Accents (semantic, used sparingly per DESIGN.md)
  // ---------------------------------------------------------------------------

  /// Warm sunny / UV indicator.
  static const accentSunny = Color(0xFFFFB800);

  /// Storm / severe weather.
  static const accentStorm = Color(0xFF7B61FF);

  /// Precipitation / rainfall.
  static const accentPrecip = Color(0xFF00D1FF);
}