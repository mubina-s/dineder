package com.example.androidexample;

import java.util.List;
import java.util.Set;

public class Session {
    private Long id;
    private String joinCode;
    private String hostname;

    private Long hostId;
    private List<Restaurant> restaurantDTOList;
    private List<SessionParticipant> participantsDTOList;

    public Session() {}

    public Session(Long sessionId, String joinCode, String hostname, Long hostId, List<Restaurant> restaurants, List<SessionParticipant> participants) {
        this.id = sessionId;
        this.joinCode = joinCode;
        this.hostname = hostname;
        this.hostId = hostId;
        this.restaurantDTOList = restaurants;
        this.participantsDTOList = participants;
    }

    public String getJoinCode() {
        return this.joinCode;
    }

    public List<Restaurant> getRestaurantList() {
        return this.restaurantDTOList;
    }

    public String getHostName() {
        return this.hostname;
    }

    public Long getHostId() {return this.hostId;}

    public long getSessionId() { return this.id; }

    public List<SessionParticipant> getParticipantsDTOList() {
        return this.participantsDTOList;
    }

    // Constructor, getters, setters...
}

