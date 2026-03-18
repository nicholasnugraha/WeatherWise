package com.weatherwise.component;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

public class WeatherCard extends VBox {

    public WeatherCard(String iconLiteral, String iconColor,
                       String title, String value, String subtitle) {
        // Styling kartu
        setAlignment(Pos.TOP_LEFT);
        setSpacing(10);
        setPadding(new Insets(20));
        setStyle("""
                -fx-background-color: white;
                -fx-background-radius: 14;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 10, 0, 0, 2);
                -fx-pref-width: 180;
                -fx-min-width: 160;
                """);

        // Icon dengan background bulat
        FontIcon icon = new FontIcon(iconLiteral);
        icon.setIconSize(24);
        icon.setIconColor(javafx.scene.paint.Color.web(iconColor));

        VBox iconBox = new VBox(icon);
        iconBox.setAlignment(Pos.CENTER);
        iconBox.setPadding(new Insets(8));
        iconBox.setStyle(String.format("""
                -fx-background-color: %s22;
                -fx-background-radius: 10;
                -fx-pref-width: 42;
                -fx-pref-height: 42;
                """, iconColor));

        // Label judul
        Label titleLabel = new Label(title);
        titleLabel.setStyle("""
                -fx-font-size: 12px;
                -fx-text-fill: #94a3b8;
                -fx-font-weight: normal;
                """);

        // Nilai utama
        Label valueLabel = new Label(value);
        valueLabel.setStyle("""
                -fx-font-size: 26px;
                -fx-font-weight: 900;
                -fx-text-fill: #0f172a;
                """);

        // Subjudul/keterangan
        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.setStyle("""
                -fx-font-size: 11px;
                -fx-text-fill: #94a3b8;
                """);
        subtitleLabel.setWrapText(true);

        getChildren().addAll(iconBox, titleLabel, valueLabel, subtitleLabel);

        // Hover effect
        setOnMouseEntered(e -> setStyle(getStyle() +
                "-fx-border-color: #2b8cee44; -fx-border-radius: 14; -fx-border-width: 1.5;"));
        setOnMouseExited(e -> setStyle("""
                -fx-background-color: white;
                -fx-background-radius: 14;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 10, 0, 0, 2);
                -fx-pref-width: 180;
                -fx-min-width: 160;
                """));
    }
}
