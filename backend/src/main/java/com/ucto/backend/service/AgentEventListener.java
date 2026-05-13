package com.ucto.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ucto.backend.entity.Project;
import com.ucto.backend.service.RepoWorkspaceService.RepoWorkspaceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.nio.file.Path;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Listens to all agent.* Redis Pub/Sub topics for event routing and audit interception.
 * 
 * Subscribes to agent.* wildcard to intercept all agent events.
 * Routes events to downstream agents based on pipeline flow.
 * Also handles workspace preparation for repo-aware developer agent.
 * See docs/agent_orchestration_design.md for topic conventions.
 */
@Service
public class AgentEventListener implements MessageListener {

    @Autowired
    private RedisMessageListenerContainer listenerContainer;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private RepoWorkspaceService repoWorkspaceService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Processed event IDs cache (simple in-memory for MVP; use Redis in production)
    private final java.util.Set<String> processedEventIds = java.util.concurrent.ConcurrentHashMap.newKeySet();

    /**
     * Redis Pub/Sub does not support wildcard subscriptions natively.
     * For MVP, we subscribe to all known agent topics.
     * In Phase 2 with RabbitMQ, we'll use topic exchanges with wildcard routing keys.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void subscribeToAgentTopics() {
        String[] topics = {
            "agent.ba.trigger", "agent.ba.complete", "agent.ba.error", "agent.ba.clarify",
            "agent.developer.trigger", "agent.developer.complete", "agent.developer.error",
            "agent.developer.workspace_ready", "agent.developer.workspace_error",
            "agent.tester.trigger", "agent.tester.complete", "agent.tester.error",
            "agent.compliance.trigger", "agent.compliance.complete", "agent.compliance.error",
            "agent.ux.trigger", "agent.ux.complete", "agent.ux.error", "agent.ux.clarify",
            "agent.architect.trigger", "agent.architect.complete", "agent.architect.error",
            "agent.pm.trigger", "agent.pm.complete", "agent.pm.error",
            "agent.documentation.trigger", "agent.documentation.complete", "agent.documentation.error"
        };


        for (String topic : topics) {
            listenerContainer.addMessageListener(this, new ChannelTopic(topic));
        }

        System.out.println("Agent Event Listener subscribed to " + topics.length + " agent topics");
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String topic = new String(message.getChannel());
        String body = new String(message.getBody());

        System.out.println("Agent event received on " + topic + ": " + body);

        try {
            // Parse the event payload
            Map<String, Object> eventPayload = objectMapper.readValue(body, 
                new TypeReference<Map<String, Object>>() {});

            // Idempotency check: skip if eventId already processed
            String eventId = (String) eventPayload.getOrDefault("eventId", "");
            if (eventId.isEmpty() || processedEventIds.contains(eventId)) {
                if (processedEventIds.contains(eventId)) {
                    System.out.println("Duplicate event " + eventId + " on " + topic + " - skipping");
                    return;
                }
                // Generate eventId if not present
                eventId = UUID.randomUUID().toString();
                eventPayload.put("eventId", eventId);
            }

            // Mark as processed
            processedEventIds.add(eventId);

            // Audit logging: log every agent event automatically
            logAgentEvent(topic, eventPayload);

            // Route based on event type
            if (topic.endsWith(".trigger")) {
                handleAgentTrigger(topic, eventPayload);
            } else if (topic.endsWith(".complete")) {
                handleAgentComplete(topic, eventPayload);
            } else if (topic.endsWith(".error")) {
                handleAgentError(topic, eventPayload);
            } else if (topic.endsWith(".clarify")) {
                handleAgentClarify(topic, eventPayload);
            }
        } catch (Exception e) {
            System.err.println("Error processing agent event on " + topic + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle trigger events. Currently handles workspace preparation for developer agent.
     */
    private void handleAgentTrigger(String topic, Map<String, Object> payload) {
        if (topic.equals("agent.developer.trigger")) {
            handleDeveloperTrigger(payload);
        }
    }

    /**
     * Extract the simulation flag from an event payload.
     */
    private boolean isSimulation(Map<String, Object> payload) {
        Object sim = payload.get("simulation");
        if (sim instanceof Boolean) return (Boolean) sim;
        if (sim instanceof String) return "true".equalsIgnoreCase((String) sim);
        // Also check nested data block
        Object data = payload.get("data");
        if (data instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> dataMap = (Map<String, Object>) data;
            sim = dataMap.get("simulation");
            if (sim instanceof Boolean) return (Boolean) sim;
            if (sim instanceof String) return "true".equalsIgnoreCase((String) sim);
        }
        return false;
    }

