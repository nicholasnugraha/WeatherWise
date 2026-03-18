package com.weatherwise.component;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

public class HumidityBarChart extends Canvas {

    private final String[] days   = {"MON","TUE","WED","THU","FRI","SAT","SUN"};
    private final double[] values = {45, 30, 80, 55, 40, 90, 50};

    private static final Color BAR_COLOR  = Color.web("#2b8cee");
    private static final Color BAR_BG     = Color.web("#2b8cee22");
    private static final Color LABEL_COLOR = Color.web("#94a3b8");
    private static final Color VALUE_COLOR = Color.web("#2b8cee");

    public HumidityBarChart(double width, double height) {
        super(width, height);
        draw();
    }

    private void draw() {
        GraphicsContext gc = getGraphicsContext2D();
        double w = getWidth();
        double h = getHeight();

        gc.clearRect(0, 0, w, h);

        int    n       = values.length;
        double labelH  = 22;
        double valueH  = 18;
        double chartH  = h - labelH - valueH - 8;
        double gap     = 12;
        double barW    = (w - gap * (n + 1)) / n;

        gc.setTextAlign(TextAlignment.CENTER);

        for (int i = 0; i < n; i++) {
            double barH = (values[i] / 100.0) * chartH;
            double x    = gap + i * (barW + gap);
            double yBg  = valueH;
            double yBar = valueH + chartH - barH;

            // Background bar tipis
            gc.setFill(BAR_BG);
            gc.fillRoundRect(x, yBg, barW, chartH, 8, 8);

            // Bar utama
            gc.setFill(BAR_COLOR);
            gc.fillRoundRect(x, yBar, barW, barH, 8, 8);

            // Nilai di atas bar
            gc.setFill(VALUE_COLOR);
            gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
            gc.fillText((int) values[i] + "%", x + barW / 2, yBar - 4);

            // Label hari di bawah
            gc.setFill(LABEL_COLOR);
            gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
            gc.fillText(days[i], x + barW / 2, h - 4);
        }
    }
}
