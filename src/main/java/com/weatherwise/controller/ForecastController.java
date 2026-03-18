package com.weatherwise.controller;

import com.weatherwise.model.ForecastDay;
import com.weatherwise.service.WeatherService;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.List;

public class ForecastController {

    @FXML private VBox  forecastContainer;
    @FXML private Label labelCity;
    @FXML private Label labelStatus;
    @FXML private Button btnCelsius;
    @FXML private Button btnFahrenheit;

    private final WeatherService weatherService = new WeatherService();

    private double  currentLat  = 37.7749;
    private double  currentLon  = -122.4194;
    private String  currentCity = "San Francisco, CA";
    private boolean isCelsius   = true;

    private List<ForecastDay> cachedForecast = null;

    @FXML
    public void initialize() {
        if (labelCity != null) labelCity.setText(currentCity);
        refreshUnitButtons();
        loadForecast(currentLat, currentLon);
    }

    // ── Load Forecast ──────────────────────────────────────────
    public void loadForecast(double lat, double lon) {
        this.currentLat = lat;
        this.currentLon = lon;

        showLoading();

        Task<List<ForecastDay>> task = new Task<>() {
            @Override
            protected List<ForecastDay> call() throws Exception {
                return weatherService.getForecast(lat, lon);
            }
        };

        task.setOnSucceeded(e -> {
            cachedForecast = task.getValue();
            renderForecast(cachedForecast);
        });

        task.setOnFailed(e -> {
            System.err.println("❌ Gagal load forecast: "
                + task.getException().getMessage());
            showError(task.getException().getMessage());
        });

        new Thread(task).start();
    }

    public void setLocation(double lat, double lon, String cityName) {
        this.currentLat  = lat;
        this.currentLon  = lon;
        this.currentCity = cityName;
        if (labelCity != null) labelCity.setText(cityName);
        loadForecast(lat, lon);
    }

    // ── Render Card ────────────────────────────────────────────
    private void renderForecast(List<ForecastDay> days) {
        if (forecastContainer == null) return;
        forecastContainer.getChildren().clear();

        if (labelStatus != null) labelStatus.setVisible(false);

        for (ForecastDay day : days) {
            HBox card = buildForecastCard(day);
            forecastContainer.getChildren().add(card);
        }
    }

