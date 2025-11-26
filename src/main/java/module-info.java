module com.kiosk {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.swing;
    requires java.desktop;
    requires opencv;

    exports com.kiosk;
    exports com.kiosk.controllers;
    exports com.kiosk.models;
    exports com.kiosk.services;
    exports com.smartbin;
    exports com.smartbin.yolo;

    opens com.kiosk.controllers to javafx.fxml;
}