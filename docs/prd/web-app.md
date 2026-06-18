# PRD: WeatherWise Web App

| Field | Value |
|---|---|
| **Status** | Draft v0.1 — menunggu UI design reference |
| **Author** | Nicholas Nugraha |
| **Created** | 2026-06-18 |
| **Last Updated** | 2026-06-18 |
| **Target Branch** | `web-app` (bercabang dari `flutter`) |
| **Related Docs** | `README.md`, `pubspec.yaml` |

---

## 1. Ringkasan

WeatherWise Web App adalah versi web dari aplikasi cuaca personal yang sudah ada di Android (branch `flutter`). Tujuan utamanya adalah **menjangkau semua platform via satu codebase Flutter** — terutama laptop, PC, dan iOS-via-browser — tanpa perlu memiliki device iOS atau biaya Apple Developer Account.

Web app ini **bukan** produk komersial atau publik. Ia adalah alat personal yang mengorbankan sedikit performa first-load (1.5–3 detik, vs 0.5 detik untuk Vite/React) demi **reusability kode 95%+ dengan mobile** dan **satu single source of truth** untuk logika bisnis, model, dan API client.

---

## 2. Latar Belakang & Masalah

### 2.1 Konteks
- Cuaca di tempat tinggal user **tidak bisa diprediksi** — akses频繁 ke weather info adalah kebutuhan harian
- User sudah punya WeatherWise Android di branch `flutter` (Flutter 3.24.4 + Riverpod + Clean Architecture)
- User tidak punya budget untuk Apple Developer Account ($99/year) atau device iOS untuk testing
- APK Android saat ini **~70MB** (bisa dioptimasi ke ~20MB dengan ABI split + R8)

### 2.2 Masalah yang Dipecahkan
1. **Reach lintas platform** — akses dari laptop/PC/tablet tanpa install aplikasi
2. **No iOS overhead** — satu web build menjangkau semua OS (termasuk iOS via Safari)
3. **Code reuse** — tidak menulis ulang domain/data layer yang sudah mature
4. **Single maintenance** — satu fix bug berlaku untuk mobile dan web

### 2.3 Konteks Trade-off (sudah disetujui)
- First load 1.5–3 detik pada broadband (acceptable untuk personal use, bukan untuk traffic publik)
- Bundle size ~1.5–2.5MB (WASM Skwasm, lebih besar dari React tapi tidak signifikan untuk personal)
- SEO tidak relevan (data personal, tidak di-index Google)
- Tidak ada native iOS push notification, app icon di home screen masih versi web (bisa di-install via "Add to Home Screen" → PWA)

---

## 3. Target User & Use Case

### 3.1 Target User
- **Primary:** Nicholas sendiri (1 user, 1–20 akses per hari)
- **Tertiary:** Siapapun yang Nicholas share URL-nya (low traffic, no SLA)

### 3.2 Use Case Utama
| ID | Use Case | Frekuensi | Konteks |
|---|---|---|---|
| UC1 | Cek cuaca saat ini di lokasi saya (auto-GPS) | 5–15×/hari | Desktop, laptop, mobile browser |
| UC2 | Cari cuaca kota lain (misal: cek cuaca kota orang tua) | 1–3×/hari | Semua device |
| UC3 | Lihat prakiraan 24 jam ke depan | 2–5×/hari | Semua device |
| UC4 | Lihat prakiraan 7 hari ke depan | 1–2×/hari | Semua device |
| UC5 | Lihat radar hujan (pergerakan awan hujan) | 1–5×/hari saat musim hujan | Semua device |
| UC6 | Lihat detail cuaca (UV, humidity, wind, sunrise/sunset) | 1–3×/hari | Semua device |

### 3.3 Use Case yang **TIDAK** Dicakup
- ❌ Multiple user accounts / login
- ❌ Share ke social media
- ❌ Push notification (web limitation)
- ❌ Historical weather (archive lebih dari 5 hari ke belakang)
- ❌ Server-side processing
- ❌ Multi-language (UI Bahasa Indonesia saja untuk saat ini, tapi string sudah `S.of()` style untuk future-proofing)

