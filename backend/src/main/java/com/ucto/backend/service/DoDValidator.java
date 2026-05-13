package com.ucto.backend.service;

import com.ucto.backend.entity.BacklogItem;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Enforces Definition of Done (DoD) before a story can be marked as "Done".
 * Based on docs/definition_of_ready_done.md.
 */
@Service
public class DoDValidator {

    /**
     * Validate all DoD criteria for a backlog item.
     * Returns a map of criterion -> (PASSED, FAILED, NOT_APPLICABLE) with details.
     */
    public Map<String, Object> validate(BacklogItem item, Map<String, Object> testResults) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> checklist = new ArrayList<>();
        boolean allPassed = true;

        // Criterion 1: All acceptance criteria pass
        boolean allAcPass = testResults != null &&
                (boolean) testResults.getOrDefault("allAcceptanceCriteriaPassed", false);
        checklist.add(checkCriterion("All acceptance criteria pass",
                allAcPass,
                "Every acceptance criterion must have a passing test"));

        // Criterion 2: Code reviewed
        boolean codeReviewed = testResults != null &&
                (boolean) testResults.getOrDefault("codeReviewed", false);
        checklist.add(checkCriterion("Code reviewed",
                codeReviewed,
                "Code must be reviewed by another team member"));

        // Criterion 3: All tests pass (unit, integration, e2e)
        boolean allTestsPass = testResults != null &&
                (boolean) testResults.getOrDefault("allTestsPass", false);
        checklist.add(checkCriterion("All tests pass (unit, integration, e2e)",
                allTestsPass,
                "Zero test failures"));

        // Criterion 4: No P0/P1 defects
        boolean noCriticalDefects = testResults != null &&
                (boolean) testResults.getOrDefault("noCriticalDefects", false);
        checklist.add(checkCriterion("No P0/P1 defects open",
                noCriticalDefects,
                "All critical and high-priority defects must be resolved"));

        // Criterion 5: Documentation updated
        boolean docUpdated = testResults != null &&
                (boolean) testResults.getOrDefault("documentationUpdated", false);
        checklist.add(checkCriterion("Documentation updated",
                docUpdated,
                "Living docs must reflect the implemented feature"));

        // Criterion 6: PO acceptance
        boolean poAccepted = testResults != null &&
                (boolean) testResults.getOrDefault("poAccepted", false);
        checklist.add(checkCriterion("PO acceptance",
                poAccepted,
                "Product Owner must accept the completed story via BA"));

        // Criterion 7: Meets Definition of Ready (regression check)
        boolean dorStillValid = testResults != null &&
                (boolean) testResults.getOrDefault("dorStillValid", true);
        checklist.add(checkCriterion("DoR still valid",
                dorStillValid,
                "Requirements should not have changed since DoR was approved"));

        for (Map<String, Object> c : checklist) {
            if ("FAILED".equals(c.get("status"))) {
                allPassed = false;
                break;
            }
        }

        result.put("allPassed", allPassed);
        result.put("checklist", checklist);
        result.put("storyId", item.getId());
        result.put("storyTitle", item.getTitle());

        return result;
    }

    private Map<String, Object> checkCriterion(String name, boolean passed, String guidance) {
        Map<String, Object> criterion = new HashMap<>();
        criterion.put("name", name);
        criterion.put("status", passed ? "PASSED" : "FAILED");
        criterion.put("guidance", guidance);
        return criterion;
    }
}
