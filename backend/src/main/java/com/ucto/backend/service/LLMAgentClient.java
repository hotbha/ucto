package com.ucto.backend.service;

import java.util.Map;

/**
 * Interface for invoking LLM agents with canonical prompts from PromptCatalog.
 *
 * Implementations can be stubs (for MVP/testing), OpenAI-compatible API clients,
 * or local model wrappers. The interface is intentionally simple to allow
 * swapping implementations without changing agent logic.
 *
 * See docs/agent_prompts.md for prompt definitions.
 */
public interface LLMAgentClient {

    /**
     * Execute an agent with a given prompt key and context variables.
     *
     * @param promptKey The prompt key (e.g. "BA_REQUIREMENTS", "DEV_IMPLEMENT")
     * @param context   Map of template variables (projectTitle, requirements, etc.)
     * @return The LLM response as a raw JSON string
     */
    String execute(String promptKey, Map<String, String> context);

    /**
     * Check whether the client is connected/available.
     */
    boolean isAvailable();
}
