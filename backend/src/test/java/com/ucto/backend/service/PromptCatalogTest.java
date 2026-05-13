package com.ucto.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for PromptCatalog loading and variable substitution.
 */
@ExtendWith(MockitoExtension.class)
class PromptCatalogTest {

    private PromptCatalog promptCatalog;

    @BeforeEach
    void setUp() {
        promptCatalog = new PromptCatalog();
        // Manually inject a test prompt
        Map<String, PromptCatalog.PromptEntry> prompts = new HashMap<>();
        prompts.put("BA_REQUIREMENTS", new PromptCatalog.PromptEntry(
                "BA_REQUIREMENTS",
                "You are a BA. Project: {{projectTitle}}. Description: {{projectDescription}}",
                "{}"
        ));
        prompts.put("TEST_GENERATE", new PromptCatalog.PromptEntry(
                "TEST_GENERATE",
                "You are a tester for {{projectTitle}}",
                "{}"
        ));
        ReflectionTestUtils.setField(promptCatalog, "prompts", prompts);
    }

    @Test
    void testGetPrompt_exists() {
        var entry = promptCatalog.getPrompt("BA_REQUIREMENTS");
        assertNotNull(entry);
        assertEquals("BA_REQUIREMENTS", entry.getKey());
        assertTrue(entry.getSystemPrompt().contains("Project:"));
    }

    @Test
    void testGetPrompt_notFound() {
        assertNull(promptCatalog.getPrompt("NONEXISTENT"));
    }

    @Test
    void testGetPrompt_withVariables() {
        Map<String, String> vars = new HashMap<>();
        vars.put("projectTitle", "My App");
        vars.put("projectDescription", "A test app");

        var entry = promptCatalog.getPrompt("BA_REQUIREMENTS", vars);
        assertNotNull(entry);
        assertTrue(entry.getSystemPrompt().contains("My App"));
        assertTrue(entry.getSystemPrompt().contains("A test app"));
    }

    @Test
    void testGetPrompt_withVariables_unsetVarRemains() {
        Map<String, String> vars = new HashMap<>();
        vars.put("projectTitle", "My App");

        var entry = promptCatalog.getPrompt("BA_REQUIREMENTS", vars);
        assertNotNull(entry);
        assertTrue(entry.getSystemPrompt().contains("My App"));
        assertTrue(entry.getSystemPrompt().contains("{{projectDescription}}")); // not substituted
    }

    @Test
    void testHasPrompt() {
        assertTrue(promptCatalog.hasPrompt("BA_REQUIREMENTS"));
        assertFalse(promptCatalog.hasPrompt("UNKNOWN"));
    }

    @Test
    void testSize() {
        assertEquals(2, promptCatalog.size());
    }

    @Test
    void testExpectedSchema() {
        var entry = promptCatalog.getPrompt("BA_REQUIREMENTS");
        assertNotNull(entry);
        assertEquals("{}", entry.getExpectedSchema());
    }
}
