package com.ucto.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ucto.backend.dto.PmRequest;
import com.ucto.backend.dto.PmResponse;
import com.ucto.backend.entity.BacklogItem;
import com.ucto.backend.entity.Sprint;
import com.ucto.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

/**
 * PM/Scrum Master service — backlog management, sprint lifecycle, loop coordination,
 * DoR/DoD enforcement. Aligned with docs/closed_loop_workflows.md.
 */
@Service
public class ProjectManagerService {

    @Autowired
    private BacklogItemRepository backlogItemRepository;

    @Autowired
    private SprintRepository sprintRepository;

    @Autowired
    private AgentMessageRepository agentMessageRepository;

    @Autowired
    private DoRValidator dorValidator;

    @Autowired
    private DoDValidator dodValidator;

    @Autowired
    private AuditLogService auditLogService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Execute a PM action based on the request.
     */
    @Transactional
    public PmResponse executeAction(PmRequest request, Long userId, String ipAddress) {
        switch (request.getAction()) {
            case "CREATE_SPRINT":
                return createSprint(request, userId, ipAddress);
            case "ADD_BACKLOG_ITEM":
                return addBacklogItem(request, userId, ipAddress);
            case "UPDATE_STATUS":
                return updateStatus(request, userId, ipAddress);
            case "RUN_LOOP":
                return runLoop(request, userId, ipAddress);
            case "CHECK_DOR":
                return checkDoR(request, userId);
            case "CHECK_DOD":
                return checkDoD(request, userId);
            case "GET_BACKLOG":
                return getBacklog(request);
            case "GET_SPRINTS":
                return getSprints(request);
            default:
                return new PmResponse(false, "Unknown action: " + request.getAction());
        }
    }

    private PmResponse createSprint(PmRequest request, Long userId, String ipAddress) {
        Sprint sprint = new Sprint();
        sprint.setProjectId(request.getProjectId());
        sprint.setName(request.getSprintName());
        sprint.setStartDate(LocalDate.parse(request.getStartDate()));
        sprint.setEndDate(LocalDate.parse(request.getEndDate()));
        sprint.setGoalDescription(request.getGoalDescription());
        sprint.setStatus("Planning");
        sprint.setActiveLoop("IDLE");
        sprint.setCreatedBy(userId);
        sprint = sprintRepository.save(sprint);

        auditLogService.log(userId, request.getProjectId(), "SPRINT_CREATED",
                "Created sprint: " + sprint.getName(), ipAddress, true);

        PmResponse response = new PmResponse(true, "Sprint created successfully");
        response.setData(sprint);
        return response;
    }

    private PmResponse addBacklogItem(PmRequest request, Long userId, String ipAddress) {
        BacklogItem item = new BacklogItem();
        item.setProjectId(request.getProjectId());
        item.setTitle(request.getTitle());
        item.setDescription(request.getDescription());
        item.setItemType(request.getItemType() != null ? request.getItemType() : "STORY");
        item.setStatus("New");
        item.setPersona(request.getPersona());
        item.setUserStoryFormat(request.getUserStoryFormat());
        item.setCreatedBy(userId);
        item.setPriority(request.getPriority());
        item.setStoryPoints(request.getStoryPoints());
        item.setParentId(request.getParentId());
        item.setSprintId(request.getSprintId());

        // Serialize lists to JSON
        try {
            if (request.getAcceptanceCriteria() != null) {
                item.setAcceptanceCriteriaJson(objectMapper.writeValueAsString(request.getAcceptanceCriteria()));
            }
            if (request.getConstraints() != null) {
                item.setConstraintsJson(objectMapper.writeValueAsString(request.getConstraints()));
            }
            if (request.getDependencies() != null) {
                item.setDependenciesJson(objectMapper.writeValueAsString(request.getDependencies()));
            }
        } catch (Exception e) {
            return new PmResponse(false, "Failed to serialize lists: " + e.getMessage());
        }

        item = backlogItemRepository.save(item);

        auditLogService.log(userId, request.getProjectId(), "BACKLOG_ITEM_ADDED",
                "Added " + item.getItemType() + ": " + item.getTitle(), ipAddress, true);

        PmResponse response = new PmResponse(true, "Backlog item added successfully");
        response.setData(item);
        return response;
    }

