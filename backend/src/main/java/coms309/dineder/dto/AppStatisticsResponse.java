package coms309.dineder.dto;

public class AppStatisticsResponse {
    private Long totalUsers;
    private Long totalReviews;
    private Double averageRating;

    public AppStatisticsResponse() {}

    public AppStatisticsResponse(Long totalUsers, Long totalReviews, Double averageRating) {
        this.totalUsers = totalUsers;
        this.totalReviews = totalReviews;
        this.averageRating = averageRating;
    }

    // Getters and Setters
    public Long getTotalUsers() { return totalUsers; }
    public void setTotalUsers(Long totalUsers) { this.totalUsers = totalUsers; }

    public Long getTotalReviews() { return totalReviews; }
    public void setTotalReviews(Long totalReviews) { this.totalReviews = totalReviews; }

    public Double getAverageRating() { return averageRating; }
    public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }
}