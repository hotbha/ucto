package com.ucto.backend.service;

import com.ucto.backend.dto.SimulatedSprintRequest;
import com.ucto.backend.dto.SimulatedSprintResponse;
import com.ucto.backend.entity.Project;
import com.ucto.backend.repository.ComplianceResultRepository;
import com.ucto.backend.repository.TestResultRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for SimulatedSprintService.
 * Verifies that the sprint flow triggers expected LLM calls, persists results,
 * and computes gate status.
 */
@ExtendWith(MockitoExtension.class)
class SimulatedSprintServiceTest {

    @Mock
    private ProjectService projectService;
    @Mock
    private RepoWorkspaceService repoWorkspaceService;
    @Mock
    private AuditLogService auditLogService;
    @Mock
    private TestResultRepository testResultRepository;
    @Mock
    private ComplianceResultRepository complianceResultRepository;
    @Mock
    private QualityGateService qualityGateService;

    private StubLLMAgentClient llmClient;
    private SimulatedSprintService service;

    @Captor
    private ArgumentCaptor<com.ucto.backend.entity.TestResult> testResultCaptor;
    @Captor
    private ArgumentCaptor<com.ucto.backend.entity.ComplianceResult> complianceResultCaptor;

    @BeforeEach
    void setUp() {
        // Create a PromptCatalog and inject test prompts
        PromptCatalog catalog = new PromptCatalog();
        java.util.Map<String, PromptCatalog.PromptEntry> prompts = new java.util.HashMap<>();
        prompts.put("BA_REQUIREMENTS", new PromptCatalog.PromptEntry(
                "BA_REQUIREMENTS", "You are a BA for {{projectTitle}}", "{}"));
        prompts.put("ARCHITECT_DESIGN", new PromptCatalog.PromptEntry(
                "ARCHITECT_DESIGN", "You are an architect", "{}"));
        prompts.put("DEV_IMPLEMENT", new PromptCatalog.PromptEntry(
                "DEV_IMPLEMENT", "You are a dev", "{}"));
        prompts.put("TEST_GENERATE", new PromptCatalog.PromptEntry(
                "TEST_GENERATE", "You are a tester", "{}"));
        prompts.put("COMPLIANCE_CHECK", new PromptCatalog.PromptEntry(
                "COMPLIANCE_CHECK", "You are compliance", "{}"));
        org.springframework.test.util.ReflectionTestUtils.setField(catalog, "prompts", prompts);

        llmClient = new StubLLMAgentClient();
        llmClient.promptCatalog = catalog;

        service = new SimulatedSprintService();
        service.projectService = projectService;
        service.repoWorkspaceService = repoWorkspaceService;
        service.auditLogService = auditLogService;
        service.llmAgentClient = llmClient;
        service.testResultRepository = testResultRepository;
        service.complianceResultRepository = complianceResultRepository;
        service.qualityGateService = qualityGateService;
    }

    @Test
    void testRunSprint_projectNotFound_throwsException() {
        when(projectService.getProjectById(1L)).thenReturn(null);

        SimulatedSprintRequest req = new SimulatedSprintRequest("main", "Add profile page");
        assertThrows(IllegalArgumentException.class, () -> service.runSprint(1L, req));
    }

    @Test
    void testRunSprint_emptyDescription_throwsException() {
        SimulatedSprintRequest req = new SimulatedSprintRequest("main", "");
        assertThrows(IllegalArgumentException.class, () -> service.runSprint(1L, req));
    }

