package com.ucto.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ucto.backend.entity.BAChatMessage;

/**
 * Repository for BA Chat messages (BA-customer clarification channel).
 */
@Repository
public interface BAChatMessageRepository extends JpaRepository<BAChatMessage, Long> {
    List<BAChatMessage> findByProjectIdOrderByCreatedAtAsc(Long projectId);
    List<BAChatMessage> findByProjectIdAndUserIdOrderByCreatedAtAsc(Long projectId, Long userId);
    long countByProjectIdAndUserId(Long projectId, Long userId);
}
