package com.weatherwise.controller;

import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.net.URL;
import java.util.List;

public class RadarMapController {

    @FXML private WebView  mapWebView;
    @FXML private Button   btnPrecipitation;
    @FXML private Button   btnClouds;
    @FXML private Button   btnWind;
    @FXML private Button   btnTemp;

    private WebEngine    webEngine;
    private List<Button> layerButtons;
    private boolean      pageLoaded = false;
    private boolean      mapInited  = false;

    @FXML
    public void initialize() {
        webEngine    = mapWebView.getEngine();
        layerButtons = List.of(btnPrecipitation, btnClouds,
                                btnWind, btnTemp);

        // KUNCI FIX #1: Set min size eksplisit sebelum load
        // WebView HARUS punya ukuran > 0 sebelum Leaflet init
        mapWebView.setMinSize(300, 300);

        loadMap();

        // KUNCI FIX #2: Gunakan layoutBoundsProperty untuk tahu
        // kapan WebView benar-benar punya ukuran nyata dari layout
        mapWebView.layoutBoundsProperty().addListener((obs, oldBounds, newBounds) -> {
            double w = newBounds.getWidth();
            double h = newBounds.getHeight();

            if (w > 0 && h > 0 && pageLoaded && !mapInited) {
                mapInited = true;
                Platform.runLater(() -> {
                    System.out.println("📐 Layout bounds: " + w + "x" + h);
                    webEngine.executeScript(
                        "document.getElementById('map').style.width='"  + (int)w + "px';" +
                        "document.getElementById('map').style.height='" + (int)h + "px';"
                    );
                    webEngine.executeScript("initMap();");
                });
            }
        });
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
                    System.out.println("✅ HTML loaded.");

                    // Cek apakah layout bounds sudah ada
                    Platform.runLater(() -> {
                        double w = mapWebView.getLayoutBounds().getWidth();
                        double h = mapWebView.getLayoutBounds().getHeight();

                        if (w > 0 && h > 0 && !mapInited) {
                            mapInited = true;
                            System.out.println("📐 Bounds saat load: " + w + "x" + h);
                            webEngine.executeScript(
                                "document.getElementById('map').style.width='"  + (int)w + "px';" +
                                "document.getElementById('map').style.height='" + (int)h + "px';"
                            );
                            webEngine.executeScript("initMap();");
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

    @FXML private void handleZoomIn()     { execJS("zoomIn();");              }
    @FXML private void handleZoomOut()    { execJS("zoomOut();");             }
    @FXML private void handleMyLocation() { execJS("goToCurrentLocation();"); }

    @FXML private void handleLayerPrecipitation() {
        execJS("setWeatherLayer('precipitation');");
        setActiveLayer(btnPrecipitation);
    }
    @FXML private void handleLayerClouds() {
        execJS("setWeatherLayer('clouds');");
        setActiveLayer(btnClouds);
    }
    @FXML private void handleLayerWind() {
        execJS("setWeatherLayer('wind');");
        setActiveLayer(btnWind);
    }
    @FXML private void handleLayerTemp() {
        execJS("setWeatherLayer('temp');");
        setActiveLayer(btnTemp);
    }
    @FXML private void handleLayers() {}
    @FXML private void handleShare()  {}

    private void setActiveLayer(Button activeBtn) {
        String normal = "-fx-background-color: rgba(255,255,255,0.92);" +
                        "-fx-text-fill: #475569; -fx-font-size: 12px;" +
                        "-fx-background-radius: 20; -fx-cursor: hand;" +
                        "-fx-padding: 8 16 8 12;" +
                        "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.08),6,0,0,1);";
        String active = "-fx-background-color: #2b8cee;" +
                        "-fx-text-fill: white; -fx-font-weight: bold;" +
                        "-fx-font-size: 12px; -fx-background-radius: 20;" +
                        "-fx-cursor: hand; -fx-padding: 8 16 8 12;";
        layerButtons.forEach(b -> b.setStyle(normal));
        activeBtn.setStyle(active);
    }

    public void flyToCity(double lat, double lng, String name, String desc) {
        execJS(String.format("flyToCity(%f, %f, '%s', '%s');", lat, lng, name, desc));
    }
}
