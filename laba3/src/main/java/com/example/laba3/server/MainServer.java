package com.example.laba3.server;

import com.example.laba3.data.Model;
import com.example.laba3.data.PlayerData;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

public class MainServer {
    public static final int DEFAULT_SERVER_PORT = 8080;
    public static final int HTTP_PORT = 8081; // Новый порт для HTTP сервера
    public final Model model = Model.Builder.build();
    InetAddress ip = null;
    ExecutorService service = Executors.newCachedThreadPool();
    ArrayList<ClientAtServer> clients = new ArrayList<>();
    Gson gson = new Gson();

    boolean isUpdaterRunning = false;

    Runnable updaterRunnable = () -> {
        isUpdaterRunning = true;
        while (isUpdaterRunning) {
            isUpdaterRunning = model.gameTick();
            try {
                //noinspection BusyWait
                Thread.sleep(30);
            } catch (InterruptedException e) {
                isUpdaterRunning = false;
            }
        }
    };

    Thread updaterThread;

    public void start(int portNumber) {
        DBHibernate db = new DBHibernate();
        db.getAllRecords().forEach(dbRecord ->
                System.out.println(dbRecord.playerName + ": " + dbRecord.score));
        System.out.println(".................");

        try {
            model.addObserver(this::onModelUpdated);

            ip = InetAddress.getLocalHost();
            ServerSocket ss = new ServerSocket(portNumber, 0, ip);
            System.out
                    .append("Server started at port ")
                    .append(String.valueOf(portNumber))
                    .append("\n");

            // Запуск HTTP сервера для проверки имени
            startHttpServer();

            Socket cs;
            while (true) {
                cs = ss.accept();
                System.out.append("Client connected\n");

                int newPlayerId = model.registerNewPlayer("");
                ClientAtServer client = new ClientAtServer(this, cs, newPlayerId);
                client.sendModelStateToClient();
                clients.add(client);
                service.submit(client);
            }

        } catch (IOException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void startHttpServer() throws IOException {
        HttpServer httpServer = HttpServer.create(new InetSocketAddress(HTTP_PORT), 0);
        httpServer.createContext("/checkName", new NameCheckHandler());
        httpServer.setExecutor(Executors.newCachedThreadPool());
        httpServer.start();
        System.out.println("HTTP Server started on port " + HTTP_PORT);
    }

    private class NameCheckHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                String query = exchange.getRequestURI().getQuery();
                String playerName = query.split("=")[1];

                boolean nameExists = Arrays.stream(model.players)
                                        .anyMatch(player -> player != null && playerName.equals(player.name));

                String response = nameExists ? "name_exists" : "name_available";
                exchange.sendResponseHeaders(200, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            } else {
                exchange.sendResponseHeaders(405, -1); // 405 Method Not Allowed
            }
        }
    }

    void onModelUpdated() {
        for (ClientAtServer clientAtServer : clients) {
            clientAtServer.sendModelStateToClient();
        }
        if (!model.isPaused && !isUpdaterRunning) {
            updaterThread = new Thread(updaterRunnable);
            updaterThread.start();
        } else if (model.isPaused && isUpdaterRunning) {
            updaterThread.interrupt();
        }
    }

    public static void main(String[] args) {
        MainServer server = new MainServer();
        server.start(DEFAULT_SERVER_PORT);
    }
}