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
                System.err.println("⚠️ config.properties tidak ditemukan! Buat file src/main/resources/config.properties");
            }
        } catch (IOException e) {
            System.err.println("Gagal memuat config.properties: " + e.getMessage());
        }
    }

    /**
     * Ambil API Key dari config.properties.
     * Isi owm.api.key di file src/main/resources/config.properties
     */
    public static String getApiKey() {
        String key = props.getProperty("owm.api.key", "");
        if (key.isEmpty() || key.equals("MASUKKAN_API_KEY_BARU_KAMU_DI_SINI")) {
            System.err.println("⚠️ API Key belum diisi! Isi owm.api.key di config.properties");
        }
        return key;
    }

    public static final String BASE_URL     = "https://api.openweathermap.org/data/2.5";
    public static final String GEO_URL      = "https://api.openweathermap.org/geo/1.0";
    public static final String TILE_URL     = "https://tile.openweathermap.org/map";
    public static final int    TIMEOUT_SEC  = 10;
    public static final String DEFAULT_UNIT = "metric"; // metric = Celsius
}
