package com.ucto.backend.dto;

import java.util.List;
import java.util.Map;

/**
 * Request DTO for PM/Scrum Master operations.
 */
public class PmRequest {
    private Long projectId;
    private String action; // CREATE_SPRINT, ADD_BACKLOG_ITEM, UPDATE_STATUS, RUN_LOOP

    // Sprint fields
    private String sprintName;
    private String startDate;
    private String endDate;
    private String goalDescription;

    // Backlog item fields
    private String title;
    private String description;
    private String itemType; // EPIC, STORY, TASK
    private String persona;
    private String userStoryFormat;
    private List<String> acceptanceCriteria;
    private List<String> constraints;
    private List<String> dependencies;
    private int priority;
    private int storyPoints;
    private Long parentId;
    private Long sprintId;

    // Status update
    private String newStatus;

    // Loop control
    private String loopType; // DISCOVERY, BUILD, RISK, UX_DOC

    public PmRequest() {}

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getSprintName() { return sprintName; }
    public void setSprintName(String sprintName) { this.sprintName = sprintName; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public String getGoalDescription() { return goalDescription; }
    public void setGoalDescription(String goalDescription) { this.goalDescription = goalDescription; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getItemType() { return itemType; }
    public void setItemType(String itemType) { this.itemType = itemType; }

    public String getPersona() { return persona; }
    public void setPersona(String persona) { this.persona = persona; }

    public String getUserStoryFormat() { return userStoryFormat; }
    public void setUserStoryFormat(String userStoryFormat) { this.userStoryFormat = userStoryFormat; }

    public List<String> getAcceptanceCriteria() { return acceptanceCriteria; }
    public void setAcceptanceCriteria(List<String> acceptanceCriteria) { this.acceptanceCriteria = acceptanceCriteria; }

    public List<String> getConstraints() { return constraints; }
    public void setConstraints(List<String> constraints) { this.constraints = constraints; }

    public List<String> getDependencies() { return dependencies; }
    public void setDependencies(List<String> dependencies) { this.dependencies = dependencies; }

    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }

    public int getStoryPoints() { return storyPoints; }
    public void setStoryPoints(int storyPoints) { this.storyPoints = storyPoints; }

    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }

    public Long getSprintId() { return sprintId; }
    public void setSprintId(Long sprintId) { this.sprintId = sprintId; }

    public String getNewStatus() { return newStatus; }
    public void setNewStatus(String newStatus) { this.newStatus = newStatus; }

    public String getLoopType() { return loopType; }
    public void setLoopType(String loopType) { this.loopType = loopType; }
}
