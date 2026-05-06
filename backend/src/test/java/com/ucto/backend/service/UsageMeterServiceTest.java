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

    @Test
    void recordAgentRun_ShouldSaveAgentRun() {
        usageMeterService.recordAgentRun(1L, "BA");
        verify(agentRunRepository, times(1)).save(any(AgentRun.class));
    }

    @Test
    void getMonthlyRuns_ShouldReturnCorrectCount() {
        when(agentRunRepository.countByUserIdAndTimestampAfter(eq(1L), any(LocalDateTime.class)))
                .thenReturn(3);

        int runs = usageMeterService.getMonthlyRuns(1L);
        assertEquals(3, runs);
    }

    @Test
    void getMonthlyRuns_WithNoRuns_ShouldReturnZero() {
        when(agentRunRepository.countByUserIdAndTimestampAfter(eq(2L), any(LocalDateTime.class)))
                .thenReturn(0);

        int runs = usageMeterService.getMonthlyRuns(2L);
        assertEquals(0, runs);
    }

    @Test
    void getMonthlyRuns_ShouldQueryFromStartOfMonth() {
        usageMeterService.getMonthlyRuns(1L);

        verify(agentRunRepository).countByUserIdAndTimestampAfter(eq(1L), any(LocalDateTime.class));
    }

    @Test
    void getProjectCount_ShouldReturnCorrectCount() {
        when(projectRepository.countByOwnerId(1L)).thenReturn(2L);

        int count = usageMeterService.getProjectCount(1L);
        assertEquals(2, count);
    }

    @Test
    void recordAgentRun_SetsUserIdAndAgentType() {
        usageMeterService.recordAgentRun(5L, "DEVELOPER");

        verify(agentRunRepository).save(argThat(run ->
                run.getUserId().equals(5L) && "DEVELOPER".equals(run.getAgentType())
        ));
    }

    @Test
    void recordAgentRun_SetsTimestamp() {
        usageMeterService.recordAgentRun(1L, "BA");

        verify(agentRunRepository).save(argThat(run -> run.getTimestamp() != null));
    }
}
