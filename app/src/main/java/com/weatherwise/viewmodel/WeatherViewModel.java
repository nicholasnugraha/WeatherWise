package com.weatherwise.viewmodel;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.weatherwise.api.WeatherRepository;
import com.weatherwise.model.CurrentWeather;
import com.weatherwise.model.OneCallResponse;
import com.weatherwise.util.AppConstants;

public class WeatherViewModel extends AndroidViewModel {

    private static final String PREF_NAME       = "weatherwise_prefs";
    private static final String PREF_LAST_CITY  = "last_city";
    private static final String PREF_UNITS      = "units";

    private final WeatherRepository repository;
    private final SharedPreferences prefs;

    // ── LiveData yang diekspos ke UI ───────────────────────────
    public final LiveData<CurrentWeather>  currentWeather;
    public final LiveData<OneCallResponse> oneCallData;
    public final LiveData<Boolean>         isLoading;
    public final LiveData<String>          errorMessage;
    public final LiveData<String>          cityName;

    // MediatorLiveData — gabungkan currentWeather + oneCallData
    // agar UI tahu kapan kedua data sudah siap
    private final MediatorLiveData<Boolean> _isDataReady = new MediatorLiveData<>();
    public  final LiveData<Boolean>          isDataReady  = _isDataReady;

    public WeatherViewModel(@NonNull Application application) {
        super(application);

        repository = new WeatherRepository();
        prefs      = application.getSharedPreferences(PREF_NAME,
                android.content.Context.MODE_PRIVATE);
        repository.setUnits(prefs.getString(PREF_UNITS, "metric"));

        // Sambungkan LiveData dari Repository
        currentWeather = repository.currentWeather;
        oneCallData    = repository.oneCallData;
        isLoading      = repository.isLoading;
        errorMessage   = repository.errorMessage;
        cityName       = repository.currentCityName;

        // MediatorLiveData: isDataReady = true jika keduanya tidak null
        _isDataReady.addSource(currentWeather, cw ->
                _isDataReady.setValue(cw != null && oneCallData.getValue() != null)
        );
        _isDataReady.addSource(oneCallData, oc ->
                _isDataReady.setValue(currentWeather.getValue() != null && oc != null)
        );

        // Auto-load kota terakhir saat ViewModel dibuat
        loadLastCity();
    }

    // ──────────────────────────────────────────────────────────
    // PUBLIC: Cari cuaca berdasarkan nama kota
    // ──────────────────────────────────────────────────────────
    public void searchByCity(String cityName) {
        if (cityName == null || cityName.trim().isEmpty()) return;

        String trimmed = cityName.trim();
        saveLastCity(trimmed); // simpan untuk sesi berikutnya
        repository.fetchWeatherByCity(trimmed);
    }

    // ──────────────────────────────────────────────────────────
    // PUBLIC: Cari cuaca berdasarkan koordinat GPS
    // ──────────────────────────────────────────────────────────
    public void searchByCoord(double lat, double lon) {
        repository.fetchWeatherByCoord(lat, lon);
    }

    // ──────────────────────────────────────────────────────────
    // PUBLIC: Refresh data dengan koordinat terakhir
    // Dipanggil oleh pull-to-refresh di HomeScreen
    // ──────────────────────────────────────────────────────────
    public void refresh() {
        repository.refresh();
    }

    // ──────────────────────────────────────────────────────────
    // PUBLIC: Hapus pesan error
    // Dipanggil saat user dismiss ErrorBanner
    // ──────────────────────────────────────────────────────────
    public void clearError() {
        repository.clearError();
    }

    // ──────────────────────────────────────────────────────────
    // PUBLIC: Toggle satuan suhu Celsius ↔ Fahrenheit
    // ──────────────────────────────────────────────────────────
    public void toggleUnits() {
        String current = prefs.getString(PREF_UNITS, "metric");
        String next    = current.equals("metric") ? "imperial" : "metric";
        prefs.edit().putString(PREF_UNITS, next).apply();

        repository.setUnits(next);

        // Refresh data dengan satuan baru
        repository.refresh();
    }

    // ──────────────────────────────────────────────────────────
    // PUBLIC: Getter satuan saat ini
    // ──────────────────────────────────────────────────────────
    public boolean isCelsius() {
        return prefs.getString(PREF_UNITS, "metric").equals("metric");
    }

    // ──────────────────────────────────────────────────────────
    // PUBLIC: Ambil alert cuaca dari OneCallResponse (bisa null)
    // ──────────────────────────────────────────────────────────
    public java.util.List<OneCallResponse.AlertData> getAlerts() {
        OneCallResponse oneCall = oneCallData.getValue();
        if (oneCall == null || oneCall.alerts == null) {
            return new java.util.ArrayList<>();
        }
        return oneCall.alerts;
    }

    // ──────────────────────────────────────────────────────────
    // PUBLIC: Ambil data daily forecast (maks 7 hari)
    // Dipanggil oleh ForecastScreen
    // ──────────────────────────────────────────────────────────
    public java.util.List<OneCallResponse.DailyData> getDailyForecast() {
        OneCallResponse oneCall = oneCallData.getValue();
        if (oneCall == null || oneCall.daily == null) {
            return new java.util.ArrayList<>();
        }
        // Lewati hari ini (index 0), tampilkan 7 hari ke depan
        return oneCall.daily.size() > 1
                ? oneCall.daily.subList(1, Math.min(8, oneCall.daily.size()))
                : new java.util.ArrayList<>();
    }

    // ──────────────────────────────────────────────────────────
    // PRIVATE: Load kota terakhir dari SharedPreferences
    // ──────────────────────────────────────────────────────────
    private void loadLastCity() {
        String lastCity = prefs.getString(PREF_LAST_CITY, AppConstants.DEFAULT_CITY);
        repository.fetchWeatherByCity(lastCity);
    }

    // ──────────────────────────────────────────────────────────
    // PRIVATE: Simpan kota terakhir ke SharedPreferences
    // ──────────────────────────────────────────────────────────
    private void saveLastCity(String cityName) {
        prefs.edit().putString(PREF_LAST_CITY, cityName).apply();
    }
}