---

## 4. Goals & Non-Goals

### 4.1 Goals (Sukses = semua terpenuhi)

| # | Goal | Metrik |
|---|---|---|
| G1 | Feature parity dengan Android app | 100% fitur Android ada di web |
| G2 | Single codebase, single bug fix | Satu fix untuk mobile + web |
| G3 | Responsif di mobile, tablet, desktop | 3 breakpoint berfungsi lancar |
| G4 | PWA installable | "Add to Home Screen" berfungsi di Chrome/Edge |
| G5 | URL deep linking | `/city/jakarta`, `/map?lat=..&lon=..` bisa di-share |
| G6 | Offline cache berfungsi | Buka web app tanpa internet → tampilkan cached data terakhir |
| G7 | Bundle size masuk akal | Web build < 3MB gzipped |
| G8 | Code base tetap KISS | Tidak ada dependency baru tanpa use case konkret |

### 4.2 Non-Goals (Eksplisit Tidak Dicakup)

- 🚫 SSR / SEO optimization
- 🚫 Multi-tenant / multi-user
- 🚫 Premium tier / payment
- 🚫 Native iOS / Android rebuild (yang sudah ada cukup)
- 🚫 Backend custom (selain thin proxy untuk CORS + API key hiding)
- 🚫 Analytics / tracking
- 🚫 A/B testing
- 🚫 Real-time push (pakai polling / refresh manual cukup)

---

## 5. Fitur

### 5.1 MVP (Phase 1) — Target: 1–2 minggu

| ID | Fitur | Prioritas | Status dari Android |
|---|---|---|---|
| F1 | Current weather (auto-location) | P0 | ✅ Sudah ada |
| F2 | Search cuaca by nama kota | P0 | ✅ Sudah ada |
| F3 | Hourly forecast (24 jam) | P0 | ✅ Sudah ada |
| F4 | Daily forecast (7 hari) | P0 | ✅ Sudah ada |
| F5 | Weather details (UV, humidity, wind, sunrise/sunset) | P1 | ✅ Sudah ada |
| F6 | Responsive layout (mobile/tablet/desktop) | P0 | ❌ Baru |
| F7 | URL routing (deep linking, browser back/forward) | P0 | ❌ Baru |
| F8 | Hive cache (offline-first) | P0 | ✅ Sudah ada |
| F9 | Refresh button / pull-to-refresh | P1 | ✅ Sudah ada |
| F10 | Light/dark theme (system) | P2 | ✅ Sudah ada |

### 5.2 Phase 2 — Target: 1 minggu setelah MVP

| ID | Fitur | Prioritas | Status |
|---|---|---|---|
| F11 | Radar map (RainViewer overlay) | P0 | ✅ Sudah ada, perlu web-tuning |
| F12 | Air quality (jika ada di One Call 3.0 response) | P2 | ⏳ Tergantung API |
| F13 | PWA manifest + service worker | P0 | ❌ Baru |

### 5.3 Phase 3 — Polish & Optimization

| ID | Fitur | Prioritas |
|---|---|---|
| F14 | Pre-render splash untuk first-paint yang lebih cepat | P2 |
| F15 | Skeleton loader (bukan spinner) | P2 |
| F16 | Hapus unused dependencies dari `pubspec.yaml` | P0 |
| F17 | Fix Android APK size (ABI split + R8) | P1 (Android side) |
| F18 | Backend proxy untuk CORS + API key hiding | P0 (sebelum public deploy) |
| F19 | Custom 404 page | P3 |
| F20 | Keyboard shortcuts (press `r` to refresh, `/` to search) | P3 |

### 5.4 Fitur Eksplisit yang DITANGGUHKAN (Future, bukan sekarang)

