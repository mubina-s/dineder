package coms309.dineder.dto;

/**
 * Simple dto for creating a session
 * @author Mason Gliege
 */
public class CreateSessionRequest {
    /**
     * Id of host (users table)
     */
    private Long hostId;
    /**
     * Id of an existing session preference to create the session with
     */
    private Long preferenceId;
    public Long getHostId() {
        return hostId;
    }
    public void setHostId(Long hostId) {
        this.hostId = hostId;
    }
    public Long getPreferenceId() {
        return preferenceId;
    }
    public void setPreferenceId(Long preferenceId) {
        this.preferenceId = preferenceId;
    }
}
