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
        // Re-apply tema saat controller baru terdaftar (pindah halaman)
        applyChrome(currentTheme);
    }

    public static void apply(Theme theme) {
        currentTheme = theme;
        Platform.runLater(() -> {
            Theme resolved = resolveTheme(theme);
            applyStylesheet(resolved);
            applyChrome(resolved);
        });
    }

    /** Dipanggil dari loadPageWithLoader agar chrome selalu sinkron */
    public static void reapplyChrome() {
        Platform.runLater(() -> applyChrome(resolveTheme(currentTheme)));
    }

    private static Theme resolveTheme(Theme theme) {
        if (theme != Theme.SYSTEM) return theme;
        return isSystemDark() ? Theme.DARK : Theme.LIGHT;
    }

    private static void applyStylesheet(Theme resolved) {
        if (resolved == Theme.DARK) {
            Application.setUserAgentStylesheet(
                new PrimerDark().getUserAgentStylesheet());
        } else {
            Application.setUserAgentStylesheet(
                new PrimerLight().getUserAgentStylesheet());
        }
    }

    private static void applyChrome(Theme resolved) {
        if (mainController != null)
            mainController.applyThemeToChrome(resolved);
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
