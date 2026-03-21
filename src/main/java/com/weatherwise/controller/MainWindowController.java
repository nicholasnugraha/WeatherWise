package com.weatherwise.controller;

import com.weatherwise.App;
import com.weatherwise.model.Location;
import com.weatherwise.service.GeocodingService;
import com.weatherwise.util.AppState;
import com.weatherwise.util.ThemeManager;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.List;

public class MainWindowController {

    @FXML
    private StackPane contentArea;
    @FXML
    private TextField searchField;

    @FXML
    private Button sidebarDashboard;
    @FXML
    private Button sidebarMaps;
    @FXML
    private Button sidebarForecast;
    @FXML
    private Button sidebarSettings;

//    @FXML
//    private Button navDashboard;
//    @FXML
//    private Button navMaps;
//    @FXML
//    private Button navForecast;
//    @FXML
//    private Button navSettings;
    // fx:id yang ditambahkan di MainWindow.fxml
    @FXML
    private HBox topNavBar;
    @FXML
    private VBox sidebarPanel;

    private List<Button> sidebarButtons;
//    private List<Button> topNavButtons;

    private final GeocodingService geocodingService = new GeocodingService();

    @FXML
    public void initialize() {
        sidebarButtons = List.of(sidebarDashboard, sidebarMaps,
                sidebarForecast, sidebarSettings);
//        topNavButtons = List.of(navDashboard, navMaps,
//                navForecast, navSettings);

        if (searchField != null) {
            searchField.setOnAction(e -> handleGlobalSearch());
        }

        // Daftarkan ke ThemeManager agar dark mode bisa ubah navbar & sidebar
        ThemeManager.setMainWindowController(this);

        loadPage("Dashboard");
    }

    // ── Navigasi ──────────────────────────────────────────────
    @FXML
    private void handleNavDashboard() {
        setActiveNav(sidebarDashboard);
        AppState state = AppState.getInstance();
        FXMLLoader loader = loadPageWithLoader("Dashboard");
        if (loader != null) {
            DashboardController dc = loader.getController();
            if (dc != null) {
                dc.loadWeatherData(state.getLat(), state.getLon());
            }
        }
    }

    @FXML
    private void handleNavMaps() {
        setActiveNav(sidebarMaps);
        // AppState.isChanged() & clearChanged() sudah dihandle di RadarMapController.initialize()
        loadPage("RadarMap");
    }

    @FXML
    private void handleNavForecast() {
        setActiveNav(sidebarForecast);
        AppState state = AppState.getInstance();
        FXMLLoader loader = loadPageWithLoader("Forecast");
        if (loader != null) {
            ForecastController fc = loader.getController();
            if (fc != null) {
                fc.setLocation(state.getLat(), state.getLon(), state.getCityName());
            }
        }
    }

    @FXML
    private void handleNavSettings() {
        setActiveNav(sidebarSettings);
        loadPage("Settings");
    }

    @FXML
    private void handleNotification() {
    }

    @FXML
    private void handleSignOut() {
    }

    // ── Global Search ─────────────────────────────────────────
    @FXML
    private void handleGlobalSearch() {
        if (searchField == null) {
            return;
        }
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            return;
        }

        Task<List<Location>> task = new Task<>() {
            @Override
            protected List<Location> call() throws Exception {
                return geocodingService.searchCity(query);
            }
        };
        task.setOnSucceeded(e -> {
            List<Location> results = task.getValue();
            if (results.isEmpty()) {
                return;
            }
            Location first = results.get(0);
            Platform.runLater(() -> {
                searchField.clear();
                // Simpan ke AppState
                AppState.getInstance().setLocation(
                        first.getLat(), first.getLon(), first.getDisplayName());
                setActiveNav(sidebarDashboard);
                FXMLLoader loader = loadPageWithLoader("Dashboard");
                if (loader != null) {
                    DashboardController dc = loader.getController();
                    if (dc != null) {
                        dc.loadWeatherData(first.getLat(), first.getLon());
                    }
                }
            });
        });
        task.setOnFailed(e -> System.err.println("Search error: "
                + (task.getException() != null ? task.getException().getMessage() : "")));

        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }

    // ── Load Halaman ──────────────────────────────────────────
    private void loadPage(String pageName) {
        loadPageWithLoader(pageName);
    }

    private FXMLLoader loadPageWithLoader(String pageName) {
        try {
            // Dispose controller sebelumnya — cegah scheduler zombie
            if (!contentArea.getChildren().isEmpty()) {
                Node old = contentArea.getChildren().get(0);
                Object userData = old.getUserData();
                if (userData instanceof DashboardController dc) {
                    dc.dispose();
                } else if (userData instanceof ForecastController fc) {
                    fc.dispose();
                }
            }

            FXMLLoader loader = new FXMLLoader(
                    App.class.getResource("/fxml/" + pageName + ".fxml"));
            Node page = loader.load();

            // Simpan controller agar bisa dispose nanti
            Object ctrl = loader.getController();
            if (ctrl instanceof DashboardController || ctrl instanceof ForecastController) {
                page.setUserData(ctrl);
            }

            if (page instanceof Region region) {
                region.setMaxWidth(Double.MAX_VALUE);
                region.setMaxHeight(Double.MAX_VALUE);
                StackPane.setAlignment(region, javafx.geometry.Pos.TOP_LEFT);
            }
            contentArea.getChildren().setAll(page);
            ThemeManager.reapplyChrome();
            return loader;
        } catch (IOException e) {
            System.err.println("Gagal load halaman: " + pageName);
            e.printStackTrace();
            return null;
        }
    }


    // ── Apply Dark/Light ke Chrome (navbar & sidebar) ─────────
    public void applyThemeToChrome(ThemeManager.Theme theme) {
        boolean dark = (theme == ThemeManager.Theme.DARK);

        String navBg = dark ? "#1e293b" : "white";
        String navBorder = dark ? "#334155" : "#e2e8f0";
        String sideBg = dark ? "#1e293b" : "white";
        String sideBdr = dark ? "#334155" : "#e2e8f0";
        String contBg = dark ? "#0f172a" : "#f6f7f8";

        if (topNavBar != null) {
            topNavBar.setStyle(
                    "-fx-background-color: " + navBg + ";"
                    + "-fx-border-color: " + navBorder + ";"
                    + "-fx-border-width: 0 0 1 0;"
                    + "-fx-padding: 12 24 12 24;");
        }

        if (sidebarPanel != null) {
            sidebarPanel.setStyle(
                    "-fx-background-color: " + sideBg + ";"
                    + "-fx-border-color: " + sideBdr + ";"
                    + "-fx-border-width: 0 1 0 0;"
                    + "-fx-pref-width: 220;"
                    + "-fx-min-width: 220;"
                    + "-fx-padding: 24 12 24 12;");
        }

        if (contentArea != null) {
            contentArea.setStyle("-fx-background-color: " + contBg + ";");
        }
    }

    // ── Set Active Nav ────────────────────────────────────────
    private void setActiveNav(Button activeSidebar) {
        for (Button btn : sidebarButtons) {
            btn.getStyleClass().remove("nav-item-active");
            if (!btn.getStyleClass().contains("nav-item")) {
                btn.getStyleClass().add("nav-item");
            }
        }
        activeSidebar.getStyleClass().remove("nav-item");
        activeSidebar.getStyleClass().add("nav-item-active");
    }

}
