package com.ucto.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ucto.backend.dto.OrchestratorRequest;
import com.ucto.backend.dto.OrchestratorResponse;
import com.ucto.backend.entity.AgentMessage;
import com.ucto.backend.entity.BacklogItem;
import com.ucto.backend.entity.Sprint;
import com.ucto.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Orchestrator Service — decides which loop to run, routes messages
 * based on needs_human flag, and coordinates agent activities.
 * Based on docs/orchestrator_prompt_template.md and docs/closed_loop_workflows.md.
 */
@Service
public class OrchestratorService {

    @Autowired
    private AgentMessageRepository agentMessageRepository;

    @Autowired
    private BacklogItemRepository backlogItemRepository;

    @Autowired
    private SprintRepository sprintRepository;

    @Autowired
    private AgentOrchestrationService agentOrchestrationService;

    @Autowired
    private GuardrailService guardrailService;

    @Autowired
    private AuditLogService auditLogService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Evaluate which loop should run next based on project state.
     * Decision matrix per docs/orchestrator_prompt_template.md.
     */
    public OrchestratorResponse evaluateNextLoop(Long projectId) {
        OrchestratorResponse response = new OrchestratorResponse(true, "Loop evaluation complete");
        Map<String, Object> loopStatus = new HashMap<>();

        // Check backlog state
        long backlogNew = backlogItemRepository.countByProjectIdAndStatus(projectId, "New");
        long backlogReady = backlogItemRepository.countByProjectIdAndStatus(projectId, "Ready");
        long backlogInProgress = backlogItemRepository.countByProjectIdAndStatus(projectId, "InProgress");
        long backlogInReview = backlogItemRepository.countByProjectIdAndStatus(projectId, "InReview");
        long backlogDone = backlogItemRepository.countByProjectIdAndStatus(projectId, "Done");

        // Check pending human questions
        long pendingHumanQuestions = agentMessageRepository.countByProjectIdAndNeedsHumanTrue(projectId);

        // Check active sprint
        Optional<Sprint> optActiveSprint = sprintRepository.findTopByProjectIdAndStatusOrderByStartDateDesc(
                projectId, "Active");
        String currentLoop = "IDLE";
        if (optActiveSprint.isPresent()) {
            currentLoop = optActiveSprint.get().getActiveLoop();
        }

        // Decision logic
        String recommendedLoop = "IDLE";
        String recommendedAgent = null;

        if (pendingHumanQuestions > 0) {
            // Human questions pending → Discovery or Risk loop needs BA/PO interaction
            recommendedLoop = "DISCOVERY";
            recommendedAgent = "BA";
            response.setMessage("Human questions pending — route through BA to PO");
        } else if (backlogNew > 0) {
            // New items need discovery → run Discovery Loop
            recommendedLoop = "DISCOVERY";
            recommendedAgent = "BA";
            response.setMessage("New backlog items need discovery");
        } else if (backlogReady > 0 && backlogInProgress == 0) {
            // Ready items waiting for development → run Build Loop
            recommendedLoop = "BUILD";
            recommendedAgent = "Developer";
            response.setMessage("Ready backlog items waiting for development");
        } else if (backlogInReview > 0) {
            // Items in review → run UX/Doc Loop
            recommendedLoop = "UX_DOC";
            recommendedAgent = "Documentation";
            response.setMessage("Completed items need documentation");
        } else if (backlogInProgress > 0) {
            // Items in progress → continue Build Loop
            recommendedLoop = "BUILD";
            recommendedAgent = "Developer";
            response.setMessage("Items in progress — continue build loop");
        } else if (backlogDone > 0) {
            // Completed items → run Risk Loop for compliance review
            recommendedLoop = "RISK";
            recommendedAgent = "Compliance";
            response.setMessage("Completed items need compliance review");
        } else {
            // No work → IDLE
            recommendedLoop = "IDLE";
            recommendedAgent = null;
            response.setMessage("No pending work. Waiting for PO input via BA.");
        }

        response.setRecommendedLoop(recommendedLoop);
        response.setRecommendedNextAgent(recommendedAgent);

        // Build loop status
        loopStatus.put("currentLoop", currentLoop);
        loopStatus.put("recommendedLoop", recommendedLoop);
        loopStatus.put("pendingHumanQuestions", pendingHumanQuestions);
        loopStatus.put("backlogNew", backlogNew);
        loopStatus.put("backlogReady", backlogReady);
        loopStatus.put("backlogInProgress", backlogInProgress);
        loopStatus.put("backlogInReview", backlogInReview);
        loopStatus.put("backlogDone", backlogDone);
        response.setLoopStatus(loopStatus);

        return response;
    }

