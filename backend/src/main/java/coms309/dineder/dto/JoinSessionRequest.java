package coms309.dineder.dto;

/**
 * Simple dto for joining a session
 * @author Mason Gliege
 */
public class JoinSessionRequest {
    /**
     * User id to join by (creates a participant from this id)
     */
    private Long userId;
    /**
     * 4 character String join code (unique per each session)
     */
    private String joinCode;

    public Long getUserId() {
        return userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getJoinCode() {
        return joinCode;
    }

    public void setJoinCode(String joinCode) {
        this.joinCode = joinCode;
    }

    public void getJoinCode(String joinCode) {
        this.joinCode = joinCode;
    }
}
