package com.weatherwise.util;

import com.weatherwise.BuildConfig;

public class AppConstants {

    // API Key — diambil dari BuildConfig (bukan hardcode)
    public static final String API_KEY = BuildConfig.WEATHER_API_KEY;

    // Default settings
    public static final String UNITS = "metric";   // Celsius
    public static final String LANG  = "id";       // Bahasa Indonesia

    // Default kota saat pertama buka app
    public static final String DEFAULT_CITY = "Jakarta";

    // Prevent instantiation
    private AppConstants() {}
}