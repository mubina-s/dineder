package com.example.androidexample;

public class SessionManager {
    private static Session currentSession;

    public static void setSession(Session session) {
        currentSession = session;
    }

    public static Session getSession() {
        return currentSession;
    }

    public static void clearSession() {
        currentSession = null;
    }
}

