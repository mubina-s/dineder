package coms309.dineder.config;

import coms309.dineder.websocket.AppReviewWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket Configuration for App Reviews
 * Configures WebSocket endpoints and handlers
 * Part of Main Feature 3/Websocket 2: App Reviews
 *
 * @author Malak Mansour
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private AppReviewWebSocketHandler appReviewWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // Register the app review WebSocket handler at /ws/reviews
        registry.addHandler(appReviewWebSocketHandler, "/ws/reviews")
                .setAllowedOrigins("*"); // Configure appropriately for production
    }
}