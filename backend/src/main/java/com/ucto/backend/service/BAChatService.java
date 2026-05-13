package com.ucto.backend.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ucto.backend.dto.BAChatHistoryResponse;
import com.ucto.backend.dto.BAChatResponse;
import com.ucto.backend.entity.BAChatMessage;
import com.ucto.backend.entity.Requirement;
import com.ucto.backend.repository.BAChatMessageRepository;
import com.ucto.backend.repository.RequirementRepository;

/**
 * BA Chat service — the only external communication channel between customer and UCTO agents.
 *
 * Per docs/ucto_playbook.md:
 * - BA is the single voice to customer
 * - BA must never disclose internal codebase, architecture, or working style
 * - All other agents communicate internally only
 * - Max 3 BA clarification rounds before escalation
 *
 * Per docs/state_machines.md §3 (BA Clarification Loop):
 * - Round counter increments each time BA publishes to agent.ba.clarify
 * - Escalation sends notification to UCTO Admin / product owner
 */
@Service
public class BAChatService {

    @Autowired
    private BAChatMessageRepository messageRepository;

    @Autowired
    private RequirementRepository requirementRepository;

    @Autowired
    private AgentOrchestrationService agentOrchestrationService;

    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private ObjectMapper objectMapper;

    /** Max clarification rounds before escalation */
    private static final int MAX_CLARIFICATION_ROUNDS = 3;

    /**
     * Process a user message in the BA chat.
     * Returns the BA's response, handling the full clarification lifecycle.
     */
    @Transactional
    public BAChatResponse processMessage(Long userId, Long projectId, String message, String ipAddress) {
        // Check usage limits
        if (!subscriptionService.canRunAgent(userId)) {
            throw new AgentOrchestrationService.AgentRunLimitExceededException(
                    "Agent run limit exceeded for user: " + userId);
        }

        // Determine current round based on existing chat messages
        long messageCount = messageRepository.countByProjectIdAndUserId(projectId, userId);
        int currentRound = (int) (messageCount / 2) + 1; // Each round = user msg + BA response

        // Check if max rounds exceeded
        boolean needsEscalation = currentRound > MAX_CLARIFICATION_ROUNDS;

        // Build BA response
        BAChatMessage chatMessage = new BAChatMessage();
        chatMessage.setProjectId(projectId);
        chatMessage.setUserId(userId);
        chatMessage.setUserMessage(message);
        chatMessage.setRoundNumber(needsEscalation ? MAX_CLARIFICATION_ROUNDS : currentRound);

        String baResponse;
        String messageType;
        List<String> ambiguities = new ArrayList<>();
        List<String> decisions = new ArrayList<>();
        boolean clarificationComplete = false;

        if (needsEscalation) {
            // Max rounds reached — escalation path
            baResponse = "I've reached the maximum of " + MAX_CLARIFICATION_ROUNDS +
                    " clarification rounds. Your request has been escalated to a UCTO Admin " +
                    "who will review and assist further. Thank you for your patience.";
            messageType = "ESCALATION";
            chatMessage.setMessageType(messageType);

            // Record the escalation in audit
            auditLogService.log(userId, projectId, "BA_ESCALATION",
                    "BA clarification max rounds reached for project " + projectId, ipAddress, true);

        } else {
            // Analyze the message and generate appropriate BA response
            BAProcessingResult result = analyzeMessage(message, projectId, userId, currentRound);
            baResponse = result.response;
            messageType = result.messageType;
            ambiguities = result.ambiguities;
            decisions = result.decisions;
            clarificationComplete = result.clarificationComplete;

            chatMessage.setMessageType(messageType);

            if (!decisions.isEmpty()) {
                try {
                    chatMessage.setDecisionsJson(objectMapper.writeValueAsString(decisions));
                } catch (JsonProcessingException e) {
                    chatMessage.setDecisionsJson(String.join(", ", decisions));
                }
            }

            // If clarification is complete, update requirements and trigger downstream agents
            if (clarificationComplete) {
                finalizeRequirements(projectId, userId, ipAddress, decisions);
            }

            // If this is a clarification round, audit log it
            if ("CLARIFICATION".equals(messageType) || "DECISION".equals(messageType)) {
                auditLogService.log(userId, projectId, "BA_CLARIFICATION_ROUND_" + currentRound,
                        "BA clarification round " + currentRound + " for project " + projectId, ipAddress, true);
            }
        }

        // Save the BA response
        chatMessage.setBaResponse(baResponse);
        chatMessage = messageRepository.save(chatMessage);

        // Build response DTO
        BAChatResponse response = new BAChatResponse();
        response.setId(chatMessage.getId());
        response.setUserMessage(chatMessage.getUserMessage());
        response.setBaResponse(baResponse);
        response.setRoundNumber(chatMessage.getRoundNumber());
        response.setMessageType(messageType);
        response.setDecisionsJson(chatMessage.getDecisionsJson());
        response.setCreatedAt(chatMessage.getCreatedAt());
        response.setAmbiguities(ambiguities);
        response.setDecisions(decisions);
        response.setClarificationComplete(clarificationComplete);
        response.setNeedsEscalation(needsEscalation);

        // Record the agent run for usage metering
        agentOrchestrationService.triggerAgent("ba", projectId, userId, ipAddress,
                Map.of("round", currentRound, "messageType", messageType));

        return response;
    }

