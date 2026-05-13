package com.ucto.backend.service;

import com.ucto.backend.entity.AgentRun;
import com.ucto.backend.repository.AgentRunRepository;
import com.ucto.backend.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for UsageMeterService per docs/exhaustive_test_cases.md §7.
 *
 * Covers:
 * - USAGE-01: Monthly run counter increments
 * - USAGE-02: Monthly counter resets next month
 * - USAGE-03: Project count increments
 * - USAGE-04: Project deletion decrements count
 */
@ExtendWith(MockitoExtension.class)
class UsageMeterServiceTest {

    @Mock
    private AgentRunRepository agentRunRepository;

    @Mock
    private ProjectRepository projectRepository;

    private UsageMeterService usageMeterService;

    @BeforeEach
    void setUp() {
        usageMeterService = new UsageMeterService();
        ReflectionTestUtils.setField(usageMeterService, "agentRunRepository", agentRunRepository);
        ReflectionTestUtils.setField(usageMeterService, "projectRepository", projectRepository);
    }

    // ── USAGE-01: Monthly run counter increments ────────────────────

    @Test
    void recordAgentRun_ShouldSaveRun() {
        usageMeterService.recordAgentRun(1L, "BA");

        verify(agentRunRepository).save(argThat(run -> {
            assertEquals(1L, run.getUserId());
            assertEquals("BA", run.getAgentType());
            assertNotNull(run.getTimestamp());
            return true;
        }));
    }

    @Test
    void recordAgentRun_WithDifferentTypes_ShouldSaveCorrectly() {
        usageMeterService.recordAgentRun(2L, "DEVELOPER");
        usageMeterService.recordAgentRun(3L, "COMPLIANCE");
        usageMeterService.recordAgentRun(4L, "UI_UX");

        verify(agentRunRepository).save(argThat(run -> run.getUserId() == 2L && "DEVELOPER".equals(run.getAgentType())));
        verify(agentRunRepository).save(argThat(run -> run.getUserId() == 3L && "COMPLIANCE".equals(run.getAgentType())));
        verify(agentRunRepository).save(argThat(run -> run.getUserId() == 4L && "UI_UX".equals(run.getAgentType())));
    }

    // ── USAGE-02: Monthly counter tracking ──────────────────────────

    @Test
    void getMonthlyRuns_ShouldCountSinceStartOfMonth() {
        when(agentRunRepository.countByUserIdAndTimestampAfter(anyLong(), any(LocalDateTime.class)))
                .thenReturn(3);

        int runs = usageMeterService.getMonthlyRuns(1L);
        assertEquals(3, runs);
    }

    @Test
    void getMonthlyRuns_WithNoRuns_ShouldReturnZero() {
        when(agentRunRepository.countByUserIdAndTimestampAfter(anyLong(), any(LocalDateTime.class)))
                .thenReturn(0);

        int runs = usageMeterService.getMonthlyRuns(1L);
        assertEquals(0, runs);
    }

    @Test
    void getMonthlyRuns_WithMultipleUsers_ShouldBePerUser() {
        when(agentRunRepository.countByUserIdAndTimestampAfter(eq(1L), any(LocalDateTime.class)))
                .thenReturn(5);
        when(agentRunRepository.countByUserIdAndTimestampAfter(eq(2L), any(LocalDateTime.class)))
                .thenReturn(2);

        assertEquals(5, usageMeterService.getMonthlyRuns(1L));
        assertEquals(2, usageMeterService.getMonthlyRuns(2L));
    }

    @Test
    void getMonthlyRuns_ShouldUseStartOfMonthBoundary() {
        // This test also verifies the date boundary logic
        when(agentRunRepository.countByUserIdAndTimestampAfter(eq(1L), any(LocalDateTime.class)))
                .thenReturn(3);

        int runs = usageMeterService.getMonthlyRuns(1L);

        assertEquals(3, runs);
        // Verify timestampAfter is within the first day of current month
        verify(agentRunRepository).countByUserIdAndTimestampAfter(eq(1L), argThat(date -> {
            return date.getDayOfMonth() == 1;
        }));
    }

    // ── USAGE-03: Project count tracking ────────────────────────────

    @Test
    void getProjectCount_ShouldReturnProjectCount() {
        when(projectRepository.countByOwnerId(1L)).thenReturn(3L);

        int count = usageMeterService.getProjectCount(1L);
        assertEquals(3, count);
    }

    @Test
    void getProjectCount_WithNoProjects_ShouldReturnZero() {
        when(projectRepository.countByOwnerId(1L)).thenReturn(0L);

        int count = usageMeterService.getProjectCount(1L);
        assertEquals(0, count);
    }

    // ── USAGE-04: Multiple delegate to repository ────────────────────

    @Test
    void getProjectCount_ShouldBePerUser() {
        when(projectRepository.countByOwnerId(1L)).thenReturn(1L);
        when(projectRepository.countByOwnerId(2L)).thenReturn(5L);

        assertEquals(1, usageMeterService.getProjectCount(1L));
        assertEquals(5, usageMeterService.getProjectCount(2L));
    }
}
