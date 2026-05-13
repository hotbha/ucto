package com.ucto.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for BA chat messages.
 */
public class BAChatResponse {
    private Long id;
    private String userMessage;
    private String baResponse;
    private int roundNumber;
    private String messageType;
    private String decisionsJson;
    private LocalDateTime createdAt;
    /** List of ambiguities/doubts BA identified, if any */
    private List<String> ambiguities;
    /** If BA identified decisions that need documentation */
    private List<String> decisions;
    /** Whether clarification is complete and requirements can be finalized */
    private boolean clarificationComplete;
    /** Whether escalation is needed (max 3 rounds reached) */
    private boolean needsEscalation;

    public BAChatResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public List<String> getAmbiguities() { return ambiguities; }
    public void setAmbiguities(List<String> ambiguities) { this.ambiguities = ambiguities; }

    public List<String> getDecisions() { return decisions; }
    public void setDecisions(List<String> decisions) { this.decisions = decisions; }

    public boolean isClarificationComplete() { return clarificationComplete; }
    public void setClarificationComplete(boolean clarificationComplete) { this.clarificationComplete = clarificationComplete; }

    public boolean isNeedsEscalation() { return needsEscalation; }
    public void setNeedsEscalation(boolean needsEscalation) { this.needsEscalation = needsEscalation; }
}
