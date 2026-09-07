package coms309.dineder.dto;

import java.time.Instant;

public class ReviewResponse {
    private Long id;
    private Long userId;
    private String username;
    private Integer rating;
    private String review;
    private Instant timestamp;

    // Constructor
    public ReviewResponse() {}

    public ReviewResponse(Long id, Long userId, String username, Integer rating, String review, Instant timestamp) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.rating = rating;
        this.review = review;
        this.timestamp = timestamp;
    }

    // Getters and Setters (generate all)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getReview() { return review; }
    public void setReview(String review) { this.review = review; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}