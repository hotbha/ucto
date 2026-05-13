package com.ucto.backend.dto;

import java.util.List;
import java.util.Map;

/**
 * Response DTO for PM/Scrum Master operations.
 */
public class PmResponse {
    private boolean success;
    private String message;
    private Object data; // Flexible payload for different actions
    private List<String> warnings;
    private Map<String, Object> metrics; // Sprint velocity, backlog counts, etc.

    public PmResponse() {}

    public PmResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Object getData() { return data; }
    public void setData(Object data) { this.data = data; }

    public List<String> getWarnings() { return warnings; }
    public void setWarnings(List<String> warnings) { this.warnings = warnings; }

    public Map<String, Object> getMetrics() { return metrics; }
    public void setMetrics(Map<String, Object> metrics) { this.metrics = metrics; }
}
