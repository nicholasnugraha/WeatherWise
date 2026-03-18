package com.weatherwise.controller;

import com.weatherwise.component.HourlyCard;
import com.weatherwise.component.WeatherCard;
import com.weatherwise.component.HumidityBarChart;
import javafx.fxml.FXML;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javafx.scene.control.Label;
import org.kordamp.ikonli.javafx.FontIcon;

public class DashboardController {

    @FXML private Label locationLabel;
    @FXML private Label dateLabel;
    @FXML private Label tempLabel;
    @FXML private Label conditionLabel;
    @FXML private Label highLabel;
    @FXML private Label lowLabel;
    @FXML private Label feelsLikeLabel;
    @FXML private FontIcon mainWeatherIcon;
    @FXML private HBox hourlyContainer;
    @FXML private HBox detailContainer;
    @FXML private Pane humidityChartContainer;

    @FXML
    public void initialize() {
        setupDate();
        setupHourlyForecast();
        setupDetailCards();
        setupHumidityChart();
    }

    // ── Tanggal hari ini ──────────────────────────────────────
    private void setupDate() {
        String today = LocalDate.now().format(
            DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")
        );
        dateLabel.setText(today);
    }

    // ── Kartu Prakiraan Per Jam ───────────────────────────────
    private void setupHourlyForecast() {
        String[][] hourlyData = {
            {"Now",  "mdi2w-weather-sunny",        "#f59e0b", "72°"},
            {"1 PM", "mdi2w-weather-sunny",        "#f59e0b", "74°"},
            {"2 PM", "mdi2w-weather-partly-cloudy","#64748b", "75°"},
            {"3 PM", "mdi2w-weather-partly-cloudy","#64748b", "74°"},
            {"4 PM", "mdi2w-weather-cloudy",       "#94a3b8", "71°"},
            {"5 PM", "mdi2w-weather-rainy",        "#2b8cee", "68°"},
            {"6 PM", "mdi2w-weather-rainy",        "#2b8cee", "65°"},
            {"7 PM", "mdi2w-weather-cloudy",       "#94a3b8", "63°"},
            {"8 PM", "mdi2w-weather-night",        "#475569", "61°"},
            {"9 PM", "mdi2w-weather-night",        "#475569", "60°"},
        };

        for (int i = 0; i < hourlyData.length; i++) {
            String[] d = hourlyData[i];
            boolean isNow = (i == 0);
            HourlyCard card = new HourlyCard(d[0], d[1], d[2], d[3], isNow);
            hourlyContainer.getChildren().add(card);
        }
    }

    // ── Kartu Detail Cuaca ────────────────────────────────────
    private void setupDetailCards() {
        WeatherCard[] cards = {
            new WeatherCard(
                "mdi2w-weather-sunny-alert", "#f59e0b",
                "UV Index", "5",
                "Moderate — use sunscreen"
            ),
            new WeatherCard(
                "mdi2w-water-percent", "#2b8cee",
                "Humidity", "62%",
                "Comfortable range"
            ),
            new WeatherCard(
                "mdi2w-weather-windy", "#06b6d4",
                "Wind Speed", "12 mph",
                "From SW direction"
            ),
            new WeatherCard(
                "mdi2w-weather-sunset-up", "#f97316",
                "Sunrise", "6:42 AM",
                "Sunset at 6:28 PM"
            ),
        };

        detailContainer.getChildren().addAll(cards);

        // Tiap card grow mengisi lebar yang tersedia
        for (WeatherCard card : cards) {
            HBox.setHgrow(card, javafx.scene.layout.Priority.ALWAYS);
        }
    }

    // ── Bar Chart Humidity Mingguan ───────────────────────────
    private void setupHumidityChart() {
        // Ambil lebar container setelah layout selesai
        humidityChartContainer.widthProperty().addListener((obs, oldVal, newVal) -> {
            humidityChartContainer.getChildren().clear();
            HumidityBarChart chart = new HumidityBarChart(
                newVal.doubleValue(), 128
            );
            humidityChartContainer.getChildren().add(chart);
        });
    }
}
