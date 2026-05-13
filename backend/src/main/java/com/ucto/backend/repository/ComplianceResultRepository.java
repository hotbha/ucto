package com.ucto.backend.repository;

import com.ucto.backend.entity.ComplianceResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ComplianceResultRepository extends JpaRepository<ComplianceResult, Long> {
    List<ComplianceResult> findByProjectIdOrderByCreatedAtDesc(Long projectId);
    Optional<ComplianceResult> findTopByProjectIdAndBranchOrderByCreatedAtDesc(Long projectId, String branch);
    List<ComplianceResult> findByProjectIdAndCorrelationId(Long projectId, String correlationId);
}
