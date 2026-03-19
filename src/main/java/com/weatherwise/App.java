package com.weatherwise;

import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Aktifkan tema AtlantaFX (bisa diganti PrimerDark untuk dark mode)
        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainWindow.fxml"));
        Scene scene = new Scene(loader.load(), 1280, 800);

        stage.setTitle("WeatherWise");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        // Di App.java main()
        // Di App.java main()
        System.setProperty("prism.order", "d3d,sw");   // coba D3D, fallback sw
        System.setProperty("prism.maxvram", "2G");    // batasi VRAM agar tidak habis
        System.setProperty("prism.targetvram", "1G");
        System.setProperty("prism.dirtyopts", "false"); // matikan dirty region opt yg sering NPE

        launch(args);
    }
}
