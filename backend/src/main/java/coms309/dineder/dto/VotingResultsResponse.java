package coms309.dineder.dto;

import java.util.List;

/**
 * DTO for voting results
 * @author Malak Mansour
 */
public class VotingResultsResponse {
    private Long sessionId;
    private Integer round;
    private List<RestaurantVoteCount> results;
    private Integer totalVotes;
    private Integer totalParticipants;
    private boolean hasWinner;
    private Integer winningRestaurantId;

    public VotingResultsResponse() {}

    // Getters and Setters
    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public Integer getRound() {
        return round;
    }

    public void setRound(Integer round) {
        this.round = round;
    }

    public List<RestaurantVoteCount> getResults() {
        return results;
    }

    public void setResults(List<RestaurantVoteCount> results) {
        this.results = results;
    }

    public Integer getTotalVotes() {
        return totalVotes;
    }

    public void setTotalVotes(Integer totalVotes) {
        this.totalVotes = totalVotes;
    }

    public Integer getTotalParticipants() {
        return totalParticipants;
    }

    public void setTotalParticipants(Integer totalParticipants) {
        this.totalParticipants = totalParticipants;
    }

    public boolean isHasWinner() {
        return hasWinner;
    }

    public void setHasWinner(boolean hasWinner) {
        this.hasWinner = hasWinner;
    }

    public Integer getWinningRestaurantId() {
        return winningRestaurantId;
    }

    public void setWinningRestaurantId(Integer winningRestaurantId) {
        this.winningRestaurantId = winningRestaurantId;
    }

    /**
     * Inner class for restaurant vote counts
     */
    public static class RestaurantVoteCount {
        private Integer restaurantId;
        private String restaurantName;
        private Integer voteCount;
        private Double percentage;

        public RestaurantVoteCount() {}

        public RestaurantVoteCount(Integer restaurantId, String restaurantName, Integer voteCount, Double percentage) {
            this.restaurantId = restaurantId;
            this.restaurantName = restaurantName;
            this.voteCount = voteCount;
            this.percentage = percentage;
        }

        // Getters and Setters
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

        public Integer getVoteCount() {
            return voteCount;
        }

        public void setVoteCount(Integer voteCount) {
            this.voteCount = voteCount;
        }

        public Double getPercentage() {
            return percentage;
        }

        public void setPercentage(Double percentage) {
            this.percentage = percentage;
        }
    }
}