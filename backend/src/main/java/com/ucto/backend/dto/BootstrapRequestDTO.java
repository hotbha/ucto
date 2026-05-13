package com.ucto.backend.dto;

/**
 * Request DTO for the POST /api/projects/bootstrap endpoint.
 * Accepts a natural-language prompt and optional stack identifier.
 */
public class BootstrapRequestDTO {

    private String prompt;
    private String targetStack; // Default: "SPRING_REACT"

    public BootstrapRequestDTO() {}

    public BootstrapRequestDTO(String prompt, String targetStack) {
        this.prompt = prompt;
        this.targetStack = targetStack;
    }

    public String getPrompt() { return prompt; }
    public void setPrompt(String prompt) { this.prompt = prompt; }

    public String getTargetStack() { return targetStack; }
    public void setTargetStack(String targetStack) { this.targetStack = targetStack; }
}
