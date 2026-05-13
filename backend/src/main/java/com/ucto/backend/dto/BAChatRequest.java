package com.ucto.backend.dto;

/**
 * Request DTO for sending a message to the BA chat.
 */
public class BAChatRequest {
    private Long projectId;
    private String message;

    public BAChatRequest() {}

    public BAChatRequest(Long projectId, String message) {
        this.projectId = projectId;
        this.message = message;
    }

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
