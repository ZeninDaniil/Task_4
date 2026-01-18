module com.cgvsu {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires org.junit.platform.launcher;
    requires org.junit.platform.engine;
    requires org.junit.jupiter.api;

    opens com.cgvsu to javafx.fxml;
    opens com.cgvsu.scene to javafx.fxml;
    opens com.cgvsu.util to javafx.fxml;
    exports com.cgvsu;
    exports com.cgvsu.scene;
    exports com.cgvsu.util;
    exports com.cgvsu.model;
    exports com.cgvsu.render_engine;
}