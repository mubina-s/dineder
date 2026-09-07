package coms309.dineder.dto;

/**
 * DTO for casting a vote request
 * @author Malak Mansour
 */
public class CastVoteRequest {
    private Long sessionParticipantId;
    private Long restaurantId;
    private Integer value; // 0 or 1
    private Integer round; // defaults to 1

    public CastVoteRequest() {}

    public CastVoteRequest(Long sessionParticipantId, Long restaurantId, Integer value, Integer round) {
        this.sessionParticipantId = sessionParticipantId;
        this.restaurantId = restaurantId;
        this.value = value;
        this.round = round;
    }

    // Getters and Setters
    public Long getSessionParticipantId() {
        return sessionParticipantId;
    }

    public void setSessionParticipantId(Long sessionParticipantId) {
        this.sessionParticipantId = sessionParticipantId;
    }

    public Long getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(Long restaurantId) {
        this.restaurantId = restaurantId;
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
}