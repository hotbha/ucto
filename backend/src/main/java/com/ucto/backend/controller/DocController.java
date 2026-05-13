package com.ucto.backend.controller;

import com.ucto.backend.dto.DocRequest;
import com.ucto.backend.dto.DocResponse;
import com.ucto.backend.service.DocumentationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Documentation Agent REST controller.
 * Handles generation, update, publishing, and archiving of living documentation.
 */
@RestController
@RequestMapping("/api/docs")
public class DocController {

    @Autowired
    private DocumentationService documentationService;

    /**
     * Execute a documentation action.
     * POST /api/docs/action
     */
    @PostMapping("/action")
    public ResponseEntity<?> executeAction(
            @RequestBody DocRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) authentication.getPrincipal();
            DocResponse response = documentationService.executeAction(
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
     * Get documents for a project.
     * GET /api/docs/project/{projectId}
     */
    @GetMapping("/project/{projectId}")
    public ResponseEntity<DocResponse> getByProject(@PathVariable Long projectId) {
        DocRequest request = new DocRequest();
        request.setProjectId(projectId);
        request.setAction("GET_BY_PROJECT");
        return ResponseEntity.ok(documentationService.executeAction(request, null, ""));
    }

    /**
     * Get documents by type for a project.
     * GET /api/docs/project/{projectId}/type/{docType}
     */
    @GetMapping("/project/{projectId}/type/{docType}")
    public ResponseEntity<DocResponse> getByType(
            @PathVariable Long projectId,
            @PathVariable String docType) {
        DocRequest request = new DocRequest();
        request.setProjectId(projectId);
        request.setAction("GET_BY_TYPE");
        request.setDocType(docType);
        return ResponseEntity.ok(documentationService.executeAction(request, null, ""));
    }
}
