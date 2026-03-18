package com.weatherwise.util;

public class AppConfig {
    // Ganti dengan API key kamu dari openweathermap.org
    public static final String OWM_API_KEY = "aa244082d13065ab701726681a9bbc7a";

    public static final String BASE_URL     = "https://api.openweathermap.org/data/2.5";
    public static final String GEO_URL      = "https://api.openweathermap.org/geo/1.0";
    public static final String TILE_URL     = "https://tile.openweathermap.org/map";

    public static final int    TIMEOUT_SEC  = 10;
    public static final String DEFAULT_UNIT = "metric"; // metric = Celsius
}
