package com.weatherwise.repository;

import com.weatherwise.api.RetrofitClient;
import com.weatherwise.model.CurrentWeather;
import com.weatherwise.model.ForecastDay;
import com.weatherwise.model.OneCallResponse;
import com.weatherwise.service.WeatherParser;
import com.weatherwise.service.GeocodingService;
import com.weatherwise.util.AppConfig;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

public class WeatherRepository {

    private final WeatherApiService oneCallApi;
    private final WeatherApiService legacyApi;
    private final WeatherParser     parser;
    private final GeocodingService  geocoder;

    public WeatherRepository() {
        oneCallApi = RetrofitClient.getOneCallInstance().create(WeatherApiService.class);
        legacyApi  = RetrofitClient.getLegacyInstance().create(WeatherApiService.class);
        parser     = new WeatherParser();
        geocoder   = new GeocodingService();
    }

    // ── Geocoding: nama kota → [lat, lon] ─────────────────────
    public void geocodeCity(String city, Callback<double[]> callback) {
        Call<ResponseBody> call = legacyApi.getWeatherByCity(
                city, AppConfig.DEFAULT_UNIT, AppConfig.getApiKey()
        );
        call.enqueue(new retrofit2.Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> c, Response<ResponseBody> r) {
                try {
                    String json = r.body().string();
                    double[] coords = geocoder.extractCoords(json); // parsing lat/lon dari /weather
                    callback.onSuccess(coords);
                } catch (Exception e) {
                    callback.onError("Kota tidak ditemukan: " + e.getMessage());
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> c, Throwable t) {
                callback.onError("Koneksi gagal: " + t.getMessage());
            }
        });
    }

    // ── One Call API 3.0 ───────────────────────────────────────
    public void fetchOneCall(double lat, double lon, Callback<OneCallResponse> callback) {
        Call<ResponseBody> call = oneCallApi.getOneCallData(
                lat, lon, AppConfig.DEFAULT_UNIT, AppConfig.getApiKey()
        );
        call.enqueue(new retrofit2.Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> c, Response<ResponseBody> r) {
                try {
                    if (r.code() == 401) { callback.onError("API Key tidak valid!"); return; }
                    if (r.code() == 404) { callback.onError("Data tidak ditemukan!"); return; }
                    if (!r.isSuccessful()) { callback.onError("Error HTTP: " + r.code()); return; }

                    String json = r.body().string();
                    // Gunakan parser lama dari WeatherService desktop
                    OneCallResponse result = parser.parseOneCallResponse(json);
                    callback.onSuccess(result);
                } catch (Exception e) {
                    callback.onError("Gagal parse data: " + e.getMessage());
                }

                // Di dalam onResponse fetchOneCallData:
                if (response.isSuccessful() && response.body() != null) {
                    _oneCallData.setValue(response.body());

                    // Enrich CurrentWeather dengan UV Index dari OneCall
                    CurrentWeather existing = _currentWeather.getValue();
                    if (existing != null) {
                        CurrentWeather enriched = WeatherParser.enrichWithOneCall(
                                existing, response.body()
                        );
                        _currentWeather.setValue(enriched); // trigger UI update
                    }
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> c, Throwable t) {
                callback.onError("Koneksi gagal: " + t.getMessage());
            }
        });
    }

    // ── Adapter — OneCallResponse → model lama ─────────────────
    public void adaptToCurrentWeather(OneCallResponse ocr, Callback<CurrentWeather> callback) {
        try {
            callback.onSuccess(parser.adaptToCurrent(ocr));
        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    }

    public void adaptToForecast(OneCallResponse ocr, Callback<List<ForecastDay>> callback) {
        try {
            callback.onSuccess(parser.adaptToForecast(ocr));
        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    }

    // ── Generic Callback interface ─────────────────────────────
    public interface Callback<T> {
        void onSuccess(T data);
        void onError(String message);
    }
}