module com.weatherwise {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.graphics;
    requires atlantafx.base;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.materialdesign2;
    requires org.json;
    // com.fasterxml.jackson.databind dihapus — tidak dipakai di codebase

    opens com.weatherwise to javafx.graphics, javafx.fxml;
    opens com.weatherwise.controller to javafx.fxml;
    opens com.weatherwise.component to javafx.fxml;
    opens com.weatherwise.model to javafx.fxml;

    exports com.weatherwise;
}
