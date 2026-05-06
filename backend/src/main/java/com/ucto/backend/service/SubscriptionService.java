package com.ucto.backend.service;

import com.ucto.backend.entity.Subscription;
import com.ucto.backend.repository.SubscriptionRepository;
import com.ucto.backend.repository.UserRepository;
import com.ucto.backend.dto.SubscriptionPlan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class SubscriptionService {

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UsageMeterService usageMeterService;

    // Tier definitions
    private static final Map<String, SubscriptionPlan> PLANS = new LinkedHashMap<>();

    static {
        PLANS.put("FREE", new SubscriptionPlan("FREE", 0, 1, 5, false, false, false));
        PLANS.put("STARTUP", new SubscriptionPlan("STARTUP", 2999, 5, 50, true, false, false));
        PLANS.put("GROWTH", new SubscriptionPlan("GROWTH", 7999, 50, 200, true, true, false));
        PLANS.put("ENTERPRISE", new SubscriptionPlan("ENTERPRISE", 49999, 999, 9999, true, true, true));
    }

    public static SubscriptionPlan getPlan(String tier) {
        return PLANS.getOrDefault(tier.toUpperCase(), PLANS.get("FREE"));
    }

    public static List<SubscriptionPlan> getAllPlans() {
        return new ArrayList<>(PLANS.values());
    }

    @Transactional
    public Subscription createSubscription(Long userId, String tier) {
        // Cancel any existing active subscription
        subscriptionRepository.findByUserId(userId).ifPresent(existing -> {
            if ("ACTIVE".equals(existing.getStatus())) {
                existing.setStatus("CANCELLED");
                existing.setEndDate(LocalDateTime.now());
                subscriptionRepository.save(existing);
            }
        });

        SubscriptionPlan plan = getPlan(tier);
        Subscription sub = new Subscription();
        sub.setUserId(userId);
        sub.setTier(tier.toUpperCase());
        sub.setStatus("ACTIVE");
        sub.setStartDate(LocalDateTime.now());
        sub.setEndDate(LocalDateTime.now().plusMonths(1));
        sub.setChargebeeSubscriptionId(null); // Will be set when Chargebee is integrated
        return subscriptionRepository.save(sub);
    }

    @Transactional
    public Subscription activateFreeTrial(Long userId) {
        // Give 14-day STARTUP trial
        SubscriptionPlan plan = getPlan("STARTUP");
        Subscription sub = new Subscription();
        sub.setUserId(userId);
        sub.setTier("STARTUP_TRIAL");
        sub.setStatus("ACTIVE");
        sub.setStartDate(LocalDateTime.now());
        sub.setEndDate(LocalDateTime.now().plusDays(14));
        return subscriptionRepository.save(sub);
    }

    public Subscription getCurrentSubscription(Long userId) {
        return subscriptionRepository.findByUserId(userId).orElse(null);
    }

    public SubscriptionPlan getEffectivePlan(Long userId) {
        Subscription sub = getCurrentSubscription(userId);
        if (sub == null || !"ACTIVE".equals(sub.getStatus())) {
            return getPlan("FREE");
        }
        if ("STARTUP_TRIAL".equals(sub.getTier()) && sub.getEndDate().isAfter(LocalDateTime.now())) {
            return getPlan("STARTUP");
        }
        if ("STARTUP_TRIAL".equals(sub.getTier()) && sub.getEndDate().isBefore(LocalDateTime.now())) {
            // Trial expired, auto-downgrade to FREE
            sub.setTier("FREE");
            sub.setStatus("EXPIRED");
            subscriptionRepository.save(sub);
            return getPlan("FREE");
        }
        return getPlan(sub.getTier());
    }

    public boolean canRunAgent(Long userId) {
        SubscriptionPlan plan = getEffectivePlan(userId);
        int runsUsed = usageMeterService.getMonthlyRuns(userId);
        return runsUsed < plan.getMaxAgentRuns();
    }

    public boolean canCreateProject(Long userId) {
        SubscriptionPlan plan = getEffectivePlan(userId);
        int projectsCount = usageMeterService.getProjectCount(userId);
        return projectsCount < plan.getMaxProjects();
    }

    @Transactional
    public void recordAgentRun(Long userId, String agentType) {
        usageMeterService.recordAgentRun(userId, agentType);
    }

    public Map<String, Object> getUsageStatus(Long userId) {
        SubscriptionPlan plan = getEffectivePlan(userId);
        int runsUsed = usageMeterService.getMonthlyRuns(userId);
        int projectsCount = usageMeterService.getProjectCount(userId);

        Map<String, Object> status = new HashMap<>();
        status.put("tier", plan.getTier());
        status.put("maxProjects", plan.getMaxProjects());
        status.put("projectsUsed", projectsCount);
        status.put("projectsRemaining", Math.max(0, plan.getMaxProjects() - projectsCount));
        status.put("maxAgentRuns", plan.getMaxAgentRuns());
        status.put("runsUsed", runsUsed);
        status.put("runsRemaining", Math.max(0, plan.getMaxAgentRuns() - runsUsed));
        status.put("hasAudit", plan.isHasAudit());
        status.put("hasCompliance", plan.isHasCompliance());
        status.put("hasPrioritySupport", plan.isHasPrioritySupport());
        status.put("needsUpgrade", runsUsed >= plan.getMaxAgentRuns() || projectsCount >= plan.getMaxProjects());

        return status;
    }

    @Transactional
    public void handleChargebeeWebhook(String eventType, Map<String, Object> payload) {
        System.out.println("Chargebee webhook received: " + eventType);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> subscription = (Map<String, Object>) payload.get("subscription");

            if (subscription == null) {
                System.out.println("Chargebee webhook: No subscription object in payload");
                return;
            }

            String chargebeeSubId = (String) subscription.get("id");
            String planId = (String) subscription.get("plan_id");
            String status = (String) subscription.get("status");
            String customerId = (String) subscription.get("customer_id");

            if (chargebeeSubId == null) {
                System.out.println("Chargebee webhook: No subscription id in payload");
                return;
            }

            // Map Chargebee plan IDs to UCTO tiers
            String tier = mapChargebeePlanToTier(planId);

            switch (eventType) {
                case "subscription_created":
                case "subscription_renewed":
                    // Create or update subscription in our system
                    Long userId = findUserIdByChargebeeCustomerId(customerId);
                    if (userId != null) {
                        Subscription sub = createSubscription(userId, tier);
                        sub.setChargebeeSubscriptionId(chargebeeSubId);
                        subscriptionRepository.save(sub);
                        System.out.println("Chargebee: Subscription " + chargebeeSubId + " created/renewed for user " + userId);
                    } else {
                        System.out.println("Chargebee: No user found for customer " + customerId);
                    }
                    break;

                case "subscription_cancelled":
                    // Find and cancel the subscription
                    Optional<Subscription> existingSub = subscriptionRepository.findByChargebeeSubscriptionId(chargebeeSubId);
                    existingSub.ifPresent(sub -> {
                        sub.setStatus("CANCELLED");
                        sub.setEndDate(LocalDateTime.now());
                        subscriptionRepository.save(sub);
                        System.out.println("Chargebee: Subscription " + chargebeeSubId + " cancelled");
                    });
                    break;

                case "subscription_updated":
                    // Handle plan changes, status updates
                    Optional<Subscription> updatedSub = subscriptionRepository.findByChargebeeSubscriptionId(chargebeeSubId);
                    updatedSub.ifPresent(sub -> {
                        sub.setTier(tier);
                        if ("cancelled".equals(status)) {
                            sub.setStatus("CANCELLED");
                        } else if ("non_renewing".equals(status)) {
                            sub.setEndDate(LocalDateTime.now());
                        }
                        subscriptionRepository.save(sub);
                        System.out.println("Chargebee: Subscription " + chargebeeSubId + " updated");
                    });
                    break;

                default:
                    System.out.println("Chargebee: Unhandled event type: " + eventType);
            }
        } catch (Exception e) {
            System.err.println("Chargebee webhook processing error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String mapChargebeePlanToTier(String planId) {
        if (planId == null) return "FREE";
        String upper = planId.toUpperCase();
        if (upper.contains("STARTUP")) return "STARTUP";
        if (upper.contains("GROWTH")) return "GROWTH";
        if (upper.contains("ENTERPRISE")) return "ENTERPRISE";
        return "FREE";
    }

    private Long findUserIdByChargebeeCustomerId(String customerId) {
        if (customerId == null) return null;
        try {
            return Long.parseLong(customerId.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
