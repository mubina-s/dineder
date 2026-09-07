package coms309.dineder.dto;

import java.util.List;
import java.util.Map;

public class UserHistoryDTO {
    private Long userId;
    private List<Map<String, Object>> sessions; // Each map contains session info & selected restaurant

    public UserHistoryDTO(Long userId, List<Map<String, Object>> sessions) {
        this.userId = userId;
        this.sessions = sessions;
    }

    public Long getUserId() { return userId; }
    public List<Map<String, Object>> getSessions() { return sessions; }

    public void setUserId(Long userId) { this.userId = userId; }
    public void setSessions(List<Map<String, Object>> sessions) { this.sessions = sessions; }
}
