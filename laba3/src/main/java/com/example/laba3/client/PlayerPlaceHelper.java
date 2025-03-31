package com.example.laba3.client;

import javafx.scene.layout.HBox;

public class PlayerPlaceHelper {
    public static void placePlayerTriangle(HBox playerTriangle, double y) {
        playerTriangle.setLayoutY(y - playerTriangle.getLayoutBounds().getHeight() / 2);
    }
}
