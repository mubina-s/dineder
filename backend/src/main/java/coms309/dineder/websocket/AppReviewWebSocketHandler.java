package coms309.dineder.websocket;

import coms309.dineder.dto.*;
import coms309.dineder.service.AppReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket handler for real-time app reviews
 * Implements real-time review updates and admin notifications
 * Part of Main Feature 3/Websocket 2: App Reviews
 *
 * Fulfills WebSocket requirements:
 * - Real-time updates (reviews appear instantly for all users)
 * - Database integration (all reviews saved and manipulated)
 * - Admin notifications (live alerts for low ratings)
 *
 * @author Malak Mansour
 */
@Component
public class AppReviewWebSocketHandler extends TextWebSocketHandler {

    @Autowired
    private AppReviewService reviewService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Store all connected sessions
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    // Store admin sessions separately for targeted notifications
    private final Map<String, WebSocketSession> adminSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = session.getId();
        sessions.put(sessionId, session);

        // Check if user is admin (would be passed via query parameter or session attribute)
        String isAdmin = (String) session.getAttributes().get("isAdmin");
        if ("true".equals(isAdmin)) {
            adminSessions.put(sessionId, session);
        }

        // Send current statistics to newly connected user
        AppStatisticsResponse stats = reviewService.getAppStatistics();
        String statsJson = objectMapper.writeValueAsString(
                new ReviewWebSocketMessage("STATISTICS", null,
                        "Total Users: " + stats.getTotalUsers() +
                                ", Total Reviews: " + stats.getTotalReviews() +
                                ", Avg Rating: " + String.format("%.2f", stats.getAverageRating()))
        );
        session.sendMessage(new TextMessage(statsJson));

        System.out.println("WebSocket connection established: " + sessionId);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();

        try {
            // Parse incoming message
            Map<String, Object> data = objectMapper.readValue(payload, Map.class);
            String action = (String) data.get("action");

            switch (action) {
                case "CREATE_REVIEW":
                    handleCreateReview(data);
                    break;
                case "UPDATE_REVIEW":
                    handleUpdateReview(data);
                    break;
                case "DELETE_REVIEW":
                    handleDeleteReview(data);
                    break;
                case "GET_REVIEWS":
                    handleGetReviews(session);
                    break;
                default:
                    System.out.println("Unknown action: " + action);
            }
        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
            sendErrorMessage(session, "Error processing request: " + e.getMessage());
        }
    }

    /**
     * Handle creating a new review
     * Saves to database and broadcasts to all users
     */
    private void handleCreateReview(Map<String, Object> data) throws Exception {
        Long userId = ((Number) data.get("userId")).longValue();
        Integer rating = (Integer) data.get("rating");
        String reviewText = (String) data.get("review");

        // Create review request
        CreateReviewRequest request = new CreateReviewRequest(userId, rating, reviewText);

        // Save to database (atomic operation)
        ReviewResponse savedReview = reviewService.createReview(request);

        // Broadcast to all connected users
        ReviewWebSocketMessage wsMessage = new ReviewWebSocketMessage(
                "NEW_REVIEW", savedReview, "New review posted!"
        );
        broadcastToAll(wsMessage);

        // Send admin notification if low rating
        if (reviewService.shouldNotifyAdmin(rating)) {
            ReviewWebSocketMessage adminNotification = new ReviewWebSocketMessage(
                    "ADMIN_NOTIFICATION", savedReview,
                    "⚠️ Low rating alert! User rated " + rating + " stars"
            );
            broadcastToAdmins(adminNotification);
        }
    }

    /**
     * Handle updating an existing review
     */
    private void handleUpdateReview(Map<String, Object> data) throws Exception {
        Long reviewId = ((Number) data.get("reviewId")).longValue();
        Integer newRating = data.containsKey("rating") ? (Integer) data.get("rating") : null;
        String newReviewText = (String) data.get("review");

        // Update in database
        ReviewResponse updatedReview = reviewService.updateReview(reviewId, newRating, newReviewText);

        // Broadcast update to all users
        ReviewWebSocketMessage wsMessage = new ReviewWebSocketMessage(
                "UPDATE_REVIEW", updatedReview, "Review updated"
        );
        broadcastToAll(wsMessage);
    }

    /**
     * Handle deleting a review
     */
    private void handleDeleteReview(Map<String, Object> data) throws Exception {
        Long reviewId = ((Number) data.get("reviewId")).longValue();

        // Delete from database
        reviewService.deleteReview(reviewId);

        // Broadcast deletion to all users
        ReviewWebSocketMessage wsMessage = new ReviewWebSocketMessage(
                "DELETE_REVIEW", null, "Review deleted: " + reviewId
        );
        broadcastToAll(wsMessage);
    }

    /**
     * Handle request to get all reviews
     */
    private void handleGetReviews(WebSocketSession session) throws Exception {
        var reviews = reviewService.getAllReviews();
        String reviewsJson = objectMapper.writeValueAsString(reviews);
        session.sendMessage(new TextMessage(reviewsJson));
    }

    /**
     * Broadcast message to all connected users
     */
    private void broadcastToAll(ReviewWebSocketMessage message) {
        String messageJson;
        try {
            messageJson = objectMapper.writeValueAsString(message);
        } catch (Exception e) {
            System.err.println("Error serializing message: " + e.getMessage());
            return;
        }

        sessions.values().forEach(session -> {
            try {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(messageJson));
                }
            } catch (IOException e) {
                System.err.println("Error sending message: " + e.getMessage());
            }
        });
    }

    /**
     * Broadcast message only to admin users
     */
    private void broadcastToAdmins(ReviewWebSocketMessage message) {
        String messageJson;
        try {
            messageJson = objectMapper.writeValueAsString(message);
        } catch (Exception e) {
            System.err.println("Error serializing admin message: " + e.getMessage());
            return;
        }

        adminSessions.values().forEach(session -> {
            try {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(messageJson));
                }
            } catch (IOException e) {
                System.err.println("Error sending admin notification: " + e.getMessage());
            }
        });
    }

    /**
     * Send error message to specific session
     */
    private void sendErrorMessage(WebSocketSession session, String error) {
        try {
            ReviewWebSocketMessage errorMessage = new ReviewWebSocketMessage(
                    "ERROR", null, error
            );
            String errorJson = objectMapper.writeValueAsString(errorMessage);
            session.sendMessage(new TextMessage(errorJson));
        } catch (IOException e) {
            System.err.println("Error sending error message: " + e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String sessionId = session.getId();
        sessions.remove(sessionId);
        adminSessions.remove(sessionId);
        System.out.println("WebSocket connection closed: " + sessionId);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        System.err.println("WebSocket error for session " + session.getId() + ": " + exception.getMessage());
    }
}