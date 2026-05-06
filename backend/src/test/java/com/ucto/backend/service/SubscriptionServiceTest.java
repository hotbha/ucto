package com.ucto.backend.service;

import com.ucto.backend.dto.SubscriptionPlan;
import com.ucto.backend.entity.Subscription;
import com.ucto.backend.repository.SubscriptionRepository;
import com.ucto.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UsageMeterService usageMeterService;

    private SubscriptionService subscriptionService;

    @BeforeEach
    void setUp() {
        subscriptionService = new SubscriptionService();
        ReflectionTestUtils.setField(subscriptionService, "subscriptionRepository", subscriptionRepository);
        ReflectionTestUtils.setField(subscriptionService, "userRepository", userRepository);
        ReflectionTestUtils.setField(subscriptionService, "usageMeterService", usageMeterService);
    }

    @Test
    void getPlan_WithValidTier_ShouldReturnCorrectPlan() {
        SubscriptionPlan plan = SubscriptionService.getPlan("STARTUP");
        assertEquals("STARTUP", plan.getTier());
        assertEquals(5, plan.getMaxProjects());
        assertEquals(50, plan.getMaxAgentRuns());
        assertEquals(2999, plan.getPrice());
    }

    @Test
    void getPlan_WithFreeTier_ShouldReturnFreePlan() {
        SubscriptionPlan plan = SubscriptionService.getPlan("FREE");
        assertEquals("FREE", plan.getTier());
        assertEquals(0, plan.getPrice());
        assertEquals(1, plan.getMaxProjects());
        assertEquals(5, plan.getMaxAgentRuns());
    }

    @Test
    void getPlan_WithInvalidTier_ShouldDefaultToFree() {
        SubscriptionPlan plan = SubscriptionService.getPlan("INVALID_TIER");
        assertEquals("FREE", plan.getTier());
    }

    @Test
    void getPlan_WithCaseInsensitiveTier() {
        SubscriptionPlan plan = SubscriptionService.getPlan("growth");
        assertEquals("GROWTH", plan.getTier());
    }

    @Test
    void getAllPlans_ShouldReturnFourPlans() {
        List<SubscriptionPlan> plans = SubscriptionService.getAllPlans();
        assertEquals(4, plans.size());
        assertEquals("FREE", plans.get(0).getTier());
        assertEquals("STARTUP", plans.get(1).getTier());
        assertEquals("GROWTH", plans.get(2).getTier());
        assertEquals("ENTERPRISE", plans.get(3).getTier());
    }

    @Test
    void createSubscription_ShouldCancelExistingActive() {
        Subscription existing = new Subscription();
        existing.setId(1L);
        existing.setUserId(1L);
        existing.setTier("FREE");
        existing.setStatus("ACTIVE");

        when(subscriptionRepository.findByUserId(1L)).thenReturn(Optional.of(existing));
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(i -> i.getArgument(0));

        subscriptionService.createSubscription(1L, "STARTUP");

        verify(subscriptionRepository, times(2)).save(any(Subscription.class));
        assertEquals("CANCELLED", existing.getStatus());
    }

    @Test
    void createSubscription_ShouldSetCorrectTierAndStatus() {
        when(subscriptionRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(i -> i.getArgument(0));

        Subscription sub = subscriptionService.createSubscription(1L, "GROWTH");

        assertEquals("GROWTH", sub.getTier());
        assertEquals("ACTIVE", sub.getStatus());
        assertEquals(1L, sub.getUserId());
        assertNotNull(sub.getStartDate());
        assertNotNull(sub.getEndDate());
    }

    @Test
    void activateFreeTrial_ShouldCreate14DayTrial() {
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(i -> i.getArgument(0));

        Subscription trial = subscriptionService.activateFreeTrial(1L);

        assertEquals("STARTUP_TRIAL", trial.getTier());
        assertEquals("ACTIVE", trial.getStatus());
        assertEquals(1L, trial.getUserId());
        assertNotNull(trial.getStartDate());
        assertNotNull(trial.getEndDate());
        assertTrue(trial.getEndDate().isAfter(trial.getStartDate().plusDays(13)));
        assertTrue(trial.getEndDate().isBefore(trial.getStartDate().plusDays(15)));
    }

    @Test
    void getCurrentSubscription_WithActiveSub_ShouldReturnIt() {
        Subscription sub = new Subscription();
        sub.setId(1L);
        sub.setUserId(1L);
        sub.setTier("STARTUP");
        sub.setStatus("ACTIVE");

        when(subscriptionRepository.findByUserId(1L)).thenReturn(Optional.of(sub));

        Subscription result = subscriptionService.getCurrentSubscription(1L);
        assertNotNull(result);
        assertEquals("STARTUP", result.getTier());
    }

    @Test
    void getCurrentSubscription_WithNoSub_ShouldReturnNull() {
        when(subscriptionRepository.findByUserId(1L)).thenReturn(Optional.empty());
        assertNull(subscriptionService.getCurrentSubscription(1L));
    }

    @Test
    void getEffectivePlan_WithNoSubscription_ShouldReturnFree() {
        when(subscriptionRepository.findByUserId(1L)).thenReturn(Optional.empty());
        SubscriptionPlan plan = subscriptionService.getEffectivePlan(1L);
        assertEquals("FREE", plan.getTier());
    }

    @Test
    void getEffectivePlan_WithActiveTrial_ShouldReturnStartup() {
        Subscription trial = new Subscription();
        trial.setTier("STARTUP_TRIAL");
        trial.setStatus("ACTIVE");
        trial.setEndDate(LocalDateTime.now().plusDays(7));

        when(subscriptionRepository.findByUserId(1L)).thenReturn(Optional.of(trial));

        SubscriptionPlan plan = subscriptionService.getEffectivePlan(1L);
        assertEquals("STARTUP", plan.getTier());
    }

    @Test
    void getEffectivePlan_WithExpiredTrial_ShouldReturnFree() {
        Subscription expiredTrial = new Subscription();
        expiredTrial.setTier("STARTUP_TRIAL");
        expiredTrial.setStatus("ACTIVE");
        expiredTrial.setEndDate(LocalDateTime.now().minusDays(1));

        when(subscriptionRepository.findByUserId(1L)).thenReturn(Optional.of(expiredTrial));

        SubscriptionPlan plan = subscriptionService.getEffectivePlan(1L);
        assertEquals("FREE", plan.getTier());
        assertEquals("EXPIRED", expiredTrial.getStatus());
    }

    @Test
    void canRunAgent_WhenUnderLimit_ShouldReturnTrue() {
        when(subscriptionRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(usageMeterService.getMonthlyRuns(1L)).thenReturn(3);

        assertTrue(subscriptionService.canRunAgent(1L));
    }

    @Test
    void canRunAgent_WhenAtLimit_ShouldReturnFalse() {
        when(subscriptionRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(usageMeterService.getMonthlyRuns(1L)).thenReturn(5);

        assertFalse(subscriptionService.canRunAgent(1L));
    }

    @Test
    void canCreateProject_WhenUnderLimit_ShouldReturnTrue() {
        when(subscriptionRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(usageMeterService.getProjectCount(1L)).thenReturn(0);

        assertTrue(subscriptionService.canCreateProject(1L));
    }

    @Test
    void canCreateProject_WhenAtLimit_ShouldReturnFalse() {
        when(subscriptionRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(usageMeterService.getProjectCount(1L)).thenReturn(1);

        assertFalse(subscriptionService.canCreateProject(1L));
    }

    @Test
    void getUsageStatus_ShouldIncludeAllFields() {
        when(subscriptionRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(usageMeterService.getMonthlyRuns(1L)).thenReturn(4);
        when(usageMeterService.getProjectCount(1L)).thenReturn(1);

        var status = subscriptionService.getUsageStatus(1L);

        assertEquals("FREE", status.get("tier"));
        assertEquals(1, status.get("maxProjects"));
        assertEquals(1, status.get("projectsUsed"));
        assertEquals(0, status.get("projectsRemaining"));
        assertEquals(5, status.get("maxAgentRuns"));
        assertEquals(4, status.get("runsUsed"));
        assertEquals(1, status.get("runsRemaining"));
        assertTrue((Boolean) status.get("needsUpgrade"));
    }
}
