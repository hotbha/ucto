package com.ucto.backend.dto;

import java.util.List;
import java.util.Map;

/**
 * Response DTO for Orchestrator operations.
 */
public class OrchestratorResponse {
    private boolean success;
    private String message;
    private String recommendedLoop;
    private String recommendedNextAgent;
    private List<Map<String, Object>> pendingHumanQuestions;
    private Map<String, Object> loopStatus;
    private Object data;

    public OrchestratorResponse() {}

    public OrchestratorResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getRecommendedLoop() { return recommendedLoop; }
    public void setRecommendedLoop(String recommendedLoop) { this.recommendedLoop = recommendedLoop; }

    public String getRecommendedNextAgent() { return recommendedNextAgent; }
    public void setRecommendedNextAgent(String recommendedNextAgent) { this.recommendedNextAgent = recommendedNextAgent; }

    public List<Map<String, Object>> getPendingHumanQuestions() { return pendingHumanQuestions; }
    public void setPendingHumanQuestions(List<Map<String, Object>> pendingHumanQuestions) { this.pendingHumanQuestions = pendingHumanQuestions; }

    public Map<String, Object> getLoopStatus() { return loopStatus; }
    public void setLoopStatus(Map<String, Object> loopStatus) { this.loopStatus = loopStatus; }

    public Object getData() { return data; }
    public void setData(Object data) { this.data = data; }
}
