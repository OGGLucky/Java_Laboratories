package com.example.laba3.server;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "DBRecords")
public class DBRecord {
    public String getPlayerName() {
        return playerName;
    }

    public int getScore() {
        return score;
    }

    @Id
    public String playerName;
    public int score;

    public DBRecord() {
    }

    public DBRecord(String playerName, int score) {
        this.playerName = playerName;
        this.score = score;
    }
}
