package com.example.laba3.data;

public class DPoint {
    public double x = 0.0;
    public double y = 0.0;

    public DPoint(double x, double y) {
        move(x, y);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void move(double newX, double newY) {
        this.x = newX;
        this.y = newY;
    }

    @Override
    public String toString() {
        return "MyPoint{" + "x = " + x + ", y = " + y + '}';
    }
}