- ❌ Favorite cities (save list) — belum ada di Android, jangan tambah di web
- ❌ Widget embed untuk website lain
- ❌ Voice command
- ❌ Geofencing / notifikasi
- ❌ Trip planner (cek cuaca multi-kota untuk tanggal tertentu)

---

## 6. Tech Stack

### 6.1 Final Stack (Locked)

| Layer | Pilihan | Alasan |
|---|---|---|
| **Framework** | Flutter 3.27+ | Skwasm renderer default, stable untuk web |
| **State Management** | Riverpod 2.5+ (existing) | Sudah proven di Android codebase |
| **HTTP Client** | Dio 5.4+ (existing) | Interceptor untuk cache + error handling |
| **Code Gen** | Freezed + json_serializable (existing) | Immutable models + JSON |
| **Local Storage** | Hive + hive_flutter (existing) | IndexedDB di web, noSQL di mobile |
| **Maps** | flutter_map 6.1+ (existing) | Open-source, OSM tiles, no API key required |
| **Location** | geolocator 10.1+ (existing) | Cross-platform, geolocator_web auto-handle |
| **Routing** | go_router (NEW — belum ada) | URL-based routing wajib untuk web |
| **Env Config** | flutter_dotenv 5.1+ (existing) | Bundled `assets/.env` |
| **Renderer** | Skwasm (WASM) | Default di Flutter 3.27+ |
| **Hosting** | Vercel | Edge CDN, free tier cukup |
| **Backend Proxy** | Vercel Edge Functions / Cloudflare Workers | Untuk hide API key + handle CORS |

### 6.2 Dependencies yang AKAN DIHAPUS (clean up)

Dari audit pubspec.yaml, dependencies ini di-declare tapi **0 penggunaan** di `lib/`:

| Package | Status | Tindakan |
|---|---|---|
| `rive: ^0.13.4` | Tidak ada `.riv` file, tidak ada import | ❌ **Hapus** |
| `flutter_svg: ^2.0.10+1` | Tidak ada import | ❌ **Hapus** |
| `google_fonts: ^6.2.1` | Tidak ada import | ❌ **Hapus** |

Prinsip KISS: jangan maintain dependency yang tidak dipakai.

### 6.3 Dependencies BARU

| Package | Versi Target | Alasan |
|---|---|---|
| `go_router` | ^14.0 | URL routing, deep linking, browser back/forward |
| `flutter_web_plugins` | bundled | Standard, tidak perlu di pubspec |
| `url_strategy` | ^2.0 | Hapus `#` di URL (path-based, bukan hash-based) |

---

## 7. Arsitektur

### 7.1 Reuse Strategy

Sesuai prinsip KISS + Clean Architecture, **hierarchy reuse** adalah:

```
┌─────────────────────────────────────────────────────────┐
│ Presentation Layer                                     │
│ ❌ TIDAK reuse langsung — perlu responsive + URL routing │
│ ✅ TAPI: ViewModel, widget logic, formatters reuse      │
├─────────────────────────────────────────────────────────┤
│ Domain Layer (entities, repositories interfaces, usecases)│
│ ✅ 100% reuse — pure Dart, no Flutter import           │
├─────────────────────────────────────────────────────────┤
│ Data Layer (Dio, Freezed models, Hive)                  │
│ ✅ 100% reuse — platform-agnostic                       │
├─────────────────────────────────────────────────────────┤
│ Core (config, providers, theme, utils)                  │
│ ✅ 90% reuse, 10% perlu platform-specific shim         │
│    (geolocator_web, hive_flutter already handle this)  │
└─────────────────────────────────────────────────────────┘
```

### 7.2 Struktur Folder (Final)

