package com.ucto.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ucto.backend.entity.Sprint;

@Repository
public interface SprintRepository extends JpaRepository<Sprint, Long> {
    List<Sprint> findByProjectIdOrderByStartDateDesc(Long projectId);
    Optional<Sprint> findTopByProjectIdAndStatusOrderByStartDateDesc(Long projectId, String status);
    long countByProjectIdAndStatus(Long projectId, String status);
}
