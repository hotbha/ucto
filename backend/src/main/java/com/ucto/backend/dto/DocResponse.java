package com.ucto.backend.dto;

import java.util.List;

/**
 * Response DTO for Documentation Agent operations.
 */
public class DocResponse {
    private boolean success;
    private String message;
    private Object data;
    private List<String> warnings;

    public DocResponse() {}

    public DocResponse(boolean success, String message) {
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
}
