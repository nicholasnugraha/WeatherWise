module com.weatherwise {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;        // ← Pastikan ada
    requires javafx.graphics;
    requires atlantafx.base;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.materialdesign2;
    requires com.fasterxml.jackson.databind;

    opens com.weatherwise to javafx.graphics, javafx.fxml;
    opens com.weatherwise.controller to javafx.fxml;
    opens com.weatherwise.component to javafx.fxml;
    opens com.weatherwise.model to javafx.fxml;

    exports com.weatherwise;
}