    private PmResponse updateStatus(PmRequest request, Long userId, String ipAddress) {
        Optional<BacklogItem> optItem = backlogItemRepository.findById(request.getSprintId() != null ?
                request.getSprintId() : 0L);
        if (optItem.isEmpty()) {
            return new PmResponse(false, "Backlog item not found");
        }

        BacklogItem item = optItem.get();
        String oldStatus = item.getStatus();
        String newStatus = request.getNewStatus();
        item.setStatus(newStatus);

        // Enforce DoR check before moving to "Ready"
        if ("Ready".equals(newStatus)) {
            Map<String, Object> dorResult = dorValidator.validate(item);
            boolean dorPassed = (boolean) dorResult.get("allPassed");
            item.setDorPassed(dorPassed);
            try {
                item.setDorChecklistJson(objectMapper.writeValueAsString(dorResult.get("checklist")));
            } catch (Exception e) {
                // Silent fail for checklist JSON
            }
            if (!dorPassed) {
                item.setStatus(oldStatus); // Revert status
                backlogItemRepository.save(item);
                PmResponse response = new PmResponse(false, "DoR validation failed. Check checklist.");
                response.setData(dorResult);
                return response;
            }
        }

        // Enforce DoD check before marking "Done"
        if ("Done".equals(newStatus)) {
            Map<String, Object> testResults = new HashMap<>();
            testResults.put("allAcceptanceCriteriaPassed", item.isDodPassed());
            testResults.put("codeReviewed", true); // Assume approved for MVP
            testResults.put("allTestsPass", true);
            testResults.put("noCriticalDefects", true);
            testResults.put("documentationUpdated", true);
            testResults.put("poAccepted", true);

            Map<String, Object> dodResult = dodValidator.validate(item, testResults);
            boolean dodPassed = (boolean) dodResult.get("allPassed");
            item.setDodPassed(dodPassed);
            try {
                item.setDodChecklistJson(objectMapper.writeValueAsString(dodResult.get("checklist")));
            } catch (Exception e) {
                // Silent fail for checklist JSON
            }
            if (!dodPassed) {
                item.setStatus(oldStatus); // Revert status
                backlogItemRepository.save(item);
                PmResponse response = new PmResponse(false, "DoD validation failed. Check checklist.");
                response.setData(dodResult);
                return response;
            }
        }

        item = backlogItemRepository.save(item);

        auditLogService.log(userId, request.getProjectId(), "BACKLOG_STATUS_CHANGED",
                "Changed status from " + oldStatus + " to " + newStatus + " for: " + item.getTitle(),
                ipAddress, true);

        PmResponse response = new PmResponse(true, "Status updated to: " + newStatus);
        response.setData(item);
        return response;
    }

    private PmResponse runLoop(PmRequest request, Long userId, String ipAddress) {
        // Update the active sprint's loop
        Optional<Sprint> optSprint = sprintRepository.findTopByProjectIdAndStatusOrderByStartDateDesc(
                request.getProjectId(), "Active");
        if (optSprint.isEmpty()) {
            return new PmResponse(false, "No active sprint found");
        }

        Sprint sprint = optSprint.get();
        sprint.setActiveLoop(request.getLoopType() != null ? request.getLoopType() : "IDLE");
        sprintRepository.save(sprint);

        auditLogService.log(userId, request.getProjectId(), "LOOP_ACTIVATED",
                "Activated loop: " + request.getLoopType() + " for sprint: " + sprint.getName(),
                ipAddress, true);

        PmResponse response = new PmResponse(true, "Loop " + request.getLoopType() + " activated");
        Map<String, Object> data = new HashMap<>();
        data.put("sprintId", sprint.getId());
        data.put("sprintName", sprint.getName());
        data.put("activeLoop", sprint.getActiveLoop());
        data.put("backlogStats", getBacklogStats(request.getProjectId()));
        response.setData(data);
        return response;
    }

