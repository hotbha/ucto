package com.ucto.backend.repository;

import com.ucto.backend.entity.AgentMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AgentMessageRepository extends JpaRepository<AgentMessage, Long> {
    List<AgentMessage> findByProjectIdOrderByCreatedAtDesc(Long projectId);
    List<AgentMessage> findByStoryIdOrderByCreatedAtAsc(Long storyId);
    List<AgentMessage> findByFromAgentAndToAgentOrderByCreatedAtDesc(String fromAgent, String toAgent);
    List<AgentMessage> findByNeedsHumanTrueAndStatus(String status);
    List<AgentMessage> findByCorrelationIdOrderByCreatedAtAsc(String correlationId);
    long countByProjectIdAndNeedsHumanTrue(Long projectId);
}
