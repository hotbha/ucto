package com.ucto.backend.repository;

import com.ucto.backend.entity.TestResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TestResultRepository extends JpaRepository<TestResult, Long> {
    List<TestResult> findByProjectIdOrderByCreatedAtDesc(Long projectId);
    Optional<TestResult> findTopByProjectIdAndBranchOrderByCreatedAtDesc(Long projectId, String branch);
    List<TestResult> findByProjectIdAndCorrelationId(Long projectId, String correlationId);
}
