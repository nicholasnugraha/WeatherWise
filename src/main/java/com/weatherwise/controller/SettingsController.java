package com.weatherwise.controller;

import com.weatherwise.model.Location;
import com.weatherwise.service.GeocodingService;
import com.weatherwise.service.WeatherService;
import com.weatherwise.util.AppState;
import com.weatherwise.util.ThemeManager;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.List;

public class SettingsController {

    // ── Location ──────────────────────────────────────────────
    @FXML
    private TextField searchField;
    @FXML
    private VBox locationListContainer;

    // ── Units ─────────────────────────────────────────────────
    @FXML
    private Button btnCelsius;
    @FXML
    private Button btnFahrenheit;
    @FXML
    private Button btnKmh;
    @FXML
    private Button btnMph;
    @FXML
    private Button btnHpa;
    @FXML
    private Button btnInhg;

    // ── Notifications ─────────────────────────────────────────
    @FXML
    private ToggleButton toggleSevere;
    @FXML
    private ToggleButton toggleDailyBriefing;

    // ── Appearance ────────────────────────────────────────────
    @FXML
    private Button btnThemeLight;
    @FXML
    private Button btnThemeDark;
    @FXML
    private Button btnThemeSystem;

    // ── Status Search ─────────────────────────────────────────
    @FXML
    private Label labelSearchStatus;

    // ── State ─────────────────────────────────────────────────
    private String selectedTempUnit = "celsius";
    private String selectedWindUnit = "mph";
    private String selectedPressUnit = "hpa";
    private String selectedTheme = "light";

    private final GeocodingService geocodingService = new GeocodingService();

    @FXML
    public void initialize() {
        refreshUnitStyles();
        refreshThemeStyles();
        refreshToggleStyles();

        // Enter key di search field
        if (searchField != null) {
            searchField.setOnAction(e -> handleSearch());
        }
    }

    // ── Search Lokasi (Geocoding API) ──────────────────────────
    @FXML
    private void handleSearch() {
        if (searchField == null) {
            return;
        }
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            return;
        }

        if (labelSearchStatus != null) {
            labelSearchStatus.setText("⏳ Mencari \"" + query + "\"...");
            labelSearchStatus.setVisible(true);
        }
        if (locationListContainer != null) {
            locationListContainer.getChildren().clear();
        }

        Task<List<Location>> task = new Task<>() {
            @Override
            protected List<Location> call() throws Exception {
                return geocodingService.searchCity(query);
            }
        };

        task.setOnSucceeded(e -> {
            List<Location> locations = task.getValue();

            if (labelSearchStatus != null) {
                labelSearchStatus.setVisible(false);
            }

            if (locations.isEmpty()) {
                if (labelSearchStatus != null) {
                    labelSearchStatus.setText("Kota \"" + query + "\" tidak ditemukan.");
                    labelSearchStatus.setVisible(true);
                }
                return;
            }

            if (locationListContainer != null) {
                locationListContainer.getChildren().clear();
                for (Location loc : locations) {
                    HBox resultCard = buildSearchResultCard(loc);
                    locationListContainer.getChildren().add(resultCard);
                }
            }
        });

        task.setOnFailed(e -> {
            String msg = task.getException().getMessage();
            System.err.println("❌ Geocoding error: " + msg);
            if (labelSearchStatus != null) {
                labelSearchStatus.setText("❌ " + msg);
                labelSearchStatus.setVisible(true);
            }
        });

