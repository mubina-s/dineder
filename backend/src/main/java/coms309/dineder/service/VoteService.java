package coms309.dineder.service;

import coms309.dineder.entity.Vote;
import coms309.dineder.entity.Restaurant;
import coms309.dineder.repository.RestaurantRepository;
import coms309.dineder.entity.Session;
import coms309.dineder.repository.SessionRepository;
import coms309.dineder.entity.SessionParticipant;
import coms309.dineder.repository.SessionParticipantRepository;
import coms309.dineder.entity.SessionResult;
import coms309.dineder.repository.SessionResultRepository;
import coms309.dineder.dto.*;

import coms309.dineder.repository.VoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

/**
 * Service layer for Voting functionality
 * @author Malak Mansour
 */
@Service
public class VoteService {

    @Autowired
    private VoteRepository voteRepository;

    @Autowired
    private SessionParticipantRepository participantRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private SessionResultRepository sessionResultRepository;

    /**
     * Cast a vote
     */
    @Transactional
    public VoteResponse castVote(CastVoteRequest request) {
        // Validate inputs
        if (request.getValue() != 0 && request.getValue() != 1) {
            throw new IllegalArgumentException("Vote value must be 0 or 1");
        }

        SessionParticipant participant = participantRepository.findById(request.getSessionParticipantId().longValue())
                .orElseThrow(() -> new RuntimeException("Participant not found"));

        Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId().longValue())
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        int round = request.getRound() != null ? request.getRound() : 1;

        // Create and save vote
        Vote vote = new Vote(participant, restaurant, request.getValue(), round);
        Vote savedVote = voteRepository.save(vote);

        return convertToResponse(savedVote);
    }

    /**
     * Get voting results for a session
     * FIX: Added @Transactional so Hibernate can load session.getParticipants()
     */
    @Transactional
    public VotingResultsResponse getVotingResults(Long sessionId, int round) {
        Session session = sessionRepository.findById((long) sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        VotingResultsResponse response = new VotingResultsResponse();
        response.setSessionId(sessionId);
        response.setRound(round);

        // Get total participants
        int totalParticipants = session.getParticipants().size();
        response.setTotalParticipants(totalParticipants);

        // Get vote counts grouped by restaurant
        List<Object[]> voteCounts = voteRepository.getVoteCountsByRestaurant(sessionId, round);
        List<VotingResultsResponse.RestaurantVoteCount> results = new ArrayList<>();

        int totalVotes = 0;
        int maxVotes = 0;
        Integer winningRestaurantId = null;
        boolean tie = false;

        for (Object[] row : voteCounts) {
            Long restaurantIdLong = (Long) row[0];
            Integer restaurantId = restaurantIdLong.intValue();
            Long count = (Long) row[1];
            int voteCount = count.intValue();

            Restaurant restaurant = restaurantRepository.findById(restaurantIdLong)
                    .orElse(null);

            if (restaurant != null) {
                double percentage = (totalParticipants > 0) ? (voteCount * 100.0 / totalParticipants) : 0;

                VotingResultsResponse.RestaurantVoteCount result =
                        new VotingResultsResponse.RestaurantVoteCount(
                                restaurantId,
                                restaurant.getName(),
                                voteCount,
                                percentage
                        );
                results.add(result);

                totalVotes += voteCount;

                // Check for winner
                if (voteCount > maxVotes) {
                    maxVotes = voteCount;
                    winningRestaurantId = restaurantId;
                    tie = false;
                } else if (voteCount == maxVotes && maxVotes > 0) {
                    tie = true;
                }
            }
        }

        // Sort by vote count descending
        results.sort((a, b) -> b.getVoteCount().compareTo(a.getVoteCount()));

        response.setResults(results);
        response.setTotalVotes(totalVotes);
        response.setHasWinner(!tie && winningRestaurantId != null);
        response.setWinningRestaurantId(winningRestaurantId);

        return response;
    }

    /**
     * Save final session result
     */
    @Transactional
    public SessionResult saveFinalResult(Long sessionId, Long restaurantId) {
        Session session = sessionRepository.findById((long) sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        Restaurant restaurant = restaurantRepository.findById((long) restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        // Check if result already exists
        SessionResult existing = sessionResultRepository.findBySessionId(sessionId);
        if (existing != null) {
            existing.setRestaurant(restaurant);
            return sessionResultRepository.save(existing);
        }

        SessionResult result = new SessionResult(session, restaurant);
        return sessionResultRepository.save(result);
    }

    /**
     * Get final session result
     */
    public SessionResult getFinalResult(Long sessionId) {
        return sessionResultRepository.findBySessionId(sessionId);
    }

    /**
     * Convert Vote entity to VoteResponse DTO
     */
    private VoteResponse convertToResponse(Vote vote) {
        return new VoteResponse(
                vote.getId().intValue(),
                vote.getParticipant().getId().intValue(),
                vote.getParticipant().getUser().getUsername(),
                vote.getRestaurant().getId().intValue(),
                vote.getRestaurant().getName(),
                vote.getValue(),
                vote.getRound(),
                vote.getTimestamp()
        );
    }

    /**
     * delete vote by id
     */
    @Transactional
    public void deleteVote(Long id) {
        if (!voteRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Vote not found by id: " + id);
        }
        voteRepository.deleteById(id);
    }

    @Transactional
    public void deleteVotesBySessionId(Long sessionId) {
        if (!sessionRepository.existsById(sessionId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found by id: " + sessionId);
        }
        voteRepository.deleteBySessionId(sessionId);
    }
}
