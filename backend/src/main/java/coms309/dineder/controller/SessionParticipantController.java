package coms309.dineder.controller;

import coms309.dineder.dto.*;
import coms309.dineder.service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for session participants. Allows for readying and unreadying (these are also controlled
 * by websocket, so ready/unready is more for testing), deleting (host kicks participant from
 * session), and marking finished swiping. No "create" here because participants are only
 * created upon joining a session
 * @author Mason Gliege
 *
 */
@RestController
public class SessionParticipantController {
    @Autowired
    private SessionService sessionService;

    /**
     * Set an existing session participant as ready. Not currently used here, usually controlled via websocket. Updates the database, and also the sessions that participant is linked to. Does a "check and start," which checks if all participants are ready and starts the session if so. Sends out updates to the connected websockets.
     * @param participantId id of the participant to mark ready
     * @return dto of participant in the session. The dto includes participant id, user id, name, and booleans for ready and finished swiping
     */
    @PutMapping("/participants/{participantId}/ready")
    public SessionParticipantDTO setReady(@PathVariable Long participantId) {
        return sessionService.setParticipantReady(participantId, true);
    }

    /**
     * Set an existing session participant not ready. Not currently used here, usually controlled via websocket. Updates the database, and also the sessions that participant is linked to. Does a "check and start," which checks if all participants are ready and starts the session if so. Sends out updates to the connected websockets.
     * @param participantId id of the participant to mark not ready
     * @return dto of participant in the session. The dto includes participant id, user id, name, and booleans for ready and finished swiping
     */
    @PutMapping("/participants/{participantId}/unready")
    public SessionParticipantDTO setNotReady(@PathVariable Long participantId) {
        return sessionService.setParticipantReady(participantId, false);
    }

    /**
     * Delete a participant by its id and host id, essentially kicking them from the session. Currently not used, usually controlled via websocket from the host. This will be used as backup. Broadcasts a lobby update upon kicking the participant via websocket to the other session members.
     * @param participantId id of the participant to search by
     * @param hostId id of the host to search by
     * @return A string indicating the success of kicking the participant. Response status exception if either of the ids were not found in the database
     */
    @DeleteMapping("/participants/{participantId}/{hostId}")
    public String kick(@PathVariable Long participantId, @PathVariable Long hostId) {
        sessionService.kick(hostId, participantId);
        return "Successfully kicked participant "+participantId;
    }

    /**
     * Mark a participant's "isFinishedSwiping" boolean as true when they are done swiping
     * @param participantId participant id to mark as finished swiping
     * @return DTO of updated participant. The dto includes participant id, user id, name, and booleans for ready and finished swiping
     */
    @PostMapping("/participants/{participantId}/finish-swiping")
    public SessionParticipantDTO finishSwiping(@PathVariable Long participantId) {
        return sessionService.setParticipantFinishedSwiping(participantId, true);
    }



}
