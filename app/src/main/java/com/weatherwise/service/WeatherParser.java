package com.weatherwise.service;

import com.weatherwise.model.CurrentWeather;
import com.weatherwise.model.CurrentWeatherResponse;
import com.weatherwise.model.OneCallResponse;

public class WeatherParser {

    // ── Prevent instantiation — semua method static ────────────
    public WeatherParser() {}

    // ──────────────────────────────────────────────────────────
    // Parse CurrentWeatherResponse → CurrentWeather (model UI)
    // ──────────────────────────────────────────────────────────
    public static CurrentWeather parseCurrentWeather(CurrentWeatherResponse r) {
        CurrentWeather w = new CurrentWeather();

        // Info kota
        w.cityName  = r.cityName != null ? r.cityName : "Unknown";
        w.country   = (r.sys != null && r.sys.country != null) ? r.sys.country : "";
        w.timestamp = r.timestamp;

        // Koordinat — dibutuhkan untuk One Call
        if (r.coord != null) {
            w.lat = r.coord.lat;
            w.lon = r.coord.lon;
        }

        // Suhu
        if (r.main != null) {
            w.temperature = r.main.temp;
            w.feelsLike   = r.main.feelsLike;
            w.tempMin     = r.main.tempMin;
            w.tempMax     = r.main.tempMax;
            w.pressure    = r.main.pressure;
            w.humidity    = r.main.humidity;
        }

        // Kondisi cuaca — ambil elemen pertama dari array
        if (r.weather != null && !r.weather.isEmpty()) {
            CurrentWeatherResponse.Weather cond = r.weather.get(0);
            w.conditionId   = cond.id;
            w.condition     = cond.description != null
                    ? capitalize(cond.description) : "";
            w.conditionMain = cond.main != null ? cond.main : "";
            w.conditionIcon = cond.icon != null ? cond.icon : "01d";
        }

        // Angin
        if (r.wind != null) {
            w.windSpeed = r.wind.speed;
            w.windDeg   = r.wind.deg;
            w.windGust  = r.wind.gust;
        }

        // Visibilitas (meter → tetap meter, konversi di UI)
        w.visibility = r.visibility;

        // Tutupan awan (persen)
        if (r.clouds != null) {
            w.cloudiness = r.clouds.all;
        }

        // Sunrise & Sunset
        if (r.sys != null) {
            w.sunrise = r.sys.sunrise;
            w.sunset  = r.sys.sunset;
        }

        // UV Index — belum tersedia di /weather endpoint
        // akan diupdate nanti dari OneCallResponse.current.uvi
        w.uvIndex = 0.0;

        return w;
    }

    // ──────────────────────────────────────────────────────────
    // Enrich CurrentWeather dengan data UV dari OneCall
    // Dipanggil setelah OneCallResponse tersedia
    // ──────────────────────────────────────────────────────────
    public static CurrentWeather enrichWithOneCall(
            CurrentWeather existing,
            OneCallResponse oneCall) {

        if (oneCall == null || existing == null) return existing;

        // UV Index dari current OneCall
        if (oneCall.current != null) {
            existing.uvIndex = oneCall.current.uvi;
        }

        return existing;
    }

    // ──────────────────────────────────────────────────────────
    // Arah angin dalam derajat → nama arah mata angin
    // Contoh: 45° → "TL" (Timur Laut)
    // ──────────────────────────────────────────────────────────
    public static String degreesToWindDirection(int degrees) {
        // Normalisasi 0-359
        int d = ((degrees % 360) + 360) % 360;

        if (d >= 337 || d < 23)  return "U";   // Utara
        if (d < 68)               return "TL";  // Timur Laut
        if (d < 113)              return "T";   // Timur
        if (d < 158)              return "TG";  // Tenggara
        if (d < 203)              return "S";   // Selatan
        if (d < 248)              return "BD";  // Barat Daya
        if (d < 293)              return "B";   // Barat
        if (d < 338)              return "BL";  // Barat Laut
        return "U";
    }

    // ──────────────────────────────────────────────────────────
    // UV Index angka → label + warna rekomendasi
    // ──────────────────────────────────────────────────────────
    public static UvInfo parseUvIndex(double uvi) {
        if (uvi < 3)  return new UvInfo("Rendah",        "#4CAF50", "Aman beraktivitas di luar");
        if (uvi < 6)  return new UvInfo("Sedang",        "#FFC107", "Gunakan tabir surya SPF 30+");
        if (uvi < 8)  return new UvInfo("Tinggi",        "#FF9800", "Hindari paparan siang hari");
        if (uvi < 11) return new UvInfo("Sangat Tinggi", "#F44336", "Kurangi waktu di luar");
        return            new UvInfo("Ekstrem",          "#9C27B0", "Hindari keluar rumah");
    }

    // ──────────────────────────────────────────────────────────
    // Visibilitas meter → label deskriptif
    // ──────────────────────────────────────────────────────────
    public static String parseVisibility(int meters) {
        if (meters >= 10000) return "Sangat baik";
        if (meters >= 5000)  return "Baik";
        if (meters >= 2000)  return "Cukup";
        if (meters >= 1000)  return "Buruk";
        return                      "Sangat buruk";
    }

    // ──────────────────────────────────────────────────────────
    // Kondisi cuaca id → apakah sedang hujan?
    // Berguna untuk mengubah warna UI secara kondisional
    // ──────────────────────────────────────────────────────────
    public static boolean isRaining(int conditionId) {
        // OWM condition codes: 2xx = petir, 3xx = drizzle,
        // 5xx = hujan, 6xx = salju
        return (conditionId >= 200 && conditionId < 700);
    }

    // ──────────────────────────────────────────────────────────
    // Kondisi cuaca id → apakah siang atau malam?
    // Digunakan untuk memilih icon "d" atau "n"
    // ──────────────────────────────────────────────────────────
    public static boolean isDaytime(long currentTime,
                                    long sunrise,
                                    long sunset) {
        return currentTime >= sunrise && currentTime < sunset;
    }

    // ──────────────────────────────────────────────────────────
    // Tekanan udara → label deskriptif
    // ──────────────────────────────────────────────────────────
    public static String parsePressure(int hPa) {
        if (hPa < 1000) return "Rendah";
        if (hPa < 1013) return "Normal bawah";
        if (hPa < 1020) return "Normal";
        if (hPa < 1030) return "Normal atas";
        return                 "Tinggi";
    }

    // ──────────────────────────────────────────────────────────
    // PRIVATE: Capitalize huruf pertama setiap kata
    // Contoh: "hujan ringan" → "Hujan Ringan"
    // ──────────────────────────────────────────────────────────
    private static String capitalize(String text) {
        if (text == null || text.isEmpty()) return text;
        String[] words = text.split(" ");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1))
                        .append(" ");
            }
        }
        return result.toString().trim();
    }

    // ──────────────────────────────────────────────────────────
    // Inner class: hasil parseUvIndex
    // ──────────────────────────────────────────────────────────
    public static class UvInfo {
        public final String label;
        public final String colorHex;
        public final String recommendation;

        public UvInfo(String label, String colorHex, String recommendation) {
            this.label          = label;
            this.colorHex       = colorHex;
            this.recommendation = recommendation;
        }
    }
}