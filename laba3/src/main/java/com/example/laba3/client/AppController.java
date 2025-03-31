package com.example.laba3.client;

import com.example.laba3.data.*;
import com.example.laba3.server.DBRecord;
import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.controlsfx.control.Rating;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.example.laba3.client.ArrowHelper.setArrowEndX;
import static com.example.laba3.client.ArrowHelper.setArrowEndY;
import static com.example.laba3.client.PlayerPlaceHelper.placePlayerTriangle;
import static com.example.laba3.server.MainServer.DEFAULT_SERVER_PORT;

public class AppController {
    @FXML
    private TextField inputPlayerName;
    @FXML
    private Button btnConnect;
    @FXML
    private Button btnReady;
    @FXML
    private Button btnPause;
    @FXML
    private Button btnShoot;
    @FXML
    private Button btnRating;

    @FXML
    private Circle targetBigCircle;
    @FXML
    private Circle targetSmallCircle;
    @FXML
    private HBox arrow1;
    @FXML
    private HBox arrow2;
    @FXML
    private HBox arrow3;
    @FXML
    private HBox arrow4;
    @FXML
    private HBox playerTriangle1;
    @FXML
    private HBox playerTriangle2;
    @FXML
    private HBox playerTriangle3;
    @FXML
    private HBox playerTriangle4;

    private HBox[] playerTriangles;
    private HBox[] playerArrows;

    @FXML
    private Text labelGameInfo;
    @FXML
    private Text labelPause;

    public static final double GAME_AREA_WIDTH = 668;
    public static final double GAME_AREA_HEIGHT = 465;

    Gson gson = new Gson();
    Socket socketAtClient;
    InetAddress ip = null;

    DataInputStream dis;
    DataOutputStream dos;

    private Thread t;

    private boolean isThreadRunning = false;

    @FXML
    private void initialize() {
        playerTriangles = new HBox[]{playerTriangle1, playerTriangle2,
                playerTriangle3, playerTriangle4};
        playerArrows = new HBox[]{arrow1, arrow2, arrow3, arrow4};
    }

    private void updateGame(ServerMsg msg) {
        Model model = msg.model;
        RatingController.setRecords(model.records);

        if (model.winnerName != null) {
            Platform.runLater(() -> Alerter.info("Победитель: " + model.winnerName));
            return;
        }

        Platform.runLater(() -> {
            targetBigCircle.setLayoutY(model.bigTargetCenter.y);
            targetSmallCircle.setLayoutY(model.smallTargetCenter.y);

            int counter = 0;

            StringBuilder gameInfoText = new StringBuilder();

            for (PlayerData player : model.players) {
                if (player != null) {
                    playerArrows[counter].setVisible(player.isArrowFlying);
                    playerTriangles[counter].setVisible(true);
                    setArrowEndX(playerArrows[counter], player.arrowEndPos.x);
                    setArrowEndY(playerArrows[counter], player.arrowEndPos.y);
                    placePlayerTriangle(playerTriangles[counter], player.arrowEndPos.y);

                    int winnerScore = 0;
                    for (DBRecord record : model.records) {
                        if (record.playerName.equals(player.name)) {
                            winnerScore = record.score;
                            break;
                        }
                    }

                    gameInfoText.append(player.name).append(":\n")
                            .append("Очков: ").append(player.score).append("\n")
                            .append("Выстрелов: ").append(player.shots).append("\n")
                            .append("Побед: ").append(winnerScore).append("\n\n");

                } else {
                    playerArrows[counter].setVisible(false);
                    playerTriangles[counter].setVisible(false);
                }
                counter++;
            }

            labelGameInfo.setText(gameInfoText.toString());
            labelPause.setVisible(model.isPaused);
        });
    }

    @FXML
    protected void onConnectClick() {
        if (t != null) return;

        String playerName = inputPlayerName.getText().trim();
        if (playerName.isEmpty()) {
            Alerter.warning("Ник игрока не должен быть пустым.");
            return;
        }

        // Проверка доступности имени через HTTP-запрос
        try {
            HttpClient httpClient = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("http://localhost:8081/checkName?name=" + playerName))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if ("name_exists".equals(response.body())) {
                Alerter.warning("Это имя уже занято. Пожалуйста, выберите другое имя.");
                return;
            }
        } catch (Exception e) {
            Alerter.warning("Ошибка проверки имени. Пожалуйста, попробуйте снова.");
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
            return;
        }

        try {
            ip = InetAddress.getLocalHost();
            socketAtClient = new Socket(ip, DEFAULT_SERVER_PORT);
            OutputStream os = socketAtClient.getOutputStream();
            dos = new DataOutputStream(os);
            InputStream is = socketAtClient.getInputStream();
            dis = new DataInputStream(is);

            t = new Thread(() -> {
                // Отправка серверу приветствия (для сообщения имени)
                try {
                    ClientMsg hello = new ClientMsg(ClientAction.HELLO, playerName);
                    dos.writeUTF(gson.toJson(hello));
                } catch (IOException e) {
                    Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
                }

                inputPlayerName.setDisable(true);
                btnConnect.setDisable(true);
                btnReady.setDisable(false);
                btnPause.setDisable(false);
                btnShoot.setDisable(false);
                btnRating.setDisable(false);

                isThreadRunning = true;
                // Бесконечное получение сообщений
                while (isThreadRunning) {
                    try {
                        String s = dis.readUTF();
                        ServerMsg msg = gson.fromJson(s, ServerMsg.class);

                        // Обновление положений мишеней и стрел, а также
                        // информации об игре (счёта, числа выстрелов)
                        updateGame(msg);
                    } catch (IOException e) {
                        Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
                    }
                }
            });
            t.start();
        } catch (IOException e) {
            Alerter.warning("Не удалось подключиться.\nУбедитесь, что сервер запущен, и попробуйте снова.");
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
        }
    }

    @FXML
    protected void onReadyClick() {
        if (dos != null) {
            ClientMsg msg = new ClientMsg(ClientAction.READY);
            try {
                dos.writeUTF(gson.toJson(msg, ClientMsg.class));
            } catch (IOException e) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
            }
        }
    }

    @FXML
    protected void onPauseClick() {
        if (dos != null) {
            ClientMsg msg = new ClientMsg(ClientAction.PAUSE);
            try {
                dos.writeUTF(gson.toJson(msg, ClientMsg.class));
            } catch (IOException e) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
            }
        }
    }

    @FXML
    protected void onShootClick() {
        if (dos != null) {
            ClientMsg msg = new ClientMsg(ClientAction.SHOOT);
            try {
                dos.writeUTF(gson.toJson(msg, ClientMsg.class));
            } catch (IOException e) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
            }
        }
    }

    @FXML
    protected void onRatingClick() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("RatingView.fxml"));
            Parent parent = fxmlLoader.load();
            Stage stage = new Stage();
            stage.setResizable(false);
            stage.setTitle("Рейтинг");

            stage.setScene(new Scene(parent));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
        }
    }

    public void shutDown() {
        if (isThreadRunning) {
            isThreadRunning = false;
            try {
                if (dis != null) dis.close();
                if (dos != null) dos.close();
                if (socketAtClient != null) socketAtClient.close();
            } catch (IOException e) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
            }
        }
    }
}
