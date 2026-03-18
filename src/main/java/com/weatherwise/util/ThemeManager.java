package com.weatherwise.util;

import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;
import com.weatherwise.controller.MainWindowController;
import javafx.application.Application;
import javafx.application.Platform;

public class ThemeManager {

    public enum Theme { LIGHT, DARK, SYSTEM }

    private static Theme currentTheme = Theme.LIGHT;
    private static MainWindowController mainController = null;

    public static void setMainWindowController(MainWindowController c) {
        mainController = c;
    }

    public static void apply(Theme theme) {
        currentTheme = theme;
        Platform.runLater(() -> {
            switch (theme) {
                case LIGHT -> {
                    Application.setUserAgentStylesheet(
                        new PrimerLight().getUserAgentStylesheet());
                    if (mainController != null)
                        mainController.applyThemeToChrome(Theme.LIGHT);
                }
                case DARK -> {
                    Application.setUserAgentStylesheet(
                        new PrimerDark().getUserAgentStylesheet());
                    if (mainController != null)
                        mainController.applyThemeToChrome(Theme.DARK);
                }
                case SYSTEM -> {
                    boolean isDark = isSystemDark();
                    Application.setUserAgentStylesheet(isDark
                        ? new PrimerDark().getUserAgentStylesheet()
                        : new PrimerLight().getUserAgentStylesheet());
                    Theme resolved = isDark ? Theme.DARK : Theme.LIGHT;
                    if (mainController != null)
                        mainController.applyThemeToChrome(resolved);
                }
            }
        });
    }

    private static boolean isSystemDark() {
        try {
            String os = System.getProperty("os.name", "").toLowerCase();
            if (os.contains("win")) {
                Process p = Runtime.getRuntime().exec(
                    "reg query HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize /v AppsUseLightTheme");
                String out = new String(p.getInputStream().readAllBytes());
                return out.contains("0x0");
            }
        } catch (Exception ignored) {}
        return false;
    }

    public static Theme getCurrentTheme() { return currentTheme; }
}
