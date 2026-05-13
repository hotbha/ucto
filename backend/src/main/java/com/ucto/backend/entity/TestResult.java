package com.ucto.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Stores test results produced by the tester agent.
 * See docs/quality_gates_and_simulation_design.md.
 */
@Entity
@Table(name = "test_results")
public class TestResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long projectId;

    @Column(nullable = false)
    private Long agentRunId;

    private String storyId;

    @Column(nullable = false)
    private int testsRun;

    @Column(nullable = false)
    private int testsPassed;

    @Column(nullable = false)
    private int testsFailed;

    private int testsSkipped;

    @Column(nullable = false)
    private double coveragePercent; // 0.0 – 100.0

    @Column(columnDefinition = "TEXT")
    private String failuresJson;

    @Column(nullable = false)
    private String status; // PASSED, FAILED, INCOMPLETE, ERROR

    @Column(nullable = false)
    private String correlationId;

    @Column(length = 128)
    private String branch; // Branch these tests were run against

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

    public String getStoryId() { return storyId; }
    public void setStoryId(String storyId) { this.storyId = storyId; }

    public int getTestsRun() { return testsRun; }
    public void setTestsRun(int testsRun) { this.testsRun = testsRun; }

    public int getTestsPassed() { return testsPassed; }
    public void setTestsPassed(int testsPassed) { this.testsPassed = testsPassed; }

    public int getTestsFailed() { return testsFailed; }
    public void setTestsFailed(int testsFailed) { this.testsFailed = testsFailed; }

    public int getTestsSkipped() { return testsSkipped; }
    public void setTestsSkipped(int testsSkipped) { this.testsSkipped = testsSkipped; }

    public double getCoveragePercent() { return coveragePercent; }
    public void setCoveragePercent(double coveragePercent) { this.coveragePercent = coveragePercent; }

    public String getFailuresJson() { return failuresJson; }
    public void setFailuresJson(String failuresJson) { this.failuresJson = failuresJson; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

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