    /**
     * Get chat history for a project.
     */
    public BAChatHistoryResponse getChatHistory(Long projectId, Long userId) {
        List<BAChatMessage> messages = messageRepository.findByProjectIdAndUserIdOrderByCreatedAtAsc(projectId, userId);

        // Get the max round number for this project
        int maxRound = messages.stream()
                .mapToInt(BAChatMessage::getRoundNumber)
                .max()
                .orElse(0);

        boolean needsEscalation = maxRound >= MAX_CLARIFICATION_ROUNDS;
        boolean clarificationComplete = messages.stream()
                .anyMatch(m -> "FINALIZATION".equals(m.getMessageType()));

        BAChatHistoryResponse history = new BAChatHistoryResponse();
        history.setMessages(messages.stream().map(this::toResponse).collect(Collectors.toList()));
        history.setCurrentRound(maxRound);
        history.setClarificationComplete(clarificationComplete);
        history.setNeedsEscalation(needsEscalation);

        return history;
    }

    /**
     * Analyze the user message and generate an appropriate BA response.
     * This is the core BA reasoning logic.
     */
    private BAProcessingResult analyzeMessage(String message, Long projectId, Long userId, int round) {
        BAProcessingResult result = new BAProcessingResult();
        String lowerMsg = message.toLowerCase().trim();

        // Check if project has requirements
        List<Requirement> requirements = requirementRepository.findByProjectId(projectId);
        boolean hasRequirements = !requirements.isEmpty();

        // Check for greeting or empty message
        if (lowerMsg.isEmpty() || isGreeting(lowerMsg)) {
            result.response = buildGreeting(hasRequirements, round);
            result.messageType = "GREETING";
            return result;
        }

        // Check if user wants to finalize
        if (isFinalizationRequest(lowerMsg)) {
            result.response = "I've documented all decisions from our conversation. " +
                    "Let me finalize the requirements and trigger the development agents. " +
                    "I'll update the BRD, UCD, and TCD documents accordingly.";
            result.messageType = "FINALIZATION";
            result.clarificationComplete = true;
            return result;
        }

        // Analyze the message for ambiguities
        result.ambiguities = identifyAmbiguities(message);
        result.decisions = extractDecisions(message);

        if (!result.ambiguities.isEmpty() && round <= MAX_CLARIFICATION_ROUNDS) {
            // BA identified ambiguities — needs clarification
            StringBuilder sb = new StringBuilder();
            sb.append("Thank you for your input. I've identified the following points that need clarification:\n\n");
            for (int i = 0; i < result.ambiguities.size(); i++) {
                sb.append("  ").append(i + 1).append(". ").append(result.ambiguities.get(i)).append("\n");
            }
            sb.append("\nCould you please provide more details on these points? ");
            sb.append("This is round ").append(round).append(" of ").append(MAX_CLARIFICATION_ROUNDS).append(".");

            if (!result.decisions.isEmpty()) {
                sb.append("\n\nI've also noted the following decisions:\n");
                for (int i = 0; i < result.decisions.size(); i++) {
                    sb.append("  ").append(i + 1).append(". ").append(result.decisions.get(i)).append("\n");
                }
                sb.append("\nThese have been documented in the project records.");
            }

            result.response = sb.toString();
            result.messageType = "CLARIFICATION";

        } else if (!result.decisions.isEmpty()) {
            // BA extracted decisions without ambiguities
            StringBuilder sb = new StringBuilder();
            sb.append("Thank you for the clear input. Here's what I've documented:\n\n");
            for (int i = 0; i < result.decisions.size(); i++) {
                sb.append("  ✓ ").append(result.decisions.get(i)).append("\n");
            }
            sb.append("\nIf everything looks correct, let me know and I'll finalize the requirements. ");
            sb.append("Otherwise, please provide any additional details.");
            result.response = sb.toString();
            result.messageType = "DECISION";

        } else {
            // General acknowledgment — BA processes but doesn't have specific items
            result.response = "I understand. I'm processing your input against the project requirements. " +
                    "If I need any clarifications, I'll let you know. " +
                    "You can also describe your requirements in more detail at any time.\n\n" +
                    "Round " + round + " of " + MAX_CLARIFICATION_ROUNDS + ".";
            result.messageType = "REQUIREMENT";
        }

        return result;
    }

