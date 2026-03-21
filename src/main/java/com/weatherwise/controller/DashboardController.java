package com.weatherwise.controller;

import com.weatherwise.component.HourlyCard;
import com.weatherwise.component.HumidityBarChart;
import com.weatherwise.component.WeatherCard;
import com.weatherwise.model.CurrentWeather;
import com.weatherwise.model.ForecastDay;
import com.weatherwise.model.OneCallResponse;
import com.weatherwise.service.WeatherService;
import com.weatherwise.util.AppState;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class DashboardController {

    @FXML private Label    labelCity;
    @FXML private Label    labelDate;
    @FXML private Label    labelTemp;
    @FXML private Label    labelCondition;
    @FXML private Label    labelFeelsLike;
    @FXML private Label    labelHigh;
    @FXML private Label    labelLow;
    @FXML private FontIcon iconWeather;
    @FXML private HBox     detailContainer;
    @FXML private HBox     hourlyContainer;
    @FXML private Pane     humidityChartContainer;
    @FXML private Label    labelStatus;

    private final WeatherService weatherService = new WeatherService();

    private double  currentLat = -6.2088;
    private double  currentLon = 106.8456;
    private volatile boolean active = true;

    @FXML
    public void initialize() {
        AppState state = AppState.getInstance();
        this.currentLat = state.getLat();
        this.currentLon = state.getLon();
        state.clearChanged();
        showLoading();
        loadWeatherData(currentLat, currentLon);
    }

    public void dispose() { active = false; }

    public void loadWeatherData(double lat, double lon) {
        this.currentLat = lat;
        this.currentLon = lon;

        if (Platform.isFxApplicationThread()) showLoading();
        else Platform.runLater(this::showLoading);

        // ✅ Satu Task — satu panggilan API untuk semua data
        Task<OneCallResponse> task = new Task<>() {
            @Override
            protected OneCallResponse call() throws Exception {
                return weatherService.getOneCallData(lat, lon);
            }
        };

        task.setOnSucceeded(e -> {
            if (!active) return;
            OneCallResponse ocr = task.getValue();

            // Adapter ke model lama untuk hero section
            CurrentWeather current = weatherService.getCurrentWeather(ocr);
            List<ForecastDay> forecast = weatherService.getForecastFromResponse(ocr);

            updateHero(current, ocr.getTimezone());
            updateForecastWidgets(forecast, ocr);
        });

        task.setOnFailed(e -> {
            if (!active) return;
            String msg = task.getException() != null
                ? task.getException().getMessage() : "Gagal memuat data";
            Platform.runLater(() -> showError(msg));
        });

        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }

    public void loadWeatherDataByCity(String cityName) {
        if (Platform.isFxApplicationThread()) showLoading();
        else Platform.runLater(this::showLoading);

        Task<CurrentWeather> task = new Task<>() {
            @Override
            protected CurrentWeather call() throws Exception {
                return weatherService.getCurrentWeatherByCity(cityName);
            }
        };
        task.setOnSucceeded(e -> {
            if (!active) return;
            CurrentWeather w = task.getValue();
            // Setelah dapat koordinat, load One Call data
            loadWeatherData(w.getLatitude(), w.getLongitude());
        });
        task.setOnFailed(e -> {
            if (!active) return;
            String msg = task.getException() != null
                ? task.getException().getMessage() : "Error";
            Platform.runLater(() -> showError(msg));
        });
        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }

    private void updateHero(CurrentWeather w, String timezone) {
        AppState state = AppState.getInstance();

        Platform.runLater(() -> {
            if (!active) return;

            // Nama kota tetap dari AppState karena One Call tidak mengembalikan city name
            String cityDisplay = state.getCityName();
            if (labelCity      != null) labelCity.setText(cityDisplay);
            if (labelDate      != null) labelDate.setText(
                LocalDate.now().format(
                    DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.ENGLISH)));
            if (labelTemp      != null) labelTemp.setText(String.valueOf((int) w.getTemperature()));
            if (labelCondition != null) labelCondition.setText(capitalize(w.getCondition()));
            if (labelFeelsLike != null) labelFeelsLike.setText("Feels like " + (int) w.getFeelsLike() + "°C");
            if (labelHigh      != null) labelHigh.setText((int) w.getTempMax() + "°");
            if (labelLow       != null) labelLow.setText((int) w.getTempMin() + "°");
            if (iconWeather    != null) iconWeather.setIconLiteral(getWeatherIcon(w.getConditionIcon()));

            if (detailContainer != null) {
                detailContainer.getChildren().setAll(
                    // ── Card lama ────────────────────────────────
                    new WeatherCard("mdi2w-water-percent", "#2b8cee",
                        "Humidity",    w.getHumidity() + "%",         "Relative humidity"),
                    new WeatherCard("mdi2w-weather-windy", "#22c55e",
                        "Wind",        (int) w.getWindSpeed() + " m/s · " + w.getWindDegDisplay(),
                        "Speed · Direction"),
                    new WeatherCard("mdi2g-gauge", "#f59e0b",
                        "Pressure",    w.getPressure() + " hPa",      "Atmospheric pressure"),
                    new WeatherCard("mdi2e-eye-outline", "#8b5cf6",
                        "Visibility",  w.getVisibilityDisplay(),       "Visibility range"),

                    // ── Card baru dari One Call 3.0 ───────────────
                    new WeatherCard("mdi2w-weather-sunny-alert", "#ef4444",
                        "UV Index",    w.getUvIndexDisplay(),          "Solar UV index"),
                    new WeatherCard("mdi2w-thermometer-water", "#06b6d4",
                        "Dew Point",   (int) w.getDewPoint() + "°C",  "Dew point temperature"),
                    new WeatherCard("mdi2w-weather-windy-variant", "#64748b",
                        "Wind Gust",   (int) w.getWindGust() + " m/s","Max wind gust"),
                    new WeatherCard("mdi2w-weather-sunset-up", "#f97316",
                        "Sunrise",     w.getSunriseDisplay(timezone),  "Local sunrise time"),
                    new WeatherCard("mdi2w-weather-sunset-down", "#6366f1",
                        "Sunset",      w.getSunsetDisplay(timezone),   "Local sunset time")
                );
            }

            if (labelStatus != null) labelStatus.setVisible(false);
        });
    }

    private void updateForecastWidgets(List<ForecastDay> days, OneCallResponse ocr) {
        Platform.runLater(() -> {
            if (!active || days == null || days.isEmpty()) return;

            // ── Hourly cards — sekarang pakai data hourly nyata dari One Call ──
            if (hourlyContainer != null) {
                hourlyContainer.getChildren().clear();
                ZoneId zone = ZoneId.of(ocr.getTimezone());
                DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("h a", Locale.ENGLISH);

                List<OneCallResponse.HourlyData> hourlyList = ocr.getHourly();
                // Tampilkan 7 jam ke depan (index 1–7, skip jam saat ini di index 0)
                for (int i = 0; i < Math.min(7, hourlyList != null ? hourlyList.size() - 1 : 0); i++) {
                    OneCallResponse.HourlyData h = hourlyList.get(i + 1);
                    String timeLabel = (i == 0) ? "Next 1h"
                        : Instant.ofEpochSecond(h.getDt())
                            .atZone(zone)
                            .format(timeFmt);

                    String iconLiteral = resolveIconLiteral(h.getConditionIcon());
                    String iconColor   = resolveIconColor(h.getConditionIcon());

                    // ✅ Suhu dan label waktu kini dari data hourly nyata
                    hourlyContainer.getChildren().add(new HourlyCard(
                        timeLabel, iconLiteral, iconColor,
                        (int) h.getTemp() + "°", i == 0
                    ));
                }
            }

            // ── Humidity bar chart (tidak berubah) ──────────────
            if (humidityChartContainer != null) {
                humidityChartContainer.getChildren().clear();
                double w = humidityChartContainer.getWidth();
                double h = humidityChartContainer.getPrefHeight();
                if (w <= 10) w = humidityChartContainer.getPrefWidth();
                if (w <= 10) w = 600;
                if (h <= 10) h = 120;

                try {
                    HumidityBarChart chart = new HumidityBarChart(w, h);
                    chart.setData(
                        days.stream().map(ForecastDay::getDayName).toArray(String[]::new),
                        days.stream().mapToDouble(ForecastDay::getHumidity).toArray()
                    );
                    humidityChartContainer.getChildren().add(chart);
                } catch (Exception ex) {
                    System.err.println("❌ HumidityBarChart error: " + ex.getMessage());
                }
            }
        });
    }

    // ── Loading & Error state ─────────────────────────────────
    private void showLoading() {
        if (labelStatus    != null) { labelStatus.setText("⏳ Memuat data cuaca..."); labelStatus.setVisible(true); }
        if (labelCity      != null) labelCity.setText("Memuat...");
        if (labelTemp      != null) labelTemp.setText("--");
        if (labelCondition != null) labelCondition.setText("--");
    }

    private void showError(String msg) {
        if (labelStatus != null) { labelStatus.setText("❌ " + (msg != null ? msg : "Terjadi kesalahan")); labelStatus.setVisible(true); }
        if (labelCity   != null) labelCity.setText("Gagal memuat data");
        if (labelTemp   != null) labelTemp.setText("--");
    }

    private String getWeatherIcon(String owmIcon) {
        if (owmIcon == null) return "mdi2w-weather-cloudy";
        return switch (owmIcon.substring(0, 2)) {
            case "01" -> "mdi2w-weather-sunny";
            case "02" -> "mdi2w-weather-partly-cloudy";
            case "03", "04" -> "mdi2w-weather-cloudy";
            case "09" -> "mdi2w-weather-pouring";
            case "10" -> "mdi2w-weather-rainy";
            case "11" -> "mdi2w-weather-lightning";
            case "13" -> "mdi2w-weather-snowy";
            case "50" -> "mdi2w-weather-fog";
            default   -> "mdi2w-weather-cloudy";
        };
    }

    private String resolveIconLiteral(String owmIcon) {
        if (owmIcon == null) return "mdi2w-weather-cloudy";
        return switch (owmIcon.substring(0, 2)) {
            case "01" -> "mdi2w-weather-sunny";
            case "02" -> "mdi2w-weather-partly-cloudy";
            case "03", "04" -> "mdi2w-weather-cloudy";
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
            case "01", "02" -> "#f59e0b";
            case "03"       -> "#64748b";
            case "04"       -> "#475569";
            case "09", "10" -> "#2b8cee";
            case "11"       -> "#6366f1";
            case "13"       -> "#7dd3fc";
            case "50"       -> "#94a3b8";
            default         -> "#64748b";
        };
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    public double getCurrentLat() { return currentLat; }
    public double getCurrentLon() { return currentLon; }
}
