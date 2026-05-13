package com.ucto.backend.dto;

/**
 * Response DTO for the POST /api/projects/bootstrap endpoint.
 * Contains the created project details and generated structure summary.
 */
public class BootstrapResultDTO {

    private Long projectId;
    private String projectTitle;
    private String targetStack;
    private String workspacePath;
    private int backendFiles;
    private int frontendFiles;
    private String status;
    private String message;

    public BootstrapResultDTO() {}

    public BootstrapResultDTO(Long projectId, String projectTitle, String targetStack,
                              String workspacePath, int backendFiles, int frontendFiles,
                              String status, String message) {
        this.projectId = projectId;
        this.projectTitle = projectTitle;
        this.targetStack = targetStack;
        this.workspacePath = workspacePath;
        this.backendFiles = backendFiles;
        this.frontendFiles = frontendFiles;
        this.status = status;
        this.message = message;
    }

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public String getProjectTitle() { return projectTitle; }
    public void setProjectTitle(String projectTitle) { this.projectTitle = projectTitle; }

    public String getTargetStack() { return targetStack; }
    public void setTargetStack(String targetStack) { this.targetStack = targetStack; }

    public String getWorkspacePath() { return workspacePath; }
    public void setWorkspacePath(String workspacePath) { this.workspacePath = workspacePath; }

    public int getBackendFiles() { return backendFiles; }
    public void setBackendFiles(int backendFiles) { this.backendFiles = backendFiles; }

    public int getFrontendFiles() { return frontendFiles; }
    public void setFrontendFiles(int frontendFiles) { this.frontendFiles = frontendFiles; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
