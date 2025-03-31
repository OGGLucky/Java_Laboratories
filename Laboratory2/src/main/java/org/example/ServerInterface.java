package org.example;

import javax.jws.WebMethod;
import javax.jws.WebService;

@WebService
public interface ServerInterface {
    @WebMethod
    String processMove(String type, int rw, int cl, int playerId);

    @WebMethod
    String getCurrentState();

    @WebMethod
    String initializeGame(int playerId);
}