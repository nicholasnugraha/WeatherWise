package com.weatherwise.controller;

import com.weatherwise.App;
import com.weatherwise.model.Location;
import com.weatherwise.service.GeocodingService;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.util.List;

public class MainWindowController {

    @FXML private StackPane  contentArea;
    @FXML private TextField  searchField;

    @FXML private Button sidebarDashboard;
    @FXML private Button sidebarMaps;
    @FXML private Button sidebarForecast;
    @FXML private Button sidebarSettings;

    @FXML private Button navDashboard;
    @FXML private Button navMaps;
    @FXML private Button navForecast;
    @FXML private Button navSettings;

    private List<Button> sidebarButtons;
    private List<Button> topNavButtons;

    private final GeocodingService geocodingService = new GeocodingService();

    // Referensi controller aktif
    private Object activeController = null;

    @FXML
    public void initialize() {
        sidebarButtons = List.of(sidebarDashboard, sidebarMaps,
                                  sidebarForecast, sidebarSettings);
        topNavButtons  = List.of(navDashboard, navMaps,
                                  navForecast, navSettings);

        if (searchField != null) {
            searchField.setOnAction(e -> handleGlobalSearch());
        }

        loadPage("Dashboard");
    }

    // ── Navigasi ──────────────────────────────────────────────
    @FXML private void handleNavDashboard() { setActiveNav(sidebarDashboard, navDashboard); loadPage("Dashboard"); }
    @FXML private void handleNavMaps()      { setActiveNav(sidebarMaps, navMaps);           loadPage("RadarMap"); }
    @FXML private void handleNavForecast()  { setActiveNav(sidebarForecast, navForecast);   loadPage("Forecast"); }
    @FXML private void handleNavSettings()  { setActiveNav(sidebarSettings, navSettings);   loadPage("Settings"); }

    @FXML private void handleNotification() { System.out.println("Notifikasi diklik"); }
    @FXML private void handleSignOut()      { System.out.println("Sign Out diklik"); }

    // ── Global Search (navbar) ────────────────────────────────
    @FXML
    private void handleGlobalSearch() {
        if (searchField == null) return;
        String query = searchField.getText().trim();
        if (query.isEmpty()) return;

        Task<List<Location>> task = new Task<>() {
            @Override protected List<Location> call() throws Exception {
                return geocodingService.searchCity(query);
            }
        };

        task.setOnSucceeded(e -> {
            List<Location> results = task.getValue();
            if (results.isEmpty()) return;
            Location first = results.get(0);
            Platform.runLater(() -> {
                searchField.clear();
                // Navigasi ke Dashboard dan load kota yang ditemukan
                setActiveNav(sidebarDashboard, navDashboard);
                FXMLLoader loader = loadPageWithLoader("Dashboard");
                if (loader != null) {
                    DashboardController dc = loader.getController();
                    if (dc != null) dc.loadWeatherData(first.getLat(), first.getLon());
                }
            });
        });

        task.setOnFailed(e -> {
            System.err.println("\u274c Search error: "
                + (task.getException() != null ? task.getException().getMessage() : ""));
        });

        Thread t = new Thread(task); t.setDaemon(true); t.start();
    }

    // ── Load halaman ──────────────────────────────────────────
    private void loadPage(String pageName) {
        loadPageWithLoader(pageName);
    }

    private FXMLLoader loadPageWithLoader(String pageName) {
        try {
            FXMLLoader loader = new FXMLLoader(
                App.class.getResource("/fxml/" + pageName + ".fxml")
            );
            Node page = loader.load();
            activeController = loader.getController();

            if (page instanceof Region region) {
                region.setMaxWidth(Double.MAX_VALUE);
                region.setMaxHeight(Double.MAX_VALUE);
                StackPane.setAlignment(region, javafx.geometry.Pos.TOP_LEFT);
            }
            contentArea.getChildren().setAll(page);
            return loader;
        } catch (IOException e) {
            System.err.println("Gagal load halaman: " + pageName);
            e.printStackTrace();
            return null;
        }
    }

    // ── Set active nav ────────────────────────────────────────
    private void setActiveNav(Button activeSidebar, Button activeTopNav) {
        for (Button btn : sidebarButtons) {
            btn.getStyleClass().remove("nav-item-active");
            if (!btn.getStyleClass().contains("nav-item")) btn.getStyleClass().add("nav-item");
        }
        for (Button btn : topNavButtons) {
            btn.getStyleClass().remove("nav-top-active");
            if (!btn.getStyleClass().contains("nav-top")) btn.getStyleClass().add("nav-top");
        }
        activeSidebar.getStyleClass().remove("nav-item");
        activeSidebar.getStyleClass().add("nav-item-active");
        activeTopNav.getStyleClass().remove("nav-top");
        activeTopNav.getStyleClass().add("nav-top-active");
    }
}
