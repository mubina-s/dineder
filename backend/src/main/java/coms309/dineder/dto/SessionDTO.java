package coms309.dineder.dto;

import coms309.dineder.entity.SessionParticipant;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Dto for session. this is likely the largest and most complex dto so far.
 * Has all session attributes. Also contains dto lists for session participants and restaurants.
 * The restaurants will also contain cuisine dtos.
 * Useful for http and websocket response, sending session object causes many problems
 * @author Mason Gliege
 */
public class SessionDTO {
    /**
     * Database id for a session (unique)
     */
    private Long id;
    /**
     * Name of session.
     * Not currently used, but may be used in the future
     */
    private String sessionName;
    /**
     * 4 character string of all capital letters (unique).
     * Used for regular users to join a session
     */
    private String joinCode;
    /**
     * Boolean indicating a sessions state
     * true means users can join
     * false means the swiping has been initiated, users cannot join
     */
    private boolean isLive;
    /**
     * Instant of when the session was created
     */
    private Instant startTime;
    /**
     * name of the host
     */
    private String hostname; // host name (name in users)
    /**
     * id of the host (to users)
     */
    private Long hostId;
    /**
     * Changes from false to true when all participants are done swiping
     */
    private Boolean swipingComplete;
    /**
     * List of participants (as DTOS) within a session
     */
    private List<SessionParticipantDTO> participantsDTOSet = new ArrayList<>();
    /**
     * List of restaurants in a session, ordered by id (to appear in same order for all participants)
     * (DTOs)
     */
    private List<RestaurantDTO> restaurantDTOSet = new ArrayList<>();

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getSessionName() {
        return sessionName;
    }

    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
    }

    public String getJoinCode() {
        return joinCode;
    }

    public void setJoinCode(String joinCode) {
        this.joinCode = joinCode;
    }

    public boolean isLive() {
        return isLive;
    }

    public void setLive(boolean live) {
        isLive = live;
    }

    public Long getHostId() {
        return hostId;
    }

    public void setHostId(Long hostId) {
        this.hostId = hostId;
    }

    public Boolean isSwipingComplete() {
        return swipingComplete;
    }

    public void setSwipingComplete(Boolean swipingComplete) {
        this.swipingComplete = swipingComplete;
    }

    public Instant getStartTime() {
        return startTime;
    }
    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }

    public String getHostname() {
        return hostname;
    }
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }
    public List<SessionParticipantDTO> getParticipantsDTOList() {
        return participantsDTOSet;
    }

    public void setParticipantsDTOList(List<SessionParticipantDTO> participantsDTOSet) {
        this.participantsDTOSet = participantsDTOSet;
    }

    public List<RestaurantDTO> getRestaurantDTOList() {
        return restaurantDTOSet;
    }

    public void setRestaurantDTOList(List<RestaurantDTO> restaurantDTOSet) {
        this.restaurantDTOSet = restaurantDTOSet;
    }
}
