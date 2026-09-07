package coms309.dineder.config;


import coms309.dineder.websocket.LobbyWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * Websocket configuration for user join/ready up screen
 * @author Mason Gliege
 */
@Configuration
@EnableWebSocket
public class LobbyWebSocketConfig implements WebSocketConfigurer {
    @Autowired
    private LobbyWebSocketHandler lobbyWebSocketHandler;


    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(lobbyWebSocketHandler, "/ws/lobby/*/*").setAllowedOrigins("*");
    }
}
