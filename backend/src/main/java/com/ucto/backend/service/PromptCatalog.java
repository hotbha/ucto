package com.ucto.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Loads canonical agent prompts from agent_prompts.yaml and makes them
 * available by prompt key (BA_REQUIREMENTS, ARCHITECT_DESIGN, etc.).
 *
 * Supports template variable substitution using {{variable}} syntax.
 * See docs/agent_prompts.md for prompt definitions and available variables.
 */
@Service
public class PromptCatalog {

    private static final Logger log = LoggerFactory.getLogger(PromptCatalog.class);

    private final Map<String, PromptEntry> prompts = new ConcurrentHashMap<>();

    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

    @PostConstruct
    public void loadPrompts() {
        try {
            ClassPathResource resource = new ClassPathResource("agent_prompts.yaml");
            if (!resource.exists()) {
                log.warn("agent_prompts.yaml not found on classpath; prompts will be empty");
                return;
            }

            try (InputStream is = resource.getInputStream()) {
                JsonNode root = yamlMapper.readTree(is);
                Iterator<String> fieldNames = root.fieldNames();
                while (fieldNames.hasNext()) {
                    String key = fieldNames.next();
                    JsonNode entry = root.get(key);
                    String systemPrompt = entry.has("systemPrompt")
                            ? entry.get("systemPrompt").asText() : "";
                    String expectedSchema = entry.has("expectedSchema")
                            ? entry.get("expectedSchema").toString() : "{}";
                    prompts.put(key, new PromptEntry(key, systemPrompt, expectedSchema));
                    log.debug("Loaded prompt: {}", key);
                }
            }

            log.info("Loaded {} agent prompts from agent_prompts.yaml", prompts.size());
        } catch (Exception e) {
            log.error("Failed to load agent prompts", e);
        }
    }

    /**
     * Get a prompt entry by key.
     *
     * @param key Prompt key (e.g. "BA_REQUIREMENTS", "DEV_IMPLEMENT")
     * @return PromptEntry, or null if not found
     */
    public PromptEntry getPrompt(String key) {
        return prompts.get(key);
    }

    /**
     * Get a prompt and substitute template variables.
     *
     * @param key        Prompt key
     * @param variables  Map of variable name -> value (without the {{}} delimiters)
     * @return PromptEntry with substituted system prompt, or null if key not found
     */
    public PromptEntry getPrompt(String key, Map<String, String> variables) {
        PromptEntry entry = prompts.get(key);
        if (entry == null) return null;

        String substituted = entry.systemPrompt;
        if (variables != null) {
            for (Map.Entry<String, String> var : variables.entrySet()) {
                substituted = substituted.replace("{{" + var.getKey() + "}}", var.getValue() != null ? var.getValue() : "");
            }
        }

        return new PromptEntry(entry.key, substituted, entry.expectedSchema);
    }

    /**
     * Check if a prompt key exists.
     */
    public boolean hasPrompt(String key) {
        return prompts.containsKey(key);
    }

    /**
     * Get the number of loaded prompts.
     */
    public int size() {
        return prompts.size();
    }

    /**
     * A single prompt entry with key, system prompt text, and expected JSON schema.
     */
    public static class PromptEntry {
        private final String key;
        private final String systemPrompt;
        private final String expectedSchema;

        public PromptEntry(String key, String systemPrompt, String expectedSchema) {
            this.key = key;
            this.systemPrompt = systemPrompt;
            this.expectedSchema = expectedSchema;
        }

        public String getKey() { return key; }
        public String getSystemPrompt() { return systemPrompt; }
        public String getExpectedSchema() { return expectedSchema; }
    }
}