```
lib/
├── core/
│   ├── config/
│   │   ├── api_config.dart          # dart-define + dotenv (existing)
│   │   ├── hive_config.dart         # existing, web-safe
│   │   └── env_loader.dart          # NEW: web-aware env loader
│   ├── providers/
│   │   ├── dio_provider.dart        # existing
│   │   ├── api_service_provider.dart # existing
│   │   └── router_provider.dart     # NEW: GoRouter config
│   ├── theme/
│   │   └── app_theme.dart           # existing
│   ├── routing/
│   │   ├── app_router.dart          # NEW: GoRouter definition
│   │   └── routes.dart              # NEW: route name constants
│   ├── responsive/
│   │   ├── breakpoints.dart         # NEW: mobile/tablet/desktop constants
│   │   └── responsive_builder.dart  # NEW: layout builder helper
│   └── utils/
│       └── ...                       # existing
│
├── features/
│   ├── home/
│   │   ├── data/        # ✅ 100% reuse dari Android
│   │   ├── domain/      # ✅ 100% reuse
│   │   └── presentation/
│   │       ├── providers/    # ✅ reuse
│   │       ├── screens/
│   │       │   ├── home_screen_mobile.dart   # NEW (web-tuned)
│   │       │   ├── home_screen_desktop.dart  # NEW
│   │       │   └── home_screen.dart          # dispatcher
│   │       └── widgets/      # ✅ reuse + tambah hover states
│   ├── forecast/        # sama seperti home
│   ├── map/             # sama, dengan web-specific tile caching
│   └── shared/          # 100% reuse
│
├── web/                 # NEW: web-only files
│   └── web_utils.dart   # e.g. browser geolocation helper
│
└── main.dart            # updated: path strategy + GoRouter
```

### 7.3 Routing

```
/                           → HomeScreen
/forecast?lat=..&lon=..     → ForecastScreen
/map?lat=..&lon=..          → MapScreen
/city/:cityName             → HomeScreen (load by city)
404                         → NotFoundScreen
```

Browser back/forward button akan berfungsi native. Share URL `/city/jakarta` akan membuka langsung kota Jakarta.

---

## 8. Prinsip KISS (Code Rules)

Ini adalah **kontrak pengembangan** yang harus diikuti setiap kali menulis kode baru atau refactor.

### 8.1 Dependency Rules
- ❌ Jangan tambah package baru tanpa **use case konkret yang sudah ada di kode**
- ❌ Jangan maintain dependency yang tidak ada import-nya
- ✅ Cek `grep -r "package:nama_package" lib/` sebelum menambah/menghapus

### 8.2 State Management Rules
- ✅ Pakai `StateNotifier` (sudah proven) — jangan migrate ke `riverpod_generator` kecuali ada benefit konkret
- ❌ Jangan buat provider untuk value yang hanya dipakai 1 tempat
- ❌ Jangan buat abstract class untuk 1 implementasi — langsung concrete class

### 8.3 Widget Rules
- ✅ Extract widget jika **dipakai ≥ 2 kali** atau **>100 baris**
- ❌ Jangan extract widget cuma untuk "kelihatan rapi"
- ❌ Jangan pakai StatefulWidget kalau semua state bisa di Riverpod
- ✅ Pakai Material 3 widgets bawaan (`Scaffold`, `AppBar`, `Card`) — jangan custom-build yang sudah ada

### 8.4 Feature Rules
- ❌ Jangan tambah fitur "future-proof" — tambah saat diminta
- ❌ Jangan tambah abstraction untuk 1 use case
- ✅ Kalau mau tambah fitur: tulis use case dulu, baru kode

### 8.5 Comment Rules
- ✅ Comment **WHY**, bukan **WHAT** (kode sudah jelas menunjukkan what)
- ❌ Jangan comment yang obvious (`// increment counter` di atas `i++`)
- ✅ Comment kalau ada **pitfall** atau **non-obvious decision**

### 8.6 Testing Rules
- ✅ Test untuk business logic (repository, usecase) — bukan untuk widget trivial
- ❌ Jangan test getter, setter, atau copyWith
- ❌ Jangan test UI yang tidak ada logic (e.g. "render X when Y" — itu snapshot test yang false confidence)

