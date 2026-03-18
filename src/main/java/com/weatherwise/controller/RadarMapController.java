package com.weatherwise.controller;

import com.weatherwise.model.CurrentWeather;
import java.util.Locale;
import com.weatherwise.service.WeatherService;
import com.weatherwise.util.AppConfig;
import com.weatherwise.util.AppState;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.net.URL;
import java.util.List;

public class RadarMapController {

    @FXML
    private WebView mapWebView;
    @FXML
    private Button btnPrecipitation;
    @FXML
    private Button btnClouds;
    @FXML
    private Button btnWind;
    @FXML
    private Button btnTemp;
    @FXML
    private Label overlayCity;
    @FXML
    private Label overlayCondition;

    private WebEngine webEngine;
    private List<Button> layerButtons;
    private boolean pageLoaded = false;
    private boolean mapInited = false;

    // Pending fly-to: disimpan jika dipanggil sebelum map siap
    private double pendingLat = Double.NaN;
    private double pendingLon = Double.NaN;
    private String pendingName = null;
    private String pendingDesc = null;

    private final WeatherService weatherService = new WeatherService();

    @FXML
    public void initialize() {
        webEngine = mapWebView.getEngine();
        layerButtons = List.of(btnPrecipitation, btnClouds, btnWind, btnTemp);

        mapWebView.setMinSize(100, 100);
        mapWebView.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        // Selalu fly ke koordinat AppState saat ini (bukan hanya saat isChanged)
        AppState state = AppState.getInstance();
        pendingLat = state.getLat();
        pendingLon = state.getLon();
        pendingName = state.getCityName();
        pendingDesc = state.getCityName();
        state.clearChanged();

        loadMap();
        fetchAndUpdateOverlay();
    }

    private void onMapReady() {
        // Dipanggil tepat setelah initMap() berhasil dieksekusi
        String apiKey = AppConfig.getApiKey();
        if (!apiKey.isEmpty()) {
            webEngine.executeScript("setApiKey('" + apiKey + "');");
        }

        // Eksekusi pending fly-to jika ada
        if (!Double.isNaN(pendingLat)) {
            String name = pendingName != null ? pendingName.replace("'", "\\'") : "";
            String desc = pendingDesc != null ? pendingDesc.replace("'", "\\'") : "";
            webEngine.executeScript(String.format(java.util.Locale.US,
                    "flyToCity(%f, %f, '%s', '%s');", pendingLat, pendingLon, name, desc));
            updateOverlay(pendingName, pendingDesc);
            // Reset pending
            pendingLat = Double.NaN;
        }
    }

    private void callInitMap() {
        double w = mapWebView.getWidth();
        double h = mapWebView.getHeight();
        if (w <= 0 && mapWebView.getScene() != null) {
            w = mapWebView.getScene().getWidth();
            h = mapWebView.getScene().getHeight() - 120;
        }
        if (w <= 0) {
            w = 1200;
        }
        if (h <= 0) {
            h = 700;
        }

        mapInited = true;
        webEngine.executeScript(
                "document.getElementById('map').style.width='" + (int) w + "px';"
                + "document.getElementById('map').style.height='" + (int) h + "px';");
        webEngine.executeScript("initMap();");
        onMapReady();
    }

    private void loadMap() {
        URL mapUrl = getClass().getResource("/map/radar_map.html");
        if (mapUrl == null) {
            System.err.println("❌ radar_map.html tidak ditemukan!");
            return;
        }

        webEngine.load(mapUrl.toExternalForm());

        webEngine.getLoadWorker().stateProperty().addListener(
                (obs, oldState, newState) -> {
                    if (newState == Worker.State.SUCCEEDED && !pageLoaded) {
                        pageLoaded = true;
                        Platform.runLater(() -> {
                            double w = mapWebView.getLayoutBounds().getWidth();
                            double h = mapWebView.getLayoutBounds().getHeight();
                            if (w > 0 && h > 0 && !mapInited) {
                                mapInited = true;
                                webEngine.executeScript(
                                        "document.getElementById('map').style.width='" + (int) w + "px';"
                                        + "document.getElementById('map').style.height='" + (int) h + "px';");
                                webEngine.executeScript("initMap();");
                                onMapReady();
                            } else if (!mapInited) {
                                // Fallback: tunggu scene
                                mapWebView.sceneProperty().addListener((o, os, ns) -> {
                                    if (ns != null && !mapInited) {
                                        callInitMap();
                                    }
                                });
                            }
                        });
                    }
                }
        );
    }

