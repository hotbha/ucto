package com.ucto.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgentOrchestrationServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private SubscriptionService subscriptionService;

    @Mock
    private UsageMeterService usageMeterService;

    @Mock
    private AuditLogService auditLogService;

    private AgentOrchestrationService agentOrchestrationService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        agentOrchestrationService = new AgentOrchestrationService();
        ReflectionTestUtils.setField(agentOrchestrationService, "redisTemplate", redisTemplate);
        ReflectionTestUtils.setField(agentOrchestrationService, "objectMapper", objectMapper);
        ReflectionTestUtils.setField(agentOrchestrationService, "subscriptionService", subscriptionService);
        ReflectionTestUtils.setField(agentOrchestrationService, "usageMeterService", usageMeterService);
        ReflectionTestUtils.setField(agentOrchestrationService, "auditLogService", auditLogService);
    }

    @Test
    void triggerAgent_WhenWithinLimit_ShouldPublishAndRecord() {
        when(subscriptionService.canRunAgent(anyLong())).thenReturn(true);
        when(redisTemplate.convertAndSend(anyString(), anyString())).thenReturn(1L);

        Map<String, Object> data = new HashMap<>();
        data.put("key", "value");

        String eventId = agentOrchestrationService.triggerAgent("ba", 10L, 1L, "127.0.0.1", data);

        assertNotNull(eventId);
        assertTrue(eventId.startsWith("evt_"));

        verify(redisTemplate).convertAndSend(eq("agent.ba.trigger"), anyString());
        verify(usageMeterService).recordAgentRun(1L, "BA");
        verify(auditLogService).log(eq(1L), eq(10L), eq("AGENT_TRIGGER_BA"), anyString(), eq("127.0.0.1"), eq(true));
    }

    @Test
    void triggerAgent_WhenOverLimit_ShouldThrowException() {
        when(subscriptionService.canRunAgent(anyLong())).thenReturn(false);

        assertThrows(AgentOrchestrationService.AgentRunLimitExceededException.class, () ->
                agentOrchestrationService.triggerAgent("ba", 1L, 1L, "127.0.0.1", new HashMap<>())
        );

        verify(redisTemplate, never()).convertAndSend(anyString(), anyString());
        verify(usageMeterService, never()).recordAgentRun(anyLong(), anyString());
    }

    @Test
    void triggerAgent_WithNullProjectId_ShouldNotThrow() {
        when(subscriptionService.canRunAgent(anyLong())).thenReturn(true);
        when(redisTemplate.convertAndSend(anyString(), anyString())).thenReturn(1L);

        assertDoesNotThrow(() ->
                agentOrchestrationService.triggerAgent("developer", null, 1L, "10.0.0.1", new HashMap<>())
        );

        verify(redisTemplate).convertAndSend(eq("agent.developer.trigger"), anyString());
        verify(usageMeterService).recordAgentRun(1L, "DEVELOPER");
    }

    @Test
    void triggerAgent_WithEmptyData_ShouldNotThrow() {
        when(subscriptionService.canRunAgent(anyLong())).thenReturn(true);
        when(redisTemplate.convertAndSend(anyString(), anyString())).thenReturn(1L);

        assertDoesNotThrow(() ->
                agentOrchestrationService.triggerAgent("tester", 5L, 2L, "1.2.3.4", null)
        );

        verify(usageMeterService).recordAgentRun(2L, "TESTER");
    }

    @Test
    void triggerAgent_ShouldPublishValidJson() {
        when(subscriptionService.canRunAgent(anyLong())).thenReturn(true);
        when(redisTemplate.convertAndSend(anyString(), anyString())).thenReturn(1L);

        Map<String, Object> data = new HashMap<>();
        data.put("testKey", "testValue");

        String eventId = agentOrchestrationService.triggerAgent("architect", 3L, 1L, "0.0.0.0", data);

        assertNotNull(eventId);
        verify(redisTemplate, times(1)).convertAndSend(eq("agent.architect.trigger"), anyString());
    }

    @Test
    void triggerAgent_DifferentAgentTypes_ShouldMapCorrectly() {
        when(subscriptionService.canRunAgent(anyLong())).thenReturn(true);
        when(redisTemplate.convertAndSend(anyString(), anyString())).thenReturn(1L);

        Map<String, Object> data = new HashMap<>();

        // Test BA maps to BA
        agentOrchestrationService.triggerAgent("ba", 1L, 1L, "", data);
        verify(usageMeterService).recordAgentRun(1L, "BA");

        // Test developer maps to DEVELOPER
        agentOrchestrationService.triggerAgent("developer", 1L, 2L, "", data);
        verify(usageMeterService).recordAgentRun(2L, "DEVELOPER");

        // Test compliance maps to COMPLIANCE
        agentOrchestrationService.triggerAgent("compliance", 1L, 3L, "", data);
        verify(usageMeterService).recordAgentRun(3L, "COMPLIANCE");

        // Test ux maps to UI_UX
        agentOrchestrationService.triggerAgent("ux", 1L, 4L, "", data);
        verify(usageMeterService).recordAgentRun(4L, "UI_UX");
    }
}
