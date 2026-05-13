package com.ucto.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ucto.backend.dto.SimulatedSprintRequest;
import com.ucto.backend.dto.SimulatedSprintResponse;
import com.ucto.backend.dto.SimulatedSprintResponse.StepResult;
import com.ucto.backend.dto.SimulatedSprintResponse.TestResultSummary;
import com.ucto.backend.dto.SimulatedSprintResponse.ComplianceResultSummary;
import com.ucto.backend.dto.SimulatedSprintResponse.BaSummary;
import com.ucto.backend.dto.SimulatedSprintResponse.ArchitectSummary;
import com.ucto.backend.dto.SimulatedSprintResponse.DevSummary;
import com.ucto.backend.dto.agent.BaRequirementsOutput;
import com.ucto.backend.dto.agent.ArchitectDesignOutput;
import com.ucto.backend.dto.agent.DevImplementOutput;
import com.ucto.backend.dto.agent.TesterOutput;
import com.ucto.backend.dto.agent.ComplianceOutput;
import com.ucto.backend.entity.ComplianceResult;
import com.ucto.backend.entity.GateEvaluation;
import com.ucto.backend.entity.Project;
import com.ucto.backend.entity.TestResult;
import com.ucto.backend.repository.ComplianceResultRepository;
import com.ucto.backend.repository.TestResultRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;


import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Orchestrates the "Simulated Sprint" hero flow.
 * Runs all 5 agents in simulation mode with strict JSON DTO parsing.
 *
 * See docs/hero_flow_mvp.md for full design.
 */
@Service
public class SimulatedSprintService {

    private static final Logger log = LoggerFactory.getLogger(SimulatedSprintService.class);
    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired private ProjectService projectService;
    @Autowired private RepoWorkspaceService repoWorkspaceService;
    @Autowired private AuditLogService auditLogService;
    @Autowired private LLMAgentClient llmAgentClient;
    @Autowired private TestResultRepository testResultRepository;
    @Autowired private ComplianceResultRepository complianceResultRepository;
    @Autowired private QualityGateService qualityGateService;

