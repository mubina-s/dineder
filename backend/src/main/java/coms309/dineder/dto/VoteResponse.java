package coms309.dineder.dto;

import java.time.Instant;

/**
 * DTO for vote response
 * @author Malak Mansour
 */
public class VoteResponse {
    private Integer id;
    private Integer sessionParticipantId;
    private String username;
    private Integer restaurantId;
    private String restaurantName;
    private Integer value;
    private Integer round;
    private Instant timestamp;

    public VoteResponse() {}

    public VoteResponse(Integer id, Integer sessionParticipantId, String username,
                        Integer restaurantId, String restaurantName, Integer value,
                        Integer round, Instant timestamp) {
        this.id = id;
        this.sessionParticipantId = sessionParticipantId;
        this.username = username;
        this.restaurantId = restaurantId;
        this.restaurantName = restaurantName;
        this.value = value;
        this.round = round;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getSessionParticipantId() {
        return sessionParticipantId;
    }

    public void setSessionParticipantId(Integer sessionParticipantId) {
        this.sessionParticipantId = sessionParticipantId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(Integer restaurantId) {
        this.restaurantId = restaurantId;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public Integer getRound() {
        return round;
    }

    public void setRound(Integer round) {
        this.round = round;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}