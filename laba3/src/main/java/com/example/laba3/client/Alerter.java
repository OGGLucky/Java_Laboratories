package com.example.laba3.client;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public class Alerter {
    public static void warning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Сообщение");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getButtonTypes().setAll(ButtonType.OK);
        alert.show();
    }

    public static void info(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Сообщение");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getButtonTypes().setAll(ButtonType.OK);
        alert.show();
    }
}
