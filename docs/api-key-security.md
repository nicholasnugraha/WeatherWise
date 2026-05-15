# API Key Security Checklist

## 1) BuildConfig injection via local.properties / CI secrets

Project sudah menginjeksi key via `BuildConfig.WEATHER_API_KEY` per build type:
- `debug` -> `WEATHER_API_KEY_DEBUG`
- `release` -> `WEATHER_API_KEY_RELEASE`

Sumber nilai mengikuti prioritas berikut:
1. Environment variable CI (`OW_API_KEY_DEBUG`, `OW_API_KEY_RELEASE`, `OW_TILE_BASE_URL`)
2. `local.properties` (`WEATHER_API_KEY_DEBUG`, `WEATHER_API_KEY_RELEASE`, `OW_TILE_BASE_URL`)

## 2) Pisahkan key debug/release + batasi di OpenWeather dashboard

Rekomendasi operasional:
- Buat 2 API key terpisah di OpenWeather account: satu untuk debug/staging, satu untuk production.
- Terapkan pembatasan aplikasi/domain/IP yang tersedia di dashboard OpenWeather untuk masing-masing key.
- Aktifkan monitoring dan rotasi berkala jika ada indikasi abuse.

> Catatan: Opsi pembatasan bisa berubah sesuai kebijakan OpenWeather. Cek dashboard terbaru sebelum rollout.

## 3) Obfuscation release (R8/ProGuard)

Release build sudah menggunakan:
- `minifyEnabled true`
- `shrinkResources true`
- `proguard-android-optimize.txt` + `app/proguard-rules.pro`

Ini membantu memperkecil kemudahan reverse engineering, tetapi **tidak membuat secret benar-benar aman** di sisi client.

## 4) Opsi proxy backend (disarankan untuk threat model ketat)

Jika ingin meminimalkan paparan key di aplikasi client:
- Buat backend proxy endpoint (mis. `/weather/current`, `/weather/forecast`) yang memanggil OpenWeather dari server.
- Simpan OpenWeather key hanya di server secret manager.
- Tambahkan rate limiting, auth, logging, dan WAF di proxy.
- Aplikasi Android hanya memanggil backend Anda, bukan OpenWeather langsung.

## 5) Audit kebocoran key

Audit yang disarankan:
- Scan working tree dari pola key (`rg`).
- Scan history git (`git log -G ...`).
- Pastikan `local.properties` tetap di `.gitignore`.
- Sediakan `local.properties.example` tanpa secret nyata.
