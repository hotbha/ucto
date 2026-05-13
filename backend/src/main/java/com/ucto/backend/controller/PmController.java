package com.ucto.backend.controller;

import com.ucto.backend.dto.PmRequest;
import com.ucto.backend.dto.PmResponse;
import com.ucto.backend.service.ProjectManagerService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * PM/Scrum Master REST controller.
 * Handles backlog management, sprint lifecycle, loop coordination, DoR/DoD enforcement.
 */
@RestController
@RequestMapping("/api/pm")
public class PmController {

    @Autowired
    private ProjectManagerService projectManagerService;

    /**
     * Execute a PM action.
     * POST /api/pm/action
     */
    @PostMapping("/action")
    public ResponseEntity<?> executeAction(
            @RequestBody PmRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) authentication.getPrincipal();
            PmResponse response = projectManagerService.executeAction(
                    request, userId, httpRequest.getRemoteAddr());
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
     * Get backlog for a project.
     * GET /api/pm/backlog/{projectId}
     */
    @GetMapping("/backlog/{projectId}")
    public ResponseEntity<PmResponse> getBacklog(
            @PathVariable Long projectId) {
        PmRequest request = new PmRequest();
        request.setProjectId(projectId);
        request.setAction("GET_BACKLOG");
        return ResponseEntity.ok(projectManagerService.executeAction(request, null, ""));
    }

    /**
     * Get sprints for a project.
     * GET /api/pm/sprints/{projectId}
     */
    @GetMapping("/sprints/{projectId}")
    public ResponseEntity<PmResponse> getSprints(
            @PathVariable Long projectId) {
        PmRequest request = new PmRequest();
        request.setProjectId(projectId);
        request.setAction("GET_SPRINTS");
        return ResponseEntity.ok(projectManagerService.executeAction(request, null, ""));
    }
}
