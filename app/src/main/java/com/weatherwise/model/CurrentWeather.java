package com.weatherwise.model;

public class CurrentWeather {

    // ── Identitas kota ─────────────────────────────────────────
    public String cityName      = "";
    public String country       = "";
    public long   timestamp     = 0L;
    public double lat           = 0.0;
    public double lon           = 0.0;

    // ── Suhu ───────────────────────────────────────────────────
    public double temperature   = 0.0;
    public double feelsLike     = 0.0;
    public double tempMin       = 0.0;
    public double tempMax       = 0.0;

    // ── Kondisi cuaca ──────────────────────────────────────────
    public int    conditionId   = 800;   // default: cerah
    public String condition     = "";    // "Hujan Ringan"
    public String conditionMain = "";    // "Rain"
    public String conditionIcon = "01d"; // "10d", "04n", dst.

    // ── Atmosfer ───────────────────────────────────────────────
    public int    humidity      = 0;     // persen
    public int    pressure      = 1013;  // hPa
    public int    visibility    = 10000; // meter
    public int    cloudiness    = 0;     // persen
    public double uvIndex       = 0.0;

    // ── Angin ──────────────────────────────────────────────────
    public double windSpeed     = 0.0;   // m/s
    public int    windDeg       = 0;     // derajat
    public double windGust      = 0.0;   // m/s

    // ── Matahari ───────────────────────────────────────────────
    public long   sunrise       = 0L;    // Unix timestamp
    public long   sunset        = 0L;    // Unix timestamp

    // ── Helper: arah angin teks ────────────────────────────────
    public String getWindDirection() {
        return com.weatherwise.service.WeatherParser
                .degreesToWindDirection(windDeg);
    }

    // ── Helper: icon URL lengkap ───────────────────────────────
    public String getIconUrl() {
        return "https://openweathermap.org/img/wn/"
                + conditionIcon + "@2x.png";
    }

    // ── Helper: icon URL resolusi besar (untuk MainWeatherCard) ─
    public String getIconUrlLarge() {
        return "https://openweathermap.org/img/wn/"
                + conditionIcon + "@4x.png";
    }
}