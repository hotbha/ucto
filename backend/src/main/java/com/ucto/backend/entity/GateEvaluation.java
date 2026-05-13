package com.ucto.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Records each quality gate evaluation for audit and traceability.
 * See docs/quality_gates_and_simulation_design.md.
 */
@Entity
@Table(name = "gate_evaluations")
public class GateEvaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long projectId;

    @Column(nullable = false)
    private String gateType; // TEST, COMPLIANCE, COORDINATED

    @Column(nullable = false)
    private boolean passed;

    private Long testResultId;
    private Long complianceResultId;

    @Column(nullable = false)
    private String correlationId;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(length = 128)
    private String branch;

    private boolean simulation;

    private LocalDateTime evaluatedAt;

    @PrePersist
    protected void onCreate() {
        evaluatedAt = LocalDateTime.now();
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public String getGateType() { return gateType; }
    public void setGateType(String gateType) { this.gateType = gateType; }

    public boolean isPassed() { return passed; }
    public void setPassed(boolean passed) { this.passed = passed; }

    public Long getTestResultId() { return testResultId; }
    public void setTestResultId(Long testResultId) { this.testResultId = testResultId; }

    public Long getComplianceResultId() { return complianceResultId; }
    public void setComplianceResultId(Long complianceResultId) { this.complianceResultId = complianceResultId; }

    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }

    public boolean isSimulation() { return simulation; }
    public void setSimulation(boolean simulation) { this.simulation = simulation; }

    public LocalDateTime getEvaluatedAt() { return evaluatedAt; }
    public void setEvaluatedAt(LocalDateTime evaluatedAt) { this.evaluatedAt = evaluatedAt; }
}
