module com.cgvsu {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;

    opens com.cgvsu to javafx.fxml;
    opens com.cgvsu.scene to javafx.fxml;
    opens com.cgvsu.util to javafx.fxml;
    exports com.cgvsu;
    exports com.cgvsu.scene;
    exports com.cgvsu.util;
}