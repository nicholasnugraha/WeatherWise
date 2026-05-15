package com.weatherwise.api;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.weatherwise.model.CurrentWeather;
import com.weatherwise.model.CurrentWeatherResponse;
import com.weatherwise.model.GeocodingResponse;
import com.weatherwise.model.OneCallResponse;
import com.weatherwise.service.WeatherParser;
import com.weatherwise.util.AppConstants;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WeatherRepository {

    private static final String TAG = "WeatherRepository";

    private final WeatherApiService weatherService;
    private final WeatherApiService oneCallService;
    private final WeatherApiService geocodingService;

    // ── LiveData yang diobservasi ViewModel ────────────────────
    private final MutableLiveData<CurrentWeather> _currentWeather = new MutableLiveData<>();
    private final MutableLiveData<OneCallResponse> _oneCallData   = new MutableLiveData<>();
    private final MutableLiveData<Boolean>  _isLoading            = new MutableLiveData<>(false);
    private final MutableLiveData<String>   _errorMessage         = new MutableLiveData<>();
    private final MutableLiveData<String>   _currentCityName      = new MutableLiveData<>("");

    // Immutable untuk ViewModel
    public LiveData<CurrentWeather>  currentWeather = _currentWeather;
    public LiveData<OneCallResponse> oneCallData    = _oneCallData;
    public LiveData<Boolean>         isLoading      = _isLoading;
    public LiveData<String>          errorMessage   = _errorMessage;
    public LiveData<String>          currentCityName = _currentCityName;

    // Simpan koordinat terakhir untuk refresh
    private double lastLat = 0;
    private double lastLon = 0;
    private String units = AppConstants.UNITS;

    public WeatherRepository() {
        weatherService   = RetrofitClient.getWeatherService();
        oneCallService   = RetrofitClient.getOneCallService();
        geocodingService = RetrofitClient.getGeocodingService();
    }

    // ──────────────────────────────────────────────────────────
    // PUBLIC: Search berdasarkan nama kota
    // Alur: geocode → getCurrentWeather → getOneCall
    // ──────────────────────────────────────────────────────────
    public void fetchWeatherByCity(String cityName) {
        _isLoading.setValue(true);
        _errorMessage.setValue(null);

        geocodingService.geocodeCity(
                cityName,
                5,
                AppConstants.API_KEY
        ).enqueue(new Callback<List<GeocodingResponse>>() {

            @Override
            public void onResponse(Call<List<GeocodingResponse>> call,
                                   Response<List<GeocodingResponse>> response) {
                if (response.isSuccessful()
                        && response.body() != null
                        && !response.body().isEmpty()) {

                    GeocodingResponse geo = response.body().get(0);
                    lastLat = geo.lat;
                    lastLon = geo.lon;

                    // Simpan nama kota untuk ditampilkan di UI
                    _currentCityName.setValue(geo.getDisplayName());

                    // Lanjut fetch cuaca dengan koordinat hasil geocode
                    fetchWeatherByCoord(geo.lat, geo.lon);

                } else {
                    _isLoading.setValue(false);
                    _errorMessage.setValue(
                            "Kota \"" + cityName + "\" tidak ditemukan. " +
                                    "Coba periksa ejaan atau gunakan nama dalam Bahasa Inggris."
                    );
                }
            }

            @Override
            public void onFailure(Call<List<GeocodingResponse>> call, Throwable t) {
                _isLoading.setValue(false);
                _errorMessage.setValue(handleNetworkError(t));
                Log.e(TAG, "Geocoding gagal: " + t.getMessage(), t);
            }
        });
    }

    // ──────────────────────────────────────────────────────────
    // PUBLIC: Search berdasarkan koordinat GPS
    // ──────────────────────────────────────────────────────────
    public void fetchWeatherByCoord(double lat, double lon) {
        _isLoading.setValue(true);
        lastLat = lat;
        lastLon = lon;

        weatherService.getCurrentWeatherByCoord(
                lat, lon,
                AppConstants.API_KEY,
                units,
                AppConstants.LANG
        ).enqueue(new Callback<CurrentWeatherResponse>() {

            @Override
            public void onResponse(Call<CurrentWeatherResponse> call,
                                   Response<CurrentWeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Parse response → model UI
                    CurrentWeather parsed =
                            WeatherParser.parseCurrentWeather(response.body());
                    _currentWeather.setValue(parsed);

                    // Lanjut fetch OneCall untuk hourly + daily + alerts
                    fetchOneCallData(lat, lon);

                } else {
                    _isLoading.setValue(false);
                    _errorMessage.setValue(parseHttpError(response.code()));
                }
            }

            @Override
            public void onFailure(Call<CurrentWeatherResponse> call, Throwable t) {
                _isLoading.setValue(false);
                _errorMessage.setValue(handleNetworkError(t));
                Log.e(TAG, "getCurrentWeather gagal: " + t.getMessage(), t);
            }
        });
    }

    // ──────────────────────────────────────────────────────────
    // PRIVATE: Fetch One Call (hourly + daily + alerts)
    // Dipanggil setelah getCurrentWeather berhasil
    // ──────────────────────────────────────────────────────────
    private void fetchOneCallData(double lat, double lon) {
        oneCallService.getOneCallData(
                lat, lon,
                AppConstants.API_KEY,
                units,
                AppConstants.LANG,
                "minutely"           // exclude minutely → hemat bandwidth
        ).enqueue(new Callback<OneCallResponse>() {

            @Override
            public void onResponse(Call<OneCallResponse> call,
                                   Response<OneCallResponse> response) {
                // Loading selesai di sini — ini call terakhir
                _isLoading.setValue(false);

                if (response.isSuccessful() && response.body() != null) {
                    _oneCallData.setValue(response.body());
                } else {
                    // OneCall gagal tidak fatal — cuaca utama sudah ada
                    // Cukup log, tidak perlu tampilkan error ke user
                    Log.w(TAG, "OneCall response gagal: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<OneCallResponse> call, Throwable t) {
                _isLoading.setValue(false);
                // OneCall gagal tidak fatal, cuaca utama tetap tampil
                Log.w(TAG, "OneCall request gagal: " + t.getMessage());
            }
        });
    }


    // ──────────────────────────────────────────────────────────
    // PUBLIC: Set unit suhu ("metric" / "imperial")
    // ──────────────────────────────────────────────────────────
    public void setUnits(String newUnits) {
        if (newUnits == null || newUnits.trim().isEmpty()) return;
        units = newUnits.trim();
    }

    public String getUnits() {
        return units;
    }

    // ──────────────────────────────────────────────────────────
    // PUBLIC: Refresh — panggil ulang dengan koordinat terakhir
    // ──────────────────────────────────────────────────────────
    public void refresh() {
        if (lastLat != 0 || lastLon != 0) {
            fetchWeatherByCoord(lastLat, lastLon);
        } else {
            _errorMessage.setValue("Belum ada data untuk di-refresh.");
        }
    }

    // ──────────────────────────────────────────────────────────
    // PUBLIC: Hapus pesan error
    // ──────────────────────────────────────────────────────────
    public void clearError() {
        _errorMessage.setValue(null);
    }

    // ──────────────────────────────────────────────────────────
    // PRIVATE: Helper — terjemahkan HTTP error code → pesan user
    // ──────────────────────────────────────────────────────────
    private String parseHttpError(int code) {
        switch (code) {
            case 401: return "API Key tidak valid. Periksa konfigurasi AppConstants.";
            case 404: return "Data tidak ditemukan untuk lokasi ini.";
            case 429: return "Terlalu banyak permintaan. Coba beberapa saat lagi.";
            case 500:
            case 502:
            case 503: return "Server OpenWeatherMap sedang bermasalah. Coba lagi nanti.";
            default:  return "Terjadi kesalahan (kode " + code + "). Coba lagi.";
        }
    }

    // ──────────────────────────────────────────────────────────
    // PRIVATE: Helper — terjemahkan network error → pesan user
    // ──────────────────────────────────────────────────────────
    private String handleNetworkError(Throwable t) {
        if (t instanceof java.net.UnknownHostException) {
            return "Tidak ada koneksi internet. Periksa jaringanmu.";
        } else if (t instanceof java.net.SocketTimeoutException) {
            return "Koneksi timeout. Jaringan terlalu lambat, coba lagi.";
        } else if (t instanceof java.io.IOException) {
            return "Gagal terhubung ke server: " + t.getMessage();
        }
        return "Terjadi kesalahan tidak terduga: " + t.getMessage();
    }
}