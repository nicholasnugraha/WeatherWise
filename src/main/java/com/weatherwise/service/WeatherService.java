package com.weatherwise.service;

import com.weatherwise.model.CurrentWeather;
import com.weatherwise.model.ForecastDay;
import com.weatherwise.util.AppConfig;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WeatherService {

    // ── Fetch Current Weather ──────────────────────────────────
    public CurrentWeather getCurrentWeather(double lat, double lon) throws Exception {
        String urlStr = AppConfig.BASE_URL + "/weather"
                + "?lat=" + lat
                + "&lon=" + lon
                + "&units=" + AppConfig.DEFAULT_UNIT
                + "&appid=" + AppConfig.OWM_API_KEY;

        String response = fetchUrl(urlStr);
        return parseCurrentWeather(new JSONObject(response));
    }

    public CurrentWeather getCurrentWeatherByCity(String cityName) throws Exception {
        String encoded = java.net.URLEncoder.encode(cityName, "UTF-8");
        String urlStr = AppConfig.BASE_URL + "/weather"
                + "?q=" + encoded
                + "&units=" + AppConfig.DEFAULT_UNIT
                + "&appid=" + AppConfig.OWM_API_KEY;

        String response = fetchUrl(urlStr);
        return parseCurrentWeather(new JSONObject(response));
    }

    // ── Fetch 5-Day Forecast (setiap 3 jam → dikelompokkan per hari) ──
    public List<ForecastDay> getForecast(double lat, double lon) throws Exception {
        String urlStr = AppConfig.BASE_URL + "/forecast"
                + "?lat=" + lat
                + "&lon=" + lon
                + "&units=" + AppConfig.DEFAULT_UNIT
                + "&appid=" + AppConfig.OWM_API_KEY;

        String response = fetchUrl(urlStr);
        return parseForecast(new JSONObject(response));
    }

    // ── Parser: Current Weather ────────────────────────────────
    private CurrentWeather parseCurrentWeather(JSONObject json) {
        CurrentWeather w = new CurrentWeather();

        w.setCityName(json.getString("name"));
        w.setCountry(json.getJSONObject("sys").getString("country"));
        w.setLatitude(json.getJSONObject("coord").getDouble("lat"));
        w.setLongitude(json.getJSONObject("coord").getDouble("lon"));

        JSONObject main = json.getJSONObject("main");
        w.setTemperature(main.getDouble("temp"));
        w.setFeelsLike(main.getDouble("feels_like"));
        w.setTempMin(main.getDouble("temp_min"));
        w.setTempMax(main.getDouble("temp_max"));
        w.setHumidity(main.getInt("humidity"));
        w.setPressure(main.getInt("pressure"));

        w.setWindSpeed(json.getJSONObject("wind").getDouble("speed"));

        JSONObject weatherObj = json.getJSONArray("weather").getJSONObject(0);
        w.setCondition(weatherObj.getString("description"));
        w.setConditionIcon(weatherObj.getString("icon"));

        return w;
    }

    // ── Parser: Forecast (ambil 1 data per hari = tengah hari) ─
    private List<ForecastDay> parseForecast(JSONObject json) {
        List<ForecastDay> result = new ArrayList<>();
        JSONArray list = json.getJSONArray("list");

        DateTimeFormatter dayFmt = DateTimeFormatter.ofPattern("EEEE", Locale.ENGLISH);
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH);

        java.util.Map<String, JSONObject> dailyMap = new java.util.LinkedHashMap<>();
        for (int i = 0; i < list.length(); i++) {
            JSONObject entry = list.getJSONObject(i);
            String dtTxt = entry.getString("dt_txt");
            String dateKey = dtTxt.substring(0, 10);

            if (dtTxt.contains("12:00:00") || !dailyMap.containsKey(dateKey)) {
                dailyMap.put(dateKey, entry);
            }
        }

        for (java.util.Map.Entry<String, JSONObject> entry : dailyMap.entrySet()) {
            if (result.size() >= 7) {
                break;
            }

            JSONObject data = entry.getValue();
            ForecastDay day = new ForecastDay();

            // Waktu
            long dt = data.getLong("dt");
            Instant instant = Instant.ofEpochSecond(dt);
            ZoneId zone = ZoneId.systemDefault();
            day.setDayName(dayFmt.format(instant.atZone(zone)));
            day.setDate(dateFmt.format(instant.atZone(zone)));

            // Suhu
            JSONObject main = data.getJSONObject("main");
            day.setTempHigh(main.getDouble("temp_max"));
            day.setTempLow(main.getDouble("temp_min"));
            day.setHumidity(main.getInt("humidity"));

            // Angin
            day.setWindSpeed(data.getJSONObject("wind").getDouble("speed"));

            // Kondisi
            JSONObject weatherObj = data.getJSONArray("weather").getJSONObject(0);
            String main_cond = weatherObj.getString("main");        // "Rain"
            String desc = weatherObj.getString("description"); // "light rain"
            String owmIcon = weatherObj.getString("icon");

            day.setCondition(capitalize(main_cond));
            day.setDescription(capitalize(desc)
                    + " · Humidity " + main.getInt("humidity") + "%");
            day.setConditionIcon(owmIcon);

            // ✅ Set iconLiteral & iconColor berdasarkan kode OWM
            day.setIconLiteral(resolveIconLiteral(owmIcon));
            day.setIconColor(resolveIconColor(owmIcon));

            result.add(day);
        }

        return result;
    }

