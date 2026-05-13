package com.ucto.backend.controller;

import com.ucto.backend.entity.Requirement;
import com.ucto.backend.repository.RequirementRepository;
import com.ucto.backend.service.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/requirements")
public class RequirementController {

    @Autowired
    private RequirementRepository requirementRepository;

    @Autowired
    private AuditLogService auditLogService;

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<Requirement>> getByProject(@PathVariable Long projectId) {
        return ResponseEntity.ok(requirementRepository.findByProjectId(projectId));
    }

    @PostMapping
    public ResponseEntity<?> createRequirement(@RequestBody Map<String, String> request,
                                                Authentication auth,
                                                HttpServletRequest httpRequest) {
        try {
            Long userId = getUserId(auth);
            Requirement req = new Requirement();
            req.setProjectId(Long.parseLong(request.get("projectId")));
            req.setTitle(request.get("title"));
            req.setDescription(request.get("description"));
            req.setStatus("DRAFT");
            req.setCreatedBy(userId);
            req = requirementRepository.save(req);

            auditLogService.log(userId, req.getProjectId(), "REQUIREMENT_CREATE",
                    "Created requirement: " + req.getTitle(), httpRequest.getRemoteAddr(), true);

            return ResponseEntity.status(HttpStatus.CREATED).body(req);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateRequirement(@PathVariable Long id,
                                                 @RequestBody Map<String, String> request) {
        Requirement req = requirementRepository.findById(id).orElse(null);
        if (req == null) return ResponseEntity.notFound().build();

        if (request.containsKey("title")) req.setTitle(request.get("title"));
        if (request.containsKey("description")) req.setDescription(request.get("description"));
        if (request.containsKey("status")) req.setStatus(request.get("status"));

        // Enforce max 3 clarification rounds per docs/ucto_playbook.md
        if (request.containsKey("clarificationRound")) {
            int round = Integer.parseInt(request.get("clarificationRound"));
            if (round > 3) {
                return ResponseEntity.badRequest().body(
                    Map.of("error", "Maximum clarification rounds (3) reached"));
            }
            req.setClarificationRound(round);
        }

        req = requirementRepository.save(req);
        return ResponseEntity.ok(req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRequirement(@PathVariable Long id) {
        requirementRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Deleted"));
    }

    private Long getUserId(Authentication auth) {
        if (auth != null && auth.getDetails() instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> details = (Map<String, Object>) auth.getDetails();
            return (Long) details.get("userId");
        }
        throw new RuntimeException("User not authenticated");
    }
}