    /**
     * Finalize requirements, update documents, and trigger downstream agents.
     */
    private void finalizeRequirements(Long projectId, Long userId, String ipAddress, List<String> decisions) {
        // Update all requirements in this project to CLARIFIED/APPROVED status
        List<Requirement> requirements = requirementRepository.findByProjectId(projectId);
        for (Requirement req : requirements) {
            req.setStatus("CLARIFIED");
            requirementRepository.save(req);
        }

        // Audit log the finalization
        auditLogService.log(userId, projectId, "BA_REQUIREMENTS_FINALIZED",
                "BA finalized requirements for project " + projectId +
                        ". Decisions: " + String.join("; ", decisions),
                ipAddress, true);

        // Trigger downstream agents via the orchestration pipeline
        // Per docs/agent_orchestration_design.md: BA → UI/UX (screen generation) then Developer
        Map<String, Object> data = new HashMap<>();
        data.put("clarificationRounds", MAX_CLARIFICATION_ROUNDS);
        data.put("decisions", decisions);
        data.put("nextAction", "screen_generation");

        // Trigger UX agent to generate screens from finalized requirements
        agentOrchestrationService.triggerAgent("ux", projectId, userId, ipAddress, data);

        // Send BA complete event
        Map<String, Object> baData = new HashMap<>(data);
        baData.put("status", "clarified");
        baData.put("summary", "Requirements finalized after up to " + MAX_CLARIFICATION_ROUNDS + " clarification rounds");
        agentOrchestrationService.triggerAgent("ba", projectId, userId, ipAddress, baData);
    }

    // --- Helper methods ---

    private boolean isGreeting(String msg) {
        return msg.matches("^(hi|hello|hey|greetings|good\\s*(morning|afternoon|evening)|howdy|yo)\\b.*");
    }

    private boolean isFinalizationRequest(String msg) {
        return msg.matches(".*\\b(finalize|confirm|approve|looks?\\s*great|that'?s?\\s*all|no\\s*more\\s*questions|proceed|ready|done)\\b.*");
    }

    /**
     * Identify potential ambiguities in the user's message.
     * This simulates BA reasoning — in production, this would use an LLM.
     */
    private List<String> identifyAmbiguities(String message) {
        List<String> ambiguities = new ArrayList<>();
        String lower = message.toLowerCase();

        // Check for vague terms
        if (lower.contains("fast") && !lower.matches(".*\\d+\\s*(ms|seconds|minutes).*")) {
            ambiguities.add("What is the specific performance target (e.g., response time in ms)?");
        }
        if (lower.contains("user") && lower.contains("multiple") && !lower.matches(".*\\b(admin|viewer|editor|role|permission)\\b.*")) {
            ambiguities.add("What user roles are needed (admin, viewer, editor)?");
        }
        if ((lower.contains("integrate") || lower.contains("integration")) && !lower.matches(".*\\b(api|payment|email|sms|database|third.?party)\\b.*")) {
            ambiguities.add("Which external services need to be integrated?");
        }
        if (lower.contains("platform") && !lower.matches(".*\\b(web|mobile|desktop|ios|android)\\b.*")) {
            ambiguities.add("Which platforms should be supported (web, mobile, both)?");
        }
        if ((lower.contains("color") || lower.contains("theme")) && !lower.matches(".*\\b(dark|light|custom|brand)\\b.*")) {
            ambiguities.add("Is there a specific theme or brand guidelines to follow?");
        }
        if (lower.contains("language") && !lower.matches(".*\\b(english|hindi|local|multi)\\b.*")) {
            ambiguities.add("Which languages should be supported?");
        }

        return ambiguities;
    }