    @Test
    void testRunSprint_happyPath() {
        Project project = new Project();
        project.setId(1L);
        project.setTitle("Test App");
        project.setRepoUrl("https://github.com/org/repo.git");
        project.setRepoBranch("main");

        when(projectService.getProjectById(1L)).thenReturn(project);
        when(testResultRepository.save(any())).thenAnswer(i -> {
            var tr = i.<com.ucto.backend.entity.TestResult>getArgument(0);
            tr.setId(100L);
            return tr;
        });
        when(complianceResultRepository.save(any())).thenAnswer(i -> {
            var cr = i.<com.ucto.backend.entity.ComplianceResult>getArgument(0);
            cr.setId(200L);
            return cr;
        });

        // Mock quality gate service to return a gate evaluation when evaluateTestGate is called
        when(qualityGateService.evaluateTestGate(anyLong(), anyString(), any(), anyBoolean(), anyString()))
                .thenAnswer(i -> {
                    var ge = new com.ucto.backend.entity.GateEvaluation();
                    ge.setId(10L);
                    ge.setGateType("TEST");
                    ge.setPassed(true);
                    ge.setSimulation(true);
                    return ge;
                });
        when(qualityGateService.evaluateComplianceGate(anyLong(), anyString(), any(), anyBoolean(), anyString()))
                .thenAnswer(i -> {
                    var ge = new com.ucto.backend.entity.GateEvaluation();
                    ge.setId(11L);
                    ge.setGateType("COMPLIANCE");
                    ge.setPassed(true);
                    ge.setSimulation(true);
                    return ge;
                });
        when(qualityGateService.getGateStatus(anyLong(), anyString()))
                .thenAnswer(i -> {
                    var status = new com.ucto.backend.service.QualityGateService.GateStatusDTO();
                    status.setOverallPass(true);
                    return status;
                });

        SimulatedSprintRequest req = new SimulatedSprintRequest("main", "Add user profile page with edit");
        SimulatedSprintResponse response = service.runSprint(1L, req);

        assertNotNull(response);
        assertEquals(1L, response.getProjectId());
        assertEquals("main", response.getBranch());
        assertEquals("Add user profile page with edit", response.getChangeDescription());
        assertNotNull(response.getCorrelationId());
        assertTrue(response.getCorrelationId().startsWith("sim_sprint_"));

        // Verify 5 steps
        assertEquals(5, response.getSteps().size());
        assertEquals("BA", response.getSteps().get(0).getAgent());
        assertEquals("ARCHITECT", response.getSteps().get(1).getAgent());
        assertEquals("DEVELOPER", response.getSteps().get(2).getAgent());
        assertEquals("TESTER", response.getSteps().get(3).getAgent());
        assertEquals("COMPLIANCE", response.getSteps().get(4).getAgent());
        assertTrue(response.getSteps().get(0).isSimulation());
        assertTrue(response.getSteps().get(0).getStatus().contains("COMPLETED"));

        // Verify test result summary
        assertNotNull(response.getTestResult());
        assertTrue(response.getTestResult().getTestsRun() > 0);

        // Verify compliance result summary
        assertNotNull(response.getComplianceResult());
        assertNotNull(response.getComplianceResult().getOverallStatus());

        // Verify gate status computed
        assertNotNull(response.getGateStatus());
        assertTrue(response.getGateStatus().isOverallPass());

        // Verify audit log calls
        verify(auditLogService, times(5)).logAuthAction(
                isNull(), anyString(), anyString(), anyString(), eq(true), eq(true));

        // Verify TestResult and ComplianceResult persisted
        verify(testResultRepository).save(testResultCaptor.capture());
        assertTrue(testResultCaptor.getValue().isSimulation());
        assertEquals("main", testResultCaptor.getValue().getBranch());

        verify(complianceResultRepository).save(complianceResultCaptor.capture());
        assertTrue(complianceResultCaptor.getValue().isSimulation());

        // Verify quality gates evaluated
        verify(qualityGateService).evaluateTestGate(
                eq(1L), anyString(), any(), eq(true), eq("main"));
        verify(qualityGateService).evaluateComplianceGate(
                eq(1L), anyString(), any(), eq(true), eq("main"));
        verify(qualityGateService).getGateStatus(eq(1L), eq("main"));
    }

    @Test
    void testRunSprint_defaultsBranchToMain() {
        Project project = new Project();
        project.setId(2L);
        project.setTitle("Another App");
        when(projectService.getProjectById(2L)).thenReturn(project);
        when(testResultRepository.save(any())).thenAnswer(i -> {
            var tr = i.<com.ucto.backend.entity.TestResult>getArgument(0);
            tr.setId(101L); return tr;
        });
        when(complianceResultRepository.save(any())).thenAnswer(i -> {
            var cr = i.<com.ucto.backend.entity.ComplianceResult>getArgument(0);
            cr.setId(201L); return cr;
        });
        when(qualityGateService.evaluateTestGate(anyLong(), anyString(), any(), anyBoolean(), anyString()))
                .thenAnswer(i -> { var ge = new com.ucto.backend.entity.GateEvaluation(); ge.setPassed(true); return ge; });
        when(qualityGateService.evaluateComplianceGate(anyLong(), anyString(), any(), anyBoolean(), anyString()))
                .thenAnswer(i -> { var ge = new com.ucto.backend.entity.GateEvaluation(); ge.setPassed(true); return ge; });
        when(qualityGateService.getGateStatus(anyLong(), anyString()))
                .thenAnswer(i -> new com.ucto.backend.service.QualityGateService.GateStatusDTO());

        SimulatedSprintRequest req = new SimulatedSprintRequest(null, "Fix login bug");
        SimulatedSprintResponse response = service.runSprint(2L, req);

        assertEquals("main", response.getBranch());
    }
}