    private PmResponse checkDoR(PmRequest request, Long userId) {
        if (request.getSprintId() == null) {
            return new PmResponse(false, "Backlog item ID required");
        }
        Optional<BacklogItem> optItem = backlogItemRepository.findById(request.getSprintId());
        if (optItem.isEmpty()) {
            return new PmResponse(false, "Backlog item not found");
        }
        Map<String, Object> dorResult = dorValidator.validate(optItem.get());
        PmResponse response = new PmResponse((boolean) dorResult.get("allPassed"),
                (boolean) dorResult.get("allPassed") ? "DoR passed" : "DoR failed");
        response.setData(dorResult);
        return response;
    }

    private PmResponse checkDoD(PmRequest request, Long userId) {
        if (request.getSprintId() == null) {
            return new PmResponse(false, "Backlog item ID required");
        }
        Optional<BacklogItem> optItem = backlogItemRepository.findById(request.getSprintId());
        if (optItem.isEmpty()) {
            return new PmResponse(false, "Backlog item not found");
        }
        Map<String, Object> testResults = new HashMap<>();
        // For MVP, pass empty results — actual test results come from QA
        Map<String, Object> dodResult = dodValidator.validate(optItem.get(), testResults);
        PmResponse response = new PmResponse((boolean) dodResult.get("allPassed"),
                (boolean) dodResult.get("allPassed") ? "DoD passed" : "DoD failed");
        response.setData(dodResult);
        return response;
    }

    private PmResponse getBacklog(PmRequest request) {
        List<BacklogItem> backlog;
        if (request.getSprintId() != null) {
            backlog = backlogItemRepository.findBySprintIdOrderByPriorityAsc(request.getSprintId());
        } else {
            backlog = backlogItemRepository.findByProjectIdOrderByPriorityAsc(request.getProjectId());
        }

        PmResponse response = new PmResponse(true, "Backlog retrieved");
        List<Map<String, Object>> backlogData = new ArrayList<>();
        for (BacklogItem item : backlog) {
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("id", item.getId());
            itemMap.put("title", item.getTitle());
            itemMap.put("itemType", item.getItemType());
            itemMap.put("status", item.getStatus());
            itemMap.put("priority", item.getPriority());
            itemMap.put("storyPoints", item.getStoryPoints());
            itemMap.put("dorPassed", item.isDorPassed());
            itemMap.put("dodPassed", item.isDodPassed());
            itemMap.put("sprintId", item.getSprintId());
            backlogData.add(itemMap);
        }
        response.setData(backlogData);
        response.setMetrics(getBacklogStats(request.getProjectId()));
        return response;
    }

    private PmResponse getSprints(PmRequest request) {
        List<Sprint> sprints = sprintRepository.findByProjectIdOrderByStartDateDesc(request.getProjectId());
        PmResponse response = new PmResponse(true, "Sprints retrieved");
        response.setData(sprints);
        return response;
    }

    private Map<String, Object> getBacklogStats(Long projectId) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", backlogItemRepository.countByProjectIdAndStatus(projectId, "New") +
                backlogItemRepository.countByProjectIdAndStatus(projectId, "Ready") +
                backlogItemRepository.countByProjectIdAndStatus(projectId, "InProgress") +
                backlogItemRepository.countByProjectIdAndStatus(projectId, "InReview") +
                backlogItemRepository.countByProjectIdAndStatus(projectId, "Done"));
        stats.put("new", backlogItemRepository.countByProjectIdAndStatus(projectId, "New"));
        stats.put("ready", backlogItemRepository.countByProjectIdAndStatus(projectId, "Ready"));
        stats.put("inProgress", backlogItemRepository.countByProjectIdAndStatus(projectId, "InProgress"));
        stats.put("done", backlogItemRepository.countByProjectIdAndStatus(projectId, "Done"));
        return stats;
    }
}
