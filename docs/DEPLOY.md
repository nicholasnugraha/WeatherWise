# Deployment: WeatherWise Web App ke Vercel

Dokumen ini menjelaskan cara deploy branch `web-app` ke Vercel.

## Prerequisites

- Akun Vercel (free tier cukup)
- Akses ke repo `github.com/nicholasnugraha/WeatherWise`
- (Opsional) Vercel CLI untuk deploy manual

## Cara 1 — Vercel GitHub Integration (Recommended, Auto-Deploy)

1. Buka https://vercel.com/new
2. **Import Git Repository** → pilih `nicholasnugraha/WeatherWise`
3. Konfigurasi project:
   - **Project Name:** `weatherwise-web` (atau sesuai keinginan)
   - **Framework Preset:** `Other`
   - **Root Directory:** `./` (default)
   - **Branch:** `web-app` (otomatis deploy dari branch ini)
   - **Build Command:** `bash vercel-build.sh` (otomatis terdeteksi dari `vercel.json`)
   - **Output Directory:** `build/web` (otomatis terdeteksi dari `vercel.json`)
4. **Environment Variables:** (kosongkan dulu, tambahkan nanti kalau perlu Edge Function proxy)
5. Klik **Deploy**

Build pertama butuh ~3-5 menit (download Flutter SDK + build). Build selanjutnya ~1-2 menit (cached).

URL production: `https://weatherwise-web.vercel.app` (atau subdomain sesuai project name)

Setiap push ke branch `web-app` akan trigger auto-deploy ke preview URL.
Merge ke production branch (default `main`) → production URL update.

## Cara 2 — Vercel CLI (Manual)

```bash
# Install Vercel CLI
npm install -g vercel

# Login (akan buka browser)
vercel login

# Deploy dari project root (pertama kali — akan buat project baru)
cd /tmp/weatherwise_flutter
vercel

# Subsequent deploys
vercel --prod   # deploy ke production
vercel          # deploy ke preview URL
```

## Cache Strategy

Vercel's `vercel.json` **does not support a top-level `cache` property** for
arbitrary filesystem paths (only built-in paths like `node_modules` are cached
automatically). Practical implications:

- **Flutter SDK download**: ~700MB compressed, ~1-2 minutes per cold build.
  `vercel-build.sh` uses the precompiled tarball from `storage.googleapis.com`
  for the fastest install. Subsequent builds in the same deployment will
  re-download unless you configure [Vercel build cache via the CLI's
  `--cache` flag](https://vercel.com/docs/cli/build#caching).
- **Dart pub cache**: kept at `$HOME/.pub-cache` (outside project tree) so it
  doesn't bloat the deployment, but it's not cached between builds.
- **Build artifacts**: regenerated every build (~30s for build_runner).

If cold build times become a problem, options are:
1. Upgrade to Vercel Pro for longer build timeout + larger cache
2. Use a pre-built Docker image with Flutter pre-installed (custom build
   environment — Enterprise tier)

## Environment Variables

Untuk sekarang tidak ada env var yang dibutuhkan. Web app baca `WEATHER_API_KEY` dari `assets/.env` (bundled di APK/web). 

⚠️ **Security note:** API key di web bundle bisa di-extract oleh siapa pun via browser DevTools. Untuk production yang serius, setup backend proxy. Lihat `docs/prd/web-app.md` Section 10.3 untuk instruksi.

## Custom Domain

1. Vercel Dashboard → Project → Settings → Domains
2. Tambahkan domain (misal `weatherwise.nicholasnugraha.dev`)
3. Ikuti instruksi DNS propagation (biasanya 5-30 menit)

## Troubleshooting

**Build gagal dengan "command not found: flutter"**
- Vercel mungkin menjalankan `buildCommand` di shell yang bukan bash
- Pastikan `vercel.json` ada di root project dan `buildCommand` dimulai dengan `bash`

**Build gagal dengan "Java not found"**
- Beberapa plugin Flutter butuh Java untuk codegen
- Vercel sudah preinstall Java 17 di environment build

**Build lambat di first deploy**
- Normal, butuh download Flutter SDK (~500MB)
- Subsequent builds pakai cache

**Web app tidak load setelah deploy**
- Cek Vercel deployment logs
- Pastikan `outputDirectory: build/web` benar
- Coba buka DevTools → Console untuk error spesifik

## Branch Strategy

- `web-app` branch = preview/staging deploy (auto-deploy setiap push)
- `main` / `master` = production deploy (setelah merge)

Untuk sekarang, branch `flutter` (Android) dan `web-app` (web) terpisah. Nanti bisa di-merge via PR.