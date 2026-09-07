package coms309.dineder.dto;

import java.util.List;

/**
 * A DTO that is used as a container for all leaderboard and statistic info for the home page
 * @author Mason Gliege
 */
public class DashboardDTO {
    /**
     * The list containing the leaderboard entries
     */
    private List<LeaderboardEntry> leaderboard;
    /**
     * The trending restaurant
     */
    private TrendingEntry trending;

    // STATS:
    private long totalDecisions; // total number of votes cast
    private long activeSessions; // number of sessions (where finishedSwiping = false)
    private long activeUsers; // Number of users in active sessions
    private String mostPopularCuisine; // Popular by number of votes associated with it

    public DashboardDTO() {
    }
    public List<LeaderboardEntry> getLeaderboard() {
        return leaderboard;
    }
    public void setLeaderboard(List<LeaderboardEntry> leaderboard) {
        this.leaderboard = leaderboard;
    }
    public TrendingEntry getTrending() {
        return trending;
    }
    public void setTrending(TrendingEntry trending) {
        this.trending = trending;
    }
    public long getTotalDecisions() {
        return totalDecisions;
    }
    public void setTotalDecisions(long totalDecisions) {
        this.totalDecisions = totalDecisions;
    }
    public long getActiveSessions() {
        return activeSessions;
    }
    public void setActiveSessions(long activeSessions) {
        this.activeSessions = activeSessions;
    }
    public long getActiveUsers() {
        return activeUsers;
    }
    public void setActiveUsers(long activeUsers) {
        this.activeUsers = activeUsers;
    }
    public String getMostPopularCuisine() {
        return mostPopularCuisine;
    }
    public void setMostPopularCuisine(String mostPopularCuisine) {
        this.mostPopularCuisine = mostPopularCuisine;
    }

    /**
     * A helper class with a restaurants name and score. One element of the list
     */
    public static class LeaderboardEntry{
        private String name;
        private Double score; // Percentage of positive votes

        public LeaderboardEntry(String name, Double score) {
            this.name = name;
            this.score = score;
        }
        public String getName() {
            return name;
        }

        public Double getScore() {
            return score;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setScore(Double score) {
            this.score = score;
        }
    }

    /**
     * Used for the "trending restaurant", and how many votes it has recently
     */
    public static class TrendingEntry{
        private String name;
        private int recentVotesCount;

        public TrendingEntry(String name, int recentVotesCount) {
            this.name = name;
            this.recentVotesCount = recentVotesCount;
        }
        public String getName() {
            return name;
        }
        public int getRecentVotesCount() {
            return recentVotesCount;
        }
        public void setName(String name) {
            this.name = name;
        }
        public void setRecentVotesCount(int recentVotesCount) {
            this.recentVotesCount = recentVotesCount;
        }
    }

}
