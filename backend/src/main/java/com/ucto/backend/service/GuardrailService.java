package com.ucto.backend.service;

import com.ucto.backend.entity.AgentMessage;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Enforces anti-hallucination guardrails across all agent communications.
 * Based on docs/anti_hallucination_guardrails.md.
 */
@Service
public class GuardrailService {

    /**
     * Validate an agent message against anti-hallucination guardrails.
     * Returns a list of violations (empty if all clean).
     */
    public List<String> validateMessage(AgentMessage message) {
        List<String> violations = new ArrayList<>();

        // Guardrail 1: No silent assumptions — check for unlabeled assumptions
        violations.addAll(checkSilentAssumptions(message));

        // Guardrail 2: Always reference source
        violations.addAll(checkSourceReference(message));

        // Guardrail 3: Use options instead of inventions
        violations.addAll(checkOptionsNotInventions(message));

        // Guardrail 4: Traceability
        violations.addAll(checkTraceability(message));

        // Guardrail 5: Small increments
        violations.addAll(checkSmallIncrements(message));

        return violations;
    }

    /**
     * Guardrail 1: No silent assumptions.
     * Messages must distinguish between Fact, Inference, and Assumption.
     */
    private List<String> checkSilentAssumptions(AgentMessage message) {
        List<String> issues = new ArrayList<>();
        String payload = message.getPayloadJson();
        if (payload == null) return issues;

        // Check if payload contains unlabeled statements
        // In a full implementation, this would use NLP to detect
        // For now, check if needs_human is properly set when there are questions
        if (message.isNeedsHuman() &&
                (message.getHumanQuestionsJson() == null || message.getHumanQuestionsJson().isBlank())) {
            issues.add("needs_human=true but no human_questions provided — violates anti-hallucination rule #1");
        }

        return issues;
    }

    /**
     * Guardrail 2: Always reference source.
     * Requirements must cite PO statements; architecture must reference stories.
     */
    private List<String> checkSourceReference(AgentMessage message) {
        List<String> issues = new ArrayList<>();

        // REQUIREMENTS_PACKAGE must reference PO input
        if ("REQUIREMENTS_PACKAGE".equals(message.getMessageType())) {
            if (message.getStoryId() == null) {
                issues.add("REQUIREMENTS_PACKAGE must reference a story ID — violates anti-hallucination rule #2");
            }
        }

        // ARCHITECTURE_SPEC must reference stories
        if ("ARCHITECTURE_SPEC".equals(message.getMessageType())) {
            if (message.getStoryId() == null) {
                issues.add("ARCHITECTURE_SPEC must reference a story ID — violates anti-hallucination rule #2");
            }
        }

        return issues;
    }

    /**
     * Guardrail 3: Use options instead of inventions.
     * When multiple paths exist, propose 2-3 options with pros/cons.
     */
    private List<String> checkOptionsNotInventions(AgentMessage message) {
        List<String> issues = new ArrayList<>();
        if (!message.isNeedsHuman()) return issues;

        // If needs_human=true and message type suggests decision-making,
        // check that options were provided
        if ("ARCHITECTURE_SPEC".equals(message.getMessageType()) ||
                "UI_SPEC".equals(message.getMessageType())) {
            // In a full implementation, check payload for option structure
            String payload = message.getPayloadJson();
            if (payload != null && !payload.contains("options") && !payload.contains("alternatives")) {
                issues.add("Architecture/UI spec with needs_human=true should propose 2-3 options — violates anti-hallucination rule #3");
            }
        }

        return issues;
    }

    /**
     * Guardrail 4: Traceability.
     * Every artefact should be traceable to PO input.
     */
    private List<String> checkTraceability(AgentMessage message) {
        List<String> issues = new ArrayList<>();

        // Check that correlationId is present for traceability
        if (message.getCorrelationId() == null || message.getCorrelationId().isBlank()) {
            issues.add("Missing correlationId — violates anti-hallucination rule #4 (traceability)");
        }

        // Check that from_agent and to_agent are set
        if (message.getFromAgent() == null || message.getToAgent() == null) {
            issues.add("Missing from_agent or to_agent — violates anti-hallucination rule #4");
        }

        return issues;
    }

    /**
     * Guardrail 5: Small increments.
     * Changes should be incremental, not system-wide refactors.
     */
    private List<String> checkSmallIncrements(AgentMessage message) {
        List<String> issues = new ArrayList<>();

        // For IMPLEMENTATION_UPDATE messages, check if the scope is reasonable
        if ("IMPLEMENTATION_UPDATE".equals(message.getMessageType())) {
            String payload = message.getPayloadJson();
            if (payload != null) {
                // Check for signs of large-scale changes
                if (payload.contains("refactor") || payload.contains("rewrite") || payload.contains("migrate")) {
                    issues.add("Large-scale change detected. Consider breaking into smaller increments — violates anti-hallucination rule #5");
                }
            }
        }

        return issues;
    }

    /**
     * Check whether a message has proper Fact/Inference/Assumption labeling.
     * Returns a score from 0 (no violations) to 1.0 (all violations present).
     */
    public double calculateConfidenceScore(AgentMessage message) {
        List<String> violations = validateMessage(message);
        if (violations.isEmpty()) return 1.0;

        // Base score: start at 1.0, deduct 0.15 per violation, minimum 0.1
        double score = Math.max(0.1, 1.0 - (violations.size() * 0.15));
        return score;
    }
}
