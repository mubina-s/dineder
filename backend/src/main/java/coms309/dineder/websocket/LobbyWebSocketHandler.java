package coms309.dineder.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import coms309.dineder.dto.SessionDTO;
import coms309.dineder.dto.SessionParticipantDTO;
import coms309.dineder.entity.SessionParticipant;
import coms309.dineder.service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Websocket handler for the lobby (the user join/ready up screen). Part of main feature 2/ websocket 1:
 * User join/ready up. All users in a lobby need real time updates when something happens in the
 * lobby (a host joins, readies up, gets kicked, 'ready percentage' increases, or session starts)
 * @author Mason Gliege
 */
@Component
public class LobbyWebSocketHandler extends TextWebSocketHandler {

    @Autowired
    SessionService sessionService;

    /**
     * This is the collection of all sessions, and each user within them. The long is the session id.
     * The String is the websockets unique id, and the WebSocketSession is the connection
     *
     */
    private final Map<Long, Map<String, WebSocketSession>> sessionGroups = new ConcurrentHashMap<>();

    /**
     * This links the String websocket session id to the Long participantId
     * Essentially acts as a translator from websocket to session (by id)
     */
    private final Map<String, Long> webSocketToParticipant = new ConcurrentHashMap<>();

    /**
     * Helps with conversion of json objects
     */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * What to do after a connection is established
     * @param session websocket session to pass in
     * @throws Exception
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // Get session and participant id from Websocket session
        Long sessionId = getSessionIdFromUri(session);
        Long participantId = getParticipantIdFromUri(session);

        // Neither of these can be null
        if (sessionId == null || participantId == null) {
            session.close(CloseStatus.BAD_DATA);
        }

        // Add a user to sessionGroups
        //Find the right lobby
        Map<String, WebSocketSession> lobby = sessionGroups.get(sessionId);
        // Check if lobby exists
        if (lobby == null) {
            // Create new map
            lobby = new ConcurrentHashMap<>();
            sessionGroups.put(sessionId, lobby);
        }
        // Add user to either new or existing map
        lobby.put(session.getId(), session);

        //Add user to websSocketToParticipantMap
        webSocketToParticipant.put(session.getId(), participantId);

