package com.ucto.backend.dto;

import java.util.List;

/**
 * Response DTO for BA chat history retrieval.
 */
public class BAChatHistoryResponse {
    private List<BAChatResponse> messages;
    private int currentRound;
    private boolean clarificationComplete;
    private boolean needsEscalation;

    public BAChatHistoryResponse() {}

    public List<BAChatResponse> getMessages() { return messages; }
    public void setMessages(List<BAChatResponse> messages) { this.messages = messages; }

    public int getCurrentRound() { return currentRound; }
    public void setCurrentRound(int currentRound) { this.currentRound = currentRound; }

    public boolean isClarificationComplete() { return clarificationComplete; }
    public void setClarificationComplete(boolean clarificationComplete) { this.clarificationComplete = clarificationComplete; }

    public boolean isNeedsEscalation() { return needsEscalation; }
    public void setNeedsEscalation(boolean needsEscalation) { this.needsEscalation = needsEscalation; }
}
