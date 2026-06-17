# WeatherWise Flutter 🌤️

Cross-platform weather application built with Flutter.

## Features

- 🌡️ **Current Weather** — Real-time temperature, humidity, wind, pressure
- 📅 **5-Day Forecast** — Hourly and daily weather predictions
- 🗺️ **Radar Map** — RainViewer radar overlay with timeline slider
- 📍 **GPS Location** — Automatic weather for your current location
- 🔍 **City Search** — Search weather for any city worldwide
- 💾 **Offline Cache** — 15-minute cache for fast loading
- 🌙 **Dark Mode** — System-aware light/dark theme

## Tech Stack

| Component | Technology |
|-----------|-----------|
| Framework | Flutter 3.x |
| Language | Dart |
| State Mgmt | Riverpod 2.x |
| Networking | Dio |
| Models | Freezed + json_serializable |
| Maps | flutter_map + RainViewer API |
| Storage | Hive (local cache) |
| Location | geolocator |
| Architecture | Clean Architecture (Feature-first) |

## Setup

1. Clone the repository
2. Get your free API key from [OpenWeatherMap](https://openweathermap.org/api)
3. Run:
```bash
flutter pub get
flutter run --dart-define=WEATHER_API_KEY=your_key_here
```

## Build

```bash
# Debug
flutter build apk --debug

# Release
flutter build apk --release --dart-define=WEATHER_API_KEY=your_key_here
```

## CI/CD

Travis CI automatically builds and creates GitHub Releases on push to `flutter-rewrite` branch.

## Architecture

```
lib/
├── core/
│   ├── config/          # API config, Dio client, Hive
│   ├── providers/       # Global Riverpod providers
│   ├── theme/           # App theme
│   └── utils/           # Location service, formatters
├── features/
│   ├── home/            # Current weather screen
│   ├── forecast/        # Hourly + daily forecast
│   ├── map/             # RainViewer radar map
│   └── shared/          # Shared models, APIs, repository
└── main.dart
```

## Data Sources

- **Weather**: [OpenWeatherMap API](https://openweathermap.org/api)
- **Radar**: [RainViewer API](https://www.rainviewer.com/api.html)
- **Maps**: [OpenStreetMap](https://www.openstreetmap.org/)

## License

MIT