        // Broadcast new lobby status
        broadcastLobbyUpdate(sessionId);
    }


    /**
     * Called when a user dc's (either by closing the app, getting kicked, or session starting)
     * @param session
     * @param status
     * @throws Exception
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // Find the participant id
        Long participantId = webSocketToParticipant.get(session.getId());
        Long sessionId = getSessionIdFromUri(session); // id of the lobby (session)

        // Remove the participant from webSocketToParticipant
        webSocketToParticipant.remove(session.getId());


        if (sessionId == null){ // do nothing if it wasnt fully set up to begin with
            return;
        }
        Map<String, WebSocketSession> lobby = sessionGroups.get(sessionId);
        if (lobby != null) {
            lobby.remove(session.getId()); // Remove the person
            if (lobby.isEmpty()){
                sessionGroups.remove(sessionId); // remove empty lobby if theres no one in it
            }
        }


        // Check the state of the session, if it exists
        SessionDTO sessionDTO;
        try{
            sessionDTO = sessionService.getSessionByIdDTO(sessionId);
        } catch (Exception e){
            System.out.println(e.getStackTrace());
            System.out.println("Session "+sessionId+" not found");
            return;
        }

        // Check if the session is live (can users still join?)
        if (sessionDTO.isLive()){
            broadcastLobbyUpdate(sessionId); //broadcast the update if its live
        }
        // No broadcast if not live
    }


    /**
     * This handles things coming from the frontend
     * @param session
     * @param message
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // STEP 1: figure out who sent the message and what session they're in
        Long participantId = webSocketToParticipant.get(session.getId());
        Long sessionId = getSessionIdFromUri(session);

        // Check for nulls
        if (participantId == null || sessionId == null) {
            System.out.println("Message from unknown participant or session");
            return;
        }
        // STEP 2: get the data from the incoming json
        String payload = message.getPayload();
        Map<String, Object> data = objectMapper.readValue(payload, Map.class);
        String action = (String) data.get("action");

        // STEP 3: Decide what to do with the incoming action
        try{
            if (action.equals("SET_READY")){
                // a participant readies up
                boolean isReady = (Boolean) data.get("isReady");
                sessionService.setParticipantReady(participantId, isReady); // save to database (and broadcast)
            } else if (action.equals("FORCE_START")){
                // A host force starts a session
                Long hostId = ((Number) data.get("hostId")).longValue();
                sessionService.startSession(hostId, sessionId); // start session (checks conditions, updates database, broadcasts)
            } else if (action.equals("KICK_PARTICIPANT")){
                // the host kicks a participant
                Long hostId = ((Number) data.get("hostUserId")).longValue();
                Long participantToKickId = ((Number) data.get("participantToKickId")).longValue();
                // kick the participant and update database
                sessionService.kick(hostId, participantToKickId);
                // close the connection for the kicked participant
                String websocketSessionIdToKick = null;
                // Find websocket id
                for (Map.Entry<String, Long> entry : webSocketToParticipant.entrySet()) {
                    if (entry.getValue().equals(participantToKickId)) {
                        websocketSessionIdToKick = entry.getKey();
                        break;
                    }
                }
                // If found, we kick them
                if (websocketSessionIdToKick != null) {
                    Map<String, WebSocketSession> lobby = sessionGroups.get(sessionId);
                    if (lobby != null) {
                        WebSocketSession sessionToKick = lobby.get(websocketSessionIdToKick);
                        if (sessionToKick != null && sessionToKick.isOpen()) {
                            // send final message and close connection
                            String kickedMessage = "{\"type\": \"KICKED\", \"message\": \"You have been kicked by the host.\"}";
                            sessionToKick.sendMessage(new TextMessage(kickedMessage));
                            sessionToKick.close(CloseStatus.NORMAL);
                        }
                    }
                }
            }
        } catch (Exception e){
            System.err.println("Error in processing message : " + e.getMessage());
            String errorJson = "{\"type\": \"ERROR\", \"message\": \"" + e.getMessage() + "\"}";
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(errorJson));
            }
        }
    }

    /**
     * This methods broadcasts the changes to the lobby (kicking, leaving, readying up, etc)
     * @param sessionId
     */
    public void broadcastLobbyUpdate(Long sessionId) {
        Map<String, WebSocketSession> lobby = sessionGroups.get(sessionId);
        objectMapper.registerModule(new JavaTimeModule()); // used for bug fix. object mapper did not know what to do with instant
        // do nothing if no one is in the lobby
        if (lobby == null || lobby.isEmpty()){
            return;
        }

        try {
            // Get updated status on the session
            SessionDTO sessionDTO = sessionService.getSessionByIdDTO(sessionId);
            // Check if not live (session has started, users can no longer join)
            if (!sessionDTO.isLive()){
                broadcastSessionStart(lobby, sessionId);
                return;
            }
            // Lobby is still live at this point
            // Calculate ready percentage
            List<SessionParticipantDTO> participantDTOList = sessionDTO.getParticipantsDTOList();

            long numParticipants = participantDTOList.size();
            long numReadyParticipants = 0;

            for (SessionParticipantDTO participantDTO : participantDTOList) {
                if (participantDTO.isReady()) {
                    numReadyParticipants++;
                }
            }

            // Store percentage as integer
            int readyPercentage = 0;
            if (numReadyParticipants > 0){
                readyPercentage = (int) (((double) numReadyParticipants / numParticipants) * 100);
            }


            // create update message
            LobbyUpdate update = new LobbyUpdate("LOBBY_UPDATE", sessionDTO, readyPercentage);

            // Convert to string
            String jsonMessage = objectMapper.writeValueAsString(update);

            // Send to every user in lobby
            for (WebSocketSession wsSession : lobby.values()) {
                if (wsSession.isOpen()) {
                    wsSession.sendMessage(new TextMessage(jsonMessage));
                }
            }

        } catch (Exception e){
            System.err.println("--- FATAL ERROR IN broadcastLobbyUpdate ---");
            e.printStackTrace();
            System.err.println("-------------------------------------------");
        }
    }

    /**
     * Private helper method that broadcasts a session starting
     * @param lobby lobby of participants connected
     * @param sessionId id of session
     * @throws IOException
     */
    private void broadcastSessionStart(Map<String, WebSocketSession> lobby, Long sessionId) throws IOException {
        // This message basically says "move screens"
        String jsonMessage = "{\"type\": \"SESSION_START\"}";
        // Broadcast to each open websocket session
        for (WebSocketSession wsSession : lobby.values()) {
            if (wsSession.isOpen()) {
                wsSession.sendMessage(new TextMessage(jsonMessage));
                wsSession.close(); // close, since its moving to the next screen
            }
        }
    }

    /**
     * Helper method for getting the session Id
     * @param session websocket session
     * @return Long for sessionId
     */
    private Long getSessionIdFromUri(WebSocketSession session) {
        try {
            String path = session.getUri().getPath();
            String[] segments = path.split("/");
            // Example of what should be at each index: 0="", 1="ws", 2="lobby",3="12",4="5"
            if (segments.length == 5){
                return Long.parseLong(segments[3]); // sessionId (12)
            }
        } catch(Exception e) {
            System.out.println(e.getMessage());
            System.out.println("Cannot parse sessionId: "+session.getUri());
        }
        return null; // If length is not 5
    }


    /**
     * Helper method for getting the participant Id
     * @param session websocket session
     * @return Long for participantId
     */
    private Long getParticipantIdFromUri(WebSocketSession session) {
        try {
            String path = session.getUri().getPath();
            String[] segments = path.split("/");
            // Example of what should be at each index: 0="", 1="ws", 2="lobby",3="12",4="5"
            if (segments.length == 5){
                return Long.parseLong(segments[4]); // participantId (5)
            }
        } catch(Exception e) {
            System.out.println(e.getMessage());
            System.out.println("Cannot parse participantId: "+session.getUri());
        }
        return null; // If length is not 5
    }

    /**
     * Dto class for lobby updates. Can be inside of this class
     * because it isn't used anywhere else.
     * Holds the type of update, int for percent of participants ready, and a session dto
     */
    private static class LobbyUpdate {
        public String type;
        public SessionDTO session;
        public int percentReady;

        public LobbyUpdate(String type, SessionDTO session, int percentReady) {
            this.type = type;
            this.session = session;
            this.percentReady = percentReady;
        }
    }

}
