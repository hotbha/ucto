package com.ucto.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for DeepSeekAgentClient.
 * Uses a mocked RestTemplate to verify request payloads and response parsing.
 */
@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class DeepSeekAgentClientTest {


    @Mock
    private RestTemplate restTemplate;

    @Mock
    private PromptCatalog promptCatalog;

    private DeepSeekAgentClient client;

    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        // Create client with mocked RestTemplate
        client = new DeepSeekAgentClient(restTemplate);
        ReflectionTestUtils.setField(client, "promptCatalog", promptCatalog);

        // Set config values via ReflectionTestUtils

        ReflectionTestUtils.setField(client, "apiKey", "sk-test-key");
        ReflectionTestUtils.setField(client, "apiUrl", "https://api.deepseek.com/v1/chat/completions");
        ReflectionTestUtils.setField(client, "model", "deepseek-chat");
        ReflectionTestUtils.setField(client, "maxTokens", 2048);
        ReflectionTestUtils.setField(client, "temperature", 0.3);
        ReflectionTestUtils.setField(client, "failureThreshold", 3);
        ReflectionTestUtils.setField(client, "cooldownMillis", 30000L);

        // Mock PromptCatalog
        when(promptCatalog.getPrompt(eq("TEST_PROMPT"), any()))
                .thenReturn(new PromptCatalog.PromptEntry(
                        "TEST_PROMPT",
                        "You are a test agent. Project: {{projectTitle}}",
                        "{}"
                ));
        when(promptCatalog.getPrompt(eq("UNKNOWN"), any())).thenReturn(null);
    }

    @Test
    void testIsAvailable_withKey() {
        assertTrue(client.isAvailable());
    }

    @Test
    void testIsAvailable_withoutKey() {
        ReflectionTestUtils.setField(client, "apiKey", "");
        assertFalse(client.isAvailable());
    }

    @Test
    void testExecute_unknownPrompt_returnsError() {
        String result = client.execute("UNKNOWN", null);
        assertTrue(result.contains("Unknown prompt key"));
    }

    @Test
    void testExecute_sendsCorrectRequestPayload() throws Exception {
        // Mock a successful DeepSeek API response
        String mockResponse = """
                {
                  "id": "chatcmpl-123",
                  "choices": [
                    {
                      "index": 0,
                      "message": {
                        "role": "assistant",
                        "content": "{\\"result\\": \\"success\\"}"
                      }
                    }
                  ]
                }
                """;

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        Map<String, String> context = new HashMap<>();
        context.put("projectTitle", "My Test App");
        context.put("changeDescription", "Add a feature");

        String result = client.execute("TEST_PROMPT", context);
        assertTrue(result.contains("success"));
    }

    @Test
    void testExecute_includesContextInUserMessage() throws Exception {
        String mockResponse = """
                {"id":"x","choices":[{"index":0,"message":{"role":"assistant","content":"ok"}}]}
                """;

        ArgumentCaptor<HttpEntity<String>> requestCaptor = ArgumentCaptor.forClass(HttpEntity.class);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                requestCaptor.capture(),
                eq(String.class)
        )).thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        Map<String, String> context = new HashMap<>();
        context.put("projectTitle", "My App");
        context.put("changeDescription", "Fix login bug");

        client.execute("TEST_PROMPT", context);

        // Verify the request body
        HttpEntity<String> captured = requestCaptor.getValue();
        String body = captured.getBody();
        assertNotNull(body);

        // Verify headers
        HttpHeaders headers = captured.getHeaders();
        assertEquals("Bearer sk-test-key", headers.getFirst("Authorization"));
        assertEquals(MediaType.APPLICATION_JSON.toString(), headers.getFirst("Content-Type"));

        // Verify body contains the expected fields
        var json = mapper.readTree(body);
        assertEquals("deepseek-chat", json.get("model").asText());
        assertEquals(2048, json.get("max_tokens").asInt());
        assertEquals(0.3, json.get("temperature").asDouble(), 0.01);

        // Verify messages
        var messages = json.get("messages");
        assertTrue(messages.isArray());
        assertEquals(2, messages.size());

        // System prompt should contain the substituted project title
        String systemContent = messages.get(0).get("content").asText();
        assertTrue(systemContent.contains("My App"));

        // User message should contain context
        String userContent = messages.get(1).get("content").asText();
        assertTrue(userContent.contains("My App"));
        assertTrue(userContent.contains("Fix login bug"));
    }

    @Test
    void testExecute_apiError_returnsErrorMessage() {
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(new ResponseEntity<>("Rate limit exceeded", HttpStatus.TOO_MANY_REQUESTS));

        String result = client.execute("TEST_PROMPT", null);
        assertTrue(result.contains("429"));
        assertTrue(result.contains("Rate limit exceeded"));
    }

    @Test
    void testExecute_exception_returnsErrorMessage() {
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenThrow(new RuntimeException("Connection timed out"));

        String result = client.execute("TEST_PROMPT", null);
        assertTrue(result.contains("Connection timed out"));
    }

    @Test
    void testExecute_nullApiKey_returnsNotConfigured() {
        ReflectionTestUtils.setField(client, "apiKey", "");
        String result = client.execute("TEST_PROMPT", null);
        assertTrue(result.contains("not configured"));
    }

    // ---- Circuit breaker tests ----

    @Test
    void testCircuitBreaker_startsClosed() {
        assertEquals(DeepSeekAgentClient.CircuitBreakerState.CLOSED, client.getCircuitBreakerState());
        assertEquals(0, client.getConsecutiveFailures());
    }

    @Test
    void testThreeConsecutiveFailures_opensCircuit() {
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenThrow(new RuntimeException("API unavailable"));

        // Three failures should open the circuit
        for (int i = 0; i < 3; i++) {
            client.execute("TEST_PROMPT", null);
        }
        assertEquals(DeepSeekAgentClient.CircuitBreakerState.OPEN, client.getCircuitBreakerState());

        // Fourth call should fast-fail without calling restTemplate
        reset(restTemplate);
        String result = client.execute("TEST_PROMPT", null);
        assertTrue(result.contains("circuit breaker is open"));
        assertTrue(result.contains("circuitBreakerOpen"));
        assertTrue(result.contains("true"));
        verify(restTemplate, never()).exchange(anyString(), any(), any(), any(Class.class));
    }

    @Test
    void testOpenCircuit_fastFailsWithoutHttpCall() {
        // Manually set circuit breaker to OPEN with recent failure time
        client.setCircuitBreakerState(DeepSeekAgentClient.CircuitBreakerState.OPEN);
        client.setLastFailureTime(System.currentTimeMillis());

        String result = client.execute("TEST_PROMPT", null);
        assertTrue(result.contains("circuit breaker is open"));
        assertTrue(result.contains("circuitBreakerOpen"));
        // Verify HTTP was never called
        verify(restTemplate, never()).exchange(anyString(), any(), any(), any(Class.class));
    }

    @Test
    void testCircuitResetsAfterCooldown_andSuccessfulCall() {
        // Start with OPEN state and a failure time older than cooldown
        client.setCircuitBreakerState(DeepSeekAgentClient.CircuitBreakerState.OPEN);
        client.setLastFailureTime(System.currentTimeMillis() - 60000L); // 60s ago, > 30s cooldown
        client.setConsecutiveFailures(3);

        String mockResponse = """
                {"id":"x","choices":[{"index":0,"message":{"role":"assistant","content":"ok"}}]}
                """;
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        String result = client.execute("TEST_PROMPT", null);
        assertTrue(result.contains("ok"));
        assertEquals(DeepSeekAgentClient.CircuitBreakerState.CLOSED, client.getCircuitBreakerState());
        assertEquals(0, client.getConsecutiveFailures());
    }

    @Test
    void testCircuitHalfOpen_failure_reopensCircuit() {
        // Start with OPEN state, cooldown elapsed → should become HALF_OPEN
        client.setCircuitBreakerState(DeepSeekAgentClient.CircuitBreakerState.OPEN);
        client.setLastFailureTime(System.currentTimeMillis() - 60000L);
        client.setConsecutiveFailures(3);

        // Make the probe call fail
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenThrow(new RuntimeException("Still down"));

        client.execute("TEST_PROMPT", null);

        // Should go back to OPEN with reset cooldown
        assertEquals(DeepSeekAgentClient.CircuitBreakerState.OPEN, client.getCircuitBreakerState());
    }
}
