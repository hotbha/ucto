package com.ucto.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Production LLMAgentClient implementation that calls the DeepSeek API.
 *
 * Activated when ucto.llm.deepseek.api-key is set.
 * Falls back to StubLLMAgentClient when no key is configured.
 *
 * Loads prompts from PromptCatalog, merges with context, sends to
 * DeepSeek chat/completions endpoint, and returns the response text.
 *
 * <h3>Circuit Breaker</h3>
 * Implements a simple in-memory circuit breaker to avoid hammering a failing
 * DeepSeek API. After {@code failureThreshold} consecutive failures the breaker
 * opens and all calls fast-fail with a structured error JSON for
 * {@code cooldownMillis}. After cooldown, a single probe call is allowed
 * (HALF_OPEN). If it succeeds the breaker resets to CLOSED; if it fails the
 * breaker reopens.
 */
@Component
@ConditionalOnProperty(name = "ucto.llm.deepseek.api-key")
public class DeepSeekAgentClient implements LLMAgentClient {

    private static final Logger log = LoggerFactory.getLogger(DeepSeekAgentClient.class);

    /** Circuit breaker states. */
    enum CircuitBreakerState { CLOSED, OPEN, HALF_OPEN }

    @Autowired
    private PromptCatalog promptCatalog;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${ucto.llm.deepseek.api-url:https://api.deepseek.com/v1/chat/completions}")
    private String apiUrl;

    @Value("${ucto.llm.deepseek.api-key:}")
    private String apiKey;

    @Value("${ucto.llm.deepseek.model:deepseek-chat}")
    private String model;

    @Value("${ucto.llm.deepseek.timeout-ms:30000}")
    private int timeoutMs;

    @Value("${ucto.llm.deepseek.max-tokens:2048}")
    private int maxTokens;

    @Value("${ucto.llm.deepseek.temperature:0.3}")
    private double temperature;

    // ---- Circuit breaker state ----

    @Value("${ucto.llm.deepseek.circuit-breaker.threshold:3}")
    private int failureThreshold;

    @Value("${ucto.llm.deepseek.circuit-breaker.cooldown-ms:30000}")
    private long cooldownMillis;

    private volatile CircuitBreakerState circuitBreakerState = CircuitBreakerState.CLOSED;
    private final AtomicInteger consecutiveFailures = new AtomicInteger(0);
    private volatile long lastFailureTime = 0L;

    public DeepSeekAgentClient() {
        this.restTemplate = new RestTemplate();
    }

    // Constructor for testing with mocked RestTemplate
    DeepSeekAgentClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /* ---------- package-private accessors for testing ---------- */

    CircuitBreakerState getCircuitBreakerState() { return circuitBreakerState; }
    void setCircuitBreakerState(CircuitBreakerState state) { this.circuitBreakerState = state; }
    int getConsecutiveFailures() { return consecutiveFailures.get(); }
    void setConsecutiveFailures(int count) { consecutiveFailures.set(count); }
    long getLastFailureTime() { return lastFailureTime; }
    void setLastFailureTime(long time) { this.lastFailureTime = time; }

    @Override
    public String execute(String promptKey, Map<String, String> context) {
        if (!isAvailable()) {
            log.warn("DeepSeekAgentClient not available (no API key configured)");
            return "{\"error\": \"DeepSeek API not configured\"}";
        }

        // ---- Circuit breaker check ----
        if (circuitBreakerState == CircuitBreakerState.OPEN) {
            if (System.currentTimeMillis() - lastFailureTime >= cooldownMillis) {
                log.info("Circuit breaker cooldown elapsed; transitioning to HALF_OPEN for probe call");
                circuitBreakerState = CircuitBreakerState.HALF_OPEN;
            } else {
                log.warn("Circuit breaker OPEN; fast-failing request for prompt key: {}", promptKey);
                return "{\"error\": \"DeepSeek API circuit breaker is open\", \"circuitBreakerOpen\": true}";
            }
        }

        // Load prompt from catalog
        PromptCatalog.PromptEntry entry = promptCatalog.getPrompt(promptKey, context);
        if (entry == null) {
            log.warn("Unknown prompt key: {}", promptKey);
            return "{\"error\": \"Unknown prompt key: " + promptKey + "\"}";
        }

        String systemPrompt = entry.getSystemPrompt();

        // Build user message from context
        StringBuilder userContent = new StringBuilder();
        userContent.append("Project: ").append(context != null ? context.getOrDefault("projectTitle", "Untitled") : "Untitled").append("\n");
        if (context != null && context.containsKey("changeDescription")) {
            userContent.append("Change Description: ").append(context.get("changeDescription")).append("\n");
        }
        if (context != null && context.containsKey("requirements")) {
            userContent.append("Requirements: ").append(context.get("requirements")).append("\n");
        }
        if (context != null && context.containsKey("acceptanceCriteria")) {
            userContent.append("Acceptance Criteria: ").append(context.get("acceptanceCriteria")).append("\n");
        }
        if (context != null && context.containsKey("architectureSpec")) {
            userContent.append("Architecture Spec: ").append(context.get("architectureSpec")).append("\n");
        }
        userContent.append("Respond only with valid JSON matching the expected schema.");

        // Build DeepSeek API request
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("max_tokens", maxTokens);
        requestBody.put("temperature", temperature);

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));
        messages.add(Map.of("role", "user", "content", userContent.toString()));
        requestBody.put("messages", messages);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);

            log.debug("Sending request to DeepSeek API for prompt key: {}", promptKey);
            ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // ---- Success: reset circuit breaker ----
                if (circuitBreakerState == CircuitBreakerState.HALF_OPEN) {
                    log.info("Probe call succeeded; resetting circuit breaker to CLOSED");
                }
                circuitBreakerState = CircuitBreakerState.CLOSED;
                consecutiveFailures.set(0);

                String responseBody = response.getBody();
                log.debug("DeepSeek API responded successfully for prompt key: {}", promptKey);

                // Parse the chat completion response to extract the content
                try {
                    var root = objectMapper.readTree(responseBody);
                    var choice = root.get("choices").get(0);
                    var message = choice.get("message");
                    String content = message.get("content").asText();
                    log.info("DeepSeek returned {} chars for prompt key: {}", content.length(), promptKey);
                    return content;
                } catch (Exception e) {
                    log.error("Failed to parse DeepSeek response: {}", e.getMessage());
                    return responseBody; // Return raw response as fallback
                }
            } else {
                log.error("DeepSeek API returned {}: {}", response.getStatusCode(), response.getBody());
                return recordFailure("{\"error\": \"DeepSeek API error: " + response.getStatusCodeValue()
                        + " - " + (response.getBody() != null ? response.getBody() : "unknown") + "\"}");
            }
        } catch (Exception e) {
            log.error("DeepSeek API call failed for prompt key {}: {}", promptKey, e.getMessage());
            return recordFailure("{\"error\": \"DeepSeek API call failed: " + e.getMessage() + "\"}");
        }
    }

    /**
     * Record a failure and potentially open the circuit breaker.
     * Returns the error payload unchanged so callers still receive it.
     */
    private String recordFailure(String errorPayload) {
        lastFailureTime = System.currentTimeMillis();
        int failures = consecutiveFailures.incrementAndGet();
        if (circuitBreakerState == CircuitBreakerState.HALF_OPEN
                || failures >= failureThreshold) {
            log.warn("Circuit breaker OPEN after {} consecutive failure(s)", failures);
            circuitBreakerState = CircuitBreakerState.OPEN;
        }
        return errorPayload;
    }

    @Override
    public boolean isAvailable() {
        return apiKey != null && !apiKey.isBlank();
    }
}
