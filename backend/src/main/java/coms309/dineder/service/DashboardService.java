package coms309.dineder.service;

import coms309.dineder.dto.DashboardDTO;
import coms309.dineder.entity.*;
import coms309.dineder.repository.RestaurantRepository;
import coms309.dineder.repository.SessionRepository;
import coms309.dineder.repository.VoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * A service class that uses complex logic to collect and generate information for the dashboard (leaderboard and stats)
 * @author Mason Gliege
 */
@Service
public class DashboardService {
    @Autowired
    private VoteRepository voteRepository;
    @Autowired
    private RestaurantRepository restaurantRepository;
    @Autowired
    private SessionRepository sessionRepository;

    public DashboardDTO generateDashboardDTO() {
        DashboardDTO dashboardDTO = new DashboardDTO();
        List<Vote> allVotes = voteRepository.findAllWithRelations();
        List<Session> allSessions = sessionRepository.findAll();
        dashboardDTO.setLeaderboard(calculateLeaderboard(allVotes));
        dashboardDTO.setTrending(calculateTrending(allVotes));
        calculateAndSetStats(dashboardDTO, allVotes, allSessions);
        return dashboardDTO;
    }





    private List<DashboardDTO.LeaderboardEntry> calculateLeaderboard(List<Vote> allVotes) {
        // Make arraylist containing each entry
        List<DashboardDTO.LeaderboardEntry> leaderboard = new ArrayList<>();
        // Mapping a restaurant name to a list of votes
        Map<String, List<Vote>> votesByRestaurant = new HashMap<>();
        for (Vote vote : allVotes) {
            String name = vote.getRestaurant().getName();
            if (!votesByRestaurant.containsKey(name)) {
                votesByRestaurant.put(name, new ArrayList<>());
            }
            votesByRestaurant.get(name).add(vote);
        }

        // Calculate the scores of each restaurant
        for (Map.Entry<String, List<Vote>> entry : votesByRestaurant.entrySet()) {
            String name = entry.getKey();
            List<Vote> restaurantVotes = entry.getValue();
            int total = restaurantVotes.size();
            if (total < 2){
                continue; // Skip this restaurant if there are less than 2 votes (too small)
            }

            // Count likes
            long likeCount = 0;
            for  (Vote v : restaurantVotes) {
                if (v.getValue() == 1){
                    likeCount++;// Increase likes
                }
            }
            // Calculate score and add to leaderboard
            double score = ((double) likeCount / total) * 100.0;
            leaderboard.add(new DashboardDTO.LeaderboardEntry(name, score));
        }



        // Make a comparator to compare on the score and then sort it in reverse
        leaderboard.sort(Comparator.comparingDouble(DashboardDTO.LeaderboardEntry::getScore).reversed());
        //Limit the results to 3 items (could potentially change later)
        if (leaderboard.size()>3){
            return leaderboard.subList(0, 3);
        }
        return leaderboard;
    }

    private DashboardDTO.TrendingEntry calculateTrending(List<Vote> allVotes) {
        // Calculate the instant over the recent period
        Instant recent = Instant.now().minus(7, ChronoUnit.DAYS);

        // Create a map to track the number of likes for each restaurant recently
        Map<String, Integer> map = new HashMap<>();

        for (Vote v : allVotes) {
            if (v.getTimestamp().isAfter(recent) && v.getValue() == 1){ // Check if the vote is recent enough, and check that the value is a like
                String name = v.getRestaurant().getName();
                if (map.containsKey(name)){
                    map.put(name, map.get(name) + 1);
                } else {
                    map.put(name, 1);
                }
            }
        }

        //Find the trending restaurant
        String winner = null;
        int max = -1;
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            if (entry.getValue() > max) {
                max = entry.getValue();
                winner = entry.getKey();
            }
        }
        if (winner != null){
            return new DashboardDTO.TrendingEntry(winner,max);
        }
        return null;
    }




    private void calculateAndSetStats(DashboardDTO dashboardDTO, List<Vote> allVotes, List<Session> allSessions) {
        // First calculate the number of active sessions and users
        long activeSessions = 0;
        long activeUsers = 0;

        // Check each session in the repository
        for (Session s : allSessions) {
            if (s.isSwipingComplete()!= null && !s.isSwipingComplete()) {
                // If swiping is not complete, increment the counts accordingly
                activeSessions++;
                activeUsers+=s.getParticipants().size();
            }
        }
        dashboardDTO.setActiveSessions(activeSessions);
        dashboardDTO.setActiveUsers(activeUsers);
        // Next, find how many votes have been cast
        dashboardDTO.setTotalDecisions(allVotes.size());
        //Finally, find the most popular cuisine
        Map<String, Integer> map = new HashMap<>();
        for (Vote v : allVotes) {// Iterate through each vote
            if (v.getValue() == 1 && v.getRestaurant() != null && v.getRestaurant().getCuisines() != null){ // Only look at likes
                for (Cuisine c : v.getRestaurant().getCuisines()) { // For a vote's restaurant, look at its cuisines
                    String name = c.getName();
                    if (map.containsKey(name)){ // Increment the count of the cuisine accordingly
                        map.put(name, map.get(name) + 1);
                    } else{
                        map.put(name, 1);
                    }
                }
            }
        }
        //Finding the winning cuisine
        String winner = "N/A";
        int max = -1;
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            if (entry.getValue() > max) {
                max = entry.getValue();
                winner = entry.getKey();
            }
        }
        dashboardDTO.setMostPopularCuisine(winner);
    }
}
