package com.ucto.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

/**
 * Default stub implementation of LLMAgentClient.
 * Returns placeholder JSON responses matching the expected schemas from
 * docs/agent_prompts.md. Used for MVP when no real LLM endpoint is configured.
 *
 * Conditionally active when no other LLMAgentClient bean is defined.
 */
@Component
@ConditionalOnMissingBean(LLMAgentClient.class)
public class StubLLMAgentClient implements LLMAgentClient {

    private static final Logger log = LoggerFactory.getLogger(StubLLMAgentClient.class);

    @Autowired
    private PromptCatalog promptCatalog;

    @Override
    public String execute(String promptKey, Map<String, String> context) {
        log.info("StubLLMAgentClient.execute('{}') called with {} context vars",
                promptKey, context != null ? context.size() : 0);

        if (!promptCatalog.hasPrompt(promptKey)) {
            log.warn("Unknown prompt key: {}", promptKey);
            return "{\"error\": \"Unknown prompt key: " + promptKey + "\"}";
        }

        // Return dummy responses matching each prompt's expected schema
        switch (promptKey) {
            case "BA_REQUIREMENTS":
                return baRequirementsStub(context);
            case "ARCHITECT_DESIGN":
                return architectDesignStub(context);
            case "DEV_IMPLEMENT":
                return devImplementStub(context);
            case "TEST_GENERATE":
                return testGenerateStub(context);
            case "COMPLIANCE_CHECK":
                return complianceCheckStub(context);
            default:
                return "{\"error\": \"No stub for prompt key: " + promptKey + "\"}";
        }
    }

    @Override
    public boolean isAvailable() {
        return true; // Stub is always available
    }

    private String baRequirementsStub(Map<String, String> context) {
        String projectTitle = context != null ? context.getOrDefault("projectTitle", "Project") : "Project";
        return String.format("""
                {
                  "epics": [
                    {
                      "id": "EPIC-001",
                      "title": "%s Core Features",
                      "description": "Core functionality for %s",
                      "stories": [
                        {
                          "id": "STORY-001",
                          "asA": "User",
                          "iWant": "to access the %s dashboard",
                          "soThat": "I can manage my tasks",
                          "acceptanceCriteria": ["AC-1: Dashboard loads in under 2s", "AC-2: Shows user-specific data"]
                        },
                        {
                          "id": "STORY-002",
                          "asA": "Admin",
                          "iWant": "to configure %s settings",
                          "soThat": "I can customize the experience",
                          "acceptanceCriteria": ["AC-1: Settings persist across sessions", "AC-2: Changes take effect immediately"]
                        }
                      ]
                    }
                  ],
                  "nonFunctionalRequirements": ["NFR-1: Response time under 500ms for 95%% of requests"],
                  "clarifications": []
                }
                """, projectTitle, projectTitle, projectTitle, projectTitle).strip();
    }

    private String architectDesignStub(Map<String, String> context) {
        return """
                {
                  "feasible": true,
                  "options": [
                    {
                      "name": "Standard Spring Boot + React",
                      "pros": ["Well-known stack", "Fast development"],
                      "cons": ["Requires PostgreSQL setup"],
                      "recommended": true
                    }
                  ],
                  "components": [
                    {"name": "API Layer", "technology": "Spring Boot", "complexity": "Low", "notes": "REST endpoints"},
                    {"name": "Frontend", "technology": "React + TypeScript", "complexity": "Medium", "notes": "Vite build"},
                    {"name": "Database", "technology": "PostgreSQL", "complexity": "Low", "notes": "Flyway migrations"}
                  ],
                  "assumptions": ["PostgreSQL 16 available"],
                  "tradeOffs": ["Monolith vs microservices"],
                  "risks": ["None identified"],
                  "needsHuman": false
                }
                """.strip();
    }

    private String devImplementStub(Map<String, String> context) {
        return String.format("""
                {
                  "storyId": "STORY-001",
                  "acAddressed": ["AC-1", "AC-2"],
                  "changesSummary": "Implemented %s features",
                  "filesChanged": [
                    {"path": "src/main/java/com/example/controller/Controller.java", "action": "CREATE", "summary": "Added REST endpoints"},
                    {"path": "src/main/java/com/example/service/Service.java", "action": "CREATE", "summary": "Business logic"}
                  ],
                  "testCoverage": 85,
                  "needsHuman": false,
                  "humanQuestions": []
                }
                """, context != null ? context.getOrDefault("projectTitle", "") : "").strip();
    }

    private String testGenerateStub(Map<String, String> context) {
        return """
                {
                  "storyId": "STORY-001",
                  "testsRun": 6,
                  "passed": 5,
                  "failed": 1,
                  "skipped": 0,
                  "coveragePercent": 75.0,
                  "failures": [
                    {
                      "testCase": "test_createWithMissingFields",
                      "expected": "400 Bad Request",
                      "actual": "500 Internal Server Error",
                      "assignedTo": "developer"
                    }
                  ],
                  "overallStatus": "needs_fix",
                  "doDMet": false
                }
                """.strip();
    }

    private String complianceCheckStub(Map<String, String> context) {
        return """
                {
                  "riskLevel": "low",
                  "findings": [],
                  "overallStatus": "pass",
                  "needsHuman": false,
                  "humanQuestions": []
                }
                """.strip();
    }
}
