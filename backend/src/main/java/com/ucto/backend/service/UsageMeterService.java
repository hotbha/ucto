package com.ucto.backend.service;

import com.ucto.backend.entity.AgentRun;
import com.ucto.backend.repository.AgentRunRepository;
import com.ucto.backend.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class UsageMeterService {

    @Autowired
    private AgentRunRepository agentRunRepository;

    @Autowired
    private ProjectRepository projectRepository;

    public void recordAgentRun(Long userId, String agentType) {
        AgentRun run = new AgentRun();
        run.setUserId(userId);
        run.setAgentType(agentType);
        run.setTimestamp(LocalDateTime.now());
        agentRunRepository.save(run);
    }

    public int getMonthlyRuns(Long userId) {
        LocalDateTime startOfMonth = LocalDateTime.now()
                .withDayOfMonth(1)
                .truncatedTo(ChronoUnit.DAYS);
        return agentRunRepository.countByUserIdAndTimestampAfter(userId, startOfMonth);
    }

    public int getProjectCount(Long userId) {
        return (int) projectRepository.countByOwnerId(userId);
    }
}
