package com.weatherwise.controller;

import com.weatherwise.model.CurrentWeather;
import com.weatherwise.service.WeatherService;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

public class DashboardController {

    // ── Labels Cuaca Utama ────────────────────────────────────
    @FXML private Label labelCity;
    @FXML private Label labelDate;
    @FXML private Label labelTemp;
    @FXML private Label labelCondition;
    @FXML private Label labelFeelsLike;
    @FXML private Label labelHigh;
    @FXML private Label labelLow;

    // ── Labels Detail ─────────────────────────────────────────
    @FXML private Label labelHumidity;
    @FXML private Label labelWind;
    @FXML private Label labelPressure;
    @FXML private Label labelVisibility;

    // ── Icon Cuaca ────────────────────────────────────────────
    @FXML private FontIcon iconWeather;

    // ── Status ────────────────────────────────────────────────
    @FXML private Label labelStatus;

    private final WeatherService weatherService = new WeatherService();

    // Koordinat default: San Francisco
    private double currentLat = 37.7749;
    private double currentLon = -122.4194;

    @FXML
    public void initialize() {
        showLoading();
        loadWeatherData(currentLat, currentLon);
    }

    // ── Load Data dari API ─────────────────────────────────────
    public void loadWeatherData(double lat, double lon) {
        this.currentLat = lat;
        this.currentLon = lon;

        Task<CurrentWeather> task = new Task<>() {
            @Override
            protected CurrentWeather call() throws Exception {
                return weatherService.getCurrentWeather(lat, lon);
            }
        };

        task.setOnSucceeded(e -> {
            CurrentWeather w = task.getValue();
            updateUI(w);
        });

        task.setOnFailed(e -> {
            String msg = task.getException().getMessage();
            System.err.println("❌ Gagal load cuaca: " + msg);
            showError(msg);
        });

        new Thread(task).start();
    }

    public void loadWeatherDataByCity(String cityName) {
        showLoading();

        Task<CurrentWeather> task = new Task<>() {
            @Override
            protected CurrentWeather call() throws Exception {
                return weatherService.getCurrentWeatherByCity(cityName);
            }
        };

        task.setOnSucceeded(e -> updateUI(task.getValue()));
        task.setOnFailed(e -> showError(task.getException().getMessage()));

        new Thread(task).start();
    }

    // ── Update Semua UI ────────────────────────────────────────
    private void updateUI(CurrentWeather w) {
        // Lokasi & Tanggal
        if (labelCity != null)
            labelCity.setText(w.getCityName() + ", " + w.getCountry());

        if (labelDate != null) {
            String date = java.time.LocalDate.now()
                .format(java.time.format.DateTimeFormatter
                    .ofPattern("EEEE, MMMM d, yyyy",
                               java.util.Locale.ENGLISH));
            labelDate.setText(date);
        }

        // Suhu
        if (labelTemp != null)
            labelTemp.setText(String.valueOf((int) w.getTemperature()));

        if (labelCondition != null)
            labelCondition.setText(capitalize(w.getCondition()));

        if (labelFeelsLike != null)
            labelFeelsLike.setText("Feels like " + (int) w.getFeelsLike() + "°C");

        if (labelHigh != null)
            labelHigh.setText((int) w.getTempMax() + "°");

        if (labelLow != null)
            labelLow.setText((int) w.getTempMin() + "°");

        // Detail
        if (labelHumidity != null)
            labelHumidity.setText(w.getHumidity() + "%");

        if (labelWind != null)
            labelWind.setText((int) w.getWindSpeed() + " m/s");

        if (labelPressure != null)
            labelPressure.setText(w.getPressure() + " hPa");

        // Icon cuaca berdasarkan kondisi
        if (iconWeather != null)
            iconWeather.setIconLiteral(getWeatherIcon(w.getConditionIcon()));

        // Sembunyikan status
        if (labelStatus != null)
            labelStatus.setVisible(false);

        System.out.println("✅ Data cuaca berhasil dimuat: "
            + w.getCityName() + " " + (int) w.getTemperature() + "°C");
    }

    // ── State Helpers ──────────────────────────────────────────
    private void showLoading() {
        if (labelStatus != null) {
            labelStatus.setText("⏳ Memuat data cuaca...");
            labelStatus.setVisible(true);
        }
        if (labelCity != null) labelCity.setText("Memuat...");
        if (labelTemp != null) labelTemp.setText("--");
        if (labelCondition != null) labelCondition.setText("--");
    }

    private void showError(String message) {
        if (labelStatus != null) {
            labelStatus.setText("❌ " + (message != null ? message : "Terjadi kesalahan"));
            labelStatus.setVisible(true);
        }
        if (labelCity != null) labelCity.setText("Gagal memuat data");
        if (labelTemp != null) labelTemp.setText("--");
    }

    // ── Mapping Icon OWM → Ikonli ──────────────────────────────
    private String getWeatherIcon(String owmIcon) {
        if (owmIcon == null) return "mdi2w-weather-cloudy";
        return switch (owmIcon.substring(0, 2)) {
            case "01" -> "mdi2w-weather-sunny";           // Clear sky
            case "02" -> "mdi2w-weather-partly-cloudy";   // Few clouds
            case "03" -> "mdi2w-weather-cloudy";          // Scattered clouds
            case "04" -> "mdi2w-weather-cloudy";          // Broken clouds
            case "09" -> "mdi2w-weather-pouring";         // Shower rain
            case "10" -> "mdi2w-weather-rainy";           // Rain
            case "11" -> "mdi2w-weather-lightning";       // Thunderstorm
            case "13" -> "mdi2w-weather-snowy";           // Snow
            case "50" -> "mdi2w-weather-fog";             // Mist
            default   -> "mdi2w-weather-cloudy";
        };
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    // Getter koordinat (dipakai controller lain)
    public double getCurrentLat() { return currentLat; }
    public double getCurrentLon() { return currentLon; }
}