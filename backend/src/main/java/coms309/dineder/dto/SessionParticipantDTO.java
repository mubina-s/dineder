package coms309.dineder.dto;

/**
 * DTO for a session participant (a user in a session)
 * @author Mason Gliege
 */
public class SessionParticipantDTO {
    /**
     * Id of a session participant within this table
     */
    private Long participantId;
    /**
     * Foreign key id to the users table
     */
    private Long userId;
    /**
     * Name of the participant
     */
    private String name;
    /**
     * "Ready" status for readying up screen
     * False (not ready) by default
     */
    private boolean isReady;
    /**
     * Boolean for if a participant is done swiping
     * False by default, will need to be set by controller after last "swipe"
     */
    private boolean isFinishedSwiping;

    public Long getParticipantId() {
        return participantId;
    }
    public void setParticipantId(Long participantId) {
        this.participantId = participantId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isReady() {
        return isReady;
    }

    public void setReady(boolean ready) {
        isReady = ready;
    }

    public boolean isFinishedSwiping() {
        return isFinishedSwiping;
    }

    public void setFinishedSwiping(boolean finishedSwiping) {
        isFinishedSwiping = finishedSwiping;
    }

}