    /**
     * Route a message to the correct agent based on message type and needs_human.
     * needs_human=true route: Origin → PM → BA → PO (Human)
     */
    @Transactional
    public OrchestratorResponse routeMessage(Long messageId) {
        Optional<AgentMessage> optMessage = agentMessageRepository.findById(messageId);
        if (optMessage.isEmpty()) {
            return new OrchestratorResponse(false, "Message not found");
        }

        AgentMessage message = optMessage.get();
        OrchestratorResponse response = new OrchestratorResponse(true, "Message routed");

        // Validate guardrails before routing
        List<String> violations = guardrailService.validateMessage(message);
        if (!violations.isEmpty()) {
            response.setMessage("Message has guardrail violations. Flagging for review.");
            response.setData(Map.of("violations", violations));
            message.setStatus("ERROR");
            agentMessageRepository.save(message);
            return response;
        }

        // Route based on needs_human
        if (message.isNeedsHuman()) {
            // Route: Origin → PM → BA → PO
            if (!"BA".equals(message.getToAgent()) && !"PM".equals(message.getToAgent())) {
                // Route to PM first for aggregation
                message.setToAgent("PM");
                message.setStatus("ROUTED");
            } else if ("PM".equals(message.getToAgent())) {
                // PM has seen it — route to BA for PO presentation
                message.setToAgent("BA");
                message.setStatus("ROUTED");
            } else {
                // BA will present to PO — mark as pending human
                message.setStatus("PENDING");
                response.setPendingHumanQuestions(getPendingHumanQuestions(message.getProjectId()));
            }
        } else {
            // Standard routing based on message type
            String targetAgent = mapMessageTypeToAgent(message.getMessageType());
            if (targetAgent != null) {
                message.setToAgent(targetAgent);
                message.setStatus("ROUTED");
            } else {
                message.setStatus("ERROR");
                response.setMessage("Unknown message type: " + message.getMessageType());
            }
        }

        agentMessageRepository.save(message);

        // Trigger the target agent via Redis Pub/Sub
        if (!message.isNeedsHuman() || "BA".equals(message.getToAgent())) {
            triggerTargetAgent(message);
        }

        response.setData(message);
        return response;
    }

    /**
     * Get all pending human questions for a project.
     */
    public List<Map<String, Object>> getPendingHumanQuestions(Long projectId) {
        List<AgentMessage> pendingMessages = agentMessageRepository
                .findByNeedsHumanTrueAndStatus("PENDING");

        return pendingMessages.stream()
                .filter(m -> m.getProjectId().equals(projectId))
                .map(m -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("messageId", m.getId());
                    item.put("fromAgent", m.getFromAgent());
                    item.put("messageType", m.getMessageType());
                    item.put("humanQuestions", m.getHumanQuestionsJson());
                    item.put("createdAt", m.getCreatedAt().toString());
                    return item;
                })
                .collect(Collectors.toList());
    }

    /**
     * Trigger an agent via the orchestration service.
     */
    private void triggerTargetAgent(AgentMessage message) {
        String agentType = mapAgentToTopic(message.getToAgent());
        if (agentType == null) return;

        Map<String, Object> data = new HashMap<>();
        data.put("messageId", message.getId());
        data.put("messageType", message.getMessageType());
        data.put("storyId", message.getStoryId());
        data.put("payload", message.getPayloadJson());

        agentOrchestrationService.triggerAgent(
                agentType,
                message.getProjectId(),
                null, // userId is optional in orchestration context
                "127.0.0.1",
                data);
    }

    /**
     * Get the current status of all loops.
     */
    public OrchestratorResponse getLoopStatus(Long projectId) {
        OrchestratorResponse response = evaluateNextLoop(projectId);
        List<Map<String, Object>> pendingQuestions = getPendingHumanQuestions(projectId);
        response.setPendingHumanQuestions(pendingQuestions);
        return response;
    }

    /**
     * Execute a full orchestrator action.
     */
    public OrchestratorResponse executeAction(OrchestratorRequest request) {
        switch (request.getAction()) {
            case "EVALUATE_NEXT_LOOP":
                return evaluateNextLoop(request.getProjectId());
            case "ROUTE_MESSAGE":
                return routeMessage(request.getMessageId());
            case "GET_LOOP_STATUS":
                return getLoopStatus(request.getProjectId());
            default:
                return new OrchestratorResponse(false, "Unknown action: " + request.getAction());
        }
    }

    private String mapMessageTypeToAgent(String messageType) {
        switch (messageType) {
            case "REQUIREMENTS_PACKAGE": return "BA";
            case "ARCHITECTURE_SPEC": return "Architect";
            case "UI_SPEC": return "UI_UX";
            case "IMPLEMENTATION_UPDATE": return "Developer";
            case "TEST_REPORT": return "Tester";
            case "COMPLIANCE_REPORT": return "Compliance";
            case "DOC_UPDATE": return "Documentation";
            default: return null;
        }
    }

    private String mapAgentToTopic(String agentName) {
        if (agentName == null) return null;
        switch (agentName.toUpperCase()) {
            case "BA": return "ba";
            case "ARCHITECT": return "architect";
            case "UI_UX": return "ux";
            case "DEVELOPER": return "developer";
            case "TESTER": return "tester";
            case "COMPLIANCE": return "compliance";
            case "DOCUMENTATION": return "ba"; // documentation uses BA topic for now
            case "PM": return "ba"; // PM uses BA topic for now
            default: return null;
        }
    }
}
