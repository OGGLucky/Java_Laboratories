package com.example.laba3.server;

import java.util.ArrayList;

public interface DB {
    ArrayList<DBRecord> getAllRecords();

    void insOrUpdRecord(DBRecord rec);
}
