package com.weatherwise.component;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

public class HumidityBarChart extends Canvas {

    private String[] days   = {"MON","TUE","WED","THU","FRI","SAT","SUN"};
    private double[] values = {45, 30, 80, 55, 40, 90, 50};

    private static final Color BAR_COLOR   = Color.web("#2b8cee");
    private static final Color BAR_BG      = Color.web("#2b8cee22");
    private static final Color LABEL_COLOR = Color.web("#94a3b8");
    private static final Color VALUE_COLOR = Color.web("#2b8cee");

    public HumidityBarChart(double width, double height) {
        super(width, height);
        draw();
    }

    /**
     * Isi chart dengan data nyata dari API.
     * @param dayLabels array nama hari, mis. ["Monday", "Tuesday", ...]
     * @param humidityValues array nilai humidity 0–100
     */
    public void setData(String[] dayLabels, double[] humidityValues) {
        if (dayLabels == null || humidityValues == null) return;
        int n = Math.min(dayLabels.length, humidityValues.length);
        this.days   = new String[n];
        this.values = new double[n];
        for (int i = 0; i < n; i++) {
            // Potong menjadi 3 huruf kapital, mis. "Monday" -> "MON"
            String label = dayLabels[i];
            this.days[i]   = (label != null && label.length() >= 3)
                             ? label.substring(0, 3).toUpperCase() : label;
            this.values[i] = humidityValues[i];
        }
        draw();
    }

    private void draw() {
        GraphicsContext gc = getGraphicsContext2D();
        double w = getWidth();
        double h = getHeight();

        gc.clearRect(0, 0, w, h);

        int    n      = values.length;
        if (n == 0) return;
        double labelH = 22;
        double valueH = 18;
        double chartH = h - labelH - valueH - 8;
        double gap    = 12;
        double barW   = (w - gap * (n + 1)) / n;

        gc.setTextAlign(TextAlignment.CENTER);

        for (int i = 0; i < n; i++) {
            double barH = (values[i] / 100.0) * chartH;
            double x    = gap + i * (barW + gap);
            double yBg  = valueH;
            double yBar = valueH + chartH - barH;

            gc.setFill(BAR_BG);
            gc.fillRoundRect(x, yBg, barW, chartH, 8, 8);

            gc.setFill(BAR_COLOR);
            gc.fillRoundRect(x, yBar, barW, barH, 8, 8);

            gc.setFill(VALUE_COLOR);
            gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
            gc.fillText((int) values[i] + "%", x + barW / 2, yBar - 4);

            gc.setFill(LABEL_COLOR);
            gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
            gc.fillText(days[i], x + barW / 2, h - 4);
        }
    }
}