    /**
     * When developer agent is triggered, attempt workspace preparation.
     * Publishes workspace_ready or workspace_error accordingly.
     * Respects simulation flag — skips real git ops when simulation=true.
     */
    private void handleDeveloperTrigger(Map<String, Object> payload) {
        String projectIdStr = getProjectId(payload);
        if (projectIdStr == null) {
            System.out.println("Developer trigger has no projectId - skipping workspace prep");
            return;
        }

        Long projectId;
        try {
            projectId = Long.parseLong(projectIdStr);
        } catch (NumberFormatException e) {
            System.out.println("Invalid projectId format: " + projectIdStr);
            return;
        }

        Project project = projectService.getProjectById(projectId);
        if (project == null) {
            System.out.println("Project " + projectId + " not found - skipping workspace prep");
            return;
        }

        // Check if repo is configured
        if (project.getRepoUrl() == null || project.getRepoUrl().isBlank()) {
            System.out.println("Project " + projectId + " has no repo configured - skipping workspace prep");
            return;
        }

        boolean simulation = isSimulation(payload);
        if (simulation) {
            System.out.println("SIMULATION MODE: Preparing workspace for project " + projectId + " (no real git operations)");
        }

        try {
            Path sourceDir = repoWorkspaceService.prepareWorkspace(project, simulation);
            System.out.println("Workspace ready for project " + projectId + " at " + sourceDir);

            // Publish workspace_ready event (preserves simulation flag)
            Map<String, Object> readyEvent = new HashMap<>();
            readyEvent.put("eventId", UUID.randomUUID().toString());
            readyEvent.put("eventType", "agent.developer.workspace_ready");
            readyEvent.put("projectId", projectIdStr);
            readyEvent.put("simulation", simulation);
            readyEvent.put("timestamp", Instant.now().toString());
            readyEvent.put("correlationId", payload.getOrDefault("correlationId", ""));
            readyEvent.put("data", Map.of(
                "workspacePath", sourceDir.toAbsolutePath().toString(),
                "sourceTopic", "agent.developer.trigger",
                "repoUrl", project.getRepoUrl(),
                "repoBranch", project.getRepoBranch(),
                "simulation", simulation
            ));

            publishEvent("agent.developer.workspace_ready", readyEvent);
        } catch (RepoWorkspaceException e) {
            System.err.println("Workspace preparation failed for project " + projectId + ": " + e.getMessage());

            // Publish workspace_error event
            Map<String, Object> errorEvent = new HashMap<>();
            errorEvent.put("eventId", UUID.randomUUID().toString());
            errorEvent.put("eventType", "agent.developer.workspace_error");
            errorEvent.put("projectId", projectIdStr);
            errorEvent.put("simulation", simulation);
            errorEvent.put("timestamp", Instant.now().toString());
            errorEvent.put("correlationId", payload.getOrDefault("correlationId", ""));
            errorEvent.put("data", Map.of(
                "error", e.getMessage(),
                "sourceTopic", "agent.developer.trigger",
                "requiresIntervention", true,
                "simulation", simulation
            ));

            publishEvent("agent.developer.workspace_error", errorEvent);
        }
    }


