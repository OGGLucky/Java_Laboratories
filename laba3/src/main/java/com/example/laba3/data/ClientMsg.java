package com.example.laba3.data;

public class ClientMsg {
    public com.example.laba3.data.ClientAction action;

    public String text = null;

    public ClientMsg(com.example.laba3.data.ClientAction action) {
        this.action = action;
    }

    public ClientMsg(com.example.laba3.data.ClientAction action, String text) {
        this.action = action;
        this.text = text;
    }

    public com.example.laba3.data.ClientAction getAction() {
        return action;
    }

    @Override
    public String toString() {
        if (text == null) {
            return "ClientMsg { " + "action = " + action + " }";
        } else {
            return "ClientMsg { " + "action = " + action + ", text = " + text + " }";
        }
    }
}
