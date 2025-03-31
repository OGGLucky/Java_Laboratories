package com.example.laba3.client;

import com.example.laba3.server.DBRecord;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.ArrayList;

public class RatingController {

    private static ArrayList<DBRecord> records;

    public static void setRecords(ArrayList<DBRecord> rds) {
        records = rds;
    }

    @FXML
    private TableView<DBRecord> tableView;

    @FXML
    private void initialize() {
        tableView.setPlaceholder(new Label("Данных об игроках нет"));

        TableColumn<DBRecord, String> column1 =
                new TableColumn<>("Игрок");
        column1.setCellValueFactory(
                new PropertyValueFactory<>("playerName"));

        TableColumn<DBRecord, String> column2 =
                new TableColumn<>("Побед");
        column2.setCellValueFactory(
                new PropertyValueFactory<>("score"));

        tableView.getColumns().add(column1);
        tableView.getColumns().add(column2);
        tableView.getItems().addAll(records);
    }
}
