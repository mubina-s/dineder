package coms309.dineder.dto;

public class ReviewWebSocketMessage {
    private String type;
    private ReviewResponse review;
    private String message;

    public ReviewWebSocketMessage() {}

    public ReviewWebSocketMessage(String type, ReviewResponse review, String message) {
        this.type = type;
        this.review = review;
        this.message = message;
    }

    // Getters and Setters
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public ReviewResponse getReview() { return review; }
    public void setReview(ReviewResponse review) { this.review = review; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}