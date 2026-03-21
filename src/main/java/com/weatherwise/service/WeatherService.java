package com.weatherwise.service;

import com.weatherwise.model.CurrentWeather;
import com.weatherwise.model.ForecastDay;
import com.weatherwise.model.OneCallResponse;
import com.weatherwise.model.OneCallResponse.CurrentData;
import com.weatherwise.model.OneCallResponse.DailyData;
import com.weatherwise.model.OneCallResponse.HourlyData;
import com.weatherwise.model.OneCallResponse.AlertData;
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

    // ══════════════════════════════════════════════════════════
    // PRIMARY: One Call API 3.0
    // Satu panggilan menghasilkan current + hourly + daily + alerts
    // ══════════════════════════════════════════════════════════

    public OneCallResponse getOneCallData(double lat, double lon) throws Exception {
        String urlStr = AppConfig.ONE_CALL_URL
                + "?lat="   + lat
                + "&lon="   + lon
                + "&units=" + AppConfig.DEFAULT_UNIT
                + "&appid=" + AppConfig.getApiKey();

        String response = fetchUrl(urlStr);
        return parseOneCallResponse(new JSONObject(response));
    }
    
    /**
     * Konversi OneCallResponse yang sudah di-fetch ke CurrentWeather. Dipakai
     * DashboardController agar tidak fetch API dua kali.
     */
    public CurrentWeather getCurrentWeather(OneCallResponse ocr) {
        return adaptToCurrent(ocr);
    }
    
    /**
     * Konversi OneCallResponse yang sudah di-fetch ke List<ForecastDay>.
     * Dipakai DashboardController agar tidak fetch API dua kali.
     */
    public List<ForecastDay> getForecastFromResponse(OneCallResponse ocr) {
        return adaptToForecast(ocr);
    }

    // ── Parser utama One Call 3.0 ──────────────────────────────
    private OneCallResponse parseOneCallResponse(JSONObject json) {
        OneCallResponse result = new OneCallResponse();

        result.setLat(json.getDouble("lat"));
        result.setLon(json.getDouble("lon"));
        result.setTimezone(json.getString("timezone"));
        result.setTimezoneOffset(json.getInt("timezone_offset"));

        // ── current ───────────────────────────────────────────
        if (json.has("current")) {
            result.setCurrent(parseCurrentData(json.getJSONObject("current")));
        }

        // ── hourly (48 jam) ───────────────────────────────────
        if (json.has("hourly")) {
            List<HourlyData> hourlyList = new ArrayList<>();
            JSONArray hourlyArr = json.getJSONArray("hourly");
            for (int i = 0; i < hourlyArr.length(); i++) {
                hourlyList.add(parseHourlyData(hourlyArr.getJSONObject(i)));
            }
            result.setHourly(hourlyList);
        }

        // ── daily (8 hari) ────────────────────────────────────
        if (json.has("daily")) {
            List<DailyData> dailyList = new ArrayList<>();
            JSONArray dailyArr = json.getJSONArray("daily");
            for (int i = 0; i < dailyArr.length(); i++) {
                dailyList.add(parseDailyData(dailyArr.getJSONObject(i)));
            }
            result.setDaily(dailyList);
        }

        // ── alerts (opsional, bisa tidak ada) ─────────────────
        if (json.has("alerts")) {
            List<AlertData> alertList = new ArrayList<>();
            JSONArray alertArr = json.getJSONArray("alerts");
            for (int i = 0; i < alertArr.length(); i++) {
                alertList.add(parseAlertData(alertArr.getJSONObject(i)));
            }
            result.setAlerts(alertList);
        }

        return result;
    }

    private CurrentData parseCurrentData(JSONObject json) {
        CurrentData c = new CurrentData();

        c.setDt(json.getLong("dt"));
        c.setSunrise(json.optLong("sunrise", 0));
        c.setSunset(json.optLong("sunset", 0));
        c.setTemp(json.getDouble("temp"));
        c.setFeelsLike(json.getDouble("feels_like"));
        c.setPressure(json.getInt("pressure"));
        c.setHumidity(json.getInt("humidity"));
        c.setDewPoint(json.optDouble("dew_point", 0));
        c.setClouds(json.optInt("clouds", 0));
        c.setUvi(json.optDouble("uvi", 0));
        c.setVisibility(json.optInt("visibility", 0));
        c.setWindSpeed(json.optDouble("wind_speed", 0));
        c.setWindGust(json.optDouble("wind_gust", 0));
        c.setWindDeg(json.optInt("wind_deg", 0));

        // rain dan snow bersarang dalam objek (hanya ada jika turun hujan/salju)
        if (json.has("rain")) {
            c.setRain1h(json.getJSONObject("rain").optDouble("1h", 0));
        }
        if (json.has("snow")) {
            c.setSnow1h(json.getJSONObject("snow").optDouble("1h", 0));
        }

        JSONObject weatherObj = json.getJSONArray("weather").getJSONObject(0);
        c.setConditionMain(weatherObj.getString("main"));
        c.setConditionDescription(weatherObj.getString("description"));
        c.setConditionIcon(weatherObj.getString("icon"));

        return c;
    }

    private HourlyData parseHourlyData(JSONObject json) {
        HourlyData h = new HourlyData();

        h.setDt(json.getLong("dt"));
        h.setTemp(json.getDouble("temp"));
        h.setFeelsLike(json.getDouble("feels_like"));
        h.setPressure(json.getInt("pressure"));
        h.setHumidity(json.getInt("humidity"));
        h.setDewPoint(json.optDouble("dew_point", 0));
        h.setUvi(json.optDouble("uvi", 0));
        h.setClouds(json.optInt("clouds", 0));
        h.setVisibility(json.optInt("visibility", 0));
        h.setWindSpeed(json.optDouble("wind_speed", 0));
        h.setWindGust(json.optDouble("wind_gust", 0));
        h.setWindDeg(json.optInt("wind_deg", 0));
        h.setPop(json.optDouble("pop", 0));

        if (json.has("rain")) {
            h.setRain1h(json.getJSONObject("rain").optDouble("1h", 0));
        }
        if (json.has("snow")) {
            h.setSnow1h(json.getJSONObject("snow").optDouble("1h", 0));
        }

        JSONObject weatherObj = json.getJSONArray("weather").getJSONObject(0);
        h.setConditionMain(weatherObj.getString("main"));
        h.setConditionDescription(weatherObj.getString("description"));
        h.setConditionIcon(weatherObj.getString("icon"));

        return h;
    }

    private DailyData parseDailyData(JSONObject json) {
        DailyData d = new DailyData();

        d.setDt(json.getLong("dt"));
        d.setSunrise(json.optLong("sunrise", 0));
        d.setSunset(json.optLong("sunset", 0));
        d.setMoonrise(json.optLong("moonrise", 0));
        d.setMoonset(json.optLong("moonset", 0));
        d.setMoonPhase(json.optDouble("moon_phase", 0));
        d.setSummary(json.optString("summary", ""));

        // Suhu harian — bersarang dalam objek "temp"
        JSONObject temp = json.getJSONObject("temp");
        d.setTempMorn(temp.optDouble("morn", 0));
        d.setTempDay(temp.optDouble("day", 0));
        d.setTempEve(temp.optDouble("eve", 0));
        d.setTempNight(temp.optDouble("night", 0));
        d.setTempMin(temp.optDouble("min", 0));
        d.setTempMax(temp.optDouble("max", 0));

        // Feels like — bersarang dalam objek "feels_like"
        JSONObject feels = json.getJSONObject("feels_like");
        d.setFeelsLikeMorn(feels.optDouble("morn", 0));
        d.setFeelsLikeDay(feels.optDouble("day", 0));
        d.setFeelsLikeEve(feels.optDouble("eve", 0));
        d.setFeelsLikeNight(feels.optDouble("night", 0));

        d.setPressure(json.optInt("pressure", 0));
        d.setHumidity(json.optInt("humidity", 0));
        d.setDewPoint(json.optDouble("dew_point", 0));
        d.setWindSpeed(json.optDouble("wind_speed", 0));
        d.setWindGust(json.optDouble("wind_gust", 0));
        d.setWindDeg(json.optInt("wind_deg", 0));
        d.setClouds(json.optInt("clouds", 0));
        d.setUvi(json.optDouble("uvi", 0));
        d.setPop(json.optDouble("pop", 0));

        if (json.has("rain")) d.setRain(json.optDouble("rain", 0));
        if (json.has("snow")) d.setSnow(json.optDouble("snow", 0));

        JSONObject weatherObj = json.getJSONArray("weather").getJSONObject(0);
        d.setConditionMain(weatherObj.getString("main"));
        d.setConditionDescription(weatherObj.getString("description"));
        d.setConditionIcon(weatherObj.getString("icon"));

        return d;
    }

    private AlertData parseAlertData(JSONObject json) {
        AlertData a = new AlertData();
        a.setSenderName(json.optString("sender_name", ""));
        a.setEvent(json.optString("event", ""));
        a.setStart(json.optLong("start", 0));
        a.setEnd(json.optLong("end", 0));
        a.setDescription(json.optString("description", ""));

        if (json.has("tags")) {
            List<String> tags = new ArrayList<>();
            JSONArray tagsArr = json.getJSONArray("tags");
            for (int i = 0; i < tagsArr.length(); i++) {
                tags.add(tagsArr.getString(i));
            }
            a.setTags(tags);
        }

        return a;
    }

    // ══════════════════════════════════════════════════════════
    // ADAPTER: Konversi OneCallResponse → model lama
    // Dipakai controller yang belum dimigrasi (Fase 3 & 4)
    // supaya DashboardController & ForecastController tidak
    // langsung broken setelah Fase 2 ini.
    // ══════════════════════════════════════════════════════════

    /**
     * Ambil current weather via One Call 3.0, kembalikan sebagai
     * CurrentWeather (model lama) agar kompatibel dengan controller saat ini.
     */
    public CurrentWeather getCurrentWeather(double lat, double lon) throws Exception {
        OneCallResponse ocr = getOneCallData(lat, lon);
        return adaptToCurrent(ocr);
    }

    /**
     * Ambil forecast via One Call 3.0, kembalikan sebagai List<ForecastDay>
     * (model lama) agar kompatibel dengan ForecastController saat ini.
     */
    public List<ForecastDay> getForecast(double lat, double lon) throws Exception {
        OneCallResponse ocr = getOneCallData(lat, lon);
        return adaptToForecast(ocr);
    }

    private CurrentWeather adaptToCurrent(OneCallResponse ocr) {
        CurrentWeather w = new CurrentWeather();
        CurrentData c = ocr.getCurrent();

        // Koordinat dari root response
        w.setLatitude(ocr.getLat());
        w.setLongitude(ocr.getLon());

        // One Call tidak mengembalikan nama kota —
        // nama kota tetap diambil dari AppState (sudah diset saat geocoding)
        w.setCityName("");
        w.setCountry("");

        w.setTemperature(c.getTemp());
        w.setFeelsLike(c.getFeelsLike());
        w.setPressure(c.getPressure());
        w.setHumidity(c.getHumidity());
        w.setVisibility(c.getVisibility());
        w.setWindSpeed(c.getWindSpeed());
        w.setCondition(c.getConditionDescription());
        w.setConditionIcon(c.getConditionIcon());

        // Field baru — Fase 3 akan menambahkan getter/setter ini ke CurrentWeather
        w.setUvIndex(c.getUvi());
        w.setDewPoint(c.getDewPoint());
        w.setWindGust(c.getWindGust());
        w.setWindDeg(c.getWindDeg());
        w.setRain1h(c.getRain1h());
        w.setSnow1h(c.getSnow1h());
        w.setClouds(c.getClouds());
        w.setSunrise(c.getSunrise());
        w.setSunset(c.getSunset());

        // tempMin & tempMax ambil dari daily[0] (lebih akurat dari One Call)
        if (ocr.getDaily() != null && !ocr.getDaily().isEmpty()) {
            DailyData today = ocr.getDaily().get(0);
            w.setTempMin(today.getTempMin());
            w.setTempMax(today.getTempMax());
        }

        return w;
    }

    private List<ForecastDay> adaptToForecast(OneCallResponse ocr) {
        List<ForecastDay> result = new ArrayList<>();
        if (ocr.getDaily() == null) return result;

        DateTimeFormatter dayFmt  = DateTimeFormatter.ofPattern("EEEE", Locale.ENGLISH);
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("MMM d",  Locale.ENGLISH);
        ZoneId zone = ZoneId.of(ocr.getTimezone());

        // Mulai dari index 1 (skip hari ini, sudah ditampilkan di current)
        List<DailyData> dailyList = ocr.getDaily();
        for (int i = 1; i < dailyList.size() && result.size() < 7; i++) {
            DailyData data = dailyList.get(i);
            ForecastDay day = new ForecastDay();

            Instant instant = Instant.ofEpochSecond(data.getDt());
            day.setDayName(dayFmt.format(instant.atZone(zone)));
            day.setDate(dateFmt.format(instant.atZone(zone)));

            day.setTempHigh(data.getTempMax());
            day.setTempLow(data.getTempMin());
            day.setHumidity(data.getHumidity());
            day.setWindSpeed(data.getWindSpeed());

            String icon = data.getConditionIcon();
            day.setConditionIcon(icon);
            day.setCondition(capitalize(data.getConditionMain()));
            day.setDescription(
                capitalize(data.getConditionDescription())
                + " \u00b7 Humidity " + data.getHumidity() + "%"
                + " \u00b7 Rain " + data.getPopDisplay()   // ✅ data nyata dari pop
            );
            day.setIconLiteral(resolveIconLiteral(icon));
            day.setIconColor(resolveIconColor(icon));
            day.setPop(data.getPop());
            day.setUvIndex(data.getUvi());
            day.setRain(data.getRain());
            day.setSnow(data.getSnow());
            day.setWindGust(data.getWindGust());
            day.setWindDeg(data.getWindDeg());
            day.setDewPoint(data.getDewPoint());
            day.setClouds(data.getClouds());
            day.setSunrise(data.getSunrise());
            day.setSunset(data.getSunset());
            day.setMoonPhase(data.getMoonPhase());
            day.setSummary(data.getSummary());
            day.setTempMorn(data.getTempMorn());
            day.setTempDay(data.getTempDay());
            day.setTempEve(data.getTempEve());
            day.setTempNight(data.getTempNight());

            result.add(day);
        }

        return result;
    }

    // ══════════════════════════════════════════════════════════
    // FALLBACK: Pencarian by city name (tetap pakai /weather 2.5)
    // Karena One Call 3.0 tidak mendukung ?q= city name
    // ══════════════════════════════════════════════════════════

    /**
     * Hanya dipakai untuk mendapat nama kota & koordinat awal.
     * Setelah lat/lon diketahui, gunakan getCurrentWeather(lat, lon).
     */
    public CurrentWeather getCurrentWeatherByCity(String cityName) throws Exception {
        String encoded = java.net.URLEncoder.encode(cityName, "UTF-8");
        String urlStr  = AppConfig.BASE_URL + "/weather"
                + "?q="     + encoded
                + "&units=" + AppConfig.DEFAULT_UNIT
                + "&appid=" + AppConfig.getApiKey();

        String response = fetchUrl(urlStr);
        return parseLegacyCurrentWeather(new JSONObject(response));
    }

    private CurrentWeather parseLegacyCurrentWeather(JSONObject json) {
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

        if (json.has("visibility")) w.setVisibility(json.getInt("visibility"));

        JSONObject weatherObj = json.getJSONArray("weather").getJSONObject(0);
        w.setCondition(weatherObj.getString("description"));
        w.setConditionIcon(weatherObj.getString("icon"));

        return w;
    }

    // ── Mapping icon OWM → Ikonli literal ─────────────────────
    // (tidak berubah dari versi lama)
    private String resolveIconLiteral(String owmIcon) {
        if (owmIcon == null) return "mdi2w-weather-cloudy";
        return switch (owmIcon.substring(0, 2)) {
            case "01" -> "mdi2w-weather-sunny";
            case "02" -> "mdi2w-weather-partly-cloudy";
            case "03" -> "mdi2w-weather-cloudy";
            case "04" -> "mdi2w-weather-cloudy";
            case "09" -> "mdi2w-weather-pouring";
            case "10" -> "mdi2w-weather-rainy";
            case "11" -> "mdi2w-weather-lightning";
            case "13" -> "mdi2w-weather-snowy";
            case "50" -> "mdi2w-weather-fog";
            default   -> "mdi2w-weather-cloudy";
        };
    }

    private String resolveIconColor(String owmIcon) {
        if (owmIcon == null) return "#64748b";
        return switch (owmIcon.substring(0, 2)) {
            case "01" -> "#f59e0b";
            case "02" -> "#f59e0b";
            case "03" -> "#64748b";
            case "04" -> "#475569";
            case "09" -> "#2b8cee";
            case "10" -> "#2b8cee";
            case "11" -> "#6366f1";
            case "13" -> "#7dd3fc";
            case "50" -> "#94a3b8";
            default   -> "#64748b";
        };
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
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

        try {
            int status = conn.getResponseCode();
            if (status == 401) throw new Exception("API Key tidak valid!");
            if (status == 404) throw new Exception("Kota tidak ditemukan!");
            if (status != 200) throw new Exception("Error HTTP: " + status);

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            reader.close();
            return sb.toString();
        } finally {
            conn.disconnect();
        }
    }
}