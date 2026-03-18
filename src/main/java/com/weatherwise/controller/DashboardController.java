package com.weatherwise.controller;

import com.weatherwise.component.HourlyCard;
import com.weatherwise.component.HumidityBarChart;
import com.weatherwise.component.WeatherCard;
import com.weatherwise.model.CurrentWeather;
import com.weatherwise.model.ForecastDay;
import com.weatherwise.service.WeatherService;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class DashboardController {

    // ── Hero ──────────────────────────────────────────────────
    @FXML private Label    labelCity;
    @FXML private Label    labelDate;
    @FXML private Label    labelTemp;
    @FXML private Label    labelCondition;
    @FXML private Label    labelFeelsLike;
    @FXML private Label    labelHigh;
    @FXML private Label    labelLow;
    @FXML private Label    labelSatelliteCity;
    @FXML private FontIcon iconWeather;

    // ── Detail Container ─────────────────────────────────────
    @FXML private HBox detailContainer;

    // ── Hourly Container ─────────────────────────────────────
    @FXML private HBox hourlyContainer;

    // ── Humidity Chart ───────────────────────────────────────
    @FXML private Pane humidityChartContainer;

    // ── Status ───────────────────────────────────────────────
    @FXML private Label labelStatus;

    private final WeatherService weatherService = new WeatherService();

    private double currentLat = -6.2088;
    private double currentLon = 106.8456;

    @FXML
    public void initialize() {
        showLoading();
        loadWeatherData(currentLat, currentLon);
    }

    // ── Public: load dari koordinat ───────────────────────────
    public void loadWeatherData(double lat, double lon) {
        this.currentLat = lat;
        this.currentLon = lon;
        showLoading();

        // Task 1: current weather
        Task<CurrentWeather> taskCurrent = new Task<>() {
            @Override protected CurrentWeather call() throws Exception {
                return weatherService.getCurrentWeather(lat, lon);
            }
        };
        taskCurrent.setOnSucceeded(e -> updateHero(taskCurrent.getValue()));
        taskCurrent.setOnFailed(e -> {
            String msg = taskCurrent.getException() != null
                    ? taskCurrent.getException().getMessage() : "Error";
            Platform.runLater(() -> showError(msg));
        });
        Thread t1 = new Thread(taskCurrent); t1.setDaemon(true); t1.start();

        // Task 2: forecast (untuk Hourly + HumidityChart)
        Task<List<ForecastDay>> taskForecast = new Task<>() {
            @Override protected List<ForecastDay> call() throws Exception {
                return weatherService.getForecast(lat, lon);
            }
        };
        taskForecast.setOnSucceeded(e -> updateForecastWidgets(taskForecast.getValue()));
        taskForecast.setOnFailed(e -> System.err.println(
                "\u274c Gagal load forecast dashboard: "
                + (taskForecast.getException() != null ? taskForecast.getException().getMessage() : "")));
        Thread t2 = new Thread(taskForecast); t2.setDaemon(true); t2.start();
    }

    public void loadWeatherDataByCity(String cityName) {
        showLoading();
        Task<CurrentWeather> task = new Task<>() {
            @Override protected CurrentWeather call() throws Exception {
                return weatherService.getCurrentWeatherByCity(cityName);
            }
        };
        task.setOnSucceeded(e -> {
            CurrentWeather w = task.getValue();
            updateHero(w);
            loadWeatherData(w.getLatitude(), w.getLongitude());
        });
        task.setOnFailed(e -> {
            String msg = task.getException() != null ? task.getException().getMessage() : "Error";
            Platform.runLater(() -> showError(msg));
        });
        Thread t = new Thread(task); t.setDaemon(true); t.start();
    }

    // ── Update Hero section ───────────────────────────────────
    private void updateHero(CurrentWeather w) {
        Platform.runLater(() -> {
            if (labelCity    != null) labelCity.setText(w.getCityName() + ", " + w.getCountry());
            if (labelSatelliteCity != null) labelSatelliteCity.setText(w.getCityName() + ", " + w.getCountry());

            if (labelDate != null) {
                String date = java.time.LocalDate.now()
                    .format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.ENGLISH));
                labelDate.setText(date);
            }

            if (labelTemp      != null) labelTemp.setText(String.valueOf((int) w.getTemperature()));
            if (labelCondition != null) labelCondition.setText(capitalize(w.getCondition()));
            if (labelFeelsLike != null) labelFeelsLike.setText("Feels like " + (int) w.getFeelsLike() + "\u00b0C");
            if (labelHigh      != null) labelHigh.setText((int) w.getTempMax() + "\u00b0");
            if (labelLow       != null) labelLow.setText((int) w.getTempMin() + "\u00b0");

            if (iconWeather != null) iconWeather.setIconLiteral(getWeatherIcon(w.getConditionIcon()));

            // Isi detail cards
            if (detailContainer != null) {
                detailContainer.getChildren().setAll(
                    new WeatherCard("mdi2w-water-percent", "#2b8cee",
                            "Humidity", w.getHumidity() + "%",
                            "Relative humidity"),
                    new WeatherCard("mdi2w-weather-windy", "#22c55e",
                            "Wind Speed", (int) w.getWindSpeed() + " m/s",
                            "Wind speed"),
                    new WeatherCard("mdi2g-gauge", "#f59e0b",
                            "Pressure", w.getPressure() + " hPa",
                            "Atmospheric pressure"),
                    new WeatherCard("mdi2e-eye-outline", "#8b5cf6",
                            "Visibility", w.getVisibilityDisplay(),
                            "Visibility range")
                );
            }

            if (labelStatus != null) labelStatus.setVisible(false);
            System.out.println("\u2705 Hero updated: " + w.getCityName() + " " + (int)w.getTemperature() + "\u00b0C");
        });
    }

    // ── Update Hourly + Humidity dari Forecast data ───────────
    private void updateForecastWidgets(List<ForecastDay> days) {
        Platform.runLater(() -> {
            // Hourly Cards — tampilkan per hari sebagai simulasi jam
            if (hourlyContainer != null) {
                hourlyContainer.getChildren().clear();
                String[] times = {"Now", "3 PM", "6 PM", "9 PM", "Tomorrow",
                                  "Day 3", "Day 4"};
                for (int i = 0; i < Math.min(days.size(), times.length); i++) {
                    ForecastDay d = days.get(i);
                    boolean isNow = (i == 0);
                    String temp   = (int) d.getTempHigh() + "\u00b0";
                    hourlyContainer.getChildren().add(
                        new HourlyCard(times[i], d.getIconLiteral(),
                                       d.getIconColor(), temp, isNow)
                    );
                }
            }

            // Humidity Bar Chart dari data nyata
            if (humidityChartContainer != null && !days.isEmpty()) {
                humidityChartContainer.getChildren().clear();
                double w = humidityChartContainer.getWidth();
                double h = humidityChartContainer.getPrefHeight();
                if (w <= 0) w = 600;

                double[] humValues = days.stream()
                        .mapToDouble(d -> d.getHumidity())
                        .toArray();

                HumidityBarChart chart = new HumidityBarChart(w, h);
                chart.setData(
                    days.stream().map(ForecastDay::getDayName).toArray(String[]::new),
                    humValues
                );
                humidityChartContainer.getChildren().add(chart);
            }
        });
    }

    // ── State helpers ─────────────────────────────────────────
    private void showLoading() {
        Platform.runLater(() -> {
            if (labelStatus != null) {
                labelStatus.setText("\u23f3 Memuat data cuaca...");
                labelStatus.setVisible(true);
            }
            if (labelCity      != null) labelCity.setText("Memuat...");
            if (labelTemp      != null) labelTemp.setText("--");
            if (labelCondition != null) labelCondition.setText("--");
        });
    }

    private void showError(String msg) {
        if (labelStatus != null) {
            labelStatus.setText("\u274c " + (msg != null ? msg : "Terjadi kesalahan"));
            labelStatus.setVisible(true);
        }
        if (labelCity != null) labelCity.setText("Gagal memuat data");
        if (labelTemp != null) labelTemp.setText("--");
    }

    // ── Weather icon mapping ──────────────────────────────────
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
