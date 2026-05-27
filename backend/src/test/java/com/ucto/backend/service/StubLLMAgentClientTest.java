package com.ucto.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for StubLLMAgentClient.
 * Verifies that stub responses match expected JSON schemas.
 */
@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class StubLLMAgentClientTest {

    @Mock
    private PromptCatalog promptCatalog;

    private StubLLMAgentClient client;

    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        // Mock promptCatalog to always "have" the keys
        when(promptCatalog.hasPrompt(anyString())).thenReturn(true);
        client = new StubLLMAgentClient();
        ReflectionTestUtils.setField(client, "promptCatalog", promptCatalog);
    }

    @Test
    void testIsAvailable() {
        assertTrue(client.isAvailable());
    }

    @Test
    void testBaRequirements_returnsValidJson() throws Exception {
        Map<String, String> context = new HashMap<>();
        context.put("projectTitle", "Task Manager");

        String result = client.execute("BA_REQUIREMENTS", context);
        assertNotNull(result);

        var json = mapper.readTree(result);
        assertTrue(json.has("epics"));
        assertTrue(json.get("epics").isArray());
        assertTrue(json.get("epics").get(0).has("stories"));
    }

    @Test
    void testBaRequirements_containsProjectTitle() throws Exception {
        Map<String, String> context = new HashMap<>();
        context.put("projectTitle", "MyApp");

        String result = client.execute("BA_REQUIREMENTS", context);
        assertTrue(result.contains("MyApp"));
    }

    @Test
    void testArchitectDesign_returnsValidJson() throws Exception {
        String result = client.execute("ARCHITECT_DESIGN", null);
        var json = mapper.readTree(result);
        assertTrue(json.has("feasible"));
        assertTrue(json.has("options"));
        assertTrue(json.has("components"));
    }

    @Test
    void testDevImplement_returnsValidJson() throws Exception {
        String result = client.execute("DEV_IMPLEMENT", null);
        var json = mapper.readTree(result);
        assertTrue(json.has("storyId"));
        assertTrue(json.has("filesChanged"));
        assertTrue(json.has("testCoverage"));
    }

    @Test
    void testTestGenerate_returnsValidJson() throws Exception {
        String result = client.execute("TEST_GENERATE", null);
        var json = mapper.readTree(result);
        assertTrue(json.has("testsRun"));
        assertTrue(json.has("passed"));
        assertTrue(json.has("failed"));
        assertTrue(json.has("overallStatus"));
    }

    @Test
    void testComplianceCheck_returnsValidJson() throws Exception {
        String result = client.execute("COMPLIANCE_CHECK", null);
        var json = mapper.readTree(result);
        assertTrue(json.has("riskLevel"));
        assertTrue(json.has("findings"));
        assertTrue(json.has("overallStatus"));
    }

    @Test
    void testUnknownKey_returnsError() throws Exception {
        String result = client.execute("UNKNOWN_KEY", null);
        var json = mapper.readTree(result);
        assertTrue(json.has("error"));
    }
}
