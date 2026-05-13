package com.ucto.backend.service;

import com.ucto.backend.entity.AgentMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Validates that agent messages include proper source references.
 * Ensures every requirement traces back to PO input or agreed artefacts.
 * Based on docs/anti_hallucination_guardrails.md §Guardrail 2.
 */
@Service
public class SourceReferenceValidator {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Validate that a message payload contains proper source citations.
     * Returns a list of missing references.
     */
    public List<String> validateReferences(AgentMessage message) {
        List<String> missingRefs = new ArrayList<>();

        if (message.getPayloadJson() == null || message.getPayloadJson().isBlank()) {
            return missingRefs;
        }

        try {
            Map<String, Object> payload = objectMapper.readValue(
                    message.getPayloadJson(),
                    Map.class);

            // Check for statements that lack source citations
            if (payload.containsKey("requirements")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> reqs = (List<Map<String, Object>>) payload.get("requirements");
                for (Map<String, Object> req : reqs) {
                    if (!req.containsKey("source") || req.get("source") == null) {
                        missingRefs.add("Requirement \"" + req.getOrDefault("title", "unknown") +
                                "\" lacks a source reference");
                    }
                }
            }

            // Check for decisions that aren't traced
            if (payload.containsKey("decisions")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> decisions = (List<Map<String, Object>>) payload.get("decisions");
                for (Map<String, Object> decision : decisions) {
                    if (!decision.containsKey("rationale") || decision.get("rationale") == null) {
                        missingRefs.add("Decision \"" + decision.getOrDefault("name", "unknown") +
                                "\" lacks a rationale");
                    }
                }
            }

            // Check for tech choices without rationale
            if (payload.containsKey("technologyChoices")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> techChoices = (List<Map<String, Object>>) payload.get("technologyChoices");
                for (Map<String, Object> choice : techChoices) {
                    if (!choice.containsKey("rationale") || choice.get("rationale") == null) {
                        missingRefs.add("Technology choice \"" + choice.getOrDefault("name", "unknown") +
                                "\" lacks a rationale");
                    }
                }
            }

        } catch (Exception e) {
            // If we can't parse the payload, flag it
            missingRefs.add("Cannot parse payload JSON to validate source references");
        }

        return missingRefs;
    }

    /**
     * Add a source citation to a payload.
     */
    public Map<String, Object> addSourceCitation(Map<String, Object> payload,
                                                   String sourceType,
                                                   String sourceId,
                                                   String sourceDescription) {
        Map<String, Object> citation = new HashMap<>();
        citation.put("sourceType", sourceType); // PO_STATEMENT, ARTEFACT, AGREED_CONSTRAINT
        citation.put("sourceId", sourceId);
        citation.put("sourceDescription", sourceDescription);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> citations = (List<Map<String, Object>>) payload.getOrDefault("sourceCitations", new ArrayList<>());
        citations.add(citation);
        payload.put("sourceCitations", citations);

        return payload;
    }
}
