package com.ucto.backend.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ucto.backend.dto.BAChatHistoryResponse;
import com.ucto.backend.dto.BAChatRequest;
import com.ucto.backend.dto.BAChatResponse;
import com.ucto.backend.service.AgentOrchestrationService;
import com.ucto.backend.service.BAChatService;

import jakarta.servlet.http.HttpServletRequest;

/**
 * BA Chat controller — the only external communication channel between customer and UCTO agents.
 *
 * Per docs/ucto_playbook.md:
 * - BA is the single voice to customer
 * - All other agents communicate internally only
 * - Max 3 BA clarification rounds before escalation
 */
@RestController
@RequestMapping("/api/ba")
public class BAChatController {

    @Autowired
    private BAChatService baChatService;

    /**
     * Send a message to the BA agent via chat.
     * POST /api/ba/chat
     */
    @PostMapping("/chat")
    public ResponseEntity<?> sendMessage(
            @RequestBody BAChatRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) authentication.getPrincipal();
            String ipAddress = httpRequest.getRemoteAddr();

            BAChatResponse response = baChatService.processMessage(
                    userId, request.getProjectId(), request.getMessage(), ipAddress);

            return ResponseEntity.ok(response);
        } catch (AgentOrchestrationService.AgentRunLimitExceededException e) {
            return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED)
                    .body(Map.of("error", e.getMessage(), "code", "AGENT_RUN_LIMIT_EXCEEDED"));
        }
    }

    /**
     * Get chat history for a project.
     * GET /api/ba/chat/{projectId}
     */
    @GetMapping("/chat/{projectId}")
    public ResponseEntity<BAChatHistoryResponse> getChatHistory(
            @PathVariable Long projectId,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        BAChatHistoryResponse history = baChatService.getChatHistory(projectId, userId);
        return ResponseEntity.ok(history);
    }
}
