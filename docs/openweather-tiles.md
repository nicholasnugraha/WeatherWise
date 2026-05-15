# OpenWeather Tiles Integration Notes

## 1) Status produk aktif di dashboard OpenWeather

Status produk **harus dicek langsung pada akun dashboard** karena bergantung subscription tiap akun.
Checklist yang perlu diverifikasi di `My Services`/`Billing`:

- Weather Maps API - Current
- Weather Maps API - History and Forecast (jika butuh historical/forecast map)
- Kuota plan aktif (calls/minute + calls/month)

> Catatan: Repo ini tidak menyimpan kredensial dashboard, jadi verifikasi aktif/tidaknya produk harus dilakukan oleh pemilik akun saat deploy.

## 2) Layer yang dipakai + penentuan “radar hujan”

Layer operasional yang disetujui:

- `precipitation_new`
- `clouds_new`
- `pressure_new`
- `wind_new`

Layer yang diposisikan sebagai **radar hujan**: `precipitation_new`.

## 3) Format URL tile, API key, rate limit, attribution

### URL tile format

Template endpoint (tile API):

```text
{TILE_BASE_URL}/{layer}/{z}/{x}/{y}.png?appid={API_KEY}
```

Default `TILE_BASE_URL` di app:

```text
https://tile.openweathermap.org/map
```

Contoh:

```text
https://tile.openweathermap.org/map/precipitation_new/{z}/{x}/{y}.png?appid=YOUR_KEY
```

### API key

- API key wajib dikirim sebagai query param `appid`.
- API key diambil dari `BuildConfig.WEATHER_API_KEY`.

### Rate limit (ringkas)

- Batas request mengikuti plan akun.
- FAQ OpenWeather menyebut error 429 jika melewati batas (contoh free/professional > 60 calls/menit).
- Detail kuota final mengikuti halaman pricing + subscription di dashboard akun.

### Attribution

Untuk layer tile OpenWeather, sertakan attribution minimal:

```text
© OpenWeatherMap
```

## 4) Konfigurasi endpoint terpusat lintas environment

Konfigurasi endpoint disimpan di:

- `app/src/main/java/com/weatherwise/config/OpenWeatherTileConfig.java`

Variabel BuildConfig yang dipakai:

- `WEATHER_API_KEY`
- `OW_TILE_BASE_URL`

Nilai default base URL dapat dioverride via `local.properties`/CI per environment (dev/staging/prod).

## 5) Error code umum untuk handling di app

- `401 Unauthorized`
  - API key tidak dikirim / belum aktif / key salah / akses produk tidak termasuk plan.
  - Handling: validasi konfigurasi key + tampilkan pesan tindakan ke user/admin.
- `429 Too Many Requests`
  - Kuota request per menit/bulan terlampaui.
  - Handling: retry dengan backoff, cache tile/data, turunkan frekuensi polling.
- `5xx (500/502/503/504)`
  - Gangguan server/provider.
  - Handling: retry bertahap, fallback state, observability (log + alert).
