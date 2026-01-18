module com.cgvsu {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires org.junit.platform.engine;
    requires org.junit.platform.launcher;


    opens com.cgvsu to javafx.fxml;
    exports com.cgvsu;
}