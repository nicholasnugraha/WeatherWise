package com.weatherwise.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppConfig {
    private static final Properties props = new Properties();

    static {
        try (InputStream is = AppConfig.class
                .getResourceAsStream("/config.properties")) {
            if (is != null) {
                props.load(is);
            } else {
                System.err.println("⚠️ config.properties tidak ditemukan!");
            }
        } catch (IOException e) {
            System.err.println("Gagal memuat config.properties: " + e.getMessage());
        }
    }

    public static String getApiKey() {
        String key = props.getProperty("owm.api.key", "");
        if (key.isEmpty() || key.equals("MASUKKAN_API_KEY_BARU_KAMU_DI_SINI")) {
            System.err.println("⚠️ OWM API Key belum diisi!");
        }
        return key;
    }

    // ✅ BARU — MapTiler API Key
    public static String getMaptilerApiKey() {
        String key = props.getProperty("maptiler.api.key", "");
        if (key.isEmpty() || key.equals("ISI_API_KEY_MAPTILER_KAMU_DI_SINI")) {
            System.err.println("⚠️ MapTiler API Key belum diisi! Daftar di https://cloud.maptiler.com/");
        }
        return key;
    }

    // ── URL Constants ──────────────────────────────────────────
    public static final String BASE_URL     = "https://api.openweathermap.org/data/2.5";
    public static final String ONE_CALL_URL = "https://api.openweathermap.org/data/3.0/onecall";
    public static final String GEO_URL      = "https://api.openweathermap.org/geo/1.0";
    public static final String TILE_URL     = "https://tile.openweathermap.org/map";
    public static final int    TIMEOUT_SEC  = 10;
    public static final String DEFAULT_UNIT = "metric";
}