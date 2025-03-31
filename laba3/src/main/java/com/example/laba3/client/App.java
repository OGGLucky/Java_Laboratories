package com.example.laba3.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
public class App extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("laba3View.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 853, 566);
        stage.setResizable(false);
        stage.setTitle("Laba3");

        AppController controller = fxmlLoader.getController();
        stage.setOnHidden(e -> controller.shutDown());

        stage.setScene(scene);
        stage.show();
    }

    public static void main() {
        launch();
    }
}