### 8.7 Refactor Trigger
Refactor hanya jika:
- Ada bug yang sulit dilacak karena struktur sekarang
- Ada feature baru yang **benar-benar butuh** struktur berbeda
- Code duplication muncul **3+ kali**

Bukan karena "kelihatan tidak rapi" — itu bukan alasan.

---

## 9. Performance Targets

| Metric | Target | Catatan |
|---|---|---|
| First Contentful Paint (broadband) | ≤ 3 detik | Acceptable trade-off |
| Time to Interactive | ≤ 4 detik | Setelah WASM loaded |
| Bundle size (gzipped) | ≤ 3 MB | Total web/build output |
| Subsequent load (cached) | ≤ 500 ms | Service worker cache |
| Lighthouse Performance Score | ≥ 70 | Web Vitals |
| Lighthouse PWA Score | ≥ 90 | Installable + manifest valid |

**Tidak ada target Lighthouse 100.** Realistis untuk Flutter Web. Yang penting: usable.

---

## 10. Deployment (Vercel)

### 10.1 Build Command
```bash
flutter build web --wasm --release \
  --dart-define=API_BASE_URL=https://api-proxy.your-domain.com
```

### 10.2 Vercel Config (`vercel.json`)
```json
{
  "buildCommand": "flutter build web --wasm --release",
  "outputDirectory": "build/web",
  "framework": null,
  "headers": [
    {
      "source": "/(.*)",
      "headers": [
        { "key": "X-Content-Type-Options", "value": "nosniff" },
        { "key": "Referrer-Policy", "value": "strict-origin-when-cross-origin" }
      ]
    },
    {
      "source": "/assets/(.*)",
      "headers": [
        { "key": "Cache-Control", "value": "public, max-age=31536000, immutable" }
      ]
    },
    {
      "source": "/canvaskit/(.*)",
      "headers": [
        { "key": "Cache-Control", "value": "public, max-age=31536000, immutable" }
      ]
    }
  ]
}
```

### 10.3 Backend Proxy (Phase 2)

Bukan deploy Vercel static only — tambah **Vercel Edge Function** atau **Cloudflare Worker** di `/api/weather/*` yang:
1. Terima request dari web app
2. Tambahkan OpenWeatherMap API key di server-side
3. Forward ke `api.openweathermap.org`
4. Return response ke client

**Alasan:** API key di `.env` bundled bisa di-extract dari APK/web bundle. Proxy + key di server = lebih aman, plus bypass CORS.

### 10.4 Environment Variables (di Vercel dashboard)
- `OPENWEATHER_API_KEY` — untuk Edge Function
- `ENV` — `production` | `preview`

---

## 11. Roadmap & Milestones

### Milestone 1: Foundation (Week 1)
- [ ] Setup branch `web-app`
- [ ] Hapus unused deps (rive, flutter_svg, google_fonts)
- [ ] Upgrade Flutter ke 3.27+ di Travis
- [ ] First `flutter build web --wasm` sukses
- [ ] Tambah `go_router` + path strategy
- [ ] Tambah `responsive_builder` + breakpoints
- [ ] Deploy blank Flutter app ke Vercel (verifikasi CI/CD)

### Milestone 2: Feature Parity (Week 2)
- [ ] Port `HomeScreen` ke responsive (mobile/tablet/desktop)
- [ ] Port `ForecastScreen` ke responsive
- [ ] Port `MapScreen` dengan web-tuned tile caching
- [ ] URL routing berfungsi untuk semua screen
- [ ] Hive cache berfungsi di IndexedDB

### Milestone 3: Polish (Week 3)
- [ ] PWA manifest + service worker
- [ ] Edge Function proxy untuk API key
- [ ] Lighthouse audit + optimization
- [ ] Custom 404 page
- [ ] Browser back/forward validation
- [ ] Mobile device testing (Chrome DevTools + real device kalau ada)

