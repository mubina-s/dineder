package coms309.dineder.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "admin_logs")
public class AdminLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // id of the log

    @Column(nullable = false)
    private Long adminId; // the id of the admin (user) performing an action

    @Column(nullable = false)
    private String actionType; //DELETE_RESTAURANT or UPDATE_RESTAURANT

    @Column(nullable = false)
    private String description; // TODO javadoc comment

    @Column(nullable = false)
    private Instant timestamp;

    public AdminLog() {}

    public AdminLog(Long adminId, String actionType, String description) {
        this.adminId = adminId;
        this.actionType = actionType;
        this.description = description;
        this.timestamp = Instant.now();
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Long getAdminId() {
        return adminId;
    }

    public void setAdminId(Long adminId) {
        this.adminId = adminId;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Instant getTimestamp() {
        return timestamp;
    }


    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
