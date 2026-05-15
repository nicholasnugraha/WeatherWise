# Keputusan Teknis Integrasi Peta: osmdroid + Jetpack Compose

## Ringkasan Keputusan
- **Library utama peta**: `org.osmdroid:osmdroid-android`.
- **Strategi integrasi Compose**: gunakan `AndroidView` untuk membungkus `org.osmdroid.views.MapView` (tetap View-based di dalam layar Compose).
- **Arah arsitektur**: **tidak** memaksa migrasi ke map composable-native saat ini; evaluasi ulang hanya ketika ada kebutuhan produk/UX/performa yang jelas.

## 1) Evaluasi Library Utama (`osmdroid-android`)
`osmdroid-android` dipilih karena:
- Berbasis OpenStreetMap (OSM) dan cocok untuk kebutuhan peta non-Google Play Services.
- Mendukung tile source/overlay yang fleksibel, termasuk overlay cuaca berbasis tile URL.
- API `MapView` stabil untuk use-case pan/zoom + layer overlay + kontrol lifecycle manual.
- Tidak mewajibkan API key Google Maps untuk base map OSM.

Trade-off yang diterima:
- Integrasi di Compose tidak se-“native” library map composable murni.
- Lifecycle (`onResume/onPause/onDetach`) harus dikelola eksplisit dari host Compose.
- Beberapa pola state sinkronisasi camera/overlay perlu adapter tambahan di sisi Compose.

## 2) Integrasi Compose via `AndroidView`
Pola integrasi yang ditetapkan:
- `AndroidView(factory = { context -> MapView(context) ... }, update = { mapView -> ... })`.
- `MapView` dibuat sekali per komposisi (gunakan `remember` bila diperlukan) untuk menghindari recreate berulang.
- Sinkronisasi state Compose (`lat/lon`, zoom, layer aktif, opacity, enabled) dilakukan di blok `update`.
- Lifecycle disambungkan dari Compose (`DisposableEffect` + `LifecycleEventObserver`) agar `MapView` mengikuti `ON_RESUME/ON_PAUSE/ON_DESTROY`.

Implikasi praktis:
- UI tetap Compose-first, komponen map tetap View-based.
- Risiko rewrite besar dapat dihindari sampai ada alasan kuat untuk migrasi ke alternatif composable murni.

## 3) Versi Library Target, Kompatibilitas `minSdk 26`, dan Implikasi APK
### Target dependency
```gradle
implementation("org.osmdroid:osmdroid-android:6.1.20")
```

### Kompatibilitas
- Konfigurasi project saat ini sudah `minSdk 26`, sehingga berada dalam rentang aman untuk pemakaian `MapView` osmdroid.
- Tidak ada kebutuhan khusus Play Services untuk rendering base map OSM.

### Implikasi ukuran APK
- Menambahkan `osmdroid-android` akan menambah ukuran binary aplikasi (AAR + transitive dependencies).
- Namun, bila dependency Google Maps (`play-services-maps` + `maps-compose`) dilepas pada fase implementasi penuh osmdroid, total ukuran APK/AAB dapat lebih efisien tergantung hasil shrinker.
- Validasi final harus berbasis artefak build aktual (`assembleRelease` + analyzer APK/AAB), bukan estimasi dokumen.

## 4) Keputusan Teknis Final
Keputusan tim untuk fase saat ini:
1. **Tetap gunakan map View-based** (`MapView` osmdroid) di dalam Compose dengan `AndroidView`.
2. **Tunda migrasi ke map composable alternatif** sampai ada kebutuhan nyata (mis. kompleksitas state berkurang signifikan, kebutuhan animation API tertentu, atau standardisasi lintas modul).
3. Prioritaskan stabilitas fitur yang sudah ada: pan/zoom, layer switch, opacity, loading/error state, dan persistensi preferensi.

## 5) Dependency Final yang Direkomendasikan untuk Tim
Status dependency target untuk arsitektur map:
- **Wajib**:
  - `org.osmdroid:osmdroid-android:6.1.20`
- **Opsional (sesuai implementasi overlay cuaca saat ini)**:
  - `androidx.preference:preference-ktx` (jika state layer/opacity dipersist via SharedPreferences)

Catatan transisi:
- Jika modul map sudah sepenuhnya berpindah ke osmdroid, tandai dependency Google Maps sebagai kandidat removal untuk menghindari duplikasi stack peta.
- Lakukan cleanup dependency hanya bersamaan dengan verifikasi parity QA agar tidak memutus fitur runtime.

## Alasan Pemilihan untuk Tim (Executive Summary)
- **Vendor-neutral**: OSM + osmdroid mengurangi ketergantungan ke ekosistem Google Maps.
- **Cocok untuk overlay tile cuaca**: kebutuhan utama aplikasi adalah visualisasi layer tile, bukan fitur POI Google.
- **Risiko migrasi rendah**: Compose tetap dipakai sebagai host UI, map engine dipertahankan View-based.
- **Jalur migrasi jelas**: keputusan saat ini tidak menutup opsi migrasi ke map composable penuh di masa depan.
