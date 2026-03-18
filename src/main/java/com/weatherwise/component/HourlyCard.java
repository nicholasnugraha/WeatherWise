package com.weatherwise.component;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

public class HourlyCard extends VBox {

    public HourlyCard(String time, String iconLiteral,
                      String iconColor, String temp, boolean isNow) {

        setAlignment(Pos.CENTER);
        setSpacing(8);
        setPadding(new Insets(16, 14, 16, 14));

        String bgColor = isNow ? "#2b8cee" : "white";
        String textColor = isNow ? "white" : "#0f172a";
        String subTextColor = isNow ? "#ffffffbb" : "#94a3b8";

        setStyle(String.format("""
                -fx-background-color: %s;
                -fx-background-radius: 14;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.07), 8, 0, 0, 2);
                -fx-min-width: 80;
                -fx-pref-width: 80;
                """, bgColor));

        // Label waktu (Now, 1PM, 2PM...)
        Label timeLabel = new Label(time);
        timeLabel.setStyle(String.format("""
                -fx-font-size: 11px;
                -fx-font-weight: bold;
                -fx-text-fill: %s;
                """, subTextColor));

        // Icon cuaca
        FontIcon icon = new FontIcon(iconLiteral);
        icon.setIconSize(28);
        icon.setIconColor(javafx.scene.paint.Color.web(isNow ? "#ffffff" : iconColor));

        // Suhu
        Label tempLabel = new Label(temp);
        tempLabel.setStyle(String.format("""
                -fx-font-size: 16px;
                -fx-font-weight: 900;
                -fx-text-fill: %s;
                """, textColor));

        getChildren().addAll(timeLabel, icon, tempLabel);
    }
}
