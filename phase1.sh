# 1. Pastikan Anda berada di branch yang benar
git checkout android-stable

# 2. Tarik perubahan terbaru jika ada (opsional, jika Anda bekerja di mesin lain)
git pull origin android-stable

# 3. Salin perubahan yang telah saya buat dari direktori sementara 
# (Atau, jika Anda menjalankan ini di mesin yang sama, cukup lakukan git status)
git add -A

# 4. Commit dengan pesan yang terstruktur
git commit -m "Phase 1: Migrate to Kotlin Coroutines, StateFlow, and fix API endpoint for free tier compatibility

- Replaced deprecated One Call 2.5 with /forecast endpoint for free-tier compatibility
- Migrated WeatherViewModel and WeatherRepository to Kotlin with StateFlow and Coroutines
- Added Room Database for offline caching of last known weather data
- Updated UI components to use collectAsStateWithLifecycle
- Added KSP plugin and Lifecycle Compose dependencies to build.gradle"

# 5. Push ke branch android-stable
git push origin android-stable

# 6. Buat Pull Request (menggunakan GitHub CLI)
gh pr create --title "Phase 1: Kotlin Migration, StateFlow, and API Fix" --body "## Phase 1 Implementation Complete\n\n- API Fix: Replaced deprecated One Call 2.5 with /forecast endpoint.\n- Kotlin Migration: Migrated ViewModel & Repository to StateFlow + Coroutines.\n- Offline Caching: Added Room Database for instant loading.\n- UI Updates: Updated Compose screens to use collectAsStateWithLifecycle." --base android-stable
