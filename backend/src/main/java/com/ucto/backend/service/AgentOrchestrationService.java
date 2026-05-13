package com.ucto.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Orchestrates AI agent communication via Redis Pub/Sub.
 * 
 * Agent topic convention: agent.<type>.<action>
 * Types: ba, developer, tester, compliance, ux, architect
 * Actions: trigger, complete, error, clarify
 * 
 * See docs/agent_orchestration_design.md for full event schema.
 */
@Service
public class AgentOrchestrationService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    private UsageMeterService usageMeterService;

    @Autowired
    private AuditLogService auditLogService;

    /**
     * Publish a trigger event to an agent topic and record the usage.
     * Returns the eventId for idempotency tracking.
     */
    public String triggerAgent(String agentType, Long projectId, Long userId, String ipAddress, Map<String, Object> data) {
        // Check usage limits before triggering
        if (!subscriptionService.canRunAgent(userId)) {
            throw new AgentRunLimitExceededException("Agent run limit exceeded for user: " + userId);
        }

        String eventId = "evt_" + UUID.randomUUID().toString().substring(0, 8);
        String topic = "agent." + agentType + ".trigger";

        Map<String, Object> event = new HashMap<>();
        event.put("eventId", eventId);
        event.put("eventType", topic);
        event.put("projectId", projectId != null ? projectId.toString() : null);
        event.put("agentId", "agent_" + agentType + "_01");
        event.put("timestamp", Instant.now().toString());
        event.put("correlationId", "proj_" + (projectId != null ? projectId : "unknown") + "_run");
        event.put("data", data != null ? data : new HashMap<>());

        try {
            String json = objectMapper.writeValueAsString(event);
            redisTemplate.convertAndSend(topic, json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize agent event", e);
        }

        // Record the agent run for usage metering
        usageMeterService.recordAgentRun(userId, mapAgentType(agentType));

        // Audit interception - log every agent trigger automatically
        auditLogService.log(userId, projectId, "AGENT_TRIGGER_" + agentType.toUpperCase(),
                "Agent triggered: " + agentType + " on project " + projectId,
                ipAddress, true);

        return eventId;
    }

    private String mapAgentType(String topicType) {
        // Map topic type to entity agentType
        switch (topicType.toLowerCase()) {
            case "ba": return "BA";
            case "developer": return "DEVELOPER";
            case "tester": return "TESTER";
            case "compliance": return "COMPLIANCE";
            case "ux": return "UI_UX";
            case "architect": return "ARCHITECT";
            case "pm": return "PM";
            case "documentation": return "DOCUMENTATION";
            default: return topicType.toUpperCase();
        }

    }

    public static class AgentRunLimitExceededException extends RuntimeException {
        public AgentRunLimitExceededException(String message) {
            super(message);
        }
    }
}
