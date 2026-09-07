package coms309.dineder.service;

import coms309.dineder.dto.UserHistoryDTO;
import coms309.dineder.dto.UserStatsDTO;
import coms309.dineder.entity.SessionResult;
import coms309.dineder.entity.Vote;
import coms309.dineder.entity.User;
import coms309.dineder.repository.SessionResultRepository;
import coms309.dineder.repository.UserRepository;
import coms309.dineder.repository.VoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserHistoryService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VoteRepository voteRepository;

    @Autowired
    private SessionResultRepository sessionResultRepository;

    // -------------------------------
    public UserHistoryDTO getUserHistory(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return null;

        List<Vote> votes = voteRepository.findByUserId(userId);

        // Group votes by session
        Map<Long, List<Vote>> votesBySession = votes.stream()
                .collect(Collectors.groupingBy(v -> v.getParticipant().getSession().getId()));

        List<Map<String, Object>> sessionHistory = new ArrayList<>();
        for (Map.Entry<Long, List<Vote>> entry : votesBySession.entrySet()) {
            Long sessionId = entry.getKey();
            List<Vote> sessionVotes = entry.getValue();

            Map<String, Object> sessionMap = new HashMap<>();
            sessionMap.put("sessionId", sessionId);
            sessionMap.put("votes", sessionVotes.stream().map(v -> {
                Map<String, Object> voteMap = new HashMap<>();
                voteMap.put("restaurantId", v.getRestaurant().getId());
                voteMap.put("restaurantName", v.getRestaurant().getName());
                voteMap.put("voteRound", v.getRound());
                voteMap.put("value", v.getValue()); // like/dislike
                return voteMap;
            }).collect(Collectors.toList()));

            // Include final session result if exists
            SessionResult result = sessionResultRepository.findBySessionId(sessionId);
            if (result != null) {
                Map<String, Object> finalResult = new HashMap<>();
                finalResult.put("restaurantId", result.getRestaurant().getId());
                finalResult.put("restaurantName", result.getRestaurant().getName());
                sessionMap.put("finalResult", finalResult);
            }

            sessionHistory.add(sessionMap);
        }

        return new UserHistoryDTO(userId, sessionHistory);
    }

    // -------------------------------
    public UserStatsDTO getUserStats(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return null;

        List<Vote> votes = voteRepository.findByUserId(userId);

        int totalVotes = votes.size();
        int totalSessions = (int) votes.stream()
                .map(v -> v.getParticipant().getSession().getId())
                .distinct()
                .count();

        double averageRating = votes.stream()
                .mapToDouble(v -> v.getRestaurant().getRating())
                .average()
                .orElse(0.0);

        List<String> topRestaurants = voteRepository.findTopRestaurantsByUserId(userId);
        List<String> topCuisines = voteRepository.findTopCuisinesByUserId(userId);

        int likes = voteRepository.countLikesByUserId(userId);
        int dislikes = voteRepository.countDislikesByUserId(userId);

        // Build stats map (you can customize further)
        UserStatsDTO stats = new UserStatsDTO(userId, totalSessions, totalVotes, averageRating);
        // Optionally, include top restaurants/cuisines in the DTO if you extend it

        return stats;
    }
}
