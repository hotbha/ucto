package com.ucto.backend.controller;

import com.ucto.backend.dto.BootstrapRequestDTO;
import com.ucto.backend.dto.BootstrapResultDTO;
import com.ucto.backend.dto.OrchestratorRequest;
import com.ucto.backend.dto.OrchestratorResponse;
import com.ucto.backend.service.BootstrapService;
import com.ucto.backend.service.OrchestratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


/**
 * Orchestrator REST controller.
 * Decides which loop to run, routes messages, and coordinates agents.
 */
@RestController
@RequestMapping("/api/orchestrator")
public class OrchestratorController {

    @Autowired
    private OrchestratorService orchestratorService;

    /**
     * Execute an orchestrator action.
     * POST /api/orchestrator/action
     */
    @PostMapping("/action")
    public ResponseEntity<?> executeAction(@RequestBody OrchestratorRequest request) {
        try {
            OrchestratorResponse response = orchestratorService.executeAction(request);
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get loop status for a project.
     * GET /api/orchestrator/status/{projectId}
     */
    @GetMapping("/status/{projectId}")
    public ResponseEntity<OrchestratorResponse> getLoopStatus(@PathVariable Long projectId) {
        OrchestratorRequest request = new OrchestratorRequest();
        request.setProjectId(projectId);
        request.setAction("GET_LOOP_STATUS");
        return ResponseEntity.ok(orchestratorService.executeAction(request));
    }

    /**
     * Evaluate next loop for a project.
     * GET /api/orchestrator/evaluate/{projectId}
     */
    @GetMapping("/evaluate/{projectId}")
    public ResponseEntity<OrchestratorResponse> evaluateNextLoop(@PathVariable Long projectId) {
        return ResponseEntity.ok(orchestratorService.evaluateNextLoop(projectId));
    }

    // ── Bootstrap endpoint ──

    @Autowired
    private BootstrapService bootstrapService;

    /**
     * Bootstrap a new project from a natural-language prompt.
     * POST /api/projects/bootstrap
     *
     * Creates a new project, generates a Spring Boot + React skeleton,
     * configures repo fields, and publishes agent events as defined in
     * docs/prompt_to_app_bootstrap_design.md.
     */
    @PostMapping("/bootstrap")
    public ResponseEntity<?> bootstrapProject(@RequestBody BootstrapRequestDTO request,
                                               Authentication auth) {
        try {
            if (request.getPrompt() == null || request.getPrompt().isBlank()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Prompt is required"));
            }

            Long userId = getUserId(auth);

            BootstrapResultDTO result = bootstrapService.bootstrap(
                    request.getPrompt(),
                    request.getTargetStack(),
                    userId
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    private Long getUserId(Authentication auth) {
        if (auth != null && auth.getDetails() instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> details = (Map<String, Object>) auth.getDetails();
            Object userId = details.get("userId");
            if (userId instanceof Number) {
                return ((Number) userId).longValue();
            }
        }
        throw new RuntimeException("User not authenticated");
    }
}