    /**
     * Route completed agent events to the next agent in the pipeline.
     * Pipeline flow per docs/agent_orchestration_design.md:
     *   BA → Developer → Tester → Compliance
     *   BA → UX (if requirements clarified)
     *   UX → BA
     */
    private void handleAgentComplete(String topic, Map<String, Object> payload) {
        System.out.println("Agent complete: " + topic);

        String projectId = getProjectId(payload);
        if (projectId == null) return;

        // Determine next agent based on which agent completed
        if (topic.equals("agent.ba.complete")) {
            // BA complete → check nextAction
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) payload.getOrDefault("data", new HashMap<>());
            String nextAction = (String) data.getOrDefault("nextAction", "screen_generation");

            if ("screen_generation".equals(nextAction)) {
                // Route to Developer
                publishEvent("agent.developer.trigger", createNextEvent(payload, "developer", projectId, data));
                System.out.println("Routing BA complete → Developer trigger for project " + projectId);
            } else {
                // Route to UX for wireframing
                publishEvent("agent.ux.trigger", createNextEvent(payload, "ux", projectId, data));
                System.out.println("Routing BA complete → UX trigger for project " + projectId);
            }
        } 
        else if (topic.equals("agent.developer.complete")) {
            // Developer complete → route to Tester
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) payload.getOrDefault("data", new HashMap<>());
            publishEvent("agent.tester.trigger", createNextEvent(payload, "tester", projectId, data));
            System.out.println("Routing Developer complete → Tester trigger for project " + projectId);
        } 
        else if (topic.equals("agent.tester.complete")) {
            // Tester complete → route to Compliance
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) payload.getOrDefault("data", new HashMap<>());
            publishEvent("agent.compliance.trigger", createNextEvent(payload, "compliance", projectId, data));
            System.out.println("Routing Tester complete → Compliance trigger for project " + projectId);
        } 
        else if (topic.equals("agent.ux.complete")) {
            // UX complete → route back to BA for presentation
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) payload.getOrDefault("data", new HashMap<>());
            publishEvent("agent.ba.trigger", createNextEvent(payload, "ba", projectId, data));
            System.out.println("Routing UX complete → BA trigger for project " + projectId);
        }
        else if (topic.equals("agent.compliance.complete")) {
            // Compliance complete → pipeline finished
            System.out.println("Compliance pipeline completed for project " + projectId);
        }
        else if (topic.equals("agent.architect.complete")) {
            // Architect complete → no further routing needed
            System.out.println("Architect review completed for project " + projectId);
        }
    }

    /**
     * Handle agent errors: log and notify BA for manual intervention.
     * Per docs: errors are fail-fast; no retries; BA notified.
     */
    private void handleAgentError(String topic, Map<String, Object> payload) {
        String errorMsg = payload.getOrDefault("error", topic + " failed").toString();
        System.out.println("Agent error on " + topic + ": " + errorMsg);

        // For errors, route back to BA for intervention
        String projectId = getProjectId(payload);
        if (projectId == null) return;

        // Publish error notification event
        Map<String, Object> errorEvent = new HashMap<>();
        errorEvent.put("eventId", UUID.randomUUID().toString());
        errorEvent.put("eventType", topic.replace(".error", ".clarify"));
        errorEvent.put("projectId", projectId);
        errorEvent.put("timestamp", Instant.now().toString());
        errorEvent.put("correlationId", payload.getOrDefault("correlationId", ""));
        
        Map<String, Object> data = new HashMap<>();
        data.put("errorMessage", errorMsg);
        data.put("sourceTopic", topic);
        data.put("requiresIntervention", true);
        errorEvent.put("data", data);

        // Notify BA about the error
        publishEvent("agent.ba.clarify", errorEvent);
        System.out.println("Error notification sent to BA for project " + projectId);
    }

    /**
     * Handle clarification requests: route back to BA.
     */
    private void handleAgentClarify(String topic, Map<String, Object> payload) {
        System.out.println("Agent clarification needed on " + topic + ": " + payload);
        // Clarification requests are already on ba.clarify or ux.clarify topics
        // The BA agent (frontend) will display these to the user
        String projectId = getProjectId(payload);
        if (projectId == null) return;

        // Log the clarification request
        auditLogService.logAuthAction(null, "AGENT_CLARIFICATION",
                "Clarification requested on " + topic + " for project " + projectId, "", false);
    }

    /**
     * Create the next event payload in the pipeline, preserving correlationId.
     */
    private Map<String, Object> createNextEvent(Map<String, Object> currentPayload, 
                                                  String nextAgentType, 
                                                  String projectId,
                                                  Map<String, Object> data) {
        Map<String, Object> nextEvent = new HashMap<>();
        nextEvent.put("eventId", UUID.randomUUID().toString());
        nextEvent.put("eventType", "agent." + nextAgentType + ".trigger");
        nextEvent.put("projectId", projectId);
        nextEvent.put("timestamp", Instant.now().toString());
        nextEvent.put("correlationId", currentPayload.getOrDefault("correlationId", UUID.randomUUID().toString()));
        
        // Preserve upstream data context
        Map<String, Object> nextData = new HashMap<>(data);
        nextData.put("upstreamEventId", currentPayload.get("eventId"));
        nextData.put("upstreamTopic", currentPayload.get("eventType"));
        nextEvent.put("data", nextData);
        
        return nextEvent;
    }

    /**
     * Publish an event to a Redis Pub/Sub topic.
     */
    private void publishEvent(String topic, Map<String, Object> eventPayload) {
        try {
            String json = objectMapper.writeValueAsString(eventPayload);
            redisTemplate.convertAndSend(topic, json);
            System.out.println("Published event to " + topic);
        } catch (Exception e) {
            System.err.println("Failed to publish event to " + topic + ": " + e.getMessage());
        }
    }

    /**
     * Extract projectId from payload.
     */
    private String getProjectId(Map<String, Object> payload) {
        String projectId = (String) payload.get("projectId");
        if (projectId == null) {
            // Try nested data.projectId
            Object data = payload.get("data");
            if (data instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> dataMap = (Map<String, Object>) data;
                Object nestedId = dataMap.get("projectId");
                if (nestedId != null) {
                    projectId = nestedId.toString();
                }
            }
        }
        return projectId;
    }

    /**
     * Log agent event to audit log.
     */
    private void logAgentEvent(String topic, Map<String, Object> payload) {
        try {
            String eventType = "AGENT_" + topic.replace("agent.", "").replace(".", "_").toUpperCase();
            String description = "Agent event: " + topic + " - " + payload.toString();
            auditLogService.logAuthAction(null, eventType, description, "", true);
        } catch (Exception e) {
            System.err.println("Failed to log agent event: " + e.getMessage());
        }
    }
}
