package com.example.laba3.data;

public class PlayerData {
    public String name;
    public int score = 0;
    public int shots = 0;
    public boolean isReady = false;

    public final com.example.laba3.data.DPoint arrowEndPos;
    public boolean isArrowFlying = false;

    public PlayerData(String name, int arrowXStart, int arrowYStart) {
        this.name = name;
        arrowEndPos = new com.example.laba3.data.DPoint(arrowXStart, arrowYStart);
    }

    public void setArrowEndPos(double newX, double newY) {
        this.arrowEndPos.move(newX, newY);
    }

    public void onArrowFlyEnd(int scoresToAdd) {
        isArrowFlying = false;
        score += scoresToAdd;
        shots += 1;
    }
}
