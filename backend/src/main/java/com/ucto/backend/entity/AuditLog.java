package com.ucto.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    private Long projectId;

    @Column(nullable = false)
    private String action; // LOGIN, REGISTER, OAUTH_LOGIN, PROJECT_CREATE, REQUIREMENT_UPDATE, AGENT_TRIGGER, SCREEN_APPROVE, etc.

    @Column(columnDefinition = "TEXT")
    private String details; // JSON payload with request details

    private String ipAddress;
    private boolean success;

    private boolean simulation;

    @Column(nullable = false)
    private LocalDateTime timestamp;


    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public boolean isSimulation() { return simulation; }
    public void setSimulation(boolean simulation) { this.simulation = simulation; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
