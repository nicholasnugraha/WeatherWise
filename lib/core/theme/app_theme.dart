import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';

import 'app_colors.dart';
import 'app_spacing.dart';

/// App-wide ThemeData for WeatherWise Web App.
///
/// Tokens sourced from `docs/prd/web-app.md` Section 16 (Design Reference)
/// which mirrors Stitch project `16629827602854843266` (Material 3 Web UI).
class AppTheme {
  AppTheme._();

  /// Inter via google_fonts — the design system mandates Inter exclusively.
  /// Cached as static final to avoid repeated HTTP font loading on web.
  static final String _fontFamily = GoogleFonts.inter().fontFamily ?? 'Inter';

  static TextTheme _buildTextTheme(TextTheme base) {
    // GoogleFonts.interTextTheme() applies Inter across the whole theme
    // while preserving our custom font sizes / weights defined in base.
    return GoogleFonts.interTextTheme(base).copyWith(
      // Hero temperature — display-temp per DESIGN.md: 80px / weight 700 / -0.04em.
      displayLarge: GoogleFonts.inter(
        fontSize: 80,
        fontWeight: FontWeight.w700,
        height: 1.125,    // 90px / 80px
        letterSpacing: -0.04 * 16, // -0.04em converted to logical px
      ),
      // headline-lg / headline-lg-mobile — handled by responsive Builder.
      headlineLarge: GoogleFonts.inter(
        fontSize: 32,
        fontWeight: FontWeight.w600,
        height: 1.25,
      ),
      titleMedium: GoogleFonts.inter(
        fontSize: 18,
        fontWeight: FontWeight.w600,
        height: 1.333,
      ),
      bodyMedium: GoogleFonts.inter(
        fontSize: 16,
        fontWeight: FontWeight.w400,
        height: 1.5,
      ),
      // label-sm — all-caps for technical categories per DESIGN.md.
      labelSmall: GoogleFonts.inter(
        fontSize: 12,
        fontWeight: FontWeight.w500,
        height: 1.333,
        letterSpacing: 0.05 * 12, // 0.05em converted to logical px
      ),
    );
  }

  static ColorScheme _lightScheme() => const ColorScheme(
        brightness: Brightness.light,
        primary: AppColors.lightPrimary,
        onPrimary: AppColors.lightOnPrimary,
        primaryContainer: AppColors.lightPrimaryContainer,
        onPrimaryContainer: AppColors.lightOnPrimaryContainer,
        secondary: AppColors.lightSecondary,
        onSecondary: AppColors.lightOnSecondary,
        secondaryContainer: AppColors.lightSecondaryContainer,
        onSecondaryContainer: AppColors.lightOnSecondaryContainer,
        tertiary: AppColors.lightTertiary,
        onTertiary: AppColors.lightOnTertiary,
        tertiaryContainer: AppColors.lightTertiaryContainer,
        onTertiaryContainer: AppColors.lightOnTertiaryContainer,
        error: AppColors.lightError,
        onError: AppColors.lightOnError,
        surface: AppColors.lightSurface,
        onSurface: AppColors.lightOnSurface,
        onSurfaceVariant: AppColors.lightOnSurfaceVariant,
        surfaceContainerLowest: AppColors.lightSurfaceContainerLowest,
        surfaceContainerLow: AppColors.lightSurfaceContainerLow,
        surfaceContainer: AppColors.lightSurfaceContainer,
        surfaceContainerHigh: AppColors.lightSurfaceContainerHigh,
        surfaceContainerHighest: AppColors.lightSurfaceContainerHighest,
        outline: AppColors.lightOutline,
        outlineVariant: AppColors.lightOutlineVariant,
      );

  static ColorScheme _darkScheme() => const ColorScheme(
        brightness: Brightness.dark,
        primary: AppColors.darkPrimary,
        onPrimary: AppColors.darkOnPrimary,
        primaryContainer: AppColors.darkPrimaryContainer,
        onPrimaryContainer: AppColors.darkOnPrimaryContainer,
        secondary: AppColors.darkSecondary,
        onSecondary: AppColors.darkOnSecondary,
        secondaryContainer: AppColors.darkSecondaryContainer,
        onSecondaryContainer: AppColors.darkOnSecondaryContainer,
        tertiary: AppColors.darkTertiary,
        onTertiary: AppColors.darkOnTertiary,
        tertiaryContainer: AppColors.darkTertiaryContainer,
        onTertiaryContainer: AppColors.darkOnTertiaryContainer,
        error: AppColors.darkError,
        onError: AppColors.darkOnError,
        surface: AppColors.darkSurface,
        onSurface: AppColors.darkOnSurface,
        onSurfaceVariant: AppColors.darkOnSurfaceVariant,
        surfaceContainerLowest: AppColors.darkSurfaceContainerLowest,
        surfaceContainerLow: AppColors.darkSurfaceContainerLow,
        surfaceContainer: AppColors.darkSurfaceContainer,
        surfaceContainerHigh: AppColors.darkSurfaceContainerHigh,
        surfaceContainerHighest: AppColors.darkSurfaceContainerHighest,
        outline: AppColors.darkOutline,
        outlineVariant: AppColors.darkOutlineVariant,
      );

