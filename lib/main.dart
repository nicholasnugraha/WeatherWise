import 'package:flutter/material.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:flutter_dotenv/flutter_dotenv.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:url_strategy/url_strategy.dart';

import 'core/config/hive_config.dart';
import 'core/localization/app_localizations_delegate.dart';
import 'core/routing/app_router.dart';
import 'core/theme/app_theme.dart';
import 'features/settings/data/repositories/settings_repository.dart'
    show SettingsRepository;
import 'features/settings/presentation/providers/settings_view_model.dart'
    show settingsRepositoryProvider, settingsViewModelProvider;

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();

  // Use path-based URLs on web (no `#` in address bar) — required for
  // clean deep-linking like /forecast instead of /#/forecast.
  setPathUrlStrategy();

  // Load .env file bundled in the APK/web before any service that depends on it.
  await dotenv.load(fileName: 'assets/env.config');
  await HiveConfig.init();

  // Open settings box synchronously before app build so UI can read
  // theme / language / default city immediately.
  final settingsRepository = await SettingsRepository.open();

  runApp(
    ProviderScope(
      overrides: [
        settingsRepositoryProvider.overrideWithValue(settingsRepository),
      ],
      child: const WeatherWiseApp(),
    ),
  );
}

class WeatherWiseApp extends ConsumerStatefulWidget {
  const WeatherWiseApp({super.key});

  @override
  ConsumerState<WeatherWiseApp> createState() => _WeatherWiseAppState();
}

class _WeatherWiseAppState extends ConsumerState<WeatherWiseApp> {
  @override
  void initState() {
    super.initState();
    // ignore: avoid-ignoring-return-values
    ref.read(settingsViewModelProvider.notifier).load();
  }

  @override
  Widget build(BuildContext context) {
    final router = ref.watch(routerProvider);
    final settings = ref.watch(settingsViewModelProvider).settings;

    return MaterialApp.router(
      title: 'WeatherWise',
      debugShowCheckedModeBanner: false,
      locale: Locale(settings.languageCode),
      supportedLocales: const [Locale('id'), Locale('en')],
      localizationsDelegates: const [
        AppLocalizationsDelegate(),
        GlobalMaterialLocalizations.delegate,
        GlobalWidgetsLocalizations.delegate,
        GlobalCupertinoLocalizations.delegate,
      ],
      theme: AppTheme.light,
      darkTheme: AppTheme.dark,
      themeMode: settings.themeMode,
      routerConfig: router,
    );
  }
}
