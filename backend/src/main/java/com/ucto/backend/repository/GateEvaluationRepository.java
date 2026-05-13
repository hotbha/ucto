package com.ucto.backend.repository;

import com.ucto.backend.entity.GateEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GateEvaluationRepository extends JpaRepository<GateEvaluation, Long> {
    List<GateEvaluation> findByProjectIdOrderByEvaluatedAtDesc(Long projectId);
    List<GateEvaluation> findByProjectIdAndCorrelationId(Long projectId, String correlationId);
    Optional<GateEvaluation> findTopByProjectIdAndGateTypeAndBranchOrderByEvaluatedAtDesc(
            Long projectId, String gateType, String branch);
}
