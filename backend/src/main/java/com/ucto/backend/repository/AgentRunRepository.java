package com.ucto.backend.repository;

import com.ucto.backend.entity.AgentRun;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface AgentRunRepository extends JpaRepository<AgentRun, Long> {
    int countByUserIdAndTimestampAfter(Long userId, LocalDateTime timestamp);
    List<AgentRun> findByUserIdOrderByTimestampDesc(Long userId);
    List<AgentRun> findByProjectIdOrderByTimestampDesc(Long projectId);
}
