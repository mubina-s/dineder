package com.example.androidexample;

public class SessionParticipantManager {
    private static SessionParticipant currentSessionPar;

    public static void setSessionPar(SessionParticipant par) {
        currentSessionPar = par;
    }

    public static SessionParticipant getSessionPar() {
        return currentSessionPar;
    }

    public static void clearSession() {
        currentSessionPar = null;
    }

}
