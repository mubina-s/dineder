package com.example.androidexample;

public class SessionParticipant {

    private Long participantId;

    private Long userId;

    private String name;

    private boolean isReady;

    private boolean finishedSwiping;

    public SessionParticipant(Long participantId, Long userId, String name, boolean isReady, boolean finishedSwiping) {
        this.participantId = participantId;
        this.userId = userId;
        this.name = name;
        this.isReady = isReady;
        this.finishedSwiping = finishedSwiping;
    }

    public Long getParticipantId() {
        return participantId;
    }

    public Long getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public boolean getIsReady() {
        return isReady;
    }

    public boolean getfinishedSwiping(){
        return finishedSwiping;
    }
}
