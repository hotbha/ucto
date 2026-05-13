package com.ucto.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Stores compliance check results produced by the compliance agent.
 * See docs/quality_gates_and_simulation_design.md.
 */
@Entity
@Table(name = "compliance_results")
public class ComplianceResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long projectId;

    @Column(nullable = false)
    private Long agentRunId;

    @Column(columnDefinition = "TEXT")
    private String checksPassedJson;

    @Column(columnDefinition = "TEXT")
    private String checksFailedJson;

    @Column(nullable = false)
    private String overallStatus; // PASS, PASS_WITH_WARNINGS, FAIL

    @Column(nullable = false)
    private String severity; // LOW, MEDIUM, HIGH, CRITICAL

    private String reportUrl;

    @Column(nullable = false)
    private String correlationId;

    @Column(length = 128)
    private String branch;

    private boolean simulation;

    private LocalDateTime createdAt;
    private LocalDateTime evaluatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public Long getAgentRunId() { return agentRunId; }
    public void setAgentRunId(Long agentRunId) { this.agentRunId = agentRunId; }

    public String getChecksPassedJson() { return checksPassedJson; }
    public void setChecksPassedJson(String checksPassedJson) { this.checksPassedJson = checksPassedJson; }

    public String getChecksFailedJson() { return checksFailedJson; }
    public void setChecksFailedJson(String checksFailedJson) { this.checksFailedJson = checksFailedJson; }

    public String getOverallStatus() { return overallStatus; }
    public void setOverallStatus(String overallStatus) { this.overallStatus = overallStatus; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getReportUrl() { return reportUrl; }
    public void setReportUrl(String reportUrl) { this.reportUrl = reportUrl; }

    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }

    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }

    public boolean isSimulation() { return simulation; }
    public void setSimulation(boolean simulation) { this.simulation = simulation; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getEvaluatedAt() { return evaluatedAt; }
    public void setEvaluatedAt(LocalDateTime evaluatedAt) { this.evaluatedAt = evaluatedAt; }
}
