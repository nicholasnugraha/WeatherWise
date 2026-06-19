import '../../../shared/domain/entities/forecast.dart';

/// Indonesian weekday names (Monday=1 .. Sunday=7 per DateTime convention).
const _weekdaysId = [
  'Senin', // 1
  'Selasa', // 2
  'Rabu', // 3
  'Kamis', // 4
  'Jumat', // 5
  'Sabtu', // 6
  'Minggu', // 7
];

/// Indonesian month abbreviations (matches Stitch "12 Feb", "13 Feb" labels).
const _monthsIdShort = [
  'Jan', 'Feb', 'Mar', 'Apr', 'Mei', 'Jun',
  'Jul', 'Agu', 'Sep', 'Okt', 'Nov', 'Des',
];

/// Returns "Hari Ini" for today's entry, "Besok" for tomorrow, else
/// "Senin, 12 Feb" — Indonesian formatted short date.
String formatForecastDateLabel(ForecastEntry entry, {required int nowEpoch}) {
  final today = DateTime.fromMillisecondsSinceEpoch(nowEpoch * 1000);
  final entryDate = DateTime.fromMillisecondsSinceEpoch(entry.timestamp * 1000);
  final todayDay = DateTime(today.year, today.month, today.day);
  final entryDay = DateTime(entryDate.year, entryDate.month, entryDate.day);
  final diff = entryDay.difference(todayDay).inDays;

  if (diff == 0) return 'Hari Ini';
  if (diff == 1) return 'Besok';

  final weekday = _weekdaysId[entryDate.weekday - 1];
  final month = _monthsIdShort[entryDate.month - 1];
  return '$weekday, ${entryDate.day} $month';
}

/// Just the Indonesian weekday + day (no month), e.g. "Senin, 12".
String formatWeekdayDay(ForecastEntry entry) {
  final d = DateTime.fromMillisecondsSinceEpoch(entry.timestamp * 1000);
  return '${_weekdaysId[d.weekday - 1]}, ${d.day}';
}

/// Indonesian UV category label (shared with dashboard metric grid).
String uviLabel(double uvi) {
  if (uvi < 3) return 'Rendah';
  if (uvi < 6) return 'Sedang';
  if (uvi < 8) return 'Tinggi';
  if (uvi < 11) return 'Sangat Tinggi';
  return 'Ekstrem';
}

/// m/s -> km/h with rounded integer + "km/j" suffix (Indonesian convention).
String formatWindKmh(double mps) => '${(mps * 3.6).round()} km/j';

/// Rain probability 0-100 -> "10%" rounded to integer.
String formatRainProbability(double pct) => '${pct.round()}%';