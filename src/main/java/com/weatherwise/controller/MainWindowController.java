package com.weatherwise.controller;

import com.weatherwise.App;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import java.io.IOException;
import java.util.List;
import javafx.scene.layout.Region;

public class MainWindowController {

    @FXML private StackPane contentArea;

    // Sidebar buttons
    @FXML private Button sidebarDashboard;
    @FXML private Button sidebarMaps;
    @FXML private Button sidebarForecast;
    @FXML private Button sidebarSettings;

    // Top nav buttons
    @FXML private Button navDashboard;
    @FXML private Button navMaps;
    @FXML private Button navForecast;
    @FXML private Button navSettings;

    private List<Button> sidebarButtons;
    private List<Button> topNavButtons;

    @FXML
    public void initialize() {
        sidebarButtons = List.of(sidebarDashboard, sidebarMaps,
                                  sidebarForecast, sidebarSettings);
        topNavButtons  = List.of(navDashboard, navMaps,
                                  navForecast, navSettings);

        // Load halaman Dashboard saat pertama buka
        loadPage("Dashboard");
    }

    // ==================== Handler Navigasi ====================

    @FXML
    private void handleNavDashboard() {
        setActiveNav(sidebarDashboard, navDashboard);
        loadPage("Dashboard");
    }

    @FXML
    private void handleNavMaps() {
        setActiveNav(sidebarMaps, navMaps);
        loadPage("RadarMap");
    }

    @FXML
    private void handleNavForecast() {
        setActiveNav(sidebarForecast, navForecast);
        loadPage("Forecast");
    }

    @FXML
    private void handleNavSettings() {
        setActiveNav(sidebarSettings, navSettings);
        loadPage("Settings");
    }

    @FXML
    private void handleNotification() {
        System.out.println("Notifikasi diklik");
    }

    @FXML
    private void handleSignOut() {
        System.out.println("Sign Out diklik");
        // Nanti bisa tambahkan dialog konfirmasi
    }

    // ==================== Helper ====================

    /**
     * Load FXML halaman ke dalam contentArea
     */
    private void loadPage(String pageName) {
    try {
        FXMLLoader loader = new FXMLLoader(
            App.class.getResource("/fxml/" + pageName + ".fxml")
        );
        Node page = loader.load();

        // FIX: Paksa node mengisi seluruh contentArea
        if (page instanceof Region region) {
            region.setMaxWidth(Double.MAX_VALUE);
            region.setMaxHeight(Double.MAX_VALUE);
            StackPane.setAlignment(region, javafx.geometry.Pos.TOP_LEFT);
        }

        contentArea.getChildren().setAll(page);

    } catch (IOException e) {
        System.err.println("Gagal load halaman: " + pageName);
        e.printStackTrace();
    }
}

    /**
     * Set visual active state pada sidebar & top nav
     */
    private void setActiveNav(Button activeSidebar, Button activeTopNav) {
        // Reset semua sidebar ke style normal
        for (Button btn : sidebarButtons) {
            btn.getStyleClass().remove("nav-item-active");
            if (!btn.getStyleClass().contains("nav-item")) {
                btn.getStyleClass().add("nav-item");
            }
        }

        // Reset semua top nav ke style normal
        for (Button btn : topNavButtons) {
            btn.getStyleClass().remove("nav-top-active");
            if (!btn.getStyleClass().contains("nav-top")) {
                btn.getStyleClass().add("nav-top");
            }
        }

        // Set yang aktif
        activeSidebar.getStyleClass().remove("nav-item");
        activeSidebar.getStyleClass().add("nav-item-active");

        activeTopNav.getStyleClass().remove("nav-top");
        activeTopNav.getStyleClass().add("nav-top-active");
    }
}