// ── Mapping icon OWM → Ikonli literal ─────────────────────────
    private String resolveIconLiteral(String owmIcon) {
        if (owmIcon == null) {
            return "mdi2w-weather-cloudy";
        }
        return switch (owmIcon.substring(0, 2)) {
            case "01" ->
                "mdi2w-weather-sunny";
            case "02" ->
                "mdi2w-weather-partly-cloudy";
            case "03" ->
                "mdi2w-weather-cloudy";
            case "04" ->
                "mdi2w-weather-cloudy";
            case "09" ->
                "mdi2w-weather-pouring";
            case "10" ->
                "mdi2w-weather-rainy";
            case "11" ->
                "mdi2w-weather-lightning";
            case "13" ->
                "mdi2w-weather-snowy";
            case "50" ->
                "mdi2w-weather-fog";
            default ->
                "mdi2w-weather-cloudy";
        };
    }

// ── Mapping icon OWM → warna hex ──────────────────────────────
    private String resolveIconColor(String owmIcon) {
        if (owmIcon == null) {
            return "#64748b";
        }
        return switch (owmIcon.substring(0, 2)) {
            case "01" ->
                "#f59e0b"; // kuning — cerah
            case "02" ->
                "#f59e0b"; // kuning — sebagian berawan
            case "03" ->
                "#64748b"; // abu — berawan
            case "04" ->
                "#475569"; // abu gelap — sangat berawan
            case "09" ->
                "#2b8cee"; // biru — hujan lebat
            case "10" ->
                "#2b8cee"; // biru — hujan
            case "11" ->
                "#6366f1"; // ungu — badai petir
            case "13" ->
                "#7dd3fc"; // biru muda — salju
            case "50" ->
                "#94a3b8"; // abu muda — berkabut
            default ->
                "#64748b";
        };
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    // ── HTTP Helper ────────────────────────────────────────────
    private String fetchUrl(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(AppConfig.TIMEOUT_SEC * 1000);
        conn.setReadTimeout(AppConfig.TIMEOUT_SEC * 1000);
        conn.setRequestProperty("Accept", "application/json");

        int status = conn.getResponseCode();
        if (status == 401) {
            throw new Exception("API Key tidak valid!");
        }
        if (status == 404) {
            throw new Exception("Kota tidak ditemukan!");
        }
        if (status != 200) {
            throw new Exception("Error HTTP: " + status);
        }

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        reader.close();
        conn.disconnect();

        return sb.toString();
    }
}
