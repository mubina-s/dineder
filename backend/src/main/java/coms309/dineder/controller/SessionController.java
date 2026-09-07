package coms309.dineder.controller;

import coms309.dineder.dto.*;
import coms309.dineder.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import coms309.dineder.entity.*;
import coms309.dineder.service.*;
import java.util.List;
import java.util.Map;

/**
 * Endpoints for session, the heart of the app. Allows for getting all sessions, session by id, session by
 * join code, joining session, starting session
 * @author Mason Gliege
 */
@RestController
public class SessionController {

    @Autowired
    private SessionService sessionService;

    // TODO: what other repos?

    /**
     * Gets all sessions stored in the database. Not practical in application, but useful in testing
     * @return return a list of session dtos. Each dto includes id, name, code, start time, host name, host id, booleans for session status, and dtos for participants and restaurants in the session
     */
    @GetMapping(path = "/sessions")
    public List<SessionDTO> findAll() {
        return sessionService.getAllSessionsDTO();
    }

    /**
     * Get an existing session stored in the database by its id, if its found
     * @param id id of a session to search by. A session is created with an id automatically once a host creates one
     * @return A session dto that exists, if found by the id. Response status exception if not found. A dto includes id, name, code, start time, host name, host id, booleans for session status, and dtos for participants and restaurants in the session
     */
    @GetMapping(path = "/sessions/id/{id}")
    public SessionDTO findSessionById(@PathVariable Long id) {
        // returns a dto of a session
        return sessionService.getSessionByIdDTO(id);
    }

    /**
     * Get an existing session stored in the database by its join code, if its found
     * @param code join code of a session to search by. Always 4 characters all caps. A session is created with an id automatically once a host creates one
     * @return A session dto that exists, if found by the code. Response status exception if not found. A dto includes id, name, code, start time, host name, host id, booleans for session status, and dtos for participants and restaurants in the session
     */
    @GetMapping(path = "/sessions/{code}")
    public SessionDTO findSessionByCode(@PathVariable String code) {
        return sessionService.getSessionByCodeDTO(code);
    }

    /**
     * Create new session. This is used only when a host user creates a new session. Uses the existing session preference as a filter for restaurants, and makes the host the user who created it. Creates a session participant for the host automatically, and adds the appropriate restaurants
     * @param request create session request, which contains fields preferenceId and hostId (from users table).
     * @return a session DTO object. Response status exception if user or preference is not found. A dto includes id, name, code, start time, host name, host id, booleans for session status, and dtos for participants and restaurants in the session
     */
    @PostMapping(path = "/sessions")
    public SessionDTO createSession(@RequestBody CreateSessionRequest request) {
        return sessionService.createSession(request.getHostId(), request.getPreferenceId());
    }

    /**
     * Allows a user to join the session from a join code. Creates a new session participant and adds it to the session.
     * @param request JoinSessionRequest, needs the id of a user and the join code of the existing session
     * @return A session participant dto (newly created). Response status exception if no existing user found, no session by that join code, or the participant is already in that session. Also broadcasts a lobby update via websocket. The dto includes participant id, user id, name, and booleans for ready and finished swiping
     */
    @PostMapping(path = "/sessions/join")
    public SessionParticipantDTO joinSession(@RequestBody JoinSessionRequest request) {
        return sessionService.joinSession(request);
    }

    /**
     * Allows the host to manually start a session, overrides the 100% ready requirement. Not used currently, as this normally happens via websocket. Useful for testing or backup in case of websocket failure
     * @param sessionId id of the session (must exist)
     * @param request request body, which must contain "hostId"
     * @return String indicating success or failure
     */
    @PostMapping(path = "/sessions/{sessionId}/start")
    public String startSession(@PathVariable Long sessionId, @RequestBody Map<String, Long> request) {
        Long hostId = (Long) request.get("hostId");
        if (hostId == null) {
            return "Failed to start session, host id not found";
        }
        sessionService.startSession(hostId, sessionId);
        return "Session "+sessionId+" started.";

    }
}
