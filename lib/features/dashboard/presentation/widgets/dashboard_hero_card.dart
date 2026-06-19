import 'package:flutter/material.dart';

import '../../../../core/theme/app_colors.dart';
import '../../../../core/theme/app_spacing.dart';
import '../../../shared/domain/entities/current_weather.dart';
import '../../../shared/domain/entities/geocoding.dart';
import '../../../home/presentation/widgets/weather_icon_helper.dart';
import '../utils/weather_translation.dart';

/// Hero weather card for the Dashboard.
///
/// Per Stitch `weatherwise_dashboard_light_mode_consistent_layout`:
///   - Background: subtle gradient from `primaryContainer` (#00A3FF)
///     to `primary` (#00629D), ~135deg (top-left to bottom-right)
///   - Top-left: city name (headline-lg) + date (label-sm uppercase)
///   - Top-right: weather condition icon + Indonesian label (title-md)
///   - Center: huge temperature in display-temp typography (80px / w700)
///   - Bottom: H:/L: in body-md
class DashboardHeroCard extends StatelessWidget {
  const DashboardHeroCard({
    super.key,
    required this.weather,
    this.location,
  });

  final CurrentWeather weather;
  final Geocoding? location;

  @override
  Widget build(BuildContext context) {
    final day = isDaytime(
      sunriseEpoch: weather.sunrise,
      sunsetEpoch: weather.sunset,
      timezoneOffsetSeconds: weather.timezone,
    );
    final conditionIcon = WeatherIconHelper.getWeatherIcon(
      weather.weatherMain,
      isDay: day,
    );
    final conditionText = weatherMainToIndonesian(weather.weatherMain);
    final cityLabel = location?.displayName ?? '${weather.cityName}, ${weather.country}';
    final dateLabel = _formatDate(DateTime.fromMillisecondsSinceEpoch(
      weather.timestamp * 1000,
    ));

    return Container(
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(AppSpacing.radiusLg),
        gradient: const LinearGradient(
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
          colors: [
            AppColors.lightPrimaryContainer, // #00A3FF
            AppColors.lightPrimary,           // #00629D
          ],
        ),
      ),
      padding: const EdgeInsets.all(AppSpacing.lg),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Top row: city + date on left, condition icon + label on right.
          Row(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      cityLabel,
                      style: const TextStyle(
                        color: Colors.white,
                        fontSize: 28,
                        fontWeight: FontWeight.w600,
                        height: 1.2,
                      ),
                    ),
                    const SizedBox(height: AppSpacing.xs),
                    Text(
                      dateLabel,
                      style: TextStyle(
                        color: Colors.white.withValues(alpha: 0.85),
                        fontSize: 12,
                        fontWeight: FontWeight.w500,
                        letterSpacing: 0.05 * 12,
                      ),
                    ),
                  ],
                ),
              ),
              Icon(
                conditionIcon,
                color: Colors.white,
                size: 40,
              ),
            ],
          ),
          const SizedBox(height: AppSpacing.md),
          Text(
            conditionText,
            style: const TextStyle(
              color: Colors.white,
              fontSize: 18,
              fontWeight: FontWeight.w600,
            ),
          ),
          const SizedBox(height: AppSpacing.lg),
          // Hero temperature — display-temp per DESIGN.md.
          Text(
            '${weather.temp.round()}°',
            style: const TextStyle(
              color: Colors.white,
              fontSize: 80,
              fontWeight: FontWeight.w700,
              height: 1.125,
              letterSpacing: -0.04 * 16,
            ),
          ),
          const SizedBox(height: AppSpacing.sm),
          Text(
            'H: ${weather.tempMax.round()}°   L: ${weather.tempMin.round()}°',
            style: TextStyle(
              color: Colors.white.withValues(alpha: 0.9),
              fontSize: 16,
              fontWeight: FontWeight.w400,
            ),
          ),
        ],
      ),
    );
  }

  String _formatDate(DateTime date) {
    // Indonesian format: "Jumat, 27 Oktober" — EEEE, d MMMM.
    const months = [
      'Januari', 'Februari', 'Maret', 'April', 'Mei', 'Juni',
      'Juli', 'Agustus', 'September', 'Oktober', 'November', 'Desember',
    ];
    const weekdays = [
      'Senin', 'Selasa', 'Rabu', 'Kamis', 'Jumat', 'Sabtu', 'Minggu',
    ];
    // Note: DateTime.weekday returns 1=Monday..7=Sunday.
    return '${weekdays[date.weekday - 1]}, ${date.day} ${months[date.month - 1]}';
  }
}