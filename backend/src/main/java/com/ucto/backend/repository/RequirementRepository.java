package com.ucto.backend.repository;

import com.ucto.backend.entity.Requirement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RequirementRepository extends JpaRepository<Requirement, Long> {
    List<Requirement> findByProjectId(Long projectId);
}
