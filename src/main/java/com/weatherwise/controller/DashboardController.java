package com.weatherwise.controller;

import com.weatherwise.component.HourlyCard;
import com.weatherwise.component.HumidityBarChart;
import com.weatherwise.component.WeatherCard;
import com.weatherwise.model.CurrentWeather;
import com.weatherwise.model.ForecastDay;
import com.weatherwise.service.WeatherService;
import com.weatherwise.util.AppState;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.LocalDate;
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

    private double  currentLat  = -6.2088;
    private double  currentLon  = 106.8456;

    // Flag: cegah update UI setelah controller di-dispose
    private volatile boolean active = true;

    @FXML
    public void initialize() {
        AppState state = AppState.getInstance();
        this.currentLat = state.getLat();
        this.currentLon = state.getLon();
        state.clearChanged();
        // showLoading() harus di FX thread — initialize() sudah di FX thread, aman
        showLoading();
        loadWeatherData(currentLat, currentLon);
    }

    // Dipanggil saat controller dibuang (halaman diganti)
    public void dispose() {
        active = false;
    }

    public void loadWeatherData(double lat, double lon) {
        this.currentLat = lat;
        this.currentLon = lon;

        // Pastikan showLoading() selalu di FX thread
        if (Platform.isFxApplicationThread()) {
            showLoading();
        } else {
            Platform.runLater(this::showLoading);
        }

        // Task 1: Current Weather
        Task<CurrentWeather> taskCurrent = new Task<>() {
            @Override protected CurrentWeather call() throws Exception {
                return weatherService.getCurrentWeather(lat, lon);
            }
        };
        taskCurrent.setOnSucceeded(e -> {
            if (!active) return; // controller sudah tidak aktif
            updateHero(taskCurrent.getValue());
        });
        taskCurrent.setOnFailed(e -> {
            if (!active) return;
            String msg = taskCurrent.getException() != null
                ? taskCurrent.getException().getMessage() : "Gagal memuat data";
            Platform.runLater(() -> showError(msg));
        });
        Thread t1 = new Thread(taskCurrent);
        t1.setDaemon(true);
        t1.start();

        // Task 2: Forecast (Hourly + HumidityChart)
        Task<List<ForecastDay>> taskForecast = new Task<>() {
            @Override protected List<ForecastDay> call() throws Exception {
                return weatherService.getForecast(lat, lon);
            }
        };
        taskForecast.setOnSucceeded(e -> {
            if (!active) return;
            updateForecastWidgets(taskForecast.getValue());
        });
        taskForecast.setOnFailed(e -> {
            if (!active) return;
            System.err.println("❌ Gagal load forecast: "
                + (taskForecast.getException() != null
                    ? taskForecast.getException().getMessage() : ""));
        });
        Thread t2 = new Thread(taskForecast);
        t2.setDaemon(true);
        t2.start();
    }

    public void loadWeatherDataByCity(String cityName) {
        if (Platform.isFxApplicationThread()) showLoading();
        else Platform.runLater(this::showLoading);

        Task<CurrentWeather> task = new Task<>() {
            @Override protected CurrentWeather call() throws Exception {
                return weatherService.getCurrentWeatherByCity(cityName);
            }
        };
        task.setOnSucceeded(e -> {
            if (!active) return;
            CurrentWeather w = task.getValue();
            updateHero(w);
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

    private void updateHero(CurrentWeather w) {
        Platform.runLater(() -> {
            if (!active) return;
            if (labelCity      != null) labelCity.setText(w.getCityName() + ", " + w.getCountry());
            if (labelDate      != null) labelDate.setText(
                LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.ENGLISH)));
            if (labelTemp      != null) labelTemp.setText(String.valueOf((int) w.getTemperature()));
            if (labelCondition != null) labelCondition.setText(capitalize(w.getCondition()));
            if (labelFeelsLike != null) labelFeelsLike.setText("Feels like " + (int) w.getFeelsLike() + "°C");
            if (labelHigh      != null) labelHigh.setText((int) w.getTempMax() + "°");
            if (labelLow       != null) labelLow.setText((int) w.getTempMin() + "°");
            if (iconWeather    != null) iconWeather.setIconLiteral(getWeatherIcon(w.getConditionIcon()));

            if (detailContainer != null) {
                detailContainer.getChildren().setAll(
                    new WeatherCard("mdi2w-water-percent", "#2b8cee",
                        "Humidity", w.getHumidity() + "%", "Relative humidity"),
                    new WeatherCard("mdi2w-weather-windy", "#22c55e",
                        "Wind Speed", (int) w.getWindSpeed() + " m/s", "Wind speed"),
                    new WeatherCard("mdi2g-gauge", "#f59e0b",
                        "Pressure", w.getPressure() + " hPa", "Atmospheric pressure"),
                    new WeatherCard("mdi2e-eye-outline", "#8b5cf6",
                        "Visibility", w.getVisibilityDisplay(), "Visibility range")
                );
            }
            if (labelStatus != null) labelStatus.setVisible(false);
        });
    }

    private void updateForecastWidgets(List<ForecastDay> days) {
        Platform.runLater(() -> {
            if (!active || days == null || days.isEmpty()) return;

            if (hourlyContainer != null) {
                hourlyContainer.getChildren().clear();
                String[] times = {"Now","3 PM","6 PM","9 PM","Tomorrow","Day 3","Day 4"};
                for (int i = 0; i < Math.min(days.size(), times.length); i++) {
                    ForecastDay d = days.get(i);
                    hourlyContainer.getChildren().add(new HourlyCard(
                        times[i], d.getIconLiteral(), d.getIconColor(),
                        (int) d.getTempHigh() + "°", i == 0));
                }
            }

            if (humidityChartContainer != null) {
                humidityChartContainer.getChildren().clear();
                // Pakai prefWidth jika width belum tersedia (node belum di-layout)
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

    private void showLoading() {
        if (labelStatus   != null) { labelStatus.setText("⏳ Memuat data cuaca..."); labelStatus.setVisible(true); }
        if (labelCity     != null) labelCity.setText("Memuat...");
        if (labelTemp     != null) labelTemp.setText("--");
        if (labelCondition!= null) labelCondition.setText("--");
    }

    private void showError(String msg) {
        if (labelStatus   != null) { labelStatus.setText("❌ " + (msg != null ? msg : "Terjadi kesalahan")); labelStatus.setVisible(true); }
        if (labelCity     != null) labelCity.setText("Gagal memuat data");
        if (labelTemp     != null) labelTemp.setText("--");
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

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    public double getCurrentLat() { return currentLat; }
    public double getCurrentLon() { return currentLon; }
}
