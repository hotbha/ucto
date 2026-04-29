package com.ucto.backend.controller;

import com.ucto.backend.dto.SubscriptionPlan;
import com.ucto.backend.service.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

    @Autowired
    private SubscriptionService subscriptionService;

    @GetMapping("/plans")
    public ResponseEntity<List<SubscriptionPlan>> getPlans() {
        return ResponseEntity.ok(SubscriptionService.getAllPlans());
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMySubscription(Authentication auth) {
        Long userId = getUserId(auth);
        return ResponseEntity.ok(subscriptionService.getUsageStatus(userId));
    }

    @PostMapping("/upgrade")
    public ResponseEntity<?> upgrade(@RequestBody Map<String, String> request,
                                      Authentication auth) {
        try {
            Long userId = getUserId(auth);
            String tier = request.get("tier");
            subscriptionService.createSubscription(userId, tier);
            return ResponseEntity.ok(Map.of("message", "Upgraded to " + tier));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/start-trial")
    public ResponseEntity<?> startTrial(Authentication auth) {
        try {
            Long userId = getUserId(auth);
            subscriptionService.activateFreeTrial(userId);
            return ResponseEntity.ok(Map.of("message", "14-day Startup trial activated"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/webhook/chargebee")
    public ResponseEntity<?> chargebeeWebhook(@RequestBody Map<String, Object> payload) {
        String eventType = (String) payload.get("event_type");
        subscriptionService.handleChargebeeWebhook(eventType, payload);
        return ResponseEntity.ok(Map.of("message", "ok"));
    }

    @GetMapping("/can-run-agent")
    public ResponseEntity<?> canRunAgent(Authentication auth) {
        Long userId = getUserId(auth);
        boolean canRun = subscriptionService.canRunAgent(userId);
        Map<String, Object> usage = subscriptionService.getUsageStatus(userId);
        usage.put("canRun", canRun);
        return ResponseEntity.ok(usage);
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
