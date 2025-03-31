package com.example.laba3.client;

import javafx.scene.layout.HBox;

public class ArrowHelper {

    public static double arrowLength(HBox arrow) {
        return arrow.getBoundsInParent().getWidth();
    }

    public static double arrowEndX(HBox arrow) {
        return arrow.getLayoutX() + arrow.getBoundsInParent().getWidth();
    }

    public static double arrowEndY(HBox arrow) {
        return arrow.getLayoutY() + arrow.getBoundsInParent().getHeight() / 2;
    }

    public static void setArrowEndX(HBox arrow, double x) {
        arrow.setLayoutX(x - arrow.getBoundsInParent().getWidth());
    }

    public static void setArrowEndY(HBox arrow, double y) {
        arrow.setLayoutY(y - arrow.getBoundsInParent().getHeight() / 2);
    }
}
