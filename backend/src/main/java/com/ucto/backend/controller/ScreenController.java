package com.ucto.backend.controller;

import com.ucto.backend.entity.Screen;
import com.ucto.backend.repository.ScreenRepository;
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
@RequestMapping("/api/screens")
public class ScreenController {

    @Autowired
    private ScreenRepository screenRepository;

    @Autowired
    private AuditLogService auditLogService;

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<Screen>> getByProject(@PathVariable Long projectId) {
        return ResponseEntity.ok(screenRepository.findByProjectId(projectId));
    }

    @PostMapping
    public ResponseEntity<?> createScreen(@RequestBody Map<String, Object> request,
                                           Authentication auth,
                                           HttpServletRequest httpRequest) {
        try {
            Long userId = getUserId(auth);
            Screen screen = new Screen();
            screen.setProjectId(Long.valueOf(request.get("projectId").toString()));
            if (request.containsKey("requirementId")) {
                screen.setRequirementId(Long.valueOf(request.get("requirementId").toString()));
            }
            screen.setType((String) request.get("type"));
            screen.setStatus("PENDING");
            screen.setStorageUrl((String) request.get("storageUrl"));
            screen.setMimeType((String) request.get("mimeType"));
            screen = screenRepository.save(screen);

            auditLogService.log(userId, screen.getProjectId(), "SCREEN_CREATED",
                    "Screen created: " + screen.getType(), httpRequest.getRemoteAddr(), true);

            return ResponseEntity.status(HttpStatus.CREATED).body(screen);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateScreenStatus(@PathVariable Long id,
                                                 @RequestBody Map<String, String> request,
                                                 Authentication auth,
                                                 HttpServletRequest httpRequest) {
        Screen screen = screenRepository.findById(id).orElse(null);
        if (screen == null) return ResponseEntity.notFound().build();

        String newStatus = request.get("status");
        String feedback = request.get("feedback");

        screen.setStatus(newStatus);
        if (feedback != null) {
            screen.setFeedback(feedback);
        }
        screen = screenRepository.save(screen);

        Long userId = getUserId(auth);
        auditLogService.log(userId, screen.getProjectId(), "SCREEN_" + newStatus.toUpperCase(),
                "Screen status: " + newStatus + (feedback != null ? " - " + feedback : ""),
                httpRequest.getRemoteAddr(), true);

        return ResponseEntity.ok(screen);
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