    /**
     * Extract clear decisions from the user's message.
     */
    private List<String> extractDecisions(String message) {
        List<String> decisions = new ArrayList<>();
        String lower = message.toLowerCase();

        // Extract platform decisions
        if (lower.contains("web")) {
            decisions.add("Target platform: Web application");
        }
        if (lower.contains("mobile")) {
            decisions.add("Target platform: Mobile application");
        }
        if (lower.contains("flutter")) {
            decisions.add("Technology: Flutter framework");
        }
        if (lower.contains("spring") || lower.contains("java")) {
            decisions.add("Technology: Spring Boot backend");
        }
        if (lower.contains("postgres") || lower.contains("database")) {
            decisions.add("Database: PostgreSQL");
        }
        if (lower.contains("payment")) {
            decisions.add("Feature: Payment integration");
        }
        if (lower.contains("auth") || lower.contains("login")) {
            decisions.add("Feature: Authentication (login/signup)");
        }
        if (lower.contains("email")) {
            decisions.add("Feature: Email notifications");
        }
        if (lower.contains("dark")) {
            decisions.add("Theme: Dark mode support");
        }
        if (lower.contains("light")) {
            decisions.add("Theme: Light mode support");
        }
        if (lower.contains("admin")) {
            decisions.add("Role: Admin panel required");
        }
        if (lower.contains("analytics") || lower.contains("dashboard")) {
            decisions.add("Feature: Analytics dashboard");
        }
        if (lower.contains("api")) {
            decisions.add("Feature: REST API");
        }

        return decisions;
    }

    private String buildGreeting(boolean hasRequirements, int round) {
        if (hasRequirements) {
            return "Welcome! I'm your UCTO Business Analyst. " +
                    "I see you already have requirements for this project. " +
                    "How can I help you refine them? " +
                    "You can ask me questions, provide additional details, " +
                    "or let me know when you're ready to finalize.\n\n" +
                    "Round " + round + " of " + MAX_CLARIFICATION_ROUNDS + ".";
        } else {
            return "Hello! I'm your UCTO Business Analyst. " +
                    "I'm here to help you define requirements for your project. " +
                    "Please describe what you're building — the features, target audience, " +
                    "and any specific requirements you have in mind. " +
                    "I'll ask clarifying questions to ensure everything is clear.\n\n" +
                    "Round " + round + " of " + MAX_CLARIFICATION_ROUNDS + ".";
        }
    }

    private BAChatResponse toResponse(BAChatMessage msg) {
        BAChatResponse resp = new BAChatResponse();
        resp.setId(msg.getId());
        resp.setUserMessage(msg.getUserMessage());
        resp.setBaResponse(msg.getBaResponse());
        resp.setRoundNumber(msg.getRoundNumber());
        resp.setMessageType(msg.getMessageType());
        resp.setDecisionsJson(msg.getDecisionsJson());
        resp.setCreatedAt(msg.getCreatedAt());
        resp.setClarificationComplete("FINALIZATION".equals(msg.getMessageType()));
        resp.setNeedsEscalation(msg.getRoundNumber() >= MAX_CLARIFICATION_ROUNDS);
        return resp;
    }

    // Inner class for BA processing results
    private static class BAProcessingResult {
        String response;
        String messageType;
        List<String> ambiguities = new ArrayList<>();
        List<String> decisions = new ArrayList<>();
        boolean clarificationComplete;
    }
}
