package com.ucto.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * BA Chat message entity for the BA-customer communication channel.
 * Stores user messages and BA responses for requirement clarification.
 * See docs/state_machines.md §3 (BA Clarification Loop) for state machine.
 * See docs/ucto_playbook.md for communication protocol rules.
 */
@Entity
@Table(name = "ba_chat_messages")
public class BAChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long projectId;

    @Column(nullable = false)
    private Long userId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String userMessage;

    @Column(columnDefinition = "TEXT")
    private String baResponse;

    /** Current clarification round for this message batch (1-3, 0 = not in clarification) */
    private int roundNumber;

    /** Classification: GREETING, REQUIREMENT, CLARIFICATION, DECISION, FINALIZATION */
    private String messageType;

    /** If BA identified ambiguities/decisions, they're documented here as JSON array */
    @Column(columnDefinition = "TEXT")
    private String decisionsJson;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUserMessage() { return userMessage; }
    public void setUserMessage(String userMessage) { this.userMessage = userMessage; }

    public String getBaResponse() { return baResponse; }
    public void setBaResponse(String baResponse) { this.baResponse = baResponse; }

    public int getRoundNumber() { return roundNumber; }
    public void setRoundNumber(int roundNumber) { this.roundNumber = roundNumber; }

    public String getMessageType() { return messageType; }
    public void setMessageType(String messageType) { this.messageType = messageType; }

    public String getDecisionsJson() { return decisionsJson; }
    public void setDecisionsJson(String decisionsJson) { this.decisionsJson = decisionsJson; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
