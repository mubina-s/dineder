package coms309.dineder.dto;

public class CreateReviewRequest {
    private Long userId;
    private Integer rating;
    private String review;

    // Constructors
    public CreateReviewRequest() {}

    public CreateReviewRequest(Long userId, Integer rating, String review) {
        this.userId = userId;
        this.rating = rating;
        this.review = review;
    }

    // Getters and Setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getReview() { return review; }
    public void setReview(String review) { this.review = review; }
}