    public SimulatedSprintResponse runSprint(Long projectId, SimulatedSprintRequest request) {
        if (request.getChangeDescription() == null || request.getChangeDescription().isBlank())
            throw new IllegalArgumentException("changeDescription is required");

        Project project = projectService.getProjectById(projectId);
        if (project == null) throw new IllegalArgumentException("Project not found: " + projectId);

        String branch = request.getBranch() != null ? request.getBranch() : "main";
        String correlationId = "sim_sprint_" + Instant.now().toString().replaceAll("[:.-]", "").substring(0, 18);
        String projectTitle = project.getTitle();
        List<StepResult> steps = new ArrayList<>();

        // ── Step 1: BA Agent ──
        auditLogService.logAuthAction(null, "AGENT_TRIGGER_BA_SIMULATED",
                "Simulated BA for project " + projectId, "", true, true);
        Map<String, String> baCtx = new HashMap<>();
        baCtx.put("projectTitle", projectTitle);
        baCtx.put("changeDescription", request.getChangeDescription());
        String baRaw = llmAgentClient.execute("BA_REQUIREMENTS", baCtx);
        BaRequirementsOutput baOutput = parseOrFallback(baRaw, BaRequirementsOutput.class, "BA_REQUIREMENTS");
        log.info("BA produced {} requirements", baOutput.getRequirements().size());
        steps.add(new StepResult("BA", "COMPLETED", true, "evt_ba_" + projectId));

        // Build readable summaries from structured output
        String requirementsSummary = baOutput.getRequirements().stream()
                .map(r -> r.getId() + ": " + r.getArea() + " - " + r.getDescription())
                .collect(Collectors.joining("; "));
        String acSummary = baOutput.getRequirements().stream()
                .flatMap(r -> r.getOpenQuestions().stream())
                .collect(Collectors.joining("; "));
        String riskSummary = baOutput.getRequirements().stream()
                .map(r -> r.getId() + " risk=" + r.getRisk())
                .collect(Collectors.joining("; "));

        // ── Step 2: Architect Agent ──
        auditLogService.logAuthAction(null, "AGENT_TRIGGER_ARCHITECT_SIMULATED",
                "Simulated architect for project " + projectId, "", true, true);
        Map<String, String> archCtx = new HashMap<>();
        archCtx.put("projectTitle", projectTitle);
        archCtx.put("requirements", requirementsSummary);
        String archRaw = llmAgentClient.execute("ARCHITECT_DESIGN", archCtx);
        ArchitectDesignOutput archOutput = parseOrFallback(archRaw, ArchitectDesignOutput.class, "ARCHITECT_DESIGN");
        log.info("Architect produced {} components, {} services", archOutput.getComponents().size(), archOutput.getServices().size());
        steps.add(new StepResult("ARCHITECT", "COMPLETED", true, "evt_arch_" + projectId));

        String componentSummary = archOutput.getComponents().stream()
                .map(c -> c.getName() + " (" + c.getTechnology() + ")")
                .collect(Collectors.joining("; "));
        String archRisksSummary = String.join("; ", archOutput.getRisks());


        // ── Step 3: Developer Agent ──
        auditLogService.logAuthAction(null, "AGENT_TRIGGER_DEVELOPER_SIMULATED",
                "Simulated dev for project " + projectId, "", true, true);
        if (project.getRepoUrl() != null && !project.getRepoUrl().isBlank())
            repoWorkspaceService.prepareWorkspace(project, true);

        Map<String, String> devCtx = new HashMap<>();
        devCtx.put("projectTitle", projectTitle);
        devCtx.put("requirements", requirementsSummary);
        devCtx.put("architectureSpec", componentSummary);
        String devRaw = llmAgentClient.execute("DEV_IMPLEMENT", devCtx);
        DevImplementOutput devOutput = parseOrFallback(devRaw, DevImplementOutput.class, "DEV_IMPLEMENT");
        log.info("Dev produced {} file changes, risk={}", devOutput.getFilesToChange().size(), devOutput.getRiskLevel());
        steps.add(new StepResult("DEVELOPER", "COMPLETED", true, "evt_dev_" + projectId));

        String fileChangeSummary = devOutput.getFilesToChange().stream()
                .map(f -> f.getAction() + " " + f.getPath())
                .collect(Collectors.joining("; "));

        // ── Steps 4-6: Persistence steps wrapped in DB error handling ──
        TestResultSummary trs = null;
        ComplianceResultSummary crs = null;
        QualityGateService.GateStatusDTO gateStatus = new QualityGateService.GateStatusDTO();
        gateStatus.setProjectId(projectId);
        gateStatus.setBranch(branch);
        gateStatus.setOverallPass(false);

        boolean dbError = false;
        String dbErrorMsg = null;

        try {
            // ── Step 4: Tester Agent ──
            auditLogService.logAuthAction(null, "AGENT_TRIGGER_TESTER_SIMULATED",
                    "Simulated tester for project " + projectId, "", true, true);
            Map<String, String> testCtx = new HashMap<>();
            testCtx.put("projectTitle", projectTitle);
            testCtx.put("acceptanceCriteria", acSummary);
            String testRaw = llmAgentClient.execute("TEST_GENERATE", testCtx);
            TesterOutput testerOutput = parseOrFallback(testRaw, TesterOutput.class, "TEST_GENERATE");
            log.info("Tester ran {} tests, {} passed, {} failed", testerOutput.getTestsRun(), testerOutput.getTestsPassed(), testerOutput.getTestsFailed());

            TestResult testResult = new TestResult();
            testResult.setProjectId(projectId);
            testResult.setAgentRunId(0L);
            testResult.setCorrelationId(correlationId);
            testResult.setBranch(branch);
            testResult.setSimulation(true);
            testResult.setTestsRun(testerOutput.getTestsRun());
            testResult.setTestsPassed(testerOutput.getTestsPassed());
            testResult.setTestsFailed(testerOutput.getTestsFailed());
            testResult.setTestsSkipped(testerOutput.getTestsSkipped());
            testResult.setCoveragePercent(testerOutput.getCoveragePercent());
            testResult.setStatus(testerOutput.getOverallStatus());
            try {
                testResult.setFailuresJson(mapper.writeValueAsString(testerOutput.getFailures()));
            } catch (Exception e) {
                testResult.setFailuresJson("[]");
            }
            testResult = testResultRepository.save(testResult);

            trs = new TestResultSummary();
            trs.setId(testResult.getId());
            trs.setTestsRun(testResult.getTestsRun());
            trs.setTestsPassed(testResult.getTestsPassed());
            trs.setTestsFailed(testResult.getTestsFailed());
            trs.setCoveragePercent(testResult.getCoveragePercent());
            trs.setStatus(testResult.getStatus());
            steps.add(new StepResult("TESTER", "COMPLETED", true, "evt_test_" + projectId));

            // ── Step 5: Compliance Agent ──
            auditLogService.logAuthAction(null, "AGENT_TRIGGER_COMPLIANCE_SIMULATED",
                    "Simulated compliance for project " + projectId, "", true, true);
            Map<String, String> compCtx = new HashMap<>();
            compCtx.put("projectTitle", projectTitle);
            compCtx.put("requirements", requirementsSummary);
            String compRaw = llmAgentClient.execute("COMPLIANCE_CHECK", compCtx);
            ComplianceOutput compOutput = parseOrFallback(compRaw, ComplianceOutput.class, "COMPLIANCE_CHECK");
            log.info("Compliance: status={}, severity={}", compOutput.getOverallStatus(), compOutput.getSeverity());

            ComplianceResult complianceResult = new ComplianceResult();
            complianceResult.setProjectId(projectId);
            complianceResult.setAgentRunId(0L);
            complianceResult.setCorrelationId(correlationId);
            complianceResult.setBranch(branch);
            complianceResult.setSimulation(true);
            complianceResult.setOverallStatus(compOutput.getOverallStatus() != null ? compOutput.getOverallStatus() : "pass");
            complianceResult.setSeverity(compOutput.getSeverity() != null ? compOutput.getSeverity() : "LOW");
            try {
                complianceResult.setChecksPassedJson(mapper.writeValueAsString(compOutput.getChecksPassed()));
                complianceResult.setChecksFailedJson(mapper.writeValueAsString(compOutput.getChecksFailed()));
            } catch (Exception e) {
                complianceResult.setChecksPassedJson("[]");
                complianceResult.setChecksFailedJson("[]");
            }
            complianceResult = complianceResultRepository.save(complianceResult);

            crs = new ComplianceResultSummary();
            crs.setId(complianceResult.getId());
            crs.setOverallStatus(complianceResult.getOverallStatus());
            crs.setSeverity(complianceResult.getSeverity());
            steps.add(new StepResult("COMPLIANCE", "COMPLETED", true, "evt_comp_" + projectId));

            // ── Step 6: Gate Evaluation ──
            GateEvaluation testGate = qualityGateService.evaluateTestGate(projectId, correlationId, testResult, true, branch);
            GateEvaluation compGate = qualityGateService.evaluateComplianceGate(projectId, correlationId, complianceResult, true, branch);
            qualityGateService.evaluateCoordinatedGate(projectId, correlationId, testGate, compGate, true, branch);
            gateStatus = qualityGateService.getGateStatus(projectId, branch);

        } catch (DataAccessException e) {
            dbError = true;
            dbErrorMsg = "DB persistence failure during sprint steps 4-6: " + e.getMessage();
            log.error(dbErrorMsg, e);
        }

        // ── Build rich Response ──
        SimulatedSprintResponse response = new SimulatedSprintResponse();
        response.setProjectId(projectId);
        response.setBranch(branch);
        response.setChangeDescription(request.getChangeDescription());
        response.setCorrelationId(correlationId);
        response.setSteps(steps);
        response.setGateStatus(gateStatus);
        response.setTestResult(trs);
        response.setComplianceResult(crs);
        response.setDbError(dbError);
        response.setDbErrorMessage(dbErrorMsg);


        BaSummary baSummary = new BaSummary();
        baSummary.setRequirementCount(baOutput.getRequirements().size());
        baSummary.setRequirementsSummary(requirementsSummary);
        baSummary.setAcceptanceCriteriaCount((int) baOutput.getRequirements().stream()
                .flatMap(r -> r.getOpenQuestions().stream()).count());

        response.setBaSummary(baSummary);

        ArchitectSummary archSummary = new ArchitectSummary();
        archSummary.setComponentCount(archOutput.getComponents().size());
        archSummary.setComponentsSummary(componentSummary);
        archSummary.setFeasible(archOutput.isFeasible());
        archSummary.setRisks(archRisksSummary);
        response.setArchitectSummary(archSummary);

        DevSummary devSummary = new DevSummary();
        devSummary.setFileChangeCount(devOutput.getFilesToChange().size());
        devSummary.setFilesSummary(fileChangeSummary);
        devSummary.setRationale(devOutput.getRationale());
        devSummary.setRiskLevel(devOutput.getRiskLevel());
        response.setDevSummary(devSummary);

        log.info("SimulatedSprint complete for project {}: {} reqs, {} files, gates {}",
                projectId, baOutput.getRequirements().size(), devOutput.getFilesToChange().size(),
                gateStatus.isOverallPass() ? "PASSED" : "FAILED");

        return response;
    }

    /**
     * Parse JSON into the target DTO. On any error, log and return an empty safe instance.
     */
    private <T> T parseOrFallback(String json, Class<T> type, String stepName) {
        try {
            T parsed = mapper.readValue(json, type);
            if (parsed != null) return parsed;
        } catch (Exception e) {
            log.warn("Failed to parse {} output: {}. Using safe fallback.", stepName, e.getMessage());
        }
        // Return an empty instance via default constructor
        try {
            return type.getDeclaredConstructor().newInstance();
        } catch (Exception ex) {
            throw new RuntimeException("Cannot create fallback instance for " + type.getSimpleName(), ex);
        }
    }
}