        new Thread(task).start();
    }

    // ── Build Search Result Card ───────────────────────────────
    private HBox buildSearchResultCard(Location loc) {
        HBox card = new HBox(12);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("""
            -fx-background-color: #eff6ff;
            -fx-background-radius: 10;
            -fx-padding: 10 14 10 14;
            -fx-cursor: hand;
            """);

        FontIcon icon = new FontIcon("mdi2m-map-marker-outline");
        icon.setIconSize(18);
        icon.setIconColor(javafx.scene.paint.Color.web("#2b8cee"));

        VBox textBox = new VBox(2);
        HBox.setHgrow(textBox, Priority.ALWAYS);

        Label nameLabel = new Label(loc.getName()
                + (loc.getState() != null && !loc.getState().isEmpty()
                ? ", " + loc.getState() : ""));
        nameLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;"
                + "-fx-text-fill: #0f172a;");

        Label countryLabel = new Label(loc.getCountry()
                + "  ·  " + String.format("%.2f", loc.getLat())
                + ", " + String.format("%.2f", loc.getLon()));
        countryLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");

        textBox.getChildren().addAll(nameLabel, countryLabel);

        Button addBtn = new Button("Tambah");
        addBtn.setStyle("""
            -fx-background-color: #2b8cee;
            -fx-text-fill: white;
            -fx-font-weight: bold;
            -fx-font-size: 11px;
            -fx-background-radius: 8;
            -fx-cursor: hand;
            -fx-padding: 6 12 6 12;
            """);
        addBtn.setOnAction(e -> selectLocation(loc));

        card.getChildren().addAll(icon, textBox, addBtn);

        // Klik kartu juga pilih lokasi
        card.setOnMouseClicked(e -> selectLocation(loc));

        return card;
    }

    private void selectLocation(Location loc) {
        // Simpan ke AppState agar Dashboard bisa reload
        AppState.getInstance().setLocation(loc.getLat(), loc.getLon(), loc.getDisplayName());

        System.out.println("📍 Lokasi dipilih: " + loc.getDisplayName()
                + " (" + loc.getLat() + ", " + loc.getLon() + ")");

        if (labelSearchStatus != null) {
            labelSearchStatus.setText("✅ " + loc.getDisplayName() + " — Dashboard akan diperbarui.");
            labelSearchStatus.setVisible(true);
        }
        if (searchField != null) {
            searchField.clear();
        }
        if (locationListContainer != null) {
            locationListContainer.getChildren().clear();
        }
    }

    // ── Unit Handlers ──────────────────────────────────────────
    @FXML
    private void handleCelsius() {
        selectedTempUnit = "celsius";
        refreshUnitStyles();
    }

    @FXML
    private void handleFahrenheit() {
        selectedTempUnit = "fahrenheit";
        refreshUnitStyles();
    }

    @FXML
    private void handleKmh() {
        selectedWindUnit = "kmh";
        refreshUnitStyles();
    }

    @FXML
    private void handleMph() {
        selectedWindUnit = "mph";
        refreshUnitStyles();
    }

    @FXML
    private void handleHpa() {
        selectedPressUnit = "hpa";
        refreshUnitStyles();
    }

    @FXML
    private void handleInhg() {
        selectedPressUnit = "inhg";
        refreshUnitStyles();
    }

    private void refreshUnitStyles() {
        String active = "-fx-background-color: #2b8cee; -fx-text-fill: white;"
                + "-fx-font-weight: bold; -fx-font-size: 12px;"
                + "-fx-background-radius: 8; -fx-cursor: hand;";
        String normal = "-fx-background-color: transparent; -fx-text-fill: #64748b;"
                + "-fx-font-size: 12px;"
                + "-fx-background-radius: 8; -fx-cursor: hand;";

        if (btnCelsius != null) {
            btnCelsius.setStyle(selectedTempUnit.equals("celsius") ? active : normal);
        }
        if (btnFahrenheit != null) {
            btnFahrenheit.setStyle(selectedTempUnit.equals("fahrenheit") ? active : normal);
        }
        if (btnKmh != null) {
            btnKmh.setStyle(selectedWindUnit.equals("kmh") ? active : normal);
        }
        if (btnMph != null) {
            btnMph.setStyle(selectedWindUnit.equals("mph") ? active : normal);
        }
        if (btnHpa != null) {
            btnHpa.setStyle(selectedPressUnit.equals("hpa") ? active : normal);
        }
        if (btnInhg != null) {
            btnInhg.setStyle(selectedPressUnit.equals("inhg") ? active : normal);
        }
    }

    // ── Notification Handlers ──────────────────────────────────
    @FXML
    private void handleToggleSevere() {
        refreshToggleStyles();
    }

    @FXML
    private void handleToggleDailyBriefing() {
        refreshToggleStyles();
    }

    private void refreshToggleStyles() {
        updateToggleStyle(toggleSevere);
        updateToggleStyle(toggleDailyBriefing);
    }

    private void updateToggleStyle(ToggleButton btn) {
        if (btn == null) {
            return;
        }
        if (btn.isSelected()) {
            btn.setText("ON");
            btn.setStyle("""
                -fx-background-color: #2b8cee;
                -fx-text-fill: white;
                -fx-font-weight: bold;
                -fx-font-size: 10px;
                -fx-background-radius: 13;
                -fx-cursor: hand;
                """);
        } else {
            btn.setText("OFF");
            btn.setStyle("""
                -fx-background-color: #e2e8f0;
                -fx-text-fill: #94a3b8;
                -fx-font-size: 10px;
                -fx-background-radius: 13;
                -fx-cursor: hand;
                """);
        }
    }

    // ── Theme Handlers ─────────────────────────────────────────
    @FXML
    private void handleThemeLight() {
        selectedTheme = "light";
        ThemeManager.apply(ThemeManager.Theme.LIGHT);
        refreshThemeStyles();
    }

    @FXML
    private void handleThemeDark() {
        selectedTheme = "dark";
        ThemeManager.apply(ThemeManager.Theme.DARK);
        refreshThemeStyles();
    }

    @FXML
    private void handleThemeSystem() {
        selectedTheme = "system";
        ThemeManager.apply(ThemeManager.Theme.SYSTEM);
        refreshThemeStyles();
    }

    private void refreshThemeStyles() {
        String active = "-fx-background-color: #eff6ff;"
                + "-fx-border-color: #2b8cee; -fx-border-width: 2;"
                + "-fx-border-radius: 12; -fx-background-radius: 12;"
                + "-fx-cursor: hand;";
        String normal = "-fx-background-color: #f1f5f9;"
                + "-fx-border-color: transparent; -fx-border-width: 2;"
                + "-fx-border-radius: 12; -fx-background-radius: 12;"
                + "-fx-cursor: hand;";

        if (btnThemeLight != null) {
            btnThemeLight.setStyle(selectedTheme.equals("light") ? active : normal);
        }
        if (btnThemeDark != null) {
            btnThemeDark.setStyle(selectedTheme.equals("dark") ? active : normal);
        }
        if (btnThemeSystem != null) {
            btnThemeSystem.setStyle(selectedTheme.equals("system") ? active : normal);
        }
    }

    // ── Save ───────────────────────────────────────────────────
    @FXML
    private void handleSaveSettings() {
        System.out.println("💾 Pengaturan disimpan:");
        System.out.println("   Suhu    : " + selectedTempUnit);
        System.out.println("   Angin   : " + selectedWindUnit);
        System.out.println("   Tekanan : " + selectedPressUnit);
        System.out.println("   Tema    : " + selectedTheme);
        System.out.println("   Notif Ekstrem  : " + (toggleSevere != null && toggleSevere.isSelected()));
        System.out.println("   Notif Harian   : " + (toggleDailyBriefing != null && toggleDailyBriefing.isSelected()));

        if (labelSearchStatus != null) {
            labelSearchStatus.setText("✅ Pengaturan berhasil disimpan!");
            labelSearchStatus.setVisible(true);
        }
    }
}
