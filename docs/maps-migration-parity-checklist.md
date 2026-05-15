# Google Maps Migration Parity Checklist (QA)

## 1) Dependency Audit (`app/build.gradle`)
- [ ] `com.google.android.gms:play-services-maps:19.0.0` terpasang.
- [ ] `com.google.maps.android:maps-compose:4.4.1` terpasang.
- [ ] Tidak ada dependency Google Maps duplikat/versi konflik yang ditambahkan saat migrasi.

## 2) Usage Inventory (Maps Compose API)

### 2.1 `WeatherLayerMapCard` (`app/src/main/java/com/weatherwise/ui/components/WeatherLayerMapCard.kt`)
- [ ] `GoogleMap` dipakai sebagai container peta utama.
- [ ] `MapUiSettings(zoomControlsEnabled = false)` dipakai (zoom gesture tetap via map gesture, tanpa tombol zoom bawaan).
- [ ] `MapProperties(isMyLocationEnabled = false)` dipakai.
- [ ] `TileOverlay` dipakai untuk render layer cuaca (OpenWeather tiles).
- [ ] `rememberCameraPositionState` dipakai untuk state kamera awal (`LatLng(lat, lon)` + zoom `6f`).
- [ ] `rememberTileOverlayState` dipakai untuk state overlay tile.
- [ ] Prefetch tile dilakukan saat kamera bergerak (snapshotFlow + debounce + hitung x/y/z + `prefetchAround`).

### 2.2 State/UI terkait map di `WeatherLayerMapCard`
- [ ] `selectedLayer` (radar/clouds/wind/pressure) tersedia dan persist ke SharedPreferences.
- [ ] `enabled` switch layer overlay tersedia.
- [ ] `opacity` slider (0.2..1.0) tersedia dan persist ke SharedPreferences.
- [ ] `isLoading` menampilkan loading indicator saat fetch metadata/prefetch.
- [ ] `radarError` menampilkan pesan error dan men-disable overlay ketika error.
- [ ] `lastUpdatedText` ditampilkan jika metadata tersedia.

### 2.3 Screen pemanggil
- [ ] `ForecastScreen` tetap memanggil `WeatherLayerMapCard(lat, lon)`.
- [ ] Koordinat tetap diambil dari `currentWeather?.lat/lon`.
- [ ] Section UI “Peta Layer Cuaca” tetap ada sebelum forecast 7 hari.

## 3) Manifest Audit (`app/src/main/AndroidManifest.xml`)
- [ ] `com.google.android.geo.API_KEY` **BELUM ADA** pada `<application>` (harus ditambahkan/diinjeksikan jika platform target tetap Google Maps).
- [ ] Permission jaringan masih ada: `INTERNET` dan `ACCESS_NETWORK_STATE`.

> Catatan: karena komponen saat ini menggunakan `GoogleMap` dari Maps Compose, ketiadaan `<meta-data android:name="com.google.android.geo.API_KEY" ...>` berpotensi menyebabkan peta dasar tidak berfungsi pada runtime.

## 4) Fitur Saat Ini yang Wajib Parity Pasca-Migrasi
- [ ] **Pan/Zoom map gesture** tetap berfungsi (kamera bergerak bebas, zoom pinch tetap aktif).
- [ ] **Layer switch** tetap ada (Radar, Clouds, Wind, Pressure).
- [ ] **Opacity control** tetap ada dan berdampak ke transparansi overlay.
- [ ] **Loading/Error state** tetap terlihat jelas (spinner + pesan error).
- [ ] **Prefetch tile radar** tetap berjalan saat pergerakan kamera untuk menjaga responsivitas layer.

## 5) QA Execution Checklist (siap pakai)
- [ ] Buka `ForecastScreen`, pastikan kartu “Peta Radar Cuaca” tampil.
- [ ] Uji pan/zoom di peta; pastikan tile overlay ikut update.
- [ ] Ganti layer 4x (Radar/Clouds/Wind/Pressure), verifikasi tile berubah.
- [ ] Ubah opacity ke nilai rendah dan tinggi, verifikasi transparansi visual.
- [ ] Toggle switch layer off/on, verifikasi overlay hide/show.
- [ ] Simulasikan kondisi error (mis. API key tile invalid/rate limit), verifikasi pesan error tampil.
- [ ] Pergerakkan map cepat beberapa kali, verifikasi loading indicator muncul lalu hilang.
- [ ] Tutup-buka screen/app, verifikasi layer & opacity terakhir ter-restore.
