package com.weatherwise.controller;

import com.weatherwise.component.ForecastRowCard;
import com.weatherwise.component.HumidityBarChart;
import com.weatherwise.model.ForecastDay;
import com.weatherwise.service.WeatherService;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.util.List;

public class ForecastController {

    @FXML private VBox   forecastContainer;       // fx:id di FXML
    @FXML private Label  labelCity;
    @FXML private Label  labelStatus;
    @FXML private Button btnCelsius;
    @FXML private Button btnFahrenheit;
    @FXML private Pane   humidityChartContainer;

    private final WeatherService weatherService = new WeatherService();

    private double  currentLat  = -6.2088;
    private double  currentLon  = 106.8456;
    private String  currentCity = "Jakarta, ID";
    private boolean isCelsius   = true;

    private List<ForecastDay> cachedForecast = null;

    @FXML
    public void initialize() {
        if (labelCity != null) labelCity.setText(currentCity);
        refreshUnitButtons();
        loadForecast(currentLat, currentLon);
    }

    // ── Load Forecast ─────────────────────────────────────────
    public void loadForecast(double lat, double lon) {
        this.currentLat = lat;
        this.currentLon = lon;
        showLoading();

        Task<List<ForecastDay>> task = new Task<>() {
            @Override protected List<ForecastDay> call() throws Exception {
                return weatherService.getForecast(lat, lon);
            }
        };

        task.setOnSucceeded(e -> {
            cachedForecast = task.getValue();
            Platform.runLater(() -> renderForecast(cachedForecast));
        });

        task.setOnFailed(e -> {
            String msg = task.getException() != null
                    ? task.getException().getMessage() : "Gagal memuat data";
            System.err.println("\u274c Gagal load forecast: " + msg);
            Platform.runLater(() -> showError(msg));
        });

        Thread t = new Thread(task); t.setDaemon(true); t.start();
    }

    public void setLocation(double lat, double lon, String cityName) {
        this.currentLat  = lat;
        this.currentLon  = lon;
        this.currentCity = cityName;
        if (labelCity != null) labelCity.setText(cityName);
        loadForecast(lat, lon);
    }

    // ── Render kartu forecast menggunakan ForecastRowCard ─────
    private void renderForecast(List<ForecastDay> days) {
        if (forecastContainer == null) return;
        forecastContainer.getChildren().clear();
        if (labelStatus != null) labelStatus.setVisible(false);

        for (ForecastDay day : days) {
            // Konversi suhu ke Fahrenheit jika diperlukan
            if (!isCelsius) {
                ForecastDay converted = new ForecastDay();
                converted.setDayName(day.getDayName());
                converted.setDate(day.getDate());
                converted.setCondition(day.getCondition());
                converted.setDescription(day.getDescription());
                converted.setConditionIcon(day.getConditionIcon());
                converted.setIconLiteral(day.getIconLiteral());
                converted.setIconColor(day.getIconColor());
                converted.setTempHigh(celsiusToF(day.getTempHigh()));
                converted.setTempLow(celsiusToF(day.getTempLow()));
                converted.setHumidity(day.getHumidity());
                converted.setWindSpeed(day.getWindSpeed());
                forecastContainer.getChildren().add(new ForecastRowCard(converted));
            } else {
                forecastContainer.getChildren().add(new ForecastRowCard(day));
            }
        }

        // Render humidity chart dari data nyata
        if (humidityChartContainer != null && !days.isEmpty()) {
            humidityChartContainer.getChildren().clear();
            double w = humidityChartContainer.getWidth();
            double h = humidityChartContainer.getPrefHeight();
            if (w <= 0) w = 400;

            HumidityBarChart chart = new HumidityBarChart(w, h);
            chart.setData(
                days.stream().map(ForecastDay::getDayName).toArray(String[]::new),
                days.stream().mapToDouble(ForecastDay::getHumidity).toArray()
            );
            humidityChartContainer.getChildren().add(chart);
        }
    }

    // ── Unit Toggle ───────────────────────────────────────────
    @FXML private void handleCelsius() {
        if (!isCelsius) {
            isCelsius = true;
            refreshUnitButtons();
            if (cachedForecast != null) renderForecast(cachedForecast);
        }
    }

    @FXML private void handleFahrenheit() {
        if (isCelsius) {
            isCelsius = false;
            refreshUnitButtons();
            if (cachedForecast != null) renderForecast(cachedForecast);
        }
    }

    private void refreshUnitButtons() {
        String active = "-fx-background-color: #2b8cee; -fx-text-fill: white;"
                      + "-fx-font-weight: bold; -fx-font-size: 13px;"
                      + "-fx-background-radius: 7; -fx-cursor: hand;"
                      + "-fx-pref-width: 48; -fx-pref-height: 32;";
        String normal = "-fx-background-color: transparent; -fx-text-fill: #64748b;"
                      + "-fx-font-weight: bold; -fx-font-size: 13px;"
                      + "-fx-background-radius: 7; -fx-cursor: hand;"
                      + "-fx-pref-width: 48; -fx-pref-height: 32;";
        if (btnCelsius    != null) btnCelsius.setStyle(isCelsius  ? active : normal);
        if (btnFahrenheit != null) btnFahrenheit.setStyle(!isCelsius ? active : normal);
    }

    // ── Tombol Open Map ───────────────────────────────────────
    @FXML
    private void handleOpenMap() {
        // Navigasi ke halaman Maps melalui MainWindowController
        // Cari MainWindowController di scene graph
        if (forecastContainer != null && forecastContainer.getScene() != null) {
            javafx.scene.Node root = forecastContainer.getScene().getRoot();
            // Trigger via lookup — cari tombol Maps di sidebar
            javafx.scene.Node mapsBtn = root.lookup("#sidebarMaps");
            if (mapsBtn instanceof javafx.scene.control.Button btn) {
                btn.fire();
            }
        }
    }

    // ── State Helpers ─────────────────────────────────────────
    private void showLoading() {
        if (forecastContainer != null) forecastContainer.getChildren().clear();
        if (labelStatus != null) {
            labelStatus.setText("\u23f3 Memuat prakiraan cuaca...");
            labelStatus.setVisible(true);
        }
    }

    private void showError(String msg) {
        if (labelStatus != null) {
            labelStatus.setText("\u274c " + (msg != null ? msg : "Gagal memuat data"));
            labelStatus.setVisible(true);
        }
    }

    private double celsiusToF(double c) { return c * 9.0 / 5.0 + 32; }
}
