package com.ucto.backend.service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ucto.backend.entity.BAChatMessage;
import com.ucto.backend.entity.Requirement;
import com.ucto.backend.repository.BAChatMessageRepository;
import com.ucto.backend.repository.RequirementRepository;

/**
 * Tests for BAChatService per docs/ucto_playbook.md and docs/state_machines.md §3.
 *
 * Covers:
 * - BA is the single voice to customer
 * - Max 3 clarification rounds before escalation
 * - Message analysis, ambiguity detection, decision extraction
 * - Requirement finalization triggering downstream agents
 * - Audit logging for all BA interactions
 * - Usage metering integration
 */
@ExtendWith(MockitoExtension.class)
class BAChatServiceTest {

    @Mock
    private BAChatMessageRepository messageRepository;

    @Mock
    private RequirementRepository requirementRepository;

    @Mock
    private AgentOrchestrationService agentOrchestrationService;

    @Mock
    private SubscriptionService subscriptionService;

    @Mock
    private AuditLogService auditLogService;

    private BAChatService baChatService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        baChatService = new BAChatService();
        ReflectionTestUtils.setField(baChatService, "messageRepository", messageRepository);
        ReflectionTestUtils.setField(baChatService, "requirementRepository", requirementRepository);
        ReflectionTestUtils.setField(baChatService, "agentOrchestrationService", agentOrchestrationService);
        ReflectionTestUtils.setField(baChatService, "subscriptionService", subscriptionService);
        ReflectionTestUtils.setField(baChatService, "auditLogService", auditLogService);
        ReflectionTestUtils.setField(baChatService, "objectMapper", objectMapper);
    }

    // ── Usage metering ──────────────────────────────────────────────

    @Test
    void processMessage_WhenUsageExceeded_ShouldThrowException() {
        when(subscriptionService.canRunAgent(anyLong())).thenReturn(false);

        assertThrows(AgentOrchestrationService.AgentRunLimitExceededException.class,
                () -> baChatService.processMessage(1L, 10L, "Hello", "127.0.0.1"));

        verify(messageRepository, never()).save(any());
        verify(auditLogService, never()).log(anyLong(), anyLong(), anyString(), anyString(), anyString(), anyBoolean());
    }

    // ── Greeting message ────────────────────────────────────────────

    @Test
    void processMessage_WithGreeting_ShouldReturnGreeting() {
        when(subscriptionService.canRunAgent(anyLong())).thenReturn(true);
        when(messageRepository.countByProjectIdAndUserId(10L, 1L)).thenReturn(0L);
        when(requirementRepository.findByProjectId(10L)).thenReturn(Collections.emptyList());
        when(messageRepository.save(any(BAChatMessage.class))).thenAnswer(i -> {
            BAChatMessage msg = i.getArgument(0);
            msg.setId(1L);
            msg.setCreatedAt(LocalDateTime.now());
            return msg;
        });

        var response = baChatService.processMessage(1L, 10L, "Hello", "127.0.0.1");

        assertNotNull(response);
        assertEquals("GREETING", response.getMessageType());
        assertTrue(response.getBaResponse().toLowerCase().contains("hello"));
        assertEquals(1, response.getRoundNumber());
        assertFalse(response.isClarificationComplete());
        assertFalse(response.isNeedsEscalation());
    }

    @Test
    void processMessage_WithGreetingAndExistingRequirements_ShouldMentionThem() {
        when(subscriptionService.canRunAgent(anyLong())).thenReturn(true);
        when(messageRepository.countByProjectIdAndUserId(10L, 1L)).thenReturn(2L);
        when(requirementRepository.findByProjectId(10L)).thenReturn(List.of(new Requirement()));
        when(messageRepository.save(any(BAChatMessage.class))).thenAnswer(i -> {
            BAChatMessage msg = i.getArgument(0);
            msg.setId(2L);
            msg.setCreatedAt(LocalDateTime.now());
            return msg;
        });

        var response = baChatService.processMessage(1L, 10L, "Hi", "127.0.0.1");

        assertEquals("GREETING", response.getMessageType());
        assertTrue(response.getBaResponse().toLowerCase().contains("requirement"));
    }

    // ── Clarification flow ──────────────────────────────────────────

    @Test
    void processMessage_WithAmbiguity_ShouldRequestClarification() {
        when(subscriptionService.canRunAgent(anyLong())).thenReturn(true);
        when(messageRepository.countByProjectIdAndUserId(10L, 1L)).thenReturn(0L);
        when(requirementRepository.findByProjectId(10L)).thenReturn(Collections.emptyList());
        when(messageRepository.save(any(BAChatMessage.class))).thenAnswer(i -> {
            BAChatMessage msg = i.getArgument(0);
            msg.setId(3L);
            msg.setCreatedAt(LocalDateTime.now());
            return msg;
        });

        // Message mentioning "fast" without specific target
        var response = baChatService.processMessage(1L, 10L,
                "I want a fast web application", "127.0.0.1");

        assertEquals("CLARIFICATION", response.getMessageType());
        assertFalse(response.getAmbiguities().isEmpty());
        assertTrue(response.getAmbiguities().get(0).toLowerCase().contains("performance"));
        assertFalse(response.isClarificationComplete());
        assertFalse(response.isNeedsEscalation());
    }

    @Test
    void processMessage_WithMultipleAmbiguities_ShouldListAll() {
        when(subscriptionService.canRunAgent(anyLong())).thenReturn(true);
        when(messageRepository.countByProjectIdAndUserId(10L, 1L)).thenReturn(0L);
        when(requirementRepository.findByProjectId(10L)).thenReturn(Collections.emptyList());
        when(messageRepository.save(any(BAChatMessage.class))).thenAnswer(i -> {
            BAChatMessage msg = i.getArgument(0);
            msg.setId(4L);
            msg.setCreatedAt(LocalDateTime.now());
            return msg;
        });

        // Message with multiple vague terms
        var response = baChatService.processMessage(1L, 10L,
                "Build a fast platform for multiple users with color themes", "127.0.0.1");

        assertEquals("CLARIFICATION", response.getMessageType());
        assertTrue(response.getAmbiguities().size() >= 2);
    }

    // ── Decision extraction ─────────────────────────────────────────

    @Test
    void processMessage_WithClearDecisions_ShouldDocumentThem() {
        when(subscriptionService.canRunAgent(anyLong())).thenReturn(true);
        when(messageRepository.countByProjectIdAndUserId(10L, 1L)).thenReturn(2L);
        when(requirementRepository.findByProjectId(10L)).thenReturn(Collections.emptyList());
        when(messageRepository.save(any(BAChatMessage.class))).thenAnswer(i -> {
            BAChatMessage msg = i.getArgument(0);
            msg.setId(5L);
            msg.setCreatedAt(LocalDateTime.now());
            return msg;
        });

        var response = baChatService.processMessage(1L, 10L,
                "Web application with Flutter frontend and Spring Boot backend, PostgreSQL database, dark mode",
                "127.0.0.1");

        assertEquals("DECISION", response.getMessageType());
        assertFalse(response.getDecisions().isEmpty());
        assertTrue(response.getDecisions().stream().anyMatch(d -> d.contains("Web")));
        assertTrue(response.getDecisions().stream().anyMatch(d -> d.contains("Flutter")));
        assertTrue(response.getDecisions().stream().anyMatch(d -> d.contains("Spring")));
        assertTrue(response.getDecisions().stream().anyMatch(d -> d.contains("PostgreSQL")));
        assertTrue(response.getDecisions().stream().anyMatch(d -> d.contains("Dark")));
    }

    // ── Escalation ──────────────────────────────────────────────────

    @Test
    void processMessage_AfterMaxRounds_ShouldEscalate() {
        when(subscriptionService.canRunAgent(anyLong())).thenReturn(true);
        // 6 messages = 3 rounds already completed (6/2 = 3)
        when(messageRepository.countByProjectIdAndUserId(10L, 1L)).thenReturn(6L);
        when(messageRepository.save(any(BAChatMessage.class))).thenAnswer(i -> {
            BAChatMessage msg = i.getArgument(0);
            msg.setId(7L);
            msg.setCreatedAt(LocalDateTime.now());
            return msg;
        });

        var response = baChatService.processMessage(1L, 10L, "I need more features", "127.0.0.1");

        assertEquals("ESCALATION", response.getMessageType());
        assertTrue(response.isNeedsEscalation());
        assertTrue(response.getBaResponse().toLowerCase().contains("escalated"));

        verify(auditLogService).log(eq(1L), eq(10L), eq("BA_ESCALATION"), anyString(), eq("127.0.0.1"), eq(true));
    }

    // ── Finalization ────────────────────────────────────────────────

    @Test
    void processMessage_WithFinalization_ShouldCompleteClarification() {
        when(subscriptionService.canRunAgent(anyLong())).thenReturn(true);
        when(messageRepository.countByProjectIdAndUserId(10L, 1L)).thenReturn(4L);
        when(requirementRepository.findByProjectId(10L)).thenReturn(Collections.emptyList());
        when(messageRepository.save(any(BAChatMessage.class))).thenAnswer(i -> {
            BAChatMessage msg = i.getArgument(0);
            msg.setId(6L);
            msg.setCreatedAt(LocalDateTime.now());
            return msg;
        });

        var response = baChatService.processMessage(1L, 10L, "Looks great, let's finalize", "127.0.0.1");

        assertEquals("FINALIZATION", response.getMessageType());
        assertTrue(response.isClarificationComplete());

        verify(agentOrchestrationService).triggerAgent(eq("ux"), eq(10L), eq(1L), eq("127.0.0.1"), anyMap());
        verify(agentOrchestrationService, times(2)).triggerAgent(eq("ba"), eq(10L), eq(1L), eq("127.0.0.1"), anyMap());
    }

    @Test
    void processMessage_WithFinalizationAndRequirements_ShouldUpdateStatus() {
        when(subscriptionService.canRunAgent(anyLong())).thenReturn(true);
        when(messageRepository.countByProjectIdAndUserId(10L, 1L)).thenReturn(4L);

        Requirement req = new Requirement();
        req.setId(100L);
        req.setProjectId(10L);
        req.setStatus("DRAFT");
        when(requirementRepository.findByProjectId(10L)).thenReturn(List.of(req));
        when(requirementRepository.save(any(Requirement.class))).thenAnswer(i -> i.getArgument(0));

        when(messageRepository.save(any(BAChatMessage.class))).thenAnswer(i -> {
            BAChatMessage msg = i.getArgument(0);
            msg.setId(7L);
            msg.setCreatedAt(LocalDateTime.now());
            return msg;
        });

        var response = baChatService.processMessage(1L, 10L, "Confirm", "127.0.0.1");

        assertEquals("FINALIZATION", response.getMessageType());
        assertTrue(response.isClarificationComplete());

        // Verify requirement status was updated to CLARIFIED
        verify(requirementRepository).save(argThat(r -> "CLARIFIED".equals(r.getStatus())));
    }

    // ── Chat History ────────────────────────────────────────────────

    @Test
    void getChatHistory_ShouldReturnMessagesAndMetadata() {
        LocalDateTime now = LocalDateTime.now();

        BAChatMessage msg1 = new BAChatMessage();
        msg1.setId(1L);
        msg1.setUserMessage("Hello");
        msg1.setBaResponse("Hi, how can I help?");
        msg1.setRoundNumber(1);
        msg1.setMessageType("GREETING");
        msg1.setCreatedAt(now);

        when(messageRepository.findByProjectIdAndUserIdOrderByCreatedAtAsc(10L, 1L))
                .thenReturn(List.of(msg1));

        var history = baChatService.getChatHistory(10L, 1L);

        assertNotNull(history);
        assertEquals(1, history.getMessages().size());
        assertEquals(1, history.getCurrentRound());
        assertFalse(history.isClarificationComplete());
        assertFalse(history.isNeedsEscalation());
    }

    @Test
    void getChatHistory_WithMaxRounds_ShouldIndicateEscalation() {
        BAChatMessage msg = new BAChatMessage();
        msg.setId(1L);
        msg.setUserMessage("test");
        msg.setRoundNumber(3);
        msg.setMessageType("CLARIFICATION");
        msg.setCreatedAt(LocalDateTime.now());

        when(messageRepository.findByProjectIdAndUserIdOrderByCreatedAtAsc(10L, 1L))
                .thenReturn(List.of(msg));

        var history = baChatService.getChatHistory(10L, 1L);

        assertTrue(history.isNeedsEscalation());
        assertEquals(3, history.getCurrentRound());
    }

    // ── Ambiguity detection helpers ─────────────────────────────────

    @Test
    void processMessage_WithPerformanceVague_ShouldAskForTarget() {
        when(subscriptionService.canRunAgent(anyLong())).thenReturn(true);
        when(messageRepository.countByProjectIdAndUserId(10L, 1L)).thenReturn(0L);
        when(requirementRepository.findByProjectId(10L)).thenReturn(Collections.emptyList());
        when(messageRepository.save(any(BAChatMessage.class))).thenAnswer(i -> {
            BAChatMessage msg = i.getArgument(0);
            msg.setId(8L);
            msg.setCreatedAt(LocalDateTime.now());
            return msg;
        });

        var response = baChatService.processMessage(1L, 10L,
                "The app should be fast", "127.0.0.1");

        assertEquals("CLARIFICATION", response.getMessageType());
        boolean hasPerformanceAmbiguity = response.getAmbiguities().stream()
                .anyMatch(a -> a.toLowerCase().contains("performance") || a.toLowerCase().contains("target"));
        assertTrue(hasPerformanceAmbiguity);
    }

    @Test
    void processMessage_WithIntegrationVague_ShouldAskForService() {
        when(subscriptionService.canRunAgent(anyLong())).thenReturn(true);
        when(messageRepository.countByProjectIdAndUserId(10L, 1L)).thenReturn(0L);
        when(requirementRepository.findByProjectId(10L)).thenReturn(Collections.emptyList());
        when(messageRepository.save(any(BAChatMessage.class))).thenAnswer(i -> {
            BAChatMessage msg = i.getArgument(0);
            msg.setId(9L);
            msg.setCreatedAt(LocalDateTime.now());
            return msg;
        });

        var response = baChatService.processMessage(1L, 10L,
                "We need integrations", "127.0.0.1");

        assertEquals("CLARIFICATION", response.getMessageType());
        boolean hasIntegrationAmbiguity = response.getAmbiguities().stream()
                .anyMatch(a -> a.toLowerCase().contains("external") || a.toLowerCase().contains("service"));
        assertTrue(hasIntegrationAmbiguity);
    }

    // ── Round number tracking ───────────────────────────────────────

    @Test
    void processMessage_RoundNumber_ShouldIncrementWithMessages() {
        when(subscriptionService.canRunAgent(anyLong())).thenReturn(true);
        when(messageRepository.countByProjectIdAndUserId(10L, 1L)).thenReturn(4L); // 2 rounds completed
        when(requirementRepository.findByProjectId(10L)).thenReturn(Collections.emptyList());
        when(messageRepository.save(any(BAChatMessage.class))).thenAnswer(i -> {
            BAChatMessage msg = i.getArgument(0);
            msg.setId(10L);
            msg.setCreatedAt(LocalDateTime.now());
            return msg;
        });

        var response = baChatService.processMessage(1L, 10L, "I need a web app", "127.0.0.1");

        assertEquals(3, response.getRoundNumber()); // 4/2 + 1 = 3
    }

    // ── Empty message handling ──────────────────────────────────────

    @Test
    void processMessage_WithEmptyMessage_ShouldTreatAsGreeting() {
        when(subscriptionService.canRunAgent(anyLong())).thenReturn(true);
        when(messageRepository.countByProjectIdAndUserId(10L, 1L)).thenReturn(0L);
        when(requirementRepository.findByProjectId(10L)).thenReturn(Collections.emptyList());
        when(messageRepository.save(any(BAChatMessage.class))).thenAnswer(i -> {
            BAChatMessage msg = i.getArgument(0);
            msg.setId(11L);
            msg.setCreatedAt(LocalDateTime.now());
            return msg;
        });

        var response = baChatService.processMessage(1L, 10L, "", "127.0.0.1");

        assertEquals("GREETING", response.getMessageType());
    }

    // ── Agent trigger on each message ───────────────────────────────

    @Test
    void processMessage_ShouldTriggerAgentOnEachMessage() {
        when(subscriptionService.canRunAgent(anyLong())).thenReturn(true);
        when(messageRepository.countByProjectIdAndUserId(10L, 1L)).thenReturn(2L);
        when(requirementRepository.findByProjectId(10L)).thenReturn(Collections.emptyList());
        when(messageRepository.save(any(BAChatMessage.class))).thenAnswer(i -> {
            BAChatMessage msg = i.getArgument(0);
            msg.setId(12L);
            msg.setCreatedAt(LocalDateTime.now());
            return msg;
        });

        baChatService.processMessage(1L, 10L, "Add dark mode", "127.0.0.1");

        verify(agentOrchestrationService).triggerAgent(eq("ba"), eq(10L), eq(1L), eq("127.0.0.1"), anyMap());
    }
}
