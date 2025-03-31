package com.example.laba3.server;

import com.example.laba3.data.ClientAction;
import com.example.laba3.data.ClientMsg;
import com.example.laba3.data.ServerMsg;
import com.google.gson.Gson;

import java.io.*;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientAtServer implements Runnable {
    private final MainServer mainServer;
    private final Socket clientSocket;
    private final int id;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private final Gson gson = new Gson();

    public ClientAtServer(MainServer mainServer, Socket clientSocket, int id) {
        this.clientSocket = clientSocket;
        this.mainServer = mainServer;
        this.id = id;

        try {
            OutputStream outputStream = clientSocket.getOutputStream();
            dataOutputStream = new DataOutputStream(outputStream);
        } catch (IOException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void run() {
        System.out.println("Thread for client started");
        try {
            InputStream inputStream = clientSocket.getInputStream();
            dataInputStream = new DataInputStream(inputStream);

            while (true) {
                // Reading messages from client
                String s = dataInputStream.readUTF();
                System.out.println("Msg: " + s);

                ClientMsg msg = gson.fromJson(s, ClientMsg.class);

                if (id != -1) {
                    if (msg.getAction() == ClientAction.HELLO) {
                        mainServer.model.players[id].name = msg.text;
                        mainServer.onModelUpdated();
                    } else if (msg.getAction() == ClientAction.READY) {
                        mainServer.model.setPlayerReady(id, true);
                    } else if (msg.getAction() == ClientAction.PAUSE) {
                        mainServer.model.setPlayerReady(id, false);
                    } else if (msg.getAction() == ClientAction.SHOOT) {
                        if (!mainServer.model.isPaused &&
                                !mainServer.model.players[id].isArrowFlying) {
                            mainServer.model.players[id].isArrowFlying = true;
                            mainServer.model.players[id].shots += 1;
                            mainServer.onModelUpdated();
                        }
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    void sendModelStateToClient() {
        try {
            dataOutputStream.writeUTF(gson.toJson(new ServerMsg(id, mainServer.model)));
        } catch (IOException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }
}
