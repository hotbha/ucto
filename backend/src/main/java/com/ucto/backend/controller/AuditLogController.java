package com.ucto.backend.controller;

import com.ucto.backend.entity.AuditLog;
import com.ucto.backend.service.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {

    @Autowired
    private AuditLogService auditLogService;

    @GetMapping
    public ResponseEntity<List<AuditLog>> getAuditLogs(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) String action,
            Authentication auth) {

        Long userId = getUserId(auth);

        if (projectId != null) {
            return ResponseEntity.ok(auditLogService.findByProjectId(projectId));
        }
        if (action != null) {
            return ResponseEntity.ok(auditLogService.findByAction(action));
        }
        // UCTO_ADMIN can see all, others see their own
        String role = getRole(auth);
        if ("UCTO_ADMIN".equals(role)) {
            return ResponseEntity.ok(auditLogService.findAll());
        }
        return ResponseEntity.ok(auditLogService.findByUserId(userId));
    }

    private Long getUserId(Authentication auth) {
        if (auth != null && auth.getDetails() instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> details = (Map<String, Object>) auth.getDetails();
            return (Long) details.get("userId");
        }
        throw new RuntimeException("User not authenticated");
    }

    private String getRole(Authentication auth) {
        if (auth != null && auth.getDetails() instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> details = (Map<String, Object>) auth.getDetails();
            return (String) details.get("role");
        }
        return "VIEWER";
    }
}
