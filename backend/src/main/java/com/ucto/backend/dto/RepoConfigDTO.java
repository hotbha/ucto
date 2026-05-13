package com.ucto.backend.dto;

/**
 * DTO for configuring a project's Git repository.
 * Matches the schema from docs/repo_aware_dev_agent_design.md.
 */
public class RepoConfigDTO {

    private Long projectId;
    private String repoUrl;
    private String repoProvider; // GITHUB, GITLAB, BITBUCKET, OTHER
    private String repoBranch;
    private String repoTokenRef;

    public RepoConfigDTO() {}

    public RepoConfigDTO(Long projectId, String repoUrl, String repoProvider,
                         String repoBranch, String repoTokenRef) {
        this.projectId = projectId;
        this.repoUrl = repoUrl;
        this.repoProvider = repoProvider;
        this.repoBranch = repoBranch;
        this.repoTokenRef = repoTokenRef;
    }

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public String getRepoUrl() { return repoUrl; }
    public void setRepoUrl(String repoUrl) { this.repoUrl = repoUrl; }

    public String getRepoProvider() { return repoProvider; }
    public void setRepoProvider(String repoProvider) { this.repoProvider = repoProvider; }

    public String getRepoBranch() { return repoBranch; }
    public void setRepoBranch(String repoBranch) { this.repoBranch = repoBranch; }

    public String getRepoTokenRef() { return repoTokenRef; }
    public void setRepoTokenRef(String repoTokenRef) { this.repoTokenRef = repoTokenRef; }
}
