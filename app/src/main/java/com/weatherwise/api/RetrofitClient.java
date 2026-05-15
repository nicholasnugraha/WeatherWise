package com.weatherwise.api;

import com.weatherwise.BuildConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.concurrent.TimeUnit;

public class RetrofitClient {

    // ── Base URL — 2 endpoint berbeda di OWM ──────────────────
    private static final String BASE_URL_WEATHER  =
            "https://api.openweathermap.org/data/2.5/";
    private static final String BASE_URL_ONECALL =
            "https://api.openweathermap.org/data/3.0/";
    private static final String BASE_URL_GEOCODING =
            "https://api.openweathermap.org/geo/1.0/";

    private static Retrofit retrofitWeather   = null;
    private static Retrofit retrofitOneCall   = null;
    private static Retrofit retrofitGeocoding = null;

    // ── Singleton OkHttpClient (shared kedua Retrofit) ─────────
    private static OkHttpClient buildOkHttpClient() {
        // Logging hanya aktif saat debug build
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(
                BuildConfig.DEBUG
                        ? HttpLoggingInterceptor.Level.BODY
                        : HttpLoggingInterceptor.Level.NONE
        );

        return new OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                // Interceptor untuk handle error HTTP secara global
                .addInterceptor(chain -> {
                    okhttp3.Response response = chain.proceed(chain.request());
                    if (!response.isSuccessful()) {
                        // Lempar exception agar Repository bisa catch dengan pesan jelas
                        throw new java.io.IOException(
                                "HTTP Error: " + response.code() + " " + response.message()
                        );
                    }
                    return response;
                })
                .build();
    }

    // ── Gson dengan toleransi untuk field null dari OWM ────────
    private static Gson buildGson() {
        return new GsonBuilder()
                .setLenient()                    // toleran terhadap JSON tidak sempurna
                .serializeNulls()
                .create();
    }

    // ── Instance untuk Weather API ─────────────────────────────
    public static Retrofit getWeatherClient() {
        if (retrofitWeather == null) {
            retrofitWeather = new Retrofit.Builder()
                    .baseUrl(BASE_URL_WEATHER)
                    .client(buildOkHttpClient())
                    .addConverterFactory(GsonConverterFactory.create(buildGson()))
                    .build();
        }
        return retrofitWeather;
    }


    // ── Instance khusus One Call 3.0 ──────────────────────────
    public static Retrofit getOneCallClient() {
        if (retrofitOneCall == null) {
            retrofitOneCall = new Retrofit.Builder()
                    .baseUrl(BASE_URL_ONECALL)
                    .client(buildOkHttpClient())
                    .addConverterFactory(GsonConverterFactory.create(buildGson()))
                    .build();
        }
        return retrofitOneCall;
    }

    // ── Instance untuk Geocoding API ───────────────────────────
    public static Retrofit getGeocodingClient() {
        if (retrofitGeocoding == null) {
            retrofitGeocoding = new Retrofit.Builder()
                    .baseUrl(BASE_URL_GEOCODING)
                    .client(buildOkHttpClient())
                    .addConverterFactory(GsonConverterFactory.create(buildGson()))
                    .build();
        }
        return retrofitGeocoding;
    }

    // ── Service accessor — shortcut ────────────────────────────
    public static WeatherApiService getWeatherService() {
        return getWeatherClient().create(WeatherApiService.class);
    }

    public static WeatherApiService getOneCallService() {
        return getOneCallClient().create(WeatherApiService.class);
    }

    public static WeatherApiService getGeocodingService() {
        return getGeocodingClient().create(WeatherApiService.class);
    }
}