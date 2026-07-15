package com.example.votingsystem3;

public class AdminSession {
    private static int currentAdminId = -1;
    public static void setCurrentAdminId(int id) { currentAdminId = id; }
    public static int getCurrentAdminId() { return currentAdminId; }
}
