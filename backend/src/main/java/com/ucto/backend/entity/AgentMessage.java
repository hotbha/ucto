package com.ucto.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Structured message model for agent-to-agent communication.
 * Follows the standard message envelope defined in docs/message_structure.md.
 * Replaces the old ad-hoc BA Chat message format.
 */
@Entity
@Table(name = "agent_messages")
public class AgentMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fromAgent;

    @Column(nullable = false)
    private String toAgent;

    @Column(nullable = false)
    private String messageType; // REQUIREMENTS_PACKAGE, ARCHITECTURE_SPEC, UI_SPEC, etc.

    @Column(nullable = false)
    private Long storyId;

    @Column(nullable = false)
    private Long projectId;

    @Column(nullable = false)
    private String correlationId;

    @Column(columnDefinition = "TEXT")
    private String payloadJson; // Structured JSON payload per message_type schema

    @Column(nullable = false)
    private boolean needsHuman;

    @Column(columnDefinition = "TEXT")
    private String humanQuestionsJson; // JSON array of questions for PO

    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
    private String status; // PENDING, ROUTED, RESOLVED, ERROR

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) status = "PENDING";
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFromAgent() { return fromAgent; }
    public void setFromAgent(String fromAgent) { this.fromAgent = fromAgent; }

    public String getToAgent() { return toAgent; }
    public void setToAgent(String toAgent) { this.toAgent = toAgent; }

    public String getMessageType() { return messageType; }
    public void setMessageType(String messageType) { this.messageType = messageType; }

    public Long getStoryId() { return storyId; }
    public void setStoryId(Long storyId) { this.storyId = storyId; }

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }

    public String getPayloadJson() { return payloadJson; }
    public void setPayloadJson(String payloadJson) { this.payloadJson = payloadJson; }

    public boolean isNeedsHuman() { return needsHuman; }
    public void setNeedsHuman(boolean needsHuman) { this.needsHuman = needsHuman; }

    public String getHumanQuestionsJson() { return humanQuestionsJson; }
    public void setHumanQuestionsJson(String humanQuestionsJson) { this.humanQuestionsJson = humanQuestionsJson; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}