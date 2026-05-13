package com.ucto.backend.entity;

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
 * Represents a backlog item (epic, story, or task) in the product/sprint backlog.
 * Follows the story state machine from docs/ucto_playbook.md:
 * New → In Discovery → Ready → In Progress → In Review → Done
 */
@Entity
@Table(name = "backlog_items")
public class BacklogItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long projectId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    /** EPIC, STORY, TASK */
    @Column(nullable = false)
    private String itemType;

    /** New, InDiscovery, Ready, InProgress, InReview, Done, Blocked */
    @Column(nullable = false)
    private String status;

    /** Reference to parent epic/story */
    private Long parentId;

    /** User persona for user stories (Founder, Developer, Viewer) */
    private String persona;

    /** As a [persona], I want [goal] so that [benefit] */
    private String userStoryFormat;

    @Column(columnDefinition = "TEXT")
    private String acceptanceCriteriaJson;

    @Column(columnDefinition = "TEXT")
    private String constraintsJson;

    @Column(columnDefinition = "TEXT")
    private String dependenciesJson;

    @Column(nullable = false)
    private Long createdBy;

    /** Sprint ID if assigned to a sprint */
    private Long sprintId;

    private int priority; // 1=highest

    private int storyPoints;

    /** DoR validation status */
    private boolean dorPassed;

    /** DoD validation status */
    private boolean dodPassed;

    @Column(columnDefinition = "TEXT")
    private String dorChecklistJson;

    @Column(columnDefinition = "TEXT")
    private String dodChecklistJson;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = "New";
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

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getItemType() { return itemType; }
    public void setItemType(String itemType) { this.itemType = itemType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }

    public String getPersona() { return persona; }
    public void setPersona(String persona) { this.persona = persona; }

    public String getUserStoryFormat() { return userStoryFormat; }
    public void setUserStoryFormat(String userStoryFormat) { this.userStoryFormat = userStoryFormat; }

    public String getAcceptanceCriteriaJson() { return acceptanceCriteriaJson; }
    public void setAcceptanceCriteriaJson(String acceptanceCriteriaJson) { this.acceptanceCriteriaJson = acceptanceCriteriaJson; }

    public String getConstraintsJson() { return constraintsJson; }
    public void setConstraintsJson(String constraintsJson) { this.constraintsJson = constraintsJson; }

    public String getDependenciesJson() { return dependenciesJson; }
    public void setDependenciesJson(String dependenciesJson) { this.dependenciesJson = dependenciesJson; }

    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }

    public Long getSprintId() { return sprintId; }
    public void setSprintId(Long sprintId) { this.sprintId = sprintId; }

    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }

    public int getStoryPoints() { return storyPoints; }
    public void setStoryPoints(int storyPoints) { this.storyPoints = storyPoints; }

    public boolean isDorPassed() { return dorPassed; }
    public void setDorPassed(boolean dorPassed) { this.dorPassed = dorPassed; }

    public boolean isDodPassed() { return dodPassed; }
    public void setDodPassed(boolean dodPassed) { this.dodPassed = dodPassed; }

    public String getDorChecklistJson() { return dorChecklistJson; }
    public void setDorChecklistJson(String dorChecklistJson) { this.dorChecklistJson = dorChecklistJson; }

    public String getDodChecklistJson() { return dodChecklistJson; }
    public void setDodChecklistJson(String dodChecklistJson) { this.dodChecklistJson = dodChecklistJson; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
