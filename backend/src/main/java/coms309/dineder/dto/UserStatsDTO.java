package coms309.dineder.dto;

public class UserStatsDTO {
    private Long userId;
    private int totalSessions;
    private int totalVotes;
    private double averageRating;

    public UserStatsDTO(Long userId, int totalSessions, int totalVotes, double averageRating) {
        this.userId = userId;
        this.totalSessions = totalSessions;
        this.totalVotes = totalVotes;
        this.averageRating = averageRating;
    }

    public Long getUserId() { return userId; }
    public int getTotalSessions() { return totalSessions; }
    public int getTotalVotes() { return totalVotes; }
    public double getAverageRating() { return averageRating; }

    public void setUserId(Long userId) { this.userId = userId; }
    public void setTotalSessions(int totalSessions) { this.totalSessions = totalSessions; }
    public void setTotalVotes(int totalVotes) { this.totalVotes = totalVotes; }
    public void setAverageRating(double averageRating) { this.averageRating = averageRating; }
}
