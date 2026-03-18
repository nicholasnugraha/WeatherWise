package com.weatherwise.controller;

import com.weatherwise.component.ForecastRowCard;
import com.weatherwise.component.HumidityBarChart;
import com.weatherwise.model.ForecastDay;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.util.List;

public class ForecastController {

    @FXML private VBox  forecastList;
    @FXML private Pane  humidityChartContainer;
    @FXML private Button btnCelsius;
    @FXML private Button btnFahrenheit;

    // Data 7 hari dalam Celsius
    private final List<ForecastDay> forecastData = List.of(
        new ForecastDay("Today",     "Oct 24",
            "mdi2w-weather-partly-cloudy", "#64748b",
            "Partly Cloudy", "Light breeze with moderate humidity", 20, 12),
        new ForecastDay("Tuesday",   "Oct 25",
            "mdi2w-weather-sunny",         "#f59e0b",
            "Sunny",         "Clear skies all day long",           24, 14),
        new ForecastDay("Wednesday", "Oct 26",
            "mdi2w-weather-rainy",         "#2b8cee",
            "Showers",       "Intermittent rain in the morning",   17, 10),
        new ForecastDay("Thursday",  "Oct 27",
            "mdi2w-weather-cloudy",        "#94a3b8",
            "Overcast",      "Dense cloud, no precipitation",      19, 11),
        new ForecastDay("Friday",    "Oct 28",
            "mdi2w-weather-partly-cloudy", "#f59e0b",
            "Mostly Sunny",  "Perfect for outdoor activities",     22, 13),
        new ForecastDay("Saturday",  "Oct 29",
            "mdi2w-weather-lightning",     "#6366f1",
            "Thunderstorm",  "Heavy rain with possible wind gusts",16,  9),
        new ForecastDay("Sunday",    "Oct 30",
            "mdi2w-weather-partly-cloudy", "#64748b",
            "Partly Cloudy", "Clouds clearing by mid-afternoon",   21, 12)
    );

    private boolean isCelsius = true;

    @FXML
    public void initialize() {
        renderForecastList();
        setupHumidityChart();
    }

    // ── Render daftar 7 hari ──────────────────────────────────
    private void renderForecastList() {
        forecastList.getChildren().clear();

        for (ForecastDay day : forecastData) {
            ForecastDay converted = isCelsius ? day : toFahrenheit(day);
            ForecastRowCard card = new ForecastRowCard(converted);
            forecastList.getChildren().add(card);
        }
    }

    // ── Toggle Celsius ────────────────────────────────────────
    @FXML
    private void handleCelsius() {
        if (!isCelsius) {
            isCelsius = true;
            renderForecastList();
            setActiveUnit(btnCelsius, btnFahrenheit);
        }
    }

    // ── Toggle Fahrenheit ─────────────────────────────────────
    @FXML
    private void handleFahrenheit() {
        if (isCelsius) {
            isCelsius = false;
            renderForecastList();
            setActiveUnit(btnFahrenheit, btnCelsius);
        }
    }

    // ── Konversi C → F ────────────────────────────────────────
    private ForecastDay toFahrenheit(ForecastDay day) {
        int highF = (int) Math.round(day.getHighTemp() * 9.0 / 5.0 + 32);
        int lowF  = (int) Math.round(day.getLowTemp()  * 9.0 / 5.0 + 32);
        return new ForecastDay(
            day.getDayName(), day.getDate(),
            day.getIconLiteral(), day.getIconColor(),
            day.getCondition(), day.getDescription(),
            highF, lowF
        );
    }

    // ── Update tampilan tombol aktif ──────────────────────────
    private void setActiveUnit(Button active, Button inactive) {
        active.setStyle("""
                -fx-background-color: #2b8cee;
                -fx-text-fill: white;
                -fx-font-weight: bold;
                -fx-font-size: 13px;
                -fx-background-radius: 7;
                -fx-cursor: hand;
                -fx-pref-width: 48;
                -fx-pref-height: 32;
                """);
        inactive.setStyle("""
                -fx-background-color: transparent;
                -fx-text-fill: #64748b;
                -fx-font-weight: bold;
                -fx-font-size: 13px;
                -fx-background-radius: 7;
                -fx-cursor: hand;
                -fx-pref-width: 48;
                -fx-pref-height: 32;
                """);
    }

    // ── Bar Chart Humidity ────────────────────────────────────
    private void setupHumidityChart() {
        humidityChartContainer.widthProperty().addListener((obs, oldVal, newVal) -> {
            humidityChartContainer.getChildren().clear();
            HumidityBarChart chart = new HumidityBarChart(
                newVal.doubleValue(), 120
            );
            humidityChartContainer.getChildren().add(chart);
        });
    }

    // ── Tombol buka peta ──────────────────────────────────────
    @FXML
    private void handleOpenMap() {
        System.out.println("Buka halaman Radar Map...");
        // Nanti akan navigasi ke halaman RadarMap
    }
}