### Milestone 4: Android Side Cleanup (parallel, tidak blocker)
- [ ] ABI split di `android/app/build.gradle.kts`
- [ ] R8 + resource shrinking
- [ ] Hapus unused deps
- [ ] Verify APK turun ke ~20MB

---

## 12. Risiko & Mitigasi

| # | Risiko | Dampak | Mitigasi |
|---|---|---|---|
| R1 | CORS error saat browser hit OpenWeatherMap langsung | App tidak load | Backend proxy di Phase 2 (F18) |
| R2 | flutter_map tile lambat di web karena tidak ada caching | UX buruk di map | Pakai `flutter_map_cancellable_tile_provider` (sudah ada) + custom cache layer |
| R3 | First load 3 detik di koneksi lambat | User kabur | Tambah splash + skeleton loader (F14, F15) |
| R4 | Geolocation permission di browser berbeda dengan mobile | UX confusing | Tulis `web_utils.dart` dengan fallback UX yang jelas |
| R5 | Service worker conflict dengan Flutter routing | Cache miss | Pakai official Flutter service worker template |
| R6 | Hive schema migration break di web | Crash on load | Sudah ada pattern di `hive_config.dart` dengan `currentCacheSchemaVersion` |
| R7 | API key bocor dari bundle (siapa pun bisa extract dari web bundle) | Quota abuse, billing | Backend proxy WAJIB sebelum public deploy |

---

## 13. Open Questions (perlu keputusan sebelum/during development)

| # | Pertanyaan | Default kalau tidak dijawab |
|---|---|---|
| Q1 | Backend proxy pakai Vercel Edge Function atau Cloudflare Worker? | Vercel Edge Function (satu platform) |
| Q2 | Custom domain atau pakai Vercel subdomain (`weatherwise.vercel.app`)? | Vercel subdomain (free, no DNS setup) |
| Q3 | Apakah perlu support IE11 / Safari < 15? | TIDAK — assume modern browser |
| Q4 | Apakah perlu cookie consent banner? | TIDAK — no tracking, no analytics |
| Q5 | Lokasi GPS: ask every time atau remember last location? | Remember last location (Hive) + tombol refresh |
| Q6 | Theme: light only, dark only, atau system? | System (sesuai existing) |
| Q7 | Default kota apa saat pertama buka (no GPS)? | Jakarta (atau deteksi via IP geolocation) |

---

## 14. Out of Scope (Eksplisit)

Untuk menghindari scope creep, ini **tidak akan dibangun** di versi awal kecuali diminta eksplisit:

- 🌐 Multi-bahasa i18n (Bahasa Indonesia only)
- 👥 User accounts / sync lintas device
- 📊 Historical weather data
- 🔔 Notifikasi (web limitation anyway)
- 📱 Native iOS / Android rebuild
- 💳 Premium tier
- 📈 Analytics
- 🎨 Custom theme builder

---

## 15. Lampiran: Referensi

### 15.1 Existing Android App
- Branch: `flutter` (stable)
- Files of interest:
  - `lib/main.dart` — entry point
  - `lib/features/home/presentation/screens/home_screen.dart`
  - `lib/features/forecast/presentation/screens/forecast_screen.dart`
  - `lib/features/map/presentation/screens/map_screen.dart`
  - `lib/core/config/api_config.dart`
  - `lib/core/config/hive_config.dart`

### 15.2 Tech Docs
- Flutter Web renderers: https://docs.flutter.dev/platform-integration/web/renderers
- go_router: https://pub.dev/packages/go_router
- Hive web: https://pub.dev/packages/hive_flutter

### 15.3 Internal Skills
- `flutter-cross-platform` — Clean Architecture + Riverpod + build_runner patterns
- `ci-cd-workflows` — Travis CI setup untuk Flutter

---

## 16. Changelog

| Date | Version | Perubahan |
|---|---|---|
| 2026-06-18 | 0.1 | Initial draft — menunggu UI design reference |
