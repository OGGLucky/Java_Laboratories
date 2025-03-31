package com.example.laba3.data;

public class ServerMsg {

    public int clientId;
    public Model model;

    public ServerMsg(int clientId, Model model) {
        this.clientId = clientId;
        this.model = model;
    }
}
