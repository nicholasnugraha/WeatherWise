package com.weatherwise.controller;

import com.weatherwise.model.CurrentWeather;
import com.weatherwise.service.WeatherService;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
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

    // Koordinat default: Jakarta, Indonesia
    private double currentLat = -6.2088;
    private double currentLon = 106.8456;

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

        task.setOnSucceeded(e -> updateUI(task.getValue()));
        task.setOnFailed(e -> {
            String msg = task.getException() != null
                    ? task.getException().getMessage()
                    : "Terjadi kesalahan tidak diketahui";
            System.err.println("\u274c Gagal load cuaca: " + msg);
            Platform.runLater(() -> showError(msg));
        });

        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
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
        task.setOnFailed(e -> {
            String msg = task.getException() != null
                    ? task.getException().getMessage()
                    : "Terjadi kesalahan tidak diketahui";
            Platform.runLater(() -> showError(msg));
        });

        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }

    // ── Update Semua UI ────────────────────────────────────────
    private void updateUI(CurrentWeather w) {
        if (labelCity != null)
            labelCity.setText(w.getCityName() + ", " + w.getCountry());

        if (labelDate != null) {
            String date = java.time.LocalDate.now()
                .format(java.time.format.DateTimeFormatter
                    .ofPattern("EEEE, MMMM d, yyyy",
                               java.util.Locale.ENGLISH));
            labelDate.setText(date);
        }

        if (labelTemp != null)
            labelTemp.setText(String.valueOf((int) w.getTemperature()));

        if (labelCondition != null)
            labelCondition.setText(capitalize(w.getCondition()));

        if (labelFeelsLike != null)
            labelFeelsLike.setText("Feels like " + (int) w.getFeelsLike() + "\u00b0C");

        if (labelHigh != null)
            labelHigh.setText((int) w.getTempMax() + "\u00b0");

        if (labelLow != null)
            labelLow.setText((int) w.getTempMin() + "\u00b0");

        if (labelHumidity != null)
            labelHumidity.setText(w.getHumidity() + "%");

        if (labelWind != null)
            labelWind.setText((int) w.getWindSpeed() + " m/s");

        if (labelPressure != null)
            labelPressure.setText(w.getPressure() + " hPa");

        // Visibility: tampilkan dalam km (data dari OWM dalam meter)
        if (labelVisibility != null)
            labelVisibility.setText(w.getVisibilityDisplay());

        if (iconWeather != null)
            iconWeather.setIconLiteral(getWeatherIcon(w.getConditionIcon()));

        if (labelStatus != null)
            labelStatus.setVisible(false);

        System.out.println("\u2705 Data cuaca berhasil dimuat: "
            + w.getCityName() + " " + (int) w.getTemperature() + "\u00b0C");
    }

    // ── State Helpers ──────────────────────────────────────────
    private void showLoading() {
        if (labelStatus != null) {
            labelStatus.setText("\u23f3 Memuat data cuaca...");
            labelStatus.setVisible(true);
        }
        if (labelCity != null)      labelCity.setText("Memuat...");
        if (labelTemp != null)      labelTemp.setText("--");
        if (labelCondition != null) labelCondition.setText("--");
        if (labelVisibility != null) labelVisibility.setText("--");
    }

    private void showError(String message) {
        if (labelStatus != null) {
            labelStatus.setText("\u274c " + (message != null ? message : "Terjadi kesalahan"));
            labelStatus.setVisible(true);
        }
        if (labelCity != null) labelCity.setText("Gagal memuat data");
        if (labelTemp != null) labelTemp.setText("--");
        if (labelVisibility != null) labelVisibility.setText("N/A");
    }

    // ── Mapping Icon OWM → Ikonli ──────────────────────────────
    private String getWeatherIcon(String owmIcon) {
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

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    public double getCurrentLat() { return currentLat; }
    public double getCurrentLon() { return currentLon; }
}
