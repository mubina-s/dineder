package coms309.dineder.dto;

/**
 * DTO for WebSocket voting messages
 * @author Malak Mansour
 */
public class VotingWebSocketMessage {
    private String type; // "VOTE_CAST", "VOTING_UPDATE", "VOTING_COMPLETE", "ERROR"
    private VoteResponse vote;
    private VotingResultsResponse results;
    private String message;

    public VotingWebSocketMessage() {}

    public VotingWebSocketMessage(String type, VoteResponse vote, VotingResultsResponse results, String message) {
        this.type = type;
        this.vote = vote;
        this.results = results;
        this.message = message;
    }

    // Getters and Setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public VoteResponse getVote() {
        return vote;
    }

    public void setVote(VoteResponse vote) {
        this.vote = vote;
    }

    public VotingResultsResponse getResults() {
        return results;
    }

    public void setResults(VotingResultsResponse results) {
        this.results = results;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}