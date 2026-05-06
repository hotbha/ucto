package com.ucto.backend.service;

import com.ucto.backend.entity.AuditLog;
import com.ucto.backend.repository.AuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    private AuditLogService auditLogService;

    @BeforeEach
    void setUp() {
        auditLogService = new AuditLogService();
        ReflectionTestUtils.setField(auditLogService, "auditLogRepository", auditLogRepository);
    }

    @Test
    void log_ShouldSaveAuditLog() {
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(i -> {
            AuditLog log = i.getArgument(0);
            log.setTimestamp(LocalDateTime.now()); // Simulate @PrePersist
            return log;
        });

        AuditLog result = auditLogService.log(1L, 10L, "PROJECT_CREATE",
                "Created project: Test", "127.0.0.1", true);

        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        assertEquals(10L, result.getProjectId());
        assertEquals("PROJECT_CREATE", result.getAction());
        assertEquals("Created project: Test", result.getDetails());
        assertEquals("127.0.0.1", result.getIpAddress());
        assertTrue(result.isSuccess());
        assertNotNull(result.getTimestamp());
    }

    @Test
    void logAuthAction_ShouldCallLogWithNullProjectId() {
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(i -> {
            AuditLog log = i.getArgument(0);
            log.setTimestamp(LocalDateTime.now()); // Simulate @PrePersist
            return log;
        });

        AuditLog result = auditLogService.logAuthAction(1L, "LOGIN",
                "User logged in", "192.168.1.1", true);

        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        assertNull(result.getProjectId());
        assertEquals("LOGIN", result.getAction());
    }

    @Test
    void findByUserId_ShouldReturnUserLogs() {
        AuditLog log1 = new AuditLog();
        log1.setUserId(1L);
        log1.setAction("LOGIN");

        when(auditLogRepository.findByUserId(1L)).thenReturn(List.of(log1));

        List<AuditLog> logs = auditLogService.findByUserId(1L);
        assertEquals(1, logs.size());
        assertEquals("LOGIN", logs.get(0).getAction());
    }

    @Test
    void findByProjectId_ShouldReturnProjectLogs() {
        AuditLog log1 = new AuditLog();
        log1.setProjectId(10L);
        log1.setAction("AGENT_TRIGGER_BA");

        when(auditLogRepository.findByProjectId(10L)).thenReturn(List.of(log1));

        List<AuditLog> logs = auditLogService.findByProjectId(10L);
        assertEquals(1, logs.size());
        assertEquals("AGENT_TRIGGER_BA", logs.get(0).getAction());
    }

    @Test
    void findByAction_ShouldReturnFilteredLogs() {
        AuditLog log1 = new AuditLog();
        log1.setAction("PROJECT_CREATE");

        when(auditLogRepository.findByAction("PROJECT_CREATE")).thenReturn(List.of(log1));

        List<AuditLog> logs = auditLogService.findByAction("PROJECT_CREATE");
        assertEquals(1, logs.size());
    }

    @Test
    void findAll_ShouldReturnAllLogs() {
        AuditLog log1 = new AuditLog();
        log1.setAction("ACTION1");
        AuditLog log2 = new AuditLog();
        log2.setAction("ACTION2");

        when(auditLogRepository.findAll()).thenReturn(List.of(log1, log2));

        List<AuditLog> logs = auditLogService.findAll();
        assertEquals(2, logs.size());
    }
}
