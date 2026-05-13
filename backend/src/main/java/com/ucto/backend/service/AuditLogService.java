package com.ucto.backend.service;

import com.ucto.backend.entity.AuditLog;
import com.ucto.backend.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    public AuditLog log(Long userId, Long projectId, String action, String details,
                        String ipAddress, boolean success) {
        return log(userId, projectId, action, details, ipAddress, success, false);
    }

    public AuditLog log(Long userId, Long projectId, String action, String details,
                        String ipAddress, boolean success, boolean simulation) {
        AuditLog log = new AuditLog();
        log.setUserId(userId);
        log.setProjectId(projectId);
        log.setAction(action);
        log.setDetails(details);
        log.setIpAddress(ipAddress);
        log.setSuccess(success);
        log.setSimulation(simulation);
        return auditLogRepository.save(log);
    }


    public AuditLog logAuthAction(Long userId, String action, String details,
                                  String ipAddress, boolean success) {
        return log(userId, null, action, details, ipAddress, success);
    }

    public List<AuditLog> findByUserId(Long userId) {
        return auditLogRepository.findByUserId(userId);
    }

    public List<AuditLog> findByProjectId(Long projectId) {
        return auditLogRepository.findByProjectId(projectId);
    }

    public List<AuditLog> findByAction(String action) {
        return auditLogRepository.findByAction(action);
    }

    public List<AuditLog> findAll() {
        return auditLogRepository.findAll();
    }
}
