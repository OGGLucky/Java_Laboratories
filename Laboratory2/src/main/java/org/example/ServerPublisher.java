package org.example;

import javax.xml.ws.Endpoint;

public class ServerPublisher {
    public static void main(String[] args) {
        Endpoint.publish("http://localhost:8080/ws/game", new Server());
        System.out.println("Server started");
    }
}