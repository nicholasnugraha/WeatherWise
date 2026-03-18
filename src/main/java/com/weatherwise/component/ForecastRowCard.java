package com.weatherwise.component;

import com.weatherwise.model.ForecastDay;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.javafx.FontIcon;

public class ForecastRowCard extends HBox {

    public ForecastRowCard(ForecastDay day) {
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(0);
        setPadding(new Insets(18, 24, 18, 24));
        setStyle("""
                -fx-background-color: white;
                -fx-background-radius: 14;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0, 0, 2);
                -fx-cursor: hand;
                """);

        // ── Kolom 1: Nama Hari & Tanggal (lebar tetap) ──────────
        VBox dayBox = new VBox(4);
        dayBox.setPrefWidth(130);
        dayBox.setMinWidth(130);
        dayBox.setAlignment(Pos.CENTER_LEFT);

        Label dayLabel = new Label(day.getDayName());
        dayLabel.setStyle("""
                -fx-font-size: 15px;
                -fx-font-weight: bold;
                -fx-text-fill: #0f172a;
                """);

        Label dateLabel = new Label(day.getDate());
        dateLabel.setStyle("""
                -fx-font-size: 11px;
                -fx-text-fill: #94a3b8;
                -fx-font-weight: bold;
                """);

        dayBox.getChildren().addAll(dayLabel, dateLabel);

        // ── Kolom 2: Icon + Kondisi ──────────────────────────────
        HBox conditionBox = new HBox(16);
        conditionBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(conditionBox, Priority.ALWAYS);

        // Icon cuaca dengan background
        VBox iconBox = new VBox();
        iconBox.setAlignment(Pos.CENTER);
        iconBox.setPadding(new Insets(10));
        iconBox.setStyle(String.format("""
                -fx-background-color: %s22;
                -fx-background-radius: 12;
                -fx-pref-width: 52;
                -fx-pref-height: 52;
                """, day.getIconColor()));

        FontIcon icon = new FontIcon(day.getIconLiteral());
        icon.setIconSize(28);
        icon.setIconColor(Color.web(day.getIconColor()));
        iconBox.getChildren().add(icon);

        // Teks kondisi & deskripsi
        VBox textBox = new VBox(4);
        textBox.setAlignment(Pos.CENTER_LEFT);

        Label condLabel = new Label(day.getCondition());
        condLabel.setStyle("""
                -fx-font-size: 15px;
                -fx-font-weight: 600;
                -fx-text-fill: #0f172a;
                """);

        Label descLabel = new Label(day.getDescription());
        descLabel.setStyle("""
                -fx-font-size: 12px;
                -fx-text-fill: #94a3b8;
                """);

        textBox.getChildren().addAll(condLabel, descLabel);
        conditionBox.getChildren().addAll(iconBox, textBox);

        // ── Kolom 3: Suhu High & Low ─────────────────────────────
        HBox tempBox = new HBox(20);
        tempBox.setAlignment(Pos.CENTER_RIGHT);
        tempBox.setPrefWidth(130);
        tempBox.setMinWidth(130);

        VBox highBox = new VBox(2);
        highBox.setAlignment(Pos.CENTER_RIGHT);
        Label highTemp = new Label(day.getHighTemp() + "°");
        highTemp.setStyle("""
                -fx-font-size: 22px;
                -fx-font-weight: 900;
                -fx-text-fill: #0f172a;
                """);
        Label highLbl = new Label("HIGH");
        highLbl.setStyle("""
                -fx-font-size: 9px;
                -fx-font-weight: bold;
                -fx-text-fill: #94a3b8;
                """);
        highBox.getChildren().addAll(highTemp, highLbl);

        VBox lowBox = new VBox(2);
        lowBox.setAlignment(Pos.CENTER_RIGHT);
        Label lowTemp = new Label(day.getLowTemp() + "°");
        lowTemp.setStyle("""
                -fx-font-size: 22px;
                -fx-font-weight: bold;
                -fx-text-fill: #94a3b8;
                """);
        Label lowLbl = new Label("LOW");
        lowLbl.setStyle("""
                -fx-font-size: 9px;
                -fx-font-weight: bold;
                -fx-text-fill: #94a3b8;
                """);
        lowBox.getChildren().addAll(lowTemp, lowLbl);
        tempBox.getChildren().addAll(highBox, lowBox);

        // ── Hover Effect ─────────────────────────────────────────
        setOnMouseEntered(e -> setStyle("""
                -fx-background-color: white;
                -fx-background-radius: 14;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0, 0, 2);
                -fx-cursor: hand;
                -fx-border-color: #2b8cee44;
                -fx-border-radius: 14;
                -fx-border-width: 1.5;
                """));
        setOnMouseExited(e -> setStyle("""
                -fx-background-color: white;
                -fx-background-radius: 14;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0, 0, 2);
                -fx-cursor: hand;
                """));

        getChildren().addAll(dayBox, conditionBox, tempBox);
    }
}