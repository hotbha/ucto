package com.ucto.backend.service;

import com.ucto.backend.entity.ComplianceResult;
import com.ucto.backend.entity.GateEvaluation;
import com.ucto.backend.entity.TestResult;
import com.ucto.backend.repository.GateEvaluationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Coordinates quality gate evaluation (test and compliance) and
 * stores GateEvaluation records for audit and traceability.
 *
 * See docs/quality_gates_and_simulation_design.md for full design.
 */
@Service
public class QualityGateService {

    @Autowired
    private GateEvaluationRepository gateEvaluationRepository;

    @Autowired
    private AuditLogService auditLogService;

    @Value("${ucto.gates.coverage-threshold:80}")
    private double coverageThreshold;

    /**
     * Evaluate a test gate based on test results.
     * Passes if all tests pass and coverage >= threshold.
     */
    public GateEvaluation evaluateTestGate(Long projectId, String correlationId,
                                           TestResult result, boolean simulation,
                                           String branch) {
        boolean passed = result.getTestsFailed() == 0
                && result.getCoveragePercent() >= coverageThreshold;

        GateEvaluation eval = new GateEvaluation();
        eval.setProjectId(projectId);
        eval.setGateType("TEST");
        eval.setPassed(passed);
        eval.setTestResultId(result.getId());
        eval.setCorrelationId(correlationId);
        eval.setSimulation(simulation);
        eval.setBranch(branch);
        eval.setDetails(String.format(
                "Tests: %d/%d passed, coverage %.1f%% (threshold %.1f%%)",
                result.getTestsPassed(), result.getTestsRun(),
                result.getCoveragePercent(), coverageThreshold));

        gateEvaluationRepository.save(eval);

        String action = simulation ? "GATE_TEST_EVALUATE_SIMULATED" : "GATE_TEST_EVALUATE";
        auditLogService.logAuthAction(null, action, eval.getDetails(), "", passed);

        return eval;
    }

    /**
     * Evaluate a compliance gate. Passes if overallStatus is "PASS".
     */
    public GateEvaluation evaluateComplianceGate(Long projectId, String correlationId,
                                                  ComplianceResult result, boolean simulation,
                                                  String branch) {
        boolean passed = "PASS".equals(result.getOverallStatus());

        GateEvaluation eval = new GateEvaluation();
        eval.setProjectId(projectId);
        eval.setGateType("COMPLIANCE");
        eval.setPassed(passed);
        eval.setComplianceResultId(result.getId());
        eval.setCorrelationId(correlationId);
        eval.setSimulation(simulation);
        eval.setBranch(branch);
        eval.setDetails(String.format(
                "Compliance: %s, severity %s",
                result.getOverallStatus(), result.getSeverity()));

        gateEvaluationRepository.save(eval);

        String action = simulation ? "GATE_COMPLIANCE_EVALUATE_SIMULATED" : "GATE_COMPLIANCE_EVALUATE";
        auditLogService.logAuthAction(null, action, eval.getDetails(), "", passed);

        return eval;
    }

    /**
     * Evaluate coordinated gate (both test + compliance must pass).
     */
    public GateEvaluation evaluateCoordinatedGate(Long projectId, String correlationId,
                                                   GateEvaluation testEval,
                                                   GateEvaluation complianceEval,
                                                   boolean simulation,
                                                   String branch) {
        boolean bothPassed = testEval.isPassed() && complianceEval.isPassed();

        GateEvaluation coord = new GateEvaluation();
        coord.setProjectId(projectId);
        coord.setGateType("COORDINATED");
        coord.setPassed(bothPassed);
        coord.setCorrelationId(correlationId);
        coord.setSimulation(simulation);
        coord.setBranch(branch);
        coord.setDetails(String.format(
                "Coordinated gate: tests=%s, compliance=%s → overall=%s",
                testEval.isPassed() ? "PASS" : "FAIL",
                complianceEval.isPassed() ? "PASS" : "FAIL",
                bothPassed ? "PASS" : "FAIL"));

        gateEvaluationRepository.save(coord);

        String action = simulation ? "GATE_COORDINATED_EVALUATE_SIMULATED" : "GATE_COORDINATED_EVALUATE";
        auditLogService.logAuthAction(null, action, coord.getDetails(), "", bothPassed);

        return coord;
    }

    /**
     * Get the latest gate status for a given project and branch.
     */
    public GateStatusDTO getGateStatus(Long projectId, String branch) {
        var testGate = gateEvaluationRepository
                .findTopByProjectIdAndGateTypeAndBranchOrderByEvaluatedAtDesc(projectId, "TEST", branch);
        var complianceGate = gateEvaluationRepository
                .findTopByProjectIdAndGateTypeAndBranchOrderByEvaluatedAtDesc(projectId, "COMPLIANCE", branch);
        var coordinatedGate = gateEvaluationRepository
                .findTopByProjectIdAndGateTypeAndBranchOrderByEvaluatedAtDesc(projectId, "COORDINATED", branch);

        GateStatusDTO dto = new GateStatusDTO();
        dto.setProjectId(projectId);
        dto.setBranch(branch);
        dto.setTestGate(testGate.orElse(null));
        dto.setComplianceGate(complianceGate.orElse(null));
        dto.setCoordinatedGate(coordinatedGate.orElse(null));

        if (coordinatedGate.isPresent()) {
            dto.setOverallPass(coordinatedGate.get().isPassed());
        } else if (testGate.isPresent() || complianceGate.isPresent()) {
            dto.setOverallPass(false);
        } else {
            dto.setOverallPass(false);
        }

        return dto;
    }

    /**
     * DTO for gate status response.
     */
    public static class GateStatusDTO {
        private Long projectId;
        private String branch;
        private GateEvaluation testGate;
        private GateEvaluation complianceGate;
        private GateEvaluation coordinatedGate;
        private boolean overallPass;

        public Long getProjectId() { return projectId; }
        public void setProjectId(Long projectId) { this.projectId = projectId; }
        public String getBranch() { return branch; }
        public void setBranch(String branch) { this.branch = branch; }
        public GateEvaluation getTestGate() { return testGate; }
        public void setTestGate(GateEvaluation testGate) { this.testGate = testGate; }
        public GateEvaluation getComplianceGate() { return complianceGate; }
        public void setComplianceGate(GateEvaluation complianceGate) { this.complianceGate = complianceGate; }
        public GateEvaluation getCoordinatedGate() { return coordinatedGate; }
        public void setCoordinatedGate(GateEvaluation coordinatedGate) { this.coordinatedGate = coordinatedGate; }
        public boolean isOverallPass() { return overallPass; }
        public void setOverallPass(boolean overallPass) { this.overallPass = overallPass; }
    }
}
