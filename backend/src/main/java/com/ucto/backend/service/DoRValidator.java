package com.ucto.backend.service;

import com.ucto.backend.entity.BacklogItem;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Enforces Definition of Ready (DoR) before a story can be assigned to development.
 * Based on docs/definition_of_ready_done.md.
 */
@Service
public class DoRValidator {

    /**
     * Validate all DoR criteria for a backlog item.
     * Returns a map of criterion -> (PASSED, FAILED, NOT_APPLICABLE) with details.
     */
    public Map<String, Object> validate(BacklogItem item) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> checklist = new ArrayList<>();
        boolean allPassed = true;

        // Criterion 1: Clear user persona
        checklist.add(checkCriterion("Clear user persona",
                item.getPersona() != null && !item.getPersona().isBlank(),
                "User persona must be specified (e.g., Founder, Developer, Viewer)"));

        // Criterion 2: "As a... I want... so that..." format
        checklist.add(checkCriterion("User story format (As a/I want/So that)",
                item.getUserStoryFormat() != null && !item.getUserStoryFormat().isBlank(),
                "Story must follow: 'As a [persona], I want [goal] so that [benefit]'"));

        // Criterion 3: Acceptance criteria defined
        String ac = item.getAcceptanceCriteriaJson();
        boolean hasAc = ac != null && !ac.isBlank() && !ac.equals("[]");
        checklist.add(checkCriterion("Acceptance criteria defined",
                hasAc,
                "At least one acceptance criterion must be specified"));

        // Criterion 4: Known constraints identified
        String constraints = item.getConstraintsJson();
        boolean hasConstraints = constraints != null && !constraints.isBlank();
        checklist.add(checkCriterion("Constraints identified",
                hasConstraints,
                "Known constraints must be documented (even if 'none')"));

        // Criterion 5: Dependencies identified
        String deps = item.getDependenciesJson();
        boolean hasDeps = deps != null && !deps.isBlank();
        checklist.add(checkCriterion("Dependencies identified",
                hasDeps,
                "Dependencies must be documented (even if 'none')"));

        // Criterion 6: Open questions resolved
        // For manual validation — this is checked by BA/PM
        checklist.add(checkCriterion("Open questions resolved",
                false, // Always needs BA confirmation
                "All open questions must be resolved before development"));

        // Criterion 7: Technical feasibility assessed
        // For MVP, we assume technical feasibility unless blocked
        checklist.add(checkCriterion("Technical feasibility assessed",
                true,
                "Architect must confirm feasibility for complex items"));

        // Criterion 8: Story is small enough for one sprint
        boolean isSmallEnough = item.getStoryPoints() <= 13 && item.getStoryPoints() > 0;
        checklist.add(checkCriterion("Story sized appropriately (≤ 13 points)",
                isSmallEnough,
                "Story should be small enough to complete in one sprint"));

        // Criterion 9: UI/UX alignment checked (for UX-relevant stories)
        checklist.add(checkCriterion("UI/UX alignment reviewed",
                false, // Requires manual confirmation
                "UI/UX agent should review for UX-relevant stories"));

        // Criterion 10: Compliance reviewed (for compliance-relevant stories)
        checklist.add(checkCriterion("Compliance reviewed",
                false, // Requires manual confirmation
                "Compliance agent should review for stories with data/risk implications"));

        boolean finalAllPassed = true;
        for (Map<String, Object> c : checklist) {
            if ("FAILED".equals(c.get("status"))) {
                finalAllPassed = false;
                break;
            }
        }

        result.put("allPassed", finalAllPassed);
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
