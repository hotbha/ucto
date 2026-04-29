package com.ucto.backend.repository;

import com.ucto.backend.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByUserId(Long userId);
    List<AuditLog> findByProjectId(Long projectId);
    List<AuditLog> findByAction(String action);
}