    private void execJS(String script) {
        if (pageLoaded) {
            Platform.runLater(() -> webEngine.executeScript(script));
        }
    }

    public void updateOverlay(String city, String condition) {
        Platform.runLater(() -> {
            if (overlayCity != null) {
                overlayCity.setText(city);
            }
            if (overlayCondition != null) {
                overlayCondition.setText(condition);
            }
        });
    }

    public void flyToCity(double lat, double lng, String name, String desc) {
        if (pageLoaded && mapInited) {
            // Map sudah siap, langsung eksekusi
            String n = name != null ? name.replace("'", "\\'") : "";
            String d = desc != null ? desc.replace("'", "\\'") : "";
            execJS(String.format(java.util.Locale.US,
                    "flyToCity(%f, %f, '%s', '%s');", lat, lng, n, d));
            updateOverlay(name, desc);
        } else {
            // Map belum siap, simpan sebagai pending
            pendingLat = lat;
            pendingLon = lng;
            pendingName = name;
            pendingDesc = desc;
        }
    }

    private void fetchAndUpdateOverlay() {
        AppState state = AppState.getInstance();
        double lat = state.getLat();
        double lon = state.getLon();

        Task<CurrentWeather> task = new Task<>() {
            @Override
            protected CurrentWeather call() throws Exception {
                return weatherService.getCurrentWeather(lat, lon);
            }
        };
        task.setOnSucceeded(e -> {
            CurrentWeather w = task.getValue();
            String city = w.getCityName() + ", " + w.getCountry();
            String cond = capitalize(w.getCondition()) + " · " + (int) w.getTemperature() + "°C";
            updateOverlay(city, cond);
        });
        task.setOnFailed(e -> updateOverlay(state.getCityName(), "Gagal memuat cuaca"));
        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }

    @FXML
    private void handleZoomIn() {
        execJS("zoomIn();");
    }

    @FXML
    private void handleZoomOut() {
        execJS("zoomOut();");
    }

    @FXML
    private void handleMyLocation() {
        execJS("goToCurrentLocation();");
    }

    @FXML
    private void handleLayerPrecipitation() {
        execJS("setWeatherLayer('precipitation');");
        setActiveLayer(btnPrecipitation);
    }

    @FXML
    private void handleLayerClouds() {
        execJS("setWeatherLayer('clouds');");
        setActiveLayer(btnClouds);
    }

    @FXML
    private void handleLayerWind() {
        execJS("setWeatherLayer('wind');");
        setActiveLayer(btnWind);
    }

    @FXML
    private void handleLayerTemp() {
        execJS("setWeatherLayer('temp');");
        setActiveLayer(btnTemp);
    }

    @FXML
    private void handleLayers() {
    }

    @FXML
    private void handleShare() {
    }

    private void setActiveLayer(Button activeBtn) {
        String normal = "-fx-background-color: rgba(255,255,255,0.92);"
                + "-fx-text-fill: #475569; -fx-font-size: 12px;"
                + "-fx-background-radius: 20; -fx-cursor: hand;"
                + "-fx-padding: 8 16 8 12;"
                + "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.08),6,0,0,1);";
        String active = "-fx-background-color: #2b8cee;"
                + "-fx-text-fill: white; -fx-font-weight: bold;"
                + "-fx-font-size: 12px; -fx-background-radius: 20;"
                + "-fx-cursor: hand; -fx-padding: 8 16 8 12;";
        layerButtons.forEach(b -> b.setStyle(normal));
        activeBtn.setStyle(active);
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
