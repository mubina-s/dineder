package coms309.dineder.websocket;

import coms309.dineder.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import coms309.dineder.service.VoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket handler for real-time voting
 * Handles CAST_VOTE, VOTING_UPDATE, FINALIZE_VOTING, and FINISHED_SWIPING.
 */
@Component
public class VotingWebSocketHandler extends TextWebSocketHandler {

    @Autowired
    private VoteService voteService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Store sessions grouped by sessionId
    private final Map<Long, Map<String, WebSocketSession>> sessionGroups = new ConcurrentHashMap<>();

    // Track number of participants finished swiping per session
    private final Map<Long, Integer> finishedSwipingCount = new ConcurrentHashMap<>();

    /** Safe converters for JSON data */
    private Long toLong(Object value) {
        return value == null ? null : Long.valueOf(String.valueOf(value));
    }

    private Integer toInt(Object value) {
        return value == null ? null : Integer.valueOf(String.valueOf(value));
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = session.getId();
        Long diningSessionId = toLong(session.getAttributes().get("sessionId"));

        if (diningSessionId != null) {
            sessionGroups.computeIfAbsent(diningSessionId, k -> new ConcurrentHashMap<>())
                    .put(sessionId, session);

            System.out.println("Voting WebSocket connected: " + sessionId + " to session " + diningSessionId);

            try {
                VotingResultsResponse results = voteService.getVotingResults(diningSessionId, 1);
                VotingWebSocketMessage message = new VotingWebSocketMessage(
                        "VOTING_UPDATE", null, results, "Current voting results"
                );
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));
            } catch (Exception e) {
                System.err.println("Error sending initial results: " + e.getMessage());
            }
        } else {
            System.out.println("Voting WebSocket connected without sessionId: " + sessionId);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();

        try {
            Map<String, Object> data = objectMapper.readValue(payload, Map.class);
            String action = (String) data.get("action");

            switch (action) {
                case "CAST_VOTE":
                    handleCastVote(session, data);
                    break;
                case "GET_RESULTS":
                    handleGetResults(session, data);
                    break;
                case "FINALIZE_VOTING":
                    handleFinalizeVoting(session, data);
                    break;
                case "FINISHED_SWIPING":
                    handleFinishedSwiping(session, data);
                    break;
                default:
                    System.out.println("Unknown action: " + action);
            }
        } catch (Exception e) {
            System.err.println("Error processing voting message: " + e.getMessage());
            sendErrorMessage(session, "Error processing request: " + e.getMessage());
        }
    }

    /** Handle casting a vote */
    private void handleCastVote(WebSocketSession session, Map<String, Object> data) throws Exception {
        Long sessionParticipantId = toLong(data.get("sessionParticipantId"));
        Long restaurantId = toLong(data.get("restaurantId"));
        Integer value = toInt(data.get("value"));
        Integer round = data.containsKey("round") ? toInt(data.get("round")) : 1;
        Long diningSessionId = toLong(data.get("sessionId"));

        CastVoteRequest request = new CastVoteRequest(sessionParticipantId, restaurantId, value, round);

        // Save vote
        VoteResponse voteResponse = voteService.castVote(request);

        // Get updated totals
        VotingResultsResponse results = voteService.getVotingResults(diningSessionId, round);

        // Broadcast "VOTE_CAST"
        VotingWebSocketMessage wsMessage = new VotingWebSocketMessage(
                "VOTE_CAST", voteResponse, results, "Vote cast successfully"
        );
        broadcastToSession(diningSessionId, wsMessage);

        // Broadcast "VOTING_UPDATE"
        VotingWebSocketMessage updateMessage = new VotingWebSocketMessage(
                "VOTING_UPDATE", null, results, "Live update after vote"
        );
        broadcastToSession(diningSessionId, updateMessage);
    }

    /** Handle participant finishing swiping */
    private void handleFinishedSwiping(WebSocketSession session, Map<String, Object> data) throws Exception {
        Long diningSessionId = toLong(data.get("sessionId"));

        // Increment finished count for this session
        finishedSwipingCount.putIfAbsent(diningSessionId, 0);
        finishedSwipingCount.compute(diningSessionId, (k, v) -> (v == null ? 0 : v) + 1);

        // Get total participants
        VotingResultsResponse results = voteService.getVotingResults(diningSessionId, 1);
        int totalParticipants = results.getTotalParticipants();

        // If all participants finished, broadcast VOTING_COMPLETE
        if (finishedSwipingCount.get(diningSessionId) >= totalParticipants) {
            VotingWebSocketMessage completeMessage = new VotingWebSocketMessage(
                    "VOTING_COMPLETE", null, results, "All participants have finished swiping."
            );
            broadcastToSession(diningSessionId, completeMessage);

            // Reset count for this session
            finishedSwipingCount.remove(diningSessionId);
        }
    }

    /** Handle getting current results */
    private void handleGetResults(WebSocketSession session, Map<String, Object> data) throws Exception {
        Long diningSessionId = toLong(data.get("sessionId"));
        Integer round = data.containsKey("round") ? toInt(data.get("round")) : 1;

        VotingResultsResponse results = voteService.getVotingResults(diningSessionId, round);

        VotingWebSocketMessage message = new VotingWebSocketMessage(
                "VOTING_UPDATE", null, results, "Current results"
        );

        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));
    }

    /** Handle finalizing the voting */
    private void handleFinalizeVoting(WebSocketSession session, Map<String, Object> data) throws Exception {
        Long diningSessionId = toLong(data.get("sessionId"));
        Long restaurantId = toLong(data.get("restaurantId"));

        voteService.saveFinalResult(diningSessionId, restaurantId);

        VotingResultsResponse results = voteService.getVotingResults(diningSessionId, 1);

        VotingWebSocketMessage wsMessage = new VotingWebSocketMessage(
                "VOTING_FINALIZED", null, results, "Voting has been finalized!"
        );
        broadcastToSession(diningSessionId, wsMessage);
    }

    /** Broadcast message to all users in a session */
    private void broadcastToSession(Long diningSessionId, VotingWebSocketMessage message) {
        Map<String, WebSocketSession> sessions = sessionGroups.get(diningSessionId);
        if (sessions == null) return;

        String messageJson;
        try {
            messageJson = objectMapper.writeValueAsString(message);
        } catch (Exception e) {
            System.err.println("Error serializing message: " + e.getMessage());
            return;
        }

        sessions.values().forEach(wsSession -> {
            try {
                if (wsSession.isOpen()) {
                    wsSession.sendMessage(new TextMessage(messageJson));
                }
            } catch (IOException e) {
                System.err.println("Error sending message: " + e.getMessage());
            }
        });
    }

    /** Send error message */
    private void sendErrorMessage(WebSocketSession session, String error) {
        try {
            VotingWebSocketMessage errorMessage = new VotingWebSocketMessage(
                    "ERROR", null, null, error
            );
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(errorMessage)));
        } catch (IOException e) {
            System.err.println("Error sending error message: " + e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String sessionId = session.getId();
        Long diningSessionId = toLong(session.getAttributes().get("sessionId"));

        if (diningSessionId != null) {
            Map<String, WebSocketSession> sessions = sessionGroups.get(diningSessionId);
            if (sessions != null) {
                sessions.remove(sessionId);
                if (sessions.isEmpty()) {
                    sessionGroups.remove(diningSessionId);
                }
            }
        }

        System.out.println("Voting WebSocket closed: " + sessionId);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        System.err.println("Voting WebSocket error for session " + session.getId() + ": " + exception.getMessage());
    }
}