    private HBox buildForecastCard(ForecastDay day) {
        HBox card = new HBox(16);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 14;
            -fx-padding: 14 20 14 20;
            -fx-effect: dropshadow(gaussian,rgba(0,0,0,0.06),10,0,0,2);
            """);

        // ── Hari & Tanggal ─────────────────────────────────────
        VBox dayBox = new VBox(3);
        dayBox.setMinWidth(110);

        Label dayLabel = new Label(day.getDayName());
        dayLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;"
                        + "-fx-text-fill: #0f172a;");

        Label dateLabel = new Label(day.getDate().toUpperCase());
        dateLabel.setStyle("-fx-font-size: 10px; -fx-font-weight: bold;"
                         + "-fx-text-fill: #94a3b8;");

        dayBox.getChildren().addAll(dayLabel, dateLabel);

        // ── Icon ───────────────────────────────────────────────
        VBox iconBox = new VBox();
        iconBox.setAlignment(Pos.CENTER);
        iconBox.setMinWidth(52);
        iconBox.setMaxWidth(52);
        iconBox.setStyle("-fx-background-color: #eff6ff;"
                       + "-fx-background-radius: 10;");

        FontIcon icon = new FontIcon();
        icon.setIconLiteral(getWeatherIcon(day.getConditionIcon()));
        icon.setIconSize(24);
        icon.setIconColor(javafx.scene.paint.Color.web("#2b8cee"));
        iconBox.getChildren().add(icon);

        // ── Kondisi ────────────────────────────────────────────
        VBox condBox = new VBox(3);
        condBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(condBox, Priority.ALWAYS);

        Label condLabel = new Label(capitalize(day.getCondition()));
        condLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 600;"
                         + "-fx-text-fill: #0f172a;");

        Label detailLabel = new Label("Humidity "  + day.getHumidity()
                                    + "% · Wind " + (int) day.getWindSpeed()
                                    + " m/s");
        detailLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");

        condBox.getChildren().addAll(condLabel, detailLabel);

        // ── Suhu ───────────────────────────────────────────────
        HBox tempBox = new HBox(12);
        tempBox.setAlignment(Pos.CENTER_RIGHT);

        double high = isCelsius ? day.getTempHigh()
                                : celsiusToF(day.getTempHigh());
        double low  = isCelsius ? day.getTempLow()
                                : celsiusToF(day.getTempLow());
        String unit = isCelsius ? "°C" : "°F";

        VBox highBox = new VBox(1);
        highBox.setAlignment(Pos.CENTER_RIGHT);
        Label highTemp = new Label((int) high + unit);
        highTemp.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;"
                        + "-fx-text-fill: #0f172a;");
        Label highLbl = new Label("High");
        highLbl.setStyle("-fx-font-size: 9px; -fx-text-fill: #94a3b8;"
                       + "-fx-font-weight: bold;");
        highBox.getChildren().addAll(highTemp, highLbl);

        VBox lowBox = new VBox(1);
        lowBox.setAlignment(Pos.CENTER_RIGHT);
        Label lowTemp = new Label((int) low + unit);
        lowTemp.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;"
                       + "-fx-text-fill: #94a3b8;");
        Label lowLbl = new Label("Low");
        lowLbl.setStyle("-fx-font-size: 9px; -fx-text-fill: #94a3b8;"
                      + "-fx-font-weight: bold;");
        lowBox.getChildren().addAll(lowTemp, lowLbl);

        tempBox.getChildren().addAll(highBox, lowBox);

        card.getChildren().addAll(dayBox, iconBox, condBox, tempBox);
        return card;
    }

    // ── Unit Toggle ────────────────────────────────────────────
    @FXML
    private void handleCelsius() {
        if (!isCelsius) {
            isCelsius = true;
            refreshUnitButtons();
            if (cachedForecast != null) renderForecast(cachedForecast);
        }
    }

    @FXML
    private void handleFahrenheit() {
        if (isCelsius) {
            isCelsius = false;
            refreshUnitButtons();
            if (cachedForecast != null) renderForecast(cachedForecast);
        }
    }

    private void refreshUnitButtons() {
        String active = "-fx-background-color: #2b8cee; -fx-text-fill: white;"
                      + "-fx-font-weight: bold; -fx-font-size: 12px;"
                      + "-fx-background-radius: 8; -fx-cursor: hand;"
                      + "-fx-padding: 6 16 6 16;";
        String normal = "-fx-background-color: transparent;"
                      + "-fx-text-fill: #64748b; -fx-font-size: 12px;"
                      + "-fx-background-radius: 8; -fx-cursor: hand;"
                      + "-fx-padding: 6 16 6 16;";

        if (btnCelsius    != null) btnCelsius.setStyle(isCelsius  ? active : normal);
        if (btnFahrenheit != null) btnFahrenheit.setStyle(!isCelsius ? active : normal);
    }

    // ── State Helpers ──────────────────────────────────────────
    private void showLoading() {
        if (forecastContainer != null) forecastContainer.getChildren().clear();
        if (labelStatus != null) {
            labelStatus.setText("⏳ Memuat prakiraan cuaca...");
            labelStatus.setVisible(true);
        }
    }

    private void showError(String msg) {
        if (labelStatus != null) {
            labelStatus.setText("❌ " + (msg != null ? msg : "Gagal memuat data"));
            labelStatus.setVisible(true);
        }
    }

    // ── Helpers ────────────────────────────────────────────────
    private double celsiusToF(double c) { return c * 9.0 / 5.0 + 32; }

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
}