  /// Card border style per DESIGN.md: 1px outlineVariant @ 10% opacity in light mode.
  /// No drop shadow — depth comes from tonal shifts.
  static ShapeBorder _cardBorder(ColorScheme scheme) => RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(AppSpacing.radiusLg),
        side: BorderSide(
          color: scheme.outlineVariant.withValues(alpha: 0.10),
          width: 1,
        ),
      );

  /// Light theme — primary use case for the web app.
  static ThemeData get light {
    final scheme = _lightScheme();
    return ThemeData(
      useMaterial3: true,
      colorScheme: scheme,
      fontFamily: _fontFamily,
      textTheme: _buildTextTheme(scheme.brightness == Brightness.light
          ? ThemeData.light().textTheme
          : ThemeData.dark().textTheme),
      scaffoldBackgroundColor: scheme.surface,
      appBarTheme: AppBarTheme(
        backgroundColor: scheme.surface,
        foregroundColor: scheme.onSurface,
        elevation: 0,
        scrolledUnderElevation: 0,
        centerTitle: false,
      ),
      cardTheme: CardThemeData(
        elevation: 0,
        margin: EdgeInsets.zero,
        color: scheme.surfaceContainerLow,
        shape: _cardBorder(scheme),
      ),
      navigationRailTheme: NavigationRailThemeData(
        backgroundColor: scheme.surfaceContainerLowest,
        indicatorColor: scheme.primaryContainer,
        selectedIconTheme: IconThemeData(color: scheme.onPrimaryContainer),
        unselectedIconTheme: IconThemeData(color: scheme.onSurfaceVariant),
        selectedLabelTextStyle: TextStyle(
          color: scheme.onSurface,
          fontWeight: FontWeight.w600,
        ),
        unselectedLabelTextStyle: TextStyle(color: scheme.onSurfaceVariant),
      ),
      navigationBarTheme: NavigationBarThemeData(
        backgroundColor: scheme.surfaceContainerLowest,
        indicatorColor: scheme.primaryContainer,
        labelTextStyle: WidgetStateProperty.resolveWith(
          (states) => TextStyle(
            color: states.contains(WidgetState.selected)
                ? scheme.onPrimaryContainer
                : scheme.onSurfaceVariant,
            fontWeight: FontWeight.w600,
            fontSize: 12,
          ),
        ),
      ),
      inputDecorationTheme: InputDecorationTheme(
        filled: true,
        fillColor: scheme.surfaceContainerLow,
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(AppSpacing.radiusFull),
          borderSide: BorderSide(color: scheme.outlineVariant),
        ),
        enabledBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(AppSpacing.radiusFull),
          borderSide: BorderSide(color: scheme.outlineVariant),
        ),
        focusedBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(AppSpacing.radiusFull),
          borderSide: BorderSide(color: scheme.primary, width: 1.5),
        ),
        contentPadding: const EdgeInsets.symmetric(
          horizontal: AppSpacing.lg,
          vertical: AppSpacing.md,
        ),
      ),
      filledButtonTheme: FilledButtonThemeData(
        style: FilledButton.styleFrom(
          shape: const StadiumBorder(),
          padding: const EdgeInsets.symmetric(
            horizontal: AppSpacing.lg,
            vertical: AppSpacing.md,
          ),
        ),
      ),
    );
  }

  /// Dark theme — deep navy-black surface, charcoal-blue containers.
  static ThemeData get dark {
    final scheme = _darkScheme();
    return ThemeData(
      useMaterial3: true,
      colorScheme: scheme,
      fontFamily: _fontFamily,
      textTheme: _buildTextTheme(ThemeData.dark().textTheme),
      scaffoldBackgroundColor: scheme.surface,
      appBarTheme: AppBarTheme(
        backgroundColor: scheme.surface,
        foregroundColor: scheme.onSurface,
        elevation: 0,
        scrolledUnderElevation: 0,
        centerTitle: false,
      ),
      cardTheme: CardThemeData(
        elevation: 0,
        margin: EdgeInsets.zero,
        color: scheme.surfaceContainer,
        shape: _cardBorder(scheme),
      ),
      navigationRailTheme: NavigationRailThemeData(
        backgroundColor: scheme.surfaceContainerLow,
        indicatorColor: scheme.primaryContainer,
        selectedIconTheme: IconThemeData(color: scheme.onPrimaryContainer),
        unselectedIconTheme: IconThemeData(color: scheme.onSurfaceVariant),
        selectedLabelTextStyle: TextStyle(
          color: scheme.onSurface,
          fontWeight: FontWeight.w600,
        ),
        unselectedLabelTextStyle: TextStyle(color: scheme.onSurfaceVariant),
      ),
      navigationBarTheme: NavigationBarThemeData(
        backgroundColor: scheme.surfaceContainerLow,
        indicatorColor: scheme.primaryContainer,
        labelTextStyle: WidgetStateProperty.resolveWith(
          (states) => TextStyle(
            color: states.contains(WidgetState.selected)
                ? scheme.onPrimaryContainer
                : scheme.onSurfaceVariant,
            fontWeight: FontWeight.w600,
            fontSize: 12,
          ),
        ),
      ),
      inputDecorationTheme: InputDecorationTheme(
        filled: true,
        fillColor: scheme.surfaceContainer,
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(AppSpacing.radiusFull),
          borderSide: BorderSide(color: scheme.outlineVariant),
        ),
        enabledBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(AppSpacing.radiusFull),
          borderSide: BorderSide(color: scheme.outlineVariant),
        ),
        focusedBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(AppSpacing.radiusFull),
          borderSide: BorderSide(color: scheme.primary, width: 1.5),
        ),
        contentPadding: const EdgeInsets.symmetric(
          horizontal: AppSpacing.lg,
          vertical: AppSpacing.md,
        ),
      ),
      filledButtonTheme: FilledButtonThemeData(
        style: FilledButton.styleFrom(
          shape: const StadiumBorder(),
          padding: const EdgeInsets.symmetric(
            horizontal: AppSpacing.lg,
            vertical: AppSpacing.md,
          ),
        ),
      ),
    );
  }
}