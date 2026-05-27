package com.ucto.backend.service;

import com.ucto.backend.entity.ComplianceResult;
import com.ucto.backend.entity.GateEvaluation;
import com.ucto.backend.entity.TestResult;
import com.ucto.backend.repository.GateEvaluationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QualityGateServiceTest {

    @Mock
    private GateEvaluationRepository gateEvaluationRepository;

    @Mock
    private AuditLogService auditLogService;

    @Captor
    private ArgumentCaptor<GateEvaluation> gateCaptor;

    private QualityGateService qualityGateService;

    @BeforeEach
    void setUp() {
        qualityGateService = new QualityGateService();
        ReflectionTestUtils.setField(qualityGateService, "gateEvaluationRepository", gateEvaluationRepository);
        ReflectionTestUtils.setField(qualityGateService, "auditLogService", auditLogService);
        ReflectionTestUtils.setField(qualityGateService, "coverageThreshold", 80.0);
    }

    @Test
    void testEvaluateTestGate_passes() {
        TestResult result = new TestResult();
        result.setId(1L);
        result.setTestsRun(10);
        result.setTestsPassed(10);
        result.setTestsFailed(0);
        result.setCoveragePercent(85.0);
        result.setStatus("PASSED");

        when(gateEvaluationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        GateEvaluation eval = qualityGateService.evaluateTestGate(1L, "corr_1", result, false, "main");

        assertTrue(eval.isPassed());
        assertEquals("TEST", eval.getGateType());
        assertFalse(eval.isSimulation());
        assertEquals(1L, eval.getTestResultId());
        verify(auditLogService).logAuthAction(null, "GATE_TEST_EVALUATE", eval.getDetails(), "", true);
    }

    @Test
    void testEvaluateTestGate_fails_lowCoverage() {
        TestResult result = new TestResult();
        result.setId(2L);
        result.setTestsRun(10);
        result.setTestsPassed(10);
        result.setTestsFailed(0);
        result.setCoveragePercent(60.0); // Below 80% threshold
        result.setStatus("PASSED");

        when(gateEvaluationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        GateEvaluation eval = qualityGateService.evaluateTestGate(1L, "corr_2", result, false, "main");

        assertFalse(eval.isPassed());
    }

    @Test
    void testEvaluateTestGate_fails_someFailed() {
        TestResult result = new TestResult();
        result.setId(3L);
        result.setTestsRun(10);
        result.setTestsPassed(8);
        result.setTestsFailed(2);
        result.setCoveragePercent(90.0);
        result.setStatus("FAILED");

        when(gateEvaluationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        GateEvaluation eval = qualityGateService.evaluateTestGate(1L, "corr_3", result, false, "main");

        assertFalse(eval.isPassed());
    }

    @Test
    void testEvaluateComplianceGate_passes() {
        ComplianceResult result = new ComplianceResult();
        result.setId(1L);
        result.setOverallStatus("PASS");
        result.setSeverity("LOW");

        when(gateEvaluationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        GateEvaluation eval = qualityGateService.evaluateComplianceGate(1L, "corr_4", result, false, "main");

        assertTrue(eval.isPassed());
        assertEquals("COMPLIANCE", eval.getGateType());
        verify(auditLogService).logAuthAction(null, "GATE_COMPLIANCE_EVALUATE", eval.getDetails(), "", true);
    }

    @Test
    void testEvaluateComplianceGate_fails() {
        ComplianceResult result = new ComplianceResult();
        result.setId(2L);
        result.setOverallStatus("FAIL");
        result.setSeverity("HIGH");

        when(gateEvaluationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        GateEvaluation eval = qualityGateService.evaluateComplianceGate(1L, "corr_5", result, false, "main");

        assertFalse(eval.isPassed());
    }

    @Test
    void testEvaluateCoordinatedGate_bothPass() {
        GateEvaluation testGate = new GateEvaluation();
        testGate.setPassed(true);
        GateEvaluation complianceGate = new GateEvaluation();
        complianceGate.setPassed(true);

        when(gateEvaluationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        GateEvaluation coord = qualityGateService.evaluateCoordinatedGate(1L, "corr_6", testGate, complianceGate, false, "main");

        assertTrue(coord.isPassed());
        assertEquals("COORDINATED", coord.getGateType());
    }

    @Test
    void testEvaluateCoordinatedGate_oneFails() {
        GateEvaluation testGate = new GateEvaluation();
        testGate.setPassed(false);
        GateEvaluation complianceGate = new GateEvaluation();
        complianceGate.setPassed(true);

        when(gateEvaluationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        GateEvaluation coord = qualityGateService.evaluateCoordinatedGate(1L, "corr_7", testGate, complianceGate, false, "main");

        assertFalse(coord.isPassed());
    }

    @Test
    void testSimulationMode_usesSimulatedActionName() {
        TestResult result = new TestResult();
        result.setId(5L);
        result.setTestsRun(5);
        result.setTestsPassed(5);
        result.setTestsFailed(0);
        result.setCoveragePercent(95.0);
        result.setStatus("PASSED");

        when(gateEvaluationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        GateEvaluation eval = qualityGateService.evaluateTestGate(1L, "corr_sim", result, true, "main");

        assertTrue(eval.isSimulation());
        assertTrue(eval.isPassed());
        verify(auditLogService).logAuthAction(null, "GATE_TEST_EVALUATE_SIMULATED", eval.getDetails(), "", true);
    }

    @Test
    void testGetGateStatus_noEvaluations() {
        when(gateEvaluationRepository.findTopByProjectIdAndGateTypeAndBranchOrderByEvaluatedAtDesc(
                anyLong(), anyString(), anyString()))
                .thenReturn(java.util.Optional.empty());

        var status = qualityGateService.getGateStatus(1L, "main");
        assertNotNull(status);
        assertFalse(status.isOverallPass());
        assertNull(status.getTestGate());
    }
}
