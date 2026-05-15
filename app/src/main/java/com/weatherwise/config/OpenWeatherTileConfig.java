package com.weatherwise.config;

import com.weatherwise.BuildConfig;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Konfigurasi terpusat endpoint OpenWeather tiles.
 * Bisa diganti lintas environment melalui BuildConfig/local.properties.
 */
public final class OpenWeatherTileConfig {

    public static final String API_KEY = BuildConfig.WEATHER_API_KEY;

    public static final String TILE_BASE_URL = BuildConfig.OW_TILE_BASE_URL;

    // Layer kandidat yang disepakati untuk aplikasi.
    public static final String LAYER_PRECIPITATION = "precipitation_new";
    public static final String LAYER_CLOUDS = "clouds_new";
    public static final String LAYER_PRESSURE = "pressure_new";
    public static final String LAYER_WIND = "wind_new";

    // Layer default yang diposisikan sebagai "radar hujan".
    public static final String RAIN_RADAR_LAYER = LAYER_PRECIPITATION;

    private OpenWeatherTileConfig() {
        // No instance.
    }

    public static String tileUrl(String layer) {
        return TILE_BASE_URL + "/" + layer + "/{z}/{x}/{y}.png?appid=" + API_KEY;
    }

    public static Map<String, String> supportedLayers() {
        Map<String, String> layers = new LinkedHashMap<>();
        layers.put("precipitation", tileUrl(LAYER_PRECIPITATION));
        layers.put("clouds", tileUrl(LAYER_CLOUDS));
        layers.put("pressure", tileUrl(LAYER_PRESSURE));
        layers.put("wind", tileUrl(LAYER_WIND));
        return layers;
    }
}
