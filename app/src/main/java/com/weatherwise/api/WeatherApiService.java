package com.weatherwise.api;

import com.weatherwise.model.CurrentWeatherResponse;
import com.weatherwise.model.OneCallResponse;
import com.weatherwise.model.GeocodingResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherApiService {

    // ── 1. Cuaca saat ini berdasarkan nama kota ────────────────
    @GET("weather")
    Call<CurrentWeatherResponse> getCurrentWeatherByCity(
            @Query("q")     String cityName,
            @Query("appid") String apiKey,
            @Query("units") String units,    // "metric" = Celsius
            @Query("lang")  String lang      // "id" = Bahasa Indonesia
    );

    // ── 2. Cuaca saat ini berdasarkan koordinat (GPS) ──────────
    @GET("weather")
    Call<CurrentWeatherResponse> getCurrentWeatherByCoord(
            @Query("lat")   double latitude,
            @Query("lon")   double longitude,
            @Query("appid") String apiKey,
            @Query("units") String units,
            @Query("lang")  String lang
    );

    // ── 3. One Call API — Hourly + Daily + Alerts ──────────────
    // Membutuhkan lat/lon → panggil setelah getCurrentWeather
    @GET("onecall")
    Call<OneCallResponse> getOneCallData(
            @Query("lat")     double latitude,
            @Query("lon")     double longitude,
            @Query("appid")   String apiKey,
            @Query("units")   String units,
            @Query("lang")    String lang,
            @Query("exclude") String exclude  // "minutely" untuk hemat data
    );

    // ── 4. Geocoding — konversi nama kota → koordinat ─────────
    // Endpoint berbeda: geo.openweathermap.org (bukan api.openweathermap.org)
    @GET("direct")
    Call<List<GeocodingResponse>> geocodeCity(
            @Query("q")     String cityName,
            @Query("limit") int    limit,    // ambil 5 kandidat teratas
            @Query("appid") String apiKey
    );

    // ── 5. Reverse Geocoding — koordinat → nama kota ──────────
    @GET("reverse")
    Call<List<GeocodingResponse>> reverseGeocode(
            @Query("lat")   double latitude,
            @Query("lon")   double longitude,
            @Query("limit") int    limit,
            @Query("appid") String apiKey
    );
}