package com.ucto.backend.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

/**
 * Represents a sprint in the PM/Scrum Master workflow.
 * Tracks the lifecycle of iterations aligned with closed-loop workflows.
 */
@Entity
@Table(name = "sprints")
public class Sprint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long projectId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    /** Planning, Active, InReview, Closed */
    @Column(nullable = false)
    private String status;

    /** Active loop: DISCOVERY, BUILD, RISK, UX_DOC, IDLE */
    private String activeLoop;

    private int totalStoryPoints;
    private int completedStoryPoints;

    @Column(columnDefinition = "TEXT")
    private String goalDescription;

    @Column(columnDefinition = "TEXT")
    private String retrospectiveNotes;

    @Column(nullable = false)
    private Long createdBy;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = "Planning";
        if (activeLoop == null) activeLoop = "IDLE";
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getActiveLoop() { return activeLoop; }
    public void setActiveLoop(String activeLoop) { this.activeLoop = activeLoop; }

    public int getTotalStoryPoints() { return totalStoryPoints; }
    public void setTotalStoryPoints(int totalStoryPoints) { this.totalStoryPoints = totalStoryPoints; }

    public int getCompletedStoryPoints() { return completedStoryPoints; }
    public void setCompletedStoryPoints(int completedStoryPoints) { this.completedStoryPoints = completedStoryPoints; }

    public String getGoalDescription() { return goalDescription; }
    public void setGoalDescription(String goalDescription) { this.goalDescription = goalDescription; }

    public String getRetrospectiveNotes() { return retrospectiveNotes; }
    public void setRetrospectiveNotes(String retrospectiveNotes) { this.retrospectiveNotes = retrospectiveNotes; }

